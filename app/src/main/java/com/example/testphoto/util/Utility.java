package com.example.testphoto.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * 判断是有sd卡。判断是否是图片与视频工具类
 */
public class Utility {

	/**
	 * 判断SD卡是否可用
	 */
	public static boolean isSDcardOK() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取SD卡跟路径。SD卡不可用时，返回null
	 */
	public static String getSDcardRoot() {
		if (isSDcardOK()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}

		return null;
	}

	public static void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(Context context, int msgId) {
		Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
	}

	/** 获取字符串中某个字符串出现的次数。 */
	public static int countMatches(String res, String findString) {
		if (res == null) {
			return 0;
		}

		if (findString == null || findString.length() == 0) {
			throw new IllegalArgumentException(
					"The param findString cannot be null or 0 length.");
		}

		return (res.length() - res.replace(findString, "").length())
				/ findString.length();
	}

	/** 判断该文件是否是一个图片。 */
	public static boolean isImage(String fileName) {
		return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
				|| fileName.endsWith(".png");
	}

	/** 判断该文件是否是一个视频。 */
	public static boolean isVideo(String fileName) {
		return fileName.endsWith(".mp4") || fileName.endsWith(".3gp")
				|| fileName.endsWith(".avi") || fileName.endsWith(".wmv");
	}

	/** 判断该文件是否是一个视频。 */
	public static boolean isAudio(String fileName) {
		return fileName.endsWith(".mp3") || fileName.endsWith(".wav")
				|| fileName.endsWith(".ape") || fileName.endsWith(".aiff")
				|| fileName.endsWith(".flac");
	}

}
