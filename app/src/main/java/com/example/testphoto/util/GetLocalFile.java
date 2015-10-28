package com.example.testphoto.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.util.LruCache;

import com.example.testphoto.R;
import com.example.testphoto.model.MyAudio;
import com.example.testphoto.model.MyVideo;
import com.example.testphoto.model.PhotoAlbumLVItem;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.example.testphoto.util.Utility.isAudio;
import static com.example.testphoto.util.Utility.isImage;
import static com.example.testphoto.util.Utility.isVideo;

/**
 * 获取本地图片与视频工具类
 * <p/>
 * 获取所有图片、视频和它们的目录。。
 *
 * @author zqh-pc
 */
public class GetLocalFile {

    // 缓存
    private static LruCache<String, Bitmap> imageCache;

    /**
     * 使用ContentProvider读取SD卡所有图片。---目录
     */

    public static ArrayList<PhotoAlbumLVItem> getImagePathsByContentProvider(
            Context context) {
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
        String key_DATA = MediaStore.Images.Media.DATA;

        ContentResolver mContentResolver = context.getContentResolver();

        // 只查询jpg和png的图片
        Cursor cursor = mContentResolver.query(mImageUri,
                new String[]{key_DATA}, key_MIME_TYPE + "=? or "
                        + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?",
                new String[]{"image/jpg", "image/jpeg", "image/png"},
                MediaStore.Images.Media.DATE_MODIFIED);

        ArrayList<PhotoAlbumLVItem> list = new ArrayList<PhotoAlbumLVItem>();
        if (cursor != null) {
            if (cursor.moveToLast()) {
                // 路径缓存，防止多次扫描同一目录
                HashSet<String> cachePath = new HashSet<String>();
                // list = new ArrayList<PhotoAlbumLVItem>();

                while (true) {
                    // 获取图片的路径
                    String imagePath = cursor.getString(0);

                    File parentFile = new File(imagePath).getParentFile();
                    if (parentFile.lastModified() != 0) {// 为0表示不存在
                        String parentPath = parentFile.getAbsolutePath();
                        // 不扫描重复路径
                        if (!cachePath.contains(parentPath)) {
                            String first_path = getFirstFilePath(parentFile, 0);
                            if (first_path != null) {// 不是图片不添加
                                list.add(new PhotoAlbumLVItem(parentPath,
                                        getFileCount(parentFile, 0), first_path));
                                cachePath.add(parentPath);
                            }
                        }
                    }
                    if (!cursor.moveToPrevious()) {
                        break;
                    }
                }
            }

            cursor.close();
        }

        return list;
    }

    /**
     * 使用ContentProvider读取SD卡所有视频。---目录
     */
    public static ArrayList<PhotoAlbumLVItem> getVideoPathsByContentProvider(
            Context context) {
        Uri mImageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        // String key_MIME_TYPE = MediaStore.Video.Media.MIME_TYPE;
        String key_DATA = MediaStore.Video.Media.DATA;

        ContentResolver mContentResolver = context.getContentResolver();

        // 只查询jpg和png的图片
        Cursor cursor = mContentResolver.query(mImageUri,
                new String[]{key_DATA}, null, null,
                MediaStore.Video.Media.DATE_MODIFIED);

        ArrayList<PhotoAlbumLVItem> list = new ArrayList<PhotoAlbumLVItem>();
        if (cursor != null) {
            if (cursor.moveToLast()) {
                // 路径缓存，防止多次扫描同一目录
                HashSet<String> cachePath = new HashSet<String>();
                // list = new ArrayList<PhotoAlbumLVItem>();

                while (true) {
                    // 获取图片的路径
                    String imagePath = cursor.getString(0);

                    File parentFile = new File(imagePath).getParentFile();
                    if (parentFile.lastModified() != 0) {// 为0表示不存在
                        String parentPath = parentFile.getAbsolutePath();
                        // 不扫描重复路径
                        if (!cachePath.contains(parentPath)) {
                            String first_path = getFirstFilePath(parentFile, 1);
                            if (first_path != null) {// 不是图片不添加
                                list.add(new PhotoAlbumLVItem(parentPath,
                                        getFileCount(parentFile, 1), first_path));
                                cachePath.add(parentPath);
                            }
                        }
                    }
                    if (!cursor.moveToPrevious()) {
                        break;
                    }
                }
            }

            cursor.close();
        }

        return list;
    }

