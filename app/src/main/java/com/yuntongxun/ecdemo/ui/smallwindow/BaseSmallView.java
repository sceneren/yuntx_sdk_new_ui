package com.yuntongxun.ecdemo.ui.smallwindow;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.yuntongxun.ecdemo.common.utils.DensityUtil;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;

/**
 * @author 容联•云通讯
 * @version 5.2.0
 * @since 2016-07-18
 */
public abstract class BaseSmallView extends FrameLayout {

    private static final String TAG = "RongXin.BaseSmallView";

    /**窗口管理器*/
    private WindowManager mWindowManager;
    /**记录小窗口的宽和高*/
    private PointF mPointF = new PointF();
    /**记录原始的小窗口坐标*/
    private Point mPoint = new Point();
    private Point mInterval = new Point();
    private Point resultPoint = new Point();
    private Point mWindowPoint;
    /**视频预览窗口*/
    ECCaptureView mCaptureView;
    /**窗口点击事件*/
    private OnClickListener mOnClickListener;
    private long mStartTime;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            float intervalTime = (float)(System.currentTimeMillis() - mStartTime) * 1.0F / 200.0F;
            if(intervalTime <= 1.0F) {
                ECHandlerHelper.postDelayedRunnOnUI(mRunnable, 5L);
                int x = mInterval.x;
                int changeX = (int)(((float) resultPoint.x * 1.0F - (float)mInterval.x) * intervalTime);
                int y = mInterval.y;
                updateWindowPosition(x + changeX, (int)(intervalTime * ((float) resultPoint.y * 1.0F - (float)mInterval.y)) + y);
            } else {
                updateWindowPosition(BaseSmallView.this.resultPoint.x, BaseSmallView.this.resultPoint.y);
                onAnimationEnd();
            }
        }
    };

    public BaseSmallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowPoint = new Point(mWindowManager.getDefaultDisplay().getWidth(), mWindowManager.getDefaultDisplay().getHeight());
    }

    /**
     * 更新当前窗口的位置
     * @param x X坐标
     * @param y Y坐标
     */
    private void updateWindowPosition(int x, int y) {
        if (mWindowManager != null) {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
            layoutParams.x = x;
            layoutParams.y = y;

            try {
                mWindowManager.updateViewLayout(this, layoutParams);
            } catch (Throwable e) {
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下的相对于屏幕坐标
                mPointF.x = event.getRawX();
                mPointF.y = event.getRawY();
                // 当前小窗口相对于屏幕的坐标
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
                mPoint.x = layoutParams.x;
                mPoint.y = layoutParams.y;
                break;
            case MotionEvent.ACTION_UP:
                // 首先判断当前的按下操作是否为点击事件
                if(Math.abs(event.getRawX() - mPointF.x) < (float) DensityUtil.px2dip(this.getContext(), 3.0F) &&
                        Math.abs(event.getRawY() - mPointF.y) < (float) DensityUtil.px2dip(this.getContext(), 3.0F) &&
                        this.mOnClickListener != null) {
                    this.mOnClickListener.onClick(this);
                }

                this.onTouchUpStart();
                // 手指抬起来的时候相对于屏幕的坐标
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                // 计算当前手指抬起来的时候小窗口相对于手指按下去的时候X/Y坐标移动的距离
                mInterval.x = (int)Math.max(Math.min(rawX + (float)mPoint.x - mPointF.x, (float)mWindowPoint.x), 0.0F);
                mInterval.y = (int)Math.max(Math.min(rawY + (float)mPoint.y - mPointF.y, (float)mWindowPoint.y), 0.0F);
                int padding = DensityUtil.px2dip(this.getContext(), 5.0F);
                if(mInterval.x + this.getWidth() / 2 <= mWindowPoint.x / 2) {
                    this.resultPoint.x = padding;
                } else {
                    this.resultPoint.x = mWindowPoint.x - this.getWidth() - padding;
                }

                this.resultPoint.y = mInterval.y;
                this.mStartTime = System.currentTimeMillis();
                ECHandlerHelper.postDelayedRunnOnUI(mRunnable, 5L);
                this.onTouchUpDone();
                break;
            case MotionEvent.ACTION_MOVE:
                // 更新当前窗口的坐标位置
                updateWindowPosition((int)Math.max(Math.min((float)mPoint.x + event.getRawX() - mPointF.x, (float)mWindowPoint.x), 0.0F),
                        (int)Math.max(Math.min((float)mPoint.y + event.getRawY() - mPointF.y, (float)mWindowPoint.y), 0.0F));
        }

        return true;
    }

    /**
     * 当手指从小窗口开始抬起之前
     */
    public abstract void onTouchUpStart();

    /**
     * 当手指从小窗口开始抬起结束
     */
    public abstract void onTouchUpDone();

    public abstract void onAnimationEnd();

    /**
     * 设置当前浮动窗口的宽和高
     * @param width 宽
     * @param height 高
     */
    public void setWindowSize(int width, int height) {

    }

    public abstract void setCaptureView(ECCaptureView view) ;

    /**
     * 设置当前窗口点击事件
     * @param l 点击事件
     */
    public void setOnClickListener(OnClickListener l) {
        this.mOnClickListener = l;
    }

    public void uninit() {
        ECHandlerHelper.removeCallbacksRunnOnUI(mRunnable);
        mWindowManager = null;
        if(mCaptureView != null) {
            this.removeView(mCaptureView);
            mCaptureView = null;
        }

    }
}
