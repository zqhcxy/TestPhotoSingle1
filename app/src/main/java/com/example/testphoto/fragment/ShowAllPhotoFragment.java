package com.example.testphoto.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.testphoto.GalleryActivity;
import com.example.testphoto.R;
import com.example.testphoto.adapter.PhotoWallAdapter;
import com.example.testphoto.model.ConfirmLocalFileInf;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.CommonUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * 图片选择界面。 最近的照片
 *
 * @author zqh-pc
 */
public class ShowAllPhotoFragment extends Fragment implements OnClickListener,
        ConfirmLocalFileInf {


    private GridView local_gv;// 显示照片
    private LinearLayout choicepic_ly;// 底部选中图片弹出布局
    private CheckBox original_cb;// 原图
    private CheckBox compression_cb;// 压缩图
    private TextView original_title;// 超级短信

    //	private ArrayList<String> imagePathList;// 照片路径集合
    private Cursor cursor;
    private PhotoWallAdapter adapter;

    private static final int PREVIEWRESULT = 0x000002;// 预览图片的回调
    private static final int TAKE_PICTURE = 0x000001;// 调用相机的回调。
    /**
     * 当前文件夹路径
     */
    private String currentFolder = null;
    /**
     * 当前展示的是否为最近照片
     */
    private boolean isLatest = true;
    public boolean compression = true;//是否压缩
    public int checkpos = -1;//当前选中的图片的位置
    private ArrayList<String> syslist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_show_photo, container,
                false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        original_cb = (CheckBox) view.findViewById(R.id.original_cb);
        compression_cb = (CheckBox) view.findViewById(R.id.compression_cb);
        choicepic_ly = (LinearLayout) view.findViewById(R.id.choicepic_ly);
        local_gv = (GridView) view.findViewById(R.id.local_gv);
        original_title = (TextView) view.findViewById(R.id.original_title);
//		SpannableString msp = new SpannableString(original_title.getText()
//				.toString());
//		//字体颜色
//		msp.setSpan(new ForegroundColorSpan(Color.CYAN), 3, 7,
//				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置前景色为洋红色
//		// 设置下划线
//		msp.setSpan(new UnderlineSpan(), 3, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//		original_title.setText(msp);
        //超链接颜色
        String result = getResources().getString(
                R.string.photo_original) + "(<u>"
                + "<font  color=\"#0b94f9\">"
                + getResources().getString(
                R.string.all_photo_hcmms) + "</font>" + "</u>)";
        original_title.setText(Html.fromHtml(result));

        local_gv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String name = (String) parent.getItemAtPosition(position);
                if (name.equals("Camera")) {// 系统相机引导
                    getCameraPhoto();
                } else {// 预览
                    Intent intent2 = new Intent(getActivity(),
                            GalleryActivity.class);
                    int selectposition;
                    //这里要判断是否有其他部件的集合，有酒减一，没有就直接使用
                    if (syslist.size() > 0) {
                        selectposition = position - 1;
                    } else {
                        selectposition = position;
                    }
                    intent2.putExtra("folderPath", currentFolder);//当前相册
                    intent2.putExtra("selection", selectposition);//点击查看预览的图片位置
                    intent2.putExtra("Type", "nomalAhoto");//进入预览的模式
                    intent2.putExtra("checkPos", checkpos);//选中的图片位置
                    intent2.putExtra("compression", compression);//是否选择缩略图
                    intent2.putExtra("is_hid", ((AlbumActivity) getActivity()).isHidButtomLy);
                    startActivityForResult(intent2, PREVIEWRESULT);
                }

            }
        });

        compression_cb
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        compression = true;
                        original_cb.setChecked(false);
                        compression_cb.setChecked(true);
                    }
                });
        original_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                compression = false;
                compression_cb.setChecked(false);
                original_cb.setChecked(true);
            }
        });
    }

    private void initData() {
        syslist = new ArrayList();
        syslist.add("Camera");
        cursor = getCursor(null);
        adapter = new PhotoWallAdapter(getActivity(), this, cursor, syslist);
        local_gv.setAdapter(adapter);

        setConfirmEnable();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }

    }

    public void onNewIntent(int code, String folderPath) {
        // 动画
        getActivity().overridePendingTransition(R.anim.in_from_right,
                R.anim.out_from_left);
        checkpos = -1;
        if (code == AlbumActivity.FLODER) {
            // 某个相册
            if (isLatest
                    || (folderPath != null && !folderPath.equals(currentFolder))) {
                currentFolder = folderPath;
                updateView(AlbumActivity.FLODER, currentFolder);
                isLatest = false;
            }
        } else if (code == AlbumActivity.ALLPICTRUE) {
            // “全部照片”
            currentFolder = null;//要清空，全部数据不需要这个，也不能有这个
            if (!isLatest) {
                updateView(AlbumActivity.ALLPICTRUE, null);
                isLatest = true;
            }
        }
    }

    /**
     * 根据图片所属文件夹路径，刷新页面
     */
    private void updateView(int code, String folderPath) {
        MySparseBooleanArray.clearSelectionMap();
//		adapter.notifyDataSetChanged();
        setConfirmEnable();

        if (code == AlbumActivity.FLODER) { // 某个相册
            cursor = getCursor(folderPath);
            syslist.clear();
        } else if (code == AlbumActivity.ALLPICTRUE) { // 最近照片
            cursor = getCursor(null);
            syslist.add("Camera");
        }
        adapter.changeCursor(cursor);
        if (cursor.getCount() > 0) {
            // 滚动至顶部
            local_gv.smoothScrollToPosition(0);
        }
    }

    // 获取已选择的图片路径
    public ArrayList<String> getSelectImagePaths() {
        SparseBooleanArray map = MySparseBooleanArray.getSelectionMap();
        if (map.size() == 0) {
            return null;
        }
        ArrayList<String> selectedImageList = new ArrayList<String>();

        int sizesys = syslist.size();
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            if (map.get(i + sizesys)) {
                String filepath = getDataOfCursor(i);
                selectedImageList.add(filepath);
            }
        }
        return selectedImageList;
    }

    /**
     * 确认选中返回
     *
     * @param paths 选中的地址集合(目前只用单选)
     */
    private void comfirmPhoto(ArrayList<String> paths) {
        Intent intent = new Intent(getActivity(), MsgFragmentActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		intent.putExtra("code", paths != null ? 100 : -1);
        intent.putExtra("compression", compression);
        boolean isoutlimit = CommonUtil.isOutLimit(paths.get(0));// 正常彩信，大小没有超出
        intent.putExtra("isnomalmms", !isoutlimit);
        intent.putStringArrayListExtra("paths", paths);
        getActivity().setResult(getActivity().RESULT_OK, intent);
        MySparseBooleanArray.clearSelectionMap();
        getActivity().finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != getActivity().RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case TAKE_PICTURE:// 相机回调
                // 直接调用 项目现在的那个方法。
                // 这里这样做只是为了展示
                if (TextUtils.isEmpty(takePicPath)) {
                    Log.d("hc", "take picture path is null");
                    return;
                }
                File file = new File(takePicPath);
                Uri uri;
                if (file.exists()) {
                    uri = Uri.fromFile(file);
                    getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                } else {
                    Log.d("hc", "take picture file is not exist");
                    return;
                }
                String paths = uri.toString();
                if (!TextUtils.isEmpty(paths)) {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(paths);
                    comfirmPhoto(arrayList);
                }
                break;
            case PREVIEWRESULT:// 预览回调
                boolean iscomfirm = data.getBooleanExtra("iscomfirm", false);
                if (iscomfirm) {
                    MySparseBooleanArray.clearSelectionMap();
                    getActivity().setResult(getActivity().RESULT_OK, data);
                    getActivity().finish();
                } else {// 正常的返回

                    int original_pos = data.getIntExtra("checkPos", -1);//选中图片的位置
                    boolean mCompreeion = data.getBooleanExtra("compression", true);

                    if (original_pos != -1) {//有选中图片
                        String filepath;
                        filepath = getDataOfCursor(original_pos);
                        MySparseBooleanArray.clearSelectionMap();
                        MySparseBooleanArray.setSelectionData(original_pos + syslist.size(),
                                true, filepath);
                        adapter.buttomLy(filepath, false);
                        adapter.notifyDataSetChanged();
                        original_cb.setChecked(!mCompreeion);
                        compression_cb.setChecked(mCompreeion);
                        setCheckpos(original_pos);
                        compression = mCompreeion;
                    } else {
                        MySparseBooleanArray.clearSelectionMap();
                        checkpos = -1;
                        hidButtonLy();
                        original_cb.setChecked(!mCompreeion);
                        compression_cb.setChecked(mCompreeion);
                        adapter.notifyDataSetChanged();
                    }
                }

                break;

            default:
                break;
        }
    }

    private String takePicPath = "";

    private void getCameraPhoto() {
        takePicPath = CommonUtil.getHcExternalFile("HC_images", "hc_" + System.currentTimeMillis() + ".jpg");
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        CommonUtil.getMmsTempUri()
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(takePicPath)));
        startActivityForResult(openCameraIntent, TAKE_PICTURE);
    }

    /**
     * 设置确定按钮是否可点击
     */
    public void setConfirmEnable() {
        ArrayList<String> lists = getSelectImagePaths();
        int size = 0;
        if (lists != null) {
            size = lists.size();
        }

        ((AlbumActivity) getActivity()).setConfirmBt(size);
    }

    @Override
    public void confirmLoacl() {
        comfirmPhoto(getSelectImagePaths());
    }

    public boolean isHaveChoice() {
        SparseBooleanArray map = MySparseBooleanArray.getSelectionMap();
        int size = cursor.getCount();
        if (size == 0) {
            return false;
        }

        int sizeOfAll = size + syslist.size();
        for (int i = 0; i < sizeOfAll; i++) {
            if (map.get(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 显示底部布局
     */
    public void showButtonLy(boolean isCompression) {
        setCompression(isCompression);
        if (choicepic_ly.getVisibility() == View.GONE && !((AlbumActivity) getActivity()).isHidButtomLy) {
//            compression_cb.setChecked(true);// 默认都是选择压缩图片
            choicepic_ly.setVisibility(View.VISIBLE);
            choicepic_ly.startAnimation(AnimationUtils.loadAnimation(
                    getActivity(), R.anim.activity_translate_in));
        }
    }

    /**
     * 隐藏底部布局
     */
    public void hidButtonLy() {
        if (choicepic_ly.getVisibility() == View.VISIBLE && !((AlbumActivity) getActivity()).isHidButtomLy) {
            choicepic_ly.setVisibility(View.GONE);
            choicepic_ly.startAnimation(AnimationUtils.loadAnimation(
                    getActivity(), R.anim.activity_translate_out));
        }
    }

    public void setCompression(boolean compression) {
        this.compression = compression;
        original_cb.setChecked(!compression);
        compression_cb.setChecked(compression);
    }
//    /**
//     * 判断当前图片是否超出短信大小限制
//     *
//     * @param filepath
//     * @return
//     */
//    public boolean thepiclimit(String filepath) {
//        Uri uri = Uri.parse(filepath);
//        int picSize = (int) CommonUtil.getFileSize(uri.getPath());
//        boolean sizeLimit = CarrierContentRestriction.isMessageSizeExceed(MessageUtils.MESSAGE_OVERHEAD, picSize);
//        return sizeLimit;
//    }

    private Cursor getCursor(String folderPath) {
        Cursor cursor = null;
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
        String key_DATA = MediaStore.Images.Media.DATA;
        String key_ID = MediaStore.Images.Media._ID;
        String selection;
        if (folderPath != null) {// 查找特定路径下的数据
            selection = key_DATA + " like " + DatabaseUtils.sqlEscapeString(folderPath + '%') + " and (" + key_MIME_TYPE + "=? or "
                    + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?)";
        } else {
            selection = key_MIME_TYPE + "=? or "
                    + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?";
        }

        ContentResolver mContentResolver = getActivity().getContentResolver();
        // 只查询jpg和png的图片,按最新修改排序
        cursor = mContentResolver.query(mImageUri,
                new String[]{key_DATA, key_ID}, selection,
                new String[]{"image/jpg", "image/jpeg", "image/png", "image/gif", "image/vnd.wap.wbmp"},
                MediaStore.Images.Media.DATE_MODIFIED + " desc");
        return cursor;
    }

    private String getDataOfCursor(int position) {
        cursor.moveToPosition(position);
        String filepath = cursor.getString(0);
        return "file://" + filepath;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
    }

    public void setCheckpos(int checkpos) {
        this.checkpos = checkpos;
    }

    public void setOriginal_cbCheck(boolean checkable) {
        original_cb.setEnabled(checkable);
    }

    private boolean isPictureOutLimit(String filepath) {
        Uri uri;
        if (filepath.contains("file://")) {
            uri = Uri.parse(filepath);
        } else {
            uri = Uri.fromFile(new File(filepath));
        }
        int picSize = (int) CommonUtil.getFileSize(uri.getPath());
        if (CommonUtil.isOutLimit(uri.getPath())) {//超出彩信大小就跳转到预览界面
            return true;
        } else {//没有就直接返回
            return false;
        }
    }
}
