package com.yuntongxun.ecdemo.ui.smallwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DensityUtil;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.SettingsCompat;

import java.util.Locale;

/**
 * 语音通话浮动窗口显示管理器
 *
 * @author 容联•云通讯
 * @version 5.2.0
 * @since 2016-07-22
 */
public class MeetingMiniManager {

    public static final String TAG = "demo.MeetingMiniManager";

    private static final int mPadding = DensityUtil.fromDPToPix(CCPAppManager.getContext(), 8);
    private static final int mHeight = DensityUtil.fromDPToPix(CCPAppManager.getContext(), 96);
    private static final int mWidth = DensityUtil.fromDPToPix(CCPAppManager.getContext(), 76);
    private VoiceSmallView mSmallView;
    private Point mPoint;

    /**
     * 根据当前给定的Intent显示一个浮动窗口
     *
     * @param intent 浮动窗口点击跳转事件
     * @return 是否显示成功
     */
    boolean addVoiceMiniWindow(final Intent intent) {
        if (mSmallView != null) {
            dismiss();
        }
        final Context mCtx = CCPAppManager.getContext();

        if (!SettingsCompat.canDrawOverlays(mCtx)) {
            LogUtil.e(TAG, "showVoiceTalking, permission denied");
            MeetingWarningDialog.showWarningDialog(mCtx);
        }


        if (mSmallView == null) {
            mSmallView = new VoiceSmallView(mCtx);
        }
        mSmallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCtx.startActivity(intent);
                CallNotificationMgr.cancelNotification(40);
                v.setOnClickListener(null);

            }
        });
        WindowManager mWindowManager = (WindowManager) mCtx.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = mWidth;
        params.height = mHeight;
        mPoint = VoiceMeetingService.getInstance().mPoint;
        if (mPoint == null) {
            params.x = DensityUtil.getWidthPixels(mCtx) - params.width - mPadding;
            params.y = mPadding;
        } else {
            params.x = mPoint.x;
            params.y = mPoint.y;
        }
        try {
            mWindowManager.addView(mSmallView, params);
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "add failed" + e.getMessage());
            return false;
        }
    }

    /**
     * 销毁当前的浮动窗口显示器
     */
    public  void dismiss() {
        LogUtil.e(TAG, "dismiss");
        final Context mCtx = CCPAppManager.getContext();
        WindowManager mWindowManager = (WindowManager) mCtx.getSystemService(Context.WINDOW_SERVICE);

        try {
            if (mSmallView != null) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mSmallView.getLayoutParams();
                VoiceMeetingService.getInstance().mPoint = new Point(layoutParams.x, layoutParams.y);
                mWindowManager.removeView(mSmallView);
                mSmallView.setOnClickListener(null);
                mSmallView = null;
            }

        } catch (Exception e) {
            LogUtil.e(TAG, "remove failed" + e.getMessage());
        }
    }

    /**
     * 更新当前的通话时间
     *
     * @param time 通话时间
     */
    public final void setCallTime(int time) {
        if (mSmallView != null) {
            String text = String.format(Locale.CHINA, "%02d:%02d", time / 60L, time % 60L);
            mSmallView.mTimerView.setTextSize(1, 14.0F);
            mSmallView.mTimerView.setText(text);
        }

    }

    /**
     * 更新当前文字提示
     *
     * @param text 文字提示
     */
    public final void setText(String text) {
        if (mSmallView != null) {
            mSmallView.mTimerView.setTextSize(1, 12.0F);
            mSmallView.mTimerView.setText(text);
        }

    }

}
