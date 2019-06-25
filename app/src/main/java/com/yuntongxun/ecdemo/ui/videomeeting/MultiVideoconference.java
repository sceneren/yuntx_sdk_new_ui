/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yuntongxun.ecdemo.ui.videomeeting;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.ActionSheetDialog;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.dialog.ECListDialog;
import com.yuntongxun.ecdemo.common.utils.CommomUtil;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.CCPAlertDialog;
import com.yuntongxun.ecdemo.ui.adapter.VideoMetAdapter;
import com.yuntongxun.ecdemo.ui.voip.VoIPCallHelper;
import com.yuntongxun.ecsdk.CameraInfo;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager;
import com.yuntongxun.ecsdk.ECMeetingManager.ECCreateMeetingParams;
import com.yuntongxun.ecsdk.ECMeetingManager.ECMeetingType;
import com.yuntongxun.ecsdk.ECMeetingManager.OnCreateOrJoinMeetingListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnDeleteMeetingListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnMemberVideoFrameChangedListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnQueryMeetingMembersListener;
import com.yuntongxun.ecsdk.ECMeetingManager.OnSelfVideoFrameChangedListener;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.ECVoipAccount;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.meeting.ECMeetingMember;
import com.yuntongxun.ecsdk.meeting.ECVideoMeetingMember;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingDeleteMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingExitMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingJoinMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMemberForbidOpt;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingRejectMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingRemoveMemberMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingVideoFrameActionMsg;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.yuntongxun.ecdemo.R.id.account;
import static com.yuntongxun.ecdemo.ui.meeting.VoiceMeetingActivity.REQUEST_CODE_INVITE_BY_PHONECALL;
import static com.yuntongxun.ecdemo.ui.meeting.VoiceMeetingActivity.REQUEST_CODE_KICK_MEMBER;

/**
 * @author luhuashan 视频会议聊天界面
 *         加入，创建，接听后刷新ui   #{refreshUIAfterjoinSucces}
 *         <p>
 *         成功加入后  adapter刷新和请求成像，缓存，否则执行，adapter删除，取消成像，去掉缓存
 */
public class MultiVideoconference extends VideoconferenceBaseActivity implements
        View.OnClickListener, CCPMulitVideoUI.OnVideoUIItemClickListener,
        CCPAlertDialog.OnPopuItemClickListener {

    public static final String VIDEOCONFERENCEID = "videoconferenceid";
    public static final String ISVIDEOCONCREATE = "isvideoconcreate";
    private ImageView mIv_left;
    private TextView mTv_right;
    private TextView mTv_note;
    private TextView mTv_name;
    private TextView mTv_time_tag;
    private RecyclerView mRv_member;

    private LinearLayout mWindowsRemoteLayout;

    private VideoMetAdapter voideoMetAdapter;

    //底部
    private TextView mTv_microphone;
    private TextView mTv_exit;
    private TextView mTv_speaker;

    /**
     * The definition of video conference pattern of joining if the creator or
     * join Invitation model
     *
     * @see #modeType
     * @see #MODE_VIDEO_C_INITIATED_INTERCOM
     */
    private static final int MODE_VIDEO_C_INVITATION = 0x0;

    /**
     * Creator pattern model
     *
     * @see #modeType
     * @see #MODE_VIDEO_C_INVITATION
     */
    private static final int MODE_VIDEO_C_INITIATED_INTERCOM = 0x1;

    /**
     * Unique identifier defined message queue
     *
     * @see #getBaseHandle()
     */
    public static final int WHAT_ON_VIDEO_NOTIFY_TIPS = 0X2;

    /**
     * Unique identifier defined message queue
     *
     * @see #getBaseHandle()
     */
    public static final int WHAT_ON_VIDEO_REFRESH_VIDEOUI = 0X3;

    /**
     * The definition of the status bar at the top of the transition time to
     * update the state background
     */
    public static final int ANIMATION_DURATION = 2000;

    /**
     * The definition of the status bar at the top of the transition time to
     * update the state background
     */
    public static final int ANIMATION_DURATION_RESET = 1000;

    /**
     *
     */
    public static final String PREFIX_LOCAL_VIDEO = "local_";

    protected static final String TAG = "MultiVideoconference";

//    public HashMap<String, Integer> mVideoMemberUI;


    private String mVideoMainScreenVoIP;
    private String mVideoConferenceId;
    private String mMakeCallId;
    private String mVideoCreate;
    private int modeType;
    //    private boolean isMute = false;
    private boolean isVideoConCreate = false;
    private boolean isVideoChatting = false;

//    private OverflowAdapter.OverflowItem[] mItems;

    // Whether to display all the members including frequency

    private int mCameraCapbilityIndex;
    private TextView mTv_camera;
    private SubVideoSurfaceView mainsurfaceview;
    private ECCaptureView ecCaptureView;

    private Chronometer chronometer;

    boolean isBackgroud = false;
    private ECHandlerHelper handlerHelper;
    private boolean isActivityCreat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isActivityCreat = true;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        EventBus.getDefault().register(this);
        handlerHelper = new ECHandlerHelper();


        bindViews();
        initialize(savedInstanceState);

        voideoMetAdapter = new VideoMetAdapter(MultiVideoconference.this);
        mRv_member.setAdapter(voideoMetAdapter);
        voideoMetAdapter.notifyDataSetChanged();

        if (isVideoConCreate) {//如果是创建者
            mTv_right.setVisibility(View.VISIBLE);
        } else {
            mTv_right.setVisibility(View.GONE);
        }


        ECVoIPSetupManager voIPSetupManager = ECDevice.getECVoIPSetupManager();
        if (voIPSetupManager == null) {

            finish();
            return;
        }
        CameraInfo[] cameraInfos = ECDevice.getECVoIPSetupManager().getCameraInfos();

        if (cameraInfos != null) {
            for (int i = 0; i < cameraInfos.length; i++) {
                if (cameraInfos[i].index == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {

                    mCameraCapbilityIndex = CommomUtil.comportCapabilityIndex(cameraInfos[cameraInfos[i].index].caps, 352 * 288);
                }
            }
        }

        // 设置本地预览图像
        ECDevice.getECVoIPSetupManager().setVideoView(ecCaptureView);

        ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);


        voideoMetAdapter.setListener(new VideoMetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position, final MultiVideoMember ecVideoMeetingMember) {
                if (ecVideoMeetingMember.getNumber().equals("add@yuntongxun.com")) {

                    if (TextUtils.isEmpty(mVideoConferenceId)) {
                        ToastUtil.showMessage("mVideoConferenceId   空");
                        return;
                    }
                    // 外呼电话邀请加入会议
                    Intent callByPhone = new Intent(MultiVideoconference.this, VideoInviteAct.class);
                    callByPhone.putExtra(VIDEOCONFERENCEID, mVideoConferenceId);
                    startActivityForResult(callByPhone, REQUEST_CODE_INVITE_BY_PHONECALL);

                } else if (ecVideoMeetingMember.getNumber().equals("del@yuntongxun.com")) {
                    //删除
                    if (TextUtils.isEmpty(mVideoConferenceId)) {
                        return;
                    }
                    Intent intent = new Intent(MultiVideoconference.this, VideoMeetingMemberManager.class);
                    intent.putExtra(VIDEOCONFERENCEID, mVideoConferenceId);
                    intent.putExtra(VideoconferenceConversation.CONFERENCE_CREATOR, mVideoCreate);
                    startActivityForResult(intent, REQUEST_CODE_KICK_MEMBER);
                } else if (ecVideoMeetingMember.getNumber().equals("mute@yuntongxun.com")) {
                    //全员静音
                } else {

                    if (TextUtils.equals(CCPAppManager.getUserId(), mVideoCreate)) {//当前登录是创建者
                        if (TextUtils.equals(mVideoCreate, ecVideoMeetingMember.getNumber())) {//自己
                            new ActionSheetDialog(MultiVideoconference.this)
                                    .builder()
                                    .setTitle(ecVideoMeetingMember.getNumber())
                                    .setCancelable(false)
                                    .setCanceledOnTouchOutside(false)
                                    .addSheetItem(mPubish ? "取消发布视频" : "发布视频", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            publishOrCancleVideo();
                                        }
                                    })
                                    .show();
                        } else {
                            new ActionSheetDialog(MultiVideoconference.this)
                                    .builder()
                                    .setTitle(ecVideoMeetingMember.getNumber())
                                    .setCancelable(false)
                                    .setCanceledOnTouchOutside(false)
                                    .addSheetItem("设置禁言", ActionSheetDialog.SheetItemColor.Blue,
                                            new ActionSheetDialog.OnSheetItemClickListener() {
                                                @Override
                                                public void onClick(int which) {
                                                    opertionControl(ECMeetingManager.ECSpeakListenType.MUTE_ON, ecVideoMeetingMember);
                                                }
                                            })
                                    .addSheetItem("取消禁言", ActionSheetDialog.SheetItemColor.Blue,
                                            new ActionSheetDialog.OnSheetItemClickListener() {
                                                @Override
                                                public void onClick(int which) {
                                                    opertionControl(ECMeetingManager.ECSpeakListenType.MUTE_OFF, ecVideoMeetingMember);
                                                }
                                            })
                                    .addSheetItem("设置禁听", ActionSheetDialog.SheetItemColor.Blue,
                                            new ActionSheetDialog.OnSheetItemClickListener() {
                                                @Override
                                                public void onClick(int which) {
                                                    opertionControl(ECMeetingManager.ECSpeakListenType.LISTEN_ON, ecVideoMeetingMember);
                                                }
                                            })
                                    .addSheetItem("取消禁听", ActionSheetDialog.SheetItemColor.Blue,
                                            new ActionSheetDialog.OnSheetItemClickListener() {
                                                @Override
                                                public void onClick(int which) {
                                                    opertionControl(ECMeetingManager.ECSpeakListenType.LISTEN_OFF, ecVideoMeetingMember);
                                                }
                                            })

                                    .show();
                        }

                    } else {//不是创建者
                        if (TextUtils.equals(CCPAppManager.getUserId(), ecVideoMeetingMember.getNumber())) {//自己
                            new ActionSheetDialog(MultiVideoconference.this)
                                    .builder()
                                    .setTitle(ecVideoMeetingMember.getNumber())
                                    .setCancelable(false)
                                    .setCanceledOnTouchOutside(false)
                                    .addSheetItem(mPubish ? "取消发布视频" : "发布视频", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            publishOrCancleVideo();
                                        }
                                    })
                                    .show();
                        }

                    }
                }
            }
        });
    }

    /**
     * 设置禁言，禁听等
     *
     * @param type
     * @param ecVoiceMeetingMember
     */
    private void opertionControl(ECMeetingManager.ECSpeakListenType type, MultiVideoMember ecVoiceMeetingMember) {
//
        ECVoipAccount voipAccount = new ECVoipAccount();


        String s = ecVoiceMeetingMember.getNumber();
        if(s!=null&&s.startsWith("m")){
            voipAccount.setAccount(s.substring(1,s.length()));
            voipAccount.setIsVoip(false);
        }else {
            voipAccount.setIsVoip(true);
            voipAccount.setAccount(ecVoiceMeetingMember.getNumber());
        }
        ECDevice.getECMeetingManager().setMemberSpeakListen(voipAccount, type, mVideoConferenceId
                , ECMeetingManager.ECMeetingType.MEETING_MULTI_VOICE, listener);
    }


    private boolean mPubish = true;

    /**
     * 如果是发布状态、就取消发布、反之亦然
     */
    public void publishOrCancleVideo() {
        if (mPubish) {
            if (!checkSDK()) {
                return;
            }
            showProcessDialog();
            ECDevice.getECMeetingManager()
                    .cancelPublishSelfVideoFrameInVideoMeeting(
                            mVideoConferenceId,
                            new OnSelfVideoFrameChangedListener() {

                                public void onSelfVideoFrameChanged(
                                        boolean isPublish, ECError reason) {
                                    dismissPostingDialog();

                                    if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                                        mPubish = false;
                                    } else {
                                        ToastUtil.showMessage("操作失败,错误码"
                                                + reason.errorCode);
                                    }

                                }
                            });

        } else {
            if (!checkSDK()) {
                return;
            }
            showProcessDialog();

            ECDevice.getECMeetingManager()
                    .publishSelfVideoFrameInVideoMeeting(
                            mVideoConferenceId,
                            new OnSelfVideoFrameChangedListener() {

                                public void onSelfVideoFrameChanged(
                                        boolean isPublish, ECError reason) {
                                    dismissPostingDialog();

                                    if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                                        mPubish = true;
                                    } else {
                                        ToastUtil.showMessage("操作失败");
                                    }

                                }
                            });

        }
    }

    @Override
    public int getTitleLayout() {
        return -1;
    }

