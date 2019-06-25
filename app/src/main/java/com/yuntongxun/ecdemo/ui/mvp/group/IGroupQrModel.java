package com.yuntongxun.ecdemo.ui.mvp.group;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;

/**
 * Created by luhuashan on 17/4/7.
 */
public interface IGroupQrModel{


    Bitmap getGroupQrBitmap(String content) throws WriterException;



}
