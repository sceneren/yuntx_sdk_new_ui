package com.yuntongxun.ecdemo.photopicker;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.ta.utdid2.android.utils.PhoneInfoUtils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.photopicker.model.Photo;
import com.yuntongxun.ecdemo.photopicker.model.PhotoDirectory;

import java.util.ArrayList;

/**
 * Created by zlk on 2017/8/17.
 */

public class GalleryUtil {

    private static OnHanlderResultCallback mCallback;
    private static int mRequestCode;
    private static Context mContext;

    /**
     * 打开Gallery-多选
     * @param requestCode
     * @param callback
     */
    public static void openGalleryMuti(Context context,int requestCode, OnHanlderResultCallback callback) {

        mContext = context;

        mRequestCode = requestCode;
        mCallback = callback;

        Intent intent = new Intent(context, PhotoDirectoryPickerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static  void resultData(ArrayList<Photo> photoList, boolean isori) {
      OnHanlderResultCallback callback = getCallback();
        int requestCode = getRequestCode();
        if (callback != null) {
            if ( photoList != null && photoList.size() > 0 ) {
                callback.onHanlderSuccess(requestCode, photoList,isori);
            } else {
                callback.onHanlderFailure(requestCode, getmContext().getString(R.string.photo_list_empty));
            }
        }
    }

    public static OnHanlderResultCallback getCallback() {
        return mCallback;
    }


    public static Context getmContext() {
        return mContext;
    }

    public static int getRequestCode() {
        return mRequestCode;
    }

}
