package com.yuntongxun.ecdemo.ui.smallwindow;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DensityUtil;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.SettingsCompat;
import com.yuntongxun.ecdemo.ui.voip.VideoActivity;
import com.yuntongxun.ecdemo.ui.voip.VoIPCallActivity;
import com.yuntongxun.ecdemo.ui.voip.VoIPCallHelper;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;

import java.util.Locale;

import static com.yuntongxun.ecdemo.ui.smallwindow.VoiceMeetingService.getMiniWindow;

/**
 * @author 容联•云通讯
 * @version 5.2.0
 * @since 2016-07-22
 */
public class VoipSmallWindow {
    public static final String TAG = "demo.VoipSmallWindow";

    public static final int STATE_SHOW_NOTIFY = 259;
    /**
     * 显示视频通话小窗口
     */
    public static final int STATE_SHOW_VIDEO_WINDOW = 260;
    /**
     * 显示语音通话小窗口
     */
    public static final int STATE_SHOW_VOICE_WINDOW = 261;
    /**
     * 移除小窗口
     */
    private static final int STATE_REMOVE_WINDOW = 262;
    /**
     * 屏幕资源锁
     */
    private PowerManager.WakeLock mWakeLock;

    private int mStatus = -1;
    protected static volatile long mTime = -1L;


    /**
     * 当前小窗口
     */
    private BaseSmallView mBaseSmallView;

    private ECCaptureView mCaptureView;

    /**
     * 是否呼出
     */
    private boolean mOutCall = false;
    /**
     * 是否视频呼叫
     */
    private boolean mVideoCall = false;

    /**
     * 状态栏时间更新
     */
    private ECTimerHandler mTimerUpdateNotify;
    private ECTimerHandler mTimerUpdate;

    /**
     * 通话联系人
     */
    private String mUserName;


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBaseSmallView != null) {
                mBaseSmallView.setOnClickListener(null);
            }
            removeSmallView();
            Context ctx = CCPAppManager.getContext();

            Intent callAction = new Intent(ctx, VideoActivity.class);
            VoIPCallHelper.mHandlerVideoCall = true;
            callAction.putExtra(VoIPCallActivity.EXTRA_CALL_NAME, VoiceMeetingService.getInstance().nickname);
            callAction.putExtra(VoIPCallActivity.EXTRA_CALL_NUMBER, VoiceMeetingService.getInstance().contactId);
            callAction.putExtra(ECDevice.CALLTYPE, VoiceMeetingService.getInstance().callType);
            callAction.putExtra(VoIPCallActivity.EXTRA_OUTGOING_CALL, VoiceMeetingService.getInstance().outgoingCall);
            if (VoiceMeetingService.getInstance().callbackCall) {
                callAction.putExtra(VoIPCallActivity.ACTION_CALLBACK_CALL, true);
            }
            callAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(callAction);
        }
    };


    /**
     * 创建一个小的浮动窗口
     */
    public VoipSmallWindow() {
        mWakeLock = ((PowerManager) CCPAppManager.getContext().getSystemService(Context.POWER_SERVICE)).
                newWakeLock(536870922, TAG);
        mWakeLock.acquire();
    }

    /**
     * 创建一个小的浮动窗口
     *
     * @param userName  用户昵称
     * @param outCall   是否呼出
     * @param videoCall 是否视频通话
     */
    public VoipSmallWindow(String userName, boolean outCall, boolean videoCall) {
        LogUtil.d(TAG, String.format("VoipSmallWindow userName:%s,outCall:%s,videoCall:%s", userName, outCall, videoCall));
        mUserName = userName;
        this.mOutCall = outCall;
        this.mVideoCall = videoCall;
        mWakeLock = ((PowerManager) CCPAppManager.getContext().getSystemService(Context.POWER_SERVICE)).
                newWakeLock(536870922, TAG);
        mWakeLock.acquire();
    }

    /**
     * 溢出通话小窗口
     */
    private void removeSmallView() {
        LogUtil.d(TAG, "removeSmallView");
        if (mBaseSmallView != null) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mBaseSmallView.getLayoutParams();
            VoiceMeetingService.getInstance().mPoint = new Point(params.x, params.y);
            WindowManager windowManager = (WindowManager) CCPAppManager.getContext().getSystemService(Context.WINDOW_SERVICE);
            mBaseSmallView.uninit();
            try {
                windowManager.removeView(mBaseSmallView);
            } catch (IllegalArgumentException e) {
                LogUtil.e(TAG, "remove failed" + e.getMessage());
            }

            mBaseSmallView = null;
        }
        if (mTimerUpdate != null) {
            mTimerUpdate.stopTimer();
        }
        VoiceMeetingService.getInstance().getMiniWindow().setText(CCPAppManager.getContext().getString(R.string.voip_call_over));
        if (mTimerUpdateNotify != null) {
            mTimerUpdateNotify.stopTimer();
        }
        CallNotificationMgr.cancelNotification(40);
        VoiceMeetingService.getInstance().getMiniWindow().dismiss();
    }

    //    /**
