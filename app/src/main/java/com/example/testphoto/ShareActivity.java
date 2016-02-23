package com.example.testphoto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.testphoto.fragment.MsgFragmentActivity;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.CommonUtil;
import com.example.testphoto.util.FileUtils;
import com.example.testphoto.views.CropImageView1;
import com.example.testphoto.views.StrokeTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * 裁剪界面
 *
 * @author zqh-pc
 */
public class ShareActivity extends Activity implements OnClickListener {
    private CropImageView1 share_iv;
    // private ImageCut cut_pic_view;
    private ImageView preview_back;
    private StrokeTextView preview_confirm;
    private TextView titleTV;

    private ImageView share_rotation_iv;//旋转
    private ImageView share_reverse_iv;//翻转

    //    private SDCardImageLoader loader;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);
        initView();
        initData();
    }

    private void initView() {
        share_iv = (CropImageView1) findViewById(R.id.share_iv);
        preview_back = (ImageView) findViewById(R.id.topbar_left_iv);
        preview_confirm = (StrokeTextView) findViewById(R.id.topbar_right_btn);
        titleTV = (TextView) findViewById(R.id.topbar_title_tv);
        share_rotation_iv = (ImageView) findViewById(R.id.share_rotation_iv);
        share_reverse_iv = (ImageView) findViewById(R.id.share_reverse_iv);

        titleTV.setText(R.string.cut_photo);
//        preview_back.setText(R.string.main_cancel);
        preview_back.setVisibility(View.VISIBLE);
        preview_confirm.setVisibility(View.VISIBLE);
        preview_confirm.setText(R.string.main_confirm);

        preview_back.setOnClickListener(this);
        preview_confirm.setOnClickListener(this);
        share_rotation_iv.setOnClickListener(this);
        share_reverse_iv.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        path = intent.getStringExtra("sharePath");
        if (path == null)
            return;
        Uri uri;
        if (path.contains("file://")) {
            uri = Uri.parse(path);
            path = uri.getPath();
        } else if (path.contains("content://")) {
        } else {
            uri = Uri.fromFile(new File(path));
            path = uri.getPath();
        }
        setButtonEnable(false);
        loadPic(path);
        preview_confirm.setText(R.string.main_confirm);

    }

    /**
     * 加载预览图片
     *
     * @param filePath
     */
    private void loadPic(String filePath) {
        Uri uri;
        if (filePath.contains("content://")) {
            uri = Uri.parse(filePath);
        } else {
            uri = Uri.fromFile(new File(filePath));
        }
        DrawableTypeRequest<Uri> requst = Glide.with(this).load(uri);
        requst.dontAnimate();//为了不让默认图因不同尺寸而错乱。
        requst.asBitmap();

        requst.listener(new RequestListener<Uri, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                setButtonEnable(false);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                share_iv.setDrawable(resource, resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                setButtonEnable(true);
                return false;
            }
        }).into(share_iv);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.topbar_left_iv:
                finish();
                break;
            case R.id.topbar_right_btn:
                comfirmPhoto();
                break;
            case R.id.share_rotation_iv://旋转
                rotationPho(90);
                break;
            case R.id.share_reverse_iv:// 翻转
                reversePhoto();

                break;
            default:
                break;
        }
    }

    private void setButtonEnable(boolean enable) {
        preview_confirm.setEnabled(enable);
        share_reverse_iv.setEnabled(enable);
        share_rotation_iv.setEnabled(enable);
    }

    /**
     * 发送编辑后图片，可以直接发送bitmap，不需要再存本地后在发送地址。 目前预留两个方法
     */

    private void comfirmPhoto() {
        Intent intent = new Intent(this, MsgFragmentActivity.class);
        ArrayList<String> paths = new ArrayList<>();
        Bitmap mBitmap = share_iv.getCropImage();
//        Timestamp now = new Timestamp(System.currentTimeMillis());//获取系统当前时间
        long pic_name = new Date().getTime();
        String path = FileUtils.saveBitmap(mBitmap, pic_name + "", this);//时间戳
        mBitmap.recycle();
        paths.add("file://" + this.path);

        Uri uri = Uri.parse(path);
        //超过大小，要用压缩
        boolean sizeLimit = CommonUtil.isOutLimit(uri.getPath());
        intent.putExtra("compression", sizeLimit);
        intent.putExtra("iscomfirm", true);//返回的标志位，是否是确定选中图片了
        intent.putExtra("code", paths != null ? 100 : -1);
        intent.putStringArrayListExtra("paths", paths);
        intent.putExtra("sendBitmap", path);//编辑后图片的路径
        intent.putExtra("isnomalmms", sizeLimit);
        setResult(RESULT_OK, intent);
        MySparseBooleanArray.clearSelectionMap();
        finish();
    }

    /**
     * 旋转图片
     *
     * @param mrotation 旋转角度
     */
    private void rotationPho(int mrotation) {

        if (mrotation == 0) {
            return;
        }
        Drawable mDrawable = share_iv.getmDrawable();
        Bitmap bitmap = CommonUtil.drawableToBitmap(mDrawable);
        Matrix matrix = new Matrix();
        matrix.setRotate(mrotation);
        Bitmap bm1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        Drawable drawable = new BitmapDrawable(this.getResources(), bm1);
        share_iv.setDrawable(drawable, bm1.getWidth(), bm1.getHeight());
    }

    /**
     * 图片的翻转
     */
    private void reversePhoto() {
        Drawable mDrawable = share_iv.getmDrawable();
        Bitmap bitmap = CommonUtil.drawableToBitmap(mDrawable);
        bitmap = CommonUtil.reverseBitmap(bitmap, 0);//默认只有水平翻转，如果要垂直翻转，就把0改成1
        Drawable drawable = new BitmapDrawable(this.getResources(), bitmap);
        share_iv.setDrawable(drawable, bitmap.getWidth(), bitmap.getHeight());
    }

}
