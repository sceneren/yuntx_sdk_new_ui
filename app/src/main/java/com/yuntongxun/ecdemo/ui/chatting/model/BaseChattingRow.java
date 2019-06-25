/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yuntongxun.ecdemo.ui.chatting.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.CircleDrawable;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.ui.chatting.ChatingDetailAct;
import com.yuntongxun.ecdemo.ui.chatting.ChattingActivity;
import com.yuntongxun.ecdemo.ui.chatting.holder.BaseHolder;
import com.yuntongxun.ecdemo.ui.chatting.holder.RedPacketAckViewHolder;
import com.yuntongxun.ecdemo.ui.contact.ContactDetailActivity;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.personcenter.FriendInfoUI;
import com.yuntongxun.ecdemo.ui.personcenter.PersonInfoUI;
import com.yuntongxun.ecsdk.ECMessage;


/**
 * 聊天页面的row基类，可自行扩展
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: Beijing Speedtong Information Technology Co.,Ltd</p>
 *
 * @author Jorstin Chan
 * @version 1.0
 * @date 2014-4-17
 */
public abstract class BaseChattingRow implements IChattingRow {

    public static final String TAG = LogUtil.getLogUtilsTag(BaseChattingRow.class);
    int mRowType;

    public BaseChattingRow(int type) {
        mRowType = type;
    }

