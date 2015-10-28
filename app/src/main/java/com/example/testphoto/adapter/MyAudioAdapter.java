package com.example.testphoto.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.testphoto.R;
import com.example.testphoto.fragment.ShowAudioFragment;
import com.example.testphoto.model.MyAudio;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.DensityUtil;
import com.example.testphoto.util.MyMediaPlayerContral;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.views.MyMediaPlayerView;

import java.util.List;

/**
 * 显示所有音频适配器
 *
 * @author zqh-pc
 */
public class MyAudioAdapter extends BaseAdapter {

    private static final String AUDIO = "Audio";
    private static final String MYAUDIO = "MyAudio";
    private static final String SYSAUDIO = "SystemAudio";
    private static final int AUDIO_TYPE = 0;
    private static final int MYAUDIO_TYPE = 1;
    private static final int SYSAUDIO_TYPE = 2;

    private List<MyAudio> listAudios;
    private LayoutInflater mLayoutInflater;
    private ShowAudioFragment showAudioFragment;
    private Context context;
    private RelativeLayout.LayoutParams layoutParams;
    /**
     * 音频播放控制器
     */
    private MyMediaPlayerContral myMediaPlayerContral;
    //    private Map<Integer, Boolean> hashMap;
//    private static SparseBooleanArray selectionMap;
    int seletionpos = -1;


    public MyAudioAdapter(Context context, ShowAudioFragment showAudioFragment,
                          List<MyAudio> listAudios) {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.listAudios = listAudios;
        this.showAudioFragment = showAudioFragment;
        myMediaPlayerContral = new MyMediaPlayerContral(context);
//        selectionMap = new SparseBooleanArray();

        int imgw = (ScreenUtils.getScreenW() - DensityUtil.dip2px(context, 4)) / 3;
        layoutParams = new RelativeLayout.LayoutParams(imgw, imgw);
    }

    @Override
    public int getCount() {
        return listAudios.size();
    }

