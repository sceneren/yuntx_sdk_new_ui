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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.encode.QRCodeEncoder;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.dialog.ECListDialog;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.Base64;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.InfoItem;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.common.view.WrapGridView;
import com.yuntongxun.ecdemo.exception.ECEncoderQrException;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.storage.ConversationSqlManager;
import com.yuntongxun.ecdemo.storage.GroupMemberSqlManager;
import com.yuntongxun.ecdemo.storage.GroupSqlManager;
import com.yuntongxun.ecdemo.storage.IMessageSqlManager;
import com.yuntongxun.ecdemo.ui.adapter.GroupInfoAdapter;
import com.yuntongxun.ecdemo.ui.chatting.ChattingActivity;
import com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper;
import com.yuntongxun.ecdemo.ui.contact.ContactDetailActivity;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.contact.MobileContactSelectActivity;
import com.yuntongxun.ecdemo.ui.mvp.group.GroupQRUI;
import com.yuntongxun.ecdemo.ui.personcenter.FriendInfoUI;
import com.yuntongxun.ecdemo.ui.settings.EditConfigureActivity;
import com.yuntongxun.ecdemo.ui.settings.SettingsActivity;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupMember;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.OnClick;

import static com.yuntongxun.ecdemo.storage.IMessageSqlManager.ACTION_GROUP_CHANGED;
import static com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper.ACTION_TRANS_OWNER;
import static com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper.INTENT_ACTION_CHANGE_ADMIN;
import static com.yuntongxun.ecdemo.ui.group.GroupGagActivity.VALUE_DEl_MEMEMBERS;
import static com.yuntongxun.ecdemo.ui.group.GroupMemberControlAct.VALUE_TRANS_OWNER;


/**
 * 群，讨论组设置界面
 * <p>
 * 更新群组信息{@link com.yuntongxun.ecsdk.ECGroupManager#modifyGroup}回调onSyncGroupInfo
 * <p>
 * 邀请成员{@link com.yuntongxun.ecsdk.ECGroupManager#inviteJoinGroup}
 * 删除成员{@link com.yuntongxun.ecsdk.ECGroupManager#deleteGroupMember}后回调onSynsGroupMember
 * 注意server同步成功后，更新本地数据库,并回调更新UI。
 */
