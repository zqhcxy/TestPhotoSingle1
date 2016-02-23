package com.example.testphoto.util;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.testphoto.model.PhotoAlbumLVItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GetLocalFile {

    public static final int IMAGES = 1;
    public static final int VIDEOS = 2;
    public static final int AUDIOS = 3;



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



    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    //    private static final Uri sArtworkUri = Uri
//            .parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static Bitmap mCachedBit = null;


    /**
     * 获取视频与图片的目录列表（带有第一张图片(视频)与各目录图片(视频)总数）
     * @param context
     * @return
     */
    public static ArrayList<PhotoAlbumLVItem> getMediaAblum(Context context,int mediaType) {
        ArrayList<PhotoAlbumLVItem> ablumList = new ArrayList<>();
        Cursor cursor = null;
        String[] projection=null;
        String selection=null;
        Uri mediaUri=null;
        switch (mediaType){
            case IMAGES:
                projection = new String[]{MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.BUCKET_ID, // 直接包含该图片文件的文件夹ID，防止在不同下的文件夹重名
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
                        MediaStore.Images.Media.DISPLAY_NAME, // 图片文件名
                        MediaStore.Images.Media.DATA, // 图片绝对路径
                        "count(*)"
                };
                selection="0=0) " +
                        "group by ("+MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
                mediaUri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case AUDIOS:
//                projection = new String[]{MediaStore.Audio.Media._ID,
//                        MediaStore.Audio.Media.ALBUM_ID, //
//                        MediaStore.Audio.Mediadia.BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
//                        MediaStore.Audio.Media.DISPLAY_NAME, // 图片文件名
//                        MediaStore.Audio.Media.DATA, // 图片绝对路径
//                        "count(*)"
//                };
//                selection="0=0) " +
//                        "group by ("+MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
//                mediaUri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case VIDEOS:
                projection = new String[]{MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.BUCKET_ID, // 直接包含该视频文件的文件夹ID，防止在不同下的文件夹重名
                        MediaStore.Video.Media.BUCKET_DISPLAY_NAME, // 直接包含该视频文件的文件夹名
                        MediaStore.Video.Media.DISPLAY_NAME, // 视频文件名
                        MediaStore.Video.Media.DATA, // 视频绝对路径
                        "count(*)"
                };
                selection="0=0) " +
                        "group by ("+MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
                mediaUri=MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
        }

        try {
            if(mediaUri==null||selection==null||projection==null)
                return ablumList;
            cursor = context.getContentResolver().query(
                    mediaUri, projection, selection,
                    null, "");
            if (cursor != null && cursor.getCount() > 0&&cursor.moveToFirst()) {
                do {
//                    String folderIdColumn = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                    String folderColumn = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
//                    int fileIdColumn = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
//                    String fileNameColumn = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    String pathColumn = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(pathColumn).getParentFile();
                    PhotoAlbumLVItem  photoAlbumLVItem = new PhotoAlbumLVItem();
                    photoAlbumLVItem.setFileCount(cursor.getInt(5));
                    photoAlbumLVItem.setFirstImagePath(pathColumn);
                    photoAlbumLVItem.setPathName(parentFile.getAbsolutePath());
                    photoAlbumLVItem.setFolderName(folderColumn);
                    ablumList.add(photoAlbumLVItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return ablumList;
    }
    /**
     * 获取相册目录列表（带有第一张图片与各目录图片总数）
     * @param context
     * @return
     */
    public static ArrayList<PhotoAlbumLVItem> getImageAblum(Context context) {
        ArrayList<PhotoAlbumLVItem> ablumList = new ArrayList<>();

        Cursor cursor = null;
        try {
            String[] projection = new String[]{MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID, // 直接包含该图片文件的文件夹ID，防止在不同下的文件夹重名
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
                    MediaStore.Images.Media.DISPLAY_NAME, // 图片文件名
                    MediaStore.Images.Media.DATA, // 图片绝对路径
                    "count(*)"
            };
            String selection="0=0) " +
                    "group by ("+MediaStore.Images.Media.BUCKET_DISPLAY_NAME;

            cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection,
                    null, "");
            if (cursor != null && cursor.getCount() > 0&&cursor.moveToFirst()) {
                do {
                    String folderIdColumn = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                    String folderColumn = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    int fileIdColumn = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    String fileNameColumn = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    String pathColumn = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(pathColumn).getParentFile();
                    PhotoAlbumLVItem photoAlbumLVItem = new PhotoAlbumLVItem();
                    photoAlbumLVItem.setFileCount(cursor.getInt(5));
                    photoAlbumLVItem.setFirstImagePath(pathColumn);
                    photoAlbumLVItem.setPathName(parentFile.getAbsolutePath());
                    ablumList.add(photoAlbumLVItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return ablumList;
    }
}