//     * 显示语音小窗口
//     */
    public void showVoiceTalking() {
        Context context = CCPAppManager.getContext();
        if (context == null) {
            return;
        }
        if (mTime == -1L) {
            mTime = System.currentTimeMillis();
        }
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager != null && !setupManager.getLoudSpeakerStatus()) {
            showMsg(R.string.voip_audio_talking_hint);
        }
        removeSmallView();

        mTimerUpdateNotify = new ECTimerHandler(new ECTimerHandler.CallBack() {
            @Override
            public boolean onTimerExpired() {
                String timer = formatNotifyTimer();
                // 这里需要动态更新状态栏通知时间
                breathEffectNotify(timer, CCPAppManager.getContext().getString(R.string.app_name), timer, false, false);
                return true;
            }
        }, true);
        // 5秒更新一次时间
        mTimerUpdateNotify.startTimer(5000L);
        // 这里需要动态更新状态栏通知时间
        String timer = formatNotifyTimer();
        breathEffectNotify(timer, CCPAppManager.getContext().getString(R.string.app_name), timer, false, false);

        ECTimerHandler mTimerUpdate = new ECTimerHandler(new ECTimerHandler.CallBack() {
            @Override
            public boolean onTimerExpired() {
                getMiniWindow().setText(formatTimer());
                return true;

            }
        }, true);
        mTimerUpdate.startTimer(1000L);

        Intent intent = new Intent();
        intent.setClass(context, VoIPCallActivity.class);
        intent.putExtra(VoIPCallActivity.EXTRA_CALL_NAME, VoiceMeetingService.getInstance().nickname);
        intent.putExtra(VoIPCallActivity.EXTRA_CALL_NUMBER, VoiceMeetingService.getInstance().contactId);
        intent.putExtra(ECDevice.CALLTYPE, VoiceMeetingService.getInstance().callType);
        intent.putExtra(VoIPCallActivity.EXTRA_OUTGOING_CALL, VoiceMeetingService.getInstance().outgoingCall);

        if (VoiceMeetingService.getInstance().callbackCall) {
            intent.putExtra(VoIPCallActivity.ACTION_CALLBACK_CALL, true);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getMiniWindow().addVoiceMiniWindow(intent);
        getMiniWindow().setText(formatTimer());
    }

    /**
     * 提示消息
     *
     * @param msg 消息内容
     */
    public static void showMsg(int msg) {
        Context ctx = CCPAppManager.getContext();
        Toast.makeText(ctx, ctx.getString(msg), Toast.LENGTH_LONG).show();
    }


    /**
     * 单纯的时间格式化字符串
     *
     * @return 时间格式和字符串
     */
    public String formatTimer() {
        int time = (int) (System.currentTimeMillis() / 1000L - mTime);
        if (mTime == -1L) {
            time = 0;
        }

        return time >= 3600 ? String.format(Locale.US, "%d:%02d:%02d", (time / 3600), (time % 3600 / 60), (time % 60)) :
                String.format(Locale.US, "%d:%02d", (time / 60), (time % 60));
    }


    public void showSmallWindow(int newState) {
        if (mStatus == newState) {
            return;
        }
        mStatus = newState;
        switch (mStatus) {
            case STATE_SHOW_VIDEO_WINDOW:
                showVideoTalking();
                return;
            case STATE_SHOW_VOICE_WINDOW:
                showVoiceTalking();
                return;
            case STATE_SHOW_NOTIFY:
                if (mTimerUpdateNotify != null) {
                    mTimerUpdateNotify.stopTimer();
                }
                String ticker = CCPAppManager.getContext().getString(R.string.voip_notification_msg);
                breathEffectNotify(ticker, CCPAppManager.getContext().getString(R.string.app_name), ticker, true, false);
                return;

            default:
                break;

        }
    }


    public void setCaptureView(ECCaptureView view) {
        mCaptureView = view;
        if (mBaseSmallView != null && mCaptureView != null) {
            if (mCaptureView.getParent() != null && mCaptureView.getParent() instanceof ViewGroup) {
                ((ViewGroup) mCaptureView.getParent()).removeView(mCaptureView);
            }

            mBaseSmallView.setCaptureView(view);
        }
    }

    /**
     * 显示视频呼叫小窗口
     */
    private void showVideoTalking() {
        Context context = CCPAppManager.getContext();
        if (context == null) {
            return;
        }
        if (!SettingsCompat.canDrawOverlays(context)) {
            MeetingWarningDialog.showWarningDialog(context);
            LogUtil.e(TAG, "showVideoTalking, permission denied");
        }
        removeSmallView();
        mBaseSmallView = new VideoSmallView(context, 0.7476636F);
        mBaseSmallView.setCaptureView(mCaptureView);
        int height = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight() / 5 + DensityUtil.px2dip(context, 7.0F);
        int width = (int) ((float) DensityUtil.px2dip(context, 7.0F) + 0.7476636F * height);
        mBaseSmallView.setWindowSize(width, height);
        mBaseSmallView.setOnClickListener(mOnClickListener);
        Point mPoint = new Point(width, height);
        LogUtil.d(TAG, "addViewToWindowManager");
        if (mTimerUpdateNotify != null) {
            mTimerUpdateNotify.stopTimer();
        }
        if (mTimerUpdate != null) {
            mTimerUpdate.stopTimer();
        }
        CallNotificationMgr.cancelNotification(40);
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        params.width = mPoint.x;
        params.height = mPoint.y;
        mPoint = VoiceMeetingService.getInstance().mPoint;
        if (mPoint == null) {
            int padding = DensityUtil.px2dip(context, 5.0F);
            params.x = displayMetrics.widthPixels - params.width - padding;
            params.y = padding;
        } else {
            params.x = mPoint.x;
            params.y = mPoint.y;
        }

        try {
            mWindowManager.addView(mBaseSmallView, params);
        } catch (Exception e) {
            LogUtil.e(TAG, "addViewToWindowManager failed: " + e.getMessage());
        }
        String ticker = CCPAppManager.getContext().getString(R.string.voip_video_is_talking_tip);
        breathEffectNotify(ticker, mUserName, ticker, false, true);
//        callService.onVoipUICreated(this, 2);
    }


    /**
     * 状态栏提醒
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void breathEffectNotify(final String ticker, final String title, final String msg, boolean create, boolean isVideo) {
        LogUtil.d(TAG, "breathEffect " + create);
        if (create) {
            mTimerUpdateNotify = new ECTimerHandler(new ECTimerHandler.CallBack() {
                private int mCount = 0;

                @Override
                public boolean onTimerExpired() {
                    ++this.mCount;
                    StringBuilder sb = new StringBuilder(formatNotifyTimer());
                    if (this.mCount % 2 == 1) {
                        sb.append(" ");
                    } else {
                        sb.append("");
                    }
                    breathEffectNotify(sb.toString(), title, msg, false, false);
                    return true;
                }
            }, true);
            mTimerUpdateNotify.startTimer(5000L);
        }

        Intent intent = new Intent();
        if (isVideo) {
            VoIPCallHelper.mHandlerVideoCall = true;
            intent.setClass(CCPAppManager.getContext(), VideoActivity.class);
        } else {
            intent.setClass(CCPAppManager.getContext(), VoIPCallActivity.class);
        }
        intent.putExtra(VoIPCallActivity.EXTRA_CALL_NAME, VoiceMeetingService.getInstance().nickname);
        intent.putExtra(VoIPCallActivity.EXTRA_CALL_NUMBER, VoiceMeetingService.getInstance().contactId);
        intent.putExtra(ECDevice.CALLTYPE, VoiceMeetingService.getInstance().callType);
        intent.putExtra(VoIPCallActivity.EXTRA_OUTGOING_CALL, VoiceMeetingService.getInstance().outgoingCall);
        if (VoiceMeetingService.getInstance().callbackCall) {
            intent.putExtra(VoIPCallActivity.ACTION_CALLBACK_CALL, true);
        }


        PendingIntent pendingIntent = PendingIntent.getActivity(CCPAppManager.getContext(), 40, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(CCPAppManager.getContext());
        builder.setTicker(ticker)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.androidtemplate)
                .setOngoing(true);
        Notification notification = builder.getNotification();
        CallNotificationMgr.showCallingNotification(40, notification);

    }

    /**
     * 需要在状态栏显示的时间格式化字符串
     *
     * @return 在状态栏显示的时间格式化字符串
     */
    private String formatNotifyTimer() {
        int time = (int) (System.currentTimeMillis() / 1000L - mTime);
        if (mTime == -1L) {
            time = 0;
        }

        String format = CCPAppManager.getContext().getString(R.string.voip_notification_msg);
        return time >= 3600 ? format + String.format(Locale.US, "    %d:%02d:%02d", (time / 3600), (time % 3600 / 60), (time % 60)) :
                format + String.format(Locale.US, "    %d:%02d", (time / 60), (time % 60));
    }

    public void dest(){
        if (this.mTimerUpdate != null) {
            this.mTimerUpdate.stopTimer();
        }
    }


    /**
     * 释放通话
     */
    public final void unInit() {
//        mTime = -1L;
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        this.removeSmallView();
        if (this.mTimerUpdate != null) {
            this.mTimerUpdate.stopTimer();
        }

        if (this.mTimerUpdateNotify != null) {
            this.mTimerUpdateNotify.stopTimer();
        }
        CallNotificationMgr.cancelNotification(40);
        mStatus = 0;
    }

    public void setTime() {
        mTime = -1L;
    }
}
