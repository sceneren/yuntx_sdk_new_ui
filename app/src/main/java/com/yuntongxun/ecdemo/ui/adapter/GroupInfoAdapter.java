package com.yuntongxun.ecdemo.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.ImageLoader;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupMember;

import java.util.List;

/**
 * Created by zlk on 2017/8/1.
 */

public class GroupInfoAdapter extends ArrayAdapter<ECGroupMember> {
    Context mContext;

    private boolean isLocalDiscussion;
    private ECGroup mGroup;

    boolean isManager = false;


    public GroupInfoAdapter(Context context, boolean isLocalDiscussion, ECGroup mGroup) {
        super(context, 0);
        mContext = context;
        this.isLocalDiscussion = isLocalDiscussion;
        this.mGroup = mGroup;
//        screenWidth = ScreenUtils.getScreenWidth(context);
    }

    public synchronized void setData(List<ECGroupMember> data) {
        clear();
        if (data != null) {
            for (ECGroupMember appEntry : data) {

                if (CCPAppManager.getUserId().equals(appEntry.getVoipAccount())) {
                    isManager = (appEntry.getMemberRole() == ECGroupMember.Role.OWNER
                            || appEntry.getMemberRole() == ECGroupMember.Role.MANAGER);
                }
                add(appEntry);
            }
        }

//        // 是否是群主/或者有修改权限
//        boolean changePermission = isManager || isLocalDiscussion;

        if (isManager) {
            //新增
            ECGroupMember add = new ECGroupMember();
            add.setVoipAccount("add@yuntongxun.com");
            //删除
            ECGroupMember del = new ECGroupMember();
            del.setVoipAccount("del@yuntongxun.com");
            add(add);
            add(del);
        } else if (!isManager && isLocalDiscussion) {
            //新增
            ECGroupMember add = new ECGroupMember();
            add.setVoipAccount("add@yuntongxun.com");
            add(add);
        }
        notifyDataSetChanged();
    }

    ViewHolder mViewHolder;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;

        if (convertView == null || convertView.getTag() == null) {
            if (!isLocalDiscussion) {
                view = View.inflate(mContext, R.layout.group_member_item, null);
            } else {
                view = View.inflate(mContext, R.layout.group_member_item, null);
            }
            mViewHolder = new ViewHolder();
            mViewHolder.mAvatar = (TextView) view.findViewById(R.id.group_card_item_avatar_iv);
            mViewHolder.mivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);


            view.setTag(mViewHolder);
        } else {
            view = convertView;
            mViewHolder = (ViewHolder) view.getTag();
        }

        final ECGroupMember item = getItem(position);
        if (item == null) {
            return view;
        }
        if (item.getVoipAccount().equals("add@yuntongxun.com")) {
            mViewHolder.mAvatar.setVisibility(View.VISIBLE);
            mViewHolder.mAvatar.setBackgroundResource(R.drawable.qunzusetting_icon_add);
            mViewHolder.mivAvatar.setVisibility(View.GONE);
        } else if (item.getVoipAccount().equals("del@yuntongxun.com")) {
            mViewHolder.mAvatar.setVisibility(View.VISIBLE);
            mViewHolder.mAvatar.setBackgroundResource(R.drawable.qunzusetting_icon_delete);
            mViewHolder.mivAvatar.setVisibility(View.GONE);

        } else {
            final String headUrl = FriendMessageSqlManager.queryURLByID(item.getVoipAccount());
            if (!TextUtils.isEmpty(headUrl)) {
                mViewHolder.mAvatar.setVisibility(View.GONE);
                mViewHolder.mivAvatar.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayCricleImage(mContext,headUrl,mViewHolder.mivAvatar);
            } else {
                if (CCPAppManager.getUserId().equals(item.getVoipAccount())&&!TextUtils.isEmpty(ECApplication.photoUrl)) {
                    mViewHolder.mAvatar.setVisibility(View.GONE);
                    mViewHolder.mivAvatar.setVisibility(View.VISIBLE);
                    ImageLoader.getInstance().displayCricleImage(mContext,ECApplication.photoUrl,  mViewHolder.mivAvatar);
                }else{
                    mViewHolder.mAvatar.setVisibility(View.VISIBLE);
                    mViewHolder.mivAvatar.setVisibility(View.GONE);
                    mViewHolder.mAvatar.setBackgroundResource(R.drawable.memer_bg);
                    mViewHolder.mAvatar.setText(TextUtils.isEmpty(item.getDisplayName()) ?
                            item.getVoipAccount() : item.getDisplayName());
                }
            }
        }
        return view;
    }


    class ViewHolder {
        /**
         * 头像
         */
        TextView mAvatar;


        ImageView mivAvatar;
    }

}