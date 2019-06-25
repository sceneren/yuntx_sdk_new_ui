
package com.yuntongxun.ecdemo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.AppManager;
import com.yuntongxun.ecdemo.common.view.TitleBar;

import java.io.Serializable;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by zlk on 2017/7/24.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected static final String TAG = BaseActivity.class.getSimpleName();
    public Context mContext;
    private Unbinder unbinder;

    protected boolean mDestroyed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(isNoTitle()){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            //设置全屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        super.onCreate(savedInstanceState);
        if (getLayoutId() == -1) {
            return;
        }



        mContext = this;
        setContentView(getLayoutId());
        unbinder = ButterKnife.bind(this);

        initView(savedInstanceState);
        initWidgetAciotns();
        mDestroyed = false;


    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSoftKeyboard();
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager != null ) {
            View localView = getCurrentFocus();
            if(localView != null && localView.getWindowToken() != null ) {
                IBinder windowToken = localView.getWindowToken();
                inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
            }
        }
    }



    public  boolean isNoTitle(){
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        mContext = null;

        if (internalReceiver !=null){
            unregisterReceiver(internalReceiver);
        }

        mDestroyed = true;

    }

    @Override
    public boolean isDestroyed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return mDestroyed || super.isDestroyed();
        } else {
            return mDestroyed;
        }
    }


    public boolean canShowUI() {
        return !(isDestroyed() || isFinishing());
    }
    /**
     * 初始化view 相关
     * @param savedInstanceState
     */
    protected abstract void initView(Bundle savedInstanceState);

    protected abstract int getLayoutId();

    /**
     * view 事件相关
     * @return
     */
    protected abstract void initWidgetAciotns();

    private ECProgressDialog mPostingdialog;
    public void showCommonProcessDialog(String tips){
        mPostingdialog = new ECProgressDialog(this, tips);
        mPostingdialog.show();
    }

    public void showCommonProcessDialog(){
        showCommonProcessDialog("请稍后...");
    }

    /**
     * 关闭对话框
     */
    public void dismissCommonPostingDialog() {
        if (mPostingdialog == null || !mPostingdialog.isShowing()) {
            return;
        }
        mPostingdialog.dismiss();
        mPostingdialog = null;
    }



    public void initTooleBar( TitleBar mTitleBar,boolean showBack, String title) {
        if (mTitleBar == null) {
            return;
        }
        if (showBack) {
            mTitleBar.setNavigationIcon(R.drawable.nav_back);
            mTitleBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppManager.getAppManager().finishActivity(BaseActivity.this);
                }
            });
        } else {
            mTitleBar.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(title)) {
            mTitleBar.setVisibility(View.GONE);
        } else {
            mTitleBar.setVisibility(View.VISIBLE);
            mTitleBar.setMyCenterTitle(title);
        }
    }

    public void initTooleBar(TitleBar mTitleBar,boolean showBack, int titleRes) {
        String string = getResources().getString(titleRes);
        initTooleBar(mTitleBar,showBack, string);
    }


    //======================= 以下封装广播===================
    public InternalReceiver internalReceiver;
    protected final void registerReceiver(String[] actionArray) {
        if (actionArray == null) {
            return;
        }
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(SDKCoreHelper.ACTION_KICK_OFF);
        for (String action : actionArray) {
            intentfilter.addAction(action);
        }
        if (internalReceiver == null) {
            internalReceiver = new InternalReceiver();
        }
        registerReceiver(internalReceiver, intentfilter);
    }

    // Internal calss.
    private class InternalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null || intent.getAction() == null ) {
                return ;
            }
            handleReceiver(context, intent);
        }
    }

    /**
     * 如果子界面需要拦截处理注册的广播
     * 需要实现该方法
     * @param context
     * @param intent
     */
    protected void handleReceiver(Context context, Intent intent) {
        // 广播处理
        if(intent == null ) {
            return ;
        }
    }



    //======================= 以下封装activity跳转=======================================
    public void startActivity(Class<?> cls) {
        startActivity(cls, null, false);
    }


    /**
     * 通过Class跳转界面
     **/
    public void startActivity(Class<?> cls, boolean isCloseSelf) {
        startActivity(cls, null, isCloseSelf);
    }

    /**
     * 含有Bundle通过Class跳转界面
     **/
    public void startActivity(Class<?> cls, Bundle bundle, boolean isCloseSelf) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        if (isCloseSelf) {
            AppManager.getAppManager().finishActivity(this);
        }
    }

    public void startNextActivity(Class<?> cls, String key, Serializable serializable) {
        Intent intent = new Intent(mContext, cls);
        intent.putExtra(key, serializable);
        startActivity(intent);

    }

}
