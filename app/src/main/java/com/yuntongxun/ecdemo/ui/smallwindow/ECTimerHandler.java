/*
 *  Copyright (c) 2015 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */package com.yuntongxun.ecdemo.ui.smallwindow;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * SDK 定时任务执行接口
 * @author 容联•云通讯
 * @version 5.0
 * @since 2015-3-30
 */
public class ECTimerHandler extends Handler {

    public static final int MAX_WHAT = 0x2000;
    /** self-propagation message what*/
    private static int HANDLER_WHAT;
    /**Uniquely identifies the message*/
    private final int mWhat;
    private long mDelayMillis = 0L;
    private final boolean mHandle;
    private final CallBack mHandlerCallback;

    /**
     * 创建一个定时任务执行接口
     * @param looper 线程对象
     * @param callback 回调接口
     * @param handle 队列
     */
    public ECTimerHandler(Looper looper , CallBack callback , boolean handle) {
        super(looper);
        mWhat = createWhat();
        mHandlerCallback = callback;
        mHandle = handle;
    }

    /**
     * 创建一个定时任务执行接口
     * @param callback 回调接口
     * @param handle 队列
     */
    public ECTimerHandler(CallBack callback , boolean handle) {
        mWhat = createWhat();
        mHandlerCallback = callback;
        mHandle = handle;
    }

    /**
     * Unique production news message what
     * @return 唯一标识
     */
    private static int createWhat() {
        if(HANDLER_WHAT > MAX_WHAT) {
            HANDLER_WHAT = 0;
        }
        HANDLER_WHAT += 1;
        return HANDLER_WHAT;
    }

    @Override
    protected void finalize() throws Throwable {
        stopTimer();
        super.finalize();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if(msg.what != mWhat && mHandlerCallback == null) {
            return ;
        }

        if(!mHandlerCallback.onTimerExpired() || !mHandle) {
            return ;
        }

        sendEmptyMessageDelayed(mWhat, mDelayMillis);
    }

    /**
     * Sends a Message containing only the what value, to be delivered
     * after the specified amount of time elapses.
     */
    public void startTimer(long delayMillis) {
        mDelayMillis = delayMillis;
        stopTimer();
        sendEmptyMessageDelayed(mWhat, delayMillis);

    }

    /**
     * Remove any pending posts of messages with code 'what' that are in the
     * message queue.
     */
    public void stopTimer(){
        removeMessages(mWhat);
    }

    /**
     * Check if there are any pending posts of messages with code 'what' in
     * the message queue.
     */
    public boolean stopped(){
        return !hasMessages(mWhat);
    }

    public interface CallBack {
        boolean onTimerExpired();
    }
}
