package com.yuntongxun.ecdemo.ui.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.MainAct;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.phonemodel.PhoneUI;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;


public class SplashActivity extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        ECHandlerHelper.postDelayedRunnOnUI(new Runnable() {
            @Override
            public void run() {
                String account = getAutoRegistAccount();
                if (TextUtils.isEmpty(account)) {
                    if (RestServerDefines.QR_APK) {
                        startActivity(new Intent(SplashActivity.this, PhoneUI.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }
                    finish();
                    return;
                }else {
                    startActivity(MainAct.class);
                    finish();
                }
            }
        },1000);





    }
    private String getAutoRegistAccount(){
        SharedPreferences sharedPreferences = ECPreferences
                .getSharedPreferences();
        ECPreferenceSettings registAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
        String registAccount = sharedPreferences.getString(registAuto.getId(),
                (String) registAuto.getDefaultValue());
        return registAccount;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.splash;
    }

    @Override
    protected void initWidgetAciotns() {

    }
}
