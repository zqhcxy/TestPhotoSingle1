package com.example.testphoto.views;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.testphoto.R;
import com.example.testphoto.util.DensityUtil;
import com.example.testphoto.util.MyMusicPlayerContral;

/**
 * 自定义播放器控件
 * Created by zqh-pc on 2015/9/16.
 */
public class MyMediaPlayerView extends RelativeLayout {

    private int mPlayingId;// 当前播放音频id
    private Context context;

    private CircleImageView circleImageView;//封面
    private ImageView audio_playing;//播放状态
    Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");

    public long albumId;
    public int audioId;
    private int circleiv_wh = 70;//单位dp，然后自己转换
    private int playinng_wh = 25;//单位dp，然后自己转换

    private RotateAnimation mAnim;


    public MyMediaPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        //图片宽高定义外部设置
        initView();
    }

    private void initView() {
        circleImageView = new CircleImageView(context);
        audio_playing = new ImageView(context);
        circleImageView.setImageResource(R.drawable.ic_audio_bg);
        audio_playing.setImageResource(R.drawable.ic_audio_play);

        int circleiv = DensityUtil.dip2px(context, circleiv_wh);
        int playingiv = DensityUtil.dip2px(context, playinng_wh);
        LayoutParams layoutParams = new LayoutParams(circleiv, circleiv);
        LayoutParams layoutParams1 = new LayoutParams(playingiv, playingiv);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);//addRule参数对应RelativeLayout XML布局的属性
        layoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(circleImageView, layoutParams);
        addView(audio_playing, layoutParams1);


    }

    public void setImageViewWH(int circleiv_wh, int playinng_wh) {

        circleiv_wh = DensityUtil.dip2px(context, circleiv_wh);
        playinng_wh = DensityUtil.dip2px(context, playinng_wh);

        LayoutParams parents = (LayoutParams) circleImageView.getLayoutParams();
        parents.width = circleiv_wh;
        parents.height = circleiv_wh;
        circleImageView.setLayoutParams(parents);

        LayoutParams parents1 = (LayoutParams) audio_playing.getLayoutParams();
        parents1.width = playinng_wh;
        parents1.height = playinng_wh;
        audio_playing.setLayoutParams(parents1);
    }

    public void initMediaData(long albumId, int audioId) {

        this.albumId = albumId;
        this.audioId = audioId;
        loadPic();
        long position = Long.parseLong(this.getTag() + "");
        if (MyMusicPlayerContral.getInstent(context).isPlaying(position)) {
            initAnim();
            if (circleImageView.getAnimation() == null) {
                circleImageView.startAnimation(mAnim);
            }
        } else {
            circleImageView.clearAnimation();
        }

    }

    private void loadPic() {
        Uri uri;
        if (albumId < 0) {
            uri = Uri.parse("content://media/external/audio/media/"
                    + audioId + "/albumart");
        } else {
            uri = ContentUris.withAppendedId(sArtworkUri, albumId);
        }
        Glide.with(context).loadFromMediaStore(uri).listener(new RequestListener<Uri, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                //TODO 因为不刷新，临时解决方法。看下源码为什么不刷新。
                if (!isFromMemoryCache) {
                    circleImageView.setImageDrawable(resource);
                }
                return false;
            }
        }).override(120, 120).placeholder(R.drawable.ic_audio_bg).crossFade().into(circleImageView);

    }

    public void setPlaysate(boolean isplay) {
        if (isplay) {
            audio_playing.setImageResource(R.drawable.ic_audio_stop);
            if (circleImageView.getAnimation() == null) {
                initAnim();
                circleImageView.setAnimation(mAnim);
            }
        } else {
            audio_playing.setImageResource(R.drawable.ic_audio_play);
            circleImageView.clearAnimation();
        }
    }

    /**
     * 播放音频动画
     */
    private void initAnim() {
        if (mAnim == null) {
            //前两个参数是旋转的方向，逆时针、顺时针
            mAnim = new RotateAnimation(0, 360, Animation.RESTART, 0.5f, Animation.RESTART, 0.5f);
            mAnim.setDuration(2000);
            LinearInterpolator lin = new LinearInterpolator();
            mAnim.setInterpolator(lin);//设置动画不停顿
            mAnim.setRepeatCount(Animation.INFINITE);
            mAnim.setRepeatMode(Animation.RESTART);
            mAnim.setStartTime(Animation.START_ON_FIRST_FRAME);
            mAnim.setFillAfter(false);
        }
    }

    /**
     * 播放时的UI
     */
    public void playingUI() {
        audio_playing.setImageResource(R.drawable.ic_audio_stop);
        initAnim();
        circleImageView.startAnimation(mAnim);
    }

    /**
     * 停止播放UI
     */
    public void stopPlayingUI() {
        audio_playing.setImageResource(R.drawable.ic_audio_play);
        mPlayingId = -1;
        circleImageView.clearAnimation();
    }


}
