package com.yuntongxun.ecdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.AppManager;
import com.yuntongxun.ecdemo.ui.account.LoginActivity;
import com.yuntongxun.ecdemo.ui.voip.VideoActivity;
import com.yuntongxun.ecdemo.ui.voip.VoIPCallActivity;

import butterknife.BindView;

/**
 * Created by zlk on 2017/7/24.
 */

public class SplashAct extends BaseActivity {
    private static final int sleepTime = 2000;
    @BindView(R.id.splash_root)
    FrameLayout splashRoot;

    @Override
    protected void initView(Bundle savedInstanceState) {
        //1.解决安装后直接打开，home键切换到后台再启动重复出现闪屏页面的问题
        // http://stackoverflow.com/questions/2280361/app-always-starts-fresh-from-root-activity-instead-of-resuming-background-state
        if (!isTaskRoot()) {
            if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
                finish();
                return;
            }
        }
        getWindow().setBackgroundDrawable(null);
        //2.执行 AlphaAnimation
        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(1500);
        splashRoot.startAnimation(animation);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.act_splash;
    }


    @Override
    protected void initWidgetAciotns() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // auto login loaed conversation
                if (true) {
                    long start = System.currentTimeMillis();
//                        EMClient.getInstance().chatManager().loadAllConversations();
//                        EMClient.getInstance().groupManager().loadAllGroups();
                    long costTime = System.currentTimeMillis() - start;
                    //wait
                    if (sleepTime - costTime > 0) {
                        try {
                            Thread.sleep(sleepTime - costTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    String topActivityName = AppManager.getAppManager().getTopActivityName(mContext);
                    if (topActivityName != null && (topActivityName.equals(VideoActivity.class.getName()) || topActivityName.equals(VoIPCallActivity.class.getName()))) {
                        // nop
                        // avoid main screen overlap Calling Activity
                    } else {
                        startActivity(LoginActivity.class);
                    }
                    finish();
                } else {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                    startActivity(LoginActivity.class, true);
                }
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
