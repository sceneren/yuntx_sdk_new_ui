package com.yuntongxun.ecdemo.ui.meeting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.ActionSheetDialog;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.ui.adapter.VoiceMetAdapter;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.ECVoipAccount;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.meeting.ECMeeting;
import com.yuntongxun.ecsdk.meeting.ECMeetingMember;
import com.yuntongxun.ecsdk.meeting.ECVoiceMeetingMember;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingExitMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingJoinMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMemberForbidOpt;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingRejectMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingRemoveMemberMsg;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;
import static com.yuntongxun.ecdemo.R.id.tv_join_time;
import static com.yuntongxun.ecdemo.R.id.tv_right;

/**
 * 语音会议界面
 * com.yuntongxun.ecdemo.ui.meeting in ECDemo_Android
 * Created by Jorstin on 2015/7/17.
 */
public class VoiceMeetingActivity extends MeetingBaseActivity implements VoiceMeetingMicAnim.OnMeetingMicEnableListener {

    private RelativeLayout mRoot;
    private ImageView mIv_left;
    private TextView mTv_right;
    private TextView mTv_creat;
    private TextView mTv_note;
    private TextView mTv_name;
    private ListView mInter_phone_list;
    private Chronometer mTv_countdown_time;
    private android.support.v7.widget.RecyclerView mGv_inter_icon;

    //底部
    private TextView mTv_microphone;
    private TextView mTv_exit;
    private TextView mTv_speaker;

    private static final String TAG = "ECSDK_Demo.VoiceMeetingActivity";

    public static final String EXTRA_MEETING = "com.yuntongxun.ecdemo.Meeting";
    public static final String EXTRA_MEETING_PASS = "com.yuntongxun.ecdemo.Meeting_Pass";
    public static final String EXTRA_CALL_IN = "com.yuntongxun.ecdemo.Meeting_Join";
    /**
     * 管理会议成员
     */
    public static final int REQUEST_CODE_KICK_MEMBER = 0x001;
    /**
     * 外呼电话邀请加入会议
     */
    public static final int REQUEST_CODE_INVITE_BY_PHONECALL = 0x002;

    /**
     * 会议成员显示控件适配器
     */
    private MeetingMemberAdapter mMeetingMemberAdapter;

    /**
     * 语音会议参与状态动画
     */
    private VoiceMeetingCenter mVoiceCenter;
    /**
     * 语音会议底部Mic和声音振幅区域
     */
    private VoiceMeetingMicAnim mMeetingMic;
    /**
     * 创建会议需要的参数
     */
    private ECMeetingManager.ECCreateMeetingParams mParams;
    /**
     * 会议信息
     */
    private ECMeeting mMeeting;
    /**
     * 会议房间密码
     */
    private String mMeetingPassword;
    /**
     * 是否需要呼入会议
     */
    private boolean mMeetingCallin;
    /**
     * 会议成员
     */
    private ArrayList<ECVoiceMeetingMember> sMembers;
    /**
     * 是否是自己创建的会议房间
     */
    private boolean isSelfMeeting = false;
    /**
     * 会议房间是否已经被解散
     */
    private boolean isMeetingOver = false;
    private boolean isMeeting = false;
    /**
     * 是否是扬声器模式
     */
    private boolean mSpeakerOn = false;
    /***会议成员是否有自己*/
    private boolean hasSelf = false;



    private VoiceMetAdapter voiceMetAdapter;



    @Override
    protected int getLayoutId() {
        return R.layout.meeting_voice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        mMeeting = getIntent().getParcelableExtra(EXTRA_MEETING);
        mMeetingPassword = getIntent().getStringExtra(EXTRA_MEETING_PASS);
        mMeetingCallin = getIntent().getBooleanExtra(EXTRA_CALL_IN, false);
        isSelfMeeting = CCPAppManager.getUserId().equals(mMeeting.getCreator());

        initView();


        if (mMeeting == null) {
            LogUtil.e(TAG, " meeting error , meeting null");
            finish();
            return;
        }


        mTv_creat.setText(getCreatName());
        AvatorUtil.getInstance().setAvatorPhoto(mTv_creat, R.drawable.memer_bg, mMeeting.getCreator());


        mTv_note.setText("主持人： " + getCreatName());
        mTv_name.setText("房间名称：" + mMeeting.getMeetingName());

        if (mMeetingCallin) {
            // 判断是否需要调用加入接口加入会议
            MeetingHelper.joinMeeting(mMeeting.getMeetingNo(), mMeetingPassword);
            return;
        }
        isMeeting = true;
    }


