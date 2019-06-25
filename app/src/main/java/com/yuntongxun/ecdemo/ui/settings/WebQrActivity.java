package com.yuntongxun.ecdemo.ui.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.Base64;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.QrUtils;
import com.yuntongxun.ecdemo.common.utils.SharedPreferencesUtils;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;

import org.apache.http.entity.StringEntity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.security.MessageDigest;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebQrActivity extends ECSuperActivity implements View.OnClickListener, QrUtils.ResultCallBack {
    TextView tv ;

    Button bu;

    @BindView(R.id.tv_group_name)
    public TextView tvName ;

    @BindView(R.id.tv_group_count)
    public TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
        url=getIntent().getStringExtra("url");
        EventBus.getDefault().register(this);

        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt,
                -1, null,
                null,
                "扫描结果", null, this);

        tv =(TextView)findViewById(R.id.tv_weba);
        bu =(Button)findViewById(R.id.bu_join_group);


        initResViews();

        if(TextUtils.isEmpty(url)){
            finish();
            return;
        }

        if(url.startsWith("http://")||url.startsWith("https://")){
            if(url.contains("appId")){
                String s = url;
                String web ="";
                s= s.replaceAll("\\{appId\\}", CCPAppManager.getClientUser().getAppKey());
                web = s+"&userName="+CCPAppManager.getClientUser().getUserId();
                long time  = System.currentTimeMillis();
                String urll = web+"&sig="+getSigQ(time)+"&timestamp="+time;
                LogUtil.e(urll);
                mWebView.loadUrl(urll);

            }else {
                mWebView.loadUrl(url);
            }
        }else if(url.contains("joinGroup")){
            bu.setVisibility(View.VISIBLE);
            bu.setOnClickListener(this);
            try {
                 jsonObject = new JSONObject(url);
                String data = jsonObject.getString("data");
                String text =  new String(Base64.decode(data));
                jsonObject = new JSONObject(text);
                Log.e("aa",text);


                if(jsonObject!=null&&jsonObject.has("groupid")){
                    String groupId = jsonObject.getString("groupid");
                    QrUtils.GroupId = groupId;
                    tvName.setText("群组id:"+groupId);
                }
                if(jsonObject!=null&&jsonObject.has("count")){
                    String count = jsonObject.getString("count");
                    tvCount.setText("群组人数:"+count);
                }


            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else  {
            tv.setText(url);
        }
    }

    public  String getSig(long time){
        String s =CCPAppManager.getClientUser().getAppKey()+CCPAppManager.getClientUser().getAppToken()+CCPAppManager.getUserId()+time;
        return getMessageDigest(s.getBytes());
    }
    public  String getSigQ(long time){
//
        String pwds = (String)SharedPreferencesUtils.getParam(CCPAppManager.getContext(), "pwd", "");
        LogUtil.e("aa","aa"+pwds);
        String pwd = getMessageDigest(pwds.getBytes()).toUpperCase();
        String s =CCPAppManager.getClientUser().getAppKey()+CCPAppManager.getClientUser().getAppToken()+CCPAppManager.getUserId()+time+pwd;
        return (getMessageDigest(s.getBytes())).toUpperCase();
    }

    public static String getMessageDigest(byte[] input){
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

    private JSONObject jsonObject ;


    private void addData(){
        StringEntity s = null;
        try {
            s = buildBody();
        } catch (Exception e){
            e.printStackTrace();
        }
        if(s!=null){
            QrUtils.init(s, this);
            new Thread(QrUtils.getInstance()).start();
        }
    }

    @Subscribe          //订阅事件FirstEvent
    public  void onEventMainThread(Object event){

        if("111".equalsIgnoreCase(event.toString())){
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private StringEntity buildBody()throws Exception{
        JSONObject object = new JSONObject();
        object.put("groupId", jsonObject.getString("groupid"));
        object.put("joinUserAcc", CCPAppManager.getClientUser().getUserId());
        object.put("generateQrUserName", jsonObject.getString("name"));
        object.put("count", jsonObject.getString("count"));
        object.put("codeCreateTime", jsonObject.getString("time"));
        StringEntity s =new StringEntity(object.toString(),"UTF-8");
        return  s;
    }



    private  String url;
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;
            case R.id.text_right:


                break;

            case R.id.bu_join_group:

                addData();

                break;

            default:
                break;
        }

    }


    @Override
    protected int getLayoutId() {



        return R.layout.activity_web_about_qr;
    }

    private WebView mWebView;

    private void initResViews() {
        mWebView = (WebView) findViewById(R.id.webviewa);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

    }

    @Override
    public void onSuccess(String reslut) {

    }
}
