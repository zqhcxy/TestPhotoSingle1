package com.example.testphoto.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.testphoto.R;

import java.util.List;

/**
 * 底部菜单的adapter
 * Created by zqh-pc on 2015/10/12.
 */
public class MyMenuAdapter extends BaseAdapter {
    List<String> strings;
    private static final int LANDSCAPE = 1001;
    private static final int PORTRAIT = 1002;
    private Context context;


    public MyMenuAdapter(Context context, List<String> strings, int fram, int window) {
        this.context = context;

        int lie = 8;
        if (window == LANDSCAPE) {
            lie = 5;
        } else if (window == PORTRAIT) {
            lie = 8;
        }

        int last_index = lie * fram;
        int size = strings.size();
        if (last_index >= size) {//最后一个位置不能大于集合大小
            last_index = size;
        }
        // 得到当前界面显示的集合
        this.strings = strings.subList((fram - 1) * lie, last_index);

    }

    @Override
    public int getCount() {
        if (strings != null) {
            return strings.size();
        } else {
            return 0;
        }


    }

    @Override
    public Object getItem(int i) {
        return strings.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.menu_item, viewGroup, false);
            viewHolder.menu_music = (TextView) view.findViewById(R.id.menu_music);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String title = strings.get(i);
        viewHolder.menu_music.setText(title);
        Drawable drawable = null;
        switch (title) {
            case "图片":
                drawable = context.getResources().getDrawable(R.drawable.but_edit_more_pic_normal);
                break;
            case "音频":
                drawable = context.getResources().getDrawable(R.drawable.but_edit_more_music_normal);
                break;
            case "名片":
                break;
            case "位置":
                break;
            case "语音":
                break;
            case "收藏":
                break;
            case "视频":
                drawable = context.getResources().getDrawable(R.drawable.but_edit_more_tv_normal);
                break;
            case "广告":
                break;
            case "涂鸦":
                break;
            case "拼写检查":
                break;
            case "添加主题":
                break;
            case "幻灯片":
                break;
            case "电子贺卡":
                break;
        }
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            viewHolder.menu_music.setCompoundDrawables(null, drawable, null, null);
        }

        return view;
    }

    class ViewHolder {

        private TextView menu_music;

    }
}

