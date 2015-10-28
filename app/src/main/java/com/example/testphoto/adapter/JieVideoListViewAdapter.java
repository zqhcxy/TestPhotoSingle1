package com.example.testphoto.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.testphoto.R;
import com.example.testphoto.fragment.ShowVideoFragment;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.model.MyVideo;
import com.example.testphoto.util.DensityUtil;
import com.example.testphoto.util.SDCardImageLoader;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.views.SquareRelativeLayout;

import java.util.List;

public class JieVideoListViewAdapter extends BaseAdapter {
    private static final String SYSVIDEO = "MyVideo";
    private static final int SYSVIDEO_TYPE = 0;
    private static final int VIDEO_TYPE = 1;
    private List<MyVideo> listVideos;
    //    int local_postion = 0;
//    boolean imageChage = false;
    private LayoutInflater mLayoutInflater;
    private SDCardImageLoader loader;
    private RelativeLayout.LayoutParams layoutParams;
    private ShowVideoFragment showVideoFragment;
    private Context context;

    public JieVideoListViewAdapter(Context context,
                                   ShowVideoFragment showVideoFragment, List<MyVideo> listVideos) {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.listVideos = listVideos;
        this.showVideoFragment = showVideoFragment;
        loader = new SDCardImageLoader(context, ScreenUtils.getScreenW(),
                ScreenUtils.getScreenH(), 2);
        int imgw = (ScreenUtils.getScreenW() - DensityUtil.dip2px(context, 4)) / 3;
        layoutParams = new RelativeLayout.LayoutParams(imgw, imgw);
    }

    @Override
    public int getCount() {
        return listVideos.size();
    }

    @Override
    public Object getItem(int position) {
        return listVideos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        String filePath = listVideos.get(position).getPath();
        if (filePath.equals(SYSVIDEO)) {
            return SYSVIDEO_TYPE;
        } else if (!filePath.equals(SYSVIDEO)) {//在优化
            return VIDEO_TYPE;
        }

        return -1;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        final ViewHolder holder;
        // final String filePath = listVideos.get(position).getPath();
        final MyVideo myVideo = listVideos.get(position);
        int viewtype = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            if (viewtype == VIDEO_TYPE) {
                convertView = LayoutInflater.from(context).inflate(
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
                convertView = LayoutInflater.from(context).inflate(R.layout.sysyem_media_item, null);
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
            Drawable drawable =convertView.getResources().getDrawable(R.drawable.ic_media_videocorder);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.sys_media_tv.setCompoundDrawables(null, drawable, null, null);
        } else if (viewtype == VIDEO_TYPE) {
            holder.imageView.setLayoutParams(layoutParams);
            holder.media_iv_ly.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(MySparseBooleanArray.get(position));
            holder.chackbox_ly.setTag(R.id.tag_first, position);
            holder.chackbox_ly.setTag(R.id.tag_second, holder.imageView);
            holder.chackbox_ly.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (holder.checkBox.isChecked()) {
                        holder.checkBox.setChecked(false);
                    } else {
                        holder.checkBox.setChecked(true);
                    }
                    Integer position = (Integer) v.getTag(R.id.tag_first);
                    MySparseBooleanArray.clearSelectionMap();
                    MySparseBooleanArray.setSelectionData(position,
                            holder.checkBox.isChecked(), myVideo.getPath());
                    // 单选就不设置选中效果了

                    showVideoFragment.setConfirmEnable();
                    notifyDataSetChanged();

                    // 底部弹出区域
                    boolean ischoice = showVideoFragment
                            .isHaveChoice();
                    if (ischoice) {
                        showVideoFragment.showButtonLy();
                    } else {
                        showVideoFragment.hidButtonLy();
                    }
                }
            });
            loader.loadImageForVideo(4, myVideo.getId(),
                    listVideos.get(position).getPath(), holder.imageView);// 压缩比要是太小，图片太大会闪烁。(第一个参数已经没用)

        }
        return convertView;
    }


    private void setVideoTime(MyVideo myVideo, ViewHolder holder) {
        long min = myVideo.getDuration() / 1000 / 60;
        long sec = myVideo.getDuration() / 1000 % 60;
//        holder.time.setText(min + " : " + sec);
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
