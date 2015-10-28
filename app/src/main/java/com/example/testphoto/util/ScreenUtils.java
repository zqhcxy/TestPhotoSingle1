package com.example.testphoto.util;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * ScreenUtils,获取屏幕宽高工具类
 * 
 */
public class ScreenUtils {
	private static int screenW;
	private static int screenH;
	private static float screenDensity;

	public static void initScreen(Activity mActivity) {
		DisplayMetrics metric = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenW = metric.widthPixels;
		screenH = metric.heightPixels;
		screenDensity = metric.density;
	}

	public static int getScreenW() {
		return screenW;
	}

	public static int getScreenH() {
		return screenH;
	}

	public static float getScreenDensity() {
		return screenDensity;
	}

	/** 根据手机的分辨率从 dp 的单位 转成为 px(像素) */
	public static int dp2px(float dpValue) {
		return (int) (dpValue * getScreenDensity() + 0.5f);
	}

	/** 根据手机的分辨率从 px(像素) 的单位 转成为 dp */
	public static int px2dp(float pxValue) {
		return (int) (pxValue / getScreenDensity() + 0.5f);
	}
}
