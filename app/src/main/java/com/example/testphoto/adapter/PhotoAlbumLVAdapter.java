package com.example.testphoto.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.testphoto.R;
import com.example.testphoto.fragment.AlbumActivity;
import com.example.testphoto.model.PhotoAlbumLVItem;
import com.example.testphoto.util.DensityUtil;
import com.example.testphoto.util.ScreenUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * 选择相册页面,ListView的adapter Created by hanj on 14-10-14.
 */
public class PhotoAlbumLVAdapter extends BaseAdapter {
//    private static final int PHOTO_TYPE = 1;
//    private static final int VIDEO_TYPE = 0;
//    private static final int AUDIO_TYPE = 2;
    private Context context;
    private ArrayList<PhotoAlbumLVItem> list;

//    private SDCardImageLoader loader;
    RelativeLayout.LayoutParams layoutParams;
    private int type;

    public PhotoAlbumLVAdapter(Context context,
                               ArrayList<PhotoAlbumLVItem> list, int type) {
        this.context = context;
        this.list = list;
        this.type = type;
//        loader = new SDCardImageLoader(context, ScreenUtils.getScreenW(),
//                ScreenUtils.getScreenH(), type);
        int imgw = (ScreenUtils.getScreenW() - DensityUtil.dip2px(context, 4)) / 3;
        layoutParams = new RelativeLayout.LayoutParams(imgw, imgw);
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            //这里并不是多布局共用，而是因为多个布局的adapter合在这里写，每次只会走一个类型。不会同时具备多个类型
            if (type == AlbumActivity.AUDIO_TYPE) {// 音频(其实可以分出一个单独类和adapter，但是会增加代码量。)
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.audiofold_item, null);
                holder.audio_fold_iv = (ImageView) convertView
                        .findViewById(R.id.audio_fold_iv);
                holder.audio_fold_title = (TextView) convertView
                        .findViewById(R.id.audio_fold_title);
                holder.audio_fold_cound = (TextView) convertView
                        .findViewById(R.id.audio_fold_cound);
            } else {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.photo_album_lv_item, null);
                holder.firstImageIV = (ImageView) convertView
                        .findViewById(R.id.select_img_gridView_img);
                holder.path_filename_tv = (TextView) convertView
                        .findViewById(R.id.path_filename_tv);
                holder.path_file_count = (TextView) convertView
                        .findViewById(R.id.path_file_count);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (type == AlbumActivity.AUDIO_TYPE) {// 音频不需要图片
            holder.audio_fold_title.setTextColor(context.getResources()
                    .getColor(R.color.world));
            getPathNameToShowAudio(list.get(position), holder);
//            holder.audio_fold_iv.setLayoutParams(layoutParams);
//            holder.audio_fold_iv.setImageResource(R.drawable.ic_file_folder);
        } else {// 视频和图片的目录
            holder.firstImageIV.setLayoutParams(layoutParams);
            getPathNameToShowForPV(list.get(position),holder);
            // 图片（缩略图）
            String filePath = list.get(position).getFirstImagePath();
            Uri uri;
            if(filePath.contains("file://")){
                uri= Uri.parse(filePath);
            }else{
                uri= Uri.fromFile(new File(filePath));
            }
            Glide.with(context).load(uri)
                    .placeholder(R.drawable.empty_photo).centerCrop()
                    .override(180, 180).crossFade()
                    .into(holder.firstImageIV);
        }

        return convertView;
    }

    private class ViewHolder {
        // 视频或图片
        ImageView firstImageIV;
        TextView path_filename_tv;
        TextView path_file_count;
        // 音频
        ImageView audio_fold_iv;
        TextView audio_fold_title;
        TextView audio_fold_cound;
    }

    /**
     * 根据完整路径，获取最后一级路径，并拼上文件数用以显示。
     */
    private void getPathNameToShowForPV(PhotoAlbumLVItem item, ViewHolder holder) {
        String absolutePath = item.getPathName();
        int lastSeparator = absolutePath.lastIndexOf(File.separator);

        holder.path_file_count.setText(item.getFileCount() + "");
        holder.path_filename_tv.setText(absolutePath.substring(lastSeparator + 1) + "");
    }

    /**
     * 根据完整路径，获取最后一级路径，并拼上文件数用以显示。
     */
    private void getPathNameToShowAudio(PhotoAlbumLVItem item, ViewHolder holder) {
        String absolutePath = item.getPathName();
        int lastSeparator = absolutePath.lastIndexOf(File.separator);

        holder.audio_fold_title.setText(absolutePath
                .substring(lastSeparator + 1));
        holder.audio_fold_cound.setText(item.getFileCount() + "");
        // return absolutePath.substring(lastSeparator + 1) + "("
        // + item.getFileCount() + ")";
    }

}
