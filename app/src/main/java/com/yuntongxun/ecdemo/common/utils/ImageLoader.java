package com.yuntongxun.ecdemo.common.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;

import java.io.File;

public class ImageLoader {
    public static final String ANDROID_RESOURCE = "android.resource://";
    public static final String FOREWARD_SLASH = "/";

    private static class ImageLoaderHolder {
        private static final ImageLoader INSTANCE = new ImageLoader();
    }

    private ImageLoader() {
    }

    public static final ImageLoader getInstance() {
        return ImageLoaderHolder.INSTANCE;
    }

    //直接加载网络图片
    public void displayImage(String url, Context context, ImageView imageView, int placeholderImage, int failureImage) {
        if (context == null) {
            return;
        }
        try {
            Glide.with(context)
                    .load(url)
                    .placeholder(placeholderImage)
                    .error(failureImage)
                    .centerCrop()
                    .dontAnimate()
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayBackground(String url, Context context, final View view) {
        Glide.with(context).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {


                view.setBackground(new BitmapDrawable(resource));
            }
        });
    }

    public void displayImage(String url, Context context, ImageView imageView) {

        if (context == null) {
            return;
        }
        try {
            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .dontAnimate()
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //加载SD卡图片
    public void displayImage(Context context, File file, ImageView imageView) {
        Glide
                .with(context)
                .load(file)
                .centerCrop()
                .into(imageView);
    }

    //加载SD卡图片并设置大小
    public void displayImage(Context context, File file, ImageView imageView, int width, int height) {
        Glide
                .with(context)
                .load(file)
                .override(width, height)
                .centerCrop()
                .into(imageView);

    }

    //加载网络图片并设置大小
    public void displayImage(Context context, String url, ImageView imageView, int width, int height) {
        Glide
                .with(context)
                .load(url)
                .centerCrop()
                .dontAnimate()
                .override(width, height)
                .crossFade()
                .into(imageView);
    }

    //加载drawable图片
    public void displayImage(Context context, int resId, ImageView imageView,int width,int height) {
        Glide.with(context)
                .load(resourceIdToUri(context, resId))
                .fitCenter()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(width,height)
                .into(imageView);
    }

    //加载drawable图片
    public void displayImage(Context context, int resId, ImageView imageView) {
        try {
            Glide.with(context)
                    .load(resourceIdToUri(context, resId))
                    .fitCenter()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //加载drawable图片显示为圆形图片
    public void displayCricleImage(Context context, int resId, ImageView imageView) {
        Glide.with(ECApplication.getInstance())
                .load(resourceIdToUri(context, resId))
                .crossFade()
                .transform(new GlideCircleTransform(context))
                .into(imageView);
    }

    //加载网络图片显示为圆形图片
    public void displayCricleImage(Context context, String url, ImageView imageView) {
        Glide
                .with(ECApplication.getInstance())
                .load(url)
                .dontAnimate()
                .transform(new GlideCircleTransform(context))
                .crossFade()
//                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(imageView);
    }

    //加载SD卡图片显示为圆形图片
    public  void displayCricleImage(Context context, File file, ImageView imageView) {
        Glide.with(ECApplication.getInstance())
                .load(file)
                //.centerCrop()
                .transform(new GlideCircleTransform(context))
                .into(imageView);

    }

    //将资源ID转为Uri
    public Uri resourceIdToUri(Context context, int resourceId) {
        return Uri.parse(ANDROID_RESOURCE + context.getPackageName() + FOREWARD_SLASH + resourceId);
    }

    public void displayBigPhoto(Context context, ImageView imageView, String url) {
        if (imageView == null) {
            throw new IllegalArgumentException("argument error");
        }
        Glide.with(context).load(url).asBitmap()
                .dontAnimate()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.header_woman)
                .error(R.drawable.header_woman)
                .into(imageView);
    }


    public void displayGif(Context context, ImageView imageView, int res) {
        if (imageView == null) {
            throw new IllegalArgumentException("argument error");
        }
        Glide.with(context).load(res).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
    }
}