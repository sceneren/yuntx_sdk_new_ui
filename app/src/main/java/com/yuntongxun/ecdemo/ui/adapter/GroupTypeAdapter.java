package com.yuntongxun.ecdemo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zlk on 2017/7/27.
 */

public class GroupTypeAdapter extends BaseAdapter {
    private Context mContext;
    private String[] types;
    private final LayoutInflater inflater;

    public GroupTypeAdapter(Context mContext, String[] types) {
        this.mContext = mContext;
        this.types = types;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return types.length;
    }

    @Override
    public Object getItem(int position) {
        return types[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_group_type, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvItemGroupType.setText(types[position]);
        return convertView;
    }


    class ViewHolder {
        @BindView(R.id.tv_item_group_type)
        TextView tvItemGroupType;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

