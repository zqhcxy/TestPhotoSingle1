package com.example.testphoto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testphoto.fragment.MsgFragmentActivity;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.CommonUtil;
import com.example.testphoto.util.FileUtils;
import com.example.testphoto.util.SDCardImageLoader;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.views.CropImageView1;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * 裁剪界面
 *
 * @author zqh-pc
 */
public class ShareActivity extends Activity implements OnClickListener {
    private CropImageView1 share_iv;
    // private ImageCut cut_pic_view;
    private ImageView preview_back;
    private Button preview_confirm;
    private TextView titleTV;

    private ImageView share_rotation_iv;//旋转
    private ImageView share_reverse_iv;//翻转

    private SDCardImageLoader loader;
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
        preview_confirm = (Button) findViewById(R.id.topbar_right_btn);
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
        loader = new SDCardImageLoader(this, ScreenUtils.getScreenW(),
                ScreenUtils.getScreenH(), 4);

        Intent intent = getIntent();
        path = (String) intent.getExtras().get("sharePath");
        if (path == null)
            return;
        share_iv.setTag(path);
        loader.loadImage(3, path, share_iv);

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

    /**
     * 发送编辑后图片，可以直接发送bitmap，不需要再存本地后在发送地址。 目前预留两个方法
     */

    private void comfirmPhoto() {
        Intent intent = new Intent(this, MsgFragmentActivity.class);
        ArrayList<String> paths = new ArrayList<>();
        Bitmap mBitmap = share_iv.getCropImage();
        Timestamp now = new Timestamp(System.currentTimeMillis());//获取系统当前时间
        String path = FileUtils.saveBitmap(mBitmap, now.toString());//时间戳
        mBitmap.recycle();
        paths.add(this.path);
        intent.putExtra("code", paths != null ? 100 : -1);
        intent.putStringArrayListExtra("paths", paths);
        intent.putExtra("sendBitmap", path);//编辑后图片的路径
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