//    void initOverflowItems() {
//
//        if (mItems == null) {
//            mItems = new OverflowAdapter.OverflowItem[6];
//        }
//
//        mItems[0] = new OverflowAdapter.OverflowItem(
//                getString(R.string.videomeeting_invite_voip));
//        mItems[1] = new OverflowAdapter.OverflowItem(getString(R.string.videomeeting_invite_phone));
//        mItems[2] = new OverflowAdapter.OverflowItem("禁言");
//        mItems[3] = new OverflowAdapter.OverflowItem("可讲");
//        mItems[4] = new OverflowAdapter.OverflowItem("禁听");
//        mItems[5] = new OverflowAdapter.OverflowItem("可听");
//
//
//    }



	    /*
     * Exceptions found during parsing
     *
     * References a layout (@layout/layout_bottom_control)
     */


    private void bindViews() {

        mIv_left = (ImageView) findViewById(R.id.iv_left);
        mTv_right = (TextView) findViewById(R.id.tv_right);
        mTv_note = (TextView) findViewById(R.id.tv_note);
        mTv_name = (TextView) findViewById(R.id.tv_name);
        mTv_time_tag = (TextView) findViewById(R.id.tv_time_tag);
        ecCaptureView = (ECCaptureView) findViewById(R.id.captureView);


        chronometer = (Chronometer) findViewById(R.id.chronometer);
        mWindowsRemoteLayout = (LinearLayout) findViewById(R.id.video_remote_windows);


        mRv_member = (android.support.v7.widget.RecyclerView) findViewById(R.id.rv_member);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRv_member.setLayoutManager(linearLayoutManager);

        mIv_left.setOnClickListener(this);
        mTv_right.setOnClickListener(this);

        //底部
        mTv_microphone = (TextView) findViewById(R.id.tv_microphone);
        mTv_exit = (TextView) findViewById(R.id.tv_exit);
        mTv_speaker = (TextView) findViewById(R.id.tv_speaker);
        mTv_camera = (TextView) findViewById(R.id.tv_camera);

        mTv_microphone.setOnClickListener(this);
        mTv_exit.setOnClickListener(this);
        mTv_speaker.setOnClickListener(this);
        mTv_camera.setOnClickListener(this);

        mainsurfaceview = (SubVideoSurfaceView) findViewById(R.id.mainsurfaceview);


    }

