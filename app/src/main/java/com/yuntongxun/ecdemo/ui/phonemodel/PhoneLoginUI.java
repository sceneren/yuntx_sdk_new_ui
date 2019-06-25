package com.yuntongxun.ecdemo.ui.phonemodel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.base.CCPFormInputView;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.SharedPreferencesUtils;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.core.ClientUser;
import com.yuntongxun.ecdemo.core.ContactsCache;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecdemo.ui.MainAct;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.contact.ContactLogic;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.SdkErrorCode;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InvalidClassException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class PhoneLoginUI extends ECSuperActivity implements View.OnClickListener{


    @BindView(R.id.phone_login_mobile)
    public CCPFormInputView ccpPhone;

    @BindView(R.id.phone_login_pwd)
    public CCPFormInputView ccpPwd;

    @BindView(R.id.phone_sign_in_button)
    public Button buLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                -1, null,
                null,
                getString(R.string.app_company_login_phone), null, this);

        registerReceiver(new String[]{SDKCoreHelper.ACTION_SDK_CONNECT});

        ButterKnife.bind(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_phone_login_ui;
    }


    private String phone ;
    private String spwd ;

    @OnClick(R.id.phone_sign_in_button)
    @Override
    public void onClick(View v){
        if(v.getId()==R.id.phone_sign_in_button){

              phone  = ccpPhone.getFormInputEditView().getText().toString().trim();
              spwd  = ccpPwd.getFormInputEditView().getText().toString().trim();

             if(TextUtils.isEmpty(phone)){
                 ToastUtil.showMessage("手机号不能为空");
                 return;
             }
             if(TextUtils.isEmpty(spwd)){
                 ToastUtil.showMessage("密码不能为空");
                 return;
             }
            handleLogin(phone,spwd);

        }else if(v.getId()==R.id.btn_left){
            hideSoftKeyboard();
            finish();
        }

    }

    public  String getSig(String stime){
        String s = RestServerDefines.APPKER+CCPAppManager.getAppToken()+stime;
        return getMessageDigest(s.getBytes());
    }

    public static String getMessageDigest(byte[] input) {
        char[] source = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(input);
            byte[] digest = mDigest.digest();
            int length = digest.length;
            char[] result = new char[length * 2];
            int j = 0;
            for (byte l : digest) {
                result[(j++)] = source[(l >>> 4 & 0xF)];
                result[(j++)] = source[(l & 0xF)];
            }
            return new String(result);
        } catch (Exception e) {
        }
        return null;
    }

    private void handleLogin(final String phone, String pwd){

        showCommonProcessDialog("请稍后");
        final Observer<Object> subscriber = new Observer<Object>(){
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
            }
            @Override
            public void onError(Throwable e){
                dismissCommonPostingDialog();
                ToastUtil.showMessage("登录失败");
            }


            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object movieEntity){
                if(movieEntity!=null){
                    ResponseBody body = (ResponseBody)movieEntity;
                    try {
                        String s = new String(body.bytes());
                        LogUtil.e("network",s);
                        if(DemoUtils.isTrue(s)){
                            handleResult(phone,spwd);
                        }else {
                            dismissCommonPostingDialog();
                            ToastUtil.showMessage("登录失败");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildLogin(phone, pwd);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).login(subscriber, RestServerDefines.APPKER, url, body);
    }

    private void handleResult(String phone, String spwd) {
        try {
            saveAccount();
        } catch (InvalidClassException e) {
            e.printStackTrace();
        }

    }

    private void saveAccount() throws InvalidClassException{
        String appKey = RestServerDefines.APPKER;
        String token = CCPAppManager.getAppToken();
        String mobile = phone;
        ClientUser user = CCPAppManager.getClientUser();
        if(user == null) {
            user = new ClientUser(mobile);
        } else {
            user.setUserId(mobile);
        }
        user.setAppToken(token);
        user.setAppKey(appKey);
        user.setLoginAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);

        SharedPreferencesUtils.setParam(CCPAppManager.getContext(),"pwd",spwd);
        CCPAppManager.setClientUser(user);

        ContactsCache.getInstance().load();
        doLauncherAction();

        SDKCoreHelper.init(this, ECInitParams.LoginMode.FORCE_LOGIN);
        ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, user.toString(), true);
        ArrayList<ECContacts> objects = ContactLogic.initContacts();
        objects = ContactLogic.converContacts(objects);
        ContactSqlManager.insertContacts(objects);

    }

    @Override
    protected void handleReceiver(Context context, Intent intent){
        // super.handleReceiver(context, intent);
        int error = intent.getIntExtra("error", -1);
        if (SDKCoreHelper.ACTION_SDK_CONNECT.equals(intent.getAction())){
            // 初始注册结果，成功或者失败
            if (SDKCoreHelper.getConnectState() == ECDevice.ECConnectState.CONNECT_SUCCESS
                    && error == SdkErrorCode.REQUEST_SUCCESS) {

//                dismissCommonPostingDialog();
//                ContactsCache.getInstance().load();
//                doLauncherAction();
                return;
            }
            if (intent.hasExtra("error")){
                if (SdkErrorCode.CONNECTTING == error){
                    return;
                }
                if (error == -1) {
//                    ToastUtil.showMessage("请检查登陆参数是否正确[" + error + "]");
                }else {
//                    dismissCommonPostingDialog();
                }
//                ToastUtil.showMessage("登录失败，请稍后重试[" + error + "]");
            }
//            dismissCommonPostingDialog();
        }
    }

    private void doLauncherAction(){
        try {
            Intent intent = new Intent(this, MainAct.class);
            intent.putExtra("launcher_from", 1);
            // 注册成功跳转
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
