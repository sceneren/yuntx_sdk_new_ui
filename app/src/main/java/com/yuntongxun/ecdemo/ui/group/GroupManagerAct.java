package com.yuntongxun.ecdemo.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.platform.comapi.radar.Event;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.common.view.WrapListview;
import com.yuntongxun.ecdemo.storage.GroupMemberSqlManager;
import com.yuntongxun.ecdemo.storage.GroupSqlManager;
import com.yuntongxun.ecdemo.ui.adapter.ManagerMemberAdapter;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupMember;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;

import static com.yuntongxun.ecdemo.storage.IMessageSqlManager.ACTION_GROUP_CHANGED;
import static com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper.INTENT_ACTION_ADD_GROUP_MEMBER;
import static com.yuntongxun.ecdemo.ui.group.GroupInfoActivity.GROUP_ID;
import static com.yuntongxun.ecdemo.ui.group.GroupInfoActivity.GROUP_MEMBERS;

/**
 * Created by zlk on 2017/8/19.
 * <p>
 * 群组管理员设置 ，转让群
 */

public class GroupManagerAct extends BaseGroupReceiveAct implements GroupMemberService.OnSynsGroupMemberListener {
    private static final int RQ_SET_MANAGER = 0x6;
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.tv_manager_count)
    TextView tvanagerMCount;

    @BindView(R.id.lv_manager_member)
    WrapListview lvMember;


    private ManagerMemberAdapter adapter;
    private ECGroup mGroup;
    private ArrayList<ECGroupMember> members;
    private ECGroupMember.Role mRole;
    private List<ECGroupMember> managerMembers;
    public static final String TYPE = "type";
    public static final int VALUE_SET_MANAGER = 0x3;//设置管理员
    private View footView;

    @Override
    protected void initView(Bundle savedInstanceState) {
        managerMembers = new ArrayList<ECGroupMember>();

        initTooleBar(titleBar, true, "设置管理员");

        final String groupId = getIntent().getStringExtra(GROUP_ID);
        mGroup = GroupSqlManager.getECGroup(groupId);

        adapter = new ManagerMemberAdapter(mContext);
        lvMember.setAdapter(adapter);


        footView = LayoutInflater.from(mContext).inflate(R.layout.view_gag_foot, lvMember, false);
        ((TextView) footView.findViewById(R.id.tv_note)).setText("添加管理员");
        footView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Iterator<ECGroupMember> iterator = members.iterator();

                while (iterator.hasNext()) {
                    ECGroupMember next = iterator.next();
                    if (next == null) {
                        continue;
                    }
                    if (isOwnerOrManager(next)) {
                        iterator.remove();
                    }
                }

                Intent intent = new Intent(mContext, GroupMemberControlAct.class)
                        .putParcelableArrayListExtra(GROUP_MEMBERS, members)
                        .putExtra(GROUP_ID, groupId)
                        .putExtra(TYPE, VALUE_SET_MANAGER);
                startActivityForResult(intent, RQ_SET_MANAGER);
            }
        });
        lvMember.addFooterView(footView);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQ_SET_MANAGER || resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            String result = data.getStringExtra("result");
            if (TextUtils.equals(result, "success")) {
                ToastUtil.showMessage("设置成功");
//                Intent intent = new Intent(ACTION_GROUP_CHANGED);
//                sendBroadcast(intent);
            } else {
                ToastUtil.showMessage("设置失败");
            }
        }
    }

    /**
     * 是否是群组创建者
     *
     * @param next
     * @return
     */
    private boolean isOwner(ECGroupMember next) {
        return next.getMemberRole() == ECGroupMember.Role.OWNER;
    }


    /**
     * 是否是群组创建者
     *
     * @return
     */
    private boolean isOwnerOrManager(ECGroupMember next) {
        return next.getMemberRole() == ECGroupMember.Role.OWNER
                || next.getMemberRole() == ECGroupMember.Role.MANAGER;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.act_group_manager;
    }

    @Override
    protected void initWidgetAciotns() {

    }


    private ECProgressDialog mPostingdialog;

    void showProcessDialog(String tips) {
        mPostingdialog = new ECProgressDialog(GroupManagerAct.this,
                R.string.login_posting_submit);
        mPostingdialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        GroupMemberService.synsGroupMember(mGroup.getGroupId());

        GroupMemberService.addListener(this);
    }

    /**
     * 关闭对话框
     */
    private void dismissPostingDialog() {
        if (mPostingdialog == null || !mPostingdialog.isShowing()) {
            return;
        }
        mPostingdialog.dismiss();
        mPostingdialog = null;
    }

    @Override
    public void onSynsGroupMember(String groupId) {
        dismissPostingDialog();

        if (mGroup == null || !mGroup.getGroupId().equals(groupId)) {
            return;
        }

        members = GroupMemberSqlManager.getGroupMemberWithName(mGroup.getGroupId());

        managerMembers.clear();

        for (ECGroupMember member : members) {
            if (member == null) {
                continue;
            }
            if (isManager(member)) {
                managerMembers.add(member);
            }
        }

        adapter.setData(managerMembers, mGroup);
        tvanagerMCount.setText("管理员(" + managerMembers.size() + "/15)");

    }

    /**
     * 判断是否是管理员
     *
     * @param member
     * @return
     */
    private boolean isManager(ECGroupMember member) {
        return member.getMemberRole() == ECGroupMember.Role.MANAGER;
    }


}
