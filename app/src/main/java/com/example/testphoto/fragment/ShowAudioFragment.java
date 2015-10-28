package com.example.testphoto.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.example.testphoto.R;
import com.example.testphoto.adapter.MyAudioAdapter;
import com.example.testphoto.model.ConfirmLocalFileInf;
import com.example.testphoto.model.MyAudio;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.GetLocalFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 显示所有音频
 *
 * @author zqh-pc
 */

public class ShowAudioFragment extends Fragment implements
        ConfirmLocalFileInf {
    private GridView audio_show;
    private List<MyAudio> videos;
    private MyAudioAdapter adapter;
    private TextView filetip_button_ly;//文件过大底部提示窗


    /**
     * 当前文件夹路径
     */
    private String currentFolder = null;

    private static final int CASE_AUDIO = 0x000003;// 调用摄像机的回调。


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
        audio_show = (GridView) view.findViewById(R.id.video_show);
        audio_show.setBackgroundColor(getActivity().getResources().getColor(R.color.media_bg1));
        filetip_button_ly = (TextView) view.findViewById(R.id.filetip_button_ly);
    }


    private void initData() {
        videos = new ArrayList<>();
        addData();
        setConfirmEnable();
    }

    private void addData() {
        MyAudio myAudio = new MyAudio();
        myAudio.setItemType("MyAudio");
        MyAudio myAudio1 = new MyAudio();
        myAudio1.setItemType("SystemAudio");// 查看系统铃声
        videos.add(myAudio);
        videos.add(myAudio1);
        List<MyAudio> list = GetLocalFile.getAllAudioPathsByFolder(
                getActivity(), null);
        if (list != null)
            videos.addAll(list);

        adapter = new MyAudioAdapter(getActivity(), this, videos);
        audio_show.setAdapter(adapter);
    }

    private void initListener() {
        audio_show.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                MyAudio myAudio = (MyAudio) parent.getItemAtPosition(position);
                if (myAudio.getItemType().equals("MyAudio")) {
                    // 录音
                    getNewAudio();
                } else if (myAudio.getItemType().equals("SystemAudio")) {// 调用系统铃声

                    ((AlbumActivity) getActivity()).setChangView(0, 100,
                            "SystemMusic");
                    ((AlbumActivity) getActivity()).setAllPhotoTitle("系统铃声");

                }

            }
        });
    }

    /**
     * 获取选中的音频
     *
     * @return
     */

    public ArrayList<MyAudio> getSelectImagePaths() {
        // SparseBooleanArray map = adapter.getSelectionMap();
        SparseBooleanArray map = MySparseBooleanArray.getSelectionMap();
        if (map.size() == 0) {
            return null;
        }
        ArrayList<MyAudio> selectedImageList = new ArrayList<>();

        for (int i = 0; i < videos.size(); i++) {
            if (map.get(i)) {
                selectedImageList.add(videos.get(i));
            }
        }
        return selectedImageList;
    }

    private void comfirmPhoto(ArrayList<MyAudio> myAudios) {
        Intent intent = new Intent(getActivity(), MsgFragmentActivity.class);
        intent.putExtra("code", myAudios != null ? 101 : -1);
        intent.putExtra("MyAudio", (Serializable) myAudios.get(0));
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

        if (code == 100) {
            // 某个音频目录
            // if (isLatest
            // || (folderPath != null && !folderPath.equals(currentFolder))) {
            if (folderPath != null) {
                if (folderPath.equals("SystemMusic")) {// 系统铃声
                    videos.clear();
                    MySparseBooleanArray.clearSelectionMap();
                    videos.addAll(getRingtoneList(RingtoneManager.TYPE_RINGTONE));
                    adapter.notifyDataSetChanged();
                } else {
                    currentFolder = folderPath;
                    updateView(100, currentFolder);
                }
            }
        } else if (code == 200) {
            // “全部音频”
            // // if (!isLatest) {
            updateView(200, null);
            // isLatest = true;
            // }
        }
    }

    /**
     * 根据音频所属文件夹路径，刷新页面
     */
    private void updateView(int code, String folderPath) {
        videos.clear();
        MySparseBooleanArray.clearSelectionMap();
        adapter.notifyDataSetChanged();
        setConfirmEnable();
        if (code == 100) { // 某个视频库
            videos.addAll(GetLocalFile.getAllAudioPathsByFolder(getActivity(),
                    folderPath));
        } else if (code == 200) { // 全部视频
            addData();
        }
        adapter.notifyDataSetChanged();
        if (videos.size() > 0) {
            // 滚动至顶部
            audio_show.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != getActivity().RESULT_OK) {
            return;
        }
        if (requestCode == CASE_AUDIO) {

            // Uri uri = data.getData();
            String path = data.getData().toString();
            MyAudio myAudio = new MyAudio();
            myAudio.setPath(path);
            ArrayList<MyAudio> myAudios = new ArrayList<>();
            myAudios.add(myAudio);
            comfirmPhoto(myAudios);
        }
    }

    /**
     * 跳转到系统摄像
     */
    private void getNewAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.setClassName("com.android.soundrecorder",
                "com.android.soundrecorder.SoundRecorder");
        startActivityForResult(intent, CASE_AUDIO);
        // Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // startActivityForResult(intent, CASE_AUDIO);
    }

    /**
     * 设置确定按钮是否可点击
     */
    public void setConfirmEnable() {
        ArrayList<MyAudio> lists = getSelectImagePaths();
        int size = 0;
        if (lists != null) {
            size = lists.size();
        }
        ((AlbumActivity) getActivity()).setConfirmBt(size);
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.stopPlay();
    }


    @Override
    public void confirmLoacl() {
        comfirmPhoto(getSelectImagePaths());
    }

    /**
     * 获取系统铃声
     *
     * @param type
     * @return
     */
    public List<MyAudio> getRingtoneList(int type) {

        List<MyAudio> audios = new ArrayList<MyAudio>();

        RingtoneManager manager = new RingtoneManager(getActivity());

        manager.setType(type);

        Cursor cursor = manager.getCursor();

        int count = cursor.getCount();
        for (int i = 0; i < count; i++) {
            MyAudio myAudio = new MyAudio();

            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            myAudio.setTitle(title);
            // myAudio.setRingtone(manager.getRingtone(i));// 这个对象可以直接播放
            Uri path = manager.getRingtoneUri(i);
            myAudio.setPath(path.toString());
            myAudio.setItemType("Audio");
            audios.add(myAudio);
        }

        return audios;

    }


    public String getRingtoneUriPath(int type, int pos, String def) {

        RingtoneManager manager = new RingtoneManager(getActivity());

        manager.setType(type);

        Uri uri = manager.getRingtoneUri(pos);

        return uri == null ? def : uri.toString();

    }


    /**
     * 判断所选文件是否大于可发送大小，是的话就底部弹窗提示
     * @return 是否弹窗
     */
    public boolean isHaveChoice() {
        //TODO 这里现在只是判断是否选择文件，是的话就弹窗。还需要一个判断是否大于规定大小文件。
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
