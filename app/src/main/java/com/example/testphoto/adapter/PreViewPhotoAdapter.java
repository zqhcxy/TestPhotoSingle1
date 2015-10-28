package com.example.testphoto.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.testphoto.GalleryActivity;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.zoom.PhotoView;

import java.io.File;
import java.util.ArrayList;

/**
 * 预览图片适配器
 *
 * @author zqh-pc
 */
public class PreViewPhotoAdapter extends PagerAdapter {
    private ArrayList<PhotoView> views;
    private ArrayList<String> lists;
    Context context;

    public PreViewPhotoAdapter(ArrayList<String> lists, Context context) {
        this.lists = lists;
        this.context = context;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public int getItemPosition(Object object) {
        // TODO Auto-generated method stub
        return super.getItemPosition(object);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        // PhotoView img = new PhotoView(context);
        // img.setId(position);
        // img.setBackgroundColor(0xff000000);
        // img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        // LayoutParams.WRAP_CONTENT));
        //
        // Glide.with(context).load(new File(lists.get(position))).asBitmap()
        // .override(ScreenUtils.getScreenW(), 400)
        // .placeholder(R.drawable.empty_photo).centerCrop()
        // .diskCacheStrategy(DiskCacheStrategy.ALL).into(img);
        // Log.e("Glide地址", Glide.getPhotoCacheDir(context).toString());
        PhotoView img = setShowView(position);
        container.addView(img);
        return img;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // container.removeView(views.get(position));
        container.removeView((View) object);
    }

    private PhotoView setShowView(final int position) {
        PhotoView img = new PhotoView(context);
        img.setId(position);
        img.setBackgroundColor(0xff000000);
        img.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(lists.get(position), opt);
        // 获取到这个图片的原始宽度和高度
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;
        // 读取图片失败时直接返回
        if (picWidth == 0 || picHeight == 0) {
            return img;
        }

        if (picWidth > ScreenUtils.getScreenW()) {
            picWidth = ScreenUtils.getScreenW();
        }
        if (picHeight >= ScreenUtils.getScreenH()) {
            picHeight = ScreenUtils.getScreenH();
        }


        DrawableTypeRequest<File> requst = Glide.with(context).load(new File(lists.get(position)));
        requst.listener(new RequestListener<File, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                ((GalleryActivity) context).setEditButtonEnable(false, position);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                ((GalleryActivity) context).setEditButtonEnable(true,position);
                return false;
            }
        });
        requst.asBitmap().override(picWidth, picHeight)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(img);
//        Glide.with(context).load(new File(lists.get(position)))
//                .asBitmap().override(picWidth, picHeight)
//                .placeholder(R.drawable.empty_photo)
//                .diskCacheStrategy(DiskCacheStrategy.ALL).into(img);
        return img;
    }

}
