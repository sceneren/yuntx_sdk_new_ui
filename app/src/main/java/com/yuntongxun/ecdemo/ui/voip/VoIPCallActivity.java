package com.yuntongxun.ecdemo.ui.voip;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.ui.smallwindow.VoiceMeetingService;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECVoIPCallManager.CallType;
import com.yuntongxun.ecsdk.SdkErrorCode;


/**
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/3.
 */
public class VoIPCallActivity extends ECVoIPBaseActivity implements View.OnClickListener /*implements ECVoIPCallManager.OnCallProcessMultiDataListener*/ {

    private static final String TAG = "ECSDK_Demo.VoIPCallActivity";
    private boolean isCallBack;

    protected ECCallHeadUILayout mCallHeaderView;
    protected ECCallControlUILayout mCallControlUIView;

    @Override
    protected int getLayoutId() {
        return R.layout.ec_call_interface;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏小窗口

        isCreated = true;
        findViewById(R.id.iv_narrow).setClickable(false);
        findViewById(R.id.iv_narrow).setEnabled(false);

        if ((VoiceMeetingService.getInstance().getVoipSmallWindow() != null)) {
            VoiceMeetingService.getMiniWindow().dismiss();
            findViewById(R.id.iv_narrow).setClickable(true);
            findViewById(R.id.iv_narrow).setEnabled(true);
        }

        initView();

        initCallEvent();


    }

    private void initCallEvent() {
        mCallHeaderView.setCalling(false);

        if (!VoiceMeetingService.inMeeting()) {//不在语音中
            if (mIncomingCall) {  // 来电
                mCallId = getIntent().getStringExtra(ECDevice.CALLID);
                mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);

                mCallName = AvatorUtil.getInstance().getMarkName(mCallNumber);

                VoiceMeetingService.getInstance().callId = mCallId;
            } else { // 呼出
                mCallName = getIntent().getStringExtra(EXTRA_CALL_NAME);
                mCallNumber = getIntent().getStringExtra(EXTRA_CALL_NUMBER);
                isCallBack = getIntent().getBooleanExtra(ACTION_CALLBACK_CALL, false);

            }
            if (!mIncomingCall) {
                // 处理呼叫逻辑
                if (TextUtils.isEmpty(mCallNumber)) {
                    ToastUtil.showMessage(R.string.ec_call_number_error);
                    finish();
                    return;
                }

                if (isCallBack) {
                    VoIPCallHelper.makeCallBack(CallType.VOICE, mCallNumber);
                } else {
                    mCallId = VoIPCallHelper.makeCall(mCallType, mCallNumber);
                    VoiceMeetingService.getInstance().callId = mCallId;
                    if (TextUtils.isEmpty(mCallId)) {
                        ToastUtil.showMessage(R.string.ec_app_err_disconnect_server_tip);
                        LogUtil.d(TAG, "Call fail, callId " + mCallId);
                        finish();
                        return;
                    }
                }
                mCallHeaderView.setCallTextMsg(R.string.ec_voip_call_connecting_server);
            } else {
                mCallHeaderView.setCallTextMsg(" ");
            }
            ECCallControlUILayout.CallLayout callLayout = mIncomingCall ? ECCallControlUILayout.CallLayout.INCOMING
                    : ECCallControlUILayout.CallLayout.OUTGOING;
            mCallControlUIView.setCallDirect(callLayout);
        } else {//会议中
            mCallName = VoiceMeetingService.getInstance().nickname;
            mCallNumber = VoiceMeetingService.getInstance().contactId;
            mCallId = VoiceMeetingService.getInstance().callId;

            mCallControlUIView.setCallDirect(ECCallControlUILayout.CallLayout.INCALL);
            mCallHeaderView.setCalling(true);
        }

        if("0000000000".equalsIgnoreCase(mCallNumber)){
            mCallNumber = CCPAppManager.PHONE;
            mCallName  = mCallNumber;
        }

        mCallControlUIView.setOnCallControlDelegate(this);
        mCallHeaderView.setCallName(mCallName);
        mCallHeaderView.setCallNumber(TextUtils.isEmpty(mCallNumber) ? mPhoneNumber : mCallNumber);

        String mHeadUrl = FriendMessageSqlManager.queryURLByID(mCallNumber);
        mCallHeaderView.setCallhead(mHeadUrl);

        mCallHeaderView.setSendDTMFDelegate(this);

