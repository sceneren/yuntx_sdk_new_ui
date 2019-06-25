/*
 *  Copyright (c) 2015 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yuntongxun.ecdemo.photopicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.photopicker.model.PhotoDirectory;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 图片目录适配器
 *
 * @author 容联•云通讯
 * @version 5.0
 * @since 2016-4-6
 */
public class FolderAdapter extends BaseAdapter {

    List<PhotoDirectory> mData = new ArrayList<PhotoDirectory>();
    Context mContext;

    public FolderAdapter(Context context, List<PhotoDirectory> mData) {
        this.mData = mData;
        this.mContext = context;
    }

    public FolderAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.picker_item_floder_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ivIcon.setImageResource(R.drawable.picker_ic_photo_loading);
        PhotoDirectory folder = mData.get(position);
        holder.tvFileName.setText(folder.getName());
        holder.tvPicNums.setText(folder.getPhotos().size() + "张");
        Glide.with(mContext).load(folder.getPhotos().get(0).getPath()).dontAnimate()
		.thumbnail(0.1f).into(holder.ivIcon);
        return convertView;
    }

    public void setData(List<PhotoDirectory> dirs) {
        mData.clear();
        mData.addAll(dirs);
        notifyDataSetChanged();
    }


    class ViewHolder {
        @BindView(R.id.iv_icon)
        ImageView ivIcon;
        @BindView(R.id.tv_file_name)
        TextView tvFileName;
        @BindView(R.id.tv_pic_nums)
        TextView tvPicNums;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
