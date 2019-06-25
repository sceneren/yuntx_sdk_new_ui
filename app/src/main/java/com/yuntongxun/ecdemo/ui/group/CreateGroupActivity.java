
/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yuntongxun.ecdemo.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.ECCircumscription;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.KeyBordUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.storage.GroupSqlManager;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.contact.MobileContactSelectActivity;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroup;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 群组创建
 * 添加群成员监听GroupMemberService.addListener(this);
 * 1. 创建 ecGroupManager.createGroup(group, this);
 * 2. onSynsGroupMember(groupId)。
 *
 * @date 2014-12-27
 */
public class CreateGroupActivity extends BaseActivity implements ECGroupManager.OnCreateGroupListener
        , GroupMemberService.OnSynsGroupMemberListener {


    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.ed_group_name)
    EditText edGroupName;
    @BindView(R.id.ed_province)
    EditText edProvince;
    @BindView(R.id.ed_city)
    EditText edCity;
    @BindView(R.id.tv_notice_select)
    TextView tvNoticeSelect;
    @BindView(R.id.ll_group_notice)
    LinearLayout llGroupNotice;
    @BindView(R.id.tv_notice)
    TextView tvNotice;
    @BindView(R.id.tv_group_type)
    TextView tvGroupType;
    @BindView(R.id.ll_group_type)
    LinearLayout llGroupType;
    @BindView(R.id.si_public)
    SettingItem siPublic;

    /**
     * 创建的群组
     */
    private ECGroup group;

    private static final int REQCODE_NOCITE = 0x1;
    private static final int REQCODE_GROUP_TYPE = 0x2;

    private int mPermissionModel = 1;
    private int mGroupTypePosition;
    private ECProgressDialog mPostingdialog;

    private boolean isDiscussion = false;

    @Override
    protected void initView(Bundle savedInstanceState) {
        initTooleBar(titleBar, true, "创建群");

        siPublic.setChecked(true);

        titleBar.setMySettingText("下一步").setSettingTextOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyBordUtil.hideSoftKeyboard(CreateGroupActivity.this);

                ECGroupManager ecGroupManager = SDKCoreHelper.getECGroupManager();
                if (!checkNameEmpty() || ecGroupManager == null) {
                    ToastUtil.showMessage("请先填写必填信息再试");
                    return;
                }
                String provinceStr = edProvince.getText().toString();
                if (!TextUtils.isEmpty(provinceStr)) {
                    boolean b = !DemoUtils.isValidNormalAccount(provinceStr);
                    if (b) {
                        ToastUtil.showMessage("请输入正确的省份信息");
                        return;
                    }
                }
                String cityStr = edCity.getText().toString();
                if (!TextUtils.isEmpty(cityStr)) {
                    boolean b = !DemoUtils.isValidNormalAccount(cityStr);
                    if (b) {
                        ToastUtil.showMessage("请输入正确的城市信息");
                        return;
                    }
                }


                ECGroup group = getGroup();

                if (group.getName().length() > ECCircumscription.GROUP_CARD) {
                    ToastUtil.showMessage("群组名称字数超过限制");
                    return;
                }
                if (!DemoUtils.isGroupNameDescValid(group.getName())) {
                    ToastUtil.showMessage("群组名称不合法，非空且仅能输入中英文、ASCII码范围内的值");
                    return;
                }

                mPostingdialog = new ECProgressDialog(mContext,
                        isDiscussion ? R.string.create_dis_posting
                                : R.string.create_group_posting);
                mPostingdialog.show();

                ecGroupManager.createGroup(group, CreateGroupActivity.this);
            }
        });

    }

    /**
     * @return
     */
    private boolean checkNameEmpty() {
        return edGroupName != null
                && edGroupName.getText().toString().trim().length() > 0;
    }

    /**
     * 创建群组参数
     *
     * @return
     */
    private ECGroup getGroup() {
        ECGroup group = new ECGroup();
        // 设置群组名称
        group.setName(edGroupName.getText().toString().trim());
        // 设置群组公告
        group.setDeclare(tvNotice.getText().toString().trim());
        // 临时群组（100人）
        group.setScope(ECGroup.Scope.TEMP);

        if (siPublic.getCheckedTextView().isChecked()) {
            mPermissionModel = 1;
        } else {
            mPermissionModel = 2;
        }

        // 群组验证权限，需要身份验证
        group.setPermission(ECGroup.Permission.values()[mPermissionModel + 1]);
        // 设置群组创建者
        group.setOwner(CCPAppManager.getClientUser().getUserId());

        group.setProvince(edProvince.getText().toString().trim());
        group.setCity(edCity.getText().toString().trim());


        group.setGroupType(mGroupTypePosition);
        group.setIsDiscuss(false);
        return group;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.new_group;
    }

    @Override
    protected void initWidgetAciotns() {
        siPublic.getCheckedTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siPublic.toggle();
            }
        });
    }


    @OnClick({R.id.tv_notice_select, R.id.tv_group_type, R.id.tv_notice})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_notice_select:
            case R.id.tv_notice:
                startActivityForResult(new Intent(mContext, GroupNoticeAct.class), REQCODE_NOCITE);
                break;
            case R.id.tv_group_type:
                startActivityForResult(new Intent(mContext, GroupSelectTypeAct.class), REQCODE_GROUP_TYPE);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x2a) { //邀请成员回调
            if (data == null) {
                finish();
                return;
            } else {
                String[] selectUser = data.getStringArrayExtra("Select_Conv_User");
                if (selectUser != null && selectUser.length > 0) {
                    mPostingdialog = new ECProgressDialog(this,
                            R.string.invite_join_group_posting);
                    mPostingdialog.show();
                    GroupMemberService.inviteMembers(group.getGroupId(), "",
                            ECGroupManager.InvitationMode.FORCE_PULL, selectUser);
                }
            }
        } else if (requestCode == REQCODE_NOCITE) {// 群公告
            if (data == null | resultCode != RESULT_OK) {
                return;
            }
            String noticeStr = data.getStringExtra(GroupNoticeAct.KEY_NOTICE);
            tvNoticeSelect.setVisibility(View.GONE);
            tvNotice.setVisibility(View.VISIBLE);
            tvNotice.setText(noticeStr);
        } else if (requestCode == REQCODE_GROUP_TYPE) {// 群类型
            if (data == null | resultCode != RESULT_OK) {
                return;
            }
            String type = data.getStringExtra(GroupSelectTypeAct.KEY_GROUP_TYPE);
            mGroupTypePosition = data.getIntExtra(GroupSelectTypeAct.KEY_GROUP_POSITION, 0);
            tvGroupType.setText(type);
        }
    }

    //创建群组完成回调
    @Override
    public void onCreateGroupComplete(ECError error, ECGroup group) {
        if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
            group.setIsNotice(true);
            // 1.创建的群组实例化到数据库
            GroupSqlManager.insertGroup(group, true, false, isDiscussion);
            this.group = group;
            // 2.邀请人页面跳转逻辑
            Intent intent = new Intent(this, MobileContactSelectActivity.class);
            intent.putExtra("group_select_need_result", true);
            intent.putExtra("isFromCreateDiscussion", false);
            startActivityForResult(intent, 0x2a);
        } else {
            if (isDiscussion) {
                ToastUtil.showMessage("创建讨论组失败[" + "您输入的群组名称包含不符合规范的特殊字符" + "]");
            } else {
                ToastUtil.showMessage("创建群组失败[" + "您输入的群组名称包含不符合规范的特殊字符" + "]");
            }
        }
        dismissPostingDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    //同步群组
    @Override
    public void onSynsGroupMember(String groupId) {
        dismissPostingDialog();
        CCPAppManager.startChattingAction(CreateGroupActivity.this, groupId,
                group.getName());
        finish();
    }
}
