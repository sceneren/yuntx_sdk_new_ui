package com.yuntongxun.ecdemo.ui.chatting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.CameraParamUtil;
import com.yuntongxun.ecdemo.common.utils.CommomUtil;
import com.yuntongxun.ecdemo.common.utils.FileUtils;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.ui.CheckPermission;
import com.yuntongxun.ecdemo.ui.ECSuperActivity;

import java.io.File;
import java.io.IOException;


public class VideoRecordActivity extends ECSuperActivity implements
        OnClickListener, SurfaceHolder.Callback, OnInfoListener,
        OnErrorListener, Camera.PreviewCallback {
    private static final String TAG = "VideoRecordActivity";
    private VideoView mVideoView;
    private Button btn_switch;
    private int frontCamera = 0;// 0是backCamera 1是frontCamera
    private MediaRecorder mediaRecorder;
    private Button btn_start;
    private Button btn_ok;
    private File tempFile;
    private Chronometer chronometer;
    private View btn_play;
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private ImageView imageview;

    //长按后处理的逻辑Runnable
    private LongPressRunnable longPressRunnable;

    private MediaPlayer mMediaPlayer;
    private ProgressBar progressBar;

    int progress = 0;
    //空状态
    public static final int STATE_NULL = 0x000;
    //点击后松开时候的状态
    public static final int STATE_UNPRESS_CLICK = 0X002;
    //点击按下时候的状态
    public static final int STATE_PRESS_CLICK = 0X001;
    //长按按下时候的状态
    public static final int STATE_PRESS_LONG_CLICK = 0x003;
    //长按后松开时候的状态
    public static final int STATE_UNPRESS_LONG_CLICK = 0x004;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == WHAT_TIME) {
                progressBar.setProgress(progress++);

                sendEmptyMessageDelayed(WHAT_TIME, 1000);

            }
        }
    };
    private int WHAT_TIME = 0x2;
    private long base;
    private RelativeLayout rl_bottom;
    private FrameLayout fl_title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = 0;
        endTime = 0;
        if (!initCamera()) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        stopRecording();
    }


//	分辨率 240x320
//	帧率 25-30fbps
//	码率 300 - 500kbps
//	len < 500kb
//	rotate 循环

    @SuppressLint("NewApi")
    private boolean initCamera() {
        try {
            if (frontCamera == 0) {
                mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
            } else {
                mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
            }
            Camera.Parameters camParams = mCamera.getParameters();
            mCamera.lock();
            mSurfaceHolder = mVideoView.getHolder();
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mCamera.setDisplayOrientation(90);
        } catch (RuntimeException re) {
            Log.v(TAG, "Could not initialize the Camera");
            re.printStackTrace();
            return false;
        }
        return true;
    }

    //Touch_Event_Down时候记录的Y值
    float event_Y;

    //当前按钮状态
    private int state;
    private boolean isRecorder = false;

    private void initViews() {
        longPressRunnable = new LongPressRunnable();


        ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);


        rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        fl_title = (FrameLayout) findViewById(R.id.fl_title);

        progressBar = (ProgressBar) findViewById(R.id.pb);
        btn_switch = (Button) findViewById(R.id.switch_btn);
        btn_switch.setOnClickListener(this);
        imageview = (ImageView) findViewById(R.id.imageview);
        mVideoView = (VideoView) findViewById(R.id.surface_video_record);

        mSurfaceHolder = mVideoView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        chronometer = (Chronometer) findViewById(R.id.chronometer);
        btn_play = findViewById(R.id.btn_play);
        btn_play.setOnClickListener(this);
        btn_start = (Button) findViewById(R.id.start);
        btn_ok = (Button) findViewById(R.id.ok);
        btn_ok.setOnClickListener(this);

        btn_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    //
                    case MotionEvent.ACTION_DOWN:
                        if (event.getPointerCount() > 1) {
                            break;
                        }
                        //记录Y值
                        event_Y = event.getY();
                        //修改当前状态为点击按下
                        state = STATE_PRESS_CLICK;
                        //当前状态能否录制
                        //判断按钮状态是否为可录制状态
                        if (!isRecorder) {
                            //同时延长500启动长按后处理的逻辑Runnable
//                            btn_start.postDelayed(longPressRunnable, 500);
                            btn_start.post(longPressRunnable);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        stopRecording();
                        break;
                }
                return true;
            }
        });


    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }


    /**
     * LongPressRunnable
     */
    private class LongPressRunnable implements Runnable {
        @Override
        public void run() {
            //如果按下后经过2000毫秒则会修改当前状态为长按状态
            state = STATE_PRESS_LONG_CLICK;
            if (CheckPermission.getRecordState() != CheckPermission.STATE_SUCCESS) {
                ToastUtil.showMessage("录音失败，请先授权");
                state = STATE_NULL;
                return;
            }

            // 开始拍摄 按钮从开始拍摄 切到停止拍摄
            startRecording();

            chronometer.setBase(SystemClock.elapsedRealtime());

            base = SystemClock.elapsedRealtime();

            chronometer.start();


            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(WHAT_TIME);
                }
            }, 1000);

            btn_switch.setVisibility(View.INVISIBLE);
            btn_play.setVisibility(View.INVISIBLE);

            btn_ok.setVisibility(View.INVISIBLE);

        }
    }


    @SuppressLint("NewApi")
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            initCamera();
        }
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.v(TAG, "Could not start the preview");
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.v(TAG, "surfaceChanged: Width x Height = " + width + "x" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            releaseCamera();
        }
        Log.v(TAG, "surfaceDestroyed ");
    }

    @SuppressLint("NewApi")
    public void startRecording() {
        progress = 0;
        try {
            if (mediaRecorder == null) {
                initRecorder();
            }
            mediaRecorder.setOnInfoListener(this);
            mediaRecorder.setOnErrorListener(this);
            mediaRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
            stopRecording();
            finish();


        }
    }

    @SuppressLint("NewApi")
    private void initRecorder() {
        if (mCamera == null) {
            initCamera();
        }

        mVideoView.setVisibility(View.VISIBLE);
        imageview.setVisibility(View.GONE);
        btn_start.setVisibility(View.VISIBLE);

        mCamera.stopPreview();
        mediaRecorder = new MediaRecorder();
//		mediaRecorder.setCaptureRate(25.0f);
//		mediaRecorder.setVideoEncodingBitRate(600*600);//3ms  98k

        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        // init
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


        if (frontCamera == 1) {
            mediaRecorder.setOrientationHint(270);
        } else {
            mediaRecorder.setOrientationHint(90);// 视频旋转
        }
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);


        // other work
        tempFile = CommomUtil.TackVideoFilePath();
        // 设置视频编码方式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        // 设置音频编码方式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(tempFile.getPath());

        mediaRecorder.setMaxDuration(30000);