    /**
     * 使用ContentProvider读取SD卡所有音频。---目录
     */
    public static ArrayList<PhotoAlbumLVItem> getAudioPathsByContentProvider(
            Context context) {

        Uri mImageUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String key_MIME_TYPE = MediaStore.Audio.Media.MIME_TYPE;
        String key_DATA = MediaStore.Audio.Media.DATA;
        // String selection = key_MIME_TYPE + "=? or " + key_MIME_TYPE +
        // "=? or "
        // + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or "
        // + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?";
        // String[] strs = new String[] { "audio/mp3", "audio/ape", "audio/wav",
        // "audio/aiff", "audio/flac", "audio/arm" };

        ContentResolver mContentResolver = context.getContentResolver();

        Cursor cursor = mContentResolver.query(mImageUri,
                new String[]{key_DATA}, null, null,
                MediaStore.Audio.Media.DATE_MODIFIED);

        ArrayList<PhotoAlbumLVItem> list = new ArrayList<PhotoAlbumLVItem>();
        HashMap<String, Integer> location;
        if (cursor != null) {
            if (cursor.moveToLast()) {
                // 路径缓存，防止多次扫描同一目录
                HashSet<String> cachePath = new HashSet<String>();

                location = new HashMap<>();

                while (true) {
                    // 获取音频的路径
                    String imagePath = cursor.getString(0);
                    // Log.i("音频路径", imagePath);

                    File parentFile = new File(imagePath).getParentFile();
                    // Log.e("父路径", parentFile.toString());
                    if (parentFile.lastModified() != 0) {// 为0表示不存在
                        String parentPath = parentFile.getAbsolutePath();
                        // 不扫描重复路径
                        if (!cachePath.contains(parentPath)) {
                            // 这里诺要限定音频的格式就显
                            // String first_path = getFirstFilePath(parentFile,
                            // 2);
                            // if (first_path != null) {
                            list.add(new PhotoAlbumLVItem(parentPath, 1,
                                    imagePath));
                            cachePath.add(parentPath);
                            location.put(parentPath, list.size() - 1);
                            // }
                        } else {
                            // 重复路径的就直接更新文件夹内容个数。
                            int posotion = location.get(parentPath);
                            int count = list.get(posotion).getFileCount();
                            list.get(posotion).setFileCount(count + 1);

                        }
                    }
                    if (!cursor.moveToPrevious()) {
                        break;
                    }
                }
            }

            cursor.close();
        }

        return list;

    }

    /**
     * 使用ContentProvider读取SD卡全部图片。
     * <p/>
     * 可优化
     */
    public static ArrayList<String> getLatestImagePaths(Context context) {
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
        String key_DATA = MediaStore.Images.Media.DATA;

        ContentResolver mContentResolver = context.getContentResolver();

        // 只查询jpg和png的图片,按最新修改排序
        Cursor cursor = mContentResolver.query(mImageUri,
                new String[]{key_DATA}, key_MIME_TYPE + "=? or "
                        + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?",
                new String[]{"image/jpg", "image/jpeg", "image/png"},
                MediaStore.Images.Media.DATE_MODIFIED);

        ArrayList<String> latestImagePaths = new ArrayList<String>();
        if (cursor != null) {
            // 从最新的图片开始读取.
            // 当cursor中没有数据时，cursor.moveToLast()将返回false
            if (cursor.moveToLast()) {
                // latestImagePaths = new ArrayList<String>();

                while (true) {
                    // 获取图片的路径
                    String path = cursor.getString(0);
                    latestImagePaths.add(path);

                    if (!cursor.moveToPrevious()) {
                        break;
                    }
                }
            }
            cursor.close();
        }

        return latestImagePaths;
    }

    /**
     * 使用ContentProvider读取SD卡全部视频。
     *
     * @param context
     * @param mVideoUri 为null表示读取所有视频，否则获取指定目录下的视频
     * @param orderBy   排序条件
     * @param selection 查找条件，为空就查找所有
     * @return
     */

