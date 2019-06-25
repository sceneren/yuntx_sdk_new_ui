package com.yuntongxun.ecdemo.ui.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.yuntongxun.ecdemo.storage.IMessageSqlManager;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import static com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper.ACTION_TRANS_OWNER;
import static com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper.INTENT_ACTION_CHANGE_ADMIN;

/**
 * Created by smileklvens on 2017/8/30.
 */

public abstract class BaseGroupReceiveAct extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(new String[]{IMessageSqlManager.ACTION_GROUP_DEL, IMessageSqlManager.ACTION_GROUP_CHANGED
                , INTENT_ACTION_CHANGE_ADMIN,ACTION_TRANS_OWNER});
    }


    @Override
    protected void handleReceiver(Context context, Intent intent) {
        super.handleReceiver(context, intent);
        //解散群组
        if (IMessageSqlManager.ACTION_GROUP_DEL.equals(intent.getAction()) && intent.hasExtra("group_id")) {
            String id = intent.getStringExtra("group_id");
            if (id != null) {
                finish();
            }
        }
//        //成员新增
//        if (IMChattingHelper.INTENT_ACTION_ADD_GROUP_MEMBER.equals(intent.getAction()) && intent.hasExtra("addmember")) {
//            ECGroupMember member = intent.getParcelableExtra("addmember");
//            members.add(member);
//            mAdapter.setData(members);
//            mAdapter.notifyDataSetChanged();
//        }




    }
}
