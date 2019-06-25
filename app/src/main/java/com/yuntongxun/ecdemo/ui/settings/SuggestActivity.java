package com.yuntongxun.ecdemo.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import butterknife.BindView;

/**
 * Created by luhuashan on 16/7/29.
 */
public class SuggestActivity extends BaseActivity implements View.OnClickListener{

    @BindView(R.id.title_bar)
    TitleBar titleBar;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_web_suggest_url;
    }

    @Override
    protected void initWidgetAciotns() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);



          WebView mWebView =(WebView)findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
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
        String url = "https://imapp.yuntongxun.com:443/2016-08-15/Application/20150314000000110000000000000010"+"/IMPlus/Suggestion.shtml?userName="+CCPAppManager.getClientUser().getUserId();
        mWebView.loadUrl(url);

    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initTooleBar(titleBar,true,"意见反馈");
    }

    @Override
    public void onClick(View v){

        switch (v.getId()){
            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;
        }

    }
}
