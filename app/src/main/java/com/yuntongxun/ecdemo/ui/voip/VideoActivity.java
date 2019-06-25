package com.yuntongxun.ecdemo.ui.voip;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.dialog.ECAlertDialog;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.smallwindow.VoiceMeetingService;
import com.yuntongxun.ecsdk.CameraCapability;
import com.yuntongxun.ecsdk.CameraInfo;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.voip.video.ECCaptureTextureView;
import com.yuntongxun.ecsdk.voip.video.ECOpenGlView;
import com.yuntongxun.ecsdk.voip.video.OnCameraInitListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.yuntongxun.ecdemo.ui.voip.ECCallHeadUILayout.formatTime;

/**
 *
 */
public class VideoActivity extends ECVoIPBaseActivity implements View.OnClickListener {

    private static final String TAG = "VideoActivity";


    private RelativeLayout mVideo_root;
    private ImageButton mCamera_switch;
    private ImageView mIv_small;
    private TextView mTv_nickname;
    private FrameLayout mVideo_layout;
    private com.yuntongxun.ecsdk.voip.video.ECOpenGlView mRemote_video_view;
    private com.yuntongxun.ecsdk.voip.video.ECOpenGlView mLocalvideo_view;
    private TextView mChronometer;
    private LinearLayout mLl_controller;
    private ImageView mIv_mute;
    private ImageView mIv_capture;
    private ImageView mIv_hf_hands_free;
    private ImageView mIv_beauty;
    private RelativeLayout mVideo_call_bottom;
    private Button mBtn_video_accept;
    private Button mBtn_video_finish;

    private ECCaptureTextureView mCaptureView;


    FrameLayout mCallRoot;
    private boolean mMaxSizeRemote = false;
    /**
     * 是否开启美颜
     */
    private boolean isBeaulty;
    private Timer mTimer;

    @Override
    protected int getLayoutId() {
        return R.layout.ec_video_call;
    }

    @Override
    protected boolean isEnableSwipe() {
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVideoLayout();

        initCallEvent();


        isCreated = true;
    }

    private void initVideoLayout() {

        mVideo_root = (RelativeLayout) findViewById(R.id.video_root);
        mCamera_switch = (ImageButton) findViewById(R.id.camera_switch);
        mIv_small = (ImageView) findViewById(R.id.iv_small);
        mTv_nickname = (TextView) findViewById(R.id.tv_nickname);
        mVideo_layout = (FrameLayout) findViewById(R.id.Video_layout);
        mRemote_video_view = (com.yuntongxun.ecsdk.voip.video.ECOpenGlView) findViewById(R.id.remote_video_view);
        mLocalvideo_view = (com.yuntongxun.ecsdk.voip.video.ECOpenGlView) findViewById(R.id.localvideo_view);
        mChronometer = (TextView) findViewById(R.id.chronometer);
        mLl_controller = (LinearLayout) findViewById(R.id.ll_controller);
        mIv_mute = (ImageView) findViewById(R.id.iv_mute);
        mIv_capture = (ImageView) findViewById(R.id.iv_capture);
        mIv_hf_hands_free = (ImageView) findViewById(R.id.iv_hf_hands_free);
        mIv_beauty = (ImageView) findViewById(R.id.iv_beauty);
        mVideo_call_bottom = (RelativeLayout) findViewById(R.id.video_call_bottom);
        mBtn_video_accept = (Button) findViewById(R.id.btn_video_accept);
        mBtn_video_finish = (Button) findViewById(R.id.btn_video_finish);

        mIv_small.setOnClickListener(this);
        mIv_beauty.setOnClickListener(this);
        mIv_mute.setOnClickListener(this);
        mIv_capture.setOnClickListener(this);
        mIv_hf_hands_free.setOnClickListener(this);
        mIv_small.setOnClickListener(this);

        mIv_small.setEnabled(false);
        mIv_small.setClickable(false);
        mBtn_video_accept.setOnClickListener(this);
        mBtn_video_finish.setOnClickListener(this);
        mCamera_switch.setOnClickListener(this);

        mRemote_video_view.setGlType(ECOpenGlView.RenderType.RENDER_REMOTE);
        mRemote_video_view.setAspectMode(ECOpenGlView.AspectMode.CROP);

        mLocalvideo_view.setGlType(ECOpenGlView.RenderType.RENDER_PREVIEW);
        mLocalvideo_view.setAspectMode(ECOpenGlView.AspectMode.CROP);
        mLocalvideo_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaxSizeRemote = !mMaxSizeRemote;
                attachGlView();
            }
        });


        mCaptureView = new ECCaptureTextureView(this);