    private String getCreatName() {
        String creatname = mMeeting.getCreator();
        return AvatorUtil.getInstance().getMarkName(creatname);
    }


    @Override
    public int getTitleLayout() {
        return -1;
    }

    /**
     * 初始化界面资源
     */
    private void initView() {



        mRoot = (RelativeLayout) findViewById(R.id.root);
        mIv_left = (ImageView) findViewById(R.id.iv_left);
        mTv_right = (TextView) findViewById(tv_right);
        mTv_right.setOnClickListener(this);
        mTv_creat = (TextView) findViewById(R.id.tv_creat);
        mTv_note = (TextView) findViewById(R.id.tv_note);
        mTv_name = (TextView) findViewById(R.id.tv_name);
        mInter_phone_list = (ListView) findViewById(R.id.inter_phone_list);
        mTv_countdown_time = (Chronometer) findViewById(R.id.tv_countdown_time);
        mGv_inter_icon = (android.support.v7.widget.RecyclerView) findViewById(R.id.gv_inter_icon);

        //底部
        mTv_microphone = (TextView) findViewById(R.id.tv_microphone);
        mTv_exit = (TextView) findViewById(R.id.tv_exit);
        mTv_speaker = (TextView) findViewById(R.id.tv_speaker);
        findViewById(R.id.tv_camera).setVisibility(View.GONE);
        mTv_microphone.setOnClickListener(this);
        mTv_exit.setOnClickListener(this);
        mTv_speaker.setOnClickListener(this);

        mTv_microphone.setTextColor(Color.parseColor("#768893"));
        mTv_exit.setTextColor(Color.parseColor("#768893"));
        mTv_speaker.setTextColor(Color.parseColor("#768893"));



//        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
//        if (setupManager == null) {
//            return;
//        }
//        setupManager.setMute(true);
//        setupManager.enableLoudSpeaker(true);
        initMicrophone();
        initSpeakerMode();

        if (isSelfMeeting) {
            mTv_right.setVisibility(View.VISIBLE);
        } else {
            mTv_right.setVisibility(View.GONE);
        }

        mMeetingMemberAdapter = new MeetingMemberAdapter(this);
        mInter_phone_list.setAdapter(mMeetingMemberAdapter);

        voiceMetAdapter = new VoiceMetAdapter(this, isSelfMeeting);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mGv_inter_icon.setLayoutManager(layoutManager);

        mGv_inter_icon.setAdapter(voiceMetAdapter);

        voiceMetAdapter.setListener(new VoiceMetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position, final ECVoiceMeetingMember ecVoiceMeetingMember) {
                if (ecVoiceMeetingMember.getNumber().equals("add@yuntongxun.com")) {

                    if (mMeeting == null && mMeeting.getMeetingNo() == null) {
                        return;
                    }
                    // 外呼电话邀请加入会议
                    Intent callByPhone = new Intent(VoiceMeetingActivity.this, InviteAct.class);
                    callByPhone.putExtra(ECDevice.MEETING_NO, mMeeting.getMeetingNo());
                    startActivityForResult(callByPhone, REQUEST_CODE_INVITE_BY_PHONECALL);

                } else if (ecVoiceMeetingMember.getNumber().equals("del@yuntongxun.com")) {
                    //删除
                    if (mMeeting == null && mMeeting.getMeetingNo() == null) {
                        return;
                    }
                    Intent intent = new Intent(VoiceMeetingActivity.this, VoiceMeetingMemberManager.class);
                    intent.putExtra(EXTRA_MEETING, mMeeting);
                    startActivityForResult(intent, REQUEST_CODE_KICK_MEMBER);
                } else if (ecVoiceMeetingMember.getNumber().equals("mute@yuntongxun.com")) {
                    //全员静音

                } else {

                    if (!isSelfMeeting || CCPAppManager.getUserId().equals(ecVoiceMeetingMember.getNumber())) {//只有管理才有相管禁言权限,点击管理员自己不弹出
                        return;
                    }
                    new ActionSheetDialog(VoiceMeetingActivity.this)
                            .builder()
                            .setTitle(ecVoiceMeetingMember.getNumber())
                            .setCancelable(false)
                            .setCanceledOnTouchOutside(false)
                            .addSheetItem("设置禁言", ActionSheetDialog.SheetItemColor.Blue,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            opertionControl(ECMeetingManager.ECSpeakListenType.MUTE_ON, ecVoiceMeetingMember);
                                        }
                                    })
                            .addSheetItem("取消禁言", ActionSheetDialog.SheetItemColor.Blue,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            opertionControl(ECMeetingManager.ECSpeakListenType.MUTE_OFF, ecVoiceMeetingMember);
                                        }
                                    })
                            .addSheetItem("设置禁听", ActionSheetDialog.SheetItemColor.Blue,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            opertionControl(ECMeetingManager.ECSpeakListenType.LISTEN_ON, ecVoiceMeetingMember);
                                        }
                                    })
                            .addSheetItem("取消禁听", ActionSheetDialog.SheetItemColor.Blue,
                                    new ActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            opertionControl(ECMeetingManager.ECSpeakListenType.LISTEN_OFF, ecVoiceMeetingMember);
                                        }
                                    })

                            .show();
                }
            }
        });
    }

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
        } else {
            drawableNormal.setBounds(0, 0, drawableNormal.getIntrinsicWidth(), drawableNormal.getIntrinsicHeight());
            mTv_speaker.setCompoundDrawables(null, drawableNormal, null, null);
        }

    }

    private void initMicrophone() {


        Drawable drawableHigh = getResources().getDrawable(R.drawable.maikefeng);
        Drawable drawableNormal = getResources().getDrawable(R.drawable.maikefeng_normal);

        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager == null) {
            return;
        }

        if (setupManager.getMuteStatus()) {
            drawableHigh.setBounds(0, 0, drawableHigh.getIntrinsicWidth(), drawableHigh.getIntrinsicHeight());
            mTv_microphone.setCompoundDrawables(null, drawableHigh, null, null);
        } else {
            drawableNormal.setBounds(0, 0, drawableNormal.getIntrinsicWidth(), drawableNormal.getIntrinsicHeight());
            mTv_microphone.setCompoundDrawables(null, drawableNormal, null, null);
        }
    }

    /**
     * 设置禁言，禁听等
     *
     * @param type
     * @param ecVoiceMeetingMember
     */
    private void opertionControl(ECMeetingManager.ECSpeakListenType type, ECVoiceMeetingMember ecVoiceMeetingMember) {
//
        ECVoipAccount voipAccount = new ECVoipAccount();
        voipAccount.setIsVoip(!ecVoiceMeetingMember.isMobile());
        voipAccount.setAccount(ecVoiceMeetingMember.getNumber());
        ECDevice.getECMeetingManager().setMemberSpeakListen(voipAccount, type, mMeeting.getMeetingNo()
                , ECMeetingManager.ECMeetingType.MEETING_MULTI_VOICE, listener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            doTitleLeftAction();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeAmplitude(true);


        if (isMeeting) {
            MeetingHelper.queryMeetingMembers(mMeeting.getMeetingNo());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        changeAmplitude(false);
    }

    private boolean isNeedGetData = true;

    @Override
    public void onMeetingStart(String meetingNo) {
        super.onMeetingStart(meetingNo);
        isMeeting = true;
        // 加入会议成功
        hasSelf = false;
        MeetingHelper.queryMeetingMembers(meetingNo);

        changeAmplitude(true);

        mTv_countdown_time.setBase(SystemClock.elapsedRealtime());
        mTv_countdown_time.start();

    }

    @Override
    protected boolean isEnableSwipe() {
        // TODO Auto-generated method stub
        return false;
    }

    private void changeAmplitude(boolean enbale) {
        if (!isMeeting && enbale) {
            return;
        }
        if (mVoiceCenter != null) {
            if (enbale) {
                mVoiceCenter.startAmplitude();
            } else {
                mVoiceCenter.stopAmplitude();
            }
        }
        if (mMeetingMic != null) {
            if (enbale) {
                mMeetingMic.startMicAmpl();
            } else {
                mMeetingMic.stopMicAmpl();
            }
        }
    }

    /**
     * 初始化下拉菜单
     */
//    void initOverflowItems() {
//        int size = isSelfMeeting ? 9 : 1;
//
//        if (mItems == null) {
//            mItems = new OverflowAdapter.OverflowItem[size];
//        }
//        if (isSelfMeeting) {//是管理员
//
//            mItems[0] = new OverflowAdapter.OverflowItem(getString(R.string.pull_invited_phone_member));
//            mItems[1] = new OverflowAdapter.OverflowItem(getString(R.string.videomeeting_invite_voip));
//            if (mSpeakerOn) {
//                mItems[2] = new OverflowAdapter.OverflowItem(getString(R.string.pull_mode_earpiece));
//            } else {
//                mItems[2] = new OverflowAdapter.OverflowItem(getString(R.string.pull_mode_speaker));
//            }
//            mItems[3] = new OverflowAdapter.OverflowItem(getString(R.string.pull_manager_member));
//            mItems[4] = new OverflowAdapter.OverflowItem(getString(R.string.pull_dissolution_room));
//            mItems[5] = new OverflowAdapter.OverflowItem("禁言");
//            mItems[6] = new OverflowAdapter.OverflowItem("可讲");
//            mItems[7] = new OverflowAdapter.OverflowItem("禁听");
//            mItems[8] = new OverflowAdapter.OverflowItem("可听");
//        } else {
//            if (mSpeakerOn) {
//                mItems[0] = new OverflowAdapter.OverflowItem(getString(R.string.pull_mode_earpiece));
//
//            } else {
//                mItems[0] = new OverflowAdapter.OverflowItem(getString(R.string.pull_mode_speaker));
//            }
//        }
//    }

//    /**
//     * 当前下拉菜单点击事件
//     *
//     * @param position
//     */
//    void doOverflowActionMenuClick(final int position) {
//        if (!isSelfMeeting) {
//            // 如果不是创建者，则只有切换听筒扬声器模式
//            if (position == 0) {
//                // 更改当前的扬声器模式
//                changeSpeakerOnMode();
//            }
//            return;
//        }
//
//        if (position == 0 || position == 1) {
//            if (mMeeting == null && mMeeting.getMeetingNo() == null) {
//                return;
//            }
//            // 外呼电话邀请加入会议
//            Intent callByPhone = new Intent(VoiceMeetingActivity.this, InviteByPhoneCall.class);
//
//            callByPhone.putExtra(ECDevice.MEETING_NO, mMeeting.getMeetingNo());
//            callByPhone.putExtra("isLandingCall", position == 0);
//            startActivityForResult(callByPhone, REQUEST_CODE_INVITE_BY_PHONECALL);
//        } else if (position == 2) {
//            // 更改当前的扬声器模式
//            changeSpeakerOnMode();
//        } else if (position == 3) {
//            if (mMeeting == null && mMeeting.getMeetingNo() == null) {
//                return;
//            }
//            // 管理会议成员操作
//            Intent intent = new Intent(VoiceMeetingActivity.this, VoiceMeetingMemberManager.class);
//            intent.putExtra(EXTRA_MEETING, mMeeting);
//            startActivityForResult(intent, REQUEST_CODE_KICK_MEMBER);
//        } else if (position == 4) {
//            // 解散会议操作
//            ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, R.string.dialog_message_dissmiss_chatroom
//                    , R.string.dailog_button_dissmiss_chatroom, R.string.app_cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            doExitChatroomAction();
//                        }
//                    }, null);
//            buildAlert.setTitle(R.string.dialog_title_dissmiss_chatroom);
//            buildAlert.show();
//        } else {
//
//            final String[] arr = getMembers();
//
//            if (arr == null || arr.length == 0) {
//                return;
//            }
//
//            ECListDialog dialog = new ECListDialog(this, arr);
//            dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
//                @Override
//                public void onDialogItemClick(Dialog d, int sposition) {
//                    handleContentMenuClick(sposition, position, arr);
//                }
//
//
//            });
//            dialog.setTitle("操作");
//            dialog.show();
//
//
//        }
//        // 如果是创建者，有管理权限
//    }


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

//    private void handleContentMenuClick(int sposition, int position, String[] arr) {
//        ECVoiceMeetingMember member = null;
//        for (ECVoiceMeetingMember item : sMembers) {
//            if (item.getNumber().equalsIgnoreCase(arr[sposition])) {
//                member = item;
//                break;
//            }
//        }
//        ECVoipAccount voipAccount = new ECVoipAccount();
//        voipAccount.setIsVoip(!member.isMobile());
//        voipAccount.setAccount(member.getNumber());
//
//        ECMeetingManager.ECSpeakListenType type = ECMeetingManager.ECSpeakListenType.MUTE_ON;
//        if (position == 5) {
//            type = ECMeetingManager.ECSpeakListenType.MUTE_ON;
//        } else if (position == 6) {
//            type = ECMeetingManager.ECSpeakListenType.MUTE_OFF;
//
//        } else if (position == 7) {
//
//            type = ECMeetingManager.ECSpeakListenType.LISTEN_ON;
//        } else if (position == 8) {
//            type = ECMeetingManager.ECSpeakListenType.LISTEN_OFF;
//
//        }
//        ECDevice.getECMeetingManager().setMemberSpeakListen(voipAccount, type, mMeeting.getMeetingNo(), ECMeetingManager.ECMeetingType.MEETING_MULTI_VOICE, listener);
//
//
//    }


    public String[] getMembers() {
        if (sMembers == null || sMembers.size() <= 0) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        for (ECVoiceMeetingMember item : sMembers) {
            if (item != null) {
                if (!item.getNumber().equalsIgnoreCase(CCPAppManager.getUserId())) {
                    list.add(item.getNumber());
                }
            }
        }
        return list.toArray(new String[list.size()]);
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
        mSpeakerOn = setupManager.getLoudSpeakerStatus();
        if (mSpeakerOn) {
            ToastUtil.showMessage(R.string.fmt_route_speaker);
        } else {
            ToastUtil.showMessage(R.string.fmt_route_phone);
        }
        initSpeakerMode();
    }

//    /**
//     * 控制菜单的显示和隐藏
//     */
//    private void controlPlusSubMenu() {
//        if (mOverflowHelper == null) {
//            return;
//        }
//
//        if (mOverflowHelper.isOverflowShowing()) {
//            mOverflowHelper.dismiss();
//            changeAmplitude(true);
//            return;
//        }
//        changeAmplitude(false);
//        mOverflowHelper.setOverflowItems(mItems);
//        mOverflowHelper.setOnOverflowItemClickListener(mOverflowItemClicKListener);
//        mOverflowHelper.showAsDropDown(findViewById(R.id.text_right));
//    }


    /**
     * 停止界面动画
     */
    private void stopAmplitude() {
        if (mMeetingMic != null) {
            mMeetingMic.stopMicAmpl();
        }
        if (mVoiceCenter != null) {
            mVoiceCenter.stopAmplitude();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSelfMeeting = false;
        stopAmplitude();

        mTv_countdown_time.stop();
    }

    private boolean isSelfMobile = false;

    @Override
    public void onMeetingMembers(List<? extends ECMeetingMember> members) {
        super.onMeetingMembers(members);
        mTv_countdown_time.setBase(SystemClock.elapsedRealtime());
        mTv_countdown_time.start();

        sMembers = (ArrayList<ECVoiceMeetingMember>) members;
        if (sMembers != null) {
            for (ECVoiceMeetingMember mbr : sMembers) {
                if (mbr != null && mbr.getNumber().equals(CCPAppManager.getUserId()) && !mbr.isMobile()) {
                    hasSelf = true;
                }
            }
        }
        if (!hasSelf) {
            ECVoiceMeetingMember member = new ECVoiceMeetingMember();
            member.setNumber(CCPAppManager.getUserId());
            member.setIsMobile(false);
            if (sMembers == null) {
                sMembers = new ArrayList<ECVoiceMeetingMember>();
            }
            sMembers.add(member);
            LogUtil.e(TAG, " onMeetingMembers  add self");
            hasSelf = true;
        }


        if (mMeetingMemberAdapter == null) {
            mMeetingMemberAdapter = new MeetingMemberAdapter(this);
            mMeetingMemberAdapter.setMembers(sMembers);

            mInter_phone_list.setAdapter(mMeetingMemberAdapter);
        }
        mMeetingMemberAdapter.setMembers(sMembers);
        voiceMetAdapter.setDatas(sMembers);
        voiceMetAdapter.notifyDataSetChanged();
    }

    @Override
    public void onError(int type, ECError e) {
        super.onError(type, e);
        dismissPostingDialog();
        if (MeetingHelper.OnMeetingCallback.MEETING_JOIN == type) {
            // 加入会议失败
            MeetingHelper.exitMeeting();
            finish();
        }
    }

    @Override
    public void onReceiveVoiceMeetingMsg(ECVoiceMeetingMsg msg) {
        super.onReceiveVoiceMeetingMsg(msg);

        if (msg == null || !(mMeeting != null && msg.getMeetingNo().equals(mMeeting.getMeetingNo()))) {
            LogUtil.e(TAG, "onReceiveVoiceMeetingMsg error msg " + msg + " , no " + msg.getMeetingNo());
            return;
        }
        if (sMembers == null) {
            sMembers = new ArrayList<ECVoiceMeetingMember>();
        }
        boolean handle = convertToVoiceMeetingMember(msg);
        // 是否列表数据有改变
        if (handle && mMeetingMemberAdapter != null) {
            if (sMembers != null) {
                mMeetingMemberAdapter.setMembers(sMembers);
                mMeetingMemberAdapter.notifyDataSetChanged();
                voiceMetAdapter.setDatas(sMembers);
                voiceMetAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onMeetingDismiss(String meetingNo) {
        super.onMeetingDismiss(meetingNo);
        isMeeting = false;
        if (mMeeting != null && meetingNo != null &&
                meetingNo.equals(mMeeting.getMeetingNo())) {
            dismissPostingDialog();
            finish();
        }
    }


    /**
     * 处理会议Mic动作
     *
     * @param enable
     */
    private void notifyMeetingMikeEnable(boolean enable) {
        if (mMeetingMic != null) {
            mMeetingMic.setMikeEnable(enable);
        }
    }


    /**
     * 转换成成员消息
     *
     * @param msg
     */
    private boolean convertToVoiceMeetingMember(ECVoiceMeetingMsg msg) {
        if (msg.getMsgType() == ECVoiceMeetingMsg.ECVoiceMeetingMsgType.JOIN) {
            ECVoiceMeetingJoinMsg joinMsg = (ECVoiceMeetingJoinMsg) msg;
            // 有人加入会议消息
            for (String who : joinMsg.getWhos()) {
                LogUtil.e(TAG, " hasSelf :" + hasSelf);
                if (who.equals(CCPAppManager.getUserId()) && hasSelf && (!joinMsg.isMobile())) {
                    LogUtil.e(TAG, " hasSelf true");
                    continue;
                }
                ECVoiceMeetingMember member = new ECVoiceMeetingMember();
                member.setNumber(who);
                member.setIsMobile(joinMsg.isMobile());
                if (!isMemberExist(member)) {
                    sMembers.add(member);
                }
                LogUtil.e(TAG, " hasSelf " + who);
//                updateTopMeetingBarTips(getString(R.string.str_chatroom_join, member.getNumber()));
            }
            return true;
        }

        if (msg.getMsgType() == ECVoiceMeetingMsg.ECVoiceMeetingMsgType.EXIT) {
            ECVoiceMeetingExitMsg exitMsg = (ECVoiceMeetingExitMsg) msg;
            // 有人退出会议消息
            List<ECVoiceMeetingMember> exitMembers = new ArrayList<ECVoiceMeetingMember>();
            for (ECVoiceMeetingMember member : sMembers) {
                if (member != null && member.getNumber() != null) {
                    for (String who : exitMsg.getWhos()) {
                        if (member.getNumber().equals(who) && (exitMsg.isMobile() == member.isMobile())) {
                            exitMembers.add(member);
                        }
                    }
                }
            }
            if (exitMembers.size() > 0) {
                isMeetingOver = false;
                sMembers.removeAll(exitMembers);
//                updateTopMeetingBarTips(getString(R.string.str_chatroom_exit, exitMembers.get(0).getNumber()));
            }
            return true;
        }

        if (msg.getMsgType() == ECVoiceMeetingMsg.ECVoiceMeetingMsgType.REMOVE_MEMBER) {
            ECVoiceMeetingRemoveMemberMsg removeMemberMsg = (ECVoiceMeetingRemoveMemberMsg) msg;
            // 有成员被移除出会议消息
            if ((!removeMemberMsg.isMobile()) && removeMemberMsg != null
                    && CCPAppManager.getClientUser().getUserId().equals(removeMemberMsg.getWho())) {
                // 如果被移除出会议的成员是自己
                // 提示被移除出聊天室对话框
                isMeetingOver = false;
                showRemovedFromChatroomAlert();
                return false;
            } else {
                // 如果移除的成员是其他人
                ECVoiceMeetingMember rMember = null;
                for (ECVoiceMeetingMember member : sMembers) {
                    String number = member.getNumber();
                    if (member != null && number != null && (member.isMobile() == removeMemberMsg.isMobile()) && number.equals(removeMemberMsg.getWho())) {
                        rMember = member;
                        break;
                    }
                }
                if (rMember != null) {
                    sMembers.remove(rMember);
                    // 刷新会议通知栏
//                    updateTopMeetingBarTips(getString(R.string.str_chatroom_kick, rMember.getNumber()));
                }
                return true;
            }
        }

        // 处理会议房间被解散消息
        if (msg.getMsgType() == ECVoiceMeetingMsg.ECVoiceMeetingMsgType.DELETE) {
            if (isSelfMeeting && isMeetingOver) {
                // 不需要处理
                return false;
            }
            onMeetingRoomDel(msg);
            return false;
        }

        if (msg.getMsgType() == ECVoiceMeetingMsg.ECVoiceMeetingMsgType.SPEAK_OPT) {
            // 处理会议成员被禁言操作
            doChatroomMemberForbidOpt((ECVoiceMeetingMemberForbidOpt) msg);
        }

        if (msg.getMsgType() == ECVoiceMeetingMsg.ECVoiceMeetingMsgType.REJECT) {
            // 处理对方拒绝邀请加入请求
            ECVoiceMeetingRejectMsg rejectMsg = (ECVoiceMeetingRejectMsg) msg;
            onVoiceMeetingRejectMsg(rejectMsg);
        }

        return false;
    }


    private boolean isMemberExist(ECVoiceMeetingMember member) {
        for (ECVoiceMeetingMember item : sMembers) {
            if (item != null && (item.getNumber().equals(member.getNumber())) && (item.isMobile() == member.isMobile())) {
                return true;
            }
        }
        return false;

    }

    private void onVoiceMeetingRejectMsg(ECVoiceMeetingRejectMsg msg) {
        if (msg == null) {
            return;
        }
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(VoiceMeetingActivity.this
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


    /**
     * 处理会议成员被禁言
     *
     * @param forbidOpt
     * @return
     */
    private boolean doChatroomMemberForbidOpt(ECVoiceMeetingMemberForbidOpt forbidOpt) {
        if (forbidOpt == null) {
            return false;
        }
        ECVoiceMeetingMsg.ForbidOptions options = forbidOpt.getForbid();

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


        if (forbidOpt.getWho().equalsIgnoreCase(CCPAppManager.getUserId())) {
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
//        updateTopMeetingBarTips(getString(R.string.top_tips_chatroom_disforbid, forbidOpt.getMember()));
        return false;
    }

    /**
     * 处理会议房间被解散消息
     *
     * @param msg 会议被解散的消息
     */
    private void onMeetingRoomDel(ECVoiceMeetingMsg msg) {

        if (isFinishing()) {
            return;
        }
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, R.string.dialog_message_be_dissmiss_chatroom
                , R.string.settings_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doExitChatroomAction();
                    }
                });
        buildAlert.setTitle(R.string.dialog_title_be_dissmiss_chatroom);
        buildAlert.setCanceledOnTouchOutside(false);
        buildAlert.setCancelable(false);
        buildAlert.show();
        // 处理Mic状态
        notifyMeetingMikeEnable(true);

    }

    /**
     * 处理退出会议按钮事件
     */
    private void doTitleLeftAction() {
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, R.string.chatroom_room_exit_room_tip
                , R.string.settings_logout, R.string.app_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitOrDismissChatroom(false);
                    }
                }, null);
        buildAlert.setTitle(R.string.chatroom_room_exit_room);
        buildAlert.show();
    }

    /**
     * 处理会议退出逻辑
     */
    private void doExitChatroomAction() {
        notifyMeetingMikeEnable(true);
        if (isSelfMeeting) {
            exitOrDismissChatroom(false);
        } else {
            // Here is the receipt dissolution news, not so directly off the Page Creator
            finish();
        }
    }

    /**
     * 处理退出会议逻辑
     *
     * @param exit 是否退出或者解散
     */
    private void exitOrDismissChatroom(boolean exit) {
        if (!exit) {
            // 处理会议退出in
            MeetingHelper.exitMeeting();

            ECHandlerHelper.postDelayedRunnOnUI(new Runnable() {

                @Override
                public void run() {

                    finish();
                }
            }, 2000);
        } else {
            showProcessDialog();
            if (mMeeting != null) {
                disMeeting(mMeeting.getMeetingNo());
                isMeetingOver = true;
                isMeeting = false;
            }
        }
    }

    public void disMeeting(String meetingNo) {

        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
        if (meetingManager == null) {
            return;
        }
        meetingManager.deleteMultiMeetingByType(ECMeetingManager.ECMeetingType.MEETING_MULTI_VOICE, meetingNo,
                new ECMeetingManager.OnDeleteMeetingListener() {
                    @Override
                    public void onMeetingDismiss(ECError reason, String meetingNo) {
                        if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

                            isMeeting = false;
                            if (mMeeting != null && meetingNo != null &&
                                    meetingNo.equals(mMeeting.getMeetingNo())) {
                                dismissPostingDialog();
                                finish();
                            }
                            return;
                        }
                        ToastUtil.showMessage("解散会议失败,错误码" + reason.errorCode);
                    }
                });

    }

    /**
     * 提示被移除出聊天室对话框
     */
    private void showRemovedFromChatroomAlert() {
        if (isFinishing()) {
            return;
        }
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, R.string.dialog_message_be_kick_chatroom
                , R.string.dialog_btn_confim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notifyMeetingMikeEnable(true);
                        finish();
                    }
                });
        buildAlert.setTitle(R.string.dialog_title_be_kick_chatroom);
        buildAlert.setCanceledOnTouchOutside(false);
        buildAlert.setCancelable(false);
        buildAlert.show();
    }

    /**
     * 是否静音
     */
    private boolean isMikeEnable;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left://缩小
                hideSoftKeyboard();
                doTitleLeftAction();
                break;
            case R.id.tv_microphone:
                //麦克风
                try {
                    mTv_microphone.setEnabled(false);
                    ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
                    if (setupManager != null) {
                        setupManager.setMute(!setupManager.getMuteStatus());
                        initMicrophone();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mTv_microphone.setEnabled(true);
                }
                mTv_microphone.setEnabled(true);
                break;
            case R.id.tv_exit:
                doTitleLeftAction();
                break;
            case R.id.tv_speaker:
                // 更改当前的扬声器模式
                changeSpeakerOnMode();
                break;
            case R.id.tv_right:
                // 解散会议操作
                ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, R.string.dialog_message_dissmiss_chatroom
                        , R.string.dailog_button_dissmiss_chatroom, R.string.app_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doExitChatroomAction();
                            }
                        }, null);
                buildAlert.setTitle(R.string.dialog_title_dissmiss_chatroom);
                buildAlert.show();
                break;


        }
    }


    @Override
    public void onMeetingMicEnable(boolean enable) {

    }

    public class MeetingMemberAdapter extends ArrayAdapter<ECVoiceMeetingMember> {
        ArrayList<String> times;

        public MeetingMemberAdapter(Context context) {
            super(context, 0, new ArrayList<ECVoiceMeetingMember>());
            times = new ArrayList<>();

        }

        public void setMembers(List<? extends ECMeetingMember> members) {
            clear();
            if (members != null) {
                for (ECMeetingMember member : members) {
                    if (member instanceof ECVoiceMeetingMember) {
                        if (member == null) {
                            continue;
                        }
                        if (((ECVoiceMeetingMember) member).getNumber().equals(CCPAppManager.getUserId())) {
                            super.insert((ECVoiceMeetingMember) member, 0);
                        } else {
                            super.add((ECVoiceMeetingMember) member);
                        }
                    }
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            MeetingHolder holder;
            if (convertView == null || convertView.getTag() == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_meeting_member, parent, false);
                holder = new MeetingHolder();
                view.setTag(holder);

                holder.name = (TextView) view.findViewById(R.id.member_name);
                holder.tv_join_time = (TextView) view.findViewById(tv_join_time);
            } else {
                view = convertView;
                holder = (MeetingHolder) convertView.getTag();
            }
            ECVoiceMeetingMember member = getItem(position);
            if (member != null) {
                holder.name.setText(getName(member) + "进入了会议");
            }
            return view;
        }

        class MeetingHolder {
            TextView name;
            TextView tv_join_time;
        }
    }

    private String getName(ECVoiceMeetingMember member) {
        if (TextUtils.isEmpty(member.getNumber())) {
            return "";
        }
        String name;
        ECContacts contact = ContactSqlManager.getContact(member.getNumber());
        name = contact.getNickname();
        if (TextUtils.isEmpty(name)) {
            if (member.isMobile()) {
                name = "m" + member.getNumber();
            } else {
                name = member.getNumber();
            }
        }
        return name;
    }
}
