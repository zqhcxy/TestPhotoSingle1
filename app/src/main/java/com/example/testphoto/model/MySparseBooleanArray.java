package com.example.testphoto.model;

import java.util.HashMap;

import android.util.SparseBooleanArray;

/**
 * 图片、视频与音频选中状态的存储对象。
 * 
 * @author zqh-pc
 * 
 */
public class MySparseBooleanArray {

	private static SparseBooleanArray selectionMap;
	private static HashMap<String, Integer> hashMap;

	public static void init() {
		if (selectionMap == null)
			selectionMap = new SparseBooleanArray();
		if (hashMap == null)
			hashMap = new HashMap<String, Integer>();
	}

	public static SparseBooleanArray getSelectionMap() {
		if (selectionMap == null)
			init();
		return selectionMap;
	}

	public static void clearSelectionMap() {
		if (selectionMap == null)
			init();
		else {
			selectionMap.clear();
			hashMap.clear();
		}
	}

	public static void setSelectionData(int position, boolean ischeck,
			String path) {
		if (selectionMap == null)
			init();
		selectionMap.put(position, ischeck);
		hashMap.put(path, position);
	}

	public static boolean get(int position) {
		if (selectionMap == null)
			init();
		return selectionMap.get(position);
	}

	/**
	 * 根据地址更改选中状态--预览全部都是选中状态的
	 * 
	 * @param path
	 * @param isCheck
	 */
	public static void setPathSelection(String path, boolean isCheck) {
		if (selectionMap == null)
			init();
		selectionMap.put(hashMap.get(path), isCheck);
	}


	public static boolean getPathSelection(String path) {
		if (hashMap == null)
			init();

		if (hashMap.get(path) == null)
			return false;
		return get(hashMap.get(path));
	}
}
