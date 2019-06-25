package com.yuntongxun.ecdemo.ui.phonemodel;

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
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.LazyFrament;
import com.yuntongxun.ecdemo.ui.RestServerDefines;

import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
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

/**
 * Created by luhuashan on 17/8/14.
 * email huashan2007@sina.cn
 */
public class ModifyPwdFragment extends LazyFrament{
    @BindView(R.id.ed_name)
    EditText edName;


    @BindView(R.id.ed_yanzhengma)
    EditText edYanzhengma;

    @BindView(R.id.tv_getcode)
    TextView tvGetcode;
    @BindView(R.id.ll_yzm)
    LinearLayout llYzm;
    @BindView(R.id.ed_pwd)
    EditText edPwd;
    @BindView(R.id.ll_top)
    LinearLayout llTop;
    @BindView(R.id.phone_reset_pwd)
    Button phoneResetPwd;
    Unbinder unbinder;

    @Override
    public void fetchData() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.modify_pwd;
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

    private ModifyPwdUI getUI(){
        return (ModifyPwdUI)getActivity();
    }

    private void handleGetSms(String phone){
        getUI().showCommonProcessDialog();
        Observer<Object> o = new Observer<Object>(){
            @Override
            public void onSubscribe(Disposable d) {
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
                            timer.start();
                        }else {
                            ToastUtil.showMessage("获取验证码失败");
                            tvGetcode.setEnabled(true);
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
                tvGetcode.setEnabled(true);
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
        HttpMethods.getInstance(time).postSms(o, RestServerDefines.APPKER,url,body);
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
            if(tvGetcode!=null){
                tvGetcode.setText(millisUntilFinished / 1000 + "秒");
            }
        }
        @Override
        public void onFinish(){
            if(tvGetcode!=null){
                tvGetcode.setEnabled(true);
                tvGetcode.setText("获取验证码");
            }

        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void handleRegister(String phoneR, String pwd, String sms){
                getUI().showCommonProcessDialog();
            Observer<Object> subscriber = new Observer<Object>(){
                @Override
                public void onError(Throwable e){
                    LogUtil.e(e.toString());
                    getUI().dismissCommonPostingDialog();
                    ToastUtil.showMessage("修改失败");
                }

                @Override
                public void onComplete() {
                    LogUtil.e("onCompleted");
                    getUI().dismissCommonPostingDialog();
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
                            if(DemoUtils.isTrue(s)){
                                ToastUtil.showMessage("修改成功");
                                getActivity().finish();
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
            JSONObject map = HttpMethods.buildNewPwd(phoneR, pwd, sms);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
            HttpMethods.getInstance(time).update(subscriber, RestServerDefines.APPKER, url, body);



    }

    @OnClick({R.id.tv_getcode, R.id.phone_reset_pwd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_getcode:
                String phone=  edName.getText().toString().trim();
                if(TextUtils.isEmpty(phone)||phone.length() !=11){
                    ToastUtil.showMessage("请输入正确的手机号");
                    return;
                }
                handleGetSms(phone);
                tvGetcode.setEnabled(false);
                break;
            case R.id.phone_reset_pwd:
                String phoneR=  edName.getText().toString().trim();
                if(TextUtils.isEmpty(phoneR)||phoneR.length() !=11){
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
