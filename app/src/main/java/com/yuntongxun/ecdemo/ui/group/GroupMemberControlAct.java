package com.yuntongxun.ecdemo.ui.group;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.KeyBordUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.ContactListFragment;
import com.yuntongxun.ecdemo.ui.GagMembesFragment;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroupMember;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

import static android.R.attr.button;
import static com.yuntongxun.ecdemo.R.id.ll;
import static com.yuntongxun.ecdemo.R.id.ll_bottom;
import static com.yuntongxun.ecdemo.ui.group.GroupGagActivity.TYPE;
import static com.yuntongxun.ecdemo.ui.group.GroupGagActivity.VALUE_DEl_MEMEMBERS;
import static com.yuntongxun.ecdemo.ui.group.GroupGagActivity.VALUE_GAG_MEMEMBERS;
import static com.yuntongxun.ecdemo.ui.group.GroupInfoActivity.GROUP_MEMBERS;
import static com.yuntongxun.ecdemo.ui.group.GroupManagerAct.VALUE_SET_MANAGER;
import static com.yuntongxun.ecdemo.ui.group.GroupMemberService.forbidMemberSpeakStatus;


/**
 * Created by zlk on 2017/8/19.
 * 禁言，删除，设置管理员，转让群主等
 */

public class GroupMemberControlAct extends BaseGroupReceiveAct implements
        ContactListFragment.OnContactClickListener, GagMembesFragment.OnSelectedEcontactLisener {

    @BindView(ll_bottom)
    LinearLayout llBottom;

    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.btn_invite)
    Button btnInvite;
    @BindView(R.id.contact_container)
    FrameLayout contactContainer;
    private static final String TAG = "ECSDK_Demo.ContactSelectListActivity";
    /**
     * 查看群组
     */
    public static final int REQUEST_CODE_VIEW_GROUP_OWN = 0x2a;
    private boolean mNeedResult;

    private GagMembesFragment gagMembesFragment;
    private String groupId;
    private int type;
    public static final int VALUE_TRANS_OWNER = 0x7;//群主转让

    @Override
    protected int getLayoutId() {
        return R.layout.layout_groupmember_select;
    }

    @Override
    protected void initWidgetAciotns() {
        llBottom.setVisibility(type == VALUE_SET_MANAGER ? View.GONE : View.VISIBLE);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();

        mNeedResult = getIntent().getBooleanExtra("group_select_need_result", false);
        ArrayList<ECGroupMember> members = getIntent().getParcelableArrayListExtra(GROUP_MEMBERS);
        groupId = getIntent().getStringExtra(GroupInfoActivity.GROUP_ID);
        type = getIntent().getIntExtra(TYPE, GroupGagActivity.VALUE_GAG_MEMEMBERS);


        if (fm.findFragmentById(R.id.contact_container) == null) {
            if (type == GroupGagActivity.VALUE_GAG_MEMEMBERS||type == GroupGagActivity.VALUE_DEl_MEMEMBERS){
                gagMembesFragment = GagMembesFragment.newInstance(GagMembesFragment.TYPE_SELECT);
            }else {
                gagMembesFragment = GagMembesFragment.newInstance(GagMembesFragment.TYPE_SET_MANAGER);
            }


            fm.beginTransaction().add(R.id.contact_container, gagMembesFragment).commit();
            gagMembesFragment.setLisener(this);
            gagMembesFragment.setData(members);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (type == GroupGagActivity.VALUE_GAG_MEMEMBERS) {
            btnInvite.setText("确定");
            initTooleBar(gagMembesFragment.getTitleBar(), true, "添加禁言成员");
            gagMembesFragment.getTitleBar().setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goBack();
                }
            });
        } else if (type == VALUE_DEl_MEMEMBERS) {
            btnInvite.setText("删除");
            initTooleBar(gagMembesFragment.getTitleBar(), true, "删除群成员");
            gagMembesFragment.getTitleBar().setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goBack();
                }
            });
        } else if (type == VALUE_SET_MANAGER) {
            btnInvite.setText("确定");
            initTooleBar(gagMembesFragment.getTitleBar(), true, "添加管理员");
            gagMembesFragment.getTitleBar().setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goBack();
                }
            });
        }else if (type == VALUE_TRANS_OWNER) {
            initTooleBar(gagMembesFragment.getTitleBar(), true, "转让群组");
            llBottom.setVisibility(View.GONE);
            gagMembesFragment.getTitleBar().setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goBack();
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void goBack() {
        KeyBordUtil.hideSoftKeyboard(GroupMemberControlAct.this);
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        LogUtil.d(TAG, "onActivityResult: requestCode=" + requestCode
//                + ", resultCode=" + resultCode + ", data=" + data);
//
//        // If there's no data (because the user didn't select a picture and
//        // just hit BACK, for example), there's nothing to do.
//        if (requestCode == REQUEST_CODE_VIEW_GROUP_OWN) {
//            if (data == null) {
//                return;
//            }
//        } else if (resultCode != RESULT_OK) {
//            LogUtil.d("onActivityResult: bail due to resultCode=" + resultCode);
//            return;
//        }
//
//        String contactId = data.getStringExtra(ChattingFragment.RECIPIENTS);
//        String contactUser = data.getStringExtra(ChattingFragment.CONTACT_USER);
//        if (contactId != null && contactId.length() > 0) {
//            Intent intent = new Intent(this, ChattingActivity.class);
//            intent.putExtra(ChattingFragment.RECIPIENTS, contactId);
//            intent.putExtra(ChattingFragment.CONTACT_USER, contactUser);
//            startActivity(intent);
//            finish();
//        }
    }


    @Override
    public void onContactClick(int count) {
        tvCount.setText("已选择：" + count + "人");
    }

    @Override
    public void onSelectGroupClick() {
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

    private String[] memberArrs;
    private String[] phoneArr;

    @OnClick(R.id.btn_invite)
    public void onViewClicked() {
//        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (gagMembesFragment != null) {
            String chatuser = gagMembesFragment.getChatuser();
            if (TextUtils.isEmpty(chatuser)) {
                return;
            }
            String[] split = chatuser.split(",");
            String userNameArr = gagMembesFragment.getChatuserName();
            memberArrs = userNameArr.split(",");
            phoneArr = split;


            if (type == VALUE_GAG_MEMEMBERS) {
                doSetMemberSpeakStatus(userNameArr, phoneArr);
            } else if (type == VALUE_DEl_MEMEMBERS) {
                doRemoveMember(userNameArr, phoneArr);
            }
//            else if (type == VALUE_SET_MANAGER) {
//                handleSetManager(userNameArr, phoneArr);
//            }
        }
    }


    public void doLogicSetMemberRole(final String groupId, final String member, final ECGroupManager.ECGroupMemberRole enRole) {

        SDKCoreHelper.getECGroupManager().setGroupMemberRole(groupId, member, enRole, new ECGroupManager.OnSetGroupMemberRoleListener() {

            @Override
            public void onSetGroupMemberRoleComplete(ECError error, String groupId) {
                if (isSuccess(error)) {
                    GroupMemberService.synsGroupMember(groupId);
                    setData("success");
                } else {
                    ToastUtil.showMessage("设置失败[" + error.errorCode + "]");
                    setData("fail");
                }
            }
        });
    }

    private void setData(String resutl) {
        Intent intent = new Intent();
        intent.putExtra("result",resutl);
        setResult(Activity.RESULT_OK,intent);
        dismissPostingDialog();
        finish();
    }


    private boolean isSuccess(ECError error) {
        if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
            return true;
        }
        return false;
    }


    /**
     * 移除群组成员
     *
     * @param userNameArr
     * @param phoneArr
     */
    private void doRemoveMember(String userNameArr, final String[] phoneArr) {
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(
                mContext,
                mContext.getString(R.string.str_group_member_remove_tips,
                        userNameArr),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProcessDialog(mContext.getString(R.string.group_remove_member_posting));

                        for (String item : phoneArr) {
                            GroupMemberService.removerMember(groupId,
                                    item);
                            finish();
                        }


                    }
                });

        buildAlert.setTitle(R.string.app_tip);
        buildAlert.show();
    }


    /**
     * 设置群组成员禁言状态
     *
     * @param userNameArr
     * @param phoneArr
     */
    private void doSetMemberSpeakStatus(String userNameArr, final String[] phoneArr) {
        String msg = getString(R.string.str_group_member_speak_tips, userNameArr);
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, msg,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        showProcessDialog(getString(R.string.login_posting_submit));

                        for (String item : phoneArr) {
                            forbidMemberSpeakStatus(groupId, item,
                                    true);
                            dismissCommonPostingDialog();
                            finish();
                        }
                    }
                });

        buildAlert.setTitle(R.string.app_tip);
        buildAlert.show();
    }

    private ECProgressDialog mPostingdialog;

    void showProcessDialog(String tips) {
        mPostingdialog = new ECProgressDialog(GroupMemberControlAct.this,
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


    //设置管理员回调
    @Override
    public void onSelectedEcontact(ECContacts ecContact) {


        if (ecContact == null) {
            return;
        }
        if (type == VALUE_SET_MANAGER) {//设置管理员
            showProcessDialog(mContext.getString(R.string.group_set_manager_posting));
            doLogicSetMemberRole(groupId, ecContact.getContactid(), ECGroupManager.ECGroupMemberRole.MANAGER);
        }else if (type == VALUE_TRANS_OWNER){//群主转让
            // 任命新群组
            transferGroupController(ecContact);

        }
    }

    /**
     * 转让群组控制权限
     * @param member 群组成员
     */
    private void transferGroupController(final ECContacts member) {
        String msg = getString(R.string.group_controller_transfer , member.getNickname());
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, msg,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProcessDialog(getString(R.string.login_posting_submit));
                        doLogicSetMemberRole(groupId, member.getContactid(), ECGroupManager.ECGroupMemberRole.TRANSFER);
                    }
                });

        buildAlert.setTitle(R.string.app_tip);
        buildAlert.show();
    }


}