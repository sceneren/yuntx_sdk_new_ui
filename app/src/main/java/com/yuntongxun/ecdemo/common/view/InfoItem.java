/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
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
package com.yuntongxun.ecdemo.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * 群。讨论组 行控件
 */
public class InfoItem extends RelativeLayout {


    @BindView(R.id.tv_left_title)
    TextView tvLeftTitle;
    @BindView(R.id.tv_right)
    TextView tvRight;
    @BindView(R.id.iv_right)
    ImageView ivRight;
    @BindView(R.id.ll_root)
    LinearLayout llRoot;
    private Unbinder bind;

    public InfoItem(Context context) {
        super(context);
        init();
    }

    public InfoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InfoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.info_item, this, true);
        bind = ButterKnife.bind(inflate);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bind.unbind();
    }

    public void setLeftTitle(String title) {
        if (tvLeftTitle != null && !TextUtils.isEmpty(title)) {
            if (tvLeftTitle.getVisibility() == GONE) {
                tvLeftTitle.setVisibility(VISIBLE);
            }
            tvLeftTitle.setText(title);

        }
    }

    public void setLeftTitle(@StringRes int id) {
        String title = getContext().getResources().getString(id);
        setLeftTitle(title);
    }

    public void setRightContent(String content) {
        if (tvRight != null && !TextUtils.isEmpty(content)) {
            if (tvRight.getVisibility() == GONE) {
                tvRight.setVisibility(VISIBLE);
            }
            if (ivRight != null && ivRight.getVisibility() == VISIBLE) {
                ivRight.setVisibility(GONE);
            }
            tvRight.setText(content);
        }
    }

    public void setRightContent(@StringRes int id) {
        String title = getContext().getResources().getString(id);
        setRightContent(title);
    }


    public void setRightImage(@DrawableRes int resId) {
        if (ivRight != null) {
            if (ivRight != null && ivRight.getVisibility() == GONE) {
                ivRight.setVisibility(VISIBLE);
            }
            ivRight.setImageResource(resId);
            if (tvLeftTitle != null && tvLeftTitle.getVisibility() == VISIBLE) {
                tvLeftTitle.setVisibility(GONE);
            }
            if (tvRight != null && tvRight.getVisibility() == VISIBLE) {
                tvRight.setVisibility(GONE);
            }
        }
    }

    public void setRightImageBitmap(Bitmap bitmap) {
        if (ivRight != null) {
            if(ivRight.getVisibility() == GONE){
                ivRight.setVisibility(VISIBLE);
            }
            ivRight.setImageBitmap(bitmap);
            if (tvRight != null && tvRight.getVisibility() == VISIBLE) {
                tvRight.setVisibility(GONE);
            }
        }
    }


    public void setRootViewOnCliclListener(OnClickListener onCliclListener) {
        if (llRoot != null && onCliclListener != null) {
            llRoot.setOnClickListener(onCliclListener);
        }
    }
}
