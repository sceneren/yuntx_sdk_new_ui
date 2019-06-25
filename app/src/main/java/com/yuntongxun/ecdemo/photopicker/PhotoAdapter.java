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
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.photopicker.model.Photo;
import com.yuntongxun.ecdemo.photopicker.utils.PhotoUtils;
import com.yuntongxun.ecdemo.photopicker.widgets.CheckView;

import java.util.ArrayList;
import java.util.List;

import static com.yuntongxun.ecdemo.photopicker.widgets.CheckView.UNCHECKED;

/**
 * 图片适配器
 *
 * @author 容联•云通讯
 * @version 5.0
 * @since 2016-4-6
 */
public class PhotoAdapter extends BaseAdapter {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_PHOTO = 1;

    private List<Photo> mData = new ArrayList<Photo>();
    //存放已选中的Photo数据
    private ArrayList<Photo> mSelectedPhotos;
    private Context mContext;
    private int mWidth;
    //是否显示相机，默认不显示
    private boolean mIsShowCamera = false;
    //照片选择模式，默认单选
    private int mSelectMode = PhotoPickerActivity02.MODE_SINGLE;
    //图片选择数量
    public static int mMaxNum = PhotoPickerActivity02.DEFAULT_NUM;

    private PhotoClickCallBack mCallBack;


    public PhotoAdapter(Context context, List<Photo> mData) {
        mSelectedPhotos = new ArrayList<Photo>();
        this.mData = mData;
        this.mContext = context;
        int screenWidth = PhotoUtils.getWidthInPx(mContext);
        mWidth = (screenWidth - PhotoUtils.dip2px(mContext, 4)) / 3;
    }

    public PhotoAdapter(Context context) {

        mSelectedPhotos = new ArrayList<Photo>();

        this.mContext = context;
        int screenWidth = PhotoUtils.getWidthInPx(mContext);
        mWidth = (screenWidth - PhotoUtils.dip2px(mContext, 4)) / 3;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mIsShowCamera) {
            return TYPE_CAMERA;
        } else {
            return TYPE_PHOTO;
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Photo getItem(int position) {
        if (mIsShowCamera) {
            if (position == 0) {
                return null;
            }
            return mData.get(position - 1);
        } else {
            return mData.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getId();
    }

    public void setData(List<Photo> mData) {
        this.mData.clear();
        this.mData = mData;
        notifyDataSetChanged();
    }

    public void setIsShowCamera(boolean isShowCamera) {
        this.mIsShowCamera = isShowCamera;
    }

    public boolean isShowCamera() {
        return mIsShowCamera;
    }

    public void setMaxNum(int maxNum) {
        this.mMaxNum = maxNum;
    }

    public void setPhotoClickCallBack(PhotoClickCallBack callback) {
        mCallBack = callback;
    }



    /**
     * 获取已选中相片
     *
     * @return 已选中相片
     */
    public List<Photo> setSelectedPhotos(ArrayList<Photo> mSelectedPhotos) {
        return this.mSelectedPhotos = mSelectedPhotos;
    }

    public void setSelectMode(int selectMode) {
        this.mSelectMode = selectMode;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_CAMERA) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.picker_item_camera_layout, null);
            convertView.setTag(null);
            //设置高度等于宽度
            GridView.LayoutParams lp = new GridView.LayoutParams(mWidth, mWidth);
            convertView.setLayoutParams(lp);
        } else {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.picker_item_photo_layout, null);
                holder.photoImageView = (ImageView) convertView.findViewById(R.id.imageview_photo);
                holder.selectView = (CheckView) convertView.findViewById(R.id.checkmark);
                holder.maskView = convertView.findViewById(R.id.mask);
                holder.wrapLayout = (FrameLayout) convertView.findViewById(R.id.wrap_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Photo photo = getItem(position);

            holder.photoImageView.setImageResource(R.drawable.picker_ic_photo_loading);
            Glide.with(mContext).load(photo.getPath()).dontAnimate()
                    .thumbnail(0.1f).into(holder.photoImageView);
            setCheckStatus(photo, holder.selectView,holder.maskView);
            holder.selectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectMode == PhotoPickerActivity02.MODE_MULTI) {
                        int checkedNum = checkedNumOf(photo);
                        if (checkedNum == CheckView.UNCHECKED) {
                            if (mSelectedPhotos.size() < mMaxNum) {
                                mSelectedPhotos.add(photo);
                                notifyDataSetChanged();
                            } else {
                                ToastUtil.showMessage("已达最大值");
                            }
                        } else {
                            mSelectedPhotos.remove(photo);
                            notifyDataSetChanged();
                        }
                    } else {
                        mSelectedPhotos.clear();
                        mSelectedPhotos.add(photo);
                    }
                    if (mCallBack != null) {
                        mCallBack.onPhotoClick(mSelectedPhotos);
                    }
                }
            });
        }
        return convertView;
    }

    private class ViewHolder {
        private ImageView photoImageView;
        private CheckView selectView;
        private View maskView;
        private FrameLayout wrapLayout;
    }


    public int checkedNumOf(Photo item) {
        int index = mSelectedPhotos.indexOf(item);
        return index == -1 ? CheckView.UNCHECKED : index + 1;
    }

    /**
     * 多选时，点击相片的回调接口
     */
    public interface PhotoClickCallBack {
        void onPhotoClick(List<Photo> mSelectedPhotos);
    }

    private void setCheckStatus(Photo item, CheckView checkView, View maskView) {
        checkView.setCountable(true);

        int checkedNum = checkedNumOf(item);
        if (checkedNum > 0) {
            checkView.setEnabled(true);
            checkView.setCheckedNum(checkedNum);
            maskView.setVisibility(View.VISIBLE);
        } else {
            if (mSelectedPhotos.size() == mMaxNum) {
                checkView.setEnabled(false);
                checkView.setCheckedNum(UNCHECKED);
                maskView.setVisibility(View.GONE);
            } else {
                checkView.setEnabled(false);
                maskView.setVisibility(View.GONE);
                checkView.setCheckedNum(checkedNum);
            }
        }

    }
}
