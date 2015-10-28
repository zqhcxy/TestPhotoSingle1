package com.example.testphoto.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件创建路径
 * */
public class FileUtil {
	private static final String APP_ROOT_DIR = ".TestpicCache";
	private static final String FILES_DIR = "files";
	private static final String SAVE_DIR = "save";
	private static final String CACHE_DIR = "cache";
	private static final String IMAGE_DIR = "images";

	public static boolean hasSDCard() {
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			return false;
		}
		return true;
	}

	public static String getRootFilePath() {
		if (hasSDCard()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/";// filePath:/sdcard/
		} else {
			return Environment.getDataDirectory().getAbsolutePath() + "/data/"; // filePath:/data/data/
		}
	}

	public static String getSaveFilePath() {
		return getRootFilePath() + APP_ROOT_DIR + "/" + FILES_DIR + "/"
				+ SAVE_DIR + "/";
	}

	/** 聊天用户主动保存原图的图片的保存路径 */
	public static String getpicPath() {
		return getRootFilePath() + APP_ROOT_DIR + "/" + FILES_DIR + "/"
				+ IMAGE_DIR + "/";

	}

	public static String getCacheFilePath() {
		return getRootFilePath() + APP_ROOT_DIR + "/" + FILES_DIR + "/"
				+ CACHE_DIR + "/";
	}

	public static boolean checkNetState(Context context) {
		boolean netstate = false;
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						netstate = true;
						break;
					}
				}
			}
		}
		return netstate;
	}

	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
}
