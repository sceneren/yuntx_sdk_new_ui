package com.yuntongxun.ecdemo.ui.phonemodel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
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
import com.yuntongxun.ecsdk.ECInitParams;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InvalidClassException;
import java.net.ConnectException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.yuntongxun.ecdemo.R.id.tv_getcode;

/**
 * Created by luhuashan on 17/8/11.
 * email huashan2007@sina.cn
 */
public class RegisterFragment extends LazyFrament {


    @BindView(R.id.ed_name)
    EditText edName;
    @BindView(R.id.ed_yanzhengma)
    EditText edYanzhengma;
    @BindView(R.id.ed_pwd)
    EditText edPwd;
    @BindView(R.id.ll_top)
    LinearLayout llTop;

    Unbinder unbinder;

    @BindView(tv_getcode)
    TextView tvCode;

    @BindView(R.id.phone_regi)
    Button phoneRegi;

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        return fragment;
    }

    private PhoneUI getUI(){
        return (PhoneUI)getActivity();
    }


    @Override
    public void fetchData() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.phone_register_plus;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

    }

    @Override
    protected void initWidgetActions() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    private void handleGetSms(String phone){
        getUI().showCommonProcessDialog();
        Observer<Object> o = new Observer<Object>(){
            @Override
            public void onSubscribe(Disposable d) {
                getUI().dismissCommonPostingDialog();
            }

            @Override
            public void onNext(Object movieEntity) {
                getUI().dismissCommonPostingDialog();
                if(movieEntity!=null){
                    LogUtil.e(movieEntity.toString());
                    ResponseBody body = (ResponseBody)movieEntity;
                    try {
                        String s = new String(body.bytes());
                        if(DemoUtils.isTrue(s)){
                            ToastUtil.showMessage("获取验证码成功");
                        }else {
                            ToastUtil.showMessage("获取验证码失败");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onError(Throwable e) {

                getUI().dismissCommonPostingDialog();
                ToastUtil.showMessage("获取验证码失败");
            }
            @Override
            public void onComplete() {
                getUI().dismissCommonPostingDialog();

            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildSmsBody(phone, getActivity(), time);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());

        String s =  RestServerDefines.QR_APK?RestServerDefines.APPKER_CODE:RestServerDefines.APPKER;
        HttpMethods.getInstance(time).postSms(o,s,url,body);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    ECAlertDialog buildAlert;

    private void handleRegister(final String phoneR, final String pwd, String sms){

        getUI().showCommonProcessDialog();
        Observer<Object> subscriber =new Observer<Object>(){
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
                getUI().dismissCommonPostingDialog();
            }

            @Override
            public void onError(Throwable e){
                ToastUtil.showMessage("注册失败");
                getUI().dismissCommonPostingDialog();
                LogUtil.e(e.toString());
            }
            @Override
            public void onSubscribe(Disposable d) {
                getUI().dismissCommonPostingDialog();
            }

            @Override
            public void onNext(Object movieEntity){
                getUI().dismissCommonPostingDialog();
                if(movieEntity!=null){
                    LogUtil.e(movieEntity.toString());
                    ResponseBody body = (ResponseBody)movieEntity;
                    try {
                        String s = new String(body.bytes());
                        String error ="";
                        JSONObject j = new JSONObject(s);
                        if(j!=null&&j.has("statusMsg")){
                            error = j.getString("statusMsg");
                        }
                        if(DemoUtils.isTrue(s)){
                            ToastUtil.showMessage("注册成功");

                       buildAlert = ECAlertDialog.buildAlert(mContext
                                    , "注册成功,是否自动登录", "取消", "登录", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (buildAlert != null) {
                                                buildAlert.dismiss();
                                            }
                                            buildAlert.setCanceledOnTouchOutside(true);
                                        }
                                    }, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
//                                            showDownloadDialog(url);

                                            autoLogin(phoneR,pwd);
                                        }
                                    }
                            );
                            buildAlert.setTitle(R.string.app_tip);
                            buildAlert.show();

                        }else {
                            ToastUtil.showMessage(error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildRegister(phoneR, getActivity(), time, sms, pwd);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).register(subscriber, RestServerDefines.APPKER, url, body);
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
                    ResponseBody body = (ResponseBody)movieEntity;
                    try {
                        String s = new String(body.bytes());
                        LogUtil.e("network", s);
                        if (DemoUtils.isTrue(s)) {
                            handleResult(phone, pwd);
                        } else {
                            ToastUtil.showMessage(DemoUtils.getErrMsg(s));
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
            saveAccount(phone,spwd);
        } catch (InvalidClassException e) {
            e.printStackTrace();
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

    private void saveAccount(String phone,String pwd) throws InvalidClassException {
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


    private void autoLogin(String phoneR, String pwd) {

        handleLogin(phoneR, pwd);

    }

    public  String getSig(String stime){

        String s = CCPAppManager.getAppKey()+CCPAppManager.getAppToken()+stime;
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

    CountDownTimer timer = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {

            if(tvCode!=null){
                tvCode.setText(millisUntilFinished / 1000 + "秒");
            }
        }
        @Override
        public void onFinish() {
            if(tvCode!=null){
                tvCode.setEnabled(true);
                tvCode.setText("获取验证码");
            }
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
        if(timer!=null){
            timer.cancel();
        }
    }

    @OnClick({tv_getcode, R.id.phone_regi})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case tv_getcode:

                String phone=  edName.getText().toString().trim();
                if(TextUtils.isEmpty(phone)||phone.length()!=11){
                    ToastUtil.showMessage("请输入正确的手机号");
                    return;
                }
                timer.start();
                tvCode.setEnabled(false);
                handleGetSms(phone);
                break;
            case R.id.phone_regi:
                String phoneR=  edName.getText().toString().trim();
                if(TextUtils.isEmpty(phoneR)||phoneR.length()!=11){
                    ToastUtil.showMessage("请输入正确的手机号");
                    return;
                }
                String sms=  edYanzhengma.getText().toString().trim();
                if(TextUtils.isEmpty(sms)){
                    ToastUtil.showMessage("请输入验证码");
                    return;
                }
                String pwd=  edPwd.getText().toString().trim();
                if(TextUtils.isEmpty(pwd)){
                    ToastUtil.showMessage("请输入密码");
                    return;
                }
                handleRegister(phoneR,pwd,sms);
                break;
        }
    }
}
