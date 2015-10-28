package com.example.testphoto.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.testphoto.R;

import java.io.File;

/**
 * 聊天界面的fragment管理器
 * Created by zqh-pc on 2015/9/17.
 */
public class MsgFragmentActivity extends FragmentActivity implements OnClickListener {
    public MsgFragemnt msgFragemnt;//消息界面
    public FullEditFragment fullEditFragment;//全屏编辑的消息界面

    private FragmentManager fragmentManager;
    private int viewCode = 0;//当前显示的是哪个界面

    public static final int PHOTO_REQUST = 0x000003;// 选中的图片的回调
    public static final int AUDIO_REQUST = 0x000004;// 预览音频的回调
    public static final int VIDEO_REQUST = 0x000005;// 预览视频的回调
    private static final int PHOTO_TYPE = 100;
    private static final int AUDIO_TYPE = 101;
    private static final int VIDEO_TYPE = 102;
    private static final int PHOTO_TYPE_CUT = 103;


    //    private Button backBT;// 返回
//    private TextView titleTV;// 标题
    private ImageView bcakIV;
    private Button sendMsg;// 发送
    private LinearLayout topbar_shadow;//标题底部阴影


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messageall_activity);
        fragmentManager = getSupportFragmentManager();
        initView();
        stapOnclick(0);
    }

    private void initView() {

//        titleTV = (TextView) findViewById(R.id.topbar_title_tv);
        topbar_shadow = (LinearLayout) findViewById(R.id.topbar_shadow);
        bcakIV = (ImageView) findViewById(R.id.topbar_left_iv);
        sendMsg = (Button) findViewById(R.id.topbar_right_btn);
        sendMsg.setText(R.string.main_confirm);
        bcakIV.setVisibility(View.VISIBLE);
        topbar_shadow.setVisibility(View.VISIBLE);

        bcakIV.setOnClickListener(this);

    }

    public void stapOnclick(int position) {
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        switch (position) {
            case 0:
                transaction.setCustomAnimations(R.anim.main_translate_in, android.R.anim.fade_out);
                viewCode = 0;
                if (msgFragemnt == null) {
                    msgFragemnt = new MsgFragemnt();
                    transaction.add(R.id.mainfragment_layout, msgFragemnt);
                } else {
                    transaction.show(msgFragemnt);

                }

                break;
            case 1:
                transaction.setCustomAnimations(R.anim.main_translate_in, android.R.anim.fade_out);
                viewCode = 1;
                if (fullEditFragment == null) {
                    fullEditFragment = new FullEditFragment();
                    transaction.add(R.id.mainfragment_layout, fullEditFragment);
                } else {
                    transaction.show(fullEditFragment);
                    fullEditFragment.initData(msgFragemnt);
                }

                break;

            default:
                break;
        }
        transaction.commit();
    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (fullEditFragment != null) {
            transaction.hide(fullEditFragment);
        }
        if (msgFragemnt != null) {
            transaction.hide(msgFragemnt);
        }
    }


    /**
     * 显示多媒体的缩略图
     *
     * @param code
     * @param editphoto
     * @param file
     * @param img
     */
    public void glidePhoto(int code, String editphoto, File file, ImageView img) {
        if (code != AUDIO_TYPE && code != PHOTO_TYPE_CUT) {
            Glide.with(this).load(file).placeholder(R.drawable.empty_photo).centerCrop().override(80, 80).into(img);
        } else if (code == PHOTO_TYPE_CUT) {
            final File file1 = new File(editphoto);
            Glide.with(this).load(new File(editphoto)).listener(new RequestListener<File, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    file1.delete();
                    return false;
                }
            }).placeholder(R.drawable.empty_photo).centerCrop().override(80, 80).into(img);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.topbar_left_iv:
                if (viewCode == 1) {// 返回外部
                    backAction();
                }
//                else {
//                    finish();
//                }
                break;
        }
    }

    private void backAction() {
        stapOnclick(0);
        fullEditFragment.hidMenuly();
        String msg = fullEditFragment.fulledit_msg_et.getText().toString();
        msgFragemnt.msg_edit.setText(msg);
    }


    // 重写返回键
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && viewCode == 1) {
            backAction();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        //这里是点击主界面的选中图片进入预览界面，然后在点击图片的回调。
        String ischanggeview = data.getStringExtra("changeview");
        if (requestCode == PHOTO_REQUST) {
            if (ischanggeview != null && ischanggeview.equals("changeview")) {
                //图片预览界面点击进入图库的回调
                Intent intent1 = new Intent(MsgFragmentActivity.this, AlbumActivity.class);
                intent1.putExtra("type", 1);
                startActivityForResult(intent1, (PHOTO_REQUST));
                return;
            }
        }
        switch (requestCode) {
            case PHOTO_REQUST:
            case AUDIO_REQUST:
            case VIDEO_REQUST:
                if (viewCode == 0) {
                    msgFragemnt.onNewIntent(data);
                } else if (viewCode == 1) {
                    fullEditFragment.onNewIntent(data);
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        msgFragemnt.initViewPage();
        if(fullEditFragment!=null){
            fullEditFragment.initViewPage();
        }
    }
}
