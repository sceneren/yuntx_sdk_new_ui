package com.yuntongxun.ecdemo.ui.videomeeting;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.base.CCPClearEditText;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.meeting.MeetingBaseActivity;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager;
import com.yuntongxun.ecsdk.ECMeetingManager.OnInviteMembersJoinToMeetingListener;
import com.yuntongxun.ecsdk.SdkErrorCode;

/**
 * 外呼邀请加入视屏会议
 * Created by Jorstin on 2015/7/26.
 */
public class VideoInviteByPhoneCall extends MeetingBaseActivity {

    private static final String TAG = "ECSDK_Demo.VideoInviteByPhoneCall";
    /**
     * 电话号码输入框
     */
    private CCPClearEditText mSayHiEdit;
    private Button btn_invite;
    /**
     * 会议号
     */
    private String videoconferenceid;
    private boolean misLandingCall;


    @Override
    protected int getLayoutId() {
        return R.layout.invite_by_phone_call;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoconferenceid = getIntent().getStringExtra(MultiVideoconference.VIDEOCONFERENCEID);
        misLandingCall = getIntent().getBooleanExtra("isLandingCall", true);
        if (TextUtils.isEmpty(videoconferenceid)) {
            ToastUtil.showMessage(R.string.toast_confno_Illegal);
            finish();
            return;
        }


        initView();

        if (misLandingCall) {
            getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                    -1, null,
                    null,
                    getString(R.string.dialog_title_invite_phone), null, this);
            mSayHiEdit.setHint("请输入被添加人的手机号码");
        } else {
            getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                    -1, null,
                    null,
                    getString(R.string.dialog_title_invite_voip), null, this);
            mSayHiEdit.setHint("请输入被添加人的通讯账号");
        }

    }

    private void initView() {
        mSayHiEdit = (CCPClearEditText) findViewById(R.id.say_hi_content);

        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(20)};
        mSayHiEdit.setFilters(filters);

        mSayHiEdit.setInputType(InputType.TYPE_CLASS_PHONE);

        btn_invite = (Button) findViewById(R.id.btn_invite);

        btn_invite.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btn_invite:
                hideSoftKeyboard();
                String mPhoneNumber = mSayHiEdit.getText().toString();
                if (TextUtils.isEmpty(mPhoneNumber)) {
                    ToastUtil.showMessage(R.string.regbymobile_reg_mobile_format_err_msg);
                    return;
                }
                doInviteMobileMember(mPhoneNumber);
                break;
        }
    }

    /**
     * 处理邀请成员加入会议请求
     */
    @SuppressWarnings("deprecation")
    private void doInviteMobileMember(String phoneNumber) {
        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
        if (meetingManager == null) {
            return;
        }
        showProcessDialog();


        meetingManager.inviteMembersJoinToVoiceMeeting(videoconferenceid, new String[]{phoneNumber}, misLandingCall, new OnInviteMembersJoinToMeetingListener() {

            @Override
            public void onInviteMembersJoinToMeeting(ECError reason, String arg1) {

                dismissPostingDialog();
                if (SdkErrorCode.REQUEST_SUCCESS == reason.errorCode) {
                    // 邀请加入会议成功
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                ToastUtil.showMessage("邀请加入会议失败[" + reason.errorCode + "]");

            }
        });


    }
}
