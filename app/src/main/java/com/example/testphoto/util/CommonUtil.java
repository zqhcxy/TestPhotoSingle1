package com.example.testphoto.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 项目中已经有，直接用项目中的（个人工具类）
 * Created by zqh-pc on 2015/9/22.
 */
public class CommonUtil {

    private static final int LIMIT_SIZE = 300 * 1024;
    private static String mSDCARD = null;
    private static int mIsInAndroid40 = -1;

    /**
     * 转换文件大小为M、kb
     *
     * @param space
     * @return
     */
    public static String getUserSpaceStr(long space) {
        java.text.NumberFormat formater = java.text.DecimalFormat.getInstance();
        formater.setMaximumFractionDigits(2);
        formater.setGroupingUsed(false);
        if (space < (1024 * 1024)) {
            float size = Float.parseFloat(space + "") / 1024;
            return formater.format(size) + "KB";
        } else if (space < (1024 * 1024 * 1024)) {
            float size = Float.parseFloat(space + "") / (1024 * 1024);
            return formater.format(size) + "MB";
        } else {
            float size = Float.parseFloat(space + "") / (1024 * 1024 * 1024);
            return formater.format(size) + "GB";
        }
    }


    /**
     * Drawable转成Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;

    }

    /**
     * Bitmap to Drawable
     *
     * @param bitmap
     * @param mcontext
     * @return
     */
    public static Drawable bitmapToDrawble(Bitmap bitmap, Context mcontext) {
        Drawable drawable = new BitmapDrawable(mcontext.getResources(), bitmap);
        return drawable;
    }

    /**
     * 图片翻转
     *
     * @param bmp
     * @param flag 0为水平翻转；1：为垂直翻转
     * @return
     */
    public static Bitmap reverseBitmap(Bitmap bmp, int flag) {
//        if (ImageCache.get("reverseBitmap") != null) {
//            return ImageCache.get("reverseBitmap");
//        }
        float[] floats = null;
        switch (flag) {
            case 0: // 水平反转
                floats = new float[]{-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
                break;
            case 1: // 垂直反转
                floats = new float[]{1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f};
                break;
        }

        if (floats != null) {
            Matrix matrix = new Matrix();
            matrix.setValues(floats);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                    bmp.getHeight(), matrix, true);
        }
//        ImageCache.put("reverseBitmap", bmp);
        return bmp;
    }


    public static String getHcExternalFile(String subFolder, String fileName) {
        try {
            String subFolderPath = getRootFilePath()
                    + "/Testphoto/"
                    + subFolder + "/";
            File fi = new File(subFolderPath);
            if (!fi.exists()) {
                fi.mkdirs();

            }
            return subFolderPath + fileName;
        } catch (Exception exp) {
            exp.printStackTrace();
            return null;
        }

    }

//    public static String getSDCardPath() {
//        if (mSDCARD == null) {
//            mSDCARD = Environment.getExternalStorageDirectory().toString();
//            if (mSDCARD == null) {
//                mSDCARD = "/sdcard";
//            }
//        }
//        return mSDCARD;
//    }

    /**
     * 获取glide的缓存目录
     *
     * @return
     */
    public static String getGlideCacheFile() {
        try {
            String subFolderPath = getRootFilePath()
                    + "/testphoto/.myCatch/";
            File fi = new File(subFolderPath);
            if (!fi.exists()) {
                fi.mkdirs();

            }
            return subFolderPath;
        } catch (Exception exp) {
            exp.printStackTrace();
            return null;
        }

    }

    /**
     * 获取SD卡地址
     *
     * @return
     */
    public static String getRootFilePath() {
        if (hasSDCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/";// filePath:/sdcard/
        } else {
            return Environment.getDataDirectory().getAbsolutePath() + "/data/"; // filePath:/data/data/
        }
    }

    /**
     * 判断是否有sd卡
     *
     * @return
     */
    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }
        return true;
    }

    public static long getFileSize(String filePath) {
        try {
            File tmp = new File(filePath);
            return tmp.length();
        } catch (Exception e) {
            e.printStackTrace();
            return -1l;
        }
    }


    public static boolean isOutLimit(String filepath) {
        Uri uri = Uri.parse(filepath);
        int picSize = (int) CommonUtil.getFileSize(uri.getPath());
        boolean sizeLimit = picSize > LIMIT_SIZE;
        return sizeLimit;
    }


    public static boolean checkAndCreateDir(String filePath, Context mContext) {
        if (filePath.startsWith("/data/data/")) {
            FileOutputStream fos;

            try {
                Log.d("",
                        "realfilepath:"
                                + filePath.substring(filePath.lastIndexOf("/") + 1));
                fos = mContext.openFileOutput(
                        filePath.substring(filePath.lastIndexOf("/") + 1),
                        Context.MODE_WORLD_WRITEABLE);
                fos.write(1);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }

        } else {
            File tmp = new File(filePath);
            File parentFile = tmp.getParentFile();

            if (!parentFile.exists() && !parentFile.mkdirs()) {
                Log.d("", "mkdir:" + parentFile.getPath() + " error!");
                return false;
            }
        }
        return true;
    }


    public static boolean isAndroid40FirmwareVersion() {

        if (mIsInAndroid40 >= 0) {
            if (mIsInAndroid40 > 0) {
                return true;
            } else {
                return false;
            }
        } else {

            int sdkVer = Integer.valueOf(android.os.Build.VERSION.SDK);
            if (sdkVer >= 14) {
                mIsInAndroid40 = 1;
                return true;
            } else {
                mIsInAndroid40 = 0;
                return false;
            }

        }
    }


    public static void recordvideo(Fragment fragment, int requestCode) {
        Context context = fragment.getContext();
//		int sizeLimit = CarrierContentRestriction.getMaxMessageSize()
//				- MESSAGE_OVERHEAD;
        //
        if (LIMIT_SIZE > 0) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.9);
            intent.putExtra("android.intent.extra.sizeLimit", Long.parseLong(String.valueOf(LIMIT_SIZE)));
            // if (CommonUtil.isAndroid20FirmwareVersion())
            // {
//            int durationLimit = getInt("ro.media.enc.lprof.duration", 60);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
            intent.putExtra("RequestedFrom", "mms");
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, Long.parseLong(String.valueOf(LIMIT_SIZE)));
            intent.putExtra("android.provider.MediaStore.extra.MAX_BYTES", Long.parseLong(String.valueOf(LIMIT_SIZE)));
            intent.putExtra("showfilesize", true);
            // }
            //
            if (context instanceof Activity) {
                fragment.startActivityForResult(intent, requestCode);
            }

        } else {
            Toast.makeText(context, "Message size limit reached.", Toast.LENGTH_SHORT).show();
        }

    }


}
