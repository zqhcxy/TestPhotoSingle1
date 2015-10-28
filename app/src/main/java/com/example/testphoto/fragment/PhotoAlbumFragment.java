package com.example.testphoto.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.testphoto.R;
import com.example.testphoto.adapter.PhotoAlbumLVAdapter;
import com.example.testphoto.model.PhotoAlbumLVItem;
import com.example.testphoto.util.GetLocalFile;
import com.example.testphoto.util.Utility;

import java.io.File;
import java.util.ArrayList;

/**
 * 相册列表
 *
 * @author zqh-pc
 */
public class PhotoAlbumFragment extends Fragment {
    private GridView select_img_gv;
    private int type;// 视频还是图片
    private static final int PHOTO_TYPE = 1;
    private static final int VIDEO_TYPE = 0;
    private static final int AUDIO_TYPE = 2;

    public PhotoAlbumFragment(int type) {
        this.type = type;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_album_activity, container,
                false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        select_img_gv = (GridView) view.findViewById(R.id.select_img_gv);
    }

    private void initData() {
        if (!Utility.isSDcardOK()) {
            Utility.showToast(getActivity(), "SD卡不可用。");
            return;
        }

        final ArrayList<PhotoAlbumLVItem> list = new ArrayList<PhotoAlbumLVItem>();
        if (type == PHOTO_TYPE) {
            // 相册
            list.addAll(GetLocalFile
                    .getImagePathsByContentProvider(getActivity()));
        } else if (type == VIDEO_TYPE) {
            // 每个视频文件夹
            list.addAll(GetLocalFile
                    .getVideoPathsByContentProvider(getActivity()));
        } else if (type == AUDIO_TYPE) {// 音频
            // 每个视频文件夹
            list.addAll(GetLocalFile
                    .getAudioPathsByContentProvider(getActivity()));
            select_img_gv.setNumColumns(1);
            select_img_gv.setBackgroundColor(getActivity().getResources().getColor(R.color.media_bg1));
            select_img_gv.setVerticalSpacing(0);
        }

        PhotoAlbumLVAdapter adapter = new PhotoAlbumLVAdapter(getActivity(),
                list, type);
        select_img_gv.setAdapter(adapter);

        select_img_gv
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        String folderPath = list.get(position).getPathName();
                        ((AlbumActivity) getActivity()).setChangView(0, 100,
                                folderPath);
                        int lastSeparator = folderPath
                                .lastIndexOf(File.separator);
                        String folderName = folderPath
                                .substring(lastSeparator + 1);
                        ((AlbumActivity) getActivity())
                                .setAllPhotoTitle(folderName);

                    }
                });
    }

}
