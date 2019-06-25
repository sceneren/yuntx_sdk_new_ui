package com.yuntongxun.ecdemo.ui.interphone;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecdemo.ui.MeetingMsgReceiver;
import com.yuntongxun.ecdemo.ui.meeting.MeetingHelper;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.meeting.ECMeeting;
import com.yuntongxun.ecsdk.meeting.ECMeetingMember;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMember;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMsg;

import java.util.List;

/**
 * Created by Jorstin on 2015/7/27.
 */
public abstract class InterPhoneBaseActivity extends ECSuperActivity implements
        InterPhoneHelper.OnInterPhoneListener , View.OnClickListener , MeetingMsgReceiver.OnVoiceMeetingMsgReceive{
    public AudioManager mAudioManager;

    private ECProgressDialog mPostingdialog;
    @Override
    protected void onResume() {
        super.onResume();
        InterPhoneHelper.addInterPhoneCallback(this);
        MeetingMsgReceiver.addVoiceMeetingListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        InterPhoneHelper.removeInterPhoneCallback(this);
        MeetingMsgReceiver.removeVoiceMeetingListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onInterPhoneError(ECError e) {
        ToastUtil.showMessage("请求错误[" + e.errorCode + "]");
    }

    @Override
    public void onInterPhoneMembers(List<ECInterPhoneMeetingMember> members) {

    }

    @Override
    public void onInterPhoneStart(String interNo) {

    }


    @Override
    public void onReceiveVoiceMeetingMsg(ECVoiceMeetingMsg msg) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;
        }
    }

    @Override
    public void onReceiveInterPhoneMeetingMsg(ECInterPhoneMeetingMsg msg) {

    }

    protected void showProcessDialog() {
        mPostingdialog = new ECProgressDialog(InterPhoneBaseActivity.this, R.string.login_posting_submit);
        mPostingdialog.show();
    }

    /**
     * 关闭对话框
     */
    protected void dismissPostingDialog() {
        if(mPostingdialog == null || !mPostingdialog.isShowing()) {
            return ;
        }
        mPostingdialog.dismiss();
        mPostingdialog = null;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 获取音频类型
        int streamType = ECDevice.getECVoIPSetupManager().getStreamType();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // 调小音量
//            adjustStreamVolumeDown(3);
            adjustStreamVolumeDown(streamType);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // 调大音量
//            adjustStreamVolumeUo(3);
            adjustStreamVolumeUo(streamType);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 向下 调整音量
     * @param streamType 类型
     */
    public final void adjustStreamVolumeDown(int streamType) {
        if (this.mAudioManager != null) {
            this.mAudioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    /**
     * 向上 调整音量
     * @param streamType 类型
     */
    public final void adjustStreamVolumeUo(int streamType) {
        if (this.mAudioManager != null) {
            this.mAudioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_RAISE,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }



}
