package com.example.testphoto.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 项目中已经有，直接用项目中的（个人工具类）
 * Created by zqh-pc on 2015/9/22.
 */
public class CommonUtil {

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
     * @param flag
     *            0为水平翻转；1：为垂直翻转
     * @return
     */
    public static Bitmap reverseBitmap(Bitmap bmp, int flag) {
//        if (ImageCache.get("reverseBitmap") != null) {
//            return ImageCache.get("reverseBitmap");
//        }
        float[] floats = null;
        switch (flag) {
            case 0: // 水平反转
                floats = new float[] { -1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f };
                break;
            case 1: // 垂直反转
                floats = new float[] { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
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
}
