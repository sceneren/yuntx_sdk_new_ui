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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.ui.videomeeting.CCPMulitVideoUI.OnVideoUIItemClickListener;
import com.yuntongxun.ecsdk.voip.video.ECOpenGlView;


public class SubVideoSurfaceView extends FrameLayout {

    private boolean mAttach = false;
    private ECOpenGlView mSurfaceView;
    private TextView mSubText;
    private FrameLayout mContainer;

    private Drawable mOpreableDraw;
    private int mIndex;

    private OnVideoUIItemClickListener mVideoUIItemClickListener;

    /**
     * @param context
     */

    public SubVideoSurfaceView(Context context) {
        this(context, null);
    }


//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(getScrenWith()/4,getScrenWith()/4);
//    }
//
//    public int  getScrenWith(){
//
//        WindowManager wm = (WindowManager) getContext()
//                .getSystemService(Context.WINDOW_SERVICE);
//
//        return  wm.getDefaultDisplay().getWidth();
//    }

    /**
     * @param context
     * @param attrs
     */
    public SubVideoSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SubVideoSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView();
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int screenWidth = getScreenWidth(getContext());
//        setMeasuredDimension(screenWidth / 3, screenWidth / 3);
//    }


    //获取屏幕的宽度
    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    //获取屏幕的高度
    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }

    /**
     *
     */
    private void initView() {
        inflate(getContext(), R.layout.mulit_video_surfaceview, this);
        mContainer = (FrameLayout) findViewById(R.id.container);
        mSubText = (TextView) findViewById(R.id.text);

//		initThreePoint();
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public ECOpenGlView initSurfaceView() {
        if (mSurfaceView == null) {
            mSurfaceView = new ECOpenGlView(getContext());
            LogUtil.d(" surfaceView init" + mSurfaceView.toString());
        } else {
            LogUtil.d(" surfaceView return" + mSurfaceView.toString());
        }
        return mSurfaceView;
    }

    /**
     *
     */
//	private void initThreePoint(){
//		mOpreableDraw = getResources().getDrawable(R.drawable.three_point);
//		mOpreableDraw.setBounds(0, 0, mOpreableDraw.getMinimumWidth(), mOpreableDraw.getMinimumHeight());
//	}
    public SurfaceView getVideoSurfaceView() {
        return mSurfaceView;
    }

    public void removeSurfaceView() {
        FrameLayout mLayout = ((FrameLayout) mContainer.getParent());
        if (mLayout == null) {
            return;
        }
        if (mAttach) {
            mLayout.removeView(mSurfaceView);
            mAttach = false;
            mSurfaceView = null;
        }
    }

    public TextView getDisplayTextView() {
        return mSubText;
    }

    /**
     * @param member
     */
    public void setVideoUIMember(MultiVideoMember member) {
        attachSurfaceView(member);
        if (mSurfaceView !=null){
            mSurfaceView.setZOrderOnTop(true);
            mSurfaceView.setZOrderMediaOverlay(true);

        }
        if (member != null) {
            setVideoUIText(AvatorUtil.getInstance().getMarkName(member.getNumber()), true);
        }
    }

    private void attachSurfaceView(Object obj) {
        mSurfaceView = initSurfaceView();
        FrameLayout mLayout = ((FrameLayout) mContainer.getParent());
        if (mLayout == null) {
            return;
        }
        if (obj == null) {
            if (mAttach) {
                mLayout.removeView(mSurfaceView);
                mAttach = false;
                mSurfaceView = null;
            }
            return;
        }
        if(!mAttach) {
            mSurfaceView.invalidate();
            mLayout.addView(mSurfaceView ,0);
            mAttach = true;
        }
    }

    /**
     * @param text
     */
    public void setVideoUIText(CharSequence text) {
        attachSurfaceView(text);
        setVideoUIText(text, true);

    }

    /**
     * @param text
     * @param Operable
     */
    private void setVideoUIText(CharSequence text, boolean Operable) {

        mSubText.setText(text);

        if (text == null) {
            mSubText.setCompoundDrawables(null, null, null, null);
            setOnClickListener(null);
            return;
        }
        mSubText.setCompoundDrawables(null, null, mOpreableDraw, null);
        // Set the members item the click listener callback
        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mVideoUIItemClickListener != null) {
                    mVideoUIItemClickListener.onVideoUIItemClick(mIndex);
                }
            }
        });

    }

    public void setOnVideoUIItemClickListener(OnVideoUIItemClickListener l) {
        mVideoUIItemClickListener = l;
    }

}
