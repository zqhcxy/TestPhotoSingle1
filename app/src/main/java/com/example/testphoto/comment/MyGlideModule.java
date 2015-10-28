package com.example.testphoto.comment;

import android.content.Context;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.module.GlideModule;

import java.io.File;

public class MyGlideModule implements GlideModule {

    @Override
    public void applyOptions(final Context context, GlideBuilder builder) {

        builder.setDiskCache(new DiskCache.Factory() {
            @Override
            public DiskCache build() {
                // Careful: the external cache directory doesn't enforce
                // permissions
                // File cacheLocation = new File(context.getExternalCacheDir(),
                // "cache_dir_name");
                // cacheLocation.mkdirs();
                DiskCache dlw = DiskLruCacheWrapper.get(new File(Environment
                        .getExternalStorageDirectory().getAbsolutePath()
                        + "/.myCatch/"), 250 * 1024 * 1024);

                return dlw;
            }
        });
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);

    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}