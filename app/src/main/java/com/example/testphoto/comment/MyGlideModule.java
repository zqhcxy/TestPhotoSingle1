package com.example.testphoto.comment;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.module.GlideModule;
import com.example.testphoto.util.CommonUtil;

import java.io.File;

public class MyGlideModule implements GlideModule {

    @Override
    public void applyOptions(final Context context, GlideBuilder builder) {

        builder.setDiskCache(new DiskCache.Factory() {
            @Override
            public DiskCache build() {
                DiskCache dlw = DiskLruCacheWrapper.get(new File(CommonUtil.getGlideCacheFile()), 250 * 1024 * 1024);
                return dlw;
            }
        });
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);

    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
