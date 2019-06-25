package com.yuntongxun.ecdemo.ui.phonemodel;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.base.CCPFormInputView;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecdemo.ui.RestServerDefines;

import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class PhoneRegisterUI extends ECSuperActivity implements View.OnClickListener{


    @BindView(R.id.phone_regi_mobile)
    public CCPFormInputView regiPhone;

    @BindView(R.id.phone_regi_sms)
    public CCPFormInputView regiSms;

    @BindView(R.id.phone_regi_pwd)
    public CCPFormInputView regiPwd;

    @BindView(R.id.phone_sms)
    public Button buSms;

    @BindView(R.id.phone_regi)
    public Button buRegi;

    private String from ;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

         from  =   getIntent().getStringExtra("from");

        ButterKnife.bind(this);
        if("1".equalsIgnoreCase(from)){
            getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                    -1, null,
                    null,
                    getString(R.string.app_company_regi_phone_pwd), null, this);

            buRegi.setText("确认修改密码");

        }else {
            getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                    -1, null,
                    null,
                    getString(R.string.app_company_regi_phone), null, this);
            buRegi.setText("确认注册");
        }

    }

    @Override
    protected int getLayoutId(){
        return R.layout.activity_phone_register_ui;
    }

    @OnClick({R.id.phone_sms,R.id.phone_regi})
    void butterknifeOnItemClick(View v){

        switch (v.getId()){
            case R.id.phone_sms:
                 String phone=  regiPhone.getFormInputEditView().getText().toString().trim();
                 if(TextUtils.isEmpty(phone)){
                     ToastUtil.showMessage("请输入手机号");
                     return;
                 }
                timer.start();

                buSms.setEnabled(false);

                handleGetSms(phone);
                break;
            case R.id.phone_regi:

                String phoneR=  regiPhone.getFormInputEditView().getText().toString().trim();
                if(TextUtils.isEmpty(phoneR)){
                    ToastUtil.showMessage("请输入手机号");
                    return;
                }
                String sms=  regiSms.getFormInputEditView().getText().toString().trim();
                if(TextUtils.isEmpty(sms)){
                    ToastUtil.showMessage("请输入验证码");
                    return;
                }
                String pwd=  regiPwd.getFormInputEditView().getText().toString().trim();
                if(TextUtils.isEmpty(pwd)){
                    ToastUtil.showMessage("请输入密码");
                    return;
                }

                handleRegister(phoneR,pwd,sms);

                break;
        }

    }

    private void handleRegister(String phoneR, String pwd, String sms){
        if("1".equalsIgnoreCase(from)){
            showCommonProcessDialog("请稍后");
            Observer<Object> subscriber = new Observer<Object>(){
                @Override
                public void onError(Throwable e){
                    LogUtil.e(e.toString());
                    ToastUtil.showMessage("修改失败");
                }

                @Override
                public void onComplete() {
                    LogUtil.e("onCompleted");
                    dismissCommonPostingDialog();
                }

                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Object movieEntity){
                    if(movieEntity!=null){
                        LogUtil.e(movieEntity.toString());
                        ResponseBody body = (ResponseBody)movieEntity;
                        try {
                            String s = new String(body.bytes());
                            if(DemoUtils.isTrue(s)){
                                ToastUtil.showMessage("修改成功");
                                finish();
                            }else {
                                ToastUtil.showMessage("修改失败");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            String time = DateUtil.formatNowDate(new Date());
            String url = getSig(time);
            JSONObject map = HttpMethods.buildNewPwd(phoneR,  pwd,sms);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
            HttpMethods.getInstance(time).update(subscriber, RestServerDefines.APPKER, url, body);

            return;
        }
        showCommonProcessDialog("请稍后");
        Observer<Object> subscriber =new Observer<Object>(){
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
                dismissCommonPostingDialog();
            }

            @Override
            public void onError(Throwable e){
                ToastUtil.showMessage("注册失败");
                LogUtil.e(e.toString());
            }
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object movieEntity){
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
        JSONObject map = HttpMethods.buildRegister(phoneR, this, time, sms, pwd);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).register(subscriber, RestServerDefines.APPKER, url, body);

    }

    public  String getSig(String stime){
        String s = RestServerDefines.APPKER+ CCPAppManager.getAppToken()+stime;
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
            buSms.setText(millisUntilFinished/1000 + "秒");
        }

        @Override
        public void onFinish() {
            buSms.setEnabled(true);
            buSms.setText("获取验证码");
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    private void handleGetSms(String phone){
        Observer<Object> o = new Observer<Object>(){

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object movieEntity) {
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

                ToastUtil.showMessage("获取验证码失败");
            }

            @Override
            public void onComplete() {

            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildSmsBody(phone, this, time);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).postSms(o,RestServerDefines.APPKER,url,body);

    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;
        }
    }
}
