package com.yuntongxun.ecdemo.ui.group;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.DiscussionListFragment;
import com.yuntongxun.ecdemo.ui.contact.MobileContactSelectActivity;

import butterknife.BindView;

/**
 * Created by zlk on 2017/8/3.
 */


public class DiscussionAct extends BaseActivity {


    @BindView(R.id.title_bar)
    TitleBar titleBar;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void initView(Bundle savedInstanceState) {

        initTooleBar(titleBar, true, getResources().getString(R.string.discussion));
        titleBar.setMySettingIcon(R.drawable.message_navbtn_go_sel).setSettingIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //讨论组
                Intent intent = new Intent(mContext, MobileContactSelectActivity.class);
                intent.putExtra("is_discussion", true);
                intent.putExtra("isFromCreateDiscussion", true);
                intent.putExtra("group_select_need_result", true);
                startActivity(intent);
            }
        });

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, DiscussionListFragment.newInstance()).commit();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.act_diacuss;
    }

    @Override
    protected void initWidgetAciotns() {

    }

}
