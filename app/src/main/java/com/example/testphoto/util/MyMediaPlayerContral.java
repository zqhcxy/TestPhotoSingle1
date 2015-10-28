package com.example.testphoto.util;

import android.content.Context;

import com.example.testphoto.views.MyMediaPlayerView;

/**
 * 音频播放控制器
 * Created by zqh-pc on 2015/9/16.
 */
public class MyMediaPlayerContral {
    private MyMediaPlayerView myMediaPlayerView;
    //    private RelativeLayout nomal_audio;
    private int position = -1;
    Context context;

    public MyMediaPlayerContral() {
    }

    public MyMediaPlayerContral(Context context) {
        this.context = context;
    }

    /**
     * 不需要回调，外部主界面使用
     */
    public void setMediaPlayerView(MyMediaPlayerView myMediaPlayerView, String path, int position) {
        if (this.position != position) {
            stopPlay();
            this.myMediaPlayerView = myMediaPlayerView;
            this.position = position;
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
    public void setMediaPlayerView(MyMediaPlayerView myMediaPlayerView, String path, int position, MyMediaPlayerView.CompletionCallback completionCallback) {
        if (this.position != position) {
            stopPlay();
            this.myMediaPlayerView = myMediaPlayerView;
            this.position = position;
        }
        playing(path, completionCallback);
    }

    /**
     * 音频界面调用，即需要回调的
     *
     * @param path
     * @param completionCallback
     */
    private void playing(String path, MyMediaPlayerView.CompletionCallback completionCallback) {
        if (myMediaPlayerView != null) {
            myMediaPlayerView.setMyOnClickListener(path, completionCallback);
        }
    }

    /**
     * 非音频界面调用 播放，即不需要回调的
     *
     * @param path
     */
    private void playing(String path) {
        if (myMediaPlayerView != null) {
            myMediaPlayerView.setMyOnClickListener(path);
        }
    }

    public void stopPlay() {
        if (myMediaPlayerView != null) {
            myMediaPlayerView.stopMediaPlayer();
        }
    }

}
