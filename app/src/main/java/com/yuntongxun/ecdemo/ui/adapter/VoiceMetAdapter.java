package com.yuntongxun.ecdemo.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecsdk.meeting.ECVoiceMeetingMember;

import java.util.ArrayList;

/**
 * Created by smileklvens on 2017/8/22.
 */

public class VoiceMetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<ECVoiceMeetingMember> datas = new ArrayList<ECVoiceMeetingMember>();
    Context mContext;

    boolean isSelfMeeting;

    //适配器初始化
    public VoiceMetAdapter(Context context, boolean isSelfMeeting) {
        mContext = context;
        this.isSelfMeeting = isSelfMeeting;
    }


    public void setDatas(ArrayList<ECVoiceMeetingMember> datas) {
        this.datas.clear();
        if (isSelfMeeting) {//管理有相应权限
            ECVoiceMeetingMember add = new ECVoiceMeetingMember();
            add.setNumber("add@yuntongxun.com");
            //删除
            ECVoiceMeetingMember del = new ECVoiceMeetingMember();
            del.setNumber("del@yuntongxun.com");

//            ECVoiceMeetingMember mute = new ECVoiceMeetingMember();
//            mute.setNumber("mute@yuntongxun.com");

            this.datas.add(add);
            this.datas.add(del);
//            this.datas.add(mute);
        }


        this.datas.addAll(datas);
//        if (datas != null&&datas.size()!=0) {
//            for (ECMeetingMember member : datas) {
//                if (member instanceof ECVoiceMeetingMember) {
//                    if (member == null) {
//                        continue;
//                    }
//
//                    if (((ECVoiceMeetingMember) member).getNumber().equals(CCPAppManager.getUserId())) {
//                        this.datas.add(3, (ECVoiceMeetingMember) member);
//
//                    } else {
//                        this.datas.add((ECVoiceMeetingMember) member);
//                    }
//                }
//            }
//        }

        notifyDataSetChanged();

    }


    //适配器初始化
    public VoiceMetAdapter(Context context, ArrayList<ECVoiceMeetingMember> datas) {
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
        final ECVoiceMeetingMember ecVoiceMeetingMember = datas.get(position);

        final String number = ecVoiceMeetingMember.getNumber();
        if (ecVoiceMeetingMember == null || TextUtils.isEmpty(number)) {
            return;
        }
        ((MyViewHolder) holder).tvPhone.setTextColor(Color.parseColor("#768893"));

        if (ecVoiceMeetingMember.getNumber().equals("add@yuntongxun.com")) {
            ((MyViewHolder) holder).mAvatar.setBackgroundResource(R.drawable.voiceadd);
            ((MyViewHolder) holder).mAvatar.setText("");
            ((MyViewHolder) holder).tvPhone.setText("邀请人员");
        } else if (ecVoiceMeetingMember.getNumber().equals("del@yuntongxun.com")) {
            ((MyViewHolder) holder).mAvatar.setBackgroundResource(R.drawable.delete);
            ((MyViewHolder) holder).mAvatar.setText("");
            ((MyViewHolder) holder).tvPhone.setText("删除人员");
        } else if (ecVoiceMeetingMember.getNumber().equals("mute@yuntongxun.com")) {
            ((MyViewHolder) holder).mAvatar.setBackgroundResource(R.drawable.jingyin_workb);
            ((MyViewHolder) holder).mAvatar.setText("");
            ((MyViewHolder) holder).tvPhone.setText("全员静音");
        } else {
            AvatorUtil.getInstance().setAvatorPhoto(((MyViewHolder) holder).mAvatar, R.drawable.memer_bg, number);

            ((MyViewHolder) holder).tvPhone.setText(AvatorUtil.getInstance().getMarkName(number));

        }

        ((MyViewHolder) holder).ivJoinState.setVisibility(View.GONE);

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

        public MyViewHolder(View view) {
            super(view);
            mAvatar = (TextView) view.findViewById(R.id.group_card_item_avatar_iv);
            tvPhone = (TextView) view.findViewById(R.id.tv_phone);
            ivJoinState = (ImageView) view.findViewById(R.id.iv_join_state);
        }
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(int position, ECVoiceMeetingMember ecVoiceMeetingMember);
    }


}
