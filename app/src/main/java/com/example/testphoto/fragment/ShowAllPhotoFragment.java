package com.example.testphoto.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.testphoto.GalleryActivity;
import com.example.testphoto.R;
import com.example.testphoto.adapter.PhotoWallAdapter;
import com.example.testphoto.model.ConfirmLocalFileInf;
import com.example.testphoto.model.MySparseBooleanArray;
import com.example.testphoto.util.FileUtils;
import com.example.testphoto.util.GetLocalFile;

import java.util.ArrayList;

/**
 * 图片选择界面。 最近的照片
 * 
 * @author zqh-pc
 * 
 */
public class ShowAllPhotoFragment extends Fragment implements OnClickListener,
		ConfirmLocalFileInf {
	private GridView local_gv;// 显示照片
	private LinearLayout choicepic_ly;// 底部选中图片弹出布局
	private CheckBox original_cb;// 原图
	private CheckBox compression_cb;// 压缩图
	private TextView original_title;// 超级短信

	private ArrayList<String> imagePathList;// 照片路径集合
	private PhotoWallAdapter adapter;

	private static final int PREVIEWRESULT = 0x000002;// 预览图片的回调
	private static final int TAKE_PICTURE = 0x000001;// 调用相机的回调。
	/**
	 * 当前文件夹路径
	 */
	private String currentFolder = null;
	/**
	 * 当前展示的是否为最近照片
	 */
	private boolean isLatest = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_show_photo, container,
				false);
		initView(view);
		initData();
		return view;
	}

	private void initView(View view) {
		original_cb = (CheckBox) view.findViewById(R.id.original_cb);
		compression_cb = (CheckBox) view.findViewById(R.id.compression_cb);
		choicepic_ly = (LinearLayout) view.findViewById(R.id.choicepic_ly);
		local_gv = (GridView) view.findViewById(R.id.local_gv);
		original_title = (TextView) view.findViewById(R.id.original_title);
		SpannableString msp = new SpannableString(original_title.getText()
				.toString());
		//字体颜色
		msp.setSpan(new ForegroundColorSpan(Color.CYAN), 3, 7,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置前景色为洋红色
		// 设置下划线
		msp.setSpan(new UnderlineSpan(), 3, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		original_title.setText(msp);
		local_gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String name = (String) parent.getItemAtPosition(position);
				if (name.equals("Camera")) {// 系统相机引导
					getCameraPhoto();
				} else {// 预览

					Intent intent2 = new Intent(getActivity(),
							GalleryActivity.class);
					intent2.putStringArrayListExtra("pathlist", imagePathList);
					intent2.putExtra("selection", position);
					intent2.putExtra("Type","nomalAhoto");
					startActivityForResult(intent2, PREVIEWRESULT);
				}

			}
		});

		compression_cb
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
												 boolean isChecked) {
						if (isChecked) {
							original_cb.setChecked(false);
						} else if (!original_cb.isChecked()) {
							compression_cb.setChecked(true);
						}

					}
				});
		original_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				if (isChecked) {
					compression_cb.setChecked(false);
				} else if (!compression_cb.isChecked()) {
					original_cb.setChecked(true);
				}
			}
		});
	}

	private void initData() {
		imagePathList = new ArrayList<>();
		imagePathList.add("Camera");
		imagePathList.addAll(GetLocalFile.getLatestImagePaths(getActivity()));
		adapter = new PhotoWallAdapter(getActivity(), this, imagePathList);
		local_gv.setAdapter(adapter);

		setConfirmEnable();
	}

	/**
	 * 第一次跳转至相册页面时，传递最新照片信息
	 */
	private boolean firstIn = true;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default:
			break;
		}

	}

	public void onNewIntent(int code, String folderPath) {
		// 动画
		getActivity().overridePendingTransition(R.anim.in_from_right,
				R.anim.out_from_left);

		if (code == 100) {
			// 某个相册
			if (isLatest
					|| (folderPath != null && !folderPath.equals(currentFolder))) {
				currentFolder = folderPath;
				updateView(100, currentFolder);
				isLatest = false;
			}
		} else if (code == 200) {
			// “全部照片”
			if (!isLatest) {
				updateView(200, null);
				isLatest = true;
			}
		}
	}

	/**
	 * 根据图片所属文件夹路径，刷新页面
	 */
	private void updateView(int code, String folderPath) {
		imagePathList.clear();
		MySparseBooleanArray.clearSelectionMap();
		adapter.notifyDataSetChanged();
		setConfirmEnable();

		if (code == 100) { // 某个相册
			imagePathList.addAll(GetLocalFile
					.getAllImagePathsByFolder(folderPath));
		} else if (code == 200) { // 最近照片
			imagePathList.add("Camera");
			imagePathList.addAll(GetLocalFile
					.getLatestImagePaths(getActivity()));
		}

		adapter.notifyDataSetChanged();
		if (imagePathList.size() > 0) {
			// 滚动至顶部
			local_gv.smoothScrollToPosition(0);
		}
	}

	// 获取已选择的图片路径
	public ArrayList<String> getSelectImagePaths() {
		// SparseBooleanArray map = adapter.getSelectionMap();
		SparseBooleanArray map = MySparseBooleanArray.getSelectionMap();
		if (map.size() == 0) {
			return null;
		}
		ArrayList<String> selectedImageList = new ArrayList<String>();

		for (int i = 0; i < imagePathList.size(); i++) {
			if (map.get(i)) {
				selectedImageList.add(imagePathList.get(i));
			}
		}
		return selectedImageList;
	}

	/**
	 * 确认选中返回
	 * @param paths 选中的地址集合(目前只用单选)
	 */
	private void comfirmPhoto(ArrayList<String> paths) {
		Intent intent = new Intent(getActivity(), MsgFragmentActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("code", paths != null ? 100 : -1);
		intent.putStringArrayListExtra("paths", paths);
		getActivity().setResult(getActivity().RESULT_OK,intent);
		MySparseBooleanArray.clearSelectionMap();
		getActivity().finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != getActivity().RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case TAKE_PICTURE:// 相机回调
			// 直接调用 项目现在的那个方法。
			// 这里这样做只是为了展示
			String fileName = String.valueOf(System.currentTimeMillis());
			Bitmap bm = (Bitmap) data.getExtras().get("data");
			String paths = FileUtils.saveBitmap(bm, fileName);
			if (!TextUtils.isEmpty(paths)) {
				ArrayList<String> arrayList = new ArrayList<>();
				arrayList.add(paths);
				comfirmPhoto(arrayList);
			}
			break;
		case PREVIEWRESULT:// 预览回调
			if(data!=null){
				getActivity().setResult(getActivity().RESULT_OK,data);
				getActivity().finish();
			}else{
				adapter.notifyDataSetChanged();
			}

			break;

		default:
			break;
		}
	}

	private void getCameraPhoto() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(openCameraIntent, TAKE_PICTURE);
	}

	/**
	 * 设置确定按钮是否可点击
	 */
	public void setConfirmEnable() {
		ArrayList<String> lists = getSelectImagePaths();
		int size = 0;
		if (lists != null) {
			size = lists.size();
		}

		((AlbumActivity) getActivity()).setConfirmBt(size);
	}

	@Override
	public void confirmLoacl() {
		comfirmPhoto(getSelectImagePaths());
	}

	public boolean isHaveChoice() {
		SparseBooleanArray map = MySparseBooleanArray.getSelectionMap();
		int size = imagePathList.size();
		if (size == 0) {
			return false;
		}

		for (int i = 0; i < size; i++) {
			if (map.get(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 显示底部布局
	 */
	public void showButtonLy() {
		if (choicepic_ly.getVisibility() == View.GONE) {
			compression_cb.setChecked(true);// 默认都是选择压缩图片
			choicepic_ly.setVisibility(View.VISIBLE);
			choicepic_ly.startAnimation(AnimationUtils.loadAnimation(
					getActivity(), R.anim.activity_translate_in));
		}
	}

	/**
	 * 隐藏底部布局
	 */
	public void hidButtonLy() {
		if (choicepic_ly.getVisibility() == View.VISIBLE) {
			choicepic_ly.setVisibility(View.GONE);
			choicepic_ly.startAnimation(AnimationUtils.loadAnimation(
					getActivity(), R.anim.activity_translate_out));
		}
	}

}