        if(TextUtils.isEmpty(ECDevice.getECVoIPSetupManager().getCurrentCall())){

        }
//        findViewById(R.id.iv_narrow).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 最小化
//                VoiceMeetingService.getInstance().onMinimizeVoip(true, false);
//                finish();
//            }
//        });
        findViewById(R.id.iv_narrow).setOnClickListener(this);

    }




    @Override
    protected void onNewIntent(Intent intent) {
        isCreated = true;
        initView();
        initCallEvent();
    }

    private boolean isCreated = false;

    private void initView() {
        mCallHeaderView = (ECCallHeadUILayout) findViewById(R.id.call_header_ll);
        mCallControlUIView = (ECCallControlUILayout) findViewById(R.id.call_control_ll);
    }


    @Override
    protected boolean isEnableSwipe() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCreated = false;

    }

    /**
     * 连接到服务器
     *
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallProceeding(String callId) {
        if (mCallHeaderView == null || !needNotify(callId)) {
            return;
        }
        LogUtil.d(TAG, "onUICallProceeding:: call id " + callId);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_call_connect);
    }

    /**
     * 连接到对端用户，播放铃音
     *
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallAlerting(String callId) {
        if (!needNotify(callId) || mCallHeaderView == null) {
            return;
        }
        LogUtil.d(TAG, "onUICallAlerting:: call id " + callId);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_wait);
        mCallControlUIView.setCallDirect(ECCallControlUILayout.CallLayout.ALERTING);
    }

    /**
     * 对端应答，通话计时开始
     *
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallAnswered(final String callId) {

        VoiceMeetingService.getInstance().setTime();
        VoiceMeetingService.getInstance().dest();

        mCallControlUIView.setCallDirect(ECCallControlUILayout.CallLayout.INCALL);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_accepting);

        findViewById(R.id.iv_narrow).setClickable(true);
        findViewById(R.id.iv_narrow).setEnabled(true);

        findViewById(R.id.iv_narrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 最小化

//                if(!DemoUtils.checkAlertWindowsPermission(VoIPCallActivity.this)){
//                    ToastUtil.showMessage("请确认已经打开了浮窗权限");
//                    return;
//                }

                VoiceMeetingService.getInstance().onMinimizeVoip(true, false);
                finish();
            }
        });

        if (!needNotify(callId) || mCallHeaderView == null) {
            return;
        }
        LogUtil.d(TAG, "onUICallAnswered:: call id " + callId);
        VoiceMeetingService.getInstance().mDuration = System.currentTimeMillis();
        mCallHeaderView.setCalling(true);
        isConnect = true;

    }

    public boolean isVoicePermission() {
        try {
            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, 22050, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(22050, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT));
            record.startRecording();
            int recordingState = record.getRecordingState();
            if (recordingState == AudioRecord.RECORDSTATE_STOPPED) {
                return false;
            }
            record.release();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onMakeCallFailed(String callId, int reason) {

        if (mCallHeaderView == null || !needNotify(callId)) {
            return;
        }
        LogUtil.d(TAG, "onUIMakeCallFailed:: call id " + callId + " ,reason " + reason);
        mCallHeaderView.setCalling(false);
        isConnect = false;
        mCallHeaderView.setCallTextMsg(CallFailReason.getCallFailReason(reason));
        if (reason != SdkErrorCode.REMOTE_CALL_BUSY && reason != SdkErrorCode.REMOTE_CALL_DECLINED) {
            VoIPCallHelper.releaseCall(mCallId);
            finish();
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    /**
     * 通话结束，通话计时结束
     *
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallReleased(String callId) {
        CCPAppManager.PHONE = "";
        if (mCallHeaderView == null || !needNotify(callId)) {
            return;
        }
        LogUtil.d(TAG, "onUICallReleased:: call id " + callId);


//        ECDevice.setAudioMode(1);

        mCallHeaderView.setCalling(false);
        isConnect = false;
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_finish);
        mCallControlUIView.setControlEnable(false);

        VoiceMeetingService.getInstance().markVoiceDel();
        VoiceMeetingService.getInstance().setTime();
        VoiceMeetingService.getInstance().dest();
        finish();
    }


    @Override
    public void onMakeCallback(ECError ecError, String caller, String called) {
        if (!TextUtils.isEmpty(mCallId)) {
            return;
        }
        if (ecError.errorCode != SdkErrorCode.REQUEST_SUCCESS) {
            mCallHeaderView.setCallTextMsg("回拨呼叫失败[" + ecError.errorCode + "]");
        } else {
            mCallHeaderView.setCallTextMsg(R.string.ec_voip_call_back_success);
        }
        mCallHeaderView.setCalling(false);
        isConnect = false;
        mCallControlUIView.setControlEnable(false);
        finish();
    }

//    @Override
//    public void setDialerpadUI() {
//        mCallHeaderView.controllerDiaNumUI();
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.iv_narrow){

//            if(!DemoUtils.checkAlertWindowsPermission(this)){
//                ToastUtil.showMessage("请确认已经打开了浮窗权限");
//                return;
//            }

            VoiceMeetingService.getInstance().onMinimizeVoip(true, false);
                finish();
        }
    }
}