    private static ArrayList<MyVideo> getLatestVideoPaths(Context context,
                                                          Uri mVideoUri, String orderBy, String selection) {
        ArrayList<MyVideo> list = new ArrayList<MyVideo>();
        ContentResolver resolver = context.getContentResolver();

        // Uri mVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        // String key_DATA = MediaStore.Video.Media.DATA;
        // // String key_MIME_TYPE = MediaStore.Images.Media.DATA;
        // Cursor cursor = context.getContentResolver().query(mVideoUri, null,
        // null, null, MediaStore.Video.Media.DATE_MODIFIED);
        // if (videopath == null) {// 查找所有数据没有条件
        // cursor = context.getContentResolver().query(mVideoUri, null, null,
        // null, MediaStore.Video.Media.DATE_MODIFIED);
        // } else {// 查找特定路径下的数据
        // cursor = context.getContentResolver().query(mVideoUri, null,
        // key_DATA + " like '" + videopath + "%'", null,
        // MediaStore.Video.Media.DATE_MODIFIED);
        // }
        Cursor cursor = context.getContentResolver().query(mVideoUri, null,
                selection, null, orderBy);
        if (cursor != null) {
//			list = new ArrayList<MyVideo>();
            if (cursor.moveToLast()) {// 为了保证最新的显示在最前面
                while (true) {
                    int id = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String album = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                    String artist = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    long duration = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    MyVideo video = new MyVideo(id, title, album, artist,
                            displayName, mimeType, path, size, duration);

                    if (imageCache == null) {
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
                    Bitmap bitmap = imageCache.get(path);
                    if (bitmap == null) {
                        // 获取视频缩略图
                        bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                                resolver, id, Images.Thumbnails.MICRO_KIND,
                                null);
                        imageCache.put(path, bitmap);
                    }
                    video.setBitmap(bitmap);
                    list.add(video);
                    if (!cursor.moveToPrevious()) {
                        break;
                    }
                }
            }
            cursor.close();
        }
        return list;
    }

    /**
     * 使用ContentProvider读取SD卡全部音频。
     *
     * @param context
     * @param mVideoUri 为null表示读取所有音频，否则获取指定目录下的音频
     * @param orderBy   排序条件
     * @param selection 查找条件，为空就查找所有
     * @return
     */

    private static ArrayList<MyAudio> getLatestAudioPaths(Context context,
                                                          Uri mVideoUri, String orderBy, String selection) {
        ArrayList<MyAudio> list = new ArrayList<MyAudio>();
        ContentResolver resolver = context.getContentResolver();

        // Uri mVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        // String key_DATA = MediaStore.Video.Media.DATA;
        // // String key_MIME_TYPE = MediaStore.Images.Media.DATA;
        // Cursor cursor = context.getContentResolver().query(mVideoUri, null,
        // null, null, MediaStore.Video.Media.DATE_MODIFIED);
        // if (videopath == null) {// 查找所有数据没有条件
        // cursor = context.getContentResolver().query(mVideoUri, null, null,
        // null, MediaStore.Video.Media.DATE_MODIFIED);
        // } else {// 查找特定路径下的数据
        // cursor = context.getContentResolver().query(mVideoUri, null,
        // key_DATA + " like '" + videopath + "%'", null,
        // MediaStore.Video.Media.DATE_MODIFIED);
        // }
        Cursor cursor = context.getContentResolver().query(mVideoUri, null,
                selection, null, orderBy);
        if (cursor != null) {
            // list = new ArrayList<MyAudio>();
            if (cursor.moveToLast()) {// 为了保证最新的显示在最前面
                while (true) {
                    int id = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String album = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String artist = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    long duration = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    long size = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    long albumid = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                    MyAudio myAudio = new MyAudio(id, title, album, artist,
                            path, displayName, mimeType, duration, size,
                            albumid);
                    myAudio.setItemType("Audio");
                    list.add(myAudio);
                    if (!cursor.moveToPrevious()) {
                        break;
                    }
                }
            }
            cursor.close();
        }
        return list;
    }

    /**
     * 获取指定路径下的所有图片文件。
     */
    public static ArrayList<String> getAllImagePathsByFolder(String folderPath) {
        File folder = new File(folderPath);
        String[] allFileNames = folder.list();
        if (allFileNames == null || allFileNames.length == 0) {
            return null;
        }

        ArrayList<String> imageFilePaths = new ArrayList<String>();
        for (int i = allFileNames.length - 1; i >= 0; i--) {
            if (isImage(allFileNames[i])) {
                imageFilePaths.add(folderPath + File.separator
                        + allFileNames[i]);
            }
        }

        return imageFilePaths;
    }

