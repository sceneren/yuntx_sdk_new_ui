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
package com.yuntongxun.ecdemo.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.ResourceHelper;
import com.yuntongxun.ecdemo.storage.ConversationSqlManager;
import com.yuntongxun.ecdemo.storage.GroupNoticeSqlManager;
import com.yuntongxun.ecdemo.storage.GroupSqlManager;
import com.yuntongxun.ecdemo.ui.chatting.base.EmojiconTextView;
import com.yuntongxun.ecdemo.ui.chatting.model.Conversation;
import com.yuntongxun.ecdemo.ui.contact.ContactLogic;
import com.yuntongxun.ecdemo.ui.group.GroupNoticeHelper;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.im.ECGroup;


/**
 * @author 容联•云通讯
 * @version 4.0
 * @date 2014-12-8
 */
public class ConversationAdapter extends CCPListAdapter<Conversation> {

    private OnListAdapterCallBackListener mCallBackListener;
    int padding;
    private ColorStateList[] colorStateLists;
    private String isAtSession;
    private Context context;

    /**
     * @param ctx
     */
    public ConversationAdapter(Context ctx, OnListAdapterCallBackListener listener) {
        super(ctx, new Conversation());
        context = ctx;
        mCallBackListener = listener;
        padding = ctx.getResources().getDimensionPixelSize(R.dimen.OneDPPadding);
        colorStateLists = new ColorStateList[]{
                ResourceHelper.getColorStateList(ctx, R.color.normal_text_color),
                ResourceHelper.getColorStateList(ctx, R.color.ccp_list_textcolor_three)
        };
    }


    @Override
    protected Conversation getItem(Conversation t, Cursor cursor) {
        Conversation conversation = new Conversation();
        conversation.setCursor(cursor);

        return conversation;
    }

    /**
     * 会话时间
     *
     * @param conversation
     * @return
     */
    protected final CharSequence getConversationTime(Conversation conversation) {
        if (conversation.getSendStatus() == ECMessage.MessageStatus.SENDING.ordinal()) {
            return mContext.getString(R.string.conv_msg_sending);
        }
        if (conversation.getDateTime() <= 0) {
            return "";
        }
        return DateUtil.getDateString(conversation.getDateTime(),
                DateUtil.SHOW_TYPE_CALL_LOG).trim();
    }

    /**
     * 根据消息类型返回相应的主题描述
     *
     * @param conversation
     * @return
     */
    protected final CharSequence getConversationSnippet(Conversation conversation) {
        if (conversation == null) {
            return "";
        }
        if (GroupNoticeSqlManager.CONTACT_ID.equals(conversation.getSessionId())) {//通知
            return GroupNoticeHelper.getNoticeContent(conversation.getContent());
        }

        String fromNickName = "";
        if (isPeerChat(conversation.getSessionId())) {//群组
            if (conversation.getContactId() != null && CCPAppManager.getClientUser() != null
                    && !conversation.getContactId().equals(CCPAppManager.getClientUser().getUserId())) {
                fromNickName = AvatorUtil.getInstance().getMarkNameByGroup(conversation.getSessionId(),conversation.getContactId()) + ": ";
            }
        }
        // Android Demo 免打扰后需要显示未读条数

        boolean b = ConversationSqlManager.queryIsNoticeBySessionId(conversation.getSessionId());

        if (!b && conversation.getUnreadCount() > 1) {
            fromNickName = " [" + conversation.getUnreadCount() + "条]" + fromNickName;
        }

        if (conversation.getMsgType() == ECMessage.Type.VOICE.ordinal()) {
            return fromNickName + mContext.getString(R.string.app_voice);
        } else if (conversation.getMsgType() == ECMessage.Type.FILE.ordinal()) {
            return fromNickName + mContext.getString(R.string.app_file);
        } else if (conversation.getMsgType() == ECMessage.Type.IMAGE.ordinal()) {
            return fromNickName + mContext.getString(R.string.app_pic);
        } else if (conversation.getMsgType() == ECMessage.Type.VIDEO.ordinal()) {
            return fromNickName + mContext.getString(R.string.app_video);
        } else if (conversation.getMsgType() == ECMessage.Type.LOCATION.ordinal()) {
            return fromNickName + mContext.getString(R.string.app_location);

        }
        String snippet = fromNickName + conversation.getContent();
        if (TextUtils.equals(conversation.getSessionId() + CCPAppManager.getUserId(), isAtSession)) {//@
            return Html.fromHtml(mContext.getString(R.string.conversation_at, snippet));
        }
        return snippet;
    }

