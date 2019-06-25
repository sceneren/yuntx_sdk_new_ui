package com.yuntongxun.ecdemo.ui.smallwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;
import com.yuntongxun.ecsdk.voip.video.ECOpenGlView;

/**
 * Created by smileklvens on 2017/9/12.
 */

public class VideoSmallView extends BaseSmallView {
    public static final String TAG = "RongXin.RongXinSmallView";

    private ECOpenGlView mPreviewView;
    private ECOpenGlView mRemoteView;
    private float mValue;

    private Runnable mPreviewViewGone = new Runnable() {
        @Override
        public void run() {
            if(mPreviewView != null) {
                mPreviewView.setVisibility(GONE);
            }
        }
    };

    public VideoSmallView(Context context, float value) {
        super(context, null);
        LayoutInflater.from(context).inflate(R.layout.voip_widget_video_talking, this);
        mValue = value;
        mRemoteView = (ECOpenGlView) findViewById(R.id.ogv_talking);
        mRemoteView.setAspectMode(ECOpenGlView.AspectMode.CROP);
        mRemoteView.setGlType(ECOpenGlView.RenderType.RENDER_REMOTE);
        mPreviewView = (ECOpenGlView) findViewById(R.id.ogv_local_video);
        mPreviewView.setAspectMode(ECOpenGlView.AspectMode.CROP);
        mPreviewView.setGlType(ECOpenGlView.RenderType.RENDER_PREVIEW);

        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if(setupManager != null) {
            setupManager.setGlDisplayWindow(mPreviewView , mRemoteView);
        }
        ECHandlerHelper.postDelayedRunnOnUI(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.voip_maximize).setVisibility(GONE);
            }
        } , 3000L);

//        ECHandlerHelper.postDelayedRunnOnUI(mPreviewViewGone , 3000L);
    }

    @Override
    public void onTouchUpStart() {
        mPreviewView.setVisibility(VISIBLE);
        ECHandlerHelper.removeCallbacksRunnOnUI(mPreviewViewGone);
        ECHandlerHelper.postDelayedRunnOnUI(mPreviewViewGone, 3000L);
    }

    @Override
    public void onTouchUpDone() {

    }

    @Override
    public void setWindowSize(int width, int height) {
        RelativeLayout.LayoutParams mRelativeParams = (RelativeLayout.LayoutParams) mPreviewView.getLayoutParams();
        mRelativeParams.height = height / 4;
        mRelativeParams.width = (int) (this.mValue * (float) mRelativeParams.height);
        mPreviewView.setLayoutParams(mRelativeParams);
        WindowManager.LayoutParams mSmallViewParams = (WindowManager.LayoutParams) getLayoutParams();
        if (mSmallViewParams == null) {
            mSmallViewParams = new WindowManager.LayoutParams();
        }

        mSmallViewParams.width = width;
        mSmallViewParams.height = height;
        this.setLayoutParams(mSmallViewParams);
    }

    @Override
    public void setCaptureView(ECCaptureView view) {
        LogUtil.d(TAG , "addCaptureView");
        if(super.mCaptureView != null) {
            this.removeView(super.mCaptureView);
            super.mCaptureView = null;
        }
        if(view != null) {
            super.mCaptureView = view;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1, 1);
            params.leftMargin = 20;
            params.topMargin = 20;
            this.addView(view, params);
            view.setVisibility(VISIBLE);
            LogUtil.d(TAG, "CaptureView added");
        }
    }

    @Override
    public void onAnimationEnd() {

    }

    public final void uninit() {
        super.uninit();
        setVisibility(INVISIBLE);
        ECHandlerHelper.removeCallbacksRunnOnUI(mPreviewViewGone);
    }
}