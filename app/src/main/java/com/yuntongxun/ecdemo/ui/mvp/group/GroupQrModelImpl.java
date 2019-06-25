package com.yuntongxun.ecdemo.ui.mvp.group;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.encode.QRCodeEncoder;

/**
 * Created by luhuashan on 17/4/7.
 */
public class GroupQrModelImpl implements IGroupQrModel{


    @Override
    public Bitmap getGroupQrBitmap(String content)throws WriterException{

        return QRCodeEncoder.encodeAsBitmap(content, 400);
    }
}