    /**
     * 获取指定路径下的所有视频文件。
     */
    public static ArrayList<MyVideo> getAllVideoPathsByFolder(Context context,
                                                              String folderPath) {

        Uri mVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;// Uri
        String orderBy = MediaStore.Video.Media.DATE_MODIFIED;
        String selection = null;

        if (folderPath != null) {// 查找特定路径下的数据
            selection = MediaStore.Video.Media.DATA + " like '" + folderPath
                    + "%'";
        }
        ArrayList<MyVideo> imageFilePaths = getLatestVideoPaths(context,
                mVideoUri, orderBy, selection);

        return imageFilePaths;
    }



    /**
     * 获取指定路径下的所有音频文件。
     *
     * @param folderPath 指定目录，诺目录为空为获取所有文件
     */
    public static ArrayList<MyAudio> getAllAudioPathsByFolder(Context context,
                                                              String folderPath) {
        Uri mVideoUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;// Uri
        String orderBy = MediaStore.Audio.Media.DATE_MODIFIED;
        String selection = null;

        if (folderPath != null) {// 查找特定路径下的数据
            selection = MediaStore.Audio.Media.DATA + " like '" + folderPath
                    + "%'";

        }
        ArrayList<MyAudio> imageFilePaths = getLatestAudioPaths(context,
                mVideoUri, orderBy, selection);
        return imageFilePaths;

    }

    // public static ArrayList<MyAudio> getSysAudio(Context context, Uri uri) {
    // // Uri mVideoUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;// Uri
    // String orderBy = MediaStore.Audio.Media.DATE_MODIFIED;
    // String selection = null;
    //
    // ArrayList<MyAudio> imageFilePaths = getLatestAudioPaths(context, uri,
    // orderBy, selection);
    // return imageFilePaths;
    //
    // }

    // 共用

    /**
     * 获取目录中(图片或视频或音频)的个数。
     */
    private static int getFileCount(File folder, int type) {
        int count = 0;

        File[] files = folder.listFiles();
        for (File file : files) {

            boolean isfile = false;
            if (type == 0) {// 图片
                isfile = isImage(file.getName());
            } else if (type == 1) {// 视频
                isfile = isVideo(file.getName());
            } else if (type == 2) {// 音频
                isfile = isAudio(file.getName());

            }
            if (isfile) {
                count++;
            }
        }

        return count;
    }

