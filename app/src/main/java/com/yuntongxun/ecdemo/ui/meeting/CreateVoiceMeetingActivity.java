package com.yuntongxun.ecdemo.ui.meeting;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.base.CCPFormInputView;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecsdk.ECMeetingManager;

/**
 * com.yuntongxun.ecdemo.ui.meeting in ECDemo_Android
 * Created by Jorstin on 2015/7/22.
 */
public class CreateVoiceMeetingActivity extends ECSuperActivity
        implements View.OnClickListener{

    private static final String TAG = "ECSDK.Demo.CreateVoiceMeetingActivity";
    public static final String EXTRA_MEETING_PARAMS = "com.yuntongxun.meeting_params";
    /**语音群聊房间名称输入控件*/
    private CCPFormInputView mNameFormInputView;
    /**语音群聊房间名称输入输入框*/
    private EditText mNameEditView;
    /**语音群聊房间密码输入控件*/
    private CCPFormInputView mPasswordFormInputView;
    /**语音群聊房间密码输入输入框*/
    private EditText mPasswordEditView;
    /**退出是否自动解散会议*/
    private CheckedTextView mCloseCheckedView;
    /**创建成功后是否自动加入会议*/
    private CheckedTextView mJoinCheckedView;
    /**会议无成员是否自动删除会议*/
    private CheckedTextView mDelCheckedView;

    /**声音设置"*/
    private RadioButton mRb_only;
    private RadioButton mRb_all;
    private RadioButton mRb_quiet;
    private RadioGroup rg;


    @Override
    protected int getLayoutId() {
        return R.layout.create_voice_meeting;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                -1, null,
                getString(R.string.app_create),
                getString(R.string.app_title_chatroom_create), null, this);
        initView();
    }

    /**
     * 初始化界面资源
     */
    private void initView() {
        mNameFormInputView = (CCPFormInputView) findViewById(R.id.meeting_name);
        mNameEditView = mNameFormInputView.getFormInputEditView();
        mNameEditView.requestFocus();
        mPasswordFormInputView = (CCPFormInputView) findViewById(R.id.meeting_pass);
        mPasswordEditView = mPasswordFormInputView.getFormInputEditView();

        mPasswordEditView.setInputType(InputType.TYPE_CLASS_PHONE);

        mCloseCheckedView = (CheckedTextView) findViewById(R.id.auto_close);
        mCloseCheckedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloseCheckedView.toggle();
            }
        });
        mJoinCheckedView = (CheckedTextView) findViewById(R.id.auto_join);
        mJoinCheckedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJoinCheckedView.toggle();
            }
        });
        mDelCheckedView = (CheckedTextView) findViewById(R.id.auto_del);
        mDelCheckedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDelCheckedView.toggle();
            }
        });

        rg = (RadioGroup) findViewById(R.id.rg);
        mRb_only = (RadioButton) findViewById(R.id.rb_only);
        mRb_all = (RadioButton) findViewById(R.id.rb_all);
        mRb_quiet = (RadioButton) findViewById(R.id.rb_quiet);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;
            case R.id.text_right:
            	
            	
            	String meetingName=mNameEditView.getText().toString().trim();
            	String meetingPwd=mPasswordEditView.getText().toString().trim();




            	if(TextUtils.isEmpty(meetingName)){
            		
            		ToastUtil.showMessage(R.string.create_meetingName_Null);
            		return;
            	}
            	if(!TextUtils.isEmpty(meetingPwd)&&meetingPwd.length()>8){
            		ToastUtil.showMessage("密码超过长度限制");
            		return;
            	}

            	int voicePosition = 0;
            	switch (rg.getCheckedRadioButtonId()){
                    case R.id.rb_only:
                        voicePosition = 0;
                        break;
                    case R.id.rb_all:
                        voicePosition = 1;
                        break;
                    case R.id.rb_quiet:
                        voicePosition = 2;
                        break;
                }
            	
                ECMeetingManager.ECCreateMeetingParams.Builder builder = new ECMeetingManager.ECCreateMeetingParams.Builder();
                // 设置语音会议房间名称
                builder.setMeetingName(mNameEditView.getText().toString().trim())
                // 设置语音会议房间加入密码
                .setMeetingPwd(mPasswordEditView.getText().toString().trim())
                // 设置语音会议创建者退出是否自动解散会议
                .setIsAutoClose(mCloseCheckedView.isChecked())
                // 设置语音会议创建成功是否自动加入
                .setIsAutoJoin(mJoinCheckedView.isChecked())
                // 设置语音会议背景音模式
                .setVoiceMod(ECMeetingManager.ECCreateMeetingParams.ToneMode.values()[voicePosition])
                // 设置语音会议所有成员退出后是否自动删除会议
                .setIsAutoDelete(mDelCheckedView.isChecked());

                Intent intent = new Intent();
                intent.putExtra(EXTRA_MEETING_PARAMS , builder.create());
                setResult(RESULT_OK , intent);
                finish();
                break;
        }
    }
}
