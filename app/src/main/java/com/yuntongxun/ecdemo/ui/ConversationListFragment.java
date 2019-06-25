/*
 *  Copyright (c) 2015 The CCP project authors. All Rights Reserved.
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
package com.yuntongxun.ecdemo.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECListDialog;
import com.yuntongxun.ecdemo.common.dialog.ECProgressDialog;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.CommonPoPWindow;
import com.yuntongxun.ecdemo.common.view.NetWarnBannerView;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.storage.ConversationSqlManager;
import com.yuntongxun.ecdemo.storage.GroupSqlManager;
import com.yuntongxun.ecdemo.storage.IMessageSqlManager;
import com.yuntongxun.ecdemo.ui.chatting.CustomerServiceHelper;
import com.yuntongxun.ecdemo.ui.chatting.model.Conversation;
import com.yuntongxun.ecdemo.ui.contact.ContactLogic;
import com.yuntongxun.ecdemo.ui.friend.AddFriendActivity;
import com.yuntongxun.ecdemo.ui.group.CreateGroupActivity;
import com.yuntongxun.ecdemo.ui.group.GroupService;
import com.yuntongxun.ecdemo.ui.group.SystemNoticeActivity;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupOption;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import java.io.InvalidClassException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 会话界面
 * Created by Jorstin on 20158.
 */
public class ConversationListFragment extends LazyFrament implements CCPListAdapter.OnListAdapterCallBackListener {

    private static final String TAG = "ECSDK_Demo.ConversationListFragment";
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.main_chatting_lv)
    ListView mainChattingLv;
    @BindView(R.id.empty_conversation_tv)
    TextView emptyConversationTv;


    private NetWarnBannerView mBannerView;
    private ConversationAdapter mAdapter;
    private OnUpdateMsgUnreadCountsListener mAttachListener;
    private ECProgressDialog mPostingdialog;
    private View.OnClickListener myOnPopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            poPWindow.dismiss();
            switch (v.getId()) {
                //发起群聊
                case R.id.tv_pop_chat:
                    startActivity(CreateGroupActivity.class);
                    break;
                //添加联系人
                case R.id.tv_pop_add:

                    startActivity(AddFriendActivity.class);

                    break;
                //扫一扫
                case R.id.tv_pop_scan:
                    startActivity(CaptureActivity.class);
                    break;
            }
        }
    };
    private CommonPoPWindow poPWindow;

    public static ConversationListFragment newInstance() {
        ConversationListFragment fragment = new ConversationListFragment();
        return fragment;
    }


    final private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View visew, int position,
                                long id) {

            if (mAdapter != null) {
                int headerViewsCount = mainChattingLv.getHeaderViewsCount();
                if (position < headerViewsCount) {
                    return;
                }
                int _position = position - headerViewsCount;

                if (mAdapter == null || mAdapter.getItem(_position) == null) {
                    return;
                }
                Conversation conversation = mAdapter.getItem(_position);
                int type = conversation.getMsgType();
                if (type == 1000) {
                    Intent intent = new Intent(getActivity(), SystemNoticeActivity.class);
                    startActivity(intent);
                    return;
                }
                if (ContactLogic.isCustomService(conversation.getSessionId())) {
                    showProcessDialog();
                    dispatchCustomerService(conversation.getSessionId());
                    return;
                }

                //conversation.getUsername() 没用，在那边是getmarknam的取的
                CCPAppManager.startChattingAction(getActivity(), conversation.getSessionId(), conversation.getUsername());

                //@相关
                try {
                    if (TextUtils.equals(conversation.getSessionId() + CCPAppManager.getUserId(), ECPreferences.getSharedPreferences().getString(ECPreferenceSettings.SETTINGS_AT.getId(), ""))) {
                        ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_AT, "", true);
                    }
                } catch (InvalidClassException e) {
                }
            }
        }
    };

    /**
     * 处理在线客服界面请求
     *
     * @param sessionId
     */
    private void dispatchCustomerService(String sessionId) {
        CustomerServiceHelper.startService(sessionId, new CustomerServiceHelper.OnStartCustomerServiceListener() {
            @Override
            public void onServiceStart(String event) {
                dismissPostingDialog();
                CCPAppManager.startCustomerServiceAction(getActivity(), event);
            }

            @Override
            public void onError(ECError error) {
                dismissPostingDialog();
            }
        });
    }

    private final AdapterView.OnItemLongClickListener mOnLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mAdapter != null) {
                int headerViewsCount = mainChattingLv.getHeaderViewsCount();
                if (position < headerViewsCount) {
                    return false;
                }
                int _position = position - headerViewsCount;

                if (mAdapter == null || mAdapter.getItem(_position) == null) {
                    return false;
                }
                Conversation conversation = mAdapter.getItem(_position);
                final int itemPosition = position;
                final String[] menu = buildMenu(conversation);
                ECListDialog dialog = new ECListDialog(getActivity(), /*new String[]{getString(R.string.main_delete)}*/menu);
                dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
                    @Override
                    public void onDialogItemClick(Dialog d, int position) {
                        handleContentMenuClick(itemPosition, position);
                    }
                });
                String markName = AvatorUtil.getInstance().getMarkName(conversation.getSessionId());
                dialog.setTitle(TextUtils.equals(markName, RestServerDefines.FILE_ASSISTANT) ? "文件助手" : markName);
                dialog.show();
                return true;
            }
            return false;
        }
    };


    private String[] buildMenu(Conversation conversation) {//设置长按条目 2*2
        if (conversation != null && conversation.getSessionId() != null) {
            boolean isTop = ConversationSqlManager.querySessionisTopBySessionId(conversation.getSessionId());//支持单人、群组
            if (conversation.getSessionId().toLowerCase().startsWith("g")) {
                ECGroup ecGroup = GroupSqlManager.getECGroup(conversation.getSessionId());
                boolean isNotice = ecGroup.isNotice();
                if (ecGroup == null || !GroupSqlManager.getJoinState(ecGroup.getGroupId())) {
                    return new String[]{getString(R.string.main_delete)};
                }
                if (ecGroup.isNotice()) {
                    if (isTop) {
                        return new String[]{getString(R.string.main_delete), getString(R.string.cancel_top), getString(R.string.menu_mute_notify)};

                    } else {
                        return new String[]{getString(R.string.main_delete), getString(R.string.set_top), getString(R.string.menu_mute_notify)};
                    }
                } else {
                    if (isTop) {
                        return new String[]{getString(R.string.main_delete), getString(R.string.cancel_top), getString(R.string.menu_notify)};
                    } else {
                        return new String[]{getString(R.string.main_delete), getString(R.string.set_top), getString(R.string.menu_notify)};

                    }

                }
            } else {
                if (isTop) {
                    return new String[]{getString(R.string.main_delete), getString(R.string.cancel_top)};
                } else {
                    return new String[]{getString(R.string.main_delete), getString(R.string.set_top)};

                }

            }
        }
        return new String[]{getString(R.string.main_delete)};
    }


    private void setcancelTopSession(ArrayList<String> arrayList, String item) {
        if (!arrayList.contains(item)) {
            ConversationSqlManager.updateSessionToTop(item, false);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        registerReceiver(new String[]{GroupService.ACTION_SYNC_GROUP, IMessageSqlManager.ACTION_SESSION_DEL});

    }

    @Override
    public void onResume() {
        super.onResume();
        updateConnectState();
        IMessageSqlManager.registerMsgObserver(mAdapter);
        mAdapter.notifyChange();
    }

    @Override
    public void fetchData() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAttachListener = (OnUpdateMsgUnreadCountsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnUpdateMsgUnreadCountsListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        IMessageSqlManager.unregisterMsgObserver(mAdapter);
    }

    /**
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        titleBar.setMyCenterTitle("消息").setMySettingIcon(R.drawable.message_navbtn_go_sel)
                .setSettingIconOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFunctionPop();
                    }
                });
        if (mainChattingLv != null) {
            mainChattingLv.setAdapter(null);

            if (mBannerView != null) {
                mainChattingLv.removeHeaderView(mBannerView);
            }
        }


        mainChattingLv.setEmptyView(emptyConversationTv);
        mainChattingLv.setDrawingCacheEnabled(false);
        mainChattingLv.setScrollingCacheEnabled(false);

        mainChattingLv.setOnItemLongClickListener(mOnLongClickListener);
        mainChattingLv.setOnItemClickListener(mItemClickListener);
        mBannerView = new NetWarnBannerView(getActivity());
        mBannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reTryConnect();
            }
        });
        mainChattingLv.addHeaderView(mBannerView);
        mAdapter = new ConversationAdapter(getActivity(), this);
        mainChattingLv.setAdapter(mAdapter);

        registerForContextMenu(mainChattingLv);
    }

    private void showFunctionPop() {

        poPWindow = new CommonPoPWindow(mContext, new CommonPoPWindow.PopCallback() {
            @Override
            public View getPopWindowChildView(View mMenuView) {
                mMenuView.findViewById(R.id.tv_pop_chat).setOnClickListener(myOnPopClickListener);
                mMenuView.findViewById(R.id.tv_pop_add).setOnClickListener(myOnPopClickListener);
                mMenuView.findViewById(R.id.tv_pop_scan).setOnClickListener(myOnPopClickListener);
                return mMenuView;
            }
        }, R.layout.popwindow_function);

        poPWindow.showAsDropDown(titleBar.getRightBtn(), 55, 55);

    }


    private String getAutoRegistAccount() {
        SharedPreferences sharedPreferences = ECPreferences
                .getSharedPreferences();
        ECPreferenceSettings registAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
        String registAccount = sharedPreferences.getString(registAuto.getId(),
                (String) registAuto.getDefaultValue());
        return registAccount;
    }

    private void reTryConnect() {
        ECDevice.ECConnectState connectState = SDKCoreHelper.getConnectState();
        if (connectState == null || connectState == ECDevice.ECConnectState.CONNECT_FAILED) {

            if (!TextUtils.isEmpty(getAutoRegistAccount())) {
                SDKCoreHelper.init(getActivity());
            }
        }
    }

    public void updateConnectState() {
        if (!isAdded()) {
            return;
        }
        ECDevice.ECConnectState connect = SDKCoreHelper.getConnectState();
        if (connect == ECDevice.ECConnectState.CONNECTING) {
            mBannerView.setNetWarnText(getString(R.string.connecting_server));
            mBannerView.reconnect(true);
        } else if (connect == ECDevice.ECConnectState.CONNECT_FAILED) {
            mBannerView.setNetWarnText(getString(R.string.connect_server_error));
            mBannerView.reconnect(false);
        } else if (connect == ECDevice.ECConnectState.CONNECT_SUCCESS) {
            mBannerView.hideWarnBannerView();
        }
        LogUtil.d(TAG, "updateConnectState connect :" + connect.name());
    }


    private Boolean handleContentMenuClick(int convresion, int position) {
        if (mAdapter != null) {
            int headerViewsCount = mainChattingLv.getHeaderViewsCount();
            if (convresion < headerViewsCount) {
                return false;
            }
            int _position = convresion - headerViewsCount;

            if (mAdapter == null || mAdapter.getItem(_position) == null) {
                return false;
            }
            final Conversation conversation = mAdapter.getItem(_position);
            switch (position) {
                case 0:
                    showProcessDialog();
                    ECHandlerHelper handlerHelper = new ECHandlerHelper();
                    handlerHelper.postRunnOnThead(new Runnable() {
                        @Override
                        public void run() {
                            IMessageSqlManager.deleteChattingMessage(conversation.getSessionId());
                            ToastUtil.showMessage(R.string.clear_msg_success);
                            ConversationListFragment.this.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissPostingDialog();
                                    mAdapter.notifyChange();
                                }
                            });
                        }
                    });
                    break;
                case 2:
                    showProcessDialog();
                    final boolean notify = GroupSqlManager.isGroupNotify(conversation.getSessionId());
                    ECGroupOption option = new ECGroupOption();
                    option.setGroupId(conversation.getSessionId());
                    option.setRule(notify ? ECGroupOption.Rule.SILENCE : ECGroupOption.Rule.NORMAL);
                    GroupService.setGroupMessageOption(option, new GroupService.GroupOptionCallback() {
                        @Override
                        public void onComplete(String groupId) {
                            if (mAdapter != null) {
                                mAdapter.notifyChange();
                            }
                            ToastUtil.showMessage(notify ? R.string.new_msg_mute_notify : R.string.new_msg_notify);
                            dismissPostingDialog();
                        }

                        @Override
                        public void onError(ECError error) {
                            dismissPostingDialog();
                            ToastUtil.showMessage("设置失败");
                        }
                    });
                    break;

                case 1:
                    showProcessDialog();
                    final boolean isTop = ConversationSqlManager.querySessionisTopBySessionId(conversation.getSessionId());
                    ECChatManager chatManager = SDKCoreHelper.getECChatManager();
                    if (chatManager == null) {
                        return null;
                    }
                    chatManager.setSessionToTop(conversation.getSessionId(), !isTop, new ECChatManager.OnSetContactToTopListener() {
                        @Override
                        public void onSetContactResult(ECError error, String contact) {

                            dismissPostingDialog();
                            if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                                ConversationSqlManager.updateSessionToTop(conversation.getSessionId(), !isTop);
                                mAdapter.notifyChange();
                                ToastUtil.showMessage("设置成功");
                            } else {
                                ToastUtil.showMessage("设置失败");
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.conversation;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

    }

    @Override
    protected void initWidgetActions() {

    }

    @Override
    public void OnListAdapterCallBack() {
        if (mAttachListener != null) {
            mAttachListener.OnUpdateMsgUnreadCounts();
        }
    }


    @OnClick(R.id.title_bar)
    public void onViewClicked() {
    }


    public interface OnUpdateMsgUnreadCountsListener {
        void OnUpdateMsgUnreadCounts();
    }

    @Override
    protected void handleReceiver(Context context, Intent intent) {
        super.handleReceiver(context, intent);
        if (GroupService.ACTION_SYNC_GROUP.equals(intent.getAction())
                || IMessageSqlManager.ACTION_SESSION_DEL.equals(intent.getAction())) {
            if (mAdapter != null) {
                mAdapter.notifyChange();
            }
        }
    }

    void showProcessDialog() {
        mPostingdialog = new ECProgressDialog(ConversationListFragment.this.getActivity(), R.string.login_posting_submit);
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
}

