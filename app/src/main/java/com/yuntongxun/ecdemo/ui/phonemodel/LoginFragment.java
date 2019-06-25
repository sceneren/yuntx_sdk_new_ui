package com.yuntongxun.ecdemo.ui.phonemodel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.Base64;
import com.yuntongxun.ecdemo.common.utils.CommomUtil;
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
import com.yuntongxun.ecdemo.ui.LazyFrament;
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
import java.net.ConnectException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by luhuashan on 17/8/11.
 * email huashan2007@sina.cn
 */
public class LoginFragment extends LazyFrament {


    @BindView(R.id.ed_name)
    EditText edName;
    @BindView(R.id.ed_pwd)
    EditText edPwd;
    @BindView(R.id.ll_top)
    LinearLayout llTop;
    @BindView(R.id.phone_login)
    Button phoneLogin;
    @BindView(R.id.tv_forget)
    TextView tvForget;
    Unbinder unbinder;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    private String phone;
    private String pwd;


    @Override
    public void fetchData() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.phone_register;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

        registerReceiver(new String[]{SDKCoreHelper.ACTION_SDK_CONNECT});
        phoneLogin.setEnabled(false);
          String tel = (String) SharedPreferencesUtils.getParam2(getActivity(),"acc","");
        if(!TextUtils.isEmpty(tel)){
            edName.setText(tel);

        }

