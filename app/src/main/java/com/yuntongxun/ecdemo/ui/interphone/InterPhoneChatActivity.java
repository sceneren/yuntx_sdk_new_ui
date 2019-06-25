package com.yuntongxun.ecdemo.ui.interphone;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.ECNotificationManager;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.WrapGridView;
import com.yuntongxun.ecdemo.common.view.WrapListview;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.adapter.InterphoneGVAdapter;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.meeting.MeetingHelper;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.meeting.ECMeetingMember;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneControlMicMsg;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneExitMsg;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneJoinMsg;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMember;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMsg;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneReleaseMicMsg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * com.yuntongxun.ecdemo.ui.interphone in ECDemo_Android
 * Created by Jorstin on 2015/7/16.
 * 实时对讲
 */
public class InterPhoneChatActivity extends InterPhoneBaseActivity
        implements InterPhoneHelper.OnInterPhoneListener, View.OnClickListener, InterPhoneMicController.OnInterPhoneMicListener {

    private static final String TAG = "ECSDK_Demo.InterPhoneChatActivity";

    public static final String EXTRA_MEMBERS = "com.yuntongxun.Meeting.meetingMembers";
    public static final String EXTRA_CREAT = "com.yuntongxun.Meeting.creat";

    /**
     * 实时对讲成员列表
     */
    private WrapListview mInterPhoneListView;

    private ImageView mIv_left;
    private TextView mTv_right;
    private TextView tv_avator;
    private TextView mTv_note;
    private WrapGridView mGv_inter_icon;


    /**
     * 实时对讲控麦面板
     */
    private InterPhoneMicController mMicController;
    /**
     * 实时对讲呼入号
     */
    private String mInterMeetingNo;
    /**
     * 实时对讲参与成员
     */
    private List<ECInterPhoneMeetingMember> mInterMembers = new ArrayList<ECInterPhoneMeetingMember>();
    /**
     * 实时对讲成员状态信息适配器
     */
    private InterPhoneMemberAdapter mInterAdapter;
    /**
     * 实时对讲在线人数统计
     */
    private int onLineCount = 0;
    private InterphoneGVAdapter interphoneGVAdapter;
    private String creater;

    @Override
    protected int getLayoutId() {
        return R.layout.inter_phone_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();

        mInterMeetingNo = getIntent().getStringExtra(ECDevice.MEETING_NO);
        // 实时对讲会议号（加入）
        if (getIntent().hasExtra("creater")){
            creater = getIntent().getStringExtra("creater");
        }
        // 如果是创建实时对讲会议
        ArrayList<ECContacts> ecContactList = getIntent().getParcelableArrayListExtra(EXTRA_MEMBERS);
        if (getIntent().hasExtra(InterPhoneChatActivity.EXTRA_CREAT)){
            creater = getIntent().getStringExtra(InterPhoneChatActivity.EXTRA_CREAT);
        }

        initProwerManager();


        if (!TextUtils.isEmpty(creater)) {
            setHostPeople();
        }

        if (mInterMeetingNo == null && ecContactList == null) {
            throw new IllegalArgumentException("create Inter phone error . meetingNo "
                    + mInterMeetingNo + " , members " + ecContactList);
        }

        if (mInterMeetingNo != null) {
            // 如果有实时对讲会议号，则加入实时对讲
            InterPhoneHelper.joinInterPhone(mInterMeetingNo);
        }else{
            InterPhoneHelper.startInterphone(ecContactList);
        }
    }

    private void setHostPeople() {

        AvatorUtil.getInstance().setAvatorPhoto(tv_avator,R.drawable.memer_bg,creater);
        mTv_note.setText("对讲发起人： " + AvatorUtil.getInstance().getMarkName(creater));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onLineCount = 0;
    }


    @Override
    public int getTitleLayout() {
        return -1;
    }

    @Override
    public void onReceiveInterPhoneMeetingMsg(ECInterPhoneMeetingMsg msg) {
        super.onReceiveInterPhoneMeetingMsg(msg);
        if (msg == null || (mInterMeetingNo != null && !mInterMeetingNo.equals(msg.getMeetingNo()))) {
            LogUtil.e(TAG, "onReceiveInterPhoneMeetingMsg error msg " + msg + " , no " + msg.getMeetingNo());
            return;
        }
        if (mInterMembers == null) {
            mInterMembers = new ArrayList<ECInterPhoneMeetingMember>();
        }
        boolean handle = convertToInterPhoneMeetingMember(msg);
        // 是否列表数据有改变

        if (onLineCount <= 0) {
            onLineCount = 0;
        }

        if (handle && mInterAdapter != null) {
            mInterAdapter.notifyDataSetChanged();
        }

        if (interphoneGVAdapter != null) {
            interphoneGVAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected boolean isEnableSwipe() {
        return false;
    }

    /**
     * 转换成成员消息
     *
     * @param msg
     * @return
     */
    private boolean convertToInterPhoneMeetingMember(ECInterPhoneMeetingMsg msg) {
        if (msg.getMsgType() == ECInterPhoneMeetingMsg.ECInterPhoneMeetingMsgType.JOIN) {
            ECInterPhoneJoinMsg joinMsg = (ECInterPhoneJoinMsg) msg;

            creater = SDKCoreHelper.getCreator(mInterMeetingNo);
            if (!TextUtils.isEmpty(creater)) {
                setHostPeople();
            }
            if (joinMsg != null) {
                // 有人加入会议消息
                for (ECInterPhoneMeetingMember member : mInterMembers) {
                    if (member != null
                            && member.getMember() != null
                            && member.getMember().equals(joinMsg.getWho())) {
                        member.setOnline(ECInterPhoneMeetingMember.Online.ONLINE);

                        if (member.getType() == ECMeetingMember.Type.SPONSOR){
                            creater  = member.getMember();
                        }
                        onLineCount++;
                        return true;
                    }
                }

                if (!TextUtils.isEmpty(creater)) {
                    setHostPeople();
                }

            } else {
                return false;
            }
        }

        // 实时对讲有人退出
        if (msg.getMsgType() == ECInterPhoneMeetingMsg.ECInterPhoneMeetingMsgType.EXIT) {
            ECInterPhoneExitMsg exitMsg = (ECInterPhoneExitMsg) msg;
            // 有人退出会议消息
            for (ECInterPhoneMeetingMember member : mInterMembers) {
                if (member != null
                        && member.getMember() != null
                        && member.getMember().equals(exitMsg.getWho())) {
                    member.setOnline(ECInterPhoneMeetingMember.Online.UN_ONLINE);
                    onLineCount--;
                    return true;
                }
            }
        }

        // 实时对讲有人控麦
        if (msg.getMsgType() == ECInterPhoneMeetingMsg.ECInterPhoneMeetingMsgType.CONTROL_MIC) {
            ECInterPhoneControlMicMsg controlMicMsg = (ECInterPhoneControlMicMsg) msg;
            // 改变成员状态
            for (ECInterPhoneMeetingMember member : mInterMembers) {
                if (member != null
                        && member.getMember() != null
                        && member.getMember().equals(controlMicMsg.getWho())) {
                    member.setMic(ECInterPhoneMeetingMember.Mic.MIC_CONTROLLER);
                    return true;
                }
            }
        }

        // 实时对讲有人结束控麦
        if (msg.getMsgType() == ECInterPhoneMeetingMsg.ECInterPhoneMeetingMsgType.RELEASE_MIC) {
            ECInterPhoneReleaseMicMsg releaseMicMsg = (ECInterPhoneReleaseMicMsg) msg;
            // 改变成员状态
            for (ECInterPhoneMeetingMember member : mInterMembers) {
                if (member != null
                        && member.getMember() != null
                        && member.getMember().equals(releaseMicMsg.getWho())) {
                    member.setMic(ECInterPhoneMeetingMember.Mic.MIC_UN_CONTROLLER);
                    return true;
                }
            }
        }

        // 实时对讲已结束
        if (msg.getMsgType() == ECInterPhoneMeetingMsg.ECInterPhoneMeetingMsgType.OVER) {
            ToastUtil.showMessage("实时对讲结束[" + mInterMeetingNo + "]");
            finish();
        }
        return false;

    }

//    private int getOnLineCount() {
//
//        int i = 0;
//        for (ECInterPhoneMeetingMember member : mInterMembers) {
//            if (member != null
//                    && member.getMember() != null
//                    && member.getOnline() == ECInterPhoneMeetingMember.Online.ONLINE) {
//
//                i++;
//            }
//        }
//        return i;
//    }


//    /**
//     * 查询创建者
//     */
//    private void initInviteMembers(ArrayList<ECContacts> members) {
//        if (members == null) {
//            return;
//        }
//        if (mInterMembers == null) {
//            mInterMembers = new ArrayList<>();
//        }
//        boolean containSelf = false;
//
//        for (ECContacts contact : members) {
//            ECInterPhoneMeetingMember meetingMember = new ECInterPhoneMeetingMember(contact.getContactid());
//            meetingMember.setMic(ECInterPhoneMeetingMember.Mic.MIC_UN_CONTROLLER);
//            meetingMember.setOnline(ECInterPhoneMeetingMember.Online.UN_ONLINE);
//
//            if (contact.getContactid().equals(CCPAppManager.getClientUser().getUserId())) {
//                // 如果是自己
//                meetingMember.setType(ECInterPhoneMeetingMember.Type.SPONSOR);
//                mInterMembers.add(0, meetingMember);
//                containSelf = true;
//
//                creater = contact.getContactid();
//                tv_avator.setText(creater);
//                mTv_note.setText("对讲发起人： " + creater);
//
//            } else {
//                meetingMember.setType(ECInterPhoneMeetingMember.Type.PARTICIPANT);
//                mInterMembers.add(meetingMember);
//            }
//
//        }
//        if (!containSelf) {
//            // 将自己添加到列表中
//            ECInterPhoneMeetingMember meetingMember = new ECInterPhoneMeetingMember(CCPAppManager.getUserId());
//            meetingMember.setMic(ECInterPhoneMeetingMember.Mic.MIC_UN_CONTROLLER);
//            meetingMember.setOnline(ECInterPhoneMeetingMember.Online.UN_ONLINE);
//            meetingMember.setType(ECInterPhoneMeetingMember.Type.SPONSOR);
//            mInterMembers.add(meetingMember);
//        }
//    }

    /**
     * 初始化资源
     */
    private void initView() {

        mInterPhoneListView = (WrapListview) findViewById(R.id.inter_phone_list);
        mMicController = (InterPhoneMicController) findViewById(R.id.inter_phone_speak_ly);

        mIv_left = (ImageView) findViewById(R.id.iv_left);
        mTv_right = (TextView) findViewById(R.id.tv_right);
        tv_avator = (TextView) findViewById(R.id.tv_avator);
        mTv_note = (TextView) findViewById(R.id.tv_note);
        mGv_inter_icon = (WrapGridView) findViewById(R.id.gv_inter_icon);


        mIv_left.setOnClickListener(this);
        mTv_right.setOnClickListener(this);

        mMicController.setOnInterPhoneMicListener(this);
        mInterAdapter = new InterPhoneMemberAdapter(this, mInterMembers);
        mInterPhoneListView.setAdapter(mInterAdapter);

        interphoneGVAdapter = new InterphoneGVAdapter(this, mInterMembers);
        mGv_inter_icon.setAdapter(interphoneGVAdapter);
    }


    //发起对讲成功
    @Override
    public void onInterPhoneStart(String interNo) {
        if (interNo == null || (mInterMeetingNo != null && !mInterMeetingNo.equals(interNo))) {
            return;
        }

        if (!TextUtils.isEmpty(interNo) && !TextUtils.isEmpty(creater)) {
            SDKCoreHelper.putCacheCreat(interNo, creater);
        }

        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager != null) {
            setupManager.enableLoudSpeaker(true);
        }
        // 缓存实时对讲会议号
        mInterMeetingNo = interNo;
        // 如果会议成员为空，则查询会议成员
        InterPhoneHelper.queryInterPhoneMember(mInterMeetingNo);

        if (mMicController != null) {
            mMicController.setInterSpeakEnabled(true);
        }
    }

    //查询对讲成员回调
    @Override
    public void onInterPhoneMembers(List<ECInterPhoneMeetingMember> members) {
        if (mInterMembers == null) {
            mInterMembers = new ArrayList<ECInterPhoneMeetingMember>();

        }
        if (members != null) {
            mInterMembers.addAll(members);
        }
        mInterAdapter.notifyDataSetChanged();
        interphoneGVAdapter.addAll(mInterMembers);
        interphoneGVAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInterPhoneError(ECError e) {
        if (e.errorCode == 111609) {
            // 实时对讲房间号码异常
            InterPhoneHelper.exitInterPhone();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
            case R.id.iv_left:
                hideSoftKeyboard();
                exitInterPhone();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) && (event.getRepeatCount() == 0)) {
            exitInterPhone();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 处理退出实时对讲操作
     */
    private void exitInterPhone() {
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, R.string.talk_room_exit_room_tip
                , R.string.app_exit, R.string.app_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MeetingHelper.exitMeeting();
                        setResult(RESULT_OK);
                        finish();
                    }
                }, null);
        buildAlert.setTitle(R.string.talk_room_exit_room);
        buildAlert.show();
    }

    @Override
    public void onPrepareControlMic() {
        try {
            ECNotificationManager.getInstance().playNotificationMusic("inter_phone_pressed.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        enterIncallMode();
    }

    @Override
    public void onControlMic() {
        controlOrReleaseMic(true);
    }

    @Override
    public void onReleaseMic() {
        controlOrReleaseMic(false);
        releaseWakeLock();
        try {
            ECNotificationManager.getInstance().playNotificationMusic("inter_phone_up.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否控麦或者放麦
     *
     * @param control
     */
    private boolean controlOrReleaseMic(boolean control) {
        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
        if (meetingManager == null) {
            return false;
        }
        if (control) {
            meetingManager.controlMicInInterPhoneMeeting(mInterMeetingNo, new ECMeetingManager.OnControlMicInInterPhoneListener() {
                @Override
                public void onControlMicState(ECError reason, String speaker) {
                    if (SdkErrorCode.REQUEST_SUCCESS == reason.errorCode) {
                        ToastUtil.showMessage("抢麦成功");
                        // 实时对讲抢麦成功
                        try {
                            ECNotificationManager.getInstance().playNotificationMusic("inter_phone_connect.mp3");
                            //手机震动
                            DemoUtils.shakeControlMic(InterPhoneChatActivity.this, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // 更新状态
                        sendControlMsgCommand(true, CCPAppManager.getUserId());
                        mMicController.setControlMicType(InterPhoneMicController.MicType.CONTROL);
                        return;
                    }
                    ToastUtil.showMessage("抢麦失败");
                    mMicController.setControlMicType(InterPhoneMicController.MicType.ERROR);
                }
            });
            return true;
        }

        meetingManager.releaseMicInInterPhoneMeeting(mInterMeetingNo, new ECMeetingManager.OnReleaseMicInInterPhoneListener() {
            @Override
            public void onReleaseMicState(ECError reason) {
                if (SdkErrorCode.REQUEST_SUCCESS == reason.errorCode) {
                    // 实时对讲放麦成功
                    mMicController.setControlMicType(InterPhoneMicController.MicType.IDLE);
                    // 更新状态
                    sendControlMsgCommand(false, CCPAppManager.getUserId());
                    return;
                }
            }
        });

        return false;
    }

    /**
     * 更改状态
     *
     * @param userId
     */
    private void sendControlMsgCommand(boolean control, String userId) {
        ECInterPhoneMeetingMsg meetingMsg;
        if (control) {
            ECInterPhoneControlMicMsg controlMicMsg = new ECInterPhoneControlMicMsg(ECInterPhoneMeetingMsg.ECInterPhoneMeetingMsgType.CONTROL_MIC);
            controlMicMsg.setMeetingNo(mInterMeetingNo);
            controlMicMsg.setWho(userId);
            meetingMsg = controlMicMsg;
        } else {
            ECInterPhoneReleaseMicMsg releaseMicMsg = new ECInterPhoneReleaseMicMsg(ECInterPhoneMeetingMsg.ECInterPhoneMeetingMsgType.RELEASE_MIC);
            releaseMicMsg.setMeetingNo(mInterMeetingNo);
            releaseMicMsg.setWho(userId);
            meetingMsg = releaseMicMsg;
        }
        onReceiveInterPhoneMeetingMsg(meetingMsg);
    }

    /**
     * 实时对讲成员列表适配器
     * 对实时对讲成员各种状态信息（加入或者未加入、控麦或者非控麦）进行区分显示
     */
    public class InterPhoneMemberAdapter extends ArrayAdapter<ECInterPhoneMeetingMember> {

        private final LayoutInflater inflate;

        public InterPhoneMemberAdapter(Context context, List<ECInterPhoneMeetingMember> objects) {
            super(context, 0, objects);
            inflate = LayoutInflater.from(getContext());
        }

        /**
         * 更新列表成员
         *
         * @param objects
         */
        public void setMembers(List<ECInterPhoneMeetingMember> objects) {
            clear();

            if (objects == null) {
                return;
            }
            for (ECInterPhoneMeetingMember meetingMember : objects) {
                add(meetingMember);
            }
        }

//        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            InterPhoneItem mInterView;
            if (convertView == null || convertView.getTag() == null) {

                view = this.inflate.inflate(R.layout.inter_phone_item, parent, false);
                mInterView = new InterPhoneItem(view);
                view.setTag(mInterView);
            } else {
                view = convertView;
                mInterView = (InterPhoneItem) view.getTag();
            }

            ECInterPhoneMeetingMember meetingMember = mInterMembers.get(position);
            mInterView.setInterMember(meetingMember);
            return view;
        }
    }
}
