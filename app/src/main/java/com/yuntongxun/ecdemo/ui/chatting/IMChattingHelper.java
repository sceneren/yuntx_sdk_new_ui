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
package com.yuntongxun.ecdemo.ui.chatting;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.BitmapUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.ECNotificationManager;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.FileAccessor;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.SharedPreferencesUtils;
import com.yuntongxun.ecdemo.core.ClientUser;
import com.yuntongxun.ecdemo.pojo.RichTextBean;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.storage.GroupNoticeSqlManager;
import com.yuntongxun.ecdemo.storage.GroupSqlManager;
import com.yuntongxun.ecdemo.storage.IMessageSqlManager;
import com.yuntongxun.ecdemo.storage.ImgInfoSqlManager;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;
import com.yuntongxun.ecdemo.ui.chatting.model.ImgInfo;
import com.yuntongxun.ecdemo.ui.chatting.redpacketutils.CheckRedPacketMessageUtil;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.group.DemoGroupNotice;
import com.yuntongxun.ecdemo.ui.group.GroupNoticeHelper;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECMessage.Direction;
import com.yuntongxun.ecsdk.ECMessage.Type;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.PersonInfo;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECFileMessageBody;
import com.yuntongxun.ecsdk.im.ECImageMessageBody;
import com.yuntongxun.ecsdk.im.ECMessageDeleteNotify;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.ECMessageNotify.NotifyType;
import com.yuntongxun.ecsdk.im.ECMessageReadNotify;
import com.yuntongxun.ecsdk.im.ECMessageRevokeNotify;
import com.yuntongxun.ecsdk.im.ECPreviewMessageBody;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.im.ECUserStateMessageBody;
import com.yuntongxun.ecsdk.im.ECVideoMessageBody;
import com.yuntongxun.ecsdk.im.ECVoiceMessageBody;
import com.yuntongxun.ecsdk.im.friend.ECFriendMessageBody;
import com.yuntongxun.ecsdk.im.friend.FriendInner;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.vov.vitamio.utils.Log;


/**
 * @author Jorstin Chan@容联•云通讯
 * @version 4.0
 * @date 2014-12-12
 */