//        setCaptureView(mCaptureView);
        mCaptureView.setOnCameraInitListener(new OnCameraInitListener() {
            @Override
            public void onCameraInit(boolean result) {
                if (!result) {
                    ToastUtil.showMessage("摄像头被占用");
                }
            }
        });


    }

    private void initCallEvent() {

        //隐藏小窗口
        if ((VoiceMeetingService.getInstance().getVoipSmallWindow() != null)) {
            VoiceMeetingService.getMiniWindow().dismiss();
        }
        mCallId = VoiceMeetingService.getInstance().callId;

        if (!VoiceMeetingService.inMeeting()) {
            if (mIncomingCall) {
                // 来电
                mCallId = getIntent().getStringExtra(ECDevice.CALLID);
                mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
                VoiceMeetingService.getInstance().callId = mCallId;
                VoiceMeetingService.getInstance().contactId = mCallNumber;

                mLl_controller.setVisibility(View.GONE);
                mVideo_layout.setVisibility(View.GONE);

                mCamera_switch.setVisibility(View.GONE);
            } else {
                // 呼出
                mCallName = getIntent().getStringExtra(EXTRA_CALL_NAME);
                mCallNumber = getIntent().getStringExtra(EXTRA_CALL_NUMBER);

                mCallId = VoIPCallHelper.makeCall(mCallType, mCallNumber);
                VoiceMeetingService.getInstance().callId = mCallId;

                mLl_controller.setVisibility(View.GONE);

                mVideo_layout.setVisibility(View.VISIBLE);

                mCamera_switch.setVisibility(View.VISIBLE);

            }
        } else {
            if (mCallId != null && !isConnect) {
                mCallNumber = VoiceMeetingService.getInstance().contactId;
                ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);

                initResVideoSuccess();
            }
        }

        mTv_nickname.setText(AvatorUtil.getInstance().getMarkName(mCallNumber));

        ECDevice.getECVoIPSetupManager().setNeedCapture(true);
        ECDevice.getECVoIPSetupManager().controlRemoteVideoEnable(true);
        isOpenCapture = true;
        attachGlView();
    }

    public void setCaptureView(ECCaptureTextureView captureView) {
        ECVoIPSetupManager setUpMgr = ECDevice.getECVoIPSetupManager();
        if (setUpMgr != null) {
//            setUpMgr.setCaptureView(captureView);
        }
        mCaptureView.setVisibility(View.VISIBLE);
        addCaptureView(captureView);
    }

    /**
     * 添加预览到视频通话界面上
     *
     * @param captureView 预览界面
     */
    private void addCaptureView(ECCaptureTextureView captureView) {
        if (mCallRoot != null && captureView != null) {
            mCallRoot.removeView(mCaptureView);
            mCaptureView = null;
            mCaptureView = captureView;
            mCallRoot.addView(captureView, new RelativeLayout.LayoutParams(1, 1));
            mCaptureView.setVisibility(View.VISIBLE);
//            mCaptureView.onResume();
            LogUtil.d(TAG, "CaptureView added");
        }
    }

    private void attachGlView() {
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager == null) {
            return;
        }

        mRemote_video_view.setVisibility(View.VISIBLE);

        if (isConnect) {
            mLocalvideo_view.setVisibility(View.VISIBLE);
        } else {
            mLocalvideo_view.setVisibility(View.GONE);
        }

        mRemote_video_view.setGlType(ECOpenGlView.RenderType.RENDER_REMOTE);
        mRemote_video_view.setAspectMode(ECOpenGlView.AspectMode.CROP);

        mLocalvideo_view.setGlType(ECOpenGlView.RenderType.RENDER_PREVIEW);
        mLocalvideo_view.setAspectMode(ECOpenGlView.AspectMode.CROP);

        if (mMaxSizeRemote) {
            setupManager.setGlDisplayWindow(mLocalvideo_view, mRemote_video_view);
        } else {
            setupManager.setGlDisplayWindow(mRemote_video_view, mLocalvideo_view);
        }
        mLocalvideo_view.onResume();
        mRemote_video_view.onResume();
    }


    private void initResVideoSuccess() {
        isConnect = true;

        mIv_small.setEnabled(true);
        mIv_small.setClickable(true);

        mCaptureView.setVisibility(View.VISIBLE);
        mCamera_switch.setVisibility(View.VISIBLE);

        mLl_controller.setVisibility(View.VISIBLE);
        mVideo_layout.setVisibility(View.VISIBLE);
        mBtn_video_accept.setVisibility(View.GONE);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mChronometer == null) {
                    return;
                }
                mChronometer.post(new Runnable() {
                    @Override
                    public void run() {
                        String time = formatTime(VoiceMeetingService.getDuration());

                        if (mChronometer != null) {
                            mChronometer.setVisibility(View.VISIBLE);
                            mChronometer.setText(time);
                        }
                    }
                });
            }
        }, 1000L, 1000L);

        mMaxSizeRemote = true;
        attachGlView();
    }


    /**
     * 根据状态,修改按钮属性及关闭操作
     */
    private void finishCalling() {
        try {
            stopCallTimer();
            mCaptureView.setVisibility(View.GONE);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isConnect = false;
        }
    }

    /**
     * 停止通话计时
     */
    void stopCallTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void finishCalling(int reason) {
        try {
            mCamera_switch.setVisibility(View.GONE);
            mCaptureView.setVisibility(View.GONE);
            isConnect = false;
            VoIPCallHelper.releaseCall(mCallId);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCaptureView!=null){
//            mCaptureView.setVisibility(View.VISIBLE);
        }
        if (mCallType == ECVoIPCallManager.CallType.VIDEO) {
            String ratio = ECPreferences
                    .getSharedPreferences()
                    .getString(
                            ECPreferenceSettings.SETTINGS_RATIO_CUSTOM.getId(),
                            (String) ECPreferenceSettings.SETTINGS_RATIO_CUSTOM
                                    .getDefaultValue());

            if (!TextUtils.isEmpty(ratio)) {
                String[] arr = ratio.split("\\*");
                int capIndex = getCampIndex(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
                if (mCaptureView != null) {
//                    mCaptureView.setLocalResolutionRatio(Integer.parseInt(arr[2]), capIndex);
                }
            } else {
                if (mCaptureView != null) {
//                    mCaptureView.onResume();
                }
            }
        }
    }

    private int getCampIndex(int width, int height, int index) {
        int sum = 0;
        ECVoIPSetupManager voIPSetupManager = ECDevice.getECVoIPSetupManager();
        if (voIPSetupManager == null) {
            return -1;
        }
        CameraInfo[] infos = voIPSetupManager.getCameraInfos();
        for (int i = 0; i < infos.length; i++) {

            CameraCapability[] arr = infos[i].caps;

            for (int j = 0; j < arr.length; j++) {

                if (index == i && width == arr[j].width && height == arr[j].height) {
                    sum = j;
                }
            }
        }
        return sum;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoIPCallHelper.mHandlerVideoCall = false;
        isCreated = false;
    }

    @Override
    public void onCallProceeding(String callId) {
        if (callId != null && callId.equals(mCallId)) {
        }
    }

    @Override
    public void onCallAlerting(String callId) {
        if (callId != null && callId.equals(mCallId)) {// 等待对方接受邀请...
        }
    }

    @Override
    public void onCallAnswered(String callId) {
        if (callId != null && callId.equals(mCallId)) {


            initResVideoSuccess();
            VoiceMeetingService.getInstance().mDuration = System.currentTimeMillis();
            ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
            mIv_hf_hands_free.setImageResource(R.drawable.shipinliaotian_icon_mianti_high);
        }
    }

    @Override
    public void onMakeCallFailed(String callId, int reason) {
        if (callId != null && callId.equals(mCallId)) {
            finishCalling(reason);
            VoiceMeetingService.getInstance().markVoiceDel();
        }
    }

    @Override
    public void onCallReleased(String callId) {
        if (callId != null && callId.equals(mCallId)) {
            VoIPCallHelper.releaseMuteAndHandFree();
            finishCalling();

            VoiceMeetingService.getInstance().markVoiceDel();
        }
    }

    boolean isOpenCapture = true;

    private boolean isFront = true;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_video_finish:
                doHandUpReleaseCall();
                break;
            case R.id.btn_video_accept:
                VoIPCallHelper.acceptCall(mCallId);
                break;
            case R.id.camera_switch:
                mCamera_switch.setEnabled(false);

                isFront = !isFront;

                if (mCaptureView != null) {
//                    mCaptureView.switchCamera();
                }
                mCamera_switch.setEnabled(true);

                int p = 0 ;
                if(isFront){
                    p =1;
                }else {
                    p = 0;
                }
//                setCaptureView(mCaptureView);
//                ECDevice.getECVoIPSetupManager().setCaptureView(mCaptureView);
                ECDevice.getECVoIPSetupManager().selectCamera(p,2,15, ECVoIPSetupManager.Rotate.ROTATE_AUTO,true);

                break;
            case R.id.iv_mute: //静音

                VoIPCallHelper.setMute();
                boolean mute = VoIPCallHelper.getMute();
                mIv_mute.setImageResource(mute ? R.drawable.jingyin_high : R.drawable.shipinliaotian_icon_jingyin_normal);
                break;
            case R.id.iv_capture://摄像头

                if (isOpenCapture) {
                    ECDevice.getECVoIPSetupManager().setNeedCapture(false);
                    ECDevice.getECVoIPSetupManager().controlRemoteVideoEnable(false);

                    isOpenCapture = false;
                } else {
                    ECDevice.getECVoIPSetupManager().setNeedCapture(true);
                    ECDevice.getECVoIPSetupManager().controlRemoteVideoEnable(true);
                    isOpenCapture = true;
                }
                mIv_capture.setImageResource(isOpenCapture ? R.drawable.shexiangtouhigh : R.drawable.shexiangtou);

                break;
            case R.id.iv_hf_hands_free://免提
                VoIPCallHelper.setHandFree();
                boolean handFree2 = VoIPCallHelper.getHandFree();
                mIv_hf_hands_free.setImageResource(handFree2 ? R.drawable.shipinliaotian_icon_mianti_high : R.drawable.shipinliaotian_icon_mianti_normal);

                break;

            case R.id.iv_beauty: //美颜

                if (isBeaulty) {
                    ECDevice.getECVoIPSetupManager().setBeautyFilter(false);
                    isBeaulty = false;
                } else {
                    ECDevice.getECVoIPSetupManager().setBeautyFilter(true);
                    isBeaulty = true;
                }
                mIv_beauty.setImageResource(isBeaulty ? R.drawable.v_meiyan : R.drawable.shipinliaotian_icon_meiyan_normal);
                break;
            case R.id.iv_small: // 缩小
                VoiceMeetingService.getInstance().onMinimizeVoip(true, true);
                finish();
                break;

            default:
                onKeyBordClick(v.getId());
                break;
        }
    }

    protected void showInputCodeDialog(String title, String message, final boolean isLandCall) {
        View view = View.inflate(this, R.layout.dialog_edit_context, null);
        final EditText editText = (EditText) view.findViewById(R.id.sendrequest_content);
        ((TextView) view.findViewById(R.id.sendrequest_tip)).setText(message);
        ECAlertDialog dialog = ECAlertDialog.buildAlert(this, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleInput(editText, isLandCall);
            }
        });
        dialog.setContentView(view);
        dialog.setTitle(title);
        dialog.show();
    }

    protected void showInputCodeDialog2(String title, String message, final boolean isLandCall) {
        View view = View.inflate(this, R.layout.dialog_edit_context, null);
        final EditText editText = (EditText) view.findViewById(R.id.sendrequest_content);
        ((TextView) view.findViewById(R.id.sendrequest_tip)).setText(message);
        ECAlertDialog dialog = ECAlertDialog.buildAlert(this, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleInput2(editText, isLandCall);
            }
        });
        dialog.setContentView(view);
        dialog.setTitle(title);
        dialog.show();
    }

    protected void handleInput(android.widget.EditText editText, boolean isLandCall) {

        if (editText != null) {
            String text = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                ECDevice.getECVoIPCallManager().inviteJoinThreePartConf(mCallId, text, CCPAppManager.getUserId(), new ECVoIPCallManager.OnThreeInviteListener() {
                    @Override
                    public void onResult(ECError ecError) {

                        Log.e("aa", ecError.toString());
                    }
                });
            }
        }

    }

    ;

    protected void handleInput2(android.widget.EditText editText, boolean isLandCall) {

        if (editText != null) {
            String text = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                JSONObject o = new JSONObject();
                try {
                    o.put("callSid", map.get("sid"));
                    o.put("number", text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ECDevice.getECVoIPCallManager().sendCmdToRest(6, o.toString(), "", new ECVoIPCallManager.OnSendCmdListener() {
                    @Override
                    public void onResult(ECError ecError) {

                    }
                });
            }
        }

    }

    ;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    private void onKeyBordClick(int id) {
        switch (id) {
            case R.id.zero: {
                keyPressed(KeyEvent.KEYCODE_0);
                return;
            }
            case R.id.one: {
                keyPressed(KeyEvent.KEYCODE_1);
                return;
            }
            case R.id.two: {
                keyPressed(KeyEvent.KEYCODE_2);
                return;
            }
            case R.id.three: {
                keyPressed(KeyEvent.KEYCODE_3);
                return;
            }
            case R.id.four: {
                keyPressed(KeyEvent.KEYCODE_4);
                return;
            }
            case R.id.five: {
                keyPressed(KeyEvent.KEYCODE_5);
                return;
            }
            case R.id.six: {
                keyPressed(KeyEvent.KEYCODE_6);
                return;
            }
            case R.id.seven: {
                keyPressed(KeyEvent.KEYCODE_7);
                return;
            }
            case R.id.eight: {
                keyPressed(KeyEvent.KEYCODE_8);
                return;
            }
            case R.id.nine: {
                keyPressed(KeyEvent.KEYCODE_9);
                return;
            }
            case R.id.star: {
                keyPressed(KeyEvent.KEYCODE_STAR);
                return;
            }
            case R.id.pound: {
                keyPressed(KeyEvent.KEYCODE_POUND);
                return;
            }
        }
    }

    private EditText mDmfInput;

    void keyPressed(int keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDmfInput.getText().clear();
        mDmfInput.onKeyDown(keyCode, event);
        sendDTMF(mDmfInput.getText().toString().toCharArray()[0]);
    }


    protected void doHandUpReleaseCall() {

        // Hang up the video call...
        LogUtil.d(TAG,
                "[VideoActivity] onClick: Voip talk hand up, CurrentCallId " + mCallId);
        try {
            if (mCallId != null) {

                if (mIncomingCall && !isConnect) {
                    VoIPCallHelper.rejectCall(mCallId);
                } else {
                    VoIPCallHelper.releaseCall(mCallId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isConnect) {
            finish();
        }
    }

    private void setupKeypad() {
        /** Setup the listeners for the buttons */
        findViewById(R.id.zero).setOnClickListener(this);
        findViewById(R.id.one).setOnClickListener(this);
        findViewById(R.id.two).setOnClickListener(this);
        findViewById(R.id.three).setOnClickListener(this);
        findViewById(R.id.four).setOnClickListener(this);
        findViewById(R.id.five).setOnClickListener(this);
        findViewById(R.id.six).setOnClickListener(this);
        findViewById(R.id.seven).setOnClickListener(this);
        findViewById(R.id.eight).setOnClickListener(this);
        findViewById(R.id.nine).setOnClickListener(this);
        findViewById(R.id.star).setOnClickListener(this);
        findViewById(R.id.pound).setOnClickListener(this);
    }

    @Override
    public void onMakeCallback(ECError arg0, String arg1, String arg2) {

    }

    public boolean isCreated = false;

    @Override
    protected void onNewIntent(Intent intent) {

        if (!isCreated) {
            setIntent(intent);
            super.onNewIntent(intent);
            initVideoLayout();
            initCallEvent();

        }
    }

    /**
     * 远端视频分辨率到达，标识收到视频图像
     *
     * @param videoRatio 视频分辨率信息
     */
    @Override
    public void onVideoRatioChanged(VideoRatio videoRatio) {
        super.onVideoRatioChanged(videoRatio);
        /*if(mVideoView != null && videoRatio != null) {
            mVideoView.getHolder().setFixedSize(videoRatio.getWidth() , videoRatio.getHeight());
        }*/
        if (videoRatio == null) {
            return;
        }
        int width = videoRatio.getWidth();
        int height = videoRatio.getHeight();
        if (width == 0 || height == 0) {
            LogUtil.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mRemote_video_view.setVisibility(View.VISIBLE);
        mRemote_video_view.onResume();
        if (width > height) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int mSurfaceViewWidth = dm.widthPixels;
            int mSurfaceViewHeight = dm.heightPixels;
            int w = mSurfaceViewWidth * height / width;
            int margin = 0;
            LogUtil.d(TAG, "margin:" + margin);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, margin, 0, margin);
            mRemote_video_view.setLayoutParams(lp);
        }
    }

    private Map map = new HashMap<String, String>();


//    public void setDialerpadUI() {
//        daiLayout.setVisibility(daiLayout.getVisibility() != View.GONE ? View.GONE : View.VISIBLE);
//    }

}
