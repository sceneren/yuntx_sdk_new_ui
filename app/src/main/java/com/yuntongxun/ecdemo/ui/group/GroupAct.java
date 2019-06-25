
package com.yuntongxun.ecdemo.ui.group;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.GroupListFragment;

import butterknife.BindView;

/**
 * Created by zlk on 2017/7/26.
 */

public class GroupAct extends BaseActivity {
    @BindView(R.id.title_bar)
    TitleBar titleBar;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void initView(Bundle savedInstanceState) {
        initTooleBar(titleBar,true,getResources().getString(R.string.group));
        titleBar.setMySettingIcon(R.drawable.message_navbtn_go_sel).setSettingIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(CreateGroupActivity.class,true);
            }
        });
    }



    @Override
    protected int getLayoutId() {
        return R.layout.act_group;
    }

    @Override
    protected void initWidgetAciotns() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content, GroupListFragment.newInstance()).commit();
    }

}
