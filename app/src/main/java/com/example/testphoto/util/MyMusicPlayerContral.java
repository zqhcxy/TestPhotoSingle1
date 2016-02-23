package com.example.testphoto.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.example.testphoto.views.MyMediaPlayerView;

import java.io.IOException;


/**
 * 音频播放控制器
 * Created by zqh-pc on 2015/9/16.
 */
public class MyMusicPlayerContral {
    public MyMediaPlayerView myMediaPlayerView;
    //    private RelativeLayout nomal_audio;
    public long position = -1;
    private static Context mContext;
    private static MyMusicPlayerContral mMyMusicPlayerContral;
    private CompletionCallback mCompletionCallback;// 音频播放完成回调
    private MediaPlayer mMediaPlayer;


    public static MyMusicPlayerContral getInstent(Context context) {
        mContext=context;
        if (mMyMusicPlayerContral == null) {
            mMyMusicPlayerContral = new MyMusicPlayerContral();
        }
        return mMyMusicPlayerContral;

    }

    /**
     * 不需要回调，外部主界面使用
     * <p/>
     * <p></position保证不会变化。不然会出错，消息列表能删除，会改变position，所以传递是msgid>
     */
    public void setMediaPlayerView(MyMediaPlayerView myMediaPlayerView, Uri path, long position) {

        if (this.position != position) {
            stopPlay();
            this.position = position;
            this.myMediaPlayerView = myMediaPlayerView;
        }
        playing(path);
    }

    /**
     * 音频界面调用,需要回调
     *
     * @param myMediaPlayerView
     * @param path
     * @param position
     * @param completionCallback
     */
    public void setMediaPlayerView(MyMediaPlayerView myMediaPlayerView, Uri path, long position, CompletionCallback completionCallback) {

        if (this.position != position) {
            stopPlay();
            this.position = position;
            this.myMediaPlayerView = myMediaPlayerView;

        }
        playing(path, completionCallback);
    }

    /**
     * 音频界面调用，即需要回调的
     *
     * @param aduioPath
     * @param completionCallback
     */
    private void playing(Uri aduioPath, CompletionCallback completionCallback) {
        if (completionCallback != null)//有些地方不需要这个
            mCompletionCallback = completionCallback;
        if (aduioPath != null) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                stopPlay();
            else
                playingMediaPlayer(aduioPath);
        }
    }

    /**
     * 非音频界面调用 播放，即不需要回调的
     *
     * @param aduioPath
     */
    private void playing(Uri aduioPath) {
        if (aduioPath != null) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                stopPlay();
            else
                playingMediaPlayer(aduioPath);
        }
    }

    /**
     * 音频播放
     */
    private void playingMediaPlayer(Uri uri) {
        stopPlay();
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }

        try {
            mMediaPlayer.setDataSource(mContext, uri);
            mMediaPlayer.setOnCompletionListener(myOnCompletionListener);
            // 手机铃声的声音
            // mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            myMediaPlayerView.playingUI();
        } catch (IOException e) {
            Log.w("MusicPicker", "Unable to play track", e);
        }
    }


    /**
     * 停止播放
     */
    public void stopPlay() {
        if (mMediaPlayer != null) {
            if (myMediaPlayerView != null) {
                myMediaPlayerView.stopPlayingUI();
            }
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            this.position = -1;
        }
    }


    // 播放完成
    private MediaPlayer.OnCompletionListener myOnCompletionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mMediaPlayer == mp) {
                stopPlay();
                if (mCompletionCallback != null)
                    mCompletionCallback.CompletionMusic();
            }
        }
    };

    /**
     * 当前位置的音频是否还是播放状态
     *
     * @param position
     * @return
     */
    public boolean isPlaying(long position) {
        if (position == this.position) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                return true;
            }
        }
        return false;
    }


    public void destroy() {
        mMyMusicPlayerContral = null;
    }


    // 对外界开放的回调接口
    public interface CompletionCallback {
        // 注意 此方法是用来设置播放完成后的回调
        public void CompletionMusic();
    }

    public void setLastMyMediaPlayerView(MyMediaPlayerView myMediaPlayerView) {
        this.myMediaPlayerView = myMediaPlayerView;
    }
}
