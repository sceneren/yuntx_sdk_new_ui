//package com.yuntongxun.ecdemo.ui.chatting;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.AnimatorSet;
//import android.animation.ObjectAnimator;
//import android.graphics.Bitmap;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.annotation.RequiresApi;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.SurfaceHolder;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.VideoView;
//
//import com.yuntongxun.ecdemo.R;
//import com.yuntongxun.ecdemo.common.utils.ToastUtil;
//import com.yuntongxun.ecdemo.recordvideo.CameraInterface;
//import com.yuntongxun.ecdemo.recordvideo.CaptureButton;
//import com.yuntongxun.ecdemo.recordvideo.FoucsView;
//import com.yuntongxun.ecdemo.recordvideo.listener.CaptureLisenter;
//import com.yuntongxun.ecdemo.recordvideo.listener.FirstFoucsLisenter;
//import com.yuntongxun.ecdemo.recordvideo.util.ScreenUtils;
//import com.yuntongxun.ecdemo.ui.BaseActivity;
//
//import java.io.IOException;
//
//import butterknife.BindView;
//import butterknife.OnClick;
//
//import static android.R.attr.layout_width;
//import static android.view.View.GONE;
//import static android.view.View.INVISIBLE;
//import static android.view.View.VISIBLE;
//
///**
// * Created by zlk on 2017/8/17.
// */
//
//public class CameraActivity extends BaseActivity implements CameraInterface.CamOpenOverCallback ,SurfaceHolder.Callback {
//
//    @BindView(R.id.foucsView)
//    FoucsView mFoucsView;
//
//    @BindView(R.id.video_view)
//    VideoView videoView;
//    @BindView(R.id.iv_back)
//    ImageView ivBack;
//    @BindView(R.id.iv_reverse)
//    ImageView ivReverse;
//    @BindView(R.id.tv_play_back)
//    TextView tvPlayBack;
//    @BindView(R.id.iv_recoder)
//    CaptureButton btn_capture;
//    @BindView(R.id.tv_send)
//    TextView tvSend;
//    @BindView(R.id.txt_tip)
//    TextView txt_tip;
//
//
//    private boolean firstTouch = true;
//    private float firstTouchLength = 0;
//
//    private boolean isBorrow = false;
//    private boolean switching = false;
//    private float screenProp;
//    private int screenWidth;
//    private int screenHeight;
//
//    //拍照按钮监听
//    private CaptureLisenter captureLisenter;
//    private boolean isFirst = true;
//
//    private int CAMERA_STATE = -1;
//    private static final int STATE_IDLE = 0x010;
//    private static final int STATE_RUNNING = 0x020;
//    private static final int STATE_WAIT = 0x030;
//    private boolean stopping = false;
//    private boolean onlyPause = false;
//
//
//    //视频URL
//    private String videoUrl;
//    private MediaPlayer mMediaPlayer;
//
//    @Override
//    protected void initView(Bundle savedInstanceState) {
//
//    }
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_camera;
//    }
//
//    @Override
//    protected void initWidgetAciotns() {
//        screenWidth = ScreenUtils.getScreenWidth(mContext);
//        screenHeight = ScreenUtils.getScreenHeight(mContext);
//        screenProp = screenHeight / screenWidth;
//
//        btn_capture.setDuration(10 * 1000);
//        btn_capture.setCaptureLisenter(new CaptureLisenter() {
//            @Override
//            public void takePictures() {
//            }
//
//            @Override
//            public void recordShort(long time) {
//                if (CAMERA_STATE != STATE_RUNNING && stopping) {
//                    return;
//                }
//                stopping = true;
//                setTextWithAnimation("录制时间过短");
//                CameraInterface.getInstance().setSwitchView(ivReverse);
//                videoView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        CameraInterface.getInstance().stopRecord(true, new
//                                CameraInterface.StopRecordCallback() {
//                                    @Override
//                                    public void recordResult(String url, Bitmap firstFrame) {
//                                        Log.i(TAG, "Record Stopping ...");
//                                        btn_capture.isRecord(false);
//                                        CAMERA_STATE = STATE_IDLE;
//                                        stopping = false;
//                                        isBorrow = false;
//                                    }
//                                });
//                    }
//                }, 1500 - time);
//            }
//
//            @Override
//            public void recordStart() {
//                if (CAMERA_STATE != STATE_IDLE && stopping) {
//                    return;
//                }
//
//                ivReverse.setVisibility(GONE);
//                btn_capture.isRecord(true);
//                isBorrow = true;
//                CAMERA_STATE = STATE_RUNNING;
//                mFoucsView.setVisibility(INVISIBLE);
//                CameraInterface.getInstance().startRecord(videoView.getHolder().getSurface(), new CameraInterface
//                        .ErrorCallback() {
//                    @Override
//                    public void onError() {
//                        Log.i("CJT", "startRecorder error");
//                        btn_capture.isRecord(false);
//                        CAMERA_STATE = STATE_WAIT;
//                        stopping = false;
//                        isBorrow = false;
//                    }
//                });
//            }
//
//            @Override
//            public void recordEnd(long time) {
//                CameraInterface.getInstance().stopRecord(false, new CameraInterface.StopRecordCallback() {
//                    @Override
//                    public void recordResult(final String url, Bitmap firstFrame) {
//                        CAMERA_STATE = STATE_WAIT;
//                        videoUrl = url;
//                        firstFrame = firstFrame;
//                        playRecordVideo(url);
//                    }
//                });
//            }
//
//            @Override
//            public void recordZoom(float zoom) {
//                CameraInterface.getInstance().setZoom(zoom, CameraInterface.TYPE_RECORDER);
//            }
//
//            @Override
//            public void recordError() {
//                //错误回调
//                ToastUtil.showMessage("录制失败");
//            }
//        });
//    }
//
//    private void playRecordVideo(final String url) {
//        new Thread(new Runnable() {
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//            @Override
//            public void run() {
//                try {
//                    if (mMediaPlayer == null) {
//                        mMediaPlayer = new MediaPlayer();
//                    } else {
//                        mMediaPlayer.reset();
//                    }
//                    Log.i("CJT", "URL = " + url);
//                    mMediaPlayer.setDataSource(url);
//                    mMediaPlayer.setSurface(videoView.getHolder().getSurface());
//                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
//                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
//                            .OnVideoSizeChangedListener() {
//                        @Override
//                        public void
//                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
//                                    .getVideoHeight());
//                        }
//                    });
//                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
//                            mMediaPlayer.start();
//                        }
//                    });
//                    mMediaPlayer.setLooping(true);
//                    mMediaPlayer.prepare();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//
//    /**
//     * TextureView resize
//     */
//    public void updateVideoViewSize(float videoWidth, float videoHeight) {
//        if (videoWidth > videoHeight) {
//            FrameLayout.LayoutParams videoViewParam;
//            int height = (int) ((videoHeight / videoWidth) *screenWidth);
//            videoViewParam = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                    height);
//            videoViewParam.gravity = Gravity.CENTER;
////            videoViewParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//            videoView.setLayoutParams(videoViewParam);
//        }
//    }
//
//
//    public void setTextWithAnimation(String tip) {
//        txt_tip.setText(tip);
//        ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 0f, 1f, 1f, 0f);
//        animator_txt_tip.setDuration(2500);
//        animator_txt_tip.start();
//    }
//
//
//    public void startAlphaAnimation() {
//        if (isFirst) {
//            ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 1f, 0f);
//            animator_txt_tip.setDuration(500);
//            animator_txt_tip.start();
//            isFirst = false;
//        }
//    }
//
//    public void startTypeBtnAnimator() {
//        //拍照录制结果后的动画
//        tvPlayBack.setVisibility(VISIBLE);
//        tvSend.setVisibility(VISIBLE);
//
//        ObjectAnimator animator_cancel = ObjectAnimator.ofFloat(tvPlayBack, "translationX", layout_width / 4, 0);
//        animator_cancel.setDuration(200);
//        animator_cancel.start();
//        ObjectAnimator animator_confirm = ObjectAnimator.ofFloat(tvSend, "translationX", -layout_width / 4, 0);
//        animator_confirm.setDuration(200);
//        animator_confirm.start();
//        animator_confirm.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                tvPlayBack.setClickable(true);
//                tvSend.setClickable(true);
//            }
//        });
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        //全屏显示
//        if (Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        } else {
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(option);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        CameraInterface.getInstance().registerSensorManager(mContext);
//        CameraInterface.getInstance().setSwitchView(ivReverse);
//        if (onlyPause) {
//            new Thread() {
//                @Override
//                public void run() {
//                    CameraInterface.getInstance().doOpenCamera(CameraActivity.this);
//                }
//            }.start();
//            mFoucsView.setVisibility(INVISIBLE);
////            }
//        }
//
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        onlyPause = true;
//        CameraInterface.getInstance().unregisterSensorManager(mContext);
//        CameraInterface.getInstance().doStopCamera();
//
//    }
//
//    public void setCaptureLisenter(CaptureLisenter captureLisenter) {
//        this.captureLisenter = captureLisenter;
//    }
//
//    @OnClick({R.id.iv_back, R.id.iv_reverse, R.id.tv_play_back, R.id.tv_send})
//    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.iv_back:
//                finish();
//                break;
//            case R.id.iv_reverse:
//                if (isBorrow || switching) {
//                    return;
//                }
//                switching = true;
//                new Thread() {
//                    /**
//                     * switch camera
//                     */
//                    @Override
//                    public void run() {
//                        CameraInterface.getInstance().switchCamera(CameraActivity.this);
//                    }
//                }.start();
//
//                break;
//            case R.id.tv_play_back:
//
//                break;
//            case R.id.tv_send:
//                break;
//        }
//    }
//
//    private void recoderVideo() {
//
//    }
//
//
//    @Override
//    public void cameraHasOpened() {
//        CameraInterface.getInstance().doStartPreview(videoView.getHolder(), screenProp, new FirstFoucsLisenter() {
//            @Override
//            public void onFouce() {
//                mFoucsView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        setFocusViewWidthAnimation(screenWidth / 2, screenHeight / 2);
//                    }
//                });
//            }
//        });
//    }
//
//    @Override
//    public void cameraSwitchSuccess() {
//        switching = false;
//    }
//
//    /**
//     * focusview animation
//     */
//    private void setFocusViewWidthAnimation(float x, float y) {
//        if (isBorrow) {
//            return;
//        }
//
//        mFoucsView.setVisibility(VISIBLE);
//        if (x < mFoucsView.getWidth() / 2) {
//            x = mFoucsView.getWidth() / 2;
//        }
//        if (x > layout_width - mFoucsView.getWidth() / 2) {
//            x = layout_width - mFoucsView.getWidth() / 2;
//        }
//        if (y < mFoucsView.getWidth() / 2) {
//            y = mFoucsView.getWidth() / 2;
//        }
//
//        CameraInterface.getInstance().handleFocus(mContext, x, y, new CameraInterface.FocusCallback() {
//            @Override
//            public void focusSuccess() {
//                mFoucsView.setVisibility(INVISIBLE);
//            }
//        });
//
//        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
//        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
//
//        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
//        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
//        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.3f, 1f, 0.3f, 1f, 0.3f, 1f);
//        AnimatorSet animSet = new AnimatorSet();
//        animSet.play(scaleX).with(scaleY).before(alpha);
//        animSet.setDuration(400);
//        animSet.start();
//    }
//
//
//    /**
//     * handler touch focus
//     */
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                if (event.getPointerCount() == 1) {
//                    //显示对焦指示器
//                    setFocusViewWidthAnimation(event.getX(), event.getY());
//                }
//                if (event.getPointerCount() == 2) {
//                    Log.i("CJT", "ACTION_DOWN = " + 2);
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (event.getPointerCount() == 1) {
//                    firstTouch = true;
//                }
//                if (event.getPointerCount() == 2) {
//                    //第一个点
//                    float point_1_X = event.getX(0);
//                    float point_1_Y = event.getY(0);
//                    //第二个点
//                    float point_2_X = event.getX(1);
//                    float point_2_Y = event.getY(1);
//
//                    float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math.pow(point_1_Y -
//                            point_2_Y, 2));
//
//                    if (firstTouch) {
//                        firstTouchLength = result;
//                        firstTouch = false;
//                    }
//                    if ((int) (result - firstTouchLength) / 40 != 0) {
//                        firstTouch = true;
//                        CameraInterface.getInstance().setZoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE);
//                    }
//                    Log.i("CJT", "result = " + (result - firstTouchLength));
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                firstTouch = true;
//                break;
//        }
//        return true;
//    }
//
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        Log.i("CJT", "surfaceCreated");
//        new Thread() {
//            @Override
//            public void run() {
//                CameraInterface.getInstance().doOpenCamera(CameraActivity.this);
//            }
//        }.start();
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        onlyPause = false;
//        Log.i("CJT", "surfaceDestroyed");
//        CameraInterface.getInstance().doDestroyCamera();
//    }
//}
