package com.example.testphoto.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.example.testphoto.fragment.ShowAllPhotoFragment;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.CommonUtil;
import com.example.testphoto.util.DensityUtil;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.views.SquareRelativeLayout;

import java.util.ArrayList;

/**
 * PhotoWall中GridView的适配器
 *
 * @author hanj
 */

public class PhotoWallAdapter extends CursorAdapter {
    private static final String SYSPHOTO = "Camera";
    private static final int SYSPHOTO_TYPE = 0;
    private static final int PHOTO_TYPE = 1;

    private Context context;
    private ArrayList<String> syslist = null;

    RelativeLayout.LayoutParams layoutParams;
    private Object myContext;
    private int imgw;

    public PhotoWallAdapter(Context context, Object myContext, Cursor c, ArrayList<String> imagePathList) {
        super(context, c, true);
        this.context = context;
        syslist = imagePathList;
        this.myContext = myContext;

        MySparseBooleanArray.init();
        imgw = (ScreenUtils.getScreenW() - DensityUtil.dip2px(context, 4)) / 3;
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
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= syslist.size() - 1) {
            return SYSPHOTO_TYPE;
        } else {
            return PHOTO_TYPE;
        }
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

        if (viewtype == SYSPHOTO_TYPE) {
            holder.sys_media_ly.setLayoutParams(layoutParams);
            holder.sys_media_ly.setVisibility(View.VISIBLE);
            holder.sys_media_tv.setText(R.string.photo_corder);
            Drawable drawable = convertView.getResources().getDrawable(R.drawable.ic_media_camera);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.sys_media_tv.setCompoundDrawables(null, drawable, null, null);
        } else if (viewtype == PHOTO_TYPE) {
            holder.imageView.setLayoutParams(layoutParams);
            holder.media_iv_ly.setVisibility(View.VISIBLE);
            holder.chackbox_ly.setTag(R.id.tag_first, position);
            holder.checkBox.setChecked(MySparseBooleanArray.get(position));
            holder.chackbox_ly.setTag(R.id.tag_first, position);
            holder.chackbox_ly.setTag(R.id.tag_second, holder.imageView);

            if (holder.checkBox.isChecked()) {
                holder.imageView.setColorFilter(context.getResources().getColor(
                        R.color.image_checked_bg));
            } else {
                holder.imageView.setColorFilter(null);
            }

            holder.chackbox_ly.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Integer position = (Integer) v.getTag(R.id.tag_first);
                    ImageView image = (ImageView) v.getTag(R.id.tag_second);
                    if (holder.checkBox.isChecked()) {
                        holder.checkBox.setChecked(false);
                        ((ShowAllPhotoFragment) myContext).setCheckpos(-1);
                    } else {
                        holder.checkBox.setChecked(true);
                        ((ShowAllPhotoFragment) myContext).setCheckpos(position - syslist.size());
                    }
                    MySparseBooleanArray.clearSelectionMap();
                    MySparseBooleanArray.setSelectionData(position,
                            holder.checkBox.isChecked(), filePath);
                    if (holder.checkBox.isChecked()) {
                        image.setColorFilter(context.getResources().getColor(
                                R.color.image_checked_bg));
                    } else {
                        image.setColorFilter(null);
                    }
                    buttomLy(filePath, holder.checkBox.isChecked());
                    if (myContext instanceof ShowAllPhotoFragment)// 全部
                        ((ShowAllPhotoFragment) myContext).setConfirmEnable();
                    notifyDataSetChanged();
                }
            });

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

            Glide.with(context).load(Uri.parse(filePath)).asBitmap()
                    .placeholder(R.drawable.empty_photo).centerCrop()
                    .override(180, 180)
                    .into(holder.imageView);
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

    /**
     * 判断是否显示底部弹窗
     *
     * @param filepath
     */
    public void buttomLy(String filepath, boolean isCompression) {
        Uri uri = Uri.parse(filepath);
        int picSize = (int) CommonUtil.getFileSize(uri.getPath());
        boolean sizeLimit = CommonUtil.isOutLimit(uri.getPath());
//        int limitlent = 2 * 1024 * 1024;
        if (sizeLimit) {//需要压缩
            ((ShowAllPhotoFragment) myContext).setOriginal_cbCheck(true);
            // 底部弹出区域
            boolean ischoice = ((ShowAllPhotoFragment) myContext)
                    .isHaveChoice();
            if (ischoice) {
                ((ShowAllPhotoFragment) myContext).showButtonLy(true);
            } else {
                ((ShowAllPhotoFragment) myContext).hidButtonLy();
            }

        } else {//原图
            boolean ischoice = ((ShowAllPhotoFragment) myContext)
                    .isHaveChoice();
            if (ischoice) {
                ((ShowAllPhotoFragment) myContext).hidButtonLy();
            }
            if (myContext instanceof ShowAllPhotoFragment)// 全部
                ((ShowAllPhotoFragment) myContext).setCompression(isCompression);
        }
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
    private String getDataOfCursor(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        String filepath = cursor.getString(0);
        return "file://" + filepath;
    }
}
