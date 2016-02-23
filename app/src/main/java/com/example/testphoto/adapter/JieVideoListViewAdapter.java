package com.example.testphoto.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.testphoto.R;
import com.example.testphoto.fragment.ShowVideoFragment;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.model.MyVideo;
import com.example.testphoto.util.DensityUtil;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.views.SquareRelativeLayout;

import java.util.ArrayList;

public class JieVideoListViewAdapter extends CursorAdapter {
    private static final int SYSVIDEO_TYPE = 0;
    private static final int VIDEO_TYPE = 1;
    private LayoutInflater mLayoutInflater;
    private RelativeLayout.LayoutParams layoutParams;
    private ShowVideoFragment showVideoFragment;
    private Context context;
    private ArrayList<String> syslist;

    public JieVideoListViewAdapter(Context context, Cursor c, ShowVideoFragment showVideoFragment, ArrayList<String> list) {
        super(context, c, true);
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
        syslist = list;
        this.showVideoFragment = showVideoFragment;
        int imgw = (ScreenUtils.getScreenW() - DensityUtil.dip2px(context, 4)) / 3;
        layoutParams = new RelativeLayout.LayoutParams(imgw, imgw);
    }

    @Override
    public int getCount() {
        Cursor cursor = getCursor();
        return cursor.getCount() + syslist.size();
    }

    @Override
    public Object getItem(int position) {
        if (position <= syslist.size() - 1) {
            return syslist.get(position);
        } else {
            return getDataOfCursor(position - syslist.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return position - syslist.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= syslist.size() - 1) {
            return SYSVIDEO_TYPE;
        } else {
            return VIDEO_TYPE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        final ViewHolder holder;
        int viewtype = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            if (viewtype == VIDEO_TYPE) {
                convertView = mLayoutInflater.inflate(
                        R.layout.photo_wall_item, null);
                holder.imageView = (ImageView) convertView
                        .findViewById(R.id.photo_wall_item_photo);
                holder.checkBox = (CheckBox) convertView
                        .findViewById(R.id.photo_wall_item_cb);
                holder.chackbox_ly = (LinearLayout) convertView
                        .findViewById(R.id.chackbox_ly);
                holder.video_ico = (ImageView) convertView.findViewById(R.id.video_ico);
                holder.media_iv_ly = (SquareRelativeLayout) convertView.findViewById(R.id.media_iv_ly);
                holder.video_ico.setVisibility(View.VISIBLE);
            } else if (viewtype == SYSVIDEO_TYPE) {
                convertView = mLayoutInflater.inflate(R.layout.sysyem_media_item, null);
                holder.sys_media_ly = (RelativeLayout) convertView.findViewById(R.id.sys_meida_ly);
                holder.sys_media_tv = (TextView) convertView.findViewById(R.id.sys_media_tv);
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (viewtype == SYSVIDEO_TYPE) {
            holder.sys_media_ly.setLayoutParams(layoutParams);
            holder.sys_media_ly.setVisibility(View.VISIBLE);
            holder.sys_media_tv.setText(R.string.video_corder);
            Drawable drawable = convertView.getResources().getDrawable(R.drawable.ic_media_videocorder);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.sys_media_tv.setCompoundDrawables(null, drawable, null, null);
        } else if (viewtype == VIDEO_TYPE) {
            final String videoPath = getDataOfCursor(position - syslist.size());
            final int videoID = getVideoIDOfCursor(position - syslist.size());

            holder.imageView.setLayoutParams(layoutParams);
            holder.media_iv_ly.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(MySparseBooleanArray.get(videoID));
            holder.chackbox_ly.setTag(R.id.tag_first, videoID);
            holder.chackbox_ly.setTag(R.id.tag_second, holder.imageView);
            holder.chackbox_ly.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean checkboxIscheck = holder.checkBox.isChecked();
                    boolean isout =  showVideoFragment.buttomLy(getDataOfCursor(position - syslist.size()), checkboxIscheck);
                    if (!isout) {
                        if (checkboxIscheck) {
                            holder.checkBox.setChecked(false);
                        } else {
                            holder.checkBox.setChecked(true);
                        }
                        Integer position = (Integer) v.getTag(R.id.tag_first);
                        MySparseBooleanArray.clearSelectionMap();
                        MySparseBooleanArray.setSelectionData(position,
                                holder.checkBox.isChecked(), videoPath);
                        // 单选就不设置选中效果了

                        showVideoFragment.setConfirmEnable();
                        notifyDataSetChanged();
                    }
                }
            });
            Glide.with(context).load(Uri.parse(videoPath))
                    .placeholder(R.drawable.empty_photo).dontAnimate()
                    .override(180, 180).into(holder.imageView);

        }
        return convertView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }


    private void setVideoTime(MyVideo myVideo, ViewHolder holder) {
        long min = myVideo.getDuration() / 1000 / 60;
        long sec = myVideo.getDuration() / 1000 % 60;
//        holder.time.setText(min + " : " + sec);
    }


    /**
     * 获取视频的地址
     *
     * @param position
     * @return
     */
    private String getDataOfCursor(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        String filepath = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        return "file://" + filepath;
    }

    /**
     * 获取视频的id
     *
     * @param position
     * @return
     */
    private int getVideoIDOfCursor(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        int id = cursor.getInt(cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media._ID));
        return id;
    }

    public final class ViewHolder {
        public ImageView imageView;
        // public TextView title;
        public ImageView video_ico;
        private CheckBox checkBox;
        private LinearLayout chackbox_ly;

        private SquareRelativeLayout media_iv_ly;//正常显示视频的布局
        private RelativeLayout sys_media_ly;//系统摄像机布局
        private TextView sys_media_tv;//系统摄像机的标题
    }
}