    @Override
    public Object getItem(int position) {
        return listAudios.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        MyAudio myAudio = listAudios.get(position);
        switch (myAudio.getItemType()) {
            case MYAUDIO:
                return MYAUDIO_TYPE;
            case SYSAUDIO:
                return SYSAUDIO_TYPE;
            case AUDIO:
                return AUDIO_TYPE;
        }
        return -1;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final MyAudio myAudio = listAudios.get(position);
        int mediatype = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            if (mediatype == MYAUDIO_TYPE) {//调用系统录音
                convertView = mLayoutInflater.inflate(R.layout.sysyem_media_item, null);
//                holder.sys_media_iv = (ImageView) convertView.findViewById(R.id.sys_media_iv);
                holder.sys_media_tv = (TextView) convertView.findViewById(R.id.sys_media_tv);
                holder.sys_meida_ly = (RelativeLayout) convertView.findViewById(R.id.sys_meida_ly);
                holder.sys_meida_ly.setLayoutParams(layoutParams);
            } else if (mediatype == SYSAUDIO_TYPE) {//铃声库
                convertView = mLayoutInflater.inflate(R.layout.photo_album_lv_item, null);
                holder.firstImageIV = (ImageView) convertView
                        .findViewById(R.id.select_img_gridView_img);
                holder.pathNameTV = (TextView) convertView
                        .findViewById(R.id.path_filename_tv);
                holder.pathNameTV.setGravity(Gravity.CENTER);
                holder.album_item_ly = (RelativeLayout) convertView.findViewById(R.id.album_item_ly);
                holder.album_item_ly.setLayoutParams(layoutParams);
            } else if (mediatype == AUDIO_TYPE) {//本地音频文件
                convertView = mLayoutInflater.inflate(R.layout.allaudio_item, null);

                holder.nomal_audio = (RelativeLayout) convertView.findViewById(R.id.nomal_audio);
                holder.media_play_ly = (MyMediaPlayerView) convertView.findViewById(R.id.media_play_ly);
                holder.checkBox = (CheckBox) convertView
                        .findViewById(R.id.allaudio_cb);
                holder.chackbox_ly = (LinearLayout) convertView
                        .findViewById(R.id.allaudio_chackbox_ly);
                holder.title = (TextView) convertView
                        .findViewById(R.id.allaudio_title);
            }

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mediatype == MYAUDIO_TYPE) {
//            holder.sys_media_iv.setImageResource(R.drawable.ic_media_recorder);
            holder.sys_media_tv.setText(R.string.audio_corder);
            Drawable drawable =convertView.getResources().getDrawable(R.drawable.ic_media_recorder);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.sys_media_tv.setCompoundDrawables(null, drawable, null, null);
        } else if (mediatype == SYSAUDIO_TYPE) {// 系统铃声
            holder.firstImageIV.setImageResource(R.drawable.ic_media_system);
            holder.firstImageIV.setScaleType(ImageView.ScaleType.CENTER);
            holder.pathNameTV.setText(R.string.system_audio);
        } else if (mediatype == AUDIO_TYPE) {// 正常音频
            final String filepath = myAudio.getPath();


            holder.checkBox.setChecked(MySparseBooleanArray.get(position));
            holder.title.setVisibility(View.VISIBLE);
            holder.chackbox_ly.setTag(R.id.tag_first, position);
            holder.chackbox_ly.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (holder.checkBox.isChecked()) {
                        holder.checkBox.setChecked(false);
                    } else {
                        holder.checkBox.setChecked(true);
                    }
                    Integer position = (Integer) v.getTag(R.id.tag_first);
                    MySparseBooleanArray.clearSelectionMap();
                    MySparseBooleanArray.setSelectionData(position,
                            holder.checkBox.isChecked(), filepath);

                    showAudioFragment.setConfirmEnable();
                    notifyDataSetChanged();
                    // 底部弹出区域
                    boolean ischoice = showAudioFragment
                            .isHaveChoice();
                    if (ischoice) {
                        showAudioFragment.showButtonLy();
                    } else {
                        showAudioFragment.hidButtonLy();
                    }

                }
            });

            holder.title.setText(myAudio.getTitle());
            holder.media_play_ly.initMediaData(myAudio.getAlbumid(), myAudio.getId());
            holder.media_play_ly.setTag(position);


            if (seletionpos == position) {
                holder.media_play_ly.setPlaysate(true);
                holder.nomal_audio.setBackgroundColor(context.getResources().getColor(R.color.media_bg3));
            } else {
                holder.media_play_ly.setPlaysate(false);
                holder.nomal_audio.setBackgroundColor(context.getResources().getColor(R.color.white));
            }

            holder.media_play_ly.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Integer position = (Integer) view.getTag();
                    if (seletionpos == position) {
                        seletionpos = -1;
                    } else {
                        seletionpos = position;
                    }
                    myMediaPlayerContral.setMediaPlayerView(holder.media_play_ly, filepath, position, completionCallback);
                    notifyDataSetChanged();
                }
            });
        }
        return convertView;
    }


    /**
     * 外部因为界面切换或者黑屏之类的要停止播放
     */
    public void stopPlay() {
        myMediaPlayerContral.stopPlay();
    }

    /**
     * 音频播放完成回调
     */
    private MyMediaPlayerView.CompletionCallback completionCallback = new MyMediaPlayerView.CompletionCallback() {

        @Override
        public void CompletionMusic() {
            seletionpos = -1;
            notifyDataSetChanged();
        }
    };


    public final class ViewHolder {
        //本地音频
        public TextView title;
        private CheckBox checkBox;
        private LinearLayout chackbox_ly;
        private MyMediaPlayerView media_play_ly;
        private RelativeLayout nomal_audio;//整个item的布局
        //系统录音布局
//        private ImageView sys_media_iv;//显示系统相机的图标
        private TextView sys_media_tv;//系统相机的标题
        private RelativeLayout sys_meida_ly;//整个布局。因为是自适应大小的，所以需要设置。
        //系统铃声库
        private ImageView firstImageIV;
        private TextView pathNameTV;
        private RelativeLayout album_item_ly;

    }
}
