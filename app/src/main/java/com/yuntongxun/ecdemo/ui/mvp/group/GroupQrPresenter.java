package com.yuntongxun.ecdemo.ui.mvp.group;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;
import com.yuntongxun.ecdemo.exception.ECEncoderQrException;

/**
 * Created by luhuashan on 17/4/7.
 */
public class GroupQrPresenter {

    private IGroupQrModel iGroupQrModel ;

    private IGroupView iGroupView;


    public GroupQrPresenter(IGroupView view,IGroupQrModel model){

        this.iGroupQrModel = model;
        this.iGroupView = view;
    }


    public Bitmap getGroupQrCode(String content) throws ECEncoderQrException {
        try {
            return  iGroupQrModel.getGroupQrBitmap(content);
        } catch (WriterException e) {
           throw  new ECEncoderQrException(e);
        }
    }

    public void showGroupQrCode(Bitmap bitmap){

        iGroupView.setGroupQrImg(bitmap);
    }


}
