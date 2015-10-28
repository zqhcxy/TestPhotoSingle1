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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.testphoto.R;
import com.example.testphoto.fragment.ShowAllPhotoFragment;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.DensityUtil;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.views.SquareRelativeLayout;

import java.io.File;
import java.util.ArrayList;

/**
 * PhotoWall中GridView的适配器
 *
 * @author hanj
 */

public class PhotoWallAdapter extends BaseAdapter {
    //    private static final String PHOTO = "photo";
    private static final String SYSPHOTO = "Camera";
    private static final int SYSPHOTO_TYPE = 0;
    private static final int PHOTO_TYPE = 1;

    private Context context;
    private ArrayList<String> imagePathList = null;

//    private SDCardImageLoader loader;
    RelativeLayout.LayoutParams layoutParams;
    private Object myContext;
    private int imgw;

    public PhotoWallAdapter(Context context, Object myContext,
                            ArrayList<String> imagePathList) {
        this.context = context;
        this.imagePathList = imagePathList;
        this.myContext = myContext;

//        loader = new SDCardImageLoader(context, ScreenUtils.getScreenW(),
//                ScreenUtils.getScreenH(), 1);
        MySparseBooleanArray.init();
        imgw = (ScreenUtils.getScreenW() - DensityUtil.dip2px(context, 4)) / 3;
        layoutParams = new RelativeLayout.LayoutParams(imgw, imgw);
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
    public int getItemViewType(int position) {
        String filePath = (String) getItem(position);
        if (filePath.equals(SYSPHOTO)) {
            return SYSPHOTO_TYPE;
        } else if (!filePath.equals(SYSPHOTO)) {//在优化
            return PHOTO_TYPE;
        }

        return -1;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String filePath = (String) getItem(position);
        int viewtype = getItemViewType(position);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            if (viewtype == PHOTO_TYPE) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.photo_wall_item, null);
                holder.imageView = (ImageView) convertView
                        .findViewById(R.id.photo_wall_item_photo);
                holder.checkBox = (CheckBox) convertView
                        .findViewById(R.id.photo_wall_item_cb);
                holder.chackbox_ly = (LinearLayout) convertView
                        .findViewById(R.id.chackbox_ly);
                holder.media_iv_ly = (SquareRelativeLayout) convertView.findViewById(R.id.media_iv_ly);
            } else if (viewtype == SYSPHOTO_TYPE) {
                convertView = LayoutInflater.from(context).inflate(R.layout.sysyem_media_item, null);
                holder.sys_media_ly = (RelativeLayout) convertView.findViewById(R.id.sys_meida_ly);
                holder.sys_media_tv = (TextView) convertView.findViewById(R.id.sys_media_tv);
//                holder.sys_media_iv = (ImageView) convertView.findViewById(R.id.sys_media_iv);
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // holder.imageView.setTag(filePath);
        if (viewtype == SYSPHOTO_TYPE) {
            holder.sys_media_ly.setLayoutParams(layoutParams);
//            holder.media_iv_ly.setVisibility(View.GONE);
            holder.sys_media_ly.setVisibility(View.VISIBLE);
            holder.sys_media_tv.setText(R.string.photo_corder);
            Drawable drawable =convertView.getResources().getDrawable(R.drawable.ic_media_camera);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.sys_media_tv.setCompoundDrawables(null, drawable, null, null);
//            holder.sys_media_iv.setImageResource(R.drawable.ic_media_camera);
//            holder.checkBox.setVisibility(View.GONE);
//            holder.imageView.setImageResource(R.drawable.ic_media_camera);
        } else if (viewtype == PHOTO_TYPE) {
            holder.imageView.setLayoutParams(layoutParams);
            holder.media_iv_ly.setVisibility(View.VISIBLE);
//            holder.sys_media_ly.setVisibility(View.GONE);
//            holder.checkBox.setVisibility(View.VISIBLE);
            holder.chackbox_ly.setTag(R.id.tag_first, position);
            holder.checkBox.setChecked(MySparseBooleanArray.get(position));
            holder.chackbox_ly.setTag(R.id.tag_first, position);
            holder.chackbox_ly.setTag(R.id.tag_second, holder.imageView);

            if (!holder.checkBox.isChecked()) {
                holder.imageView.setColorFilter(null);
            }

            holder.chackbox_ly.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ImageView image = (ImageView) v.getTag(R.id.tag_second);
                    if (holder.checkBox.isChecked()) {
                        holder.checkBox.setChecked(false);
                    } else {
                        holder.checkBox.setChecked(true);
                    }
                    Integer position = (Integer) v.getTag(R.id.tag_first);
                    MySparseBooleanArray.clearSelectionMap();
                    MySparseBooleanArray.setSelectionData(position,
                            holder.checkBox.isChecked(), filePath);
                    // 单选就不设置选中效果了
                    if (holder.checkBox.isChecked()) {
                        image.setColorFilter(context.getResources().getColor(
                                R.color.image_checked_bg));
                    } else {
                        image.setColorFilter(null);
                    }

                    if (myContext instanceof ShowAllPhotoFragment)// 全部
                        ((ShowAllPhotoFragment) myContext).setConfirmEnable();
                    notifyDataSetChanged();

                    // 底部弹出区域
                    boolean ischoice = ((ShowAllPhotoFragment) myContext)
                            .isHaveChoice();
                    if (ischoice) {
                        ((ShowAllPhotoFragment) myContext).showButtonLy();
                    } else {
                        ((ShowAllPhotoFragment) myContext).hidButtonLy();
                    }
                }
            });

            // tag的key必须使用id的方式定义以保证唯一，否则会出现IllegalArgumentException.
            // holder.checkBox.setTag(R.id.tag_first, position);
            // holder.checkBox.setTag(R.id.tag_second, holder.imageView);
            // // 单选
            // holder.checkBox.setOnClickListener(new OnClickListener() {
            //
            // @Override
            // public void onClick(View buttonView) {
            //
            // Integer position = (Integer) buttonView
            // .getTag(R.id.tag_first);
            // MySparseBooleanArray.clearSelectionMap();
            // MySparseBooleanArray.setSelectionData(position,
            // holder.checkBox.isChecked(), filePath);
            // // 单选就不设置选中效果了
            // if (myContext instanceof ShowAllPhotoFragment)// 全部
            // ((ShowAllPhotoFragment) myContext).setConfirmEnable();
            // notifyDataSetChanged();
            //
            // }
            // });

            // 多选
            // holder.checkBox
            // .setOnCheckedChangeListener(new
            // CompoundButton.OnCheckedChangeListener() {
            // @Override
            // public void onCheckedChanged(CompoundButton buttonView,
            // boolean isChecked) {
            // Integer position = (Integer) buttonView
            // .getTag(R.id.tag_first);
            // ImageView image = (ImageView) buttonView
            // .getTag(R.id.tag_second);
            // // MySparseBooleanArray.clearSelectionMap();
            // MySparseBooleanArray.setSelectionData(position,
            // isChecked, filePath);
            // // selectionMap.put(position, isChecked);
            // if (isChecked) {
            // image.setColorFilter(context.getResources()
            // .getColor(R.color.image_checked_bg));
            // } else {
            // image.setColorFilter(null);
            // }
            // showPhotoActivity.sumSelectPho();
            // // notifyDataSetChanged();
            // }
            // });

//            loader.loadImage(4, filePath, holder.imageView);

            Glide.with(context).load(new File(filePath))
                    .placeholder(R.drawable.empty_photo).centerCrop()
                    .override(180, 180).crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.imageView);
            // 压缩比要是太小，图片太大会闪烁。
        }
        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
        LinearLayout chackbox_ly;

        private SquareRelativeLayout media_iv_ly;//正常显示图片的布局
        private RelativeLayout sys_media_ly;//系统相机布局
//        private ImageView sys_media_iv;//显示系统相机的图标
        private TextView sys_media_tv;//系统相机的标题
    }

}
