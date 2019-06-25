package com.yuntongxun.ecdemo.ui.group;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.KeyBordUtil;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.ContactListFragment;
import com.yuntongxun.ecdemo.ui.GagMembesFragment;
import com.yuntongxun.ecsdk.im.ECGroupMember;

import java.util.ArrayList;

import static com.yuntongxun.ecdemo.ui.group.GroupInfoActivity.GROUP_MEMBERS;

/**
 * Created by smileklvens on 2017/9/6.
 * 群成员展示
 */

public class GroupMembersAct extends BaseActivity implements
        ContactListFragment.OnContactClickListener{

    private GagMembesFragment gagMembesFragment;
    private ArrayList<ECGroupMember> members;
    private boolean isLocalDiscussion;

    @Override
    protected void initView(Bundle savedInstanceState) {
        members = getIntent().getParcelableArrayListExtra(GROUP_MEMBERS);
        isLocalDiscussion = getIntent().getBooleanExtra("isLocalDiscussion", false);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.contact_container) == null) {
            //群成员
            gagMembesFragment = GagMembesFragment.newInstance( GagMembesFragment.TYPE_GROUP_MEMBERS);
            fm.beginTransaction().add(R.id.contact_container, gagMembesFragment).commit();
            gagMembesFragment.setData(members,isLocalDiscussion);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.act_group_members;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLocalDiscussion){
            initTooleBar(gagMembesFragment.getTitleBar(), true, "讨论组成员");
        }else{
            initTooleBar(gagMembesFragment.getTitleBar(), true, "群成员");
        }
        gagMembesFragment.getTitleBar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });


    }

    @Override
    protected void initWidgetAciotns() {

    }


    private void goBack() {
        KeyBordUtil.hideSoftKeyboard(GroupMembersAct.this);
        setResult(RESULT_CANCELED);
        finish();
    }


    @Override
    public void onContactClick(int count) {

    }

    @Override
    public void onSelectGroupClick() {

    }
}
