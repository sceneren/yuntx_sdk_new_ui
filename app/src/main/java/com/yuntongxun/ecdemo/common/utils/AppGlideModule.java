package com.yuntongxun.ecdemo.common.utils;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;

/**
 * 类描述： 全局配置
 */

public class AppGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //设置磁盘缓存
        builder.setDiskCache(getDiskFactory(context));
        MemorySizeCalculator memorySizeCalculator = new MemorySizeCalculator(context);
        int customMemorySize = (int) (1.2 * memorySizeCalculator.getMemoryCacheSize());
        int customBitmaPoolSize = (int) (1.2 * memorySizeCalculator.getBitmapPoolSize());
        builder.setMemoryCache(new LruResourceCache(customMemorySize));
        builder.setBitmapPool(new LruBitmapPool(customBitmaPoolSize));
    }




    @Override
    public void registerComponents(Context context, Glide glide) {

    }

    private DiskCache.Factory getDiskFactory(Context context) {
        return new ExternalCacheDiskCacheFactory(context, "imageCache", 200 * 1024 * 1024);
    }

}
