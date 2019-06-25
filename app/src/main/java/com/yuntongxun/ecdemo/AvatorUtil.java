package com.yuntongxun.ecdemo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.CircleDrawable;
import com.yuntongxun.ecdemo.common.utils.ImageLoader;
import com.yuntongxun.ecdemo.pojo.Friend;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.storage.GroupMemberSqlManager;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;

import java.lang.ref.WeakReference;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;

/**
 * Created by smileklvens on 2017/10/19.
 * 快速设置头像和备注昵称
 */

public class AvatorUtil {
    private static final AvatorUtil ourInstance = new AvatorUtil();

    public static AvatorUtil getInstance() {
        return ourInstance;
    }

    private AvatorUtil() {
    }


    /**
     * 获取备注> 昵称 > 手机号
     *
     * @param phone
     * @return
     */
    public String getMarkName(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return "";
        }
        if (TextUtils.equals(CCPAppManager.getUserId(), phone)) {//登录者取app昵称
            return CCPAppManager.getClientUser().getUserName();
        } else {
            Friend friend = FriendMessageSqlManager.queryFriendById(phone);//friend
            if (friend != null) {
                String mark = friend.getRemarkName();//备注
                if (!TextUtils.isEmpty(mark)) {
                    return mark;
                } else {
                    String friendNickName = friend.getNickName();
                    if (!TextUtils.isEmpty(friendNickName)) {
                        return friendNickName;
                    } else {
                       return getContactNick(phone);
                    }
                }
            } else {
                return getContactNick(phone);
            }
        }
    }

    private String getContactNick(String phone) {
        ECContacts contact = ContactSqlManager.getContact(phone);//昵称
        if (contact == null) {
            return phone;
        } else {
            String nickname = contact.getNickname();
            if (TextUtils.isEmpty(nickname)) {
                return phone;
            } else {
                return nickname;
            }
        }
    }

    /**
     * 获取备注> 群昵称 > 手机号
     *
     * @return
     */
    public String getMarkNameByGroup(String groupId, String voipCount) {

        if (TextUtils.isEmpty(groupId)) {
            return "";
        }
        if (TextUtils.isEmpty(voipCount)) {
            return "";
        }
        String mark = FriendMessageSqlManager.queryMarkByID(voipCount);//备注
        if (!TextUtils.isEmpty(mark)) {
            return mark;
        } else {

            String groupName  =  GroupMemberSqlManager.getRemarkWithGroupId(groupId, voipCount);//群昵称

            if (!TextUtils.isEmpty(groupName)) {
                return groupName;
            } else {
                return getContactNick(voipCount);
            }
        }
    }


    public String getAvatorUrl(String phone) {
        String headUrl;
        if (TextUtils.isEmpty(phone)) {
            headUrl = "";
        }
        if (TextUtils.equals(CCPAppManager.getUserId(), phone)) {//登录者
            headUrl = ECApplication.photoUrl;
        } else {//好友表
            headUrl = FriendMessageSqlManager.queryURLByID(phone);
        }
        return headUrl;
    }


    public void setAvatorPhoto(TextView tv, @DrawableRes int resId, String phone) {
        if (tv == null) {
            return;
        } else {
            final WeakReference<TextView> weakReference = new WeakReference<TextView>(tv);
            String avatorUrl = getAvatorUrl(phone);
            if (TextUtils.isEmpty(avatorUrl)) {
                weakReference.get().setBackgroundResource(resId);
                weakReference.get().setText(getMarkName(phone));
            } else {
                Glide.with(ECApplication.getInstance())
                        .load(avatorUrl)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                weakReference.get().setBackground((new CircleDrawable(bitmap, Color.GRAY, 1)));
                                weakReference.get().setText("");
                            }
                        });
            }
        }
    }


    public void setAvatorPhoto(ImageView imageView, @DrawableRes int resId, String phone) {
        if (imageView == null) {
            return;
        } else {
            final WeakReference<ImageView> weakReference = new WeakReference<ImageView>(imageView);
            String avatorUrl = getAvatorUrl(phone);
            if (TextUtils.isEmpty(avatorUrl)) {
                weakReference.get().setBackgroundResource(resId);
            } else {
                ImageLoader.getInstance().displayCricleImage(context, avatorUrl, weakReference.get());
            }
        }
    }
}