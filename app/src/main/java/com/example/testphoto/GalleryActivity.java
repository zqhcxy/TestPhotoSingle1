package com.example.testphoto;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testphoto.adapter.PreViewPhotoAdapter;
import com.example.testphoto.fragment.AlbumActivity;
import com.example.testphoto.util.CommonUtil;
import com.example.testphoto.util.FileUtils;
import com.example.testphoto.views.MyGalleryPageView;
import com.example.testphoto.views.StrokeTextView;
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
    private StrokeTextView preview_confirm;
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
    public static final String MAINACTIVITY_TYPE = "mainActivity";//主界面进来(附件区域)
    public static final String CAMERARESULT_TYPE = "cameraresult";//拍照回调
    public static final String NOMALPHOTO_TYPE = "nomalAhoto";//正常预览
    private boolean ischangge = false;//是否进行图片编辑
    private int checkPos = -1;//外部选中的图片位置
    private boolean compression;//是否是压缩图（选中图片的）
    private boolean checkboxState;
    private boolean showArtwork = true;// 是否显示原图按钮
    private Cursor cursor;
    private int dataSize;
    public int lastdata = 0;//默认是正常绝对地址预览
    public SparseBooleanArray isReadyMap;//保存已经加载好的图片

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
        preview_confirm = (StrokeTextView) findViewById(R.id.topbar_right_btn);
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
        isReadyMap = new SparseBooleanArray();
        Intent intent = getIntent();
        type = intent.getStringExtra("Type");
        show_selection = intent.getIntExtra("selection", 0);//要预览的图片位置
        compression = intent.getBooleanExtra("compression", true);//是否是缩略图
        checkboxState = compression;
        adapter = new PreViewPhotoAdapter(type, this);
        if (type.equals(MAINACTIVITY_TYPE) || type.equals(CAMERARESULT_TYPE)) {
            lists = (ArrayList<String>) intent.getSerializableExtra("pathlist");
            lastdata = intent.getIntExtra("lastdata", 0);
            if (type.equals(MAINACTIVITY_TYPE)) {
                preview_confirm.setText(R.string.photo_album);
            } else {
                preview_confirm.setText(R.string.main_confirm);
            }
            checkPos = 0;
            preview_title.setText(R.string.pref_prepare_look_title);
            showCheckBoxData(0);
            adapter.setData(lists);
            preview_vp.setAdapter(adapter);
            dataSize = lists.size();
            CanEdit(lists.get(0));
        } else if (type.equals(NOMALPHOTO_TYPE)) {
            String folderPath = intent.getStringExtra("folderPath");
            checkPos = intent.getIntExtra("checkPos", -1);
//            isHidButtomlt = intent.getBooleanExtra("is_hid", false);

            cursor = getCursor(type, folderPath);
            adapter.setData(cursor);
            preview_vp.setAdapter(adapter);
            dataSize = cursor.getCount();
            preview_confirm.setText(R.string.main_confirm);
            preview_title.setText(show_selection + 1 + "/" + dataSize);
            showCheckBoxData(show_selection);
        }

    }

    private void initData() {

        preview_vp.setCurrentItem(show_selection);
        setEditButtonEnable(show_selection);
        preview_vp.addOnPageChangeListener(new OnPageChangeListener() {
            /**
             * 页面选中的时候触发
             */
            @Override
            public void onPageSelected(int position) {
                showCheckBoxData(position);
                CanEdit(getFilePath(position));
                if (type.equals(NOMALPHOTO_TYPE)) {
                    show_selection = position;
                    preview_title.setText(position + 1 + "/" + dataSize);
                }
                if (rotation != 0) {// 如果旋转角度不为空就是有变化，就要把图片转回去
                    rotationPho(whitchView, -rotation);
                }
                setEditButtonEnable(preview_vp.getCurrentItem());
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

        gy_compression_cb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (gy_compression_cb.isChecked()) {
                    compression = false;
                    checkPos = preview_vp.getCurrentItem();
                } else {
                    compression = true;
                    checkPos = -1;// 取消选择就去掉选中的图片记录
                }
                if (type.equals(MAINACTIVITY_TYPE)) {
                    if (checkboxState == compression) {
                        setConfirmButtonTxt(false);
                    } else {
                        setConfirmButtonTxt(true);
                    }
                }
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
        if (type.equals(MAINACTIVITY_TYPE) || type.equals(CAMERARESULT_TYPE)) {
            finish();
        } else if (type.equals(NOMALPHOTO_TYPE)) {
            Intent intent = new Intent();
            intent.putExtra("iscomfirm", false);//是确定还是返回
            intent.putExtra("checkPos", checkPos);
            intent.putExtra("compression", compression);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void setConfirmButtonTxt(boolean ischangge) {
        if (ischangge) {
            preview_confirm.setText(R.string.main_confirm);
        } else {
            preview_confirm.setText(R.string.photo_album);
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
                    if (ischangge || checkboxState != compression) {
                        confirmActivion(null);
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("code", AlbumActivity.FLODER);
                        intent.putExtra("changeview", "changeview");
                        setResult(RESULT_OK, intent);
                        finish();
                    }
//                    Intent intent = new Intent();
//                    intent.putExtra("code", 100);
//                    intent.putExtra("changeview", "changeview");
//                    setResult(RESULT_OK, intent);
//                    finish();
                } else if (type.equals(NOMALPHOTO_TYPE)) {
                    confirmActivion(null);
                }
                break;
            case R.id.gallery_shear:// 剪切
                Intent intent1 = new Intent(GalleryActivity.this,
                        ShareActivity.class);
                intent1.putExtra("sharePath",
                        getFilePath(preview_vp.getCurrentItem()));
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
        boolean isnomalmms = CommonUtil.isOutLimit(getFilePath(position));// 正常彩信，大小没有超出
        if (isnomalmms) {
            gy_compression_cb.setChecked(false);
            gy_compression_cb.setVisibility(View.GONE);
            gy_pic_size.setVisibility(View.GONE);
        } else if (showArtwork) {//是否显示原图按钮-默认是显示
            gy_compression_cb.setVisibility(View.VISIBLE);
            gy_pic_size.setVisibility(View.VISIBLE);
            if (position == checkPos) {// 当前显示的图片是否就是外部选中的图片的位置
                gy_compression_cb.setChecked(!compression);
            } else {
                gy_compression_cb.setChecked(false);
            }
            String path = getFilePath(position);
            Uri uri = Uri.parse(path);
            uri.getPath();
            File file = new File(uri.getPath());
            String picleng = CommonUtil.getUserSpaceStr(file.length());
            gy_pic_size.setText(getResources().getString(R.string.photo_original) + "(" + picleng + ")");
        }
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
        Drawable drawable = photoView.getDrawable();
        if (drawable == null) {
            return;
        }
        ischangge = true;
        Bitmap bitmap = CommonUtil.drawableToBitmap(drawable);
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
            ischangge = false;
        }
        if (type.equals(MAINACTIVITY_TYPE)) {
            setConfirmButtonTxt(ischangge);
        }
    }


    private void confirmActivion(String normalFilePath) {
        ArrayList<String> selectedImageList = new ArrayList<>();
        if (TextUtils.isEmpty(normalFilePath)) {
            selectedImageList.add(getFilePath(preview_vp.getCurrentItem()));
        } else {
            selectedImageList.add("file://" + normalFilePath);
        }
        // 选择图片完成,回到起始页面

        Intent intent = new Intent();
        intent.putExtra("iscomfirm", true);
//        intent.putExtra("code", selectedImageList != null ? AlbumActivity.FLODER : -1);
        intent.putStringArrayListExtra("paths", selectedImageList);
        String filepath = selectedImageList.get(0);


        if (ischangge) {
            PhotoView photoView = (PhotoView) preview_vp.findViewById(preview_vp
                    .getCurrentItem());

            Drawable drawable = photoView.getDrawable();
//            BitmapDrawable bitmapDrawable = ((BitmapDrawable) photoView.getDrawable());
            if (drawable == null) {
                return;
            }

            Bitmap bitmap = CommonUtil.drawableToBitmap(drawable);
            Timestamp now = new Timestamp(System.currentTimeMillis());//获取系统当前时间

            String path = FileUtils.saveBitmap(bitmap, now.toString(), this);//时间戳
            bitmap.recycle();
            if (path != null) {
                intent.putExtra("sendBitmap", path);
                filepath = path;
            } else {
            }
        }
        boolean isnomalmms = CommonUtil.isOutLimit(filepath);
        intent.putExtra("isnomalmms", isnomalmms);
        if (isnomalmms) {//没有超过限制的图片都是原图
            intent.putExtra("compression", false);
        } else {
            intent.putExtra("compression", !gy_compression_cb.isChecked());
        }
        setResult(RESULT_OK, intent);
        finish();
    }


    public void setEditButtonEnable(int position) {
        if (preview_vp.getCurrentItem() != position) {
            return;
        }
        boolean clickble = isReadyMap.get(position);
        gallery_rotation.setEnabled(clickble);
        gallery_shear.setEnabled(clickble);
        preview_confirm.setEnabled(clickble);
        if (clickble) {
            if (preview_vp.findViewWithTag(position) != null) {
                preview_vp.findViewWithTag(position).setVisibility(View.GONE);
            }
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

    /**
     * 如果是gif就，没有编辑的控件
     *
     * @param filepath
     */
    private void CanEdit(String filepath) {
        String name;
        name = filepath;
        if (name.toLowerCase().endsWith(".gif")) {
            if (gy_compression_cb.getVisibility() == View.VISIBLE) {
                if (gallery_rotation.getVisibility() == View.VISIBLE) {
                    gallery_rotation.setVisibility(View.GONE);
                }
                if (gallery_shear.getVisibility() == View.VISIBLE) {
                    gallery_shear.setVisibility(View.GONE);
                }
            }
        } else {
            if (gallery_rotation.getVisibility() == View.GONE) {
                gallery_rotation.setVisibility(View.VISIBLE);
            }
            if (gallery_shear.getVisibility() == View.GONE) {
                gallery_shear.setVisibility(View.VISIBLE);
            }
        }
    }

    private Cursor getCursor(String type, String folderPath) {
        Cursor cursor = null;
        ContentResolver mContentResolver = getContentResolver();
        switch (type) {
            case NOMALPHOTO_TYPE:
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
                String key_DATA = MediaStore.Images.Media.DATA;
                String selection;
                if (folderPath != null) {// 查找特定路径下的数据
                    selection = key_DATA + " like " + DatabaseUtils.sqlEscapeString(folderPath + '%') + " and (" + key_MIME_TYPE + "=? or "
                            + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?)";
                } else {
                    selection = key_MIME_TYPE + "=? or "
                            + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?";
                }

                try {
                    // 只查询jpg和png的图片,按最新修改排序
                    cursor = mContentResolver.query(mImageUri,
                            new String[]{key_DATA}, selection,
                            new String[]{"image/jpg", "image/jpeg", "image/png", "image/gif", "image/vnd.wap.wbmp"},
                            MediaStore.Images.Media.DATE_MODIFIED + " desc");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return cursor;
    }

    private String getFilePath(int position) {
        if (type.equals(MAINACTIVITY_TYPE) || type.equals(CAMERARESULT_TYPE)) {
            return lists.get(position);
        } else {
            cursor.moveToPosition(position);
            String filepath = cursor.getString(0);
            return "file://" + filepath;
        }

    }

}
