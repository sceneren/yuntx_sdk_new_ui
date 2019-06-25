package com.yuntongxun.ecdemo.ui.smallwindow;

import android.graphics.Point;
import android.text.TextUtils;

import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;

/**
 * Created with Android Studio IDEA.
 *
 * @author WJ
 * @version 1.0
 * @since 2016/10/19 10:07
 */
public class VoiceMeetingService {
    private static VoiceMeetingService ourInstance = new VoiceMeetingService();
    private int mUIType;
    /**
     * 当前会议进行的时间
     */
    public long mDuration = -1;


    public ECVoIPCallManager.CallType callType;
    public String nickname;
    public String contactId;
    public String callId;
    public boolean flag;
    public boolean outgoingCall;

    public boolean callbackCall;
    private ECCaptureView mCaptureView;

    private VoiceMeetingService() {
    }

    public static VoiceMeetingService getInstance() {
        if (ourInstance == null) {
            ourInstance = new VoiceMeetingService();
        }
        return ourInstance;
    }

    /**
     * 是否处于小窗口模式
     *
     * @return
     */
    static boolean isMiniWindowsing() {
        return ourInstance.mUIType == 2;
    }

    /**
     * 语音通话小窗口
     */
    private MeetingMiniManager mMeetingMiniManager;

    Point mPoint;
    /**
     * 小窗
     */
    private VoipSmallWindow mVoipSmallWindow;

    private boolean mOnlyHidenVoip;


    /**
     * 进入小窗口模式
     *
     * @param miniOnlyHidenVoip 如果是true就是立即显示浮动窗口，否则只有接通才显示
     */
    public void onMinimizeVoip(boolean miniOnlyHidenVoip, boolean isVideo) {
        mOnlyHidenVoip = miniOnlyHidenVoip;

        if (mVoipSmallWindow != null) {
            mVoipSmallWindow.unInit();
            mVoipSmallWindow = null;
        }
        showMiniCallWindow(isVideo);
    }

    /**
     * 显示浮动窗口
     *
     * @param isVideo
     */
    private void showMiniCallWindow(boolean isVideo) {

        VoipSmallWindow mAbstractVoip = getVoipSmallWindow();
        if (mAbstractVoip != null) {
            if (mOnlyHidenVoip) {
                mAbstractVoip.mTime = ourInstance.mDuration / 1000;
                mAbstractVoip.showSmallWindow(isVideo ? VoipSmallWindow.STATE_SHOW_VIDEO_WINDOW : VoipSmallWindow.STATE_SHOW_VOICE_WINDOW);
//                setCaptureView();
                return;
            }
            // 如果没有正在通话是没有必要显示小窗口的
            // 正在呼叫过程中的时候需要状态栏提醒
            if (!mOnlyHidenVoip) {
                mAbstractVoip.showSmallWindow(VoipSmallWindow.STATE_SHOW_NOTIFY);
            }
        }
    }

    /**
     * 设置通话图像预览界面
     */
    private void setCaptureView() {

        if (mCaptureView == null) {
            mCaptureView = new ECCaptureView(CCPAppManager.getContext());
        }
        getVoipSmallWindow().setCaptureView(mCaptureView);
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager != null) {
            setupManager.setCaptureView(mCaptureView);
        }

    }



    /**
     * 初始化聊天小窗口
     *
     * @return 小窗口
     */
    public VoipSmallWindow getVoipSmallWindow() {
        if (mVoipSmallWindow == null) {
            mVoipSmallWindow = new VoipSmallWindow();
        }
        return mVoipSmallWindow;
    }


    public void setTime(){
        if (mVoipSmallWindow != null) {
            mVoipSmallWindow.setTime();
        }
    }
    public void dest(){
        if (mVoipSmallWindow != null) {
            mVoipSmallWindow.dest();
        }
    }


    /**
     * 返回小窗口管理器
     *
     * @return 小窗口管理器
     */
    public static MeetingMiniManager getMiniWindow() {
        if (ourInstance.mMeetingMiniManager == null) {
            ourInstance.mMeetingMiniManager = new MeetingMiniManager();
        }
        return ourInstance.mMeetingMiniManager;
    }


    /**
     * 获取会议时长
     *
     * @return 多少s
     */
    public static long getDuration() {
        if (ourInstance == null || ourInstance.mDuration == -1) {
            return 0L;
        }
        return (System.currentTimeMillis() - ourInstance.mDuration) / 1000;
    }


    /**
     * 会议结束
     */
    public void markVoiceDel() {
        getVoipSmallWindow().unInit();
        if (ourInstance != null) {

            ourInstance.callId = "";
            ourInstance.mDuration = -1;
            VoiceMeetingService.getMiniWindow().dismiss();

        }

    }

    /**
     * 当前是否正在会议中
     *
     * @return 是否正在会议中
     */
    public static boolean inMeeting() {
        return ourInstance != null && !TextUtils.isEmpty(ourInstance.callId) && ourInstance.mDuration != -1;
    }

}
