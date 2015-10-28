package com.example.testphoto.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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

/**
 * 全屏编辑模式界面
 */
public class FullEditFragment extends Fragment implements View.OnClickListener {
    private static final int PHOTO_TYPE = 100;
    private static final int AUDIO_TYPE = 101;
    private static final int VIDEO_TYPE = 102;
    private static final int PHOTO_TYPE_CUT = 103;
    //    private static final int VIDEO_SYS_TYPE = 104;

    private static final int LANDSCAPE = 1001;
    private static final int PORTRAIT = 1002;
    private int nowwindow = PORTRAIT;
    //是否需要跳转界面
    private static final String CHANGGEVIEW = "changeview";
    private int showType = -1;// 当前显示的是哪个多媒体类型的文件


    private ImageView fulledit_addmenu;//其他功能
    private ImageView fulledit_localphoto;//图片
    //    private Button fulledit_audio_bt;//音频
//    private Button fulledit_video_bt;//视频
    public EditText fulledit_msg_et;//消息编辑框

    private LinearLayout fulledit_mediashow_ly;//显示选择的多媒体文件布局
    private LinearLayout fulledit_buttonmenu_ly;//底部其他功能布局
    private ImageView fulledit_thumbnail_iv;//多媒体的缩略图（视频、图片）
    private MyMediaPlayerView fulledit_audio_thumbnail;//音频显示控件（带播放功能）
    private TextView fulledit_title_tv;//多媒体文件的名字
    private ImageView fulledit_clear_iv;//删除选择的多媒体文件
    private TextView fulledit_limit_tv;//可输入的字数
    private ViewPager full_menu_vp;//底部菜单