    /**
     * 获取目录中最新(图片、视频、音频)的绝对路径。
     */
    private static String getFirstFilePath(File folder, int type) {
        File[] files = folder.listFiles();
        for (int i = files.length - 1; i >= 0; i--) {
            File file = files[i];
            boolean isfile = false;
            if (type == 0) {// 图片
                isfile = isImage(file.getName());
            } else if (type == 1) {// 视频
                isfile = isVideo(file.getName());
            } else if (type == 2) {// 音频
                isfile = isAudio(file.getName());
            }
            if (isfile) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

//	/*
//     * 获取音乐封面
//	 */
//
//    public static Bitmap getArtwork(Context context, long song_id,
//                                    long album_id, boolean allowdefault) {
//        if (album_id < 0) {
//            // This is something that is not in the database, so get the album
//            // art directly
//            // from the file.
//            if (song_id >= 0) {
//                Bitmap bm = getArtworkFromFile(context, song_id, -1);
//                if (bm != null) {
//                    return bm;
//                }
//            }
//            if (allowdefault) {
//                return getDefaultArtwork(context);
//            }
//            return null;
//        }
//        ContentResolver res = context.getContentResolver();
//        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
//        if (uri != null) {
//            InputStream in = null;
//            try {
//                in = res.openInputStream(uri);
//                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
//            } catch (FileNotFoundException ex) {
//                // The album art thumbnail does not actually exist. Maybe the
//                // user deleted it, or
//                // maybe it never existed to begin with.
//                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
//                if (bm != null) {
//                    if (bm.getConfig() == null) {
//                        bm = bm.copy(Bitmap.Config.RGB_565, false);
//                        if (bm == null && allowdefault) {
//                            return getDefaultArtwork(context);
//                        }
//                    }
//                } else if (allowdefault) {
//                    bm = getDefaultArtwork(context);
//                }
//                return bm;
//            } finally {
//                try {
//                    if (in != null) {
//                        in.close();
//                    }
//                } catch (IOException ex) {
//                }
//            }
//        }
//
//        return null;
//    }
//
//    private static Bitmap getArtworkFromFile(Context context, long songid,
//                                             long albumid) {
//        Bitmap bm = null;
//        byte[] art = null;
//        String path = null;
//        if (albumid < 0 && songid < 0) {
//            throw new IllegalArgumentException(
//                    "Must specify an album or a song id");
//        }
//        try {
//            if (albumid < 0) {
//                Uri uri = Uri.parse("content://media/external/audio/media/"
//                        + songid + "/albumart");
//                ParcelFileDescriptor pfd = context.getContentResolver()
//                        .openFileDescriptor(uri, "r");
//                if (pfd != null) {
//                    FileDescriptor fd = pfd.getFileDescriptor();
//                    bm = BitmapFactory.decodeFileDescriptor(fd);
//                }
//            } else {
//                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
//                ParcelFileDescriptor pfd = context.getContentResolver()
//                        .openFileDescriptor(uri, "r");
//                if (pfd != null) {
//                    FileDescriptor fd = pfd.getFileDescriptor();
//                    bm = BitmapFactory.decodeFileDescriptor(fd);
//                }
//            }
//        } catch (FileNotFoundException ex) {
//
//        }
//        if (bm != null) {
//            mCachedBit = bm;
//        }
//        return bm;
//    }
//
//    private static Bitmap getDefaultArtwork(Context context) {
//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        opts.inPreferredConfig = Bitmap.Config.RGB_565;
//        return BitmapFactory.decodeStream(context.getResources()
//                .openRawResource(R.drawable.empty_photo), null, opts);
//    }



    /**
     * 获取默认专辑图片
     * @param context
     * @return
     */
    public static Bitmap getDefaultArtwork(Context context,boolean small) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if(small){	//返回小图片
            return BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.empty_photo), null, opts);
        }
        return BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.empty_photo), null, opts);
    }


    /**
     * 从文件当中获取专辑封面位图
     * @param context
     * @param songid
     * @param albumid
     * @return
     */
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid){
        Bitmap bm = null;
        if(albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if(albumid < 0){
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if(pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if(pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            // 只进行大小判断
            options.inJustDecodeBounds = true;
            // 调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            // 我们的目标是在800pixel的画面上显示
            // 所以需要调用computeSampleSize得到图片缩放的比例
            options.inSampleSize = 100;
            // 我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }

    /**
     * 获取专辑封面位图对象
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefalut
     * @return
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefalut, boolean small){
        if(album_id < 0) {
            if(song_id < 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if(bm != null) {
                    return bm;
                }
            }
            if(allowdefalut) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        if(uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //先制定原始大小
                options.inSampleSize = 1;
                //只进行大小判断
                options.inJustDecodeBounds = true;
                //调用此方法得到options得到图片的大小
                BitmapFactory.decodeStream(in, null, options);
                /** 我们的目标是在你N pixel的画面上显示。 所以需要调用computeSampleSize得到图片缩放的比例 **/
                /** 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的结合 **/
                if(small){
                    options.inSampleSize = computeSampleSize(options, 120);
                } else{
                    options.inSampleSize = computeSampleSize(options, 600);
                }
                // 我们得到了缩放比例，现在开始正式读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, options);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if(bm != null) {
                    if(bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if(bm == null && allowdefalut) {
                            return getDefaultArtwork(context, small);
                        }
                    }
                } else if(allowdefalut) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;
            } finally {
                try {
                    if(in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 对图片进行合适的缩放
     * @param options
     * @param target
     * @return
     */
    public static int computeSampleSize(  BitmapFactory.Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if(candidate == 0) {
            return 1;
        }
        if(candidate > 1) {
            if((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if(candidate > 1) {
            if((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }
        return candidate;
    }






    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
//    private static final Uri sArtworkUri = Uri
//            .parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static Bitmap mCachedBit = null;

}
