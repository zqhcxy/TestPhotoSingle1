package com.example.testphoto.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.testphoto.GalleryActivity;
import com.example.testphoto.R;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.zoom.PhotoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 预览图片适配器
 *
 * @author zqh-pc
 */
public class PreViewPhotoAdapter extends PagerAdapter {
    private ArrayList<String> lists;
    private Cursor cursor;
    private Context context;
    private String type;
    private String mmsplus_linkpath;
    ProgressBar galleryitem_pb;

    public PreViewPhotoAdapter(String type, Context context) {

        this.type = type;
        this.context = context;
    }

    public void setData(ArrayList<String> lists) {
        this.lists = lists;
    }

    public void setData(Cursor cursor) {
        this.cursor = cursor;
    }

    public void setData(String filepath) {
        mmsplus_linkpath = filepath;
    }

    @Override
    public int getCount() {
        if (type.equals(GalleryActivity.MAINACTIVITY_TYPE) || type.equals(GalleryActivity.CAMERARESULT_TYPE)) {
            return lists.size();
        } else {
            return cursor.getCount();
        }
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
        View view = LayoutInflater.from(context).inflate(R.layout.gallerypic_item, null);
        PhotoView photoView = (PhotoView) view.findViewById(R.id.galleryitem_pv);
//        photoView.setMinScale(16f);
        galleryitem_pb = (ProgressBar) view.findViewById(R.id.galleryitem_pb);
        boolean hidpb = ((GalleryActivity) context).isReadyMap.get(position);
        galleryitem_pb.setTag(position);
        if (hidpb) {
            galleryitem_pb.setVisibility(View.GONE);
        } else {
            galleryitem_pb.setVisibility(View.VISIBLE);
        }
        setShowView(position, photoView);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private PhotoView setShowView(final int position, PhotoView img) {
        img.setId(position);
        img.setBackgroundColor(0xff000000);

        String pic_path = getFilePath(position);
        if (pic_path != null) {
            Uri uri;
            if (type.equals(GalleryActivity.MAINACTIVITY_TYPE)||type.equals(GalleryActivity.CAMERARESULT_TYPE)) {// 收藏是URl地址
                uri = Uri.parse(pic_path);
            }  else {
                uri = Uri.fromFile(new File(pic_path));
            }
            showGlidePicForUri(position, img, uri);
        }
        return img;
    }


    /**
     * 读取的本地文件路径
     *
     * @param position
     * @return
     */
    private String getFilePath(int position) {
       if (type.equals(GalleryActivity.MAINACTIVITY_TYPE)||type.equals(GalleryActivity.CAMERARESULT_TYPE)) {
            return lists.get(position);
        }else if(type.equals(GalleryActivity.NOMALPHOTO_TYPE)){
           cursor.moveToPosition(position);
           return cursor.getString(0);
       }
        return null;

    }


    private PhotoView showGlidePicForUri(final int position, final PhotoView img, Uri uri) {
        Log.d("", "hc gallery show uri:" + (uri == null ? "null" : uri.toString()));
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            InputStream inputStream = null;
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                BitmapFactory.decodeStream(inputStream, null, opt);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 获取到这个图片的原始宽度和高度
            int picWidth = opt.outWidth;
            int picHeight = opt.outHeight;
            int newWidth = 0;
            int newHeight = 0;
            Log.d("", "preview mms or smms image:wh=" + picWidth + "*" + picHeight);
            // 读取图片失败时直接返回
            if (picWidth == 0 || picHeight == 0 || picWidth == -1 || picHeight == -1) {
                return img;
            }
            int screenH = ScreenUtils.getScreenH();
            int screenW = ScreenUtils.getScreenW();

            float pic_ratio = (float) picHeight / (float) picWidth;
            float screen_ratio = (float) screenH / (float) screenW;

            /*
                a、先获取图片、屏幕的比例。
                b、对比两个比例，图片的比例大就是高可能超出了边界。图片的比例小就是宽可能超出边界。
                c、对比比例大的那个边，看是否大于屏幕，没有就用图片自己的大小，如果有就进行各自的缩放。按比例缩放。
             */
            if (pic_ratio > screen_ratio) {//高可能超出屏幕高
                if (picHeight > screenH) {//就缩放
                    newHeight = screenH;
                    newWidth = (picWidth * newHeight) / picHeight;
                } else {//没有超就按照原始大小
                    newHeight = picHeight;
                    newWidth = picWidth;
                }
            } else {
                if (picWidth > screenW) {
                    newWidth = screenW;
                    newHeight = (picHeight * newWidth) / picWidth;
                } else {
                    newHeight = picHeight;
                    newWidth = picWidth;
                }
            }

            DrawableTypeRequest<Uri> requst = Glide.with(context).loadFromMediaStore(uri);
            requst.listener(new RequestListener<Uri, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                    setEnableOfButton(position, false);
                    galleryitem_pb.setVisibility(View.GONE);
//                    img.setBackgroundResource(R.drawable.ic_image_failure);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    setEnableOfButton(position, true);
//                    img.setBackgroundResource(0);
                    return false;
                }
            }).dontAnimate().fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE);

//            if (newHeight == picHeight && newWidth == picWidth) {
//                requst.override(newWidth, newHeight);
//            }
            requst.into(img);
        return img;
    }


    private void setEnableOfButton(int position, boolean clickble) {
        ((GalleryActivity) context).isReadyMap.put(position, clickble);
        if (clickble) {//成功的就再次手动去设置按钮使能，预防第一张加载慢没有刷新。
            ((GalleryActivity) context).setEditButtonEnable(position);
        }
    }
}