//    private void initMicrophone() {
//
//
//        Drawable drawableHigh = getResources().getDrawable(R.drawable.maikefeng);
//        Drawable drawableNormal = getResources().getDrawable(R.drawable.maikefeng_normal);
//
//        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
//        if (setupManager == null) {
//            return;
//
//        }
//        if (setupManager.getMuteStatus()) {
//            drawableNormal.setBounds(0, 0, drawableHigh.getIntrinsicWidth(), drawableHigh.getIntrinsicHeight());
//            mTv_microphone.setCompoundDrawables(null, drawableNormal, null, null);
//        } else {
//            drawableHigh.setBounds(0, 0, drawableHigh.getIntrinsicWidth(), drawableHigh.getIntrinsicHeight());
//            mTv_microphone.setCompoundDrawables(null, drawableHigh, null, null);
//        }
//    }

    private void initSpeakerMode() {
        Drawable drawableHigh = getResources().getDrawable(R.drawable.mianti_workb);
        Drawable drawableNormal = getResources().getDrawable(R.drawable.mianti_normal);

        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager == null) {
            return;
        }
        boolean speakerOn = setupManager.getLoudSpeakerStatus();

        if (speakerOn) {
            drawableHigh.setBounds(0, 0, drawableHigh.getIntrinsicWidth(), drawableHigh.getIntrinsicHeight());
            mTv_speaker.setCompoundDrawables(null, drawableHigh, null, null);
            mTv_speaker.setTextColor(Color.parseColor("#118bfc"));
        } else {
            drawableNormal.setBounds(0, 0, drawableNormal.getIntrinsicWidth(), drawableNormal.getIntrinsicHeight());
            mTv_speaker.setCompoundDrawables(null, drawableNormal, null, null);
            mTv_speaker.setTextColor(Color.parseColor("#ffffff"));
        }

    }


