package com.example.testphoto.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.testphoto.R;
import com.example.testphoto.views.CropImageView1;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 从SDCard异步加载图片
 *
 * @author hanj 2013-8-22 19:25:46
 */
public class SDCardImageLoader {
    // 缓存
    private LruCache<String, Bitmap> imageCache;
    ContentResolver resolver;
    // 软应用缓存
    // private HashMap<String, SoftReference<Bitmap>> imageCache1 = new
    // HashMap<String, SoftReference<Bitmap>>();
    // 固定2个线程来执行任务
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private Handler handler = new Handler();

    private int screenW, screenH;
    public int type;// 缩略图类型
    private Context context;

    /**
     * @param screenW
     * @param screenH
     * @param type    缩略图类型：0：视频缩略图（人工生成）；1：图片缩略图;2:视频的本地缩略图(系统生成)
     */
    public SDCardImageLoader(Context context, int screenW, int screenH, int type) {
        this.screenW = screenW;
        this.screenH = screenH;
        this.type = type;
        this.context = context;

        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;// 32M
        // int cacheSize1=4 * 1024 * 1024; // 4MiB
        // int cacheSize2 = maxMemory / 8;//16M
        // 设置图片缓存大小为程序最大可用内存的1/8
        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    ImageCallback callback = new ImageCallback() {

        @Override
        public void imageLoaded(ImageView imageView, Bitmap bmp, String filePath) {

            if (imageView.getTag().equals(filePath)) {

                if (type == 4) {
                    if (bmp != null) {
                        // imageView.setImageBitmap(bmp);
                        Drawable drw = new BitmapDrawable(
                                context.getResources(), bmp);
                        ((CropImageView1) imageView).setDrawable(drw,
                                bmp.getWidth(), bmp.getHeight());
                    }
                } else {
                    if (bmp != null) {
                        imageView.setImageBitmap(bmp);
                    } else {
                        imageView.setImageResource(R.drawable.empty_photo);
                    }
                }
            }

        }

    };

    private Bitmap loadDrawable(final ImageView imageView, final int smallRate,
                                final String filePath, final long id, final long albumid,
                                final ImageCallback callback) {
        // 如果缓存过就从缓存中取出数据
        if (imageCache.get(filePath) != null) {
            // Log.e("缓存读取", "1");
            return imageCache.get(filePath);
        }
        // if (imageCache1.containsKey(filePath)) {
        // SoftReference<Bitmap> reference = imageCache1.get(filePath);
        // Bitmap bmp = reference.get();
        // if (bmp != null) {
        // Log.e("缓存读取", filePath);
        // return bmp;
        // }
        // }

        // Log.e("SDka读取", "0");
        // 如果缓存没有则读取SD卡

        executorService.submit(new Runnable() {
            public void run() {
                try {

                    if (type == 0) {// 视频缩略图--自己制作的

                    } else if (type == 1 || type == 4) {// 图片

                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(filePath, opt);
                        // 获取到这个图片的原始宽度和高度
                        int picWidth = opt.outWidth;
                        int picHeight = opt.outHeight;

                        // 读取图片失败时直接返回
                        if (picWidth == 0 || picHeight == 0) {
                            return;
                        }

                        opt.inSampleSize = smallRate;
                        // 根据屏的大小和图片大小计算出缩放比例
                        if (picWidth > picHeight) {
                            if (picWidth > screenW)
                                opt.inSampleSize *= picWidth / screenW;
                        } else {
                            if (picHeight > screenH)
                                opt.inSampleSize *= picHeight / screenH;
                        }

                        // 这次再真正地生成一个有像素的，经过缩放了的bitmap
                        opt.inJustDecodeBounds = false;
                        final Bitmap bmp = BitmapFactory.decodeFile(filePath,
                                opt);
                        // 存入map
                        imageCache.put(filePath, bmp);
                        handler.post(new Runnable() {
                            public void run() {
                                callback.imageLoaded(imageView, bmp, filePath);
                            }
                        });

                    } else if (type == 2) {// 读取本地视频图片
                        if (resolver == null) {
                            resolver = context.getContentResolver();
                        }
                        // 获取视频缩略图
                        final Bitmap bmp = MediaStore.Video.Thumbnails
                                .getThumbnail(resolver, id,
                                        Images.Thumbnails.MICRO_KIND, null);
                        imageCache.put(filePath, bmp);
                        handler.post(new Runnable() {
                            public void run() {
                                callback.imageLoaded(imageView, bmp, filePath);
                            }
                        });
                    } else if (type == 3) {// 本地音频封面---(已废弃)

                        final Bitmap bmp = GetLocalFile.getArtwork(context, id,
                                albumid, true, true);
//                        SaveBitmap.saveBitmap("audio"+albumid+"-"+id + ".png", bmp);

                        imageCache.put(filePath, bmp);
                        handler.post(new Runnable() {
                            public void run() {
                                callback.imageLoaded(imageView, bmp, filePath);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        return null;
    }

    /**
     * 异步读取SD卡图片，并按指定的比例进行压缩（最大不超过屏幕像素数）--图片专用
     *
     * @param smallRate 压缩比例，不压缩时输入1，此时将按屏幕像素数进行输出
     * @param filePath  图片在SD卡的全路径
     * @param imageView 组件
     */
    public void loadImage(final int smallRate, final String filePath,
                          final ImageView imageView) {

        if (type != 4) {
            // if (!Glide.isSetup()) {// 缓存本地
            // GlideBuilder gb = new GlideBuilder(context);
            // DiskCache dlw = DiskLruCacheWrapper.get(new File(Environment
            // .getExternalStorageDirectory().getAbsolutePath()
            // + "/myCatch/"), 250 * 1024 * 1024);
            // gb.setDiskCache(dlw);
            // Glide.setup(gb);
            // }
            // MyGlideModule myGlideModule = new MyGlideModule();
            // GlideBuilder builder = new GlideBuilder(context)
            // .setDiskCache(new ExternalCacheDiskCacheFactory(context,
            // DiskCache.Factory.DEFAULT_DISK_CACHE_DIR,
            // DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE));
            // myGlideModule.applyOptions(context, builder);
            // Glide.buildFileDescriptorModelLoader(MyGlideModule.class,
            // context);
            // buildFileDescriptorModelLoader(new MyGlideModule(), context);

//            File path = Glide.getPhotoCacheDir(context);
//			Log.e("路径", "请求前：" + path.toString());

            Glide.with(context).load(new File(filePath))
                    .placeholder(R.drawable.empty_photo).centerCrop()
                    .override(120, 120).crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
//			Log.e("路径", "请求后：" + path.toString());
        } else {

            // 如果缓存过就从缓存中取出数据
            imageView.setTag(filePath);
            Bitmap bmp = loadDrawable(imageView, smallRate, filePath, -1, 0,
                    callback);
            if (bmp != null) {
                if (imageView.getTag().equals(filePath)) {
                    imageView.setImageBitmap(bmp);
                }
            } else {
                imageView.setImageResource(R.drawable.empty_photo);
            }
        }

    }

    /**
     * 异步读取SD卡图片，并按指定的比例进行压缩（最大不超过屏幕像素数）---视频专用
     *
     * @param smallRate 压缩比例，不压缩时输入1，此时将按屏幕像素数进行输出
     * @param id        视频本地缩略图获取需要的id，只有 type=2时才有效。不需要的时就传-1
     * @param filePath  图片在SD卡的全路径
     * @param imageView 组件
     */
    public void loadImageForVideo(int smallRate, long id,
                                  final String filePath, final ImageView imageView) {

        Glide.with(context).load(new File(filePath))
                .placeholder(R.drawable.empty_photo)
                .override(120, 120).into(imageView);

        // 如果缓存过就从缓存中取出数据
//        imageView.setTag(filePath);
//        Bitmap bmp = loadDrawable(imageView, smallRate, filePath, id, 0,
//                callback);
//
//        if (bmp != null) {
//            if (imageView.getTag().equals(filePath)) {
//                imageView.setImageBitmap(bmp);
//            }
//        } else {
//            imageView.setImageResource(R.drawable.empty_photo);
//        }

    }

    /**
     * 异步读取SD卡图片，并按指定的比例进行压缩（最大不超过屏幕像素数）---音频专用
     *
     * @param smallRate 压缩比例，不压缩时输入1，此时将按屏幕像素数进行输出
     * @param id        视频本地缩略图获取需要的id，只有 type=2时才有效。不需要的时就传-1
     * @param filePath  图片在SD卡的全路径
     * @param imageView 组件
     */
    public void loadImageForAudio(final int smallRate, final long id,
                                  final String filePath, final long albumid, final ImageView imageView) {

        imageView.setTag(filePath);
        // 如果缓存过就从缓存中取出数据
        Bitmap bmp = loadDrawable(imageView, smallRate, filePath, id, albumid,
                new ImageCallback() {

                    @Override
                    public void imageLoaded(ImageView imageView, Bitmap bmp,
                                            String filePath) {
                        if (imageView.getTag().equals(filePath)) {
                            if (bmp != null) {
                                imageView.setImageBitmap(bmp);
                            } else {// 设置默认图片--音频专属de
                                imageView
                                        .setImageResource(R.drawable.empty_photo);
                            }
                        }

                    }
                });

        if (bmp != null) {
            if (imageView.getTag().equals(filePath)) {
                imageView.setImageBitmap(bmp);
            }
        } else {
            imageView.setImageResource(R.drawable.empty_photo);
        }
    }


    // 对外界开放的回调接口
    public interface ImageCallback {
        // 注意 此方法是用来设置目标对象的图像资源
        public void imageLoaded(ImageView imageView, Bitmap imageDrawable,
                                String filePath);
    }

}
