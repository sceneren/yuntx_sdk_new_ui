package com.yuntongxun.ecdemo.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMember;

import java.util.List;

/**
 * Created by zlk on 2017/8/1.
 */

public class InterphoneGVAdapter extends ArrayAdapter<ECInterPhoneMeetingMember> {
    Context mContext;
    List<ECInterPhoneMeetingMember> mInterMembers;

    public InterphoneGVAdapter(Context context, List<ECInterPhoneMeetingMember> mInterMembers) {
        super(context, 0);
        mContext = context;
        this.mInterMembers = mInterMembers;
        addAll(mInterMembers);
    }


    ViewHolder mViewHolder;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;

        if (convertView == null || convertView.getTag() == null) {
            view = View.inflate(mContext, R.layout.inter_gv_item, null);
            mViewHolder = new ViewHolder();
            mViewHolder.mAvatar = (TextView) view.findViewById(R.id.group_card_item_avatar_iv);
            mViewHolder.tvPhone = (TextView) view.findViewById(R.id.tv_phone);
            mViewHolder.ivJoinState = (ImageView) view.findViewById(R.id.iv_join_state);

            view.setTag(mViewHolder);
        } else {
            view = convertView;
            mViewHolder = (ViewHolder) view.getTag();
        }



        final ECInterPhoneMeetingMember item = getItem(position);
        if (item == null) {
            return view;
        }

        if (item.getOnline() == ECInterPhoneMeetingMember.Online.ONLINE){
            mViewHolder.ivJoinState.setBackgroundResource(R.drawable.green);
        }else{
            mViewHolder.ivJoinState.setBackgroundResource(R.drawable.gray);
        }

        AvatorUtil.getInstance().setAvatorPhoto(mViewHolder.mAvatar,R.drawable.memer_bg,(item.getMember()));
        mViewHolder.tvPhone.setText(AvatorUtil.getInstance().getMarkName(item.getMember()));
        mViewHolder.tvPhone.setTextColor(Color.parseColor("#666666"));
        return view;
    }


    class ViewHolder {
        /**
         * 头像
         */
        TextView mAvatar;

        TextView tvPhone;

       ImageView ivJoinState;
    }


}