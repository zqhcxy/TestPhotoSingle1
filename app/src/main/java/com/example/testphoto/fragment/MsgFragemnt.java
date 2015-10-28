package com.example.testphoto.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testphoto.GalleryActivity;
import com.example.testphoto.R;
import com.example.testphoto.adapter.MyMenuAdapter;
import com.example.testphoto.adapter.MyPageViewAdapter;
import com.example.testphoto.model.MyAudio;
import com.example.testphoto.util.DensityUtil;
import com.example.testphoto.util.MyMediaPlayerContral;
import com.example.testphoto.util.ScreenUtils;
import com.example.testphoto.views.MyMediaPlayerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MsgFragemnt extends Fragment implements OnClickListener {


    private static final int PHOTO_TYPE = 100;
    private static final int AUDIO_TYPE = 101;
    private static final int VIDEO_TYPE = 102;
    //分别是截图与录制视频的返回，各不需要用glide进行获取缩略图
    private static final int PHOTO_TYPE_CUT = 103;
    private static final int MSG_EDITOR_TEXT_UPDATE = 10001;
    //    private static final int VIDEO_SYS_TYPE = 104;
    //是否需要跳转界面
    private static final String CHANGGEVIEW = "changeview";

    private int koCounter = 80;//字数限制


    //    private Button localphoto_bt;
//    private Button video_bt;// 本地视频
//    private Button audio_bt;// 本地音频
    public EditText msg_edit;//编辑框
    private TextView msgactivity_limit_tv;//字数显示

    private LinearLayout media_show_ly;//显示选中的多媒体文件布局
    private ImageView media_thumbnail_iv;//选中的多媒体文件缩略图


    private TextView media_title_tv;//选中文件的标题
    private ImageView media_clear_iv;// 清除选中的文件
    private ImageView menu_iv;//打开底部菜单
    private ImageView full_edit_iv;//全屏编辑
    private ImageView video_iv;//录音按钮(发送消息按钮即SM1)
    private MyMediaPlayerView audio_thumbnail;//音频播放视图
    private LinearLayout button_menu_ly;//底部菜单
    private ViewPager msg_menu_vp;


    private MyMediaPlayerContral myMediaPlayerContral;//音频播放控制器

    //选择的多媒体数据
    public int showType = -1;// 当前显示的是哪个多媒体类型的文件
    public String mMediaPath;//选中的多媒体文件地址
    public MyAudio myAudio;
    public boolean compression;//是否发送原图
    public String mEditPhoto;//编辑过的图片，显示完要删除

    private int num = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        ScreenUtils.initScreen(getActivity());
        initView(view);
        mOnlistion();
        return view;
    }


    private void initView(View view) {

//        LinearLayout menu_top=(LinearLayout)view.findViewById(R.id.menu_top);
//        menu_top.setAlpha(0.66f);

//        localphoto_bt = (Button) view.findViewById(R.id.localphoto_bt);
//        video_bt = (Button) view.findViewById(R.id.video_bt);
//        audio_bt = (Button) view.findViewById(R.id.audio_bt);
        msg_edit = (EditText) view.findViewById(R.id.msg_edit);
        msgactivity_limit_tv = (TextView) view.findViewById(R.id.msgactivity_limit_tv);

        media_show_ly = (LinearLayout) view.findViewById(R.id.media_show_ly);
        media_thumbnail_iv = (ImageView) view.findViewById(R.id.media_thumbnail_iv);

        media_title_tv = (TextView) view.findViewById(R.id.media_title_tv);
        media_clear_iv = (ImageView) view.findViewById(R.id.media_clear_iv);
        audio_thumbnail = (MyMediaPlayerView) view.findViewById(R.id.audio_thumbnail);
        button_menu_ly = (LinearLayout) view.findViewById(R.id.button_menu_ly);
        menu_iv = (ImageView) view.findViewById(R.id.menu_iv);
        full_edit_iv = (ImageView) view.findViewById(R.id.full_edit_iv);
        video_iv = (ImageView) view.findViewById(R.id.video_iv);
        msg_menu_vp = (ViewPager) view.findViewById(R.id.msg_menu_vp);


        media_title_tv.setFocusable(true);
        media_title_tv.setFocusableInTouchMode(true);
        media_title_tv.requestFocus();
        media_title_tv.requestFocusFromTouch();
        audio_thumbnail.setImageViewWH(40, 15);

//        localphoto_bt.setOnClickListener(this);
//        video_bt.setOnClickListener(this);
//        audio_bt.setOnClickListener(this);
        media_thumbnail_iv.setOnClickListener(this);
        media_clear_iv.setOnClickListener(this);
        audio_thumbnail.setOnClickListener(this);
        menu_iv.setOnClickListener(this);
        full_edit_iv.setOnClickListener(this);
        msg_edit.addTextChangedListener(textWatcher);
        initViewPage();
    }

    private void mOnlistion() {
        msg_edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                hidMenuly();

                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.localphoto_bt:
//                Intent intent = new Intent(getActivity(), AlbumActivity.class);
//                intent.putExtra("type", 1);
//                getActivity().startActivityForResult(intent, ((MsgFragmentActivity) getActivity()).PHOTO_REQUST);
//                hidMenuly();
//                break;
            case R.id.menu_iv://打开与关闭底部菜单
                showMenuIV(false);

                if (button_menu_ly.getVisibility() == View.VISIBLE) {
//                    button_menu_ly.startAnimation(AnimationUtils.loadAnimation(
//                            getActivity(), R.anim.activity_translate_out));
                    button_menu_ly.setVisibility(View.GONE);
                } else if (button_menu_ly.getVisibility() == View.GONE) {
                    showMenuIV(true);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(msg_edit.getWindowToken(), 0);
                    button_menu_ly.setVisibility(View.VISIBLE);
//                    button_menu_ly.startAnimation(AnimationUtils.loadAnimation(
//                            getActivity(), R.anim.activity_translate_in));
                }
                break;
//            case R.id.video_bt:
//                Intent intent2 = new Intent(getActivity(), AlbumActivity.class);
//                intent2.putExtra("type", 0);
//                getActivity().startActivityForResult(intent2, ((MsgFragmentActivity) getActivity()).VIDEO_REQUST);
//                hidMenuly();
//                break;
//            case R.id.audio_bt:
//                Intent intent3 = new Intent(getActivity(), AlbumActivity.class);
//                intent3.putExtra("type", 2);
//                getActivity().startActivityForResult(intent3, ((MsgFragmentActivity) getActivity()).AUDIO_REQUST);
//                hidMenuly();
//                break;
            case R.id.media_thumbnail_iv://点击缩略图
                if (showType == PHOTO_TYPE) {//图片的缩略图
                    Intent intent4 = new Intent(getActivity(),
                            GalleryActivity.class);
                    ArrayList<String> imagePathList = new ArrayList<>();// 照片路径集合
                    imagePathList.add(mMediaPath);
                    intent4.putStringArrayListExtra("pathlist", imagePathList);
                    intent4.putExtra("Type", "mainActivity");
                    intent4.putExtra("compression", compression);
                    getActivity().startActivityForResult(intent4, ((MsgFragmentActivity) getActivity()).PHOTO_REQUST);
                } else if (showType == VIDEO_TYPE) {//视频的缩略图
                    Intent intent5 = new Intent(Intent.ACTION_VIEW);
                    intent5.setDataAndType(Uri.parse(mMediaPath), "video/*");
                    startActivity(intent5);
                }
                break;
            case R.id.media_clear_iv://点击删除按钮
                setDistroyMediaData();
                break;
            case R.id.audio_thumbnail://播放音频
                if (myMediaPlayerContral == null) {
                    myMediaPlayerContral = new MyMediaPlayerContral();
                }
                myMediaPlayerContral.setMediaPlayerView(audio_thumbnail, myAudio.getPath(), 0);
                break;
            case R.id.full_edit_iv://全屏编辑
                hidMenuly();
                ((MsgFragmentActivity) getActivity()).stapOnclick(1);


                break;
            default:
                break;
        }

    }

    private void hidMenuly() {
        if (button_menu_ly.getVisibility() == View.VISIBLE) {
            button_menu_ly.setVisibility(View.GONE);
            showMenuIV(false);
        }
    }


    /**
     * 回调 形式
     * <p/>
     * <p></>注意编辑过的图片的，发送用的地址就为 mEditPhoto
     *
     * @param intent
     */
    public void onNewIntent(Intent intent) {
        int code = intent.getIntExtra("code", -1);
        if (code == -1) {
            return;
        }

        if (code == PHOTO_TYPE) {//图片(是否统一传地址，截取图片那现在是发送的内存bitmap)
            ArrayList<String> paths = intent.getStringArrayListExtra("paths");
            String pic_path = null;
            compression = intent.getBooleanExtra("compression", false);
            Toast.makeText(getActivity(), "是否发送原图" + compression, Toast.LENGTH_LONG).show();
            if (paths != null && paths.size() > 0)
                pic_path = paths.get(0);
            mEditPhoto = intent.getStringExtra("sendBitmap");
            setShowPhotoData(pic_path);
        } else if (code == AUDIO_TYPE) {// 音频
            myAudio = (MyAudio) intent.getSerializableExtra("MyAudio");
            setShowAudioData();

        } else if (code == VIDEO_TYPE) {// 视频
            ArrayList<String> paths = intent.getStringArrayListExtra("paths");
            String path = paths.get(0);
            setShowVideoData(path);
        }
    }


    /**
     * 显示选择的多媒体文件
     *
     * @param path
     * @param code
     */
    private void setMediaDataShow(String path, int code) {
        audio_thumbnail.setVisibility(View.GONE);
        media_thumbnail_iv.setVisibility(View.VISIBLE);
        File file = new File(path);
        String title = file.getName();
        media_show_ly.setVisibility(View.VISIBLE);
        media_title_tv.setText(title);


        ((MsgFragmentActivity) getActivity()).glidePhoto(code, mEditPhoto, file, media_thumbnail_iv);
    }

    /**
     * 清除多媒体数据
     */
    public void setDistroyMediaData() {
        media_show_ly.setVisibility(View.GONE);
        media_thumbnail_iv.setImageResource(R.drawable.empty_photo);
        media_title_tv.setText("");
        mMediaPath = null;
        myAudio = null;
        mEditPhoto = null;
        showType = -1;
        if (myMediaPlayerContral != null) {
            myMediaPlayerContral.stopPlay();
        }
    }


    public void setShowPhotoData(String pic_path) {
        showType = PHOTO_TYPE;
        mMediaPath = pic_path;
        if (mEditPhoto != null) {//是否是截取图片-
            setMediaDataShow(pic_path, PHOTO_TYPE_CUT);
        } else {//正常图片地址--传递的是地址
            setMediaDataShow(pic_path, PHOTO_TYPE);
        }

    }

    public void setShowAudioData() {
        showType = AUDIO_TYPE;
        mMediaPath = myAudio.getPath();
        setMediaDataShow(mMediaPath, AUDIO_TYPE);
        //如果这里不需要封面的话就不要下面这一段
        audio_thumbnail.initMediaData(myAudio.getAlbumid(), myAudio.getId());
        audio_thumbnail.setVisibility(View.VISIBLE);
        media_thumbnail_iv.setVisibility(View.GONE);

    }

    public void setShowVideoData(String path) {
        showType = VIDEO_TYPE;
        mMediaPath = path;
        setMediaDataShow(path, VIDEO_TYPE);

    }

    private final Handler mTextEditorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_EDITOR_TEXT_UPDATE:
                    updateCounter();
                    break;
            }
        }
    };


    /**
     * 消息编辑内容监听
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (charSequence.length() > 0) {
                video_iv.setImageResource(R.drawable.but_edit_send_normal);
            } else {
                video_iv.setImageResource(R.drawable.but_edit_send_voice_normal);
            }

            int[] params = SmsMessage.calculateLength(charSequence, false);

            int mMsgCount = params[0];
            int mMsgSize = params[1];

            int remainingInCurrentMessage = params[2];
            if ((mMsgCount == 1) && (mMsgSize == 0))
                remainingInCurrentMessage = 160;

            msgactivity_limit_tv.setText(remainingInCurrentMessage + "/"
                    + koCounter);

            mTextEditorHandler.sendEmptyMessageDelayed(MSG_EDITOR_TEXT_UPDATE,
                    100);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    /**
     * 编辑框输入文字判断
     */
    private void updateCounter() {
        if (msg_edit.getLineCount() <= 1) {//只有1行时
            if (msgactivity_limit_tv.getVisibility() == View.VISIBLE)
                msgactivity_limit_tv.setVisibility(View.GONE);
        } else if (msg_edit.getLineCount() < 4) {//大于1行小于四行
            if (msgactivity_limit_tv.getVisibility() == View.GONE)
                msgactivity_limit_tv.setVisibility(View.VISIBLE);
        } else {//四行
            msgactivity_limit_tv.setText("双卡");
//            msgactivity_limit_tv.setBackgroundResource(R.drawable.but_edit_send_cards_normal);
        }

    }

    /**
     * 底部菜单按钮的变化
     *
     * @param show
     */
    private void showMenuIV(boolean show) {
        if (show) {
            menu_iv.setImageResource(R.drawable.menu_selector_true);
        } else {
            menu_iv.setImageResource(R.drawable.menu_selector);
        }

    }

    /**
     * 底部菜单初始化
     */
    public void initViewPage() {
        int lie = 8;
        int row = 2;
        int lie_count = 4;
        int LANDSCAPE = 1001;
        int PORTRAIT = 1002;
        int nowwindow = PORTRAIT;
        int height_hl = 0;

        List views = new ArrayList<>();
        List<String> menulist = new ArrayList<>();
        menulist.add("图片");
        menulist.add("音频");
        menulist.add("名片");
        menulist.add("位置");
        menulist.add("语音");
        menulist.add("收藏");
        menulist.add("视频");
        menulist.add("广告");
        menulist.add("涂鸦");
        menulist.add("拼写检查");
        menulist.add("添加主题");
        menulist.add("幻灯片");
        menulist.add("电子贺卡");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 217);
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            lie = 8;
            row = 2;
            lie_count = 4;
            nowwindow = PORTRAIT;
            height_hl = DensityUtil.dip2px(getActivity(), 217);

        } else if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            nowwindow = LANDSCAPE;
            lie = 5;
            lie_count = 5;
            row = 1;
            height_hl = DensityUtil.dip2px(getActivity(), 137);

        }
        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height_hl);
        button_menu_ly.setLayoutParams(layoutParams);
        int size = menulist.size();
        int fram = size / lie + (size % lie == 0 ? 0 : 1);// 有几页
        for (int i = 1; i <= fram; i++) {
            GridView gridView = setGridView(size, row, lie_count);
            MyMenuAdapter menuAdapter = new MyMenuAdapter(getActivity(), menulist, i, nowwindow);
            gridView.setAdapter(menuAdapter);
            views.add(gridView);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String title = (String) adapterView.getItemAtPosition(i);
                    switch (title) {
                        case "图片":
                            Intent intent = new Intent(getActivity(), AlbumActivity.class);
                            intent.putExtra("type", 1);
                            getActivity().startActivityForResult(intent, ((MsgFragmentActivity) getActivity()).PHOTO_REQUST);
                            hidMenuly();
                            break;
                        case "音频":
                            Intent intent3 = new Intent(getActivity(), AlbumActivity.class);
                            intent3.putExtra("type", 2);
                            getActivity().startActivityForResult(intent3, ((MsgFragmentActivity) getActivity()).AUDIO_REQUST);
                            hidMenuly();
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
                            Intent intent2 = new Intent(getActivity(), AlbumActivity.class);
                            intent2.putExtra("type", 0);
                            getActivity().startActivityForResult(intent2, ((MsgFragmentActivity) getActivity()).VIDEO_REQUST);
                            hidMenuly();
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
                }
            });
        }
        MyPageViewAdapter myPageViewAdapter = new MyPageViewAdapter(views);
        msg_menu_vp.setAdapter(myPageViewAdapter);

    }

    /**
     * 设置GirdView参数，绑定数据
     * <p/>
     * <p/>
     * size 当前页的表情数量
     * <p/>
     * row_count 一页表情的行数
     */
    private GridView setGridView(int size, int row_count, int lie) {
        ScreenUtils.initScreen(getActivity());
        int width_iv = (ScreenUtils.getScreenW() - 45) / lie;
        int columncount = size / row_count + (size % row_count == 0 ? 0 : 1);
        if (columncount == 0) {
            columncount = 1;
        }

        // grideview的宽度由列数*没咧宽度+间隔
//        int gridviewWidth = (int) (columncount * (width_iv + 9) + 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        GridView gridView = new GridView(getActivity());
        gridView.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
        gridView.setColumnWidth(width_iv); // 设置列表项宽
        gridView.setHorizontalSpacing(9); // 设置列表项水平间距
        gridView.setVerticalSpacing(39); // 设置列表项水平间距
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setNumColumns(lie); // 设置列数量=列表集合数
        return gridView;

    }


    @Override
    public void onPause() {
        super.onPause();
        if (myMediaPlayerContral != null) {
            myMediaPlayerContral.stopPlay();
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