        getToken();

    }

    private void getToken() {
        final Observer<Object> subscriber = new Observer<Object>() {
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object movieEntity) {
                if (movieEntity != null) {
                    ResponseBody body = (ResponseBody) movieEntity;
                    try {
                        String s = new String(body.bytes());
                        LogUtil.e("network", s);
                        if (DemoUtils.isTrue(s)) {
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.has("appToken")) {
                                String token = jsonObject.getString("appToken");
                                CCPAppManager.putHttpToken(token);
                            }
                        } else {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getTokenSig(getTokenAuth(time));
        JSONObject map = HttpMethods.buildGetToken(RestServerDefines.APPKER_CODE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());

        String s = RestServerDefines.QR_APK ? RestServerDefines.APPKER_CODE : RestServerDefines.APPKER;

        HttpMethods.getInstance(time).getToken(subscriber, s, url, body);


    }

    @Override
    protected void initWidgetActions() {
        edName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 11 && edPwd.getText().toString().trim().length() >= 6) {
                    phoneLogin.setEnabled(true);
                } else {
                    phoneLogin.setEnabled(false);
                }
            }
        });

        edPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 6 && edName.getText().toString().trim().length() == 11) {
                    phoneLogin.setEnabled(true);
                } else {
                    phoneLogin.setEnabled(false);
                }
            }
        });
    }

    private PhoneUI getUI() {
        return (PhoneUI) getActivity();
    }

    private void handleLogin(final String phone, final String pwd) {
        getUI().showCommonProcessDialog();
        final Observer<Object> subscriber = new Observer<Object>() {
            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                getUI().dismissCommonPostingDialog();
                phoneLogin.setClickable(true);

                if (e instanceof ConnectException) {
                    ToastUtil.showMessage("网络异常请检查重试");
                } else {
                    ToastUtil.showMessage("登录失败");
                }
            }

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object movieEntity) {
                getUI().dismissCommonPostingDialog();
                if (movieEntity != null) {
                    ResponseBody body = (ResponseBody) movieEntity;
                    try {
                        String s = new String(body.bytes());
                        LogUtil.e("network", s);
                        if (DemoUtils.isTrue(s)) {
                            handleResult(phone, pwd);
                        } else {
                            phoneLogin.setClickable(true);
                            ToastUtil.showMessage(DemoUtils.getErrMsg(s));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        phoneLogin.setClickable(true);
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

    public String getSig(String stime) {

        String appid = RestServerDefines.QR_APK ? RestServerDefines.APPKER_CODE : RestServerDefines.APPKER;
        String token = RestServerDefines.QR_APK ? CCPAppManager.getHttpToken() : RestServerDefines.TOKEN;


        String s = appid + token + stime;
        return getMessageDigest(s.getBytes());
    }

    public String getTokenAuth(String time) {
        String s = "yuntongxun" + ":" + time;
        return Base64.encode(s.getBytes());
    }

    public String getTokenSig(String stime) {
        String s = "yuntongxun" + "ytx123" + stime;
        return getMessageDigest(s.getBytes());
    }

    public static String getMessageDigest(byte[] input) {
        char[] source = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f'};
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

    private void handleResult(String phone, String spwd) {
        try {
            saveAccount();
            phoneLogin.setClickable(true);
        } catch (InvalidClassException e) {
            e.printStackTrace();
        }

    }

    private void saveAccount() throws InvalidClassException {
        String appKey = RestServerDefines.APPKER_CODE;
        String token = CCPAppManager.getHttpToken();
        String mobile = phone;
        ClientUser user = CCPAppManager.getClientUser();
        if (user == null) {
            user = new ClientUser(mobile);
        } else {
            user.setUserId(mobile);
        }
        user.setAppToken(token);
        user.setAppKey(appKey);
        user.setLoginAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);
        CCPAppManager.setClientUser(user);
        SharedPreferencesUtils.setParam(CCPAppManager.getContext(), "pwd", pwd);

        ContactsCache.getInstance().load();
        doLauncherAction();

        SDKCoreHelper.init(getActivity(), ECInitParams.LoginMode.FORCE_LOGIN);

        ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, user.toString(), true);
        ArrayList<ECContacts> objects = ContactLogic.initContacts();
        objects = ContactLogic.converContacts(objects);
        ContactSqlManager.insertContacts(objects);
    }


    @Override
    protected void handleReceiver(Context context, Intent intent) {
        // super.handleReceiver(context, intent);
        int error = intent.getIntExtra("error", -1);
        if (SDKCoreHelper.ACTION_SDK_CONNECT.equals(intent.getAction())) {
            // 初始注册结果，成功或者失败
            if (SDKCoreHelper.getConnectState() == ECDevice.ECConnectState.CONNECT_SUCCESS
                    && error == SdkErrorCode.REQUEST_SUCCESS) {

//                dismissCommonPostingDialog();
//                ContactsCache.getInstance().load();
//                doLauncherAction();

                return;
            }
            if (intent.hasExtra("error")) {
                if (SdkErrorCode.CONNECTTING == error) {
                    return;
                }
                if (error == -1) {
//                    ToastUtil.showMessage("请检查登陆参数是否正确[" + error + "]");
                } else {
//                    dismissCommonPostingDialog();
                }
//                ToastUtil.showMessage("登录失败，请稍后重试[" + error + "]");
            }
//            dismissCommonPostingDialog();
        }
    }

    private void doLauncherAction() {
        try {
            Intent intent = new Intent(getActivity(), MainAct.class);
            intent.putExtra("launcher_from", 1);
            // 注册成功跳转
            startActivity(intent);
            getActivity().finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @OnClick({R.id.ll_top, R.id.phone_login, R.id.tv_forget})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_top:
                break;
            case R.id.phone_login:
                phone = edName.getText().toString().trim();
                pwd = edPwd.getText().toString().trim();
                if (TextUtils.isEmpty(phone)
                        || (!DemoUtils.isPhone(phone))) {
                    ToastUtil.showMessage("请输入正确的手机号");
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    ToastUtil.showMessage("请输入正确的密码");
                    return;
                }

                if (!CommomUtil.isFastDoubleClick(R.id.phone_login)) {
                    phoneLogin.setClickable(false);
                    handleLogin(phone, pwd);
                }
                break;
            case R.id.tv_forget:
                getActivity().startActivity(new Intent(getContext(), ModifyPwdUI.class));
                break;
        }
    }


}
