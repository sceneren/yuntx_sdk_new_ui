package com.yuntongxun.ecdemo.ui.livechatroom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;

import java.util.ArrayList;


/**
 * author：Administrator on 2016/12/26 15:03
 * description:文件说明
 * version:版本
 */
///定影GridView的Adapter
public class GiftGridViewAdapter extends BaseAdapter{
    private int page;
    private int count;
    private ArrayList<Gift> gifts ;
    private Context context ;

    public void setGifts(ArrayList<Gift> gifts){
        this.gifts = gifts;
        notifyDataSetChanged();
    }
    public GiftGridViewAdapter(Context context,int page, int count) {
        this.page = page;
        this.count = count;
        this.context = context ;
    }

    @Override
    public int getCount(){
        // TODO Auto-generated method stub
        return page==0?8:2;
    }

    @Override
    public Gift getItem(int position) {
        // TODO Auto-generated method stub
        return gifts.get(page * count + position);
    }

    @Override
    public long getItemId(int position){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent){
        // TODO Auto-generated method stub
        ViewHolder viewHolder = null;
        final Gift catogary = gifts.get(page * count + position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gift, null);
            viewHolder.grid_fragment_home_item_img =
                    (ImageView) convertView.findViewById(R.id.grid_fragment_home_item_img);
            viewHolder.grid_fragment_home_item_txt =
                    (TextView) convertView.findViewById(R.id.grid_fragment_home_item_txt);
            viewHolder.tvNum = (TextView)convertView.findViewById(R.id.tv_num);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.grid_fragment_home_item_txt.setText(catogary.name);
        viewHolder.grid_fragment_home_item_img.setImageResource(catogary.giftType);
        final View finalConvertView = convertView;
        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (onGridViewClickListener!=null){
                    onGridViewClickListener.click(catogary);
                    finalConvertView.setBackgroundColor(context.getResources().getColor(R.color.violet02));
                }
                for(int i=0;i<getCount();i++){
                    View v=parent.getChildAt(i);
                    if (position == i) {//当前选中的Item改变背景颜色
                        view.setBackgroundColor(context.getResources().getColor(R.color.violet02));
                    } else {
                        v.setBackgroundColor(context.getResources().getColor(R.color.translucent01));
                    }
                }

            }
        });

        return convertView;
    }
    public class ViewHolder{
        public ImageView grid_fragment_home_item_img;
        public TextView grid_fragment_home_item_txt;
        public TextView tvNum ;
    }

    public  OnGridViewClickListener onGridViewClickListener ;

    public void setOnGridViewClickListener(OnGridViewClickListener onGridViewClickListener) {
        this.onGridViewClickListener = onGridViewClickListener;
    }

    public interface OnGridViewClickListener{
        void click(Gift gift);
    }
}
