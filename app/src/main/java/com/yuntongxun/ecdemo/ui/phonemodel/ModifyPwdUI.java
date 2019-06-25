package com.yuntongxun.ecdemo.ui.phonemodel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import butterknife.BindView;

/**
 * Created by luhuashan on 17/8/14.
 * email huashan2007@sina.cn
 */
public class ModifyPwdUI extends BaseActivity {


    @BindView(R.id.ll_top)
    TitleBar titleBar;

    private FragmentManager fragmentManager;

    @Override
    protected void initView(Bundle savedInstanceState) {

        initTooleBar(titleBar, true, "修改密码");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_modify_pwd;
    }

    @Override
    protected void initWidgetAciotns() {

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager =getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.add(R.id.phone_panel, new ModifyPwdFragment());
        transaction.commit();
    }
}
