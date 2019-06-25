package com.yuntongxun.ecdemo.ui.phonemodel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.PermissionUtils;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by luhuashan on 17/8/11.
 * email huashan2007@sina.cn
 */
public class PhoneUI extends BaseActivity {


    @BindView(R.id.bu_login)
    public TextView tvLogin;

    @BindView(R.id.bu_register)
    public TextView tvRegister;

    @BindView(R.id.ll_bu)
    public LinearLayout ll;
    @BindView(R.id.phone_top)
    ImageView phoneTop;


    private LoginFragment loginFragment;

    private RegisterFragment registerFragment;

    private FragmentManager fragmentManager;


    @Override
    protected void initView(Bundle savedInstanceState) {

        PermissionUtils.requestMultiPermissions(this, mPermissionGrant);


    }

    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.CODE_MULTI_PERMISSION:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ButterKnife.bind(this);

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        loginFragment = LoginFragment.newInstance();
        transaction.add(R.id.phone_panel, loginFragment);
        transaction.commit();

        registerFragment = RegisterFragment.newInstance();

    }


    @Override
    protected int getLayoutId() {
        return R.layout.phone_ui;
    }

    @Override
    protected void initWidgetAciotns() {

    }

    @Override
    public boolean isNoTitle() {
        return false;
    }


    @OnClick({R.id.bu_login, R.id.bu_register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bu_login:
                ll.setBackgroundResource(R.drawable.smallleft);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.show(loginFragment);
                transaction.commit();
                break;
            case R.id.bu_register:
                ll.setBackgroundResource(R.drawable.small);
                FragmentTransaction transaction2 = fragmentManager.beginTransaction();

                if (!registerFragment.isAdded()){
                    transaction2.add(R.id.phone_panel, registerFragment);
                }
                transaction2.hide(loginFragment);
                transaction2.show(registerFragment);
                transaction2.commit();
                break;
        }
    }
}
