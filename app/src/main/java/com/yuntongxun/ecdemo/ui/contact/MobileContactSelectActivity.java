
package com.yuntongxun.ecdemo.ui.contact;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.KeyBordUtil;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.storage.GroupSqlManager;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.ContactListFragment;
import com.yuntongxun.ecdemo.ui.MobileContactFragment;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.chatting.ChattingActivity;
import com.yuntongxun.ecdemo.ui.chatting.ChattingFragment;
import com.yuntongxun.ecdemo.ui.group.GroupMemberService;
import com.yuntongxun.ecdemo.ui.group.GroupMemberService.OnSynsGroupMemberListener;
import com.yuntongxun.ecdemo.ui.interphone.InterPhoneListActivity;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.ECGroupManager.OnCreateGroupListener;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * com.yuntongxun.ecdemo.ui.contact in ECDemo_Android
 * Created by Jorstin on 2015/4/1.
 * 创建讨论组
 */
public class MobileContactSelectActivity extends BaseActivity implements
        ContactListFragment.OnContactClickListener, OnCreateGroupListener, OnSynsGroupMemberListener {
    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.btn_invite)
    Button btnInvite;
    @BindView(R.id.contact_container)
    FrameLayout contactContainer;
    private ECProgressDialog mPostingdialog;
    private static final String TAG = "ECSDK_Demo.ContactSelectListActivity";
    /**
     * 查看群组
     */
    public static final int REQUEST_CODE_VIEW_GROUP_OWN = 0x2a;
    private boolean mNeedResult;

    private boolean isFromCreateDiscussion = false;
    private MobileContactFragment mobileContactFragment;
    private String type;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_contact_select;
    }

    @Override
    protected void initWidgetAciotns() {

    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        isFromCreateDiscussion = false;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();
        type = getIntent().getStringExtra(InterPhoneListActivity.TYPE);
        mNeedResult = getIntent().getBooleanExtra("group_select_need_result", false);
        isFromCreateDiscussion = getIntent().getBooleanExtra("isFromCreateDiscussion", false);
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(R.id.contact_container) == null) {
            mobileContactFragment = MobileContactFragment.newInstance(ContactListFragment.TYPE_SELECT);
            fm.beginTransaction().add(R.id.contact_container, mobileContactFragment).commit();

        }

        ECGroupManager ecGroupManager = SDKCoreHelper.getECGroupManager();
        if (ecGroupManager == null) {
            finish();
            return;
        }


    }


    /**
     * 获取讨论组名称
     */
    private ECGroup getDisGroup() {
        ECGroup group = new ECGroup();
        // 设置讨论组名称
        group.setName(getDisGroupName());
        // 设置讨论组公告
        group.setDeclare("");
        group.setScope(ECGroup.Scope.TEMP);
        // 讨论组验证权限，需要身份验证
        group.setPermission(ECGroup.Permission.AUTO_JOIN);
        // 设置讨论组创建者
        group.setOwner(CCPAppManager.getUserId());

        group.setProvince("");
        group.setCity("");
        group.setIsDiscuss(true);
        return group;
    }


    private String getDisGroupName() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CCPAppManager.getClientUser().getUserName());
        stringBuilder.append("、");
        for (int i = 0; i < memberArrs.length; i++) {
            if (i == 5) {
                break;
            }
            if (TextUtils.isEmpty(memberArrs[i])){
              continue;
            }
            stringBuilder.append(memberArrs[i]);
            if (!(i == memberArrs.length - 1)) {
                stringBuilder.append("、");
            }
        }
        stringBuilder.append("创建的讨论组");
        return stringBuilder.toString();
    }

    private String[] phoneArr;


    @Override
    protected void onResume() {
        super.onResume();
        GroupMemberService.addListener(this);


        if (TextUtils.equals(type, InterPhoneListActivity.CREAT_POC)) {
            initTooleBar(mobileContactFragment.getTitleBar(), true, "创建实时对讲");
            btnInvite.setText("创建");
        } else {
            initTooleBar(mobileContactFragment.getTitleBar(), true, "邀请新成员");
        }

        mobileContactFragment.getTitleBar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void goBack() {
        KeyBordUtil.hideSoftKeyboard(MobileContactSelectActivity.this);
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult: requestCode=" + requestCode
                + ", resultCode=" + resultCode + ", data=" + data);

        // If there's no data (because the user didn't select a picture and
        // just hit BACK, for example), there's nothing to do.
        if (requestCode == REQUEST_CODE_VIEW_GROUP_OWN) {
            if (data == null) {
                return;
            }
        } else if (resultCode != RESULT_OK) {
            LogUtil.d("onActivityResult: bail due to resultCode=" + resultCode);
            return;
        }

        String contactId = data.getStringExtra(ChattingFragment.RECIPIENTS);
        String contactUser = data.getStringExtra(ChattingFragment.CONTACT_USER);
        if (contactId != null && contactId.length() > 0) {
            Intent intent = new Intent(this, ChattingActivity.class);
            intent.putExtra(ChattingFragment.RECIPIENTS, contactId);
            intent.putExtra(ChattingFragment.CONTACT_USER, contactUser);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public void onContactClick(int count) {
        tvCount.setText("已选择：" + count + "人");
    }

    @Override
    public void onSelectGroupClick() {
    }

    private ECGroup mGroup;
    private String[] memberArrs;

    /**
     * 群组创建成功回调
     * @param error
     * @param group
     */
    @Override
    public void onCreateGroupComplete(ECError error, ECGroup group) {
        dismissCommonPostingDialog();
        if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
            // 创建的群组实例化到数据库
            // 其他的页面跳转逻辑
            group.setIsNotice(true);
            this.mGroup = group;
            GroupSqlManager.insertGroup(group, true, false, true);

            showCommonProcessDialog("");
            /**
             * 邀请成员
             */
            GroupMemberService.inviteMembers(mGroup.getGroupId(), "",
                    ECGroupManager.InvitationMode.FORCE_PULL, phoneArr);
        } else {
            ToastUtil.showMessage("创建讨论组失败[" + error.errorCode + "]");
            finish();
        }
    }
    /**
     * 邀请成员回调
     */
    @Override
    public void onSynsGroupMember(String groupId) {
        if (mGroup != null) {
            dismissCommonPostingDialog();

            CCPAppManager.startChattingAction(MobileContactSelectActivity.this, groupId,
                    mGroup.getName());
            finish();
        }
    }

    public void showCommonProcessDialog(String tips) {
        mPostingdialog = new ECProgressDialog(this, R.string.progress_common);
        mPostingdialog.show();
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

    /**
     * 创建讨论组
     */
    @OnClick(R.id.btn_invite)
    public void onViewClicked() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        if (fragments.get(0) instanceof MobileContactFragment) {
            String chatuser = ((MobileContactFragment) fragments.get(0)).getChatuser();
            String[] split = chatuser.split(",");
            String userNameArr = ((MobileContactFragment) fragments.get(0)).getChatuserName();
            if (TextUtils.isEmpty(userNameArr)){
                ToastUtil.showMessage("请选择邀请的成员");
                return;
            }
            memberArrs = userNameArr.split(",");
            phoneArr = split;
            if (split.length == 1 && !mNeedResult) {
                String recipient = split[0];
                CCPAppManager.startChattingAction(MobileContactSelectActivity.this, recipient, recipient);
                finish();
                return;
            }

            if (mNeedResult && isFromCreateDiscussion) {
                if (split.length > 0) {
                    showCommonProcessDialog(null);
                    SDKCoreHelper.getECGroupManager().createGroup(getDisGroup(), this);
                    return;
                }
            }

            if (TextUtils.equals(type, InterPhoneListActivity.CREAT_POC) && mNeedResult) {
                ArrayList<ECContacts> selectedEcontacts = ((MobileContactFragment) fragments.get(0)).getSelectedEcontacts();
                if (selectedEcontacts.size() == 0) {
                    ToastUtil.showMessage("请先选择联系人");
                    return;
                }else{
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra("selected_econtacts", selectedEcontacts);
                    setResult(-1, intent);
                    finish();
                }

            }
            if (mNeedResult) {
                Intent intent = new Intent();
                intent.putExtra("Select_Conv_User", split);
                setResult(-1, intent);
                finish();
                return;
            }
        }
    }


}
