package com.yuntongxun.ecdemo.ui.smallwindow;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.yuntongxun.ecdemo.common.CCPAppManager;


/**
 * 呼叫提示
 * @since 2016-08-08
 */
public class CallNotificationMgr {



    /**
     * 显示状态栏信息
     * @param id 标识
     * @param notification 信息
     */
    public static void showCallingNotification(int id, Notification notification) {
        Context ctx = CCPAppManager.getContext();
        if(ctx == null) {
            return;
        }
        try {

            NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            mgr.notify(id, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 取消状态栏通知
     * @param id 标识
     */
    public static void cancelNotification(int id) {
        Context ctx = CCPAppManager.getContext();
        if(ctx == null) {
            return;
        }

        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(id);
    }

}
