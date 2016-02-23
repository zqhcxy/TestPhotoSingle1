package com.example.testphoto.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Html;
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
    //    private List<MyAudio> videos;
    private List<String> syslist;
    private Cursor mCursor;
    private MyAudioAdapter adapter;
    private TextView filetip_button_ly;//文件过大底部提示窗


    /**
     * 当前文件夹路径
     */
    private String currentFolder = null;

    private static final int CASE_AUDIO = 0x000003;// 调用摄像机的回调。
    public boolean isSysAudio = false;
    private RingtoneManager ringtonemanager;


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
//        videos = new ArrayList<>();
        syslist = new ArrayList<>();
        addData();
        setConfirmEnable();
        String result = getResources().getString(
                R.string.limit_media1)
                + "<u><font  color=\"#0b94f9\">"
                + getResources().getString(
                R.string.all_photo_hcmms) + "</font></u>" + getResources().getString(
                R.string.limit_media2);
        filetip_button_ly.setText(Html.fromHtml(result));
    }

    private void addData() {
//        MyAudio myAudio = new MyAudio();
//        myAudio.setItemType("MyAudio");
//        MyAudio myAudio1 = new MyAudio();
//        myAudio1.setItemType("SystemAudio");// 查看系统铃声
//        videos.add(myAudio);
//        videos.add(myAudio1);
//        List<MyAudio> list = GetLocalFile.getAllAudioPathsByFolder(
//                getActivity(), null);
//        if (list != null)
//            videos.addAll(list);
        syslist.add("MyAudio");
        syslist.add("SystemAudio");
        mCursor = getCursor(null);
        adapter = new MyAudioAdapter(getActivity(), mCursor, syslist, this);
        audio_show.setAdapter(adapter);
    }

    private void initListener() {
        audio_show.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String nameofItem = (String) parent.getItemAtPosition(position);
                if (nameofItem.equals("MyAudio")) {
                    // 录音
                    getNewAudio();
                } else if (nameofItem.equals("SystemAudio")) {// 调用系统铃声

                    ((AlbumActivity) getActivity()).setChangView(0, AlbumActivity.FLODER,
                            "SystemMusic");
                    ((AlbumActivity) getActivity()).setAllPhotoTitle(getResources().getString(R.string.system_audio));

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

        int size = mCursor.getCount();
        for (int i = 0; i < size; i++) {
            int id = getIDOfCursor(i);
            if (map.get(id)) {
                String filepath = getDataOfCursor(i);
                MyAudio myAudio = new MyAudio();
                myAudio.setPath(filepath);
                myAudio.setAlbumid(getAlbumidOfCursor(i));
                myAudio.setId(id);
                myAudio.setTitle(getTitleOfCursor(i));
                selectedImageList.add(myAudio);
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

        if (code == AlbumActivity.FLODER) {
            // 某个音频目录
            // if (isLatest
            // || (folderPath != null && !folderPath.equals(currentFolder))) {
            if (folderPath != null) {
                if (folderPath.equals("SystemMusic")) {// 系统铃声
                    isSysAudio = true;
                    syslist.clear();
                    MySparseBooleanArray.clearSelectionMap();
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                    mCursor = getRingtoneList(RingtoneManager.TYPE_RINGTONE);
                    adapter.setRingtoneList(ringtonemanager);
                    adapter.changeCursor(mCursor);
                } else {
                    isSysAudio = false;
                    currentFolder = folderPath;
                    updateView(AlbumActivity.FLODER, currentFolder);
                }
            }
        } else if (code == AlbumActivity.ALLPICTRUE) {
            isSysAudio = false;
            // “全部音频”
            // // if (!isLatest) {
            updateView(AlbumActivity.ALLPICTRUE, null);
            // isLatest = true;
            // }
        }
    }

    /**
     * 根据音频所属文件夹路径，刷新页面
     */
    private void updateView(int code, String folderPath) {
        MySparseBooleanArray.clearSelectionMap();
        setConfirmEnable();
        if (code == AlbumActivity.FLODER) { // 某个视频库
            syslist.clear();
            mCursor = getCursor(folderPath);
//            videos.addAll(GetLocalFile.getAllAudioPathsByFolder(getActivity(),
//                    folderPath));
        } else if (code == AlbumActivity.ALLPICTRUE) { // 全部视频
            mCursor = getCursor(null);
            syslist.add("MyAudio");
            syslist.add("SystemAudio");
        }
        adapter.changeCursor(mCursor);
        if (mCursor.getCount() > 0) {
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
//        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, Long.parseLong(String.valueOf(sizeLimit)));// 现在大小
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
        adapter.notifyDataSetChanged();
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
    public Cursor getRingtoneList(int type) {

//        List<MyAudio> audios = new ArrayList<MyAudio>();

        ringtonemanager = new RingtoneManager(getActivity());

        ringtonemanager.setType(type);
        Cursor cursor = ringtonemanager.getCursor();
//        Cursor cursor = manager.getCursor();
//
//        int count = cursor.getCount();
//        for (int i = 0; i < count; i++) {
//            MyAudio myAudio = new MyAudio();
//
//            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
//            myAudio.setTitle(title);
//            // myAudio.setRingtone(manager.getRingtone(i));// 这个对象可以直接播放
//            Uri path = manager.getRingtoneUri(i);
//            myAudio.setPath(path.toString());
//            myAudio.setItemType("Audio");
//            audios.add(myAudio);
//        }

        return cursor;

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

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                res = cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return res;
    }

    /**
     * 获取音频数据
     *
     * @param folderPath 指定路径，没有就获取全部音频数据
     * @return
     */
    private Cursor getCursor(String folderPath) {
        Uri mAudioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;// Uri
        String orderBy = MediaStore.Audio.Media.DATE_MODIFIED;
        String selection = null;

        if (folderPath != null) {// 查找特定路径下的数据
            selection = MediaStore.Audio.Media.DATA + " like " + DatabaseUtils.sqlEscapeString(folderPath + '%');


        }
        Cursor cursor = getActivity().getContentResolver().query(mAudioUri, null,
                selection, null, orderBy + " desc");
        return cursor;
    }

    /**
     * 获取音频数据的地址
     *
     * @param position 需要获取地址的音频
     * @return
     */
    private String getDataOfCursor(int position) {
        String filepath;
        mCursor.moveToPosition(position);
        if (isSysAudio) {
            Uri uri = ringtonemanager.getRingtoneUri(position);
            filepath = uri.toString();
        } else {
            filepath = "file://" + mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        }
        return filepath;

    }

    private int getIDOfCursor(int position) {
        int id;
        mCursor.moveToPosition(position);
        if (isSysAudio) {
            id = mCursor.getInt(RingtoneManager.ID_COLUMN_INDEX);
        } else {
            id = mCursor.getInt(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        }
        return id;
    }

    private long getAlbumidOfCursor(int position) {
        if (isSysAudio) {
            return 0;
        }
        mCursor.moveToPosition(position);
        long albumid = mCursor.getLong(mCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        return albumid;
    }

    private String getTitleOfCursor(int position) {
        String title;
        mCursor.moveToPosition(position);
        if (isSysAudio) {
            title = mCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
        } else {
            title = mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        }
        return title;
    }


    /**
     * 因为读取系统铃声方法导致Cursor被activity管理，所以节面finish（）要先清空cursor。
     */
    public void clear() {
        if (adapter != null)
            adapter = null;
        audio_show.setAdapter(null);
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }
}
