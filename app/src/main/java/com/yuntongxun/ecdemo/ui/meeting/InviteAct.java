package com.yuntongxun.ecdemo.ui.meeting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecsdk.ECDevice;

import static com.yuntongxun.ecdemo.ui.meeting.VoiceMeetingActivity.REQUEST_CODE_INVITE_BY_PHONECALL;

/**
 * Created by smileklvens on 2017/8/23.
 */

public class InviteAct extends MeetingBaseActivity {
    private TextView tv_by_voip;
    private TextView tv_by_phone;

    /**
     * 会议号
     */
    private String mMeetingNo;

    @Override
    protected int getLayoutId() {
        return R.layout.invite_act;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMeetingNo = getIntent().getStringExtra(ECDevice.MEETING_NO);
        if (TextUtils.isEmpty(mMeetingNo)) {
            ToastUtil.showMessage(R.string.toast_confno_Illegal);
            finish();
            return;
        }


        initView();

        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                -1, null,
                null,
                getString(R.string.dialog_title_invite), null, this);
    }

    private void initView() {
        tv_by_voip = (TextView) findViewById(R.id.tv_by_voip);
        tv_by_phone = (TextView) findViewById(R.id.tv_by_phone);
        tv_by_voip.setOnClickListener(this);
        tv_by_phone.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Intent callByPhone = new Intent(this, InviteByPhoneCall.class);
        callByPhone.putExtra(ECDevice.MEETING_NO, mMeetingNo);
        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.tv_by_voip:
                callByPhone.putExtra("isLandingCall", false);
                startActivityForResult(callByPhone, REQUEST_CODE_INVITE_BY_PHONECALL);
                break;
            case R.id.tv_by_phone:
                callByPhone.putExtra("isLandingCall", true);
                startActivityForResult(callByPhone, REQUEST_CODE_INVITE_BY_PHONECALL);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_INVITE_BY_PHONECALL == requestCode) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }

    }
}
