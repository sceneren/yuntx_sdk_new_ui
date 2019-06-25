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




import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.yuntongxun.ecdemo.ECCircumscription;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager.ECCreateMeetingParams;
import com.yuntongxun.ecsdk.ECMeetingManager.ECCreateMeetingParams.ToneMode;
import com.yuntongxun.ecsdk.ECMeetingManager.ECMeetingType;
import com.yuntongxun.ecsdk.ECMeetingManager.OnCreateOrJoinMeetingListener;
import com.yuntongxun.ecsdk.SdkErrorCode;

/**
 * 
 * @author luhuashan创建视频会议界面
 *
 */
public class CreateVideoConference extends VideoconferenceBaseActivity
		implements View.OnClickListener {

	private com.yuntongxun.ecdemo.common.base.CCPFormInputView mRoom_name;
	private RadioGroup mRg;
	private RadioButton mRb_only;
	private RadioButton mRb_all;
	private RadioButton mRb_quiet;
	private CheckedTextView mAuto_del;
	private CheckedTextView mAuto_close;
	private CheckedTextView mAuto_join;

	private int autoDelete = 1;

	/**房间名称输入输入框*/
	private EditText mNameEditView;

	protected static final String TAG = "CreateVideoConference";


	private ToneMode voiceMod = ToneMode.ALL;
	private int mVideoConfType = VideoconferenceConversation.TYPE_MULIT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                -1, null,
				getString(R.string.app_create),
                getString(R.string.videomeeting_create), null, this);

		initResourceRefs();
		initialize(savedInstanceState);

	}

	private void initResourceRefs() {

		bindViews();

		mNameEditView = mRoom_name.getFormInputEditView();
		mNameEditView.requestFocus();

		mAuto_del.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAuto_del.toggle();
			}
		});

		mAuto_close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAuto_close.toggle();
			}
		});

		mAuto_join.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAuto_join.toggle();
			}
		});

		mRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int id) {
				switch (id) {
					case R.id.rb_all:
						voiceMod = ToneMode.ALL;
						break;
					case R.id.rb_only:
						voiceMod = ToneMode.ONLY_BACKGROUND;
						break;
					case R.id.rb_quiet:
						voiceMod = ToneMode.NONE;
						break;

				}
			}
		});


	}

	private void bindViews() {

		mRoom_name = (com.yuntongxun.ecdemo.common.base.CCPFormInputView) findViewById(R.id.room_name);
		mRg = (RadioGroup) findViewById(R.id.rg);
		mRb_only = (RadioButton) findViewById(R.id.rb_only);
		mRb_all = (RadioButton) findViewById(R.id.rb_all);
		mRb_quiet = (RadioButton) findViewById(R.id.rb_quiet);

		mAuto_del = (CheckedTextView) findViewById(R.id.auto_del);
		mAuto_close = (CheckedTextView) findViewById(R.id.auto_close);
		mAuto_join = (CheckedTextView) findViewById(R.id.auto_join);
	}


	private void initialize(Bundle savedInstanceState) {

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.text_right:
			HideSoftKeyboard();
			
			String name=mNameEditView.getText().toString().trim();
			if(TextUtils.isEmpty(name)){
				ToastUtil.showMessage("请输入会议名称");
				return;
			}
			if(name.length()> ECCircumscription.ACCONT){
				ToastUtil.showMessage("您输入的房间名超过限制了");
				return;
			}
			if(!DemoUtils.isValidNormalAccount(name)){
				ToastUtil.showMessage("您输入的房间名包含非法字符");
				return;
			}

			
			if (!mAuto_join.isChecked()) {
				if (mVideoConfType == 0) {

				} else {
					showConnectionProgress(getString(R.string.str_dialog_message_default));
					
					ECCreateMeetingParams.Builder builder = new ECCreateMeetingParams.Builder();
					
					builder.setMeetingName(
							mNameEditView.getText().toString())
							.setSquare(5).setVoiceMod(voiceMod)
							.setIsAutoDelete(mAuto_del.isChecked())
							.setIsAutoJoin(false).setKeywords("")
							.setMeetingPwd("")
							.setIsAutoClose(mAuto_close.isChecked());
					
					
					ECCreateMeetingParams params = builder.create();
					
					if(!checkSDK()){
						return;
					}
					ECDevice.getECMeetingManager().createMultiMeetingByType(
							params, ECMeetingType.MEETING_MULTI_VIDEO,
							new OnCreateOrJoinMeetingListener() {

								@Override
								public void onCreateOrJoinMeeting(
										ECError reason, String meetingNo) {
									closeConnectionProgress();
									LogUtil.e(TAG, reason.toString() + "---"
											+ meetingNo);
									if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
										finish();
									} else {
										if(reason.errorCode==111007||reason.errorCode==111000){

											ToastUtil.showMessage("您输入的房间名称含有不支持类型符号");
										}else {

											ToastUtil.showMessage("创建会议失败,错误码"+reason.errorCode);
										}
									}

								}
							});

				}

				return;
			}

			Intent intent = new Intent();
			if (mVideoConfType == 0) {
			} else {
				intent.setClass(CreateVideoConference.this,
						MultiVideoconference.class);

			}
			intent.putExtra(VideoconferenceConversation.CONFERENCE_CREATOR,
					CCPAppManager.getUserId());
			intent.putExtra(ECGlobalConstants.CHATROOM_NAME, mNameEditView
					.getText().toString());
			intent.putExtra(ECGlobalConstants.IS_AUTO_CLOSE,
					mAuto_close.isChecked());
			intent.putExtra(ECGlobalConstants.AUTO_DELETE, mAuto_del.isChecked()?1:0);
			intent.putExtra(ECGlobalConstants.VOICE_MOD, voiceMod.ordinal());
			startActivity(intent);
			finish();
			break;

		case R.id.btn_left:
			
			finishVideo();
            break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			HideSoftKeyboard();
			finishVideo();
		}
		return super.onKeyDown(keyCode, event);

	}

	private void finishVideo() {
		HideSoftKeyboard();
		finish();
		
		overridePendingTransition(R.anim.push_empty_out,
				R.anim.video_push_down_out);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.video_conference_create;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}