    private String mMediaPath;//选中的多媒体文件地址
    private MyMediaPlayerContral myMediaPlayerContral;//音频播放控制器
    private MsgFragemnt msgFragemnt;
    private boolean compression;
    private List<View> views;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.full_edit_activity, container, false);
        ScreenUtils.initScreen(getActivity());
        initView(view);
        initData(((MsgFragmentActivity) getActivity()).msgFragemnt);
        mOnlistion();
        return view;
    }

    private void initView(View view) {
        fulledit_addmenu = (ImageView) view.findViewById(R.id.fulledit_addmenu);
        fulledit_localphoto = (ImageView) view.findViewById(R.id.fulledit_localphoto);
//        fulledit_audio_bt = (Button) view.findViewById(R.id.fulledit_audio_bt);
//        fulledit_video_bt = (Button) view.findViewById(R.id.fulledit_video_bt);
        fulledit_msg_et = (EditText) view.findViewById(R.id.fulledit_msg_et);
        fulledit_limit_tv = (TextView) view.findViewById(R.id.fulledit_limit_tv);
        full_menu_vp = (ViewPager) view.findViewById(R.id.full_menu_vp);


        fulledit_mediashow_ly = (LinearLayout) view.findViewById(R.id.fulledit_mediashow_ly);
        fulledit_thumbnail_iv = (ImageView) view.findViewById(R.id.fulledit_thumbnail_iv);
        fulledit_audio_thumbnail = (MyMediaPlayerView) view.findViewById(R.id.fulledit_audio_thumbnail);
        fulledit_title_tv = (TextView) view.findViewById(R.id.fulledit_title_tv);
        fulledit_clear_iv = (ImageView) view.findViewById(R.id.fulledit_clear_iv);
        fulledit_buttonmenu_ly = (LinearLayout) view.findViewById(R.id.fulledit_buttonmenu_ly);
        fulledit_audio_thumbnail.setImageViewWH(40, 15);

        fulledit_localphoto.setOnClickListener(this);
        fulledit_addmenu.setOnClickListener(this);
        fulledit_clear_iv.setOnClickListener(this);
        fulledit_audio_thumbnail.setOnClickListener(this);
        fulledit_thumbnail_iv.setOnClickListener(this);
//        fulledit_audio_bt.setOnClickListener(this);
//        fulledit_video_bt.setOnClickListener(this);
        fulledit_msg_et.addTextChangedListener(textWatcher);
        initViewPage();
    }

    /**
     * 底部菜单初始化
     */
    public void initViewPage() {
        int lie = 8;
        int row = 2;
        int lie_count = 4;
        int height_hl = 0;

        views = new ArrayList<>();
        List<String> menulist = new ArrayList<>();
        menulist.add("音频");
        menulist.add("名片");
        menulist.add("位置");
        menulist.add("视频");
        menulist.add("广告");
        menulist.add("涂鸦");
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
        fulledit_buttonmenu_ly.setLayoutParams(layoutParams);

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
        full_menu_vp.setAdapter(myPageViewAdapter);

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


    /**
     * 读取主界面的数据
     *
     * @param msgFragemnt
     */
    public void initData(MsgFragemnt msgFragemnt) {
        if (this.msgFragemnt == null) {
            this.msgFragemnt = msgFragemnt;
        }
        String msg = msgFragemnt.msg_edit.getText().toString();
        fulledit_msg_et.setText(msg);
        switch (msgFragemnt.showType) {
            case PHOTO_TYPE:
                compression = msgFragemnt.compression;
                setShowPhotoData(msgFragemnt.mEditPhoto, msgFragemnt.mMediaPath);
                break;
            case AUDIO_TYPE:
                setShowAudioData(msgFragemnt.myAudio);
                break;
            case VIDEO_TYPE:
                setShowVideoData(msgFragemnt.mMediaPath);
                break;
            case -1:
                setDistroyMediaData();
                break;
            default:
                break;
        }

    }


    private void mOnlistion() {
        fulledit_msg_et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (fulledit_buttonmenu_ly.getVisibility() == View.VISIBLE) {
                    fulledit_buttonmenu_ly.setVisibility(View.GONE);
                }

                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fulledit_addmenu://其他功能
                if (fulledit_buttonmenu_ly.getVisibility() == View.VISIBLE) {
                    showMenuIV(false);
                    fulledit_buttonmenu_ly.setVisibility(View.GONE);
                } else if (fulledit_buttonmenu_ly.getVisibility() == View.GONE) {
                    showMenuIV(true);
                    fulledit_buttonmenu_ly.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(fulledit_msg_et.getWindowToken(), 0);
                }

                break;
            case R.id.fulledit_localphoto://添加本地图片
                Intent intent = new Intent(getActivity(), AlbumActivity.class);
                intent.putExtra("type", 1);
                getActivity().startActivityForResult(intent, ((MsgFragmentActivity) getActivity()).PHOTO_REQUST);
                hidMenuly();
                break;
//            case R.id.fulledit_audio_bt://音频
//                Intent intent3 = new Intent(getActivity(), AlbumActivity.class);
//                intent3.putExtra("type", 2);
//                getActivity().startActivityForResult(intent3, ((MsgFragmentActivity) getActivity()).AUDIO_REQUST);
//                hidMenuly();
//                break;
//            case R.id.fulledit_video_bt://视频
//                Intent intent2 = new Intent(getActivity(), AlbumActivity.class);
//                intent2.putExtra("type", 0);
//                getActivity().startActivityForResult(intent2, ((MsgFragmentActivity) getActivity()).VIDEO_REQUST);
//                hidMenuly();
//                break;
            case R.id.fulledit_clear_iv://删除选择文件
                setDistroyMediaData();
                msgFragemnt.setDistroyMediaData();//  删除主界面的数据
                break;
            case R.id.fulledit_audio_thumbnail://点击音频缩略图播放音频
                if (myMediaPlayerContral == null) {
                    myMediaPlayerContral = new MyMediaPlayerContral();
                }
                myMediaPlayerContral.setMediaPlayerView(fulledit_audio_thumbnail, mMediaPath, 0);
                break;
            case R.id.fulledit_thumbnail_iv://点击图片或视频
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
        }
    }


    /**
     * 回调 形式
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
            String mEditPhoto = intent.getStringExtra("sendBitmap");
            String pic_path = null;
            compression = intent.getBooleanExtra("compression", false);
            Toast.makeText(getActivity(), "是否发送原图" + compression, Toast.LENGTH_LONG).show();
            if (paths != null && paths.size() > 0)
                pic_path = paths.get(0);
            setShowPhotoData(mEditPhoto, pic_path);
            msgFragemnt.mEditPhoto = mEditPhoto;
            msgFragemnt.compression = this.compression;
            msgFragemnt.setShowPhotoData(pic_path);
        } else if (code == AUDIO_TYPE) {// 音频
            MyAudio myAudio = (MyAudio) intent.getSerializableExtra("MyAudio");
            setShowAudioData(myAudio);
            msgFragemnt.myAudio = myAudio;
            msgFragemnt.setShowAudioData();
        } else if (code == VIDEO_TYPE) {// 视频
            ArrayList<String> paths = intent.getStringArrayListExtra("paths");
            String path = paths.get(0);
            setShowVideoData(path);
            msgFragemnt.mMediaPath = path;
            msgFragemnt.setShowVideoData(path);
        }
    }

    /**
     * 显示选择的多媒体文件
     *
     * @param path
     * @param code
     */
    private void setMediaDataShow(String editphoto, String path, int code) {

        fulledit_audio_thumbnail.setVisibility(View.GONE);
        fulledit_thumbnail_iv.setVisibility(View.VISIBLE);
        File file = new File(path);
        String title = file.getName();
        fulledit_mediashow_ly.setVisibility(View.VISIBLE);
        fulledit_title_tv.setText(title);
        ((MsgFragmentActivity) getActivity()).glidePhoto(code, editphoto, file, fulledit_thumbnail_iv);
    }


    /**
     * 清除多媒体数据
     */
    public void setDistroyMediaData() {
        fulledit_mediashow_ly.setVisibility(View.GONE);
        fulledit_thumbnail_iv.setImageResource(R.drawable.empty_photo);
        fulledit_title_tv.setText("");
        mMediaPath = null;
        showType = -1;
        if (myMediaPlayerContral != null) {
            myMediaPlayerContral.stopPlay();
        }
    }

    public void hidMenuly() {
        if (fulledit_buttonmenu_ly.getVisibility() == View.VISIBLE) {
            showMenuIV(false);
            fulledit_buttonmenu_ly.setVisibility(View.GONE);
        }
    }

    private void showMenuly() {
        if (fulledit_buttonmenu_ly.getVisibility() == View.GONE) {
            fulledit_buttonmenu_ly.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示选择的图片数据
     *
     * @param editphoto
     * @param pic_path
     */
    private void setShowPhotoData(String editphoto, String pic_path) {
        showType = PHOTO_TYPE;
        mMediaPath = pic_path;
        if (editphoto != null) {//是否是截取图片--直接传递bitmap对象的
            setMediaDataShow(editphoto, pic_path, PHOTO_TYPE_CUT);
        } else {//正常图片地址--传递的是地址
            setMediaDataShow(editphoto, pic_path, PHOTO_TYPE);
        }

    }

    /**
     * 显示选中的音频数据
     *
     * @param myAudio
     */
    private void setShowAudioData(MyAudio myAudio) {
        showType = AUDIO_TYPE;
        mMediaPath = myAudio.getPath();
        setMediaDataShow(null, mMediaPath, AUDIO_TYPE);
        //如果这里不需要封面的话就不要下面这一段
        fulledit_audio_thumbnail.initMediaData(myAudio.getAlbumid(), myAudio.getId());
        fulledit_audio_thumbnail.setVisibility(View.VISIBLE);
        fulledit_thumbnail_iv.setVisibility(View.GONE);

    }

    /**
     * 显示的视频数据
     *
     * @param path
     */
    private void setShowVideoData(String path) {
        showType = VIDEO_TYPE;
        mMediaPath = path;
        setMediaDataShow(null, path, VIDEO_TYPE);

    }


    /**
     * 消息编辑内容监听
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (charSequence.length() > 0 && fulledit_limit_tv.getVisibility() == View.GONE) {
                fulledit_limit_tv.setVisibility(View.VISIBLE);
            } else if (charSequence.length() <= 0 && fulledit_limit_tv.getVisibility() == View.VISIBLE) {
                fulledit_limit_tv.setVisibility(View.GONE);
            }

//            int[] params = SmsMessage.calculateLength(charSequence, false);
//            int mMsgCount = params[0];
//            int mMsgSize = params[1];
//            int remainingInCurrentMessage = params[2];
//            if ((mMsgCount == 1) && (mMsgSize == 0))
//                remainingInCurrentMessage = 160;


            fulledit_limit_tv.setText(getActivity().getResources().getString(R.string.msg_limit));

//            mTextEditorHandler.sendEmptyMessageDelayed(MSG_EDITOR_TEXT_UPDATE,
//                    100);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    /**
     * 底部菜单按钮的变化
     *
     * @param show
     */
    private void showMenuIV(boolean show) {
        if (show) {
            fulledit_addmenu.setImageResource(R.drawable.full_menu_selector_true);
        } else {
            fulledit_addmenu.setImageResource(R.drawable.full_menu_selector);
        }

    }




    @Override
    public void onPause() {
        super.onPause();
        if (myMediaPlayerContral != null) {
            myMediaPlayerContral.stopPlay();
        }
    }
}
