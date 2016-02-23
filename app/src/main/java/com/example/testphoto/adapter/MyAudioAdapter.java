package com.example.testphoto.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.testphoto.R;
import com.example.testphoto.fragment.ShowAudioFragment;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.DensityUtil;
import com.example.testphoto.util.MyMusicPlayerContral;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.views.MyMediaPlayerView;

import java.util.List;

/**
 * 显示所有音频适配器
 *
 * @author zqh-pc
 */
public class MyAudioAdapter extends CursorAdapter {

    private static final String AUDIO = "Audio";
    private static final String MYAUDIO = "MyAudio";
    private static final String SYSAUDIO = "SystemAudio";
    private static final int AUDIO_TYPE = 0;
    private static final int MYAUDIO_TYPE = 1;
    private static final int SYSAUDIO_TYPE = 2;

    //    private List<MyAudio> listAudios;
    private List<String> syslist;
    private LayoutInflater mLayoutInflater;
    private ShowAudioFragment showAudioFragment;
    private Context context;
    private RelativeLayout.LayoutParams layoutParams;
    int seletionpos = -1;
    private RingtoneManager mRingtoneManager;

    public MyAudioAdapter(Context context, Cursor c, List<String> list, ShowAudioFragment showAudioFragment) {
        super(context, c, true);
        this.context = context;
        syslist = list;
        mLayoutInflater = LayoutInflater.from(context);
        this.showAudioFragment = showAudioFragment;
        int imgw = (ScreenUtils.getScreenW() - DensityUtil.dip2px(context, 4)) / 3;
        layoutParams = new RelativeLayout.LayoutParams(imgw, imgw);
    }

    public void setRingtoneList(RingtoneManager ringtoneList) {
        mRingtoneManager = ringtoneList;
    }

    @Override
    public int getCount() {
        Cursor cursor = getCursor();
        return cursor.getCount() + syslist.size();
    }

    @Override
    public Object getItem(int position) {
        if (position <= syslist.size() - 1) {
            return syslist.get(position);
        } else {
            return getDataOfCursor(position - syslist.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return position - syslist.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= syslist.size() - 1) {
            if (position == 0)
                return MYAUDIO_TYPE;
            else
                return SYSAUDIO_TYPE;
        } else {
            return AUDIO_TYPE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
//        final MyAudio myAudio = listAudios.get(position);
        int mediatype = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            if (mediatype == MYAUDIO_TYPE) {//调用系统录音
                convertView = mLayoutInflater.inflate(R.layout.sysyem_media_item, null);
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
            Drawable drawable = convertView.getResources().getDrawable(R.drawable.ic_media_recorder);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.sys_media_tv.setCompoundDrawables(null, drawable, null, null);
        } else if (mediatype == SYSAUDIO_TYPE) {// 系统铃声
            holder.firstImageIV.setImageResource(R.drawable.ic_media_system);
            holder.firstImageIV.setScaleType(ImageView.ScaleType.CENTER);
            holder.pathNameTV.setText(R.string.system_audio);
        } else if (mediatype == AUDIO_TYPE) {// 正常音频
            final String filepath = getDataOfCursor(position - syslist.size());
            String audioTitle = getTitleOfCursor(position - syslist.size());
            long audioAlbumid = getAlbumidOfCursor(position - syslist.size());
            int audioID = getIDOfCursor(position - syslist.size());


            holder.checkBox.setChecked(MySparseBooleanArray.get(audioID));
            holder.title.setVisibility(View.VISIBLE);
            holder.chackbox_ly.setTag(R.id.tag_first, audioID);
            holder.chackbox_ly.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean checkboxIscheck = holder.checkBox.isChecked();
                    if (checkboxIscheck) {
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
                }
            });

            holder.title.setText(audioTitle);
            holder.media_play_ly.setTag(audioID);
            holder.media_play_ly.initMediaData(audioAlbumid, audioID);


            if (seletionpos == audioID) {
                holder.media_play_ly.setPlaysate(true);
                holder.nomal_audio.setBackgroundColor(ContextCompat.getColor(context, R.color.media_bg3));
            } else {
                holder.media_play_ly.setPlaysate(false);
                holder.nomal_audio.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
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
                    final Uri uri = Uri.parse(filepath);
                    MyMusicPlayerContral.getInstent(context).setMediaPlayerView(holder.media_play_ly, uri, position, completionCallback);
                    notifyDataSetChanged();
                }
            });
        }
        return convertView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }


    /**
     * 音频播放完成回调
     */
    private MyMusicPlayerContral.CompletionCallback completionCallback = new MyMusicPlayerContral.CompletionCallback() {

        @Override
        public void CompletionMusic() {
            seletionpos = -1;
            notifyDataSetChanged();
        }
    };

    /**
     * 外部因为界面切换或者黑屏之类的要停止播放
     */
    public void stopPlay() {
        MyMusicPlayerContral.getInstent(context).stopPlay();
        seletionpos = -1;
    }

    /**
     * 获取音频数据的地址
     *
     * @param position 需要获取地址的音频
     * @return
     */
    private String getDataOfCursor(int position) {
        Cursor mCursor = getCursor();
        String filepath;
        mCursor.moveToPosition(position);
        if (showAudioFragment.isSysAudio) {

            Uri uri = mRingtoneManager.getRingtoneUri(position);
            filepath = uri.toString();
        } else {
            filepath = "file://" + mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        }
        return filepath;
    }

    private int getIDOfCursor(int position) {
        Cursor mCursor = getCursor();
        int id;
        mCursor.moveToPosition(position);
        if (showAudioFragment.isSysAudio) {
            id = mCursor.getInt(RingtoneManager.ID_COLUMN_INDEX);
        } else {
            id = mCursor.getInt(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        }
        return id;
    }

    private long getAlbumidOfCursor(int position) {
        if (showAudioFragment.isSysAudio) {
            return 0;
        }
        Cursor mCursor = getCursor();
        mCursor.moveToPosition(position);
        long albumid = mCursor.getLong(mCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        return albumid;
    }

    private String getTitleOfCursor(int position) {
        Cursor mCursor = getCursor();
        String title;
        mCursor.moveToPosition(position);
        if (showAudioFragment.isSysAudio) {
            title = mCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
        } else {
            title = mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        }
        return title;
    }

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
