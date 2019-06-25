package com.yuntongxun.ecdemo.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.ui.videomeeting.MultiVideoMember;

import java.util.ArrayList;

/**
 * Created by smileklvens on 2017/8/22.
 */

public class VideoMetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<MultiVideoMember> datas = new ArrayList<MultiVideoMember>();
    Context mContext;

    boolean isSelfMeeting;

    //适配器初始化
    public VideoMetAdapter(Context context) {
        mContext = context;
    }


    public synchronized void setDatas(ArrayList<MultiVideoMember> datas, boolean isSelfMeeting) {
        this.isSelfMeeting = isSelfMeeting;
        this.datas.clear();
        if (isSelfMeeting) {//管理有相应权限
            MultiVideoMember add = new MultiVideoMember();
            add.setNumber("add@yuntongxun.com");
            //删除
            MultiVideoMember del = new MultiVideoMember();
            del.setNumber("del@yuntongxun.com");

//            MultiVideoMember mute = new MultiVideoMember();
//            mute.setNumber("mute@yuntongxun.com");

            this.datas.add(add);
            this.datas.add(del);
//            this.datas.add(mute);
        }
        this.datas.addAll(datas);
        notifyDataSetChanged();

    }


    //适配器初始化
    public VideoMetAdapter(Context context, ArrayList<MultiVideoMember> datas) {
        mContext = context;
        this.datas = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext
        ).inflate(R.layout.inter_gv_item, parent,
                false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final MultiVideoMember ecVoiceMeetingMember = datas.get(position);
        if (ecVoiceMeetingMember == null) {
            return;
        }

        ((MyViewHolder) holder).linearLayout.setPadding(15, 0, 15, 0);
        if (ecVoiceMeetingMember.isPublish()) {
            ((MyViewHolder) holder).ivJoinState.setBackgroundResource(R.drawable.opencamera);
        } else {
            ((MyViewHolder) holder).ivJoinState.setVisibility(View.GONE);
        }
        if (ecVoiceMeetingMember.getNumber().equals("add@yuntongxun.com")) {
            ((MyViewHolder) holder).mAvatar.setBackgroundResource(R.drawable.video_add);
            ((MyViewHolder) holder).tvPhone.setText("邀请人员");

            ((MyViewHolder) holder).ivJoinState.setVisibility(View.GONE);
        } else if (ecVoiceMeetingMember.getNumber().equals("del@yuntongxun.com")) {
            ((MyViewHolder) holder).mAvatar.setBackgroundResource(R.drawable.video_delete);
            ((MyViewHolder) holder).tvPhone.setText("删除人员");
            ((MyViewHolder) holder).ivJoinState.setVisibility(View.GONE);
        } else if (ecVoiceMeetingMember.getNumber().equals("mute@yuntongxun.com")) {
            ((MyViewHolder) holder).mAvatar.setBackgroundResource(R.drawable.e_quanyuanjingyin);
            ((MyViewHolder) holder).tvPhone.setText("全员静音");
            ((MyViewHolder) holder).ivJoinState.setVisibility(View.GONE);
        } else {
            AvatorUtil.getInstance().setAvatorPhoto(((MyViewHolder) holder).mAvatar, R.drawable.memer_bg
                    , ecVoiceMeetingMember.getNumber());
            ((MyViewHolder) holder).tvPhone.setText(AvatorUtil.getInstance().getMarkName(ecVoiceMeetingMember.getNumber()));
            ((MyViewHolder) holder).ivJoinState.setVisibility(View.VISIBLE);
        }

        ((MyViewHolder) holder).mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onItemClick(position, ecVoiceMeetingMember);
                }
            }
        });


    }

    @Override
    public int getItemCount() {

        return datas == null ? 0 : datas.size();
    }


    //自定义ViewHolder，用于显示页数
    class MyViewHolder extends RecyclerView.ViewHolder {
        /**
         * 头像
         */
        TextView mAvatar;

        TextView tvPhone;

        ImageView ivJoinState;

        LinearLayout linearLayout;

        public MyViewHolder(View view) {
            super(view);
            mAvatar = (TextView) view.findViewById(R.id.group_card_item_avatar_iv);
            tvPhone = (TextView) view.findViewById(R.id.tv_phone);
            ivJoinState = (ImageView) view.findViewById(R.id.iv_join_state);

            linearLayout = (LinearLayout) view.findViewById(R.id.contact_item_ll);

        }
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(int position, MultiVideoMember ecVideoMeetingMember);
    }


}