public class GroupInfoActivity extends BaseGroupReceiveAct implements GroupMemberService.OnSynsGroupMemberListener
        , GroupService.Callback {

    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.info_count)
    InfoItem infoCount;
    @BindView(R.id.gv_member)
    WrapGridView gvMember;
    @BindView(R.id.name)
    InfoItem name;
    @BindView(R.id.qr)
    InfoItem qr;
    @BindView(R.id.notice)
    InfoItem notice;
    @BindView(R.id.card)
    InfoItem card;
    @BindView(R.id.gag)
    InfoItem gag;
    @BindView(R.id.set_manager)
    InfoItem setManager;
    @BindView(R.id.info_msg_notify)
    SettingItem infoMsgNotify;
    @BindView(R.id.info_msg_push)
    SettingItem infoMsgPush;
    @BindView(R.id.info_dissolve)
    SettingItem infoDissolve;
    @BindView(R.id.clear_msg)
    SettingItem clearMsg;
    @BindView(R.id.btn_group_quit)
    Button btnGroupQuit;
    @BindView(R.id.info_content)
    LinearLayout infoContent;

    @BindView(R.id.info_trans_owner)
    SettingItem infoTransOwner;


    public static final String TYPE = "type";
    private static final int RQ_TRANS_OWNER = 0x6;

    private static final String TAG = "ECDemo.GroupInfoActivity";
    public final static String GROUP_ID = "group_id";
    public final static String GROUP_MEMBERS = "group_members";
    public static final String EXTRA_RELOAD = "com.yuntongxun.ecdemo_reload";
    public static final String EXTRA_QUEIT = "com.yuntongxun.ecdemo_quit";

    private ECGroup mGroup;
    private String groupId;
    private boolean isLocalDiscussion;//是否是讨论组
    private ECProgressDialog mPostingdialog;
    private ECGroupMember.Role mRole = ECGroupMember.Role.MEMBER;//自己角色权限
    private ArrayList<ECGroupMember> members;// 全局的
    private int mEditMode = -1;
    boolean isCreat;//activity是否存在

    private boolean mClearChatmsg = false;
    /**
     * 群组成员适配器
     */
    private GroupInfoAdapter mAdapter;


    public static boolean isFromDiscussionInviteClick = false;
    private int memCount;


    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ECGroupMember item = mAdapter.getItem(position);
            if (item == null) {
                return;
            }

            //新增联系人
            if ("add@yuntongxun.com".equals(item.getVoipAccount())) {
                Intent intent = new Intent(GroupInfoActivity.this, MobileContactSelectActivity.class);
                intent.putExtra("group_select_need_result", true);
                intent.putExtra("select_type", false);
                startActivityForResult(intent, 0x2a);
                return;
            }


            // 删除联系人
            if ("del@yuntongxun.com".equals(item.getVoipAccount())) {
                delOrgagintent(VALUE_DEl_MEMEMBERS);
                return;
            }

            // GroupMemberService.queryGroupMemberCard(mGroup.getGroupId() ,
            // item.getVoipAccount());
            // GroupMemberService.modifyGroupMemberCard(mGroup.getGroupId(),
            // item.getVoipAccount());
            ECContacts contact = ContactSqlManager.getContact(item.getVoipAccount());
            if (contact == null || contact.getId() == -1)

            {
                ToastUtil.showMessage(R.string.contact_none);
                return;
            }


            Intent intent = new Intent(GroupInfoActivity.this, FriendInfoUI.class);
            intent.putExtra(ContactDetailActivity.RAW_ID, contact.getId());
            intent.putExtra(FriendInfoUI.MOBILE, contact.getContactid() + "");
            intent.putExtra(ContactDetailActivity.DISPLAY_NAME, contact.getNickname());
            startActivity(intent);
        }
    };

    private void delOrgagintent(int type) {

        Iterator<ECGroupMember> iterator = members.iterator();
        ECGroupMember loginMember = null;


        while (iterator.hasNext()) {
            ECGroupMember next = iterator.next();
            if (next == null) {
                continue;
            }
//            if (CCPAppManager.getUserId().equals(next.getVoipAccount())) {
//                loginMember = next;
//            }
//            if (loginMember != null) {
//                if (next.getMemberRole().ordinal() <= loginMember.getMemberRole().ordinal()) {
//                    iterator.remove();
//                }
//            }
            if(mRole.ordinal()>=next.getMemberRole().ordinal()){
                iterator.remove();
            }


        }

        Intent intent = new Intent(mContext, GroupMemberControlAct.class)
                .putParcelableArrayListExtra(GROUP_MEMBERS, members)
                .putExtra(GROUP_ID, groupId)
                .putExtra(GroupGagActivity.TYPE, type);
        startActivity(intent);
    }

    private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return doShowMoreMenu(position);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCreat = false;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        isCreat = true;

        registerReceiver(new String[]{IMessageSqlManager.ACTION_GROUP_DEL, IMChattingHelper.INTENT_ACTION_ADD_GROUP_MEMBER});


        groupId = getIntent().getStringExtra(GROUP_ID);
        mGroup = GroupSqlManager.getECGroup(groupId);
        isLocalDiscussion = GroupSqlManager.isDiscussionGroup(mGroup.getGroupId());

        mRole = ECGroupMember.Role.values()[GroupMemberSqlManager.getSelfRoleWithGroupId(groupId, CCPAppManager.getUserId()) - 1];

        refreshGroupInfo();


        GroupService.syncGroupInfo(mGroup.getGroupId());
        GroupMemberService.synsGroupMember(mGroup.getGroupId());


        isFromDiscussionInviteClick = false;

        if (isLocalDiscussion) {//讨论组
            initTooleBar(titleBar, true, "讨论组设置");
            titleBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goBack();
                }
            });
            infoCount.setLeftTitle(R.string.discussion_count);

            name.setLeftTitle(getString(R.string.dis_name));
            qr.setLeftTitle(R.string.dis_qr);

            try {
                String s = buildString();
                qr.setRightImageBitmap(getQrCode(s));
            } catch (ECEncoderQrException e) {
                e.printStackTrace();
            }

            notice.setLeftTitle(getString(R.string.dis_notice));
            card.setLeftTitle(R.string.dis_card);

            infoDissolve.setVisibility(View.GONE);
            gag.setVisibility(View.GONE);
            setManager.setVisibility(View.GONE);
            btnGroupQuit.setText(R.string.quit_discussion);
        } else {//群组
            initTooleBar(titleBar, true, "群设置");
            titleBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goBack();
                }
            });


            infoCount.setLeftTitle(R.string.group_count);

            name.setLeftTitle(getString(R.string.group_title_name));
            name.setRightContent(mGroup.getName());
            qr.setLeftTitle(R.string.str_group_notice_erweima);
            try {
                String s = buildString();
                qr.setRightImageBitmap(getQrCode(s));
            } catch (ECEncoderQrException e) {
                e.printStackTrace();
            }
            notice.setLeftTitle(R.string.str_group_notice_tips);
            notice.setRightContent(mGroup.getDeclare());
            card.setLeftTitle(R.string.str_group_notice_card);

            infoDissolve.setVisibility(View.VISIBLE);

            btnGroupQuit.setText(R.string.str_group_quit);

            setManager.setVisibility(isCreat() ? View.VISIBLE : View.GONE);

        }

        mAdapter = new GroupInfoAdapter(this, isLocalDiscussion, mGroup);
        gvMember.setAdapter(mAdapter);
        gvMember.setOnItemClickListener(mItemClickListener);

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
        //成员新增
        if (IMChattingHelper.INTENT_ACTION_ADD_GROUP_MEMBER.equals(intent.getAction()) && intent.hasExtra("addmember")) {
            ECGroupMember member = intent.getParcelableExtra("addmember");
            members.add(member);
            mAdapter.setData(members);
        }

        //管理员变化, 转让群主等回调
        if (ACTION_GROUP_CHANGED.equals(intent.getAction())
                || INTENT_ACTION_CHANGE_ADMIN.equals(intent.getAction())
                || ACTION_TRANS_OWNER.equals(intent.getAction())) {
            //群变化重新请求
            GroupService.addListener(this);
            GroupMemberService.addListener(this);

            GroupService.syncGroupInfo(mGroup.getGroupId());
            GroupMemberService.synsGroupMember(mGroup.getGroupId());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        GroupService.addListener(this);
        GroupMemberService.addListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public String buildString() throws ECEncoderQrException {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("groupid", mGroup.getGroupId());
            jsonObject.put("creator", mGroup.getOwner());
            jsonObject.put("name", mGroup.getName());
            jsonObject.put("time", DateUtil.sFormatNowDate(new Date()));
            jsonObject.put("count", mGroup.getCount());

            String data = Base64.encode(jsonObject.toString().getBytes());
            JSONObject obj = new JSONObject();
            obj.put("url", "joinGroup");
            obj.put("data", data);
            return obj.toString();
        } catch (JSONException e) {
            throw new ECEncoderQrException(e);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult: requestCode=" + requestCode
                + ", resultCode=" + resultCode + ", data=" + data);

        // If there's no data (because the user didn't select a picture and
        // just hit BACK, for example), there's nothing to do.
        if (requestCode == 0x2a || requestCode == 0xa || requestCode == RQ_TRANS_OWNER) {
            if (data == null) {
                return;
            }
        } else if (resultCode != RESULT_OK) {
            LogUtil.d("onActivityResult: bail due to resultCode=" + resultCode);
            return;
        }
        if (requestCode == 0x2a) {
            String[] selectUser = data.getStringArrayExtra("Select_Conv_User");
            if (selectUser != null && selectUser.length > 0) {
                mPostingdialog = new ECProgressDialog(this,
                        R.string.invite_join_group_posting);
                mPostingdialog.show();
                String reason = getString(R.string.group_invite_reason,
                        CCPAppManager.getClientUser().getUserName(),
                        mGroup.getName());

                if (GroupSqlManager.isDiscussionGroup(groupId)) {
                    isFromDiscussionInviteClick = true;
                    GroupMemberService.inviteMembers(mGroup.getGroupId(),
                            reason, ECGroupManager.InvitationMode.FORCE_PULL,
                            selectUser);
                } else {
                    GroupMemberService.inviteMembers(mGroup.getGroupId(),
                            reason, ECGroupManager.InvitationMode.NEED_CONFIRM,
                            selectUser);
                }
            }
        } else if (requestCode == 0xa) {//修改群组信息回调
            String result_data = data.getStringExtra("result_data");
            if (mGroup == null) {
                return;
            }
            if (TextUtils.isEmpty(result_data)) {
                ToastUtil.showMessage("不允许为空");
                return;
            }
            if (!DemoUtils.isGroupNameDescValid(result_data)) {
                ToastUtil.showMessage("群组名称不合法，非空且仅能输入中英文、ASCII码范围内的值");
                return;
            }

            if (mEditMode == SettingsActivity.CONFIG_TYPE_GROUP_NAME) {
                mGroup.setName(result_data);
            } else {
                mGroup.setDeclare(result_data);
            }
            doModifyGroup();
        } else if (requestCode == RQ_TRANS_OWNER) {
            String result = data.getStringExtra("result");
            if (TextUtils.equals(result, "success")) {
                ToastUtil.showMessage("设置成功");//成员
                gag.setVisibility(View.GONE);
                infoTransOwner.setVisibility(View.GONE);
                infoDissolve.setVisibility(View.GONE);
            } else {
                ToastUtil.showMessage("设置失败");
            }
        }

    }

    private void doModifyGroup() {
        // 修改群组信息请求
        showProcessDialog(getString(R.string.login_posting_submit));
        GroupService.modifyGroup(mGroup);
        LogUtil.e(mGroup.getPermission().name());
    }

    private Bitmap getQrCode(String content) {
        try {
            return QRCodeEncoder.encodeAsBitmap(content, 400);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.group_info_activity;
    }

    @Override
    protected void initWidgetAciotns() {


        boolean b = ConversationSqlManager.queryIsNoticeBySessionId(mGroup.getGroupId());
        if(b){
            infoMsgNotify.getCheckedTextView().setChecked(true);
        }else {
            infoMsgNotify.getCheckedTextView().setChecked(false);
        }



        //群组免打扰
        infoMsgNotify.getCheckedTextView().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateGroupNewMsgNotify();
                    }
                });

        //消息推送
        infoMsgPush.getCheckedTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGroupNewMsgNotify();
            }
        });
    }

    /**
     * 处理消息免打扰
     */
    private void updateGroupNewMsgNotify() {//
        if (mGroup == null || mGroup.getGroupId() == null) {
            return;
        }
        try {
            if (infoMsgNotify == null) {
                return;
            }
            final boolean checked = infoMsgNotify.isChecked();
//            showCommonProcessDialog();

            ECDevice.setMuteNotification(mGroup.getGroupId(), !checked, new ECDevice.OnSetDisturbListener() {
                @Override
                public void onResult(ECError ecError) {
//                    dismissCommonPostingDialog();
                    if(ecError.errorCode== SdkErrorCode.REQUEST_SUCCESS){
                        ToastUtil.showMessage("设置成功");
                        ConversationSqlManager.updateSessionIdNotify(checked ? "1" : "2", mGroup.getGroupId());
                        infoMsgNotify.toggle();
                    } else {
                        ToastUtil.showMessage("设置失败");
                    }
                }
            });



//
//            showProcessDialog(getString(R.string.login_posting_submit));
//            ECGroupOption option = new ECGroupOption();
//            option.setGroupId(mGroup.getGroupId());
//            option.setRule(checked ? ECGroupOption.Rule.SILENCE
//                    : ECGroupOption.Rule.NORMAL);
//            GroupService.setGroupMessageOption(option);
//            infoMsgNotify.toggle();
//            LogUtil.d(TAG, "updateGroupNewMsgNotify: " + checked);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * E
     */
    private void refreshGroupInfo() {


        if (mGroup == null) {
            return;
        }
        setMemberLongCLick();
        notice.setRightContent(mGroup.getDeclare());

        if (mGroup.getName() != null && mGroup.getName().endsWith("@priategroup.com")) {
            ArrayList<String> member = GroupMemberSqlManager.getGroupMemberID(mGroup.getGroupId());
            if (member != null) {
                ArrayList<String> contactName = ContactSqlManager.getContactName(member.toArray(new String[]{}));
                String chatroomName = DemoUtils.listToString(contactName, ",");
                mGroup.setName(chatroomName);
            }
        }
        name.setRightContent(mGroup.getName());
//        infoMsgNotify.setChecked(!mGroup.isNotice());

        if (GroupSqlManager.isDiscussionGroup(groupId)) {
            btnGroupQuit.setText(R.string.quit_discussion);
            name.setLeftTitle(getString(R.string.dis_name));
            notice.setLeftTitle(getString(R.string.dis_notice));

        } else {
            btnGroupQuit.setText(R.string.str_group_quit);
            name.setRightContent(mGroup.getName());
            notice.setRightContent(mGroup.getDeclare());
        }


    }

    private void setMemberLongCLick() {
        if (!isLocalDiscussion && isCreat()) {
            gvMember.setOnItemLongClickListener(mOnItemLongClickListener);
            return;
        }
        gvMember.setOnItemLongClickListener(null);
    }


    /**
     * 处理长按操作®
     *
     * @param position 选择位置
     * @return 返回
     */
    private boolean doShowMoreMenu(int position) {
        ECGroupMember item = mAdapter.getItem(position);
        if (item == null || item.getVoipAccount().equals(CCPAppManager.getUserId())) {
            return true;
        }
        if (!"add@yuntongxun.com".equals(item.getVoipAccount())) {
            showMoreMenu(this, item);
        }
        return true;
    }

    /**
     * 多选菜单
     *
     * @param ctx    上下文
     * @param member 账号
     */
    public void showMoreMenu(final Context ctx, final ECGroupMember member) {
        int resource = (member.getMemberRole() == ECGroupMember.Role.MEMBER) ?
                R.array.role_controller_one : R.array.role_controller_multi;
        ECListDialog dialog = new ECListDialog(ctx, resource);
        dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
            @Override
            public void onDialogItemClick(Dialog d, int position) {
                if (position == 0) {
                    // 任命新群组
//                    transferGroupController(member);
                    return;
                }
//                doSetMemberRole(member);
            }
        });
        dialog.setTitle(R.string.ec_group_controller_mode_select);
        dialog.show();
    }


    /**
     * 是否是群组创建者 或者是讨论组
     *
     * @return
     */
    private boolean isLocalDiscussionOrCreat() {
        return isCreat() || isLocalDiscussion;
    }

    /**
     * 是否是群组创建者，管理员 或者是讨论组
     *
     * @return
     */
    private boolean isLocalDiscussionORCreatOrManager() {
        return isCreatOrManager() || isLocalDiscussion;
    }

    /**
     * 是否是群组创建者 或者是讨论组
     *
     * @return
     */
    private boolean isCreat() {
        return mRole == ECGroupMember.Role.OWNER;
    }


    void showProcessDialog(String tips) {
        mPostingdialog = new ECProgressDialog(GroupInfoActivity.this,
                R.string.login_posting_submit);
        mPostingdialog.show();
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


    @OnClick({R.id.info_count, R.id.name, R.id.qr, R.id.notice, R.id.card
            , R.id.info_dissolve, R.id.info_trans_owner, R.id.clear_msg
            , R.id.btn_group_quit, R.id.gag, R.id.set_manager})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.info_count://群成员
                if (members == null || members.isEmpty()) {
                    ToastUtil.showMessage("未获取到群成员信息");
                    return;
                }
                Intent intentMembers = new Intent(this, GroupMembersAct.class);
                intentMembers.putExtra("isLocalDiscussion", isLocalDiscussion);
                intentMembers.putParcelableArrayListExtra(GROUP_MEMBERS, members);
                startActivity(intentMembers);
                break;
            case R.id.name:

                if (!isCreatOrManager()) {
                    return;
                }
                mEditMode = SettingsActivity.CONFIG_TYPE_GROUP_NAME;
                Intent intent = new Intent(GroupInfoActivity.this,
                        EditConfigureActivity.class);
                if (!isLocalDiscussion) {
                    intent.putExtra("edit_title", getString(R.string.edit_group_name));
                } else {
                    intent.putExtra("edit_title", getString(R.string.edit_discussion_name));
                }
                intent.putExtra("edit_default_data", mGroup.getName());

                startActivityForResult(intent, 0xa);
                break;
            case R.id.qr:
                Intent intentQR = new Intent(GroupInfoActivity.this, GroupQRUI.class);
                intentQR.putExtra("group", mGroup);
                startActivity(intentQR);
                break;
            case R.id.notice:
                if (!isCreatOrManager()) {
                    return;
                }
                mEditMode = SettingsActivity.CONFIG_TYPE_GROUP_NOTICE;
                Intent intentEdit = new Intent(GroupInfoActivity.this,
                        EditConfigureActivity.class);
                if (isLocalDiscussion) {
                    intentEdit.putExtra("edit_title", getString(R.string.edit_discussion_notice));
                } else {
                    intentEdit.putExtra("edit_title", getString(R.string.edit_group_notice));
                }
                intentEdit.putExtra("edit_default_data", mGroup.getDeclare());
                startActivityForResult(intentEdit, 0xa);
                break;
            case R.id.card:
                startActivity(new Intent(this, GroupMemberCardActivity.class).putExtra("groupId", groupId));
                break;

            case R.id.gag://禁言
                if (!isCreatOrManager()) {
                    return;
                }
                Intent gagIntent = new Intent(this, GroupGagActivity.class);
                gagIntent.putExtra(GROUP_ID, groupId);
                gagIntent.putParcelableArrayListExtra(GROUP_MEMBERS, members);
                startActivity(gagIntent);

                break;
            case R.id.info_dissolve:
                dissolveGroup();
                break;
            case R.id.clear_msg:
                clearMsg();
                break;
            case R.id.btn_group_quit:

                if (isLocalDiscussion) {
                    GroupService.quitGroup(mGroup.getGroupId());
                } else {
                    doOwnerQuitGroup();
                }
                break;
            case R.id.set_manager://设置管理员

                if (!isCreat()) {
                    return;
                }

                Intent managerIntent = new Intent(this, GroupManagerAct.class);
                managerIntent.putExtra(GROUP_ID, groupId);
                startActivity(managerIntent);

                break;

            case R.id.info_trans_owner:// 转让群主

                if (!isCreat()) {
                    return;
                }
                Iterator<ECGroupMember> iterator = members.iterator();

                while (iterator.hasNext()) {
                    ECGroupMember next = iterator.next();
                    if (next == null) {
                        continue;
                    }
                    if (next.getMemberRole() == ECGroupMember.Role.OWNER) {
                        iterator.remove();
                    }
                }

                Intent memntent = new Intent(mContext, GroupMemberControlAct.class)
                        .putParcelableArrayListExtra(GROUP_MEMBERS, members)
                        .putExtra(GROUP_ID, groupId)
                        .putExtra(TYPE, VALUE_TRANS_OWNER);
                startActivityForResult(memntent, RQ_TRANS_OWNER);

                break;

        }
    }

    private void doOwnerQuitGroup() {
        String msg = null;
        final boolean isHasManagerInner = GroupMemberSqlManager.isGroupHasManager(groupId);
        if (!isHasManagerInner) {
            msg = "当前没有管理员、选择取消去设置管理员";
        } else {
            msg = "确定是否退出群组";
        }


        if (isCreat()) {
            ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, msg,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isHasManagerInner) {
                                showProcessDialog(getString(R.string.login_posting_submit));
                                GroupService.quitGroup(groupId);
                            }
                        }
                    });
            buildAlert.setTitle(R.string.app_tip);
            buildAlert.show();
        } else {
            GroupService.quitGroup(groupId);
        }
    }

    /**
     * 清除消息
     */
    private void clearMsg() {
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(
                GroupInfoActivity.this,
                R.string.fmt_delcontactmsg_confirm_group, null,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPostingdialog = new ECProgressDialog(
                                GroupInfoActivity.this, R.string.clear_chat);
                        mPostingdialog.show();
                        ECHandlerHelper handlerHelper = new ECHandlerHelper();
                        handlerHelper.postRunnOnThead(new Runnable() {
                            @Override
                            public void run() {
                                IMessageSqlManager.deleteChattingMessage(mGroup.getGroupId());
                                ToastUtil.showMessage(R.string.clear_msg_success);
                                mClearChatmsg = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissPostingDialog();
                                    }
                                });
                            }
                        });

                    }

                });
        buildAlert.setTitle(R.string.app_tip);
        buildAlert.show();
    }

    /**
     * 解散群组
     */
    private void dissolveGroup() {
        mPostingdialog = new ECProgressDialog(this,
                "请稍后...");
        mPostingdialog.show();

        if (GroupSqlManager.isDiscussionGroup(groupId)) {
            GroupService.quitGroup(mGroup.getGroupId());
            return;
        }

        if (isCreat()) {
            GroupService.disGroup(mGroup.getGroupId());
            return;
        }
        GroupService.quitGroup(mGroup.getGroupId());
    }


    private void goBack() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RELOAD, mClearChatmsg);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onSynsGroupMember(String groupId) {
        if (!isCreat) {
            return;
        }
        if (isFromDiscussionInviteClick) {
            finish();
            return;
        }
        dismissPostingDialog();
        if (mGroup == null || !mGroup.getGroupId().equals(groupId)) {
            return;
        }

        memCount = mAdapter.getCount();
        members = GroupMemberSqlManager.getGroupMemberWithName(mGroup.getGroupId());

        if (members == null) {
            members = new ArrayList<ECGroupMember>();
        }
        boolean hasSelf = false;
        for (ECGroupMember member : members) {
            if (CCPAppManager.getUserId().equals(member.getVoipAccount())) {
                hasSelf = true;
                mRole = member.getMemberRole();
                break;
            }
        }
        if (!hasSelf) {
            ECContacts contact = ContactSqlManager.getContact(CCPAppManager.getUserId());
            if (contact != null) {
                ECGroupMember member = new ECGroupMember();
                member.setVoipAccount(contact.getContactid());
                member.setRemark(contact.getRemark());
                member.setDisplayName(contact.getNickname());
                members.add(member);
            }
        }


        if (isLocalDiscussion) {
            gag.setVisibility(View.GONE);
            setManager.setVisibility(View.GONE);
            infoTransOwner.setVisibility(View.GONE);
        } else {
            setManager.setLeftTitle("设置管理员");
            setManager.setVisibility(isCreat() ? View.VISIBLE : View.GONE);
            infoTransOwner.setVisibility(isCreat() ? View.VISIBLE : View.GONE);

            gag.setVisibility(isCreatOrManager() ? View.VISIBLE : View.GONE);
            gag.setLeftTitle("设置群内禁言");


            if (isCreat()) {
                infoDissolve.setVisibility(View.VISIBLE);
            } else {
                infoDissolve.setVisibility(View.GONE);
            }
        }

        int memCount = members.size();
        mAdapter.setData(members);
        if (isLocalDiscussion) {
            infoCount.setRightContent(getString(R.string.str_discussion_members_tips,
                    memCount));
        } else {
            infoCount.setRightContent(getString(R.string.str_group_members_tips,
                    memCount));
        }

    }

    private boolean isCreatOrManager() {
        return mRole == ECGroupMember.Role.OWNER || mRole == ECGroupMember.Role.MANAGER;
    }

    @Override
    public void onSyncGroup() {
    }

    @Override
    public void onSyncGroupInfo(String groupId) {
        dismissPostingDialog();
        if (mGroup == null || !mGroup.getGroupId().equals(groupId)) {
            return;
        }
        mGroup = GroupSqlManager.getECGroup(groupId);
        isLocalDiscussion = GroupSqlManager.isDiscussionGroup(groupId);
        refreshGroupInfo();
        ;
    }

    @Override
    public void onGroupDel(String groupId) {
        if (mGroup == null || !mGroup.getGroupId().equals(groupId)) {
            return;
        }
        dismissPostingDialog();
        ECGroup ecGroup = GroupSqlManager.getECGroup(mGroup.getGroupId());
        Intent intent = new Intent(this, ChattingActivity.class);
        intent.putExtra(EXTRA_QUEIT, true);
        setResult(RESULT_OK, intent);
        if (ecGroup == null) {
            // 群组被解散
            finish();
            return;
        }
        finish();
        // 更新群组界面 已经退出群组
    }

    @Override
    public void onError(ECError error) {
        dismissPostingDialog();
    }

    @Override
    public void onUpdateGroupAnonymitySuccess(String groupId, boolean isAnonymity) {
        dismissCommonPostingDialog();
        ToastUtil.showMessage(R.string.modify_success);
        try {
            ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_SHOW_CHATTING_NAME, isAnonymity, true);
        } catch (InvalidClassException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 关闭对话框
     */
    public void dismissCommonPostingDialog() {
        if (mPostingdialog == null || !mPostingdialog.isShowing()) {
            return;
        }
        mPostingdialog.dismiss();
        mPostingdialog = null;
    }

}
