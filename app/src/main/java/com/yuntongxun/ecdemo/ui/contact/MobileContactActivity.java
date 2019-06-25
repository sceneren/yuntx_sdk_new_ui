package com.yuntongxun.ecdemo.ui.contact;

import android.os.Bundle;
import android.view.View;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.AppManager;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import butterknife.BindView;

/**
 * Created by Jorstin on 2015/3/20.
 */
public class MobileContactActivity extends BaseActivity {


    @BindView(R.id.title_bar)
    TitleBar titleBar;

    @Override
    protected int getLayoutId() {
        return R.layout.mobile_contacts_list;
    }

    @Override
    protected void initWidgetAciotns() {
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        //titlebar
        titleBar.setNavigationIcon(R.drawable.topbar_back_bt);
        titleBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppManager.getAppManager().finishActivity(MobileContactActivity.this);
            }
        });
        titleBar.setMyCenterTitle(getString(R.string.mobile_contact));


//        FragmentManager fm = getSupportFragmentManager();
//        if (savedInstanceState == null) {
//            MobileContactFragment list = new MobileContactFragment();
//            fm.beginTransaction().add(R.id.mobile_content, list).commit();
//        }
    }
}
