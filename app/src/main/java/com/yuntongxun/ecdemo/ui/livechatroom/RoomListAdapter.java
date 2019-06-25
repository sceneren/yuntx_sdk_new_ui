package com.yuntongxun.ecdemo.ui.livechatroom;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecsdk.ECLiveChatRoom;

import java.util.List;


public class RoomListAdapter extends RecyclerView.Adapter {
    public static interface OnRecyclerViewListener{
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    private static final String TAG = RoomListAdapter.class.getSimpleName();
    private List<ECLiveChatRoom> list;

    public RoomListAdapter(List<ECLiveChatRoom> list){
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_test_item_person, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i){
        PersonViewHolder holder = (PersonViewHolder) viewHolder;
        holder.position = i;
        ECLiveChatRoom person = list.get(i);
        holder.nameTv.setText("房间号:"+person.roomId);
        holder.ageTv.setText("房间名:"+person.roomName);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public View rootView;
        public TextView nameTv;
        public TextView ageTv;
        public int position;
        public ImageView iv;

        public PersonViewHolder(View itemView){
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.recycler_view_test_item_person_name_tv);
            ageTv = (TextView) itemView.findViewById(R.id.recycler_view_test_item_person_age_tv);
            iv= (ImageView)itemView.findViewById(R.id.live_list_iv);
            rootView = itemView.findViewById(R.id.recycler_view_test_item_person_view);
            LogUtil.e("position"+position);
            ECLiveChatRoom room = list.get(this.position);
            Glide.with(CCPAppManager.getContext()).load(room.pic).dontAnimate().into(iv);

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