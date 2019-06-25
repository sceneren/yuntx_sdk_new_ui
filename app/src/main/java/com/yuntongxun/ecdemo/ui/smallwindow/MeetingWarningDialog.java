package com.yuntongxun.ecdemo.ui.smallwindow;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.SettingsCompat;


/**
 * @author 容联•云通讯
 * @version 5.2.0
 * @since 2016-07-22
 */

public class MeetingWarningDialog extends Activity {

    public static final String TAG = "RongXin.VoipWarningDialog";

    private ECAlertDialog mAlertDialog;

    private void init() {
        if (getIntent() == null) {
            return;
        }
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            LogUtil.e(TAG, "invalid params");
        }

        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this,R.string.voip_warning_dialog_tips_title, null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                SettingsCompat.manageDrawOverlays(MeetingWarningDialog.this);
                finish();
            }
        });
        buildAlert.setTitle(R.string.chatting_resend_title);
        buildAlert.show();

    }

    /**
     * 显示一个警告对话框
     *
     * @param context 上下文
     */
    static void showWarningDialog(Context context) {
        Intent intent = new Intent(context, MeetingWarningDialog.class);
        intent.putExtra("warning_content", context.getString(R.string.voip_warning_dialog_tips));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        init();
    }
}