    /**
     * 根据消息发送状态处理
     *
     * @param context
     * @param conversation
     * @return
     */
    public static Drawable getChattingSnippentCompoundDrawables(Context context, Conversation conversation) {
        if (conversation.getSendStatus() == ECMessage.MessageStatus.FAILED.ordinal()) {
            return DemoUtils.getDrawables(context, R.drawable.msg_state_failed);
        } else if (conversation.getSendStatus() == ECMessage.MessageStatus.SENDING.ordinal()) {
            return DemoUtils.getDrawables(context, R.drawable.msg_state_sending);
        } else {
            return null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        ViewHolder mViewHolder;
        if (convertView == null || convertView.getTag() == null) {
            view = View.inflate(mContext, R.layout.conversation_item, null);

            mViewHolder = new ViewHolder();
            mViewHolder.user_avatar = (ImageView) view.findViewById(R.id.avatar_iv);
            mViewHolder.prospect_iv = (ImageView) view.findViewById(R.id.avatar_prospect_iv);
            mViewHolder.nickname_tv = (EmojiconTextView) view.findViewById(R.id.nickname_tv);
            mViewHolder.tipcnt_tv = (TextView) view.findViewById(R.id.tipcnt_tv);
            mViewHolder.update_time_tv = (TextView) view.findViewById(R.id.update_time_tv);
            mViewHolder.last_msg_tv = (EmojiconTextView) view.findViewById(R.id.last_msg_tv);
            mViewHolder.image_input_text = (ImageView) view.findViewById(R.id.image_input_text);
            mViewHolder.image_mute = (ImageView) view.findViewById(R.id.image_mute);
            view.setTag(mViewHolder);
        } else {
            view = convertView;
            mViewHolder = (ViewHolder) view.getTag();
        }

        Conversation conversation = getItem(position);
        if (conversation != null) {
            handleDisplayNameTextColor(mViewHolder.nickname_tv, conversation.getSessionId());

            CharSequence  t = getConversationSnippet(conversation);
            mViewHolder.last_msg_tv.setText(TextUtils.isEmpty(t)?"":t);
            if(t!=null&&"null".equalsIgnoreCase(t.toString())){
                mViewHolder.last_msg_tv.setText("");
            }
            mViewHolder.last_msg_tv.setCompoundDrawables(getChattingSnippentCompoundDrawables(mContext, conversation), null, null, null);
            // 未读提醒设置
            setConversationUnread(mViewHolder, conversation);

            mViewHolder.image_input_text.setVisibility(View.GONE);
            mViewHolder.update_time_tv.setText(getConversationTime(conversation));
            if (isPeerChat(conversation.getSessionId())) {//群组
                mViewHolder.user_avatar.setImageResource(R.drawable.message_icon_qunzu);
                ECGroup ecGroup = GroupSqlManager.getECGroup(conversation.getSessionId());
                if (ecGroup != null) {
                    mViewHolder.nickname_tv.setText(ecGroup.getName());
                } else {
                    mViewHolder.nickname_tv.setText(conversation.getSessionId());
                }

                boolean b = ConversationSqlManager.queryIsNoticeBySessionId(conversation.getSessionId());
                mViewHolder.image_mute.setVisibility(b ? View.VISIBLE : View.GONE);
                if(b){
//                    mViewHolder.tipcnt_tv.setVisibility(View.GONE);
//                    mViewHolder.prospect_iv.setVisibility(View.GONE);
                }

//                mViewHolder.image_mute.setVisibility(ecGroup.isNotice() ? View.GONE : View.VISIBLE);

            } else if (TextUtils.equals(conversation.getSessionId(), RestServerDefines.FILE_ASSISTANT)) {//文件助手

                mViewHolder.nickname_tv.setText("文件助手");
                mViewHolder.user_avatar.setImageResource(R.drawable.filea);
                mViewHolder.image_mute.setVisibility( View.GONE);

            } else if(conversation.getSessionId().startsWith("~ytxro")){
                mViewHolder.user_avatar.setImageResource(R.drawable.detail_robot);
                mViewHolder.nickname_tv.setText("智能机器人");
            }

            else {
                mViewHolder.user_avatar.setBackgroundDrawable(null);
                mViewHolder.nickname_tv.setText(AvatorUtil.getInstance().getMarkName(conversation.getSessionId()));

                if (conversation.getMsgType() == 1000) {//系统通知
                    mViewHolder.user_avatar.setImageResource(R.drawable.message_icon_tongzhi);
                    mViewHolder.image_mute.setVisibility(View.GONE);
                } else {//个人
                    AvatorUtil.getInstance().setAvatorPhoto(mViewHolder.user_avatar, R.drawable.message_icon_header, conversation.getSessionId());
                    boolean b = ConversationSqlManager.queryIsNoticeBySessionId(conversation.getSessionId());
                    mViewHolder.image_mute.setVisibility(b ? View.VISIBLE : View.GONE);
                    if(b){
//                        mViewHolder.tipcnt_tv.setVisibility(View.GONE);
//                        mViewHolder.prospect_iv.setVisibility(View.GONE);
                    }
                }
            }
        }

        boolean isTop = ConversationSqlManager.querySessionisTopBySessionId(conversation.getSessionId());
        if (isTop && !conversation.getSessionId().equals(GroupNoticeSqlManager.CONTACT_ID)) {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.list_bg_gaoliang));
        } else {
            view.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.list_item_selector));

        }

        return view;
    }

    private void handleDisplayNameTextColor(EmojiconTextView textView, String contactId) {
        if (ContactLogic.isCustomService(contactId)) {
            textView.setTextColor(colorStateLists[1]);
        } else {
            textView.setTextColor(colorStateLists[0]);
        }
    }

    /**
     * 设置未读图片显示规则
     *
     * @param mViewHolder
     * @param conversation
     */
    private void setConversationUnread(ViewHolder mViewHolder, Conversation conversation) {
        boolean b = ConversationSqlManager.queryIsNoticeBySessionId(conversation.getSessionId());

        String msgCount = conversation.getUnreadCount() > 100 ? "..." : String.valueOf(conversation.getUnreadCount());
        mViewHolder.tipcnt_tv.setText(msgCount);
        if (conversation.getUnreadCount() == 0) {
            mViewHolder.tipcnt_tv.setVisibility(View.GONE);
            mViewHolder.prospect_iv.setVisibility(View.GONE);
        } else if (conversation.isNotice()||b) {
            mViewHolder.tipcnt_tv.setVisibility(View.VISIBLE);
            mViewHolder.prospect_iv.setVisibility(View.GONE);
        } else {
            mViewHolder.prospect_iv.setVisibility(View.VISIBLE);
            mViewHolder.tipcnt_tv.setVisibility(View.GONE);
        }
        if(b){
            mViewHolder.tipcnt_tv.setVisibility(View.GONE);
            if(conversation.getUnreadCount()>0){
                mViewHolder.prospect_iv.setVisibility(View.VISIBLE);
            }
        }
    }


    static class ViewHolder {
        ImageView user_avatar;
        TextView tipcnt_tv;
        ImageView prospect_iv;
        EmojiconTextView nickname_tv;
        TextView update_time_tv;
        EmojiconTextView last_msg_tv;
        ImageView image_input_text;
        ImageView image_mute;
    }

    @Override
    protected void initCursor() {
        notifyChange();
    }

    @Override
    protected void notifyChange() {
        if (mCallBackListener != null) {
            mCallBackListener.OnListAdapterCallBack();
        }


        Cursor cursor = ConversationSqlManager.getConversationCursor();

        setCursor(cursor);
        isAtSession = ECPreferences.getSharedPreferences().getString(ECPreferenceSettings.SETTINGS_AT.getId(), "");
        super.notifyDataSetChanged();
    }

    /**
     * 是否群组
     *
     * @return
     */
    public boolean isPeerChat(String sessionId) {
        return sessionId != null && sessionId.toLowerCase().startsWith("g");
    }

}
