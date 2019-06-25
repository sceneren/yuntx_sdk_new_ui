/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yuntongxun.ecdemo.ui.videomeeting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.yuntongxun.ecdemo.R;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 视频会议主界面显示控件
 *
 * @author Jorstin Chan
 * @version 3.5
 * @date 2013-11-11
 */
public class CCPMulitVideoUI extends FrameLayout {

    public static final int LAYOUT_KEY_SUB_VIEW_1 = 0X0;
    public static final int LAYOUT_KEY_SUB_VIEW_2 = 0X1;
    public static final int LAYOUT_KEY_SUB_VIEW_3 = 0X2;
    public static final int LAYOUT_KEY_SUB_VIEW_4 = 0x3;
    private Context mContext;

    public HashMap<Integer, SubVideoSurfaceView> mSubViews = new HashMap<Integer, SubVideoSurfaceView>();

    @BindView(R.id.subvi_top_left)
    SubVideoSurfaceView subviTopLeft;
    @BindView(R.id.subvi_top_midlet)
    SubVideoSurfaceView subviTopMidlet;
    @BindView(R.id.subvi_top_right)
    SubVideoSurfaceView subviTopRight;
    @BindView(R.id.subvi_botom_left)
    SubVideoSurfaceView subviBotomLeft;
    @BindView(R.id.subvi_botom_midle)
    SubVideoSurfaceView subviBotomMidle;
    @BindView(R.id.subvi_botom_right)
    SubVideoSurfaceView subviBotomRight;

    private int mVideoUIMainKey = -1;

    private OnVideoUIItemClickListener mVideoUIItemClickListener;


    public CCPMulitVideoUI(Context context) {
        super(context);
        initVideoUILayout(context);
    }

    public CCPMulitVideoUI(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoUILayout(context);
    }

    public CCPMulitVideoUI(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVideoUILayout(context);
    }

    private void initVideoUILayout(Context context) {
        mContext = context;
        View rootView = inflate(getContext(), R.layout.mulit_video_ui, this);
        ButterKnife.bind(rootView);


        subviTopLeft.setIndex(LAYOUT_KEY_SUB_VIEW_1);
        subviTopMidlet.setIndex(LAYOUT_KEY_SUB_VIEW_2);
        subviTopRight.setIndex(LAYOUT_KEY_SUB_VIEW_3);
        subviBotomLeft.setIndex(LAYOUT_KEY_SUB_VIEW_4);

        mSubViews.put(LAYOUT_KEY_SUB_VIEW_1, subviTopLeft);
        mSubViews.put(LAYOUT_KEY_SUB_VIEW_2, subviTopMidlet);
        mSubViews.put(LAYOUT_KEY_SUB_VIEW_3, subviTopRight);
        mSubViews.put(LAYOUT_KEY_SUB_VIEW_4, subviBotomLeft);

        allViewGone();
    }

    private void allViewGone() {


        subviTopLeft.setVisibility(GONE);
        subviTopMidlet.setVisibility(GONE);
        subviTopRight.setVisibility(GONE);
        subviBotomLeft.setVisibility(GONE);
        subviBotomMidle.setVisibility(GONE);
        subviBotomRight.setVisibility(GONE);

    }

    /**
     * @param index
     * @param member
     */
    public void setVideoMember(int index, MultiVideoMember member) {


        SubVideoSurfaceView subVideoSurfaceView = mSubViews.get(index);
        if (subVideoSurfaceView == null) {
            return;
        }


        subVideoSurfaceView.setOnVideoUIItemClickListener(mVideoUIItemClickListener);
        if (member != null) {
            subVideoSurfaceView.setVideoUIMember(member);
            subVideoSurfaceView.setVisibility(VISIBLE);
        } else {
            subVideoSurfaceView.setVisibility(GONE);
        }

        //解决surfacrview 重叠的bug

        subVideoSurfaceView.getVideoSurfaceView().setZOrderOnTop(true);
        subVideoSurfaceView.getVideoSurfaceView().setZOrderMediaOverlay(true);

    }


    public synchronized SurfaceView getSurfaceView(int index, boolean remove) {
        SubVideoSurfaceView subVideoSurfaceView = mSubViews.remove(index);
        if (subVideoSurfaceView == null) {
            return null;
        }

        if (remove) {
            subVideoSurfaceView.removeSurfaceView();
        }
        mSubViews.put(index, subVideoSurfaceView);
        return subVideoSurfaceView.getVideoSurfaceView();
    }


    public void setOnVideoUIItemClickListener(OnVideoUIItemClickListener l) {
        mVideoUIItemClickListener = l;
    }

//    @OnClick({R.id.subvi_top_left, R.id.subvi_top_midlet, R.id.subvi_top_right, R.id.subvi_botom_left, R.id.subvi_botom_midle, R.id.subvi_botom_right})
//    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.subvi_top_left:
//                break;
//            case R.id.subvi_top_midlet:
//                break;
//            case R.id.subvi_top_right:
//                break;
//            case R.id.subvi_botom_left:
//                break;
//            case R.id.subvi_botom_midle:
//                break;
//            case R.id.subvi_botom_right:
//                break;
//        }
//    }


    /**
     * <p>Title: .java</p>
     * <p>Description:The interface is used to manage the members of the conference
     * If you want to know the results of the implementation of click each member
     * You must set the monitor through {@link CCPMulitVideoUI#setOnVideoUIItemClickListener(OnVideoUIItemClickListener)}</p>
     *
     * @version 3.5
     * @see CCPMulitVideoUI#setOnVideoUIItemClickListener(OnVideoUIItemClickListener)
     */
    public interface OnVideoUIItemClickListener {

        /**
         * Callback method to be invoked when an item in this VideoUI has
         * been clicked.
         * <p>
         * You must set the monitor through
         * {@link CCPMulitVideoUI#setOnVideoUIItemClickListener(OnVideoUIItemClickListener)}</p>
         *
         * @param
         */
        void onVideoUIItemClick(int key);
    }
}