//        mediaRecorder.setVideoSize(1280,720);
        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        try {
            mediaRecorder.prepare();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopRecording() {

        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);

            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            releaseRecorder();
        }

        handler.removeCallbacksAndMessages(null);

        if (mCamera != null) {
            if (null != mCamera) {
                try {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    //这句要在stopPreview后执行，不然会卡顿或者花屏
                    mCamera.setPreviewDisplay(null);
                    mCamera.release();
                    mCamera = null;
                    Log.i(TAG, "=== Stop Camera ===");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //
//        mVideoView.setVisibility(View.GONE);
        btn_switch.setVisibility(View.VISIBLE);
        btn_play.setVisibility(View.VISIBLE);

        btn_ok.setVisibility(View.VISIBLE);
        chronometer.stop();

//        imageview.setVisibility(View.VISIBLE);
//        if (tempFile == null)
//            return;

//        Bitmap createVideoThumbnail = FileUtils //生成缩图
//                .createVideoThumbnail(tempFile.getAbsolutePath());
//        if (createVideoThumbnail != null) {
//            imageview.setImageBitmap(createVideoThumbnail);
//            // saveBitmapFile(createVideoThumbnail);
//        }

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void releaseRecorder() {

        try {
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            e.printStackTrace();
            mediaRecorder = null;
            mediaRecorder = new MediaRecorder();
            Log.i("CJT", "stop RuntimeException");
        } catch (Exception e) {
            e.printStackTrace();
            mediaRecorder = null;
            mediaRecorder = new MediaRecorder();
            Log.i("CJT", "stop Exception");
        } finally {
            if (mediaRecorder != null) {
                mediaRecorder.release();
            }
            mediaRecorder = null;
            isRecorder = false;
        }

    }

    private long startTime = 0;
    private long endTime = 0;

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.start || v.getId() == R.id.stop) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            } else {

                endTime = System.currentTimeMillis();

                if (endTime - startTime < 1500) {
                    return;
                }
                startTime = System.currentTimeMillis();

            }

        }

        switch (v.getId()) {

            case R.id.switch_btn:
                flipit();
                break;
            case R.id.iv_back:
                hideSoftKeyboard();
                stopRecording();
                finish();
                break;
            case R.id.btn_play:
                if (CheckPermission.getRecordState() != CheckPermission.STATE_SUCCESS) {
                    ToastUtil.showMessage("录音失败，请先授权");
                    state = STATE_NULL;
                    return;
                }
                if (tempFile != null) {

                    snedFilePrevieIntent(tempFile.getAbsolutePath());
                }
                break;
            case R.id.start:
                break;
            case R.id.ok:
                handleVideoSend();
                break;
            case R.id.stop:
                // 点击停止拍摄 状态切换到重新拍照 和预览
//                stopRecording();
                break;
        }

    }

    private void handleVideoSend() {

        if (tempFile == null) {
            return;
        }


        Intent intent = new Intent();
        intent.putExtra("file_name", tempFile.getName());
        intent.putExtra("file_url", tempFile.getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_chat_videorecord;
    }

    @SuppressLint("NewApi")
    public void flipit() {
        // mCamera is the Camera object
        if (mCamera == null) {
            return;
        }
        if (Camera.getNumberOfCameras() >= 2) {
            btn_switch.setEnabled(false);
            if (null != mCamera) {
                try {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    //这句要在stopPreview后执行，不然会卡顿或者花屏
                    mCamera.setPreviewDisplay(null);
                    mCamera.release();
                    mCamera = null;
                    Log.i(TAG, "=== Stop Camera ===");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // "which" is just an integer flag
            switch (frontCamera) {
                case 0:
                    mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
                    frontCamera = 1;
                    break;
                case 1:
                    mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
                    frontCamera = 0;
                    break;
            }

            if (Build.VERSION.SDK_INT > 17 && this.mCamera != null) {
                try {
                    this.mCamera.enableShutterSound(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("CJT", "enable shutter_sound sound faild");
                }
            }

            doStartPreview();
            btn_switch.setEnabled(true);
        }
    }

    private void doStartPreview() {


        if (mCamera != null) {
            try {
                Camera.Parameters mParams = mCamera.getParameters();


                mParams.setPreviewSize(mVideoView.getWidth(), mVideoView.getHeight());


                if (CameraParamUtil.getInstance().isSupportedFocusMode(
                        mParams.getSupportedFocusModes(),
                        Camera.Parameters.FOCUS_MODE_AUTO)) {
                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                if (CameraParamUtil.getInstance().isSupportedPictureFormats(mParams.getSupportedPictureFormats(),
                        ImageFormat.JPEG)) {
                    mParams.setPictureFormat(ImageFormat.JPEG);
                    mParams.setJpegQuality(100);
                }
                mCamera.setParameters(mParams);
                mParams = mCamera.getParameters();
                //SurfaceView
                mCamera.setPreviewDisplay(mVideoView.getHolder());
                //浏览角度
                mCamera.setDisplayOrientation(90);
//              //每一帧回调
                mCamera.setPreviewCallback(this);
//              //启动浏览
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
//                mCamera.stopPreview();
            }
        }


        try {
            // Camera.Parameters parameters = mCamera.getParameters();
            mCamera.lock();
            mCamera.setDisplayOrientation(90);
            // mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mVideoView.getHolder());
            mCamera.startPreview();
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.i(TAG, "got a recording event");
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.i(TAG, "...max duration reached");
            stopRecording();


//            mVideoView.setVisibility(View.GONE);
            btn_switch.setVisibility(View.INVISIBLE);
            btn_play.setVisibility(View.VISIBLE);


            chronometer.stop();
            if (tempFile == null) {
                return;
            }

            imageview.setVisibility(View.VISIBLE);
            Bitmap createVideoThumbnail = FileUtils
                    .createVideoThumbnail(tempFile.getAbsolutePath());
            if (createVideoThumbnail != null) {
//                imageview.setImageBitmap(createVideoThumbnail);
            }
        }
    }

    @Override
    public int getTitleLayout() {
        return -1;
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.e(TAG, "got a recording error");
        stopRecording();
        Toast.makeText(this,
                "Recording error has occurred. Stopping the recording",
                Toast.LENGTH_SHORT).show();
    }

    void snedFilePrevieIntent(final String fileName) {


        rl_bottom.setVisibility(View.GONE);
        fl_title.setVisibility(View.GONE);
        chronometer.setVisibility(View.GONE);

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                try {
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(fileName);
                    mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
//                            .OnVideoSizeChangedListener() {
//                        @Override
//                        public void
//                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
//                                    .getVideoHeight());
//                        }
//                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });

                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            rl_bottom.setVisibility(View.VISIBLE);
                            fl_title.setVisibility(View.VISIBLE);
                            chronometer.setVisibility(View.VISIBLE);
                        }
                    });
//                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
//        String type = "";
//        try {
//            Intent intent = new Intent();
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setAction(Intent.ACTION_VIEW);
//            type = MimeTypesTools
//                    .getMimeType(getApplicationContext(), fileName);
//            File file = new File(fileName);
//            Uri uri = null;
//            if (Build.VERSION.SDK_INT >= 24) {
//                uri = FileProvider.getUriForFile(this, "com.yuntongxun.ecdemo.fileprovider", file);
//            } else {
//                uri = Uri.fromFile(file);
//            }
//            intent.setDataAndType(uri, type);
//            startActivity(intent);
//        } catch (Exception e) {
//            System.out
//                    .println("android.content.ActivityNotFoundException: No Activity found to handle Intent { act=android.intent.action.VIEW dat=file:///mnt/sdcard/xxx typ="
//                            + type + " flg=0x10000000");
//        }
    }

//    /**
//     * TextureView resize
//     */
//    public void updateVideoViewSize(float videoWidth, float videoHeight) {
//        if (videoWidth > videoHeight) {
//            FrameLayout.LayoutParams videoViewParam;
//            int height = (int) ((videoHeight / videoWidth) * ());
//            videoViewParam = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                    height);
//            videoViewParam.gravity = Gravity.CENTER;
////            videoViewParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//            mVideoView.setLayoutParams(videoViewParam);
//        }
//    }

}
