package com.yuntongxun.ecdemo.ui.livechatroom;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecsdk.ECLiveChatRoomMember;

import java.util.List;


public class RoomMemberAdapter extends RecyclerView.Adapter {
    public static interface OnRecyclerViewListener{
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener){
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    private static final String TAG = RoomMemberAdapter.class.getSimpleName();
    private List<ECLiveChatRoomMember> list;

    public RoomMemberAdapter(List<ECLiveChatRoomMember> list){
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.live_chat_h_item, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);



        view.setLayoutParams(lp);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i){
        PersonViewHolder holder = (PersonViewHolder) viewHolder;
        holder.position = i;
        ECLiveChatRoomMember person = list.get(i);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public View rootView;
        public ImageView nameTv;
        public TextView ageTv;
        public int position;

        public PersonViewHolder(View itemView){
            super(itemView);
            nameTv = (ImageView) itemView.findViewById(R.id.live_h_im);
            ageTv = (TextView) itemView.findViewById(R.id.live_h_tv);
            nameTv.setImageResource(DemoUtils.getRandRes(RestServerDefines.arr));
            rootView = itemView.findViewById(R.id.live_parent);
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (null != onRecyclerViewListener) {
                onRecyclerViewListener.onItemClick(position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(null != onRecyclerViewListener){
                return onRecyclerViewListener.onItemLongClick(position);
            }
            return false;
        }
    }

}