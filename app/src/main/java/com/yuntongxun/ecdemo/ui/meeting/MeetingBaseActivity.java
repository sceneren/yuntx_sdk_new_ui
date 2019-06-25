package com.yuntongxun.ecdemo.ui.meeting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecdemo.ui.MeetingMsgReceiver;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.meeting.ECMeeting;
import com.yuntongxun.ecsdk.meeting.ECMeetingMember;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMsg;

import java.util.List;

/**
 * com.yuntongxun.ecdemo.ui.meeting in ECDemo_Android
 * Created by Jorstin on 2015/7/17.
 */
public abstract class MeetingBaseActivity extends ECSuperActivity
        implements MeetingHelper.OnMeetingCallback , View.OnClickListener , MeetingMsgReceiver.OnVoiceMeetingMsgReceive{


    private ECProgressDialog mPostingdialog;

    public AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
        mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
    	MeetingMsgReceiver.addVoiceMeetingListener(this);
    }
    
    @Override
    protected void onResume(){
        super.onResume();
        MeetingHelper.addInterPhoneCallback(this);
    }


    @Override
    protected void onPause(){
        super.onPause();
        MeetingHelper.removeInterPhoneCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MeetingHelper.removeInterPhoneCallback(this);
        MeetingMsgReceiver.removeVoiceMeetingListener(this);
    }

    @Override
    public void onMeetings(List<ECMeeting> list) {

    }

    @Override
    public void onError(int type ,ECError e) {

        if(e!=null&&e.errorCode==175708){
            ToastUtil.showMessage("您输入的密码不正确");
            return;
        }else if(e!=null&&e.errorCode==111000){
            ToastUtil.showMessage("您输入的房间名称含有不支持类型符号");
            return;

        }else if(e!=null&&e.errorCode==111715){
            ToastUtil.showMessage("您输入的房间名称超过字数限制");
            return;
        }
        ToastUtil.showMessage("请求错误[" + e.errorCode + "]");
    }

    @Override
    public void onMeetingStart(String meetingNo) {

    }

    @Override
    public void onMeetingDismiss(String meetingNo) {

    }

    @Override
    public void onMeetingMembers(List<? extends ECMeetingMember> members) {

    }

    @Override
    public void onReceiveVoiceMeetingMsg(ECVoiceMeetingMsg msg) {

    }

    @Override
    public void onReceiveInterPhoneMeetingMsg(ECInterPhoneMeetingMsg msg) {

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

    /**
     * 跳转到会议聊天界面
     * @param meeting
     * @param pass
     */
    protected void doStartMeetingActivity(ECMeeting meeting ,String pass) {
        doStartMeetingActivity(meeting , pass , true);
    }

    /**
     * 跳转到会议聊天界面
     * @param meeting
     * @param pass
     */
    protected void doStartMeetingActivity(ECMeeting meeting ,String pass , boolean callin) {
        Intent intent = new Intent(MeetingBaseActivity.this , VoiceMeetingActivity.class);
        intent.putExtra(VoiceMeetingActivity.EXTRA_MEETING , meeting);
        if(!TextUtils.isEmpty(pass)) {
            intent.putExtra(VoiceMeetingActivity.EXTRA_MEETING_PASS , pass);
        }
        intent.putExtra(VoiceMeetingActivity.EXTRA_CALL_IN , callin);
        startActivity(intent) ;
    }

    protected void showInputCodeDialog(final ECMeeting meeting, String title, String message) {
        View view = View.inflate(this , R.layout.dialog_edit_context , null);
        final EditText editText = (EditText) view.findViewById(R.id.sendrequest_content);
        ((TextView) view.findViewById(R.id.sendrequest_tip)).setText(message);
        ECAlertDialog dialog = ECAlertDialog.buildAlert(this, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleInput(meeting , editText);
            }
        });
        dialog.setContentView(view);
        dialog.setTitle(title);
        dialog.show();
    }

    protected void handleInput(ECMeeting meeting , EditText editText) {

    }


    protected  void showProcessDialog(){
    	if(mPostingdialog!=null && mPostingdialog.isShowing()){
    		return;
    	}
        mPostingdialog = new ECProgressDialog(MeetingBaseActivity.this, R.string.login_posting_submit);
        mPostingdialog.show();
    }
    protected synchronized   void  showCustomProcessDialog(String content) {
    	
    	if(mPostingdialog!=null&&mPostingdialog.isShowing()){
    		return;
    	}
    	
    	mPostingdialog = new ECProgressDialog(MeetingBaseActivity.this, content);
    	mPostingdialog.show();
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


    public void cb(){


    }

    
    /**
     * 关闭对话框
     */
    protected void dismissPostingDialog(){
        if(mPostingdialog == null || !mPostingdialog.isShowing()) {
            return ;
        }
        mPostingdialog.dismiss();
        mPostingdialog = null;
    }
}
