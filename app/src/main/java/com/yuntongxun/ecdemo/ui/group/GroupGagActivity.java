package com.yuntongxun.ecdemo.ui.group;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.common.view.WrapListview;
import com.yuntongxun.ecdemo.storage.GroupMemberSqlManager;
import com.yuntongxun.ecdemo.storage.GroupSqlManager;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.adapter.GagMemberAdapter;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupMember;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;

import static com.yuntongxun.ecdemo.ui.group.GroupInfoActivity.GROUP_ID;
import static com.yuntongxun.ecdemo.ui.group.GroupInfoActivity.GROUP_MEMBERS;
import static com.yuntongxun.ecdemo.ui.group.GroupMemberService.forbidMemberSpeakStatus;

/**
 * Created by zlk on 2017/8/19.
 * <p>
 * 群组禁言
 */

public class GroupGagActivity extends BaseGroupReceiveAct implements GroupMemberService.OnSynsGroupMemberListener {
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.lv_aga_member)
    WrapListview lvAgaMember;
    @BindView(R.id.si_all_gag)
    SettingItem siAllGag;
    private GagMemberAdapter adapter;
    private ECGroup mGroup;
    private ArrayList<ECGroupMember> members;
    private ECGroupMember.Role mRole;
    private List<ECGroupMember> banMembers;
    public static final String TYPE = "type";
    public static final int VALUE_GAG_MEMEMBERS = 0x1;//禁言
    public static final int VALUE_DEl_MEMEMBERS = 0x2;//删除
    private View footView;

    @Override
    protected void initView(Bundle savedInstanceState) {
        banMembers = new ArrayList<ECGroupMember>();

        initTooleBar(titleBar, true, "设置群内禁言");

        final String groupId = getIntent().getStringExtra(GROUP_ID);
        members = getIntent().getParcelableArrayListExtra(GroupInfoActivity.GROUP_MEMBERS);
        mGroup = GroupSqlManager.getECGroup(groupId);

//        siAllGag.setVisibility(isOwner() ? View.VISIBLE : View.GONE);
        adapter = new GagMemberAdapter(mContext);
        lvAgaMember.setAdapter(adapter);

        mRole = ECGroupMember.Role.values()[GroupMemberSqlManager.getSelfRoleWithGroupId(groupId, CCPAppManager.getUserId()) - 1];

        footView = LayoutInflater.from(mContext).inflate(R.layout.view_gag_foot, lvAgaMember, false);
        footView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Iterator<ECGroupMember> iterator = members.iterator();
                while (iterator.hasNext()) {
                    ECGroupMember next = iterator.next();
                    if (next == null) {
                        continue;
                    }
                    if (next.getMemberRole().ordinal() <= mRole.ordinal()) {
                        iterator.remove();
                    }
                    if (next.isBan()) {
                        iterator.remove();
                    }
                }
                Intent intent = new Intent(mContext, GroupMemberControlAct.class)
                        .putParcelableArrayListExtra(GROUP_MEMBERS, members)
                        .putExtra(GROUP_ID, groupId)
                        .putExtra(TYPE, VALUE_GAG_MEMEMBERS);
                startActivity(intent);
            }
        });
        lvAgaMember.addFooterView(footView);

        siAllGag.getCheckedTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuiet();
            }
        });

    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        footView.measure(0, 0);
        totalHeight += footView.getMeasuredHeight();
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    /**
     * 全员禁言
     */
    private void allQuiet() {

    }

    /**
     * 是否是群组创建者
     *
     * @return
     */
    private boolean isOwner() {
        return CCPAppManager.getUserId().equals(mGroup.getOwner());
    }


    @Override
    protected int getLayoutId() {
        return R.layout.act_gag;
    }

    @Override
    protected void initWidgetAciotns() {


        siAllGag.getCheckedTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //全员禁言
                doSetMemberSpeakStatus();
            }
        });
    }


    /**
     * 设置群组成员禁言状态
     */
    private void doSetMemberSpeakStatus() {
        String msg = getString(R.string.str_group_all_member_speak_tips);
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, msg,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (members.size() == 0) {
                            return;
                        }
                        if (siAllGag == null) {
                            return;
                        }
                        siAllGag.toggle();

                        showProcessDialog(getString(R.string.login_posting_submit));
                        for (ECGroupMember item : members) {
                            forbidMemberSpeakStatus(mGroup.getGroupId(), item.getVoipAccount(),
                                    !item.isBan());
                        }
                    }
                });

        buildAlert.setTitle(R.string.app_tip);
        buildAlert.show();
    }

    private ECProgressDialog mPostingdialog;

    void showProcessDialog(String tips) {
        mPostingdialog = new ECProgressDialog(GroupGagActivity.this,
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

        banMembers.clear();
        boolean hasSelf = false;
        for (ECGroupMember member : members) {

            if (member.isBan()) {
                banMembers.add(member);
            }
        }

        adapter.setData(banMembers, mGroup);
//        setListViewHeightBasedOnChildren(lvAgaMember);
    }
}
