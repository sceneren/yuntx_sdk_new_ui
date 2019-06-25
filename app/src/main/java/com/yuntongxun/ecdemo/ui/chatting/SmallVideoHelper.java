package com.yuntongxun.ecdemo.ui.chatting;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by luhuashan on 17/2/8.
 */
public class SmallVideoHelper {


    private static final String TAG = "SmallVideoHelper";


    private HashMap<String, AnimationDrawable> map = new HashMap<String, AnimationDrawable>();


    public AnimationDrawable get(String url) {

        return map.get(url);
    }

    private static SmallVideoHelper helper = new SmallVideoHelper();

    public static SmallVideoHelper getInstance() {
        return helper;
    }


    public void decoder(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        AnimationDrawable d = get(url);

        if (d != null){
            return;
        }

        AnimationDrawable drawable = new AnimationDrawable();

        for (int i = 0; i < getDurationLong(url); i += 1 * 25 ) {
            Bitmap bitmap = createVideoThumbnail(url, i);

            BitmapDrawable bi = new BitmapDrawable(bitmap);
            drawable.addFrame(bi, 25);

        }
        map.put(url, drawable);
    }


    //根据url获取音视频时长，返回毫秒
    public long getDurationLong(String url) {
        String duration = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {

            retriever.setDataSource(url);
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception ex) {

        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {

            }
        }
        if (!TextUtils.isEmpty(duration)) {
            return Long.parseLong(duration);
        } else {
            return 0;
        }
    }


    //获取视频缩略图
    private Bitmap createVideoThumbnail(String url, long timeUs) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(url);
            bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_NEXT_SYNC);
        } catch (IllegalArgumentException ex) {

        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return bitmap;
    }


}