//    private OverflowHelper mOverflowHelper;
//
//    private final AdapterView.OnItemClickListener mOverflowItemClicKListener = new AdapterView.OnItemClickListener() {
//
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position,
//                                long id) {
//            if (position == 0 || position == 1) {
//                controlPlusSubMenu();
//
//                showInputCodeDialog(null,
//                        getString(R.string.videomeeting_invite_member), position != 0);
//            } else {
//                controlPlusSubMenu();
//                handleSetMemberState(position);
//            }
//
//        }
//
//    };

    private void handleSetMemberState(final int position) {

        final String[] arr = getMembers();
        if (arr == null || arr.length == 0) {
            return;
        }
        ECListDialog dialog = new ECListDialog(this, arr);
        dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
            @Override
            public void onDialogItemClick(Dialog d, int sposition) {
                handleContentMenuClick(sposition, position, arr);
            }


        });
        dialog.setTitle("操作");
        dialog.show();


    }

    final ECMeetingManager.OnSetMemberSpeakListenListener listener = new ECMeetingManager.OnSetMemberSpeakListenListener() {
        @Override
        public void onSetMemberSpeakListenResult(ECError error, String meetingNum) {

            if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                ToastUtil.showMessage("设置成功");
            } else {
                ToastUtil.showMessage("设置失败");

            }
        }
    };

    private void handleContentMenuClick(int sposition, int position, String[] arr) {

        MultiVideoMember member = null;
        for (MultiVideoMember item : mulitMembers) {
            if (item.getNumber().equalsIgnoreCase(arr[sposition])) {
                member = item;
                break;
            }
        }
        ECVoipAccount voipAccount = new ECVoipAccount();
        voipAccount.setIsVoip(!member.isMobile());

        String s = member.getNumber();
        if(s!=null&&s.startsWith("m")){
            voipAccount.setAccount(s.substring(1,s.length()));
        }else {
            voipAccount.setAccount(member.getNumber());
        }

        ECMeetingManager.ECSpeakListenType type = ECMeetingManager.ECSpeakListenType.MUTE_ON;
        if (position == 2) {
            type = ECMeetingManager.ECSpeakListenType.MUTE_ON;
        } else if (position == 3) {
            type = ECMeetingManager.ECSpeakListenType.MUTE_OFF;

        } else if (position == 4) {

            type = ECMeetingManager.ECSpeakListenType.LISTEN_ON;
        } else if (position == 5) {
            type = ECMeetingManager.ECSpeakListenType.LISTEN_OFF;

        }
        ECDevice.getECMeetingManager().setMemberSpeakListen(voipAccount, type, mVideoConferenceId, ECMeetingManager.ECMeetingType.MEETING_MULTI_VOICE, listener);


    }


    private void initialize(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String roomName = null;
        boolean is_auto_close = true;
        int autoDelete = 1;
        int voiceMode = 1;
        if (intent.hasExtra(ECGlobalConstants.AUTO_DELETE)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                autoDelete = extras.getInt(ECGlobalConstants.AUTO_DELETE);
            }
        }
        if (intent.hasExtra(ECGlobalConstants.VOICE_MOD)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                voiceMode = extras.getInt(ECGlobalConstants.VOICE_MOD);
            }
        }

        if (intent.hasExtra(ECGlobalConstants.IS_AUTO_CLOSE)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                is_auto_close = extras
                        .getBoolean(ECGlobalConstants.IS_AUTO_CLOSE);
            }
        }
        if (intent.hasExtra(ECGlobalConstants.CHATROOM_NAME)) {//列表跳转过来的
            modeType = MODE_VIDEO_C_INITIATED_INTERCOM;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                roomName = extras.getString(ECGlobalConstants.CHATROOM_NAME);
                if (TextUtils.isEmpty(roomName)) {
                    finish();
                } else {
                    mVideoCreate = extras
                            .getString(VideoconferenceConversation.CONFERENCE_CREATOR);
                    isVideoConCreate = CCPAppManager.getUserId().equals(
                            mVideoCreate);
//					if (!isVideoConCreate && instructionsView != null)
//						instructionsView.setVisibility(View.GONE);
                    mTv_name.setText("房间名称：" + roomName);
                    setHostPeople();
                }
            }
        }

        if (intent.hasExtra(ECGlobalConstants.CONFERENCE_ID)) {//中途被邀请加入
            // To invite voice group chat
            modeType = MODE_VIDEO_C_INVITATION;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mVideoConferenceId = extras.getString(ECGlobalConstants.CONFERENCE_ID);
                mMakeCallId = extras.getString("com.voice.demo.ccp.VIDEO_CALL_INVITE");
                if (TextUtils.isEmpty(mVideoConferenceId)) {
                    finish();
                    return;
                }
            }

        }


        /**
         * 预览摄像头
         */
        ECDevice.getECVoIPSetupManager().setVideoView(mainsurfaceview.getVideoSurfaceView(), null);
        if (modeType == MODE_VIDEO_C_INITIATED_INTERCOM) {// 自动创建、加入

            ECCreateMeetingParams.Builder builder = new ECCreateMeetingParams.Builder();
            builder.setMeetingName(roomName).setSquare(5)
                    .setVoiceMod(getToneMode(voiceMode))
                    .setIsAutoDelete(autoDelete == 1 ? true : false)
                    .setIsAutoJoin(true).setKeywords("").setMeetingPwd("")
                    .setIsAutoClose(is_auto_close);
            ECCreateMeetingParams params = builder.create();

            if (!checkSDK()) {
                return;
            }

            /**
             * 创建视屏会议
             */
            ECDevice.getECMeetingManager().createMultiMeetingByType(params,
                    ECMeetingType.MEETING_MULTI_VIDEO,
                    new OnCreateOrJoinMeetingListener() {

                        @Override
                        public void onCreateOrJoinMeeting(ECError reason,
                                                          String meetingNo) {

                            if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                                mVideoConferenceId = meetingNo;

                                refreshUIAfterjoinSuccess(reason, meetingNo);
                            } else {

                                ToastUtil.showMessage("加入会议失败reason="
                                        + reason.errorCode);
                                finish();
                            }
                        }
                    });

        } else if (modeType == MODE_VIDEO_C_INVITATION) {// 加入会议
            if (mMakeCallId != null) {
                showInComingMeeting();
                return;
            }
            checkSDK();
            ECDevice.getECMeetingManager().joinMeetingByType(
                    mVideoConferenceId, "", ECMeetingType.MEETING_MULTI_VIDEO,
                    new OnCreateOrJoinMeetingListener() {

                        public void onCreateOrJoinMeeting(ECError arg0,
                                                          String arg1) {
                            // TODO Auto-generated method stub
                            if (arg0.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

                                ECDevice.getECVoIPSetupManager()
                                        .enableLoudSpeaker(true);
                                refreshUIAfterjoinSuccess(arg0, arg1);
                            } else {
                                ToastUtil.showMessage("加入会议失败reason="
                                        + arg0.errorCode);
                                finish();
                            }
                        }
                    });
        }


    }

    private void setHostPeople() {
        mTv_note.setText("主持人：  " + AvatorUtil.getInstance().getMarkName(mVideoCreate));
    }

    /**
     * 邀请加入会议提示
     */
    private void showInComingMeeting() {
        String msg = getString(R.string.meeting_incoming_accept, mVideoConferenceId);
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, msg,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initCallEvent();
                        /**
                         * 拒绝加入
                         */
                        ECDevice.getECVoIPCallManager().rejectCall(mMakeCallId, SdkErrorCode.REMOTE_CALL_BUSY);
                        finish();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initCallEvent();

                        ECDevice.getECVoIPCallManager().acceptCall(mMakeCallId);

                        if (chronometer != null) {
                            chronometer.setBase(SystemClock.elapsedRealtime());
                            chronometer.start();
                        }
                    }
                });

        buildAlert.setTitle(R.string.app_tip);
        buildAlert.show();

    }

    private void initCallEvent() {
        VoIPCallHelper.setOnCallEventNotifyListener(new VoIPCallHelper.OnCallEventNotifyListener() {
            @Override
            public void onCallProceeding(String callId) {

            }

            @Override
            public void onMakeCallback(ECError arg0, String arg1, String arg2) {

            }

            @Override
            public void onCallAlerting(String callId) {

            }

            @Override
            public void onCallAnswered(String callId) {
                ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
                // 这里刷新会议界面
                refreshUIAfterjoinSuccess(new ECError(SdkErrorCode.REQUEST_SUCCESS, ""), mVideoConferenceId);
            }

            @Override
            public void onMakeCallFailed(String callId, int reason) {
            }

            @Override
            public void onCallReleased(String callId) {
                // 这里需要主动调用停止请求视频成员图像接口
                // 关键就是这里的问题
                if (mulitMembers != null) {
                    for (MultiVideoMember member : mulitMembers) {
                        ECDevice.getECMeetingManager().cancelRequestMemberVideoInVideoMeeting(mVideoConferenceId, "", member.getNumber(), null);
                    }
                }
            }

            @Override
            public void onVideoRatioChanged(VideoRatio videoRatio) {

            }
        });
    }


    @SuppressWarnings("unchecked")
    protected void refreshUIAfterjoinSuccess(ECError reason, String conferenceId) {

        if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

            isVideoChatting = true;
            mVideoConferenceId = conferenceId;

            ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);

            initMute();

            initSpeakerMode();

            if (!checkSDK()) {
                return;
            }
            querymembers(true);

            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();

        } else {
            isVideoChatting = false;
            checkSDK();
            ECDevice.getECMeetingManager().exitMeeting(
                    ECMeetingType.MEETING_MULTI_VIDEO);

            ToastUtil.showMessage(R.string.str_join_video_c_failed_content);
            finish();
        }

    }

    private void querymembers(final boolean b) {
        ECDevice.getECMeetingManager().queryMeetingMembersByType(
                mVideoConferenceId, ECMeetingType.MEETING_MULTI_VIDEO,
                new OnQueryMeetingMembersListener() {
                    @Override
                    public void onQueryMeetingMembers(ECError reason,
                                                      List members) {
                        if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

                            if (mulitMembers == null) {
                                mulitMembers = new ArrayList<MultiVideoMember>();
                            }
                            if (members == null || members.size() <= 0) {
                                return;
                            }
                            mulitMembers.clear();
                            ArrayList<ECVideoMeetingMember> membersNew = (ArrayList<ECVideoMeetingMember>) members;
                            for (ECVideoMeetingMember member : membersNew) {
                                MultiVideoMember mulitMember = new MultiVideoMember(
                                        member);
                                if (member.getType() == ECMeetingMember.Type.SPONSOR) {
                                    mVideoCreate = member.getNumber();
                                    setHostPeople();
                                }
                                mulitMembers.add(mulitMember);
                            }
                            //members 界面
                            voideoMetAdapter.setDatas(mulitMembers, isVideoConCreate);

                            //视屏界面
                            initMembersOnVideoUI(mulitMembers);
                        }
                    }
                });
    }

    private void initMute() {

        Drawable drawableHigh = getResources().getDrawable(R.drawable.maikefeng);
        Drawable drawableNormal = getResources().getDrawable(R.drawable.maikefeng_normal);

        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager == null) {
            return;
        }
        if (setupManager.getMuteStatus()) {
            drawableNormal.setBounds(0, 0, drawableNormal.getIntrinsicWidth(), drawableNormal.getIntrinsicHeight());
            mTv_microphone.setCompoundDrawables(null, drawableNormal, null, null);

            mTv_microphone.setTextColor(Color.parseColor("#ffffff"));
        } else {
            drawableHigh.setBounds(0, 0, drawableHigh.getIntrinsicWidth(), drawableHigh.getIntrinsicHeight());
            mTv_microphone.setCompoundDrawables(null, drawableHigh, null, null);
            mTv_microphone.setTextColor(Color.parseColor("#118bfc"));
        }
    }

    private void setMuteUI() {

        try {
            ECDevice.getECVoIPSetupManager().setMute(!ECDevice.getECVoIPSetupManager().getMuteStatus());
            initMute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ArrayList<MultiVideoMember> mulitMembers = new ArrayList<>();

    private ECAlertDialog buildAlert;


    private boolean isVideoUIMemberExist(String who) {
        synchronized (mVidyoFrames) {
            if (TextUtils.isEmpty(who)) {
                return false;
            }

            if (mVidyoFrames.containsKey(who)) {
                return true;
            }

            return false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();


        handlerHelper.postRunnOnThead(new Runnable() {
            @Override
            public void run() {
                ecCaptureView.setCaptureParams(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT, mCameraCapbilityIndex);
                ecCaptureView.onResume();
            }
        });

        lockScreen();
    }

    @Override
    protected boolean isEnableSwipe() {
        // TODO Auto-generated method stub
        return false;
    }

    public void exitOrDismissVideoConference(boolean dismiss) {
        if (dismiss && isVideoConCreate) {
            if (!checkSDK()) {
                return;
            }
            showCustomProcessDialog(getString(R.string.common_progressdialog_title));
            ECDevice.getECMeetingManager().deleteMultiMeetingByType(
                    ECMeetingType.MEETING_MULTI_VIDEO, mVideoConferenceId,
                    new OnDeleteMeetingListener() {

                        public void onMeetingDismiss(ECError reason,
                                                     String meetingNo) {
                            // TODO Auto-generated method stub
                            dismissPostingDialog();
                            removeMemberFormVideoUI(CCPAppManager.getUserId(), mVidyoFrames.get(account));
                        }
                    });
        } else {
            if (mMakeCallId != null) {
                ECDevice.getECVoIPCallManager().releaseCall(mMakeCallId);
                finish();
                return;
            }
            checkSDK();

//            initMembersOnVideoUI(mulitMembers);
            ECDevice.getECMeetingManager().exitMeeting(
                    ECMeetingType.MEETING_MULTI_VIDEO);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    for(MultiVideoMember item:mulitMembers){

                        if(CCPAppManager.getUserId().equalsIgnoreCase(item.getNumber())){
                            continue;
                        }
                        ECDevice.getECMeetingManager().cancelRequestMemberVideoInVideoMeeting(mVideoConferenceId, "", item.getNumber(), new OnMemberVideoFrameChangedListener() {
                            @Override
                            public void onMemberVideoFrameChanged(boolean b, ECError ecError, String s, String s1) {

                            }
                        });
                    }

                }
            }).start();

            if (!isVideoConCreate && dismiss) {
                Intent disIntent = new Intent(
                        ECGlobalConstants.INTENT_VIDEO_CONFERENCE_DISMISS);
                disIntent.putExtra(ECGlobalConstants.CONFERENCE_ID,
                        mVideoConferenceId);
                sendBroadcast(disIntent);
            }

        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseLockScreen();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                dissolveMetting();
                break;
            case R.id.iv_left:
                exitOrDismissVideoConference(false);
                break;
            //底部
            case R.id.tv_microphone:
                setMuteUI();
                break;
            case R.id.tv_exit:
                exitOrDismissVideoConference(false);
                break;
            case R.id.tv_speaker:
                // 更改当前的扬声器模式
                changeSpeakerOnMode();
                break;

            case R.id.tv_camera:
                mTv_camera.setEnabled(false);
                ecCaptureView.switchCamera();
                isBackgroud = !isBackgroud;

                Drawable drawableHigh = getResources().getDrawable(R.drawable.camera);
                Drawable drawableNormal = getResources().getDrawable(R.drawable.cameranormal);

                if (isBackgroud) {
                    mTv_camera.setTextColor(Color.parseColor("#ffffff"));
                    drawableNormal.setBounds(0, 0, drawableNormal.getIntrinsicWidth(), drawableNormal.getIntrinsicHeight());
                    mTv_camera.setCompoundDrawables(null, drawableNormal, null, null);
                } else {
                    drawableHigh.setBounds(0, 0, drawableHigh.getIntrinsicWidth(), drawableHigh.getIntrinsicHeight());
                    mTv_camera.setCompoundDrawables(null, drawableHigh, null, null);
                    mTv_camera.setTextColor(Color.parseColor("#118bfc"));
                }

                mTv_camera.setEnabled(true);
                break;

            default:
                break;
        }
    }

    /**
     * 解散会议
     */
    private void dissolveMetting() {
        if (!TextUtils.isEmpty(mVideoConferenceId)) {
            if (!checkSDK()) {
                return;
            }
            showCustomProcessDialog(getString(R.string.common_progressdialog_title));
            ECDevice.getECMeetingManager().deleteMultiMeetingByType(
                    ECMeetingType.MEETING_MULTI_VIDEO,
                    mVideoConferenceId,
                    new OnDeleteMeetingListener() {

                        public void onMeetingDismiss(ECError reason,
                                                     String meetingNo) {
                            dismissPostingDialog();
                            finish();
                        }
                    });
        }
    }

    /**
     * 更改当前的扬声器模式
     */
    private void changeSpeakerOnMode() {
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager == null) {
            return;
        }
        boolean speakerOn = setupManager.getLoudSpeakerStatus();
        setupManager.enableLoudSpeaker(!speakerOn);

        boolean mSpeakerOn = setupManager.getLoudSpeakerStatus();
        if (mSpeakerOn) {
            ToastUtil.showMessage(R.string.fmt_route_speaker);
        } else {
            ToastUtil.showMessage(R.string.fmt_route_phone);
        }
        initSpeakerMode();
    }

    public void showIOSAlert(String content) {

        buildAlert = ECAlertDialog.buildAlert(this, content,
                getString(R.string.dialog_btn_confim),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        buildAlert.dismiss();
                    }
                });
        buildAlert.setTitle("提示");
        buildAlert.setCanceledOnTouchOutside(true);
        buildAlert.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isActivityCreat = false;

        EventBus.getDefault().unregister(this);

        chronometer.stop();


        if (mVidyoFrames != null) {
//            mVideoConUI.release();
            mVidyoFrames = null;
        }
