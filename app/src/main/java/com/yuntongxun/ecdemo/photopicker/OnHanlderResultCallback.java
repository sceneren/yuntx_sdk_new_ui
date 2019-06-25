package com.yuntongxun.ecdemo.photopicker;

/**
 * Created by zlk on 2017/8/17.
 */

import com.yuntongxun.ecdemo.photopicker.model.Photo;

import java.util.List;

/**
 * 处理结果
 */
public  interface OnHanlderResultCallback {
    /**
     * 处理成功
     * @param reqeustCode
     * @param resultList
     * isori 是否原图
     */
     void onHanlderSuccess(int reqeustCode, List<Photo> resultList,boolean isori);

    /**
     * 处理失败或异常
     * @param requestCode
     * @param errorMsg
     */
     void onHanlderFailure(int requestCode, String errorMsg);
}