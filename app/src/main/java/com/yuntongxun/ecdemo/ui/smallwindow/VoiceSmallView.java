package com.yuntongxun.ecdemo.ui.smallwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;


/**
 * @author 容联•云通讯
 * @version 5.2.0
 * @since 2016-07-22
 */
public class VoiceSmallView extends BaseSmallView {

    TextView mTimerView;

    public VoiceSmallView(Context context) {
        super(context, null);
        LayoutInflater.from(context).inflate(R.layout.meeting_widget_voice_talking, this);
        mTimerView = (TextView) findViewById(R.id.mini_window_timer);
    }

    @Override
    public void onTouchUpStart() {

    }

    @Override
    public void onTouchUpDone() {

    }

    @Override
    public void onAnimationEnd() {

    }

    @Override
    public void setCaptureView(ECCaptureView view) {

    }
}
