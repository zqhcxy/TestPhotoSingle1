package com.example.testphoto.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.example.testphoto.R;
import com.example.testphoto.adapter.JieVideoListViewAdapter;
import com.example.testphoto.model.ConfirmLocalFileInf;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.CommonUtil;

import java.util.ArrayList;

public class ShowVideoFragment extends Fragment implements OnClickListener,
        ConfirmLocalFileInf {

    private GridView video_show;
    private TextView filetip_button_ly;

    //    private List<MyVideo> videos;
    private Cursor mCursor;//视频数据源
    private ArrayList<String> syslist;//存放其他item
    private JieVideoListViewAdapter adapter;
    /**
     * 当前文件夹路径
     */
    private String currentFolder = null;
    /**
     * 当前展示的是否为全部视频
     */
    private boolean isLatest = true;

    private static final int CASE_VIDEO = 0x000003;// 调用摄像机的回调。
    public boolean isnomalmms;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.showvideo_activity, container,
                false);
        initView(view);
        initData();
        initListener();

        return view;
    }

    private void initView(View view) {
        video_show = (GridView) view.findViewById(R.id.video_show);
        filetip_button_ly = (TextView) view.findViewById(R.id.filetip_button_ly);
        video_show.setBackgroundColor(getActivity().getResources().getColor(R.color.media_bg));

    }

    private void initData() {
        syslist = new ArrayList<>();
        syslist.add("MyVideo");
        mCursor = getCursor(null);
        adapter = new JieVideoListViewAdapter(getActivity(), mCursor, this, syslist);
        video_show.setAdapter(adapter);
        setConfirmEnable();

        String result = getResources().getString(
                R.string.limit_media1)
                + "<u><font  color=\"#0b94f9\">"
                + getResources().getString(
                R.string.all_photo_hcmms) + "</font></u>" + getResources().getString(
                R.string.limit_media2);
        filetip_button_ly.setText(Html.fromHtml(result));
        filetip_button_ly.setOnClickListener(this);
    }

    private void initListener() {
        video_show.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String myvideo = (String) parent
                        .getItemAtPosition(position);
                if (myvideo.equals("MyVideo")) {
                    getNewVideo();
                } else {
                    try {
                        Intent intent5 = new Intent(Intent.ACTION_VIEW);
                        intent5.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent5.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent5.setDataAndType(Uri.parse(myvideo), "video/*");
                        if (CommonUtil.isAndroid40FirmwareVersion())
                            intent5.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, false);
                        startActivity(intent5);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // case R.id.topbar_left_btn:
            // finish();
            // break;
            // case R.id.confirm_bt:// 完成
            // ArrayList<String> paths = getSelectImagePaths();
            // comfirmPhoto(paths);
            default:
                break;
        }

    }

    /**
     * 获取选中的视频
     *
     * @return
     */

    public ArrayList<String> getSelectImagePaths() {
        SparseBooleanArray map = MySparseBooleanArray.getSelectionMap();
        if (map.size() == 0) {
            return null;
        }
        ArrayList<String> selectedImageList = new ArrayList<String>();

        int size = mCursor.getCount();
        for (int i = 0; i < size; i++) {
            int id = getVideoIDOfCursor(i);
            if (map.get(id)) {
                String filepath = getDataOfCursor(i);
                selectedImageList.add(filepath);
            }
        }
        return selectedImageList;
    }

    private void comfirmVideo(ArrayList<String> paths) {
        Intent intent = new Intent();
//        intent.putExtra("code", paths != null ? 102 : -1);
        intent.putStringArrayListExtra("paths", paths);
        intent.putExtra("isnomalmms", isnomalmms);
        getActivity().setResult(getActivity().RESULT_OK, intent);
        MySparseBooleanArray.clearSelectionMap();
        getActivity().finish();
    }

    /**
     * 外部点击更新当前界面数据
     *
     * @param code
     * @param folderPath
     */
    public void onNewIntent(int code, String folderPath) {
        // 动画
        getActivity().overridePendingTransition(R.anim.in_from_right,
                R.anim.out_from_left);

        if (code == AlbumActivity.FLODER) {
            // 某个相册
            if (isLatest
                    || (folderPath != null && !folderPath.equals(currentFolder))) {
                currentFolder = folderPath;
                updateView(AlbumActivity.FLODER, currentFolder);
                isLatest = false;
            }
        } else if (code == AlbumActivity.ALLPICTRUE) {
            // “全部视频”
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
        adapter.notifyDataSetChanged();
        setConfirmEnable();
        if (code == AlbumActivity.FLODER) { // 某个视频库
            mCursor = getCursor(folderPath);
            syslist.clear();
        } else if (code == AlbumActivity.ALLPICTRUE) { // 全部视频
            mCursor = getCursor(null);
            syslist.add("MyVideo");
        }
        adapter.changeCursor(mCursor);
        if (mCursor.getCount() > 0) {
            // 滚动至顶部
            video_show.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != getActivity().RESULT_OK) {
            return;
        }

        if (requestCode == CASE_VIDEO) {
            Uri uri = data.getData();
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver().query(uri, null,
                        null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    ArrayList<String> paths = new ArrayList<>();
                    paths.add("file://" + path);
//                    boolean isout = CommonUtil.isOutLimit(path);
//                    if (!isout) {
                        comfirmVideo(paths);
//                    } else {
//                        Toast.makeText(getActivity(), "视频超出大小限制", Toast.LENGTH_SHORT).show();
//                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
        }
    }

    /**
     * 跳转到系统摄像
     */
    private void getNewVideo() {
        CommonUtil.recordvideo(this, CASE_VIDEO);
//        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        startActivityForResult(intent, CASE_VIDEO);
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
        comfirmVideo(getSelectImagePaths());
    }


    public void showButtonLy() {
        if (filetip_button_ly.getVisibility() == View.GONE) {
            filetip_button_ly.setVisibility(View.VISIBLE);
        }
    }

    public void hidButtonLy() {
        if (filetip_button_ly.getVisibility() == View.VISIBLE) {
            filetip_button_ly.setVisibility(View.GONE);
        }
    }

    /**
     * 选中文件底部弹窗判断逻辑
     *
     * @param filepath
     * @param ischoice
     * @return
     */
    public boolean buttomLy(String filepath, boolean ischoice) {

        Uri uri = Uri.parse(filepath);
        boolean sizeLimit = CommonUtil.isOutLimit(uri.getPath());
        if (sizeLimit) {//超级彩信
            isnomalmms = false;
            // 底部弹出区域
            if (ischoice) {
                hidButtonLy();
            } else {
                showButtonLy();
            }
        } else {
            hidButtonLy();
            isnomalmms = true;
        }
        return false;
    }

    /**
     * 获取所有视频数据
     *
     * @param folderPath 查找指定路径，没有就查找全部
     * @return
     */
    private Cursor getCursor(String folderPath) {
        Uri mVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;// Uri
        String orderBy = MediaStore.Video.Media.DATE_MODIFIED;
        String selection = null;

        if (folderPath != null) {// 查找特定路径下的数据
            selection = MediaStore.Video.Media.DATA + " like " + DatabaseUtils.sqlEscapeString(folderPath + '%');
        }
        Cursor cursor = getActivity().getContentResolver().query(mVideoUri, null,
                selection, null, orderBy + " desc");
        return cursor;
    }

    /**
     * 获取视频的地址
     *
     * @param position
     * @return
     */
    private String getDataOfCursor(int position) {
        mCursor.moveToPosition(position);
        String filepath = mCursor.getString(mCursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        return "file://" + filepath;
    }

    /**
     * 获取视频的id
     *
     * @param position
     * @return
     */
    private int getVideoIDOfCursor(int position) {
        mCursor.moveToPosition(position);
        int id = mCursor.getInt(mCursor
                .getColumnIndexOrThrow(MediaStore.Video.Media._ID));
        return id;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCursor != null) {
            mCursor.close();
        }
    }

}
