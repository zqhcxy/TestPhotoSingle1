package com.example.testphoto.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.testphoto.R;
import com.example.testphoto.model.MainShowPhoto;
import com.example.testphoto.util.SDCardImageLoader;
import com.example.testphoto.util.ScreenUtils;

/**
 * 主页面中GridView的适配器
 * 
 * @author hanj
 */

public class MainGVAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<MainShowPhoto> imagePathList = null;

	private SDCardImageLoader loader;

	public MainGVAdapter(Context context, ArrayList<MainShowPhoto> imagePathList) {
		this.context = context;
		this.imagePathList = imagePathList;

		loader = new SDCardImageLoader(context, ScreenUtils.getScreenW(),
				ScreenUtils.getScreenH(), 1);
	}

	@Override
	public int getCount() {
		return imagePathList == null ? 0 : imagePathList.size();
	}

	@Override
	public Object getItem(int position) {
		return imagePathList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MainShowPhoto showPhoto = imagePathList.get(position);

		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.main_gridview_item, null);
			holder = new ViewHolder();

			holder.imageView = (ImageView) convertView
					.findViewById(R.id.main_gridView_item_photo);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String filePath = showPhoto.getPath();
		if (!TextUtils.isEmpty(filePath)) {
			holder.imageView.setTag(filePath);
			loader.loadImage(2, filePath, holder.imageView);
		} else {
			Bitmap bitmap = showPhoto.getBitmap();
			if (bitmap != null) {
				holder.imageView.setImageBitmap(bitmap);
			}

		}

		return convertView;
	}

	private class ViewHolder {
		ImageView imageView;
	}

}