    /**
     * 处理消息的发送状态设置
     *
     * @param position 消息的列表所在位置
     * @param holder   消息ViewHolder
     * @param l
     */
    protected static void getMsgStateResId(int position, BaseHolder holder, ECMessage msg, View.OnClickListener l) {
        if (msg != null && msg.getDirection() == ECMessage.Direction.SEND) {
            ECMessage.MessageStatus msgStatus = msg.getMsgStatus();
            if (msgStatus == ECMessage.MessageStatus.FAILED) {
                holder.getUploadState().setImageResource(R.drawable.msg_state_failed_resend);
                holder.getUploadState().setVisibility(View.VISIBLE);
                if (holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }
            } else if (msgStatus == ECMessage.MessageStatus.SUCCESS || msgStatus == ECMessage.MessageStatus.RECEIVE) {
                holder.getUploadState().setImageResource(0);
                holder.getUploadState().setVisibility(View.GONE);
                if (holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }

            } else if (msgStatus == ECMessage.MessageStatus.SENDING) {
                holder.getUploadState().setImageResource(0);
                holder.getUploadState().setVisibility(View.GONE);
                if (holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.VISIBLE);
                }

            } else {
                if (holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }
                LogUtil.d(TAG, "getMsgStateResId: not found this state");
            }

            ViewHolderTag holderTag = ViewHolderTag.createTag(msg, ViewHolderTag.TagType.TAG_RESEND_MSG, position);
            holder.getUploadState().setTag(holderTag);
            holder.getUploadState().setOnClickListener(l);
        }
    }


    /**
     * 设置超链接
     *
     * @param tv
     */
    public void setAutoLinkForTextView(TextView tv) {

        if (tv != null) {
            String text = tv.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                if (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("www.")) {
                    tv.setAutoLinkMask(Linkify.WEB_URLS);
                }
            }
        }
    }

    /**
     * @param contextMenu
     * @param targetView
     * @param detail
     * @return
     */
    public abstract boolean onCreateRowContextMenu(ContextMenu contextMenu, View targetView, ECMessage detail);


    /**
     * @param baseHolder
     * @param displayName
     */
    public static void setDisplayName(BaseHolder baseHolder, String displayName) {
        if (baseHolder == null || baseHolder.getChattingUser() == null) {
            return;
        }

        if (TextUtils.isEmpty(displayName)) {
            baseHolder.getChattingUser().setVisibility(View.GONE);
            return;
        }

        if (!(baseHolder instanceof RedPacketAckViewHolder)) {
            baseHolder.getChattingUser().setText(displayName);
            baseHolder.getChattingUser().setVisibility(View.VISIBLE);
        }
    }

    protected abstract void buildChattingData(Context context, BaseHolder baseHolder, ECMessage detail, int position);

    /**
     * 封装基础的 头像，名字等展示和点击，抽象buildChattingData待子类实现
     *
     * @param context
     * @param baseHolder
     * @param detail
     * @param position
     */
    @Override
    public void buildChattingBaseData(Context context, BaseHolder baseHolder, ECMessage detail, int position) {

        // 处理其他使用逻辑
        buildChattingData(context, baseHolder, detail, position);

        if (((ChattingActivity) context).isPeerChat()
                && detail.getDirection() == ECMessage.Direction.RECEIVE) {//群主接收方展示群昵称
            setDisplayName(baseHolder, getGroupNickName(detail));
        }

        setContactPhoto(context, baseHolder, detail);

        setContactPhotoClickListener(context, baseHolder, detail);
    }

    /**
     * 头像点击事件
     *
     * @param context
     * @param baseHolder
     * @param detail
     */
    private void setContactPhotoClickListener(final Context context, BaseHolder baseHolder, final ECMessage detail) {
        if (baseHolder.getChattingAvatar() != null && detail != null) {
            baseHolder.getChattingAvatar().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if ("~ytxfa".equalsIgnoreCase(detail.getForm())||TextUtils.equals(CCPAppManager.getUserId(), detail.getForm())) {//自己跳个人中心
                        Intent intent = new Intent(context, PersonInfoUI.class);

//                        intent.putExtra(ContactDetailActivity.MOBILE, contact.getContactid());
//                        intent.putExtra(ContactDetailActivity.DISPLAY_NAME, TextUtils.isEmpty(contact.getNickname()) ? contact.getContactid() : contact.getNickname());
//                        intent.putExtra(ContactDetailActivity.RAW_ID, contact.getId());
                        context.startActivity(intent);

                    } else if(detail.getForm().startsWith("~ytxro")){
                        Intent intent = new Intent(context, ChatingDetailAct.class);
                        intent.putExtra("extra_sessionid",detail.getForm());
                        context.startActivity(intent);
                    }
                    else {
                        ECContacts contact = ContactSqlManager.getContact(detail.getForm());
                        if (contact == null || contact.getId() == -1) {
                            return;
                        }
                        Intent intent = new Intent(context, FriendInfoUI.class);

                        intent.putExtra(ContactDetailActivity.MOBILE, contact.getContactid());
                        intent.putExtra(ContactDetailActivity.DISPLAY_NAME, TextUtils.isEmpty(contact.getNickname()) ? contact.getContactid() : contact.getNickname());
                        intent.putExtra(ContactDetailActivity.RAW_ID, contact.getId());
                        context.startActivity(intent);
                    }
                }
            });

            baseHolder.getChattingAvatar().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (context instanceof ChattingActivity) {
                        final ChattingActivity activity = (ChattingActivity) context;
                        if (activity.isPeerChat() && !activity.mChattingFragment.mAtsomeone) {
                            activity.mChattingFragment.mAtsomeone = true;
                            // 群组
                            ECContacts contact = ContactSqlManager.getContact(detail.getForm());
                            if (contact != null) {
                                if (TextUtils.isEmpty(contact.getNickname())) {
                                    contact.setNickname(contact.getContactid());
                                }
                                activity.mChattingFragment.getChattingFooter().setLastText(activity.mChattingFragment.getChattingFooter().getLastText() + "@" + contact.getNickname() + (char) (8197));
                                activity.mChattingFragment.getChattingFooter().putSomebody(contact);
                                activity.mChattingFragment.getChattingFooter().setMode(1);
                                v.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.mChattingFragment.mAtsomeone = false;
                                    }
                                }, 2000L);
                            }
                        }
                    }

                    return true;
                }
            });
        }
    }


    /**
     * 设置头像昵称等
     */
    private void setContactPhoto(Context context, final BaseHolder baseHolder, ECMessage detail) {

        if (baseHolder.getChattingAvatar() != null && detail != null) {
            String receiveAvatorUrl = AvatorUtil.getInstance().getAvatorUrl(detail.getForm());
            String sendAvatorUrl = ECApplication.photoUrl;
            String markName = AvatorUtil.getInstance().getMarkName(detail.getForm());
            String groupNickName = getGroupNickName(detail);
            boolean isGroup = ((ChattingActivity) context).isPeerChat();

            if (detail.getDirection() == ECMessage.Direction.SEND) {//发送方
                if (!TextUtils.isEmpty(sendAvatorUrl)) {
                    displayBg(baseHolder, sendAvatorUrl);
                } else {
                    if (isGroup) {//群
                        baseHolder.getChattingAvatar().setText(groupNickName);
                    } else {
                        baseHolder.getChattingAvatar().setText(markName);
                    }
                    baseHolder.getChattingAvatar().setBackgroundResource(R.drawable.bule_circle_bg);
                }


            } else {//接收方

                if (!TextUtils.isEmpty(receiveAvatorUrl)) {
                    displayBg(baseHolder, receiveAvatorUrl);
                } else {
                    if (isGroup) {//群
                        baseHolder.getChattingAvatar().setText(groupNickName);
                    } else {
                        baseHolder.getChattingAvatar().setText(markName);
                    }
                    baseHolder.getChattingAvatar().setBackgroundResource(R.drawable.bule_circle_bg);
                }
                if(detail.getForm().startsWith("~ytxro")){
                    baseHolder.getChattingAvatar().setBackgroundResource(R.drawable.detail_robot);
                    baseHolder.getChattingAvatar().setText("");
                }
            }
        }
    }

    private void displayBg(final BaseHolder baseHolder, String url) {
        Glide.with(CCPAppManager.getContext())
                .load(url)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        baseHolder.getChattingAvatar().setBackground((new CircleDrawable(bitmap, Color.GRAY, 1)));
                        baseHolder.getChattingAvatar().setText("");
                    }
                });
    }


    /**
     * 获取群昵称
     *
     * @param detail
     * @return
     */
    public String getGroupNickName(ECMessage detail) {
        if (detail == null) {
            return "";
        }
        return AvatorUtil.getInstance().getMarkNameByGroup(detail.getSessionId(), detail.getForm());

    }


}
