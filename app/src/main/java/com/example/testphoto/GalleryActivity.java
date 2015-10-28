package com.example.testphoto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testphoto.adapter.PreViewPhotoAdapter;
import com.example.testphoto.util.CommonUtil;
import com.example.testphoto.util.FileUtils;
import com.example.testphoto.views.MyGalleryPageView;
import com.example.testphoto.zoom.PhotoView;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * 预览照片界面
 *
 * @author zqh-pc
 */

public class GalleryActivity extends Activity implements OnClickListener {

    private static final int SHARE_REQUSE = 0x000006;//截图界面的回调

//    private Button preview_back;
    private ImageView preview_back;
    private ImageView gallery_rotation;// 旋转
    private ImageView gallery_shear;

    private MyGalleryPageView preview_vp;
    private Button preview_confirm;
    private TextView preview_title;

    private CheckBox gy_compression_cb;
    private TextView gy_pic_size;

    private ArrayList<String> lists;
    private PreViewPhotoAdapter adapter;

    private int show_selection = 0;// 当前显示的第几张图片
    // 历史旋转角度
    private int rotation = 0;
    /**
     * 旋转的界面
     */
    private int whitchView = 0;

    private String type;//当前是哪个模式
    private static final String MAINACTIVITY_TYPE = "mainActivity";//主界面进来
    private static final String NOMALPHOTO_TYPE = "nomalAhoto";//正常预览
    private boolean ischangge = false;//是否进行图片编辑


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);

        initView();
        getintent();
        initData();
    }

    private void initView() {
        gallery_rotation = (ImageView) findViewById(R.id.gallery_rotation);
        gallery_shear = (ImageView) findViewById(R.id.gallery_shear);
        preview_back = (ImageView) findViewById(R.id.topbar_left_iv);
        preview_vp = (MyGalleryPageView) findViewById(R.id.preview_vp);
        preview_confirm = (Button) findViewById(R.id.topbar_right_btn);
        preview_title = (TextView) findViewById(R.id.topbar_title_tv);
        gy_compression_cb = (CheckBox) findViewById(R.id.gy_compression_cb);
        gy_pic_size = (TextView) findViewById(R.id.gy_pic_size);


//        preview_back.setText(R.string.main_back);
        preview_back.setVisibility(View.VISIBLE);
        preview_confirm.setVisibility(View.VISIBLE);
        preview_confirm.setText(R.string.main_confirm);
        gy_compression_cb.setChecked(false);

        preview_confirm.setOnClickListener(this);
        gallery_shear.setOnClickListener(this);
        preview_back.setOnClickListener(this);
        gallery_rotation.setOnClickListener(this);


    }

    private void getintent() {
        Intent intent = getIntent();
        type = intent.getStringExtra("Type");
        lists = (ArrayList<String>) intent.getSerializableExtra("pathlist");
        if (type.equals(MAINACTIVITY_TYPE)) {
            boolean compression = intent.getBooleanExtra("compression", false);
            preview_confirm.setText(R.string.photo_album);
            File file = new File(lists.get(0));
            preview_title.setText(file.getName());
            showCheckBoxData(0);
            gy_compression_cb.setChecked(compression);
        } else if (type.equals(NOMALPHOTO_TYPE)) {
            show_selection = intent.getIntExtra("selection", 0);
            if (lists.get(0).equals("Camera")) {
                lists.remove(0);
                show_selection = show_selection - 1;
            }
            int pho_size = lists.size();
            preview_confirm.setText(R.string.main_confirm);
            preview_title.setText(show_selection + 1 + "/" + pho_size);
            showCheckBoxData(show_selection);
        }

    }

    private void initData() {
        adapter = new PreViewPhotoAdapter(lists, this);
        preview_vp.setAdapter(adapter);

        preview_vp.setCurrentItem(show_selection);

        preview_vp.addOnPageChangeListener(new OnPageChangeListener() {
            /**
             * 页面选中的时候触发
             */
            @Override
            public void onPageSelected(int position) {
                showCheckBoxData(position);
                if (type.equals(NOMALPHOTO_TYPE)) {
                    preview_title.setText(position + 1 + "/" + lists.size());
                }
                if (rotation != 0) {// 如果旋转角度不为空就是有变化，就要把图片转回去
                    rotationPho(whitchView, -rotation);
                }
                final PhotoView photoView = (PhotoView) preview_vp.findViewById(preview_vp.getCurrentItem());
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BitmapDrawable bitmapDrawable = ((BitmapDrawable) photoView.getDrawable());
                        if (bitmapDrawable != null) {//用了这个方法后，延时估计可以去掉
                            Bitmap bitmap = bitmapDrawable.getBitmap();
                            if (bitmap == null) {
                                setEditButtonEnable(false, preview_vp.getCurrentItem());
                            } else {
                                setEditButtonEnable(true, preview_vp.getCurrentItem());
                            }
                        }
                    }
                }, 200);


            }

            /**
             * 页面滚动的时候触发
             */
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            /**
             * 页面滚动状态发生改变的时候触发
             */
            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void backAction() {
        if (type.equals(MAINACTIVITY_TYPE)) {
            confirmActivion();
        } else if (type.equals(NOMALPHOTO_TYPE)) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.topbar_left_iv:
                backAction();
                break;
            case R.id.topbar_right_btn:
                if (type.equals(MAINACTIVITY_TYPE)) {
                    Intent intent = new Intent();
                    intent.putExtra("code", 100);
                    intent.putExtra("changeview", "changeview");
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (type.equals(NOMALPHOTO_TYPE)) {
                    confirmActivion();
                }
                break;
            case R.id.gallery_shear:// 剪切

                Intent intent1 = new Intent(GalleryActivity.this,
                        ShareActivity.class);
                intent1.putExtra("sharePath",
                        lists.get(preview_vp.getCurrentItem()));
                startActivityForResult(intent1, SHARE_REQUSE);
                break;
            case R.id.gallery_rotation:// 旋转
                rotationPho(preview_vp.getCurrentItem(), 90);
                break;
            default:
                break;
        }

    }


    private void showCheckBoxData(int position) {
        if (gy_compression_cb.isChecked()) {
            gy_compression_cb.setChecked(false);
        }
        File file = new File(lists.get(position));
        String picleng = CommonUtil.getUserSpaceStr(file.length());
        gy_pic_size.setText("原图(" + picleng + ")");
    }


    /**
     * 旋转图片
     *
     * @param position  旋转图片的位置
     * @param mrotation 旋转角度
     */
    private void rotationPho(int position, int mrotation) {

        if (mrotation == 0) {
            return;
        }

        PhotoView photoView = (PhotoView) preview_vp.findViewById(position);
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) photoView.getDrawable());
        if (bitmapDrawable == null) {
            return;
        }
        ischangge = true;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        Matrix matrix = new Matrix();
        matrix.setRotate(mrotation);
        Bitmap bm1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        photoView.setImageBitmap(bm1);
        // views.get(position).setImageBitmap(bm1);
        // 记录当前界面的位置和当前的选中角度。
        whitchView = preview_vp.getCurrentItem();
        rotation = rotation + mrotation;
        if (rotation >= 360) {
            rotation = 0;
        }
    }


    private void confirmActivion() {
        ArrayList<String> selectedImageList = new ArrayList<>();
        selectedImageList.add(lists.get(preview_vp.getCurrentItem()));
        // 选择图片完成,回到起始页面

        Intent intent = new Intent();
        intent.putExtra("code", selectedImageList != null ? 100 : -1);
        intent.putStringArrayListExtra("paths", selectedImageList);
        intent.putExtra("compression", gy_compression_cb.isChecked());

        if (ischangge) {
            PhotoView photoView = (PhotoView) preview_vp.findViewById(preview_vp
                    .getCurrentItem());
//            Bitmap bitmap = ((BitmapDrawable) photoView.getDrawable()).getBitmap();
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) photoView.getDrawable());
            if (bitmapDrawable == null) {
                return;
            }
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Timestamp now = new Timestamp(System.currentTimeMillis());//获取系统当前时间

            String path = FileUtils.saveBitmap(bitmap, now.toString());//时间戳
            bitmap.recycle();
            if (path != null) {
                intent.putExtra("sendBitmap", path);
                Log.e("保存图片", "保存成功");
            } else {
                Log.e("保存图片", "保存失败");
            }
        }
        setResult(RESULT_OK, intent);
        finish();
    }


//    /**
//     * 对Bitmap进行压缩处理，不然无法传输--- 到时候整理成工具类
//     *
//     * @param photo
//     * @param newHeight
//     * @param context
//     * @return
//     */
//    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {
//        final float densityMultiplier = context.getResources().getDisplayMetrics().density;
//
//        int h = (int) (newHeight * densityMultiplier);
//        int w = (int) (h * photo.getWidth() / ((double) photo.getHeight()));
//
//        photo = Bitmap.createScaledBitmap(photo, w, h, true);
//
//        return photo;
//    }

    public void setEditButtonEnable(boolean clickble, int position) {
        if (position == preview_vp.getCurrentItem()) {
            gallery_rotation.setEnabled(clickble);
            gallery_shear.setEnabled(clickble);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == SHARE_REQUSE) {
            setResult(RESULT_OK, data);
            finish();
        }

    }
}
