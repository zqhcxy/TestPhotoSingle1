package com.example.testphoto.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 简单viewpage显示。底部菜单切换
 * <p/>
 * Created by zqh-pc on 2015/10/12.
 */
public class MyPageViewAdapter extends PagerAdapter {
    private List<View> views;

    public MyPageViewAdapter(List<View> views) {
        this.views = views;

    }


    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        container.removeView(views.get(position));

    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }
}
