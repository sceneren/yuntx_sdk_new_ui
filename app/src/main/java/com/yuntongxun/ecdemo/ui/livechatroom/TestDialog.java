package com.yuntongxun.ecdemo.ui.livechatroom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.SettingItem;

public class TestDialog extends DialogFragment {

	// mUniqueFlag作用是唯一码,可以使返回时做判断
	private int mUniqueFlag = -1;
	private onTestListener mOnListener;
	private EditText meditTextName, meditTextHigh;
	protected Button mButtonPositive;
	private static boolean   isChec;

	/**
	 * 新建实例
	 *
	 * @param title
	 * @param unique
	 * @param strName
	 * @param strHigh
	 * @return
	 */
	public static TestDialog newInstance(String title, int unique,
										 String strName, String strHigh,String ext,boolean isCheck) {
		TestDialog tDialog = new TestDialog();
		Bundle args = new Bundle();
		args.putString("SelectTemplateTitle", title);
		args.putInt("MultipleTemplate", unique);
		args.putString("TemplateName", strName);
		args.putString("TemplateHigh", strHigh);
		args.putString("ext", ext);
		args.putBoolean("isCheck", isCheck);
		tDialog.setArguments(args);
		isChec = isCheck;
		return tDialog;

	}

	public interface onTestListener {

		/**
		 *
		 * @param uniqueIdentifier
		 *            唯一标识
		 * @param strName
		 * @param strHigh
		 */
		public abstract void onTestListener(int uniqueIdentifier,
											String strName, String strHigh,String ext,boolean isMute);
	}

	// 旋转时候保存
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("InputName", meditTextName.getText().toString());
		outState.putString("InputHigh", meditTextHigh.getText().toString());
	}

	@Override
	public Dialog onCreateDialog(Bundle saveInstanceState) {
		String title = getArguments().getString("SelectTemplateTitle");
		mUniqueFlag = getArguments().getInt("MultipleTemplate");

		AlertDialog.Builder Builder = new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 触发数据回调
						if (mOnListener != null) {
                            mOnListener.onTestListener(mUniqueFlag,
                                    meditTextName.getText().toString().trim(),
                                    meditTextHigh.getText().toString().trim(), meditTextExt.getText().toString().trim(), settingItem.isChecked());
                        }
					}
				}).setNegativeButton("取消", null);

		// 添加xml布局
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.test_dialog, null);
		setupUI(view);

		// 旋转后,恢复数据
		if (saveInstanceState != null) {
			String strName = saveInstanceState.getString("InputName");
			if (strName != null) {
                meditTextName.setText(strName);
            }

			String strHigh = saveInstanceState.getString("InputHigh");
			if (strHigh != null) {
                meditTextHigh.setText(strHigh);
            }
		}
		Builder.setView(view);

		//创建对话框
		AlertDialog dialog = (AlertDialog) Builder.create();
		return dialog;
	}

	private EditText meditTextExt;
	private SettingItem settingItem;
	private void setupUI(View view){
		if (view == null) {
            return;
        }
		String strName = getArguments().getString("TemplateName");
		String strHigh = getArguments().getString("TemplateHigh");
		meditTextName = (EditText) view.findViewById(R.id.modify_name);
		meditTextHigh = (EditText) view.findViewById(R.id.modify_desc);
		meditTextExt = (EditText) view.findViewById(R.id.modify_ext);
		settingItem = (SettingItem)view.findViewById(R.id.modify_mute);

		meditTextName.setText(strName);
		meditTextHigh.setText(strHigh);

		settingItem.setChecked(isChec);


		settingItem.getCheckedTextView().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(settingItem == null) {
					return ;
				}
				settingItem.toggle();
			}
		});

	}

	// onAttach是关联activity的,用接口回调
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnListener = (onTestListener) activity;
		} catch (ClassCastException e) {
			dismiss();
		}
	}

}