public class IMChattingHelper implements OnChatReceiveListener,
        ECChatManager.OnDownloadMessageListener {

    private static final String TAG = "IMChattingHelper";
    public static final String INTENT_ACTION_SYNC_MESSAGE = "com.yuntongxun.ecdemo_sync_message";
    public static final String INTENT_ACTION_CHAT_USER_STATE = "com.yuntongxun.ecdemo_chat_state";
    public static final String INTENT_ACTION_CHAT_EDITTEXT_FOUCU = "com.yuntongxun.ecdemo_chat_edit_foucs";

    public static final String INTENT_ACTION_ADD_GROUP_MEMBER = "com.yuntongxun.ecdemo_INTENT_ACTION_ADD_GROUP_MEMBER";
    public static final String INTENT_ACTION_CHANGE_ADMIN = "com.yuntongxun.ecdemo_INTENT_ACTION_CHANGE_ADMIN";
    public static final String ACTION_TRANS_OWNER = "com.yuntonxun.ecdemo.ACTION_TRANS_OWNER";


    public static final String USER_STATE = "chat_state";
    public static final String GROUP_PRIVATE_TAG = "@priategroup.com";
    private static HashMap<String, SyncMsgEntry> syncMessage = new HashMap<String, SyncMsgEntry>();
    private static IMChattingHelper sInstance;
    private boolean isSyncOffline = false;

    public static IMChattingHelper getInstance() {
        if (sInstance == null) {
            sInstance = new IMChattingHelper();
        }
        return sInstance;
    }

    /**
     * 云通讯SDK聊天功能接口
     */
    private ECChatManager mChatManager;
    /**
     * 全局处理所有的IM消息发送回调
     */
    private ChatManagerListener mListener;
    /**
     * 是否是同步消息
     */
    private boolean isFirstSync = false;

    private IMChattingHelper() {
        initManager();
        mListener = new ChatManagerListener();
    }


    public void initManager() {

        mChatManager = SDKCoreHelper.getECChatManager();
    }


    /**
     * 消息发送报告
     */
    private OnMessageReportCallback mOnMessageReportCallback;

    /**
     * 发送ECMessage 消息
     *
     * @param msg
     */
    public static long sendECMessage(ECMessage msg) {
        getInstance().initManager();
        // 获取一个聊天管理器
        ECChatManager manager = getInstance().mChatManager;
        boolean isSelf = CheckRedPacketMessageUtil.isGroupAckMessage(msg);
        if (manager != null) {
            // 调用接口发送IM消息
            boolean isShowChatName = ECPreferences
                    .getSharedPreferences()
                    .getBoolean(
                            ECPreferenceSettings.SETTINGS_SHOW_CHATTING_NAME
                                    .getId(),
                            false);
            msg.setMsgTime(System.currentTimeMillis());

            if (isSelf) {
                LogUtil.d("不发送消息");
                msg.setMsgId(UUID.randomUUID().toString());
            } else {
                manager.sendMessage(msg, getInstance().mListener);
            }

            // 保存发送的消息到数据库
            if (msg.getType() == Type.FILE
                    && msg.getBody() instanceof ECFileMessageBody) {
                ECFileMessageBody fileMessageBody = (ECFileMessageBody) msg.getBody();
                msg.setUserData("fileName=" + fileMessageBody.getFileName());
            }
        } else {
            msg.setMsgStatus(ECMessage.MessageStatus.FAILED);
        }

        if (isSelf) {
            return IMessageSqlManager.insertIMessage(msg,
                    Direction.RECEIVE.ordinal());
        } else {
            return IMessageSqlManager.insertIMessage(msg,
                    ECMessage.Direction.SEND.ordinal());
        }


    }


    public void destroy() {
        if (syncMessage != null) {
            syncMessage.clear();
        }
        mListener = null;
        mChatManager = null;
        isFirstSync = false;
        sInstance = null;
    }

    /**
     * 消息重发
     *
     * @param msg
     * @return
     */
    public static long reSendECMessage(ECMessage msg) {
        ECChatManager manager = getInstance().mChatManager;
        if (manager != null) {
            // 调用接口发送IM消息
            String oldMsgId = msg.getMsgId();

            if (msg.getType() == Type.IMAGE
                    && IMessageSqlManager.isFireMsg(oldMsgId)) {
                msg.setUserData("fireMessage");
            }

            manager.sendMessage(msg, getInstance().mListener);
            if (msg.getType() == ECMessage.Type.IMAGE) {
                ImgInfo imgInfo = ImgInfoSqlManager.getInstance().getImgInfo(
                        oldMsgId);
                if (imgInfo == null
                        || TextUtils.isEmpty(imgInfo.getBigImgPath())) {
                    return -1;
                }
                String bigImagePath = new File(FileAccessor.getImagePathName(),
                        imgInfo.getBigImgPath()).getAbsolutePath();
                imgInfo.setMsglocalid(msg.getMsgId());
                ECFileMessageBody body = (ECFileMessageBody) msg.getBody();
                body.setLocalUrl(bigImagePath);
                BitmapFactory.Options options = DemoUtils
                        .getBitmapOptions(new File(FileAccessor.IMESSAGE_IMAGE,
                                imgInfo.getThumbImgPath()).getAbsolutePath());
                msg.setUserData("outWidth://" + options.outWidth
                        + ",outHeight://" + options.outHeight + ",THUMBNAIL://"
                        + msg.getMsgId());
                ImgInfoSqlManager.getInstance().updateImageInfo(imgInfo);
            }
            // 保存发送的消息到数据库
            return IMessageSqlManager.changeResendMsg(msg.getId(), msg);
        }
        return -1;
    }

    //发送图片
    public static long sendImageMessage(ImgInfo imgInfo, ECMessage message) {
        ECChatManager manager = getInstance().mChatManager;
        if (manager != null) {
            // 调用接口发送IM消息
            if (ChattingFragment.isFireMsg
                    || IMessageSqlManager.isFireMsg(message.getMsgId())) {
                message.setUserData("fireMessage");
            }
            manager.sendMessage(message, getInstance().mListener);

            if (TextUtils.isEmpty(message.getMsgId())) {
                return -1;
            }
            imgInfo.setMsglocalid(message.getMsgId());
            BitmapFactory.Options options = DemoUtils
                    .getBitmapOptions(new File(FileAccessor.IMESSAGE_IMAGE,
                            imgInfo.getThumbImgPath()).getAbsolutePath());
            message.setUserData("outWidth://" + options.outWidth
                    + ",outHeight://" + options.outHeight + ",THUMBNAIL://"
                    + message.getMsgId() + ",PICGIF://" + imgInfo.isGif);
            long row = IMessageSqlManager.insertIMessage(message,
                    ECMessage.Direction.SEND.ordinal());

            if (row != -1) {
                return ImgInfoSqlManager.getInstance().insertImageInfo(imgInfo);
            }
        }
        return -1;

    }

    public void getPersonInfo() {
        final ClientUser clientUser = CCPAppManager.getClientUser();
        if (clientUser == null) {
            return;
        }
        LogUtil.d(TAG, "[getPersonInfo] currentVersion :" + clientUser.getpVersion() + " ,ServerVersion: " + mServicePersonVersion);
        if (true) {
            ECDevice.getPersonInfo(clientUser.getUserId(), new ECDevice.OnGetPersonInfoListener() {
                @Override
                public void onGetPersonInfoComplete(ECError e, PersonInfo p) {
                    if (e.errorCode == SdkErrorCode.REQUEST_SUCCESS && p != null) {
                        clientUser.setpVersion(p.getVersion());
                        clientUser.setSex(p.getSex().ordinal());
                        clientUser.setUserName(p.getNickName());
                        clientUser.setSignature(p.getSign());

                        ECApplication.nick = p.getNickName();
                        ECApplication.sign = p.getSign();
                        LogUtil.e("persion--" + p.getNickName());
                        LogUtil.e("persion--" + p.getSign());
                        if (!TextUtils.isEmpty(p.getBirth())) {
                            clientUser.setBirth(p.getBirth());
//									clientUser.setBirth(DateUtil.getActiveTimelong(p.getBirth()));
                        } else {
                            clientUser.setBirth("1989-01-21");
                        }


                        String newVersion = clientUser.toString();
                        LogUtil.d(TAG, "[getPersonInfo -result] ClientUser :" + newVersion);
                        try {
                            ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, newVersion, true);
                            CCPAppManager.setClientUser(clientUser);
                        } catch (InvalidClassException e1) {
                            e1.printStackTrace();
                        }
                    }

                }
            });
        }
    }

    private class ChatManagerListener implements
            ECChatManager.OnSendMessageListener {

        @Override
        public void onSendMessageComplete(ECError error, ECMessage message) {
            if (message == null) {
                return;
            }
            // 处理ECMessage的发送状态
            if (message != null) {
                if (message.getType() == ECMessage.Type.VOICE) {
                    try {
                        DemoUtils.playNotifycationMusic(
                                CCPAppManager.getContext(),
                                "sound/voice_message_sent.mp3");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                IMessageSqlManager.setIMessageSendStatus(message.getMsgId(),
                        message.getMsgStatus().ordinal());
                IMessageSqlManager.notifyMsgChanged(message.getSessionId());
                if (mOnMessageReportCallback != null) {
                    mOnMessageReportCallback.onMessageReport(error, message);
                }
                return;
            }
        }

        @Override
        public void onProgress(String msgId, int total, int progress) {
            // 处理发送文件IM消息的时候进度回调
            LogUtil.d(TAG, "[IMChattingHelper - onProgress] msgId：" + msgId
                    + " ,total：" + total + " ,progress:" + progress);
        }

    }

    public static void setOnMessageReportCallback(
            OnMessageReportCallback callback) {
        getInstance().mOnMessageReportCallback = callback;
    }

    public interface OnMessageReportCallback {
        void onMessageReport(ECError error, ECMessage message);

        void onPushMessage(String sessionId, List<ECMessage> msgs);
    }

    public void handleSendRichTextMessage(RichTextBean bean, String to) {
        // 组建一个待发送的ECMessage
        ECMessage msg = ECMessage.createECMessage(ECMessage.Type.RICH_TEXT);
        // 设置消息接收者
        msg.setTo(to);
        // 创建一个文本消息体，并添加到消息对象中
        ECPreviewMessageBody msgBody = new ECPreviewMessageBody();
        msgBody.setTitle(bean.getTitle());
        msgBody.setDescContent(bean.getDesc());

        File file = new File(FileAccessor.IMESSAGE_RICH_TEXT + "/" + DemoUtils.md5(bean.getPicUrl()) + ".jpg");
        if (file != null && file.exists() && file.length() == 0) {
            String imagePath = FileAccessor.IMESSAGE_RICH_TEXT + "/" + DemoUtils.md5(BitmapUtil.ATTACT_ICON) + ".jpg";
            msgBody.setLocalUrl(imagePath);
        } else {
            msgBody.setLocalUrl(FileAccessor.IMESSAGE_RICH_TEXT + "/" + DemoUtils.md5(bean.getPicUrl()) + ".jpg");
        }
        msgBody.setUrl(bean.getUrl());
        msgBody.setRemoteUrl(bean.getPicUrl());
        msg.setBody(msgBody);
        try {
            // 发送消息，该函数见上
            long rowId = -1;
            rowId = sendECMessage(msg);
            // 通知列表刷新
            msg.setId(rowId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private int getMaxVersion() {
        int maxVersion = IMessageSqlManager.getMaxVersion();
        int maxVersion1 = GroupNoticeSqlManager.getMaxVersion();
        return maxVersion > maxVersion1 ? maxVersion : maxVersion1;
    }

    /**
     * 收到新的IM文本和附件消息
     */
    @Override
    public void OnReceivedMessage(ECMessage msg) {
        LogUtil.d(TAG, "[OnReceivedMessage] show notice true");
        if (msg == null) {
            return;
        }
        postReceiveMessage(msg, !msg.isNotify());

//        if(msg.getType()==Type.TXT){
//             String[] arr =   ((ECTextMessageBody)msg.getBody()).getRelateArray();
//             LogUtil.e("aaaaaaaaa","aa====="+arr[0]+"--"+arr[1]);
//        }

    }

    private void handlerPushMsg(ECMessage msg) {
        DemoGroupNotice demoGroupNotice = new DemoGroupNotice();
        ECTextMessageBody body = (ECTextMessageBody) msg.getBody();
        demoGroupNotice.setContent(body.getMessage());

        demoGroupNotice.setAdmin(msg.getForm());
        demoGroupNotice.setNickName(msg.getNickName());
        demoGroupNotice.setSender(msg.getForm());

        GroupNoticeSqlManager.insertNoticeMsgForSystem(demoGroupNotice);

    }

    private boolean isGroup(ECMessage msg) {
        if (msg == null || TextUtils.isEmpty(msg.getTo())) {
            return false;
        }

        return msg.getTo().startsWith("g") || msg.getTo().startsWith("G");
    }

    /**
     * 处理接收消息
     *
     * @param msg
     * @param showNotice
     */
    private synchronized void postReceiveMessage(ECMessage msg,
                                                 boolean showNotice) {
        // 接收到的IM消息，根据IM消息类型做不同的处理
        // IM消息类型：ECMessage.Type

        if("~ytxfa".equalsIgnoreCase(msg.getForm())){
            msg.setDirection(Direction.SEND);
        }


        if (msg.getType() == Type.FRIEND) {//好友相关
            Intent intent = new Intent(SDKCoreHelper.ACTION_SDK_FRIENDNUM);
            CCPAppManager.getContext().sendBroadcast(intent);

            int num = (int) SharedPreferencesUtils.getParam(CCPAppManager.getContext(), SharedPreferencesUtils.FRIEND_NUM, -1);
            if (num > 0) {
                num++;
                SharedPreferencesUtils.setParam(CCPAppManager.getContext(), SharedPreferencesUtils.FRIEND_NUM, num);
            } else {
                SharedPreferencesUtils.setParam(CCPAppManager.getContext(), SharedPreferencesUtils.FRIEND_NUM, 1);
            }
            ECFriendMessageBody body = (ECFriendMessageBody) msg.getBody();

            if (body != null) {
                FriendInner inner = body.getInner();
                String url = inner.getAvatar();
                String userId = msg.getForm();
                String nick = msg.getNickName();
                String content = inner.getMsg();
                String to = msg.getTo();

                String markSelf = TextUtils.isEmpty(nick) ? to : nick;//用户昵称》账号

                String markSOther = TextUtils.isEmpty(nick) ? userId : nick;//用户昵称》账号


                ECFriendMessageBody.FriendNoticeType type = inner.getSubType();
                if (type == ECFriendMessageBody.FriendNoticeType.IS_FRIENDLY) {

                    if (userId.equalsIgnoreCase(CCPAppManager.getUserId())) {
                        ECNotificationManager.getInstance()
                                .showFriendNotification(
                                        CCPAppManager.getContext(), "您与" + markSelf + "已成为好友",
                                        "好友添加通知", null,
                                        msg.getType().ordinal());
                        FriendMessageSqlManager.insertFriendByUserId(to, markSelf, "1", url);


                        insertFriendToGroupNotice("好友添加通知", "您与" + markSelf + "已成为好友", to);
                    } else {
                        ECNotificationManager.getInstance()
                                .showFriendNotification(
                                        CCPAppManager.getContext(), "您与" + markSOther + "已成为好友",
                                        "好友添加通知", null,
                                        msg.getType().ordinal());
                        FriendMessageSqlManager.insertFriendByUserId(userId, markSOther, "1", url);

                        insertFriendToGroupNotice("好友添加通知", "您与" + markSOther + "已成为好友", userId);
                    }

                } else if (type == ECFriendMessageBody.FriendNoticeType.DELETE_FRIEND) {
                    if (userId.equalsIgnoreCase(CCPAppManager.getUserId())) {

                        ECNotificationManager.getInstance()
                                .showFriendNotification(
                                        CCPAppManager.getContext(), "您已与" + markSelf + "解除好友关系",
                                        "好友删除通知", null,
                                        msg.getType().ordinal());

                        FriendMessageSqlManager.delFriend(to);

                        insertFriendToGroupNotice("好友删除通知", "您已与" + markSelf + "解除好友关系", to);
                    } else {
                        FriendMessageSqlManager.delFriend(userId);
                        ECNotificationManager.getInstance()
                                .showFriendNotification(
                                        CCPAppManager.getContext(), "您已与" + markSOther + "解除好友关系",
                                        "好友删除通知", null,
                                        msg.getType().ordinal());

                        insertFriendToGroupNotice("好友删除通知", "您已与" + markSOther + "解除好友关系", userId);
                    }
                } else if (type == ECFriendMessageBody.FriendNoticeType.ADD_FRIEND) {

                    if (!CCPAppManager.getUserId().equalsIgnoreCase(userId)) {
                        ECNotificationManager.getInstance()
                                .showFriendNotification(
                                        CCPAppManager.getContext(), markSOther + "请求添加您为好友",
                                        "好友添加通知", null,
                                        msg.getType().ordinal());
                        FriendMessageSqlManager.insertFriendByUserId(userId, markSOther, "0", url);

                        insertFriendToGroupNotice("好友添加通知", markSOther + "请求添加您为好友", userId);
                    }
                } else if (type == ECFriendMessageBody.FriendNoticeType.AGREE_ADD_FRIEND) {

                    if (!CCPAppManager.getUserId().equalsIgnoreCase(userId)) {
                        FriendMessageSqlManager.insertFriendByUserId(userId, nick, "1", url);
                        ECNotificationManager.getInstance()
                                .showFriendNotification(
                                        CCPAppManager.getContext(), "您与" + markSOther + "成为好友",
                                        "好友同意通知", null,
                                        msg.getType().ordinal());

                        insertFriendToGroupNotice("好友同意通知", "您与" + markSOther + "成为好友", userId);

                    } else {
                        ECNotificationManager.getInstance()
                                .showFriendNotification(
                                        CCPAppManager.getContext(), "您与" + markSelf + "成为好友",
                                        "好友同意通知", null,
                                        msg.getType().ordinal());

                        insertFriendToGroupNotice("好友同意通知", "您与" + markSelf + "成为好友", to);
                    }
                }
            }

            return;
        }

        if (msg.getType() == Type.STATE) { //状态消息
            String msgTo = msg.getTo();
            if (isGroup(msg)) {
                return;
            }
            ECUserStateMessageBody stateBody = (ECUserStateMessageBody) msg.getBody();
            String state = stateBody.getMessage();
            Intent intent = new Intent();
            intent.putExtra(USER_STATE, state);
            intent.setAction(INTENT_ACTION_CHAT_USER_STATE);
            CCPAppManager.getContext().sendBroadcast(intent);

            return;
        }
        if (msg.isMultimediaBody()) {
            {
                if (msg.getType() != Type.CALL) {
                    ECFileMessageBody body = (ECFileMessageBody) msg.getBody();
                    FileAccessor.initFileAccess();
                    if (!TextUtils.isEmpty(body.getRemoteUrl())) {
                        boolean thumbnail = false;
                        String fileExt = DemoUtils
                                .getExtensionName(body.getRemoteUrl());
                        if (msg.getType() == ECMessage.Type.VOICE) {
                            body.setLocalUrl(new File(FileAccessor.getVoicePathName(),
                                    DemoUtils.md5(String.valueOf(System
                                            .currentTimeMillis())) + ".amr")
                                    .getAbsolutePath());
                        } else if (msg.getType() == ECMessage.Type.IMAGE) {
                            ECImageMessageBody imageBody = (ECImageMessageBody) body;

                            Log.e("HD----", imageBody.gethDImageURL());
                            Log.e("HDs----", imageBody.isHPicture() + "");
                            thumbnail = !TextUtils.isEmpty(imageBody
                                    .getThumbnailFileUrl());
                            imageBody.setLocalUrl(new File(FileAccessor
                                    .getImagePathName(), DemoUtils
                                    .md5(thumbnail ? imageBody.getThumbnailFileUrl()
                                            : imageBody.getRemoteUrl())
                                    + "." + fileExt).getAbsolutePath());
                        } else if (msg.getType() == Type.RICH_TEXT) {

                            ECPreviewMessageBody previewMessageBody = (ECPreviewMessageBody) body;
                            thumbnail = !TextUtils.isEmpty(previewMessageBody
                                    .getThumbnailFileUrl());

                            previewMessageBody.setLocalUrl(FileAccessor.IMESSAGE_RICH_TEXT + "/" + previewMessageBody.getFileName());


                        } else {
                            if (msg.getBody() instanceof ECVideoMessageBody) {
                                ECVideoMessageBody videoBody = (ECVideoMessageBody) body;

                                thumbnail = !TextUtils.isEmpty(videoBody
                                        .getThumbnailUrl());
                                StringBuilder builder = new StringBuilder(
                                        videoBody.getFileName());
                                builder.append("_thum.png");
                                body.setLocalUrl(new File(FileAccessor
                                        .getFilePathName(), builder.toString())
                                        .getAbsolutePath());

                            } else {
                                body.setLocalUrl(new File(FileAccessor
                                        .getFilePathName(), DemoUtils.md5(String
                                        .valueOf(System.currentTimeMillis()))
                                        + "."
                                        + fileExt).getAbsolutePath());
                            }
                        }
                        if (syncMessage != null) {
                            syncMessage.put(msg.getMsgId(), new SyncMsgEntry(
                                    showNotice, thumbnail, msg));
                        }
                        if (mChatManager != null) {
                            if (thumbnail) {
                                mChatManager.downloadThumbnailMessage(msg, this);
                            } else {
                                mChatManager.downloadMediaMessage(msg, this);
                            }
                        }
                        if (TextUtils.isEmpty(body.getFileName())
                                && !TextUtils.isEmpty(body.getRemoteUrl())) {
                            body.setFileName(FileAccessor.getFileName(body
                                    .getRemoteUrl()));
                        }
                        if (msg.getType() == Type.IMAGE
                                && msg.getDirection() == Direction.RECEIVE) {
                            msg.setUserData(msg.getUserData());
                        } else {
                            msg.setUserData("fileName=" + body.getFileName());
                        }
                    }
                    if (IMessageSqlManager.insertIMessage(msg, msg.getDirection()
                            .ordinal()) > 0) {
                        return;

                    }
                } else {
                    LogUtil.e(TAG, "ECMessage fileUrl: null");
                }
            }
        } else {
            if (msg.getType() == Type.TXT && msg.getSessionId().toUpperCase().startsWith("G")) {//群组
                ECTextMessageBody body = (ECTextMessageBody) msg.getBody();
                if (body != null && body.isAt()) {//@消息
                    try {
                        ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_AT, msg.getSessionId()+CCPAppManager.getUserId(), true);
                    } catch (InvalidClassException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (IMessageSqlManager
                .insertIMessage(msg, msg.getDirection().ordinal()) <= 0) {
            return;
        }

        if (mOnMessageReportCallback != null) {
            ArrayList<ECMessage> msgs = new ArrayList<ECMessage>();
            msgs.add(msg);
            mOnMessageReportCallback.onPushMessage(msg.getSessionId(), msgs);
        }

        // 是否状态栏提示
        if (showNotice) {
            showNotification(msg);
        }
    }

    private void insertFriendToGroupNotice(String nameTitle, String content, String member) {
        DemoGroupNotice demoGroupNotice = new DemoGroupNotice();
        demoGroupNotice.setGroupName(nameTitle);
        demoGroupNotice.setContent(content);
        demoGroupNotice.setMember(member);
        GroupNoticeSqlManager.insertNoticeMsg(demoGroupNotice, member);
    }

    private static void showNotification(ECMessage msg) {
        if (checkNeedNotification(msg.getSessionId())) {
            ECNotificationManager.getInstance().forceCancelNotification();
            String lastMsg = "";
            if (msg.getType() == ECMessage.Type.TXT) {
                lastMsg = ((ECTextMessageBody) msg.getBody()).getMessage();
            }
            ECContacts contact = ContactSqlManager.getContact(msg.getForm());
            if (contact == null) {
                return;
            }
            ECNotificationManager.getInstance()
                    .showCustomNewMessageNotification(
                            CCPAppManager.getContext(), lastMsg,
                            contact.getNickname(), msg.getSessionId(),
                            msg.getType().ordinal());
        }
    }

    public static void checkDownFailMsg() {
        getInstance().postCheckDownFailMsg();
    }

    private void postCheckDownFailMsg() {
        List<ECMessage> downdFailMsg = IMessageSqlManager.getDowndFailMsg();
        if (downdFailMsg == null || downdFailMsg.isEmpty()) {
            return;
        }
        for (ECMessage msg : downdFailMsg) {
            ECImageMessageBody body = (ECImageMessageBody) msg.getBody();
            body.setThumbnailFileUrl(body.getRemoteUrl() + "_thum");
            if (syncMessage != null) {
                syncMessage.put(msg.getMsgId(), new SyncMsgEntry(false, true,
                        msg));
            }
            if (mChatManager != null) {
                mChatManager.downloadThumbnailMessage(msg, this);
            }
        }

    }

    /**
     * 是否需要状态栏通知
     *
     * @param contactId
     */
    public static boolean checkNeedNotification(String contactId) {
        String currentChattingContactId = ECPreferences
                .getSharedPreferences()
                .getString(
                        ECPreferenceSettings.SETTING_CHATTING_CONTACTID.getId(),
                        (String) ECPreferenceSettings.SETTING_CHATTING_CONTACTID
                                .getDefaultValue());
        if (contactId == null) {
            return true;
        }
        // 当前聊天
        if (contactId.equals(currentChattingContactId)) {
            return false;
        }
        // 群组免打扰
        if (contactId.toUpperCase().startsWith("G")) {
            return GroupSqlManager.isGroupNotify(contactId);
        }
        return true;
    }

    @Override
    public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage notice) {
        if (notice == null) {
            return;
        }

        // 接收到的群组消息，根据群组消息类型做不同处理
        // 群组消息类型：ECGroupMessageType
        GroupNoticeHelper.insertNoticeMessage(notice,
                new GroupNoticeHelper.OnPushGroupNoticeMessageListener() {

                    @Override
                    public void onPushGroupNoticeMessage(DemoGroupNotice system) {
                        IMessageSqlManager
                                .notifyMsgChanged(GroupNoticeSqlManager.CONTACT_ID);

                        ECMessage msg = ECMessage
                                .createECMessage(ECMessage.Type.TXT);
                        msg.setSessionId(system.getSender());
                        msg.setFrom(system.getSender());
                        ECTextMessageBody tx = new ECTextMessageBody(system
                                .getContent());
                        msg.setBody(tx);
                        // 是否状态栏提示
                        showNotification(msg);
                    }
                });

    }

    private int mHistoryMsgCount = 0;

    @Override
    public void onOfflineMessageCount(int count) {
        mHistoryMsgCount = count;
    }

    @Override
    public int onGetOfflineMessage() {
        // 获取全部的离线历史消息
        return ECDevice.SYNC_OFFLINE_MSG_ALL;
    }

    private ECMessage mOfflineMsg = null;

    @Override
    public void onReceiveOfflineMessage(List<ECMessage> msgs) {
        // 离线消息的处理可以参考 void OnReceivedMessage(ECMessage msg)方法
        // 处理逻辑完全一样
        // 参考 IMChattingHelper.java
        LogUtil.d(TAG, "[onReceiveOfflineMessage] show notice false");
        if (msgs != null && !msgs.isEmpty() && !isFirstSync) {
            isFirstSync = true;
        }
        for (ECMessage msg : msgs) {
            mOfflineMsg = msg;
            postReceiveMessage(msg, false);
        }
    }

    @Override
    public void onReceiveOfflineMessageCompletion() {
        if (mOfflineMsg == null) {
            return;
        }
        // SDK离线消息拉取完成之后会通过该接口通知应用
        // 应用可以在此做类似于Loading框的关闭，Notification通知等等
        // 如果已经没有需要同步消息的请求时候，则状态栏开始提醒
        ECMessage lastECMessage = mOfflineMsg;
        try {
            if (lastECMessage != null && mHistoryMsgCount > 0 && isFirstSync) {
                showNotification(lastECMessage);
                // lastECMessage.setSessionId(lastECMessage.getTo().startsWith("G")?lastECMessage.getTo():lastECMessage.getForm());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isFirstSync = isSyncOffline = false;
        // 无需要同步的消息
        CCPAppManager.getContext().sendBroadcast(
                new Intent(INTENT_ACTION_SYNC_MESSAGE));
        mOfflineMsg = null;
    }

    public int mServicePersonVersion = 0;

    @Override
    public void onServicePersonVersion(int version) {
        mServicePersonVersion = version;
        CCPAppManager.setPversion(version);
        LogUtil.e(TAG, "onServicePersonVersion " + version);

        ECApplication.version = version;
    }

    /**
     * 客服消息
     *
     * @param msg
     */
    @Override
    public void onReceiveDeskMessage(ECMessage msg) {
        LogUtil.d(TAG, "[onReceiveDeskMessage] show notice true");
        OnReceivedMessage(msg);
    }


    @Override
    public void onSoftVersion(String version, int sUpdateMode) {



    }

    public static boolean isSyncOffline() {
        return getInstance().isSyncOffline;
    }

    /**
     * 下载
     */
    @Override
    public void onDownloadMessageComplete(ECError e, ECMessage message) {
        if (e.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
            if (message == null) {
                return;
            }
            // 处理发送文件IM消息的时候进度回调
            LogUtil.d(TAG,
                    "[onDownloadMessageComplete] msgId：" + message.getMsgId());
            postDowloadMessageResult(message);

            if (message.getType() == Type.VIDEO
                    && mOnMessageReportCallback != null
                    && message.getDirection() == Direction.RECEIVE
                    && mOnMessageReportCallback instanceof ChattingFragment) {

                ((ChattingFragment) mOnMessageReportCallback)
                        .dismissPostingDialog();
            }

        } else {
            // 重试下载3次
            SyncMsgEntry remove = syncMessage.remove(message.getMsgId());
            if (remove == null) {
                return;
            }
            LogUtil.d(TAG,
                    "[onDownloadMessageComplete] download fail , retry ："
                            + remove.retryCount);
            retryDownload(remove);
        }
    }

    @Override
    public void onProgress(String msgId, int totalByte, int progressByte) {
        // 处理发送文件IM消息的时候进度回调 //download
        LogUtil.d(TAG, "[IMChattingHelper - onProgress] msgId: " + msgId
                + " , totalByte: " + totalByte + " , progressByte:"
                + progressByte);

    }

    /**
     * 重试下载3次
     *
     * @param entry
     */
    private void retryDownload(SyncMsgEntry entry) {
        if (entry == null || entry.msg == null || entry.isRetryLimit()) {
            return;
        }
        entry.increase();
        // download ..
        if (mChatManager != null) {
            if (entry.thumbnail) {
                mChatManager.downloadThumbnailMessage(entry.msg, this);
            } else {
                mChatManager.downloadMediaMessage(entry.msg, this);
            }
        }
        syncMessage.put(entry.msg.getMsgId(), entry);
    }

    private synchronized void postDowloadMessageResult(ECMessage message) {
        if (message == null) {
            return;
        }
        if (message.getType() == ECMessage.Type.VOICE) {
            ECVoiceMessageBody voiceBody = (ECVoiceMessageBody) message
                    .getBody();
            voiceBody.setDuration(DemoUtils.calculateVoiceTime(voiceBody
                    .getLocalUrl()));
        } else if (message.getType() == ECMessage.Type.IMAGE) {
            ImgInfo thumbImgInfo = ImgInfoSqlManager.getInstance()
                    .getThumbImgInfo(message);
            if (thumbImgInfo == null) {
                return;
            }
            ImgInfoSqlManager.getInstance().insertImageInfo(thumbImgInfo);
            BitmapFactory.Options options = DemoUtils
                    .getBitmapOptions(new File(FileAccessor.getImagePathName(),
                            thumbImgInfo.getThumbImgPath()).getAbsolutePath());
            message.setUserData("outWidth://" + options.outWidth
                    + ",outHeight://" + options.outHeight + ",THUMBNAIL://"
                    + message.getMsgId() + ",PICGIF://" + thumbImgInfo.isGif);
        }
        if (IMessageSqlManager.updateIMessageDownload(message) <= 0) {
            return;
        }
        if (mOnMessageReportCallback != null) {
            mOnMessageReportCallback.onMessageReport(null, message);
        }
        boolean showNotice = true;
        SyncMsgEntry remove = syncMessage.remove(message.getMsgId());
        if (remove != null) {
            showNotice = remove.showNotice;
            if (mOnMessageReportCallback != null && remove.msg != null) {
                ArrayList<ECMessage> msgs = new ArrayList<ECMessage>();
                msgs.add(remove.msg);
                mOnMessageReportCallback.onPushMessage(
                        remove.msg.getSessionId(), msgs);
            }
        }
        if (showNotice) {
            showNotification(message);
        }
    }

    public class SyncMsgEntry {
        // 是否是第一次初始化同步消息
        boolean showNotice = false;
        boolean thumbnail = false;

        // 重试下载次数
        private int retryCount = 1;
        ECMessage msg;

        public SyncMsgEntry(boolean showNotice, boolean thumbnail,
                            ECMessage message) {
            this.showNotice = showNotice;
            this.msg = message;
            this.thumbnail = thumbnail;
        }

        public void increase() {
            retryCount++;
        }

        public boolean isRetryLimit() {
            return retryCount >= 3;
        }
    }

    @Override
    public void onReceiveMessageNotify(ECMessageNotify msg) {
        if (msg.getNotifyType() == NotifyType.DELETE) {
            ECMessageDeleteNotify deleteMsg = (ECMessageDeleteNotify) msg;
            IMessageSqlManager.updateMsgReadStatus(msg.getMsgId(), true);
            IMessageSqlManager.deleteLocalFileAfterFire(msg.getMsgId());
            if (mOnMessageReportCallback != null) {
                mOnMessageReportCallback.onMessageReport(null, null);
            }
        } else if (msg.getNotifyType() == NotifyType.REVOKE) {
            ECMessageRevokeNotify revokeNotify = (ECMessageRevokeNotify) msg;
            if (revokeNotify != null) {
                if (!revokeNotify.getRevoker().equalsIgnoreCase(CCPAppManager.getUserId())) {
                    IMessageSqlManager.insertSysMessage(revokeNotify.getRevoker() + "撤销了一条消息", revokeNotify.getSessionId());
                }
            }
            IMessageSqlManager.delSingleMsg(revokeNotify.getMsgId());
            if (mOnMessageReportCallback != null) {
                mOnMessageReportCallback.onMessageReport(null, null);
            }
        } else if (msg.getNotifyType() == NotifyType.READ) {
            ECMessageReadNotify readNotify = (ECMessageReadNotify) msg;
            if (readNotify.getSessionId().toLowerCase().startsWith("g")) {
            }
            if (readNotify.getSessionId().endsWith(CCPAppManager.getUserId())) {
            }
            IMessageSqlManager.updateMsgReadCount(readNotify.getMsgId());


            if (mOnMessageReportCallback != null) {
                mOnMessageReportCallback.onMessageReport(null, null);
            }
        }
        CCPAppManager.getContext().sendBroadcast(
                new Intent(IMessageSqlManager.ACTION_SESSION_DEL));

    }

}