//

        mVideoConferenceId = null;
        mVideoCreate = null;

        // The first rear facing camera
        isVideoConCreate = false;
        isVideoChatting = false;
        /**
         * 静音
         */
        ECDevice.getECVoIPSetupManager().enableLoudSpeaker(false);
    }

    /**
     * 图像点击
     */
    @Override
    public void onVideoUIItemClick(int CCPUIKey) {
//
//        int[] ccpAlertResArray = new int[2];
//        int title = 0;
//        if (CCPUIKey == CCPMulitVideoUI.LAYOUT_KEY_SUB_SURFACEVIEW) {
//            // If he is the create of Video Conference
//            // The main object is not myself
//            if (!mPubish) {
//                ccpAlertResArray[0] = R.string.video_publish_video_frame;
//            } else {
//                ccpAlertResArray[0] = R.string.video_unpublish_video_frame;
//            }
//            if (isVideoConCreate) {
//                ccpAlertResArray[1] = R.string.video_c_dismiss;
//            } else {
//                ccpAlertResArray[1] = R.string.video_c_logout;
//            }
//        } else {
//            String who = getVideoVoIPByCCPUIKey(CCPUIKey);
//            MultiVideoMember multiVideoMember = getMultiVideoMember(who);
//            if (multiVideoMember == null) {
//                return;
//            }
//
//            ccpAlertResArray = new int[2];
//            if (multiVideoMember.isRequestVideoFrame()) {
//                ccpAlertResArray[0] = R.string.str_quxiao_sp;
//            } else {
//                ccpAlertResArray[0] = R.string.str_request_sp;
//            }
//            if (who.equals(mVideoMainScreenVoIP)) {
//                ccpAlertResArray[1] = R.string.str_xiao;
//            } else {
//                ccpAlertResArray[1] = R.string.str_da;
//            }
//            if (isVideoConCreate) {
//                int[] _arr = new int[]{ccpAlertResArray[0],
//                        ccpAlertResArray[1], R.string.str_video_manager_remove};
//                ccpAlertResArray = _arr;
//            }
//        }
//        CCPAlertDialog ccpAlertDialog = new CCPAlertDialog(
//                MultiVideoconference.this, title, ccpAlertResArray, 0,
//                R.string.dialog_cancle_btn);
//
//        // set CCP UIKey
//        ccpAlertDialog.setUserData(CCPUIKey);
//        ccpAlertDialog.setOnItemClickListener(this);
//        ccpAlertDialog.create();
//        ccpAlertDialog.show();
    }

    /**
     * 根据name获取member
     */
    public MultiVideoMember getMultiVideoMember(String who) {

        if (mulitMembers == null) {
            return null;
        }
        for (MultiVideoMember member : mulitMembers) {
            if (member != null && who.equals(member.getNumber())) {
                return member;
            }
        }
        return null;
    }

    public void onItemClick(ListView parent, View view, int position,
                            int resourceId) {
        switch (resourceId) {
            case R.string.video_c_logout:
                exitOrDismissVideoConference(false);
                break;
            case R.string.video_c_dismiss:
                exitOrDismissVideoConference(isVideoConCreate);
                break;
            case R.string.video_publish_video_frame:
            case R.string.video_unpublish_video_frame:

                if (mPubish) {// 如果是发布状态、就取消发布、反之亦然

                    if (!checkSDK()) {
                        return;
                    }
                    showProcessDialog();
                    ECDevice.getECMeetingManager()
                            .cancelPublishSelfVideoFrameInVideoMeeting(
                                    mVideoConferenceId,
                                    new OnSelfVideoFrameChangedListener() {

                                        public void onSelfVideoFrameChanged(
                                                boolean isPublish, ECError reason) {
                                            dismissPostingDialog();

                                            if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                                                mPubish = false;
                                            } else {
                                                ToastUtil.showMessage("操作失败,错误码"
                                                        + reason.errorCode);
                                            }

                                        }
                                    });

                } else {
                    if (!checkSDK()) {
                        return;
                    }
                    showProcessDialog();

                    ECDevice.getECMeetingManager()
                            .publishSelfVideoFrameInVideoMeeting(
                                    mVideoConferenceId,
                                    new OnSelfVideoFrameChangedListener() {

                                        public void onSelfVideoFrameChanged(
                                                boolean isPublish, ECError reason) {
                                            dismissPostingDialog();

                                            if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                                                mPubish = true;
                                            } else {
                                                ToastUtil.showMessage("操作失败");
                                            }

                                        }
                                    });

                }

                break;

            case R.string.dialog_cancle_btn:

                break;
        }
    }

    /**
     * 视频会议图像缓存
     */
    private HashMap<String, SubVideoSurfaceView> mVidyoFrames = new HashMap<String, SubVideoSurfaceView>(4);

    /**
     * RequestVideoFrame
     * <p>
     * requestMemberVideo或者cancelRequestMemberVideo
     *
     * @param multiVideoMember
     */
    private void doHandlerMemberVideoFrameRequest(final MultiVideoMember multiVideoMember, final SubVideoSurfaceView surfaceLayout) {
        if (multiVideoMember != null
                && mVideoConferenceId.length() > 8) {
            if (!multiVideoMember.isRequestVideoFrame()) {
                surfaceLayout.setVideoUIMember(multiVideoMember);
                requestMemberVideo(multiVideoMember, surfaceLayout);
            } else {
                cancleMemberVideo(multiVideoMember, surfaceLayout);
            }
        }
    }

    private void requestMemberVideo(final MultiVideoMember multiVideoMember, final SubVideoSurfaceView surfaceLayout) {
        if (!checkSDK()) {
            return;
        }
        int result = ECDevice.getECMeetingManager().requestMemberVideoInVideoMeeting(
                mVideoConferenceId, "", multiVideoMember.getNumber(), surfaceLayout.getVideoSurfaceView(),
                multiVideoMember.getIp(), multiVideoMember.getPort(),
                new OnMemberVideoFrameChangedListener() {

                    @Override
                    public void onMemberVideoFrameChanged(boolean isRequest, ECError reason, String meetingNo, String account) {
                        if (isActivityCreat) {
                            obtainVideoFrameChange(multiVideoMember, isRequest, reason, account, surfaceLayout);
                        }
                        LogUtil.e("reason" + reason.toString());
                    }

                });
        LogUtil.e("result======" + result);
    }

    private void cancleMemberVideo(final MultiVideoMember multiVideoMember, final SubVideoSurfaceView surfaceLayout) {
        checkSDK();
        if (multiVideoMember == null) {
            return;
        }
        int ret = ECDevice.getECMeetingManager().cancelRequestMemberVideoInVideoMeeting(
                mVideoConferenceId, "",
                multiVideoMember.getNumber(),
                new OnMemberVideoFrameChangedListener() {

                    @Override
                    public void onMemberVideoFrameChanged(boolean isRequest, ECError reason, String meetingNo, String account) {
                        obtainVideoFrameChange(multiVideoMember, isRequest, reason, account, surfaceLayout);
                    }
                });
        if (ret == 0) {
            multiVideoMember.setRequestVideoFrame(false);
        }
    }

    /**
     * 根据账号缓存当前的图像显示控件
     *
     * @param account 账号
     * @param iFrame  图像显示控件
     */
    private void putVidyoFrame(String account, SubVideoSurfaceView iFrame) {
        ViewGroup parent = (ViewGroup) iFrame.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        mWindowsRemoteLayout.addView(iFrame);
        synchronized (MultiVideoconference.class) {
            if (mVidyoFrames != null) {
                mVidyoFrames.put(account, iFrame);
            }
        }
    }

    /**
     * 视频会议成员图像操作结果回调接口
     *
     * @param multiVideoMember 成员
     * @param isRequest        是否请求成员图像
     * @param reason           结果错误码
     * @param account          成员账号
     * @param surfaceLayout
     */
    private void obtainVideoFrameChange(MultiVideoMember multiVideoMember, boolean isRequest, ECError reason, String account, SubVideoSurfaceView surfaceLayout) {
        if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS || reason.errorCode == 0) {
            multiVideoMember.setRequestVideoFrame(isRequest);
            if (isRequest) {
                ToastUtil.showMessage("成员[" + account + "]视频图像请求成功");
                putVidyoFrame(multiVideoMember.getNumber(), surfaceLayout);
            } else {
                removeMemberFormVideoUI(account, surfaceLayout);
            }
            return;
        }
        // 请求失败/取消失败
        multiVideoMember.setRequestVideoFrame(!isRequest);
        String errMsg = isRequest ? "成员[" + account + "]视频图像请求失败" : "成员[" + account + "]视频图像取消失败";
        ToastUtil.showMessage(errMsg);
        removeMemberFormVideoUI(account, surfaceLayout);
    }

    private void clearScreen(SurfaceView surfaceView) {
        if (surfaceView == null) {
            return;
        }
        Paint p = new Paint();
        // 清屏
        p.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        Canvas lockCanvas = surfaceView.getHolder().lockCanvas();
        if (lockCanvas == null) {
            return;
        }
        lockCanvas.drawPaint(p);
        p.setXfermode(new PorterDuffXfermode(Mode.SRC));
        surfaceView.getHolder().unlockCanvasAndPost(lockCanvas);
        surfaceView.invalidate();
    }

    /**
     * @param view
     * @param multiVideoMember
     */
    private void doChangeVideoFrameSurfaceViewRequest(SurfaceView view, MultiVideoMember multiVideoMember) {
        if (multiVideoMember != null) {
            if (mVideoConferenceId.length() > 8) {
                String id = mVideoConferenceId.substring(mVideoConferenceId.length() - 8, mVideoConferenceId.length());
                if (multiVideoMember.isRequestVideoFrame()) {
                    SurfaceView surfaceView = view;
                    //surfaceView.getHolder().setFixedSize( multiVideoMember.getWidth(), multiVideoMember.getHeight());

                    if (!checkSDK()) {
                        return;
                    }

                    ECDevice.getECMeetingManager().resetVideoMeetingWindow(multiVideoMember.getNumber(), surfaceView);
                }

            }

        }
    }

    /**
     * <p>
     * Title: doVideoConferenceDisconnect
     * </p>
     * <p>
     * Description: The end of processing video conference popu menu list
     * </p>
     *
     * @see CCPAlertDialog#CCPAlertDialog(Context, int, int[], int, int)
     */
    private void doVideoConferenceDisconnect() {
        int videoTips = R.string.video_c_logout_warning_tip;
        int videoExit = R.string.video_c_logout;

        CCPAlertDialog ccpAlertDialog = new CCPAlertDialog(
                MultiVideoconference.this, videoTips, null, videoExit,
                R.string.dialog_cancle_btn);
        ccpAlertDialog.setOnItemClickListener(this);
        ccpAlertDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        ccpAlertDialog.create();
        ccpAlertDialog.show();
    }


    /**
     * <p>
     * Title: removeMemberFormVideoUI
     * </p>
     * <p>
     * Description: remove the member of Video Conference form VideoUI
     * </p>
     */
    private void removeMemberFormVideoUI(String voip, SubVideoSurfaceView subVideoSurfaceView) {
        mWindowsRemoteLayout.removeView(subVideoSurfaceView);
        mVidyoFrames.remove(voip);
    }

    private void removeMemberMulitAddapterCache(String who) {
        MultiVideoMember removeMember = getMember(who);
        if (removeMember != null) {
            mulitMembers.remove(removeMember);
            voideoMetAdapter.setDatas(mulitMembers, isVideoConCreate);
        }
    }


    public MultiVideoMember getMember(String who) {
        if (mulitMembers == null) {
            return null;
        }
        MultiVideoMember removeMember = null;
        for (MultiVideoMember multiVideoMember : mulitMembers) {
            if (multiVideoMember != null
                    && multiVideoMember.getNumber().equals(who)) {
                removeMember = multiVideoMember;
                break;
            }
        }
        return removeMember;
    }

    /**
     * 成员集合转name数组
     *
     * @return
     */
    public String[] getMembers() {
        if (mulitMembers == null || mulitMembers.size() <= 0) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        for (MultiVideoMember item : mulitMembers) {
            if (item != null) {
                if (!item.getNumber().equalsIgnoreCase(CCPAppManager.getUserId())) {
                    list.add(item.getNumber());
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 会议成员
     *
     * @param members
     */
    private void initMembersOnVideoUI(List<MultiVideoMember> members) {
        synchronized (this) {
            for (int i = 0; (i < 5) && i < members.size(); i++) {
                final MultiVideoMember member = members.get(i);
                if (CCPAppManager.getUserId().equals(member.getNumber())) {
                    continue;
                }
                final SubVideoSurfaceView surfaceLayout = getSurfaceLayout(i);
                if (surfaceLayout == null) {
                    return;
                }
//                doHandlerMemberVideoFrameRequest(member, surfaceLayout);

                surfaceLayout.setVideoUIMember(member);
                requestMemberVideo(member, surfaceLayout);

            }
        }
    }


    /**
     * 需要显示视频的控件
     *
     * @param index 角标
     * @return 图像View
     */
    public SubVideoSurfaceView getSurfaceLayout(int index) {
        if (index >= 5) {
            return null;
        }
        SubVideoSurfaceView videoView = new SubVideoSurfaceView(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                getScreenWidth() / 4, getScreenWidth() / 4);

        videoView.setLayoutParams(layoutParams);

        int padding = px2dip(this, 2);
        videoView.setPadding(padding, padding, padding, padding);
        return videoView;
    }

    //获取屏幕的宽度
    public int getScreenWidth() {
        WindowManager manager = (WindowManager)
                getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }


    /**
     * 像素转换成手机屏幕密度
     *
     * @param pxValue 像素
     * @return 密度
     */
    public static int px2dip(Context ctx, float pxValue) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        return Math.round(displayMetrics.densityDpi * pxValue / 160.0F);
    }

    @Subscribe
    public void onEventMainThread(EventRemovedMembers event) {
        ArrayList<MultiVideoMember> removedMembers = event.getRemovedMembers();
        //刷新adapter Ui
        mulitMembers.removeAll(removedMembers);
        voideoMetAdapter.setDatas(mulitMembers, isVideoConCreate);

        //取消请求video
        for (MultiVideoMember member : removedMembers) {
            String who = member.getNumber();
            if (who.startsWith("m")) {
                who = who.substring(1, who.length());
                member.setNumber(who);
            }
            final SubVideoSurfaceView subVideoSurfaceView = mVidyoFrames.get(who);
            cancleMemberVideo(member, subVideoSurfaceView);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            if (isVideoChatting) {
                doVideoConferenceDisconnect();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void handleDialogOkEvent(int requestKey) {
        super.handleDialogOkEvent(requestKey);

        if (DIALOG_SHOW_KEY_DISSMISS_VIDEO == requestKey) {
            exitOrDismissVideoConference(true);
        } else if (DIALOG_SHOW_KEY_REMOVE_VIDEO == requestKey) {
            // The removed member of the video conference is self.
            exitOrDismissVideoConference(false);
        }
    }

    // -----------------------------------------------------SDK Callback
    // ---------------------------

    @Override
    protected void handleVideoConferenceDismiss(int reason, String conferenceId) {
        super.handleVideoConferenceDismiss(reason, conferenceId);

        dismissPostingDialog();

        if (reason != 0 && (reason != 111805)) {
            Toast.makeText(MultiVideoconference.this,
                    getString(R.string.toast_video_dismiss_result, reason),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        exitOrDismissVideoConference(false);
    }

    //视屏消息回调
    @Override
    protected void handleReceiveVideoConferenceMsg(ECVideoMeetingMsg VideoMsg) {
        super.handleReceiveVideoConferenceMsg(VideoMsg);
        synchronized (MultiVideoconference.class) {
            if (TextUtils.isEmpty(mVideoConferenceId)) {
                return;
            }
            if (VideoMsg == null
                    || !mVideoConferenceId.equals(VideoMsg.getMeetingNo())) {
                return;
            }

            if (VideoMsg instanceof ECVideoMeetingJoinMsg) {//加入会议
                handMsgJoinMetting((ECVideoMeetingJoinMsg) VideoMsg);
            } else if (VideoMsg instanceof ECVideoMeetingExitMsg) {//退出会议
                handExitMsg((ECVideoMeetingExitMsg) VideoMsg);
            } else if (VideoMsg instanceof ECVideoMeetingDeleteMsg) {//解散
                deleteMettingMsg((ECVideoMeetingDeleteMsg) VideoMsg);
            } else if (VideoMsg instanceof ECVideoMeetingRemoveMemberMsg) {//移除成员
                handRemoveMemberMes((ECVideoMeetingRemoveMemberMsg) VideoMsg);
            } else if (VideoMsg instanceof ECVideoMeetingVideoFrameActionMsg) {//取消或者发布的通知回调
                ECVideoMeetingVideoFrameActionMsg msg = (ECVideoMeetingVideoFrameActionMsg) VideoMsg;
                if (msg != null
                        && msg.getMember()
                        .equals(CCPAppManager.getUserId())) {
                    return;
                }
                if (msg.isPublish()) {
                    showIOSAlert(getString(
                            R.string.videomeeting_member_publish,
                            msg.getMember()));
                } else {
                    showIOSAlert(getString(
                            R.string.videomeeting_member_unpublish,
                            msg.getMember()));
                }
            } else if (VideoMsg instanceof ECVideoMeetingRejectMsg) {//拒绝
                onVideoMeetingRejectMsg((ECVideoMeetingRejectMsg) VideoMsg);
            } else if (VideoMsg instanceof ECVideoMeetingMemberForbidOpt) {//可听禁言的状态改变回调
                onReceSetMemberForbid((ECVideoMeetingMemberForbidOpt) VideoMsg);
            }
        }
    }

    private void handRemoveMemberMes(ECVideoMeetingRemoveMemberMsg VideoMsg) {
        ECVideoMeetingRemoveMemberMsg vCRemoveMemberMsg = VideoMsg;

        if (CCPAppManager.getUserId().equals(vCRemoveMemberMsg.getWho()) && !vCRemoveMemberMsg.isMobile()) {
            showAlertTipsDialog(
                    DIALOG_SHOW_KEY_REMOVE_VIDEO,
                    getString(R.string.str_system_message_remove_v_title),
                    getString(R.string.str_system_message_remove_v_message),
                    getString(R.string.dialog_btn), null);
        } else {
            String who;
            if (vCRemoveMemberMsg.isMobile()) {
                who = "m" + vCRemoveMemberMsg.getWho();
            } else {
                who = vCRemoveMemberMsg.getWho();
            }
            removeMemberMulitAddapterCache(who);
            removeMemberFormVideoUI(who, mVidyoFrames.get(account));
//            if (isVideoUIMemberExist(who)) {//有画面
//                SubVideoSurfaceView subVideoSurfaceView = mVidyoFrames.get(who);
//                MultiVideoMember removemember = getMember(who);
//                if (removemember != null
//                        && subVideoSurfaceView != null) {
//                    cancleMemberVideo(removemember, subVideoSurfaceView);
//                }
//            }
        }
    }

    private void deleteMettingMsg(ECVideoMeetingDeleteMsg VideoMsg) {
        if (isVideoConCreate) {
            return;
        }
        ECVideoMeetingDeleteMsg videoConferenceDismissMsg = VideoMsg;
        if (videoConferenceDismissMsg.getMeetingNo().equals(
                mVideoConferenceId)) {
            showAlertTipsDialog(
                    DIALOG_SHOW_KEY_DISSMISS_VIDEO,
                    getString(R.string.dialog_title_be_dissmiss_video_conference),
                    getString(R.string.dialog_message_be_dissmiss_video_conference),
                    getString(R.string.dialog_btn), null);
        }
    }

    private void handExitMsg(ECVideoMeetingExitMsg VideoMsg) {
        ECVideoMeetingExitMsg videoExitMessage = VideoMsg;
        if (!videoExitMessage.getMeetingNo().equals(mVideoConferenceId)) {
            return;
        }
        String[] whos = videoExitMessage.getWhos();
        boolean isCurrent = false;
        for (String who : whos) {
            if (videoExitMessage.isMobile()) {
                who = "m" + who;
            }


            MultiVideoMember member = getMember(who);
            SubVideoSurfaceView subVideoSurfaceView = mVidyoFrames.get(who);
            if (subVideoSurfaceView != null) {
//                cancleMemberVideo(member, subVideoSurfaceView);
                mWindowsRemoteLayout.removeView(subVideoSurfaceView);
                mVidyoFrames.remove(who);
            }
            removeMemberMulitAddapterCache(who);
            if (!isCurrent) {
                isCurrent = CCPAppManager.getUserId().equals(who);
            }
        }
        if (isCurrent) {
            finish();
        }
    }

    private void handMsgJoinMetting(ECVideoMeetingJoinMsg VideoMsg) {
        ECVideoMeetingJoinMsg videoJoinMessage = VideoMsg;
        String[] whos = videoJoinMessage.getWhos();

        for (String who : whos) {
            if (videoJoinMessage.isMobile()) {
                who = "m" + who;
            }
//            if (isVideoUIMemberExist(who)) {
//                continue;
//            }
            if (CCPAppManager.getUserId().equals(who)) {
                continue;
            }

            MultiVideoMember member = new MultiVideoMember();
            member.setNumber(who);
            member.setIp(videoJoinMessage.getIp());
            member.setPort(videoJoinMessage.getPort());
            member.setIsMobile(videoJoinMessage.isMobile());
            member.setPublish(videoJoinMessage.isPublish());

            mulitMembers.add(member);
            voideoMetAdapter.setDatas(mulitMembers, isVideoConCreate);
            voideoMetAdapter.notifyDataSetChanged();

            int index = 0;
            if (!mVidyoFrames.isEmpty()) {
                index = mVidyoFrames.size();
            }
            SubVideoSurfaceView surfaceLayout = getSurfaceLayout(index);
            if (surfaceLayout == null) {
                return;
            }

            surfaceLayout.setVideoUIMember(member);
            requestMemberVideo(member, surfaceLayout);
        }
    }

    private void onReceSetMemberForbid(ECVideoMeetingMemberForbidOpt videoMsg) {

        LogUtil.d(TAG, "onReceSetMemberForbid");


        if (videoMsg == null) {
            return;
        }
        ECVideoMeetingMemberForbidOpt.ForbidOptions options = videoMsg.getForbid();

        String text = null;
        boolean listen = options.canListen();
        boolean speak = options.canSpeak();
        if (listen) {
            text = "你的状态被设置为" + "可听" + (speak == true ? "可说" : "禁言");
        }
        if (speak) {
            text = "你的状态被设置为可说" + (listen == true ? "可听" : "不可听");
        }
        if (!speak && !listen) {
            text = "你的状态被设置为禁言禁听";
        }


        if (videoMsg.getWho().equalsIgnoreCase(CCPAppManager.getUserId())) {
            final ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, text
                    , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            buildAlert.setTitle(R.string.app_tip);
            buildAlert.show();
        }

    }

    private void onVideoMeetingRejectMsg(ECVideoMeetingRejectMsg msg) {
        if (msg == null) {
            return;
        }
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(MultiVideoconference.this
                , getString(R.string.meeting_invite_reject, msg.getWho()),
                getString(R.string.dialog_btn_confim), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        buildAlert.setTitle(R.string.app_tip);
        buildAlert.setCanceledOnTouchOutside(false);
        buildAlert.show();
    }

    @Override
    protected void handleSwitchRealScreenToVoip(int reason) {
        super.handleSwitchRealScreenToVoip(reason);
        dismissPostingDialog();
        if (reason != 0) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.str_video_switch_failed, reason),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void handleVideoConferenceRemoveMember(int reason, String member) {
        super.handleVideoConferenceRemoveMember(reason, member);
        dismissPostingDialog();
        if (reason != 0) {
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.str_video_remove_failed, member, reason),
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    protected void handleNotifyMessage(Message msg) {
        super.handleNotifyMessage(msg);

        int what = msg.what;
        if (what == WHAT_ON_VIDEO_NOTIFY_TIPS) {
//			mVideoTips.setText(getString(R.string.video_tips_joining,
//					mVideoConferenceId));
//			TransitionDrawable transition = (TransitionDrawable) mVideoTips
//					.getBackground();
//			transition.reverseTransition(ANIMATION_DURATION_RESET);

        }

    }

    /**
     * <p>
     * Title: MultiVideoconference.java
     * </p>
     * <p>
     * Description:
     * </p>
     * <p>
     * Copyright: Copyright (c) 2007
     * </p>
     * <p>
     * Company: http://www.cloopen.com
     * </p>
     *
     * @author zhanjichun
     * @version 1.0
     * @date 2013-11-7
     */
    class CCPFilenameFilter implements FilenameFilter {

        String fileName = null;

        public CCPFilenameFilter(String fileNoExtensionNoDot) {
            fileName = fileNoExtensionNoDot;
        }

        @Override
        public boolean accept(File dir, String filename) {

            return filename.startsWith(fileName);
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.mulit_video_conference;
    }

    @Override
    protected void handleVideoRatioChanged(String voip, int width, int height) {

        if (TextUtils.isEmpty(voip)) {
            return;
        }
        MultiVideoMember multiVideoMember = getMultiVideoMember(voip);
        if (multiVideoMember != null) {
            multiVideoMember.setWidth(width);
            multiVideoMember.setHeight(height);
        }

    }

}
