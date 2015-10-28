package com.example.testphoto.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.testphoto.R;
import com.example.testphoto.adapter.FragmentViewPagerAdapter;
import com.example.testphoto.model.ConfirmLocalFileInf;
import com.example.testphoto.model.MySparseBooleanArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地图片限制界面
 *
 * @author zqh
 */
public class AlbumActivity extends FragmentActivity implements OnClickListener,
        OnPageChangeListener {

    private static final int PHOTO_TYPE = 1;
    private static final int VIDEO_TYPE = 0;
    private static final int AUDIO_TYPE = 2;
    private boolean isAll = true;
    private boolean isFirst = true;

    //    private Button backBT;// 返回相册
    private ImageView bcakIV;
    private TextView titleTV;// 标题
    private Button confirm;// 确定


    private TextView album_all;//全部
    private TextView album_files;//文件夹

    // *******头部滑动元素*********//
    /**
     * 滑动界面
     */
    private ViewPager mViewPager;
    private LinearLayout title_ly;

    /**
     * 存放Fragment界面
     */
    private static List<Fragment> mTabs;
    /**
     * 头部的标题
     */
    private List<TextView> mTabIndicator;

    /**
     * 显示全部图片界面
     */
    public ShowAllPhotoFragment showAllPhotoFragment;
    /**
     * 相册界面
     */
    public PhotoAlbumFragment photoAlbumFragment;
    public ShowVideoFragment showVideoFragment;
    public ShowAudioFragment showAudioFragment;

    private boolean isSystemMusic = false;
    private int type;//当前显示界面的多媒体类型。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity);
        initView();
        initData();
        mViewPager.addOnPageChangeListener(this);// 滑动渐变效果监听
        resetOtherTabs();
        setSelectView(0);

    }

    private void initView() {
        titleTV = (TextView) findViewById(R.id.topbar_title_tv);
        bcakIV = (ImageView) findViewById(R.id.topbar_left_iv);
        confirm = (Button) findViewById(R.id.topbar_right_btn);
        title_ly = (LinearLayout) findViewById(R.id.title_ly);
        confirm.setText(R.string.main_confirm);
//		backBT.setText(R.string.main_back);
        bcakIV.setVisibility(View.VISIBLE);
        confirm.setVisibility(View.VISIBLE);
        confirm.setAlpha(0.5f);

        setOverflowShowingAlways();
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);

        bcakIV.setOnClickListener(this);
        confirm.setOnClickListener(this);
    }

    private void setOverflowShowingAlways() {
        try {
            // true if a permanent menu key is present, false otherwise.
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        Intent intent = getIntent();
        type = intent.getIntExtra("type", PHOTO_TYPE);

        mTabs = new ArrayList<Fragment>();
        mTabIndicator = new ArrayList<TextView>();
        photoAlbumFragment = new PhotoAlbumFragment(type);

        if (type == PHOTO_TYPE) {
            titleTV.setText(R.string.select_image);
            showAllPhotoFragment = new ShowAllPhotoFragment();
            mTabs.add(showAllPhotoFragment);
        } else if (type == VIDEO_TYPE) {
            titleTV.setText(R.string.select_video);
            showVideoFragment = new ShowVideoFragment();
            mTabs.add(showVideoFragment);
        } else if (type == AUDIO_TYPE) {
            titleTV.setText(R.string.select_audio);
            showAudioFragment = new ShowAudioFragment();
            mTabs.add(showAudioFragment);
        }
        mTabs.add(photoAlbumFragment);
        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(
                this.getSupportFragmentManager(), mViewPager, mTabs);
        initTabIndicator();
    }

    /**
     * 初始化标题
     */
    private void initTabIndicator() {
        TextView one = (TextView) findViewById(R.id.id_indicator_one);
        TextView two = (TextView) findViewById(R.id.id_indicator_two);
        album_all = (TextView) findViewById(R.id.album_all);
        album_files = (TextView) findViewById(R.id.album_files);
        // mainmovice_cimemas = (TextView)
        // findViewById(R.id.mainmovice_cimemas);
        mTabIndicator.add(one);
        mTabIndicator.add(two);

        album_all.setOnClickListener(this);
        album_files.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.album_all:
                resetOtherTabs();
                setSelectView(0);
                break;
            case R.id.album_files:
                resetOtherTabs();
                setSelectView(1);
                break;
            case R.id.topbar_left_iv:// 返回
                backAction();
                break;
            case R.id.topbar_right_btn:// 确定
                int position = mViewPager.getCurrentItem();// 获取当前显示第几个界面
                if (mTabs.get(position) instanceof ConfirmLocalFileInf) {
                    ConfirmLocalFileInf inf = (ConfirmLocalFileInf) mTabs
                            .get(position);
                    inf.confirmLoacl();
                }
                break;
        }
    }

    /**
     * 外部切换界面数据
     *
     * @param posotion 要切换的界面
     * @param code     切换到显示全部图片界面需要的参数，用来区分
     * @param path
     */
    public void setChangView(int posotion, int code, String path) {
        if (code == 100) {// 非显示全部
            isAll = false;
            isFirst = false;
        }
        MySparseBooleanArray.clearSelectionMap();
        if (mTabs.get(posotion) instanceof ShowAllPhotoFragment) {
//            MySparseBooleanArray.clearSelectionMap();
            ((ShowAllPhotoFragment) mTabs.get(posotion)).hidButtonLy();//隐藏底部布局
            ((ShowAllPhotoFragment) mTabs.get(posotion))
                    .onNewIntent(code, path);
        } else if (mTabs.get(posotion) instanceof ShowVideoFragment) {
            ((ShowVideoFragment) mTabs.get(posotion)).hidButtonLy();//隐藏底部布局
            ((ShowVideoFragment) mTabs.get(posotion)).onNewIntent(code, path);
        } else if (mTabs.get(posotion) instanceof ShowAudioFragment) {
//            MySparseBooleanArray.clearSelectionMap();
            ((ShowAudioFragment) mTabs.get(posotion)).hidButtonLy();//隐藏底部布局
            if (path != null && path.equals("SystemMusic")) {// 如果是系统铃声就要手动隐藏标题
                hidTabHost();
                isSystemMusic = true;
            }
            ((ShowAudioFragment) mTabs.get(posotion)).onNewIntent(code, path);
        }
        resetOtherTabs();
        setSelectView(posotion);
    }

    /**
     * 设置选中项线条颜色
     */
    private void setSelectView(int position) {
        setGroupTitleColor(position);
//        mTabIndicator.get(position).setBackgroundColor(
//                getResources().getColor(R.color.blue2_bg));
        mTabIndicator.get(position).setBackgroundColor(Color.parseColor("#aeddff"));
        mTabIndicator.get(position).setAlpha(1.0f);
        mViewPager.setCurrentItem(position, true);
    }

    /**
     * 设置选中页面的标题颜色
     *
     * @param position 哪个页面
     */
    private void setGroupTitleColor(int position) {
        if (position == 0) {
            album_files.setTextColor(getResources().getColor(R.color.media_grouptit_bg));
            album_all.setTextColor(getResources().getColor(R.color.topbar_btn_text_color));
        } else if (position == 1) {
            album_files.setTextColor(getResources().getColor(R.color.topbar_btn_text_color));
            album_all.setTextColor(getResources().getColor(R.color.media_grouptit_bg));
        }
    }

    /**
     * 重置其他的Tab
     */
    private void resetOtherTabs() {
        for (int i = 0; i < mTabIndicator.size(); i++) {
            mTabIndicator.get(i).setAlpha(0);
        }
    }

    // 重写返回键
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int arg2) {
        if (positionOffset > 0) {
            TextView left = mTabIndicator.get(position);
            TextView right = mTabIndicator.get(position + 1);
            left.setAlpha(1 - positionOffset);
            right.setAlpha(positionOffset);
        }

    }

    @Override
    public void onPageSelected(int arg0) {
        setGroupTitleColor(arg0);
        if (arg0 == 1) {// 文件夹
            confirm.setVisibility(View.GONE);
            setAllPhotoTitle("");
            isAll = true;
            isSystemMusic = false;
            showTabHost();
        } else if (arg0 == 0) {
            confirm.setVisibility(View.VISIBLE);
            if (isAll) {// 全部
                if (!isFirst) {
                    showTabHost();
                    setChangView(0, 200, null);
                    isFirst = true;
                }
            } else {
                hidTabHost();
            }
        }

    }

    /**
     * 返回按钮事件
     */
    private void backAction() {
        if (title_ly.getVisibility() == View.VISIBLE) {
            MySparseBooleanArray.clearSelectionMap();
            finish();
        } else if (title_ly.getVisibility() == View.GONE) {
            // 看具体情况需不需要，系统铃声是否需要返回到全部界面
            if (isSystemMusic) {
                showTabHost();
                setChangView(0, 200, null);
                setAllPhotoTitle("");
                isFirst = true;
            } else {// 返回的是文件夹列表界面
                resetOtherTabs();
                setSelectView(1);
                setConfirmBt(0);
            }

        }

    }


    /**
     * 显示头部标题
     *
     * @param title 标题
     */
    public void setAllPhotoTitle(String title) {
//		titleTV.setText(title);
        if (TextUtils.isEmpty(title)) {
            if (type == PHOTO_TYPE) {
                titleTV.setText(R.string.select_image);
            } else if (type == VIDEO_TYPE) {
                titleTV.setText(R.string.select_video);
            } else if (type == AUDIO_TYPE) {
                titleTV.setText(R.string.select_audio);
            }
        } else {
            titleTV.setText(title);
        }

    }

    /**
     * 显示确定按钮
     *
     * @param size
     */
    public void setConfirmBt(int size) {
        if (size > 0) {
            confirm.setAlpha(1f);

            confirm.setEnabled(true);
//            confirm.setVisibility(View.VISIBLE);

        } else {
            confirm.setAlpha(0.5f);
            confirm.setEnabled(false);
//            confirm.setVisibility(View.GONE);
        }
    }

    /**
     * 显示分组标题
     */
    private void showTabHost() {
        if (title_ly.getVisibility() == View.GONE)
            title_ly.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏分组标题
     */
    private void hidTabHost() {
        if (title_ly.getVisibility() == View.VISIBLE)
            title_ly.setVisibility(View.GONE);
    }
}
