package com.example.testphoto.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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
import com.example.testphoto.model.MyVideo;
import com.example.testphoto.util.GetLocalFile;

import java.util.ArrayList;
import java.util.List;

public class ShowVideoFragment extends Fragment implements OnClickListener,
        ConfirmLocalFileInf {

    private GridView video_show;
    private TextView filetip_button_ly;

    private List<MyVideo> videos;
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
        videos = new ArrayList<>();

        MyVideo myVideo = new MyVideo();
        myVideo.setPath("MyVideo");
        videos.add(myVideo);
        List<MyVideo> list = GetLocalFile.getAllVideoPathsByFolder(
                getActivity(), null);
        if (list != null)
            videos.addAll(list);

        adapter = new JieVideoListViewAdapter(getActivity(), this, videos);
        video_show.setAdapter(adapter);
        setConfirmEnable();
    }

    private void initListener() {
        video_show.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {// 第一位是摄影
                    MyVideo myvideo = (MyVideo) parent
                            .getItemAtPosition(position);
                    if (myvideo.getPath().equals("MyVideo"))
                        getNewVideo();
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
        // SparseBooleanArray map = adapter.getSelectionMap();
        SparseBooleanArray map = MySparseBooleanArray.getSelectionMap();
        if (map.size() == 0) {
            return null;
        }
        ArrayList<String> selectedImageList = new ArrayList<String>();

        for (int i = 0; i < videos.size(); i++) {
            if (map.get(i)) {
                selectedImageList.add(videos.get(i).getPath());
            }
        }
        return selectedImageList;
    }

    private void comfirmPhoto(ArrayList<String> paths) {
        Intent intent = new Intent(getActivity(), MsgFragmentActivity.class);
        intent.putExtra("code", paths != null ? 102 : -1);
        intent.putStringArrayListExtra("paths", paths);
        getActivity().setResult(getActivity().RESULT_OK, intent);
        MySparseBooleanArray.clearSelectionMap();
        getActivity().finish();
    }

    // 刷新当前选中的视频数量
    // public void sumSelectPho() {
    // int sum = 0;
    // ArrayList<String> lists = getSelectImagePaths();
    // if (lists != null) {
    // sum = lists.size();
    // }
    // if (sum == 0) {
    // confirm.setText("发送");
    // } else {
    // confirm.setText("发送(" + sum + ")");
    // }
    // setConfirmEnable();
    // }

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

        if (code == 100) {
            // 某个相册
            if (isLatest
                    || (folderPath != null && !folderPath.equals(currentFolder))) {
                currentFolder = folderPath;
                updateView(100, currentFolder);
                isLatest = false;
            }
        } else if (code == 200) {
            // “全部视频”
            if (!isLatest) {
                updateView(200, null);
                isLatest = true;
            }
        }
    }

    /**
     * 根据图片所属文件夹路径，刷新页面
     */
    private void updateView(int code, String folderPath) {
        videos.clear();
        MySparseBooleanArray.clearSelectionMap();
        adapter.notifyDataSetChanged();
        setConfirmEnable();
        if (code == 100) { // 某个视频库
            videos.addAll(GetLocalFile.getAllVideoPathsByFolder(getActivity(),
                    folderPath));
        } else if (code == 200) { // 全部视频
            MyVideo myVideo = new MyVideo();
            myVideo.setPath("MyVideo");
            videos.add(myVideo);
            videos.addAll(GetLocalFile.getAllVideoPathsByFolder(getActivity(),
                    null));
        }

        adapter.notifyDataSetChanged();
        if (videos.size() > 0) {
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
            Cursor cursor = getActivity().getContentResolver().query(uri, null,
                    null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                String path = cursor
                        .getString(cursor
                                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                ArrayList<String> paths = new ArrayList<>();
                paths.add(path);
                comfirmPhoto(paths);
                cursor.close();
            }

//            String path = data.getData().toString();
//            ArrayList<String> paths = new ArrayList<>();
//            paths.add(path);
//            comfirmPhoto(paths);
        }
    }

    /**
     * 跳转到系统摄像
     */
    private void getNewVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, CASE_VIDEO);
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
        int size = videos.size();
        if (size == 0) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (map.get(i)) {
                return true;
            }
        }
        return false;
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

}
