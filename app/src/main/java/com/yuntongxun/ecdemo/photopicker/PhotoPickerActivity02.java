package com.yuntongxun.ecdemo.photopicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.photopicker.model.Photo;
import com.yuntongxun.ecdemo.photopicker.model.PhotoDirectory;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;



/**
 * Created by zlk on 2017/8/5.
 */

public class PhotoPickerActivity02 extends BaseActivity implements PhotoAdapter.PhotoClickCallBack {
    public static String EXTRA_ALBUM = "extra_album";
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.photo_gridview)
    GridView mGridView;
    @BindView(R.id.button_preview)
    TextView buttonPreview;
    @BindView(R.id.cb)
    CheckBox cb;
    @BindView(R.id.tv_size)
    TextView tvSize;
    @BindView(R.id.btn_send)
    TextView btnSend;


    private PhotoAdapter mPhotoAdapter;


    //已选中的图片
    private ArrayList<Photo> mSelectList = new ArrayList<Photo>();

    public final static String TAG = "PhotoPickerActivity";

    private static final int REQUEST_CODE_PREVIEW = 23;

    public static final String EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";

    /**
     * 单选
     */
    public final static int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public final static int MODE_MULTI = 1;
    /**
     * 默认最大选择数量
     */
    public final static int DEFAULT_NUM = 9;
    private PhotoDirectory photoDir;
    private final static String KEY_PHOTOS_RESULT = "key_photos_result";


    @Override
    protected void initView(Bundle savedInstanceState) {
        photoDir = getIntent().getExtras().getParcelable(EXTRA_ALBUM);

        initTooleBar(titleBar, true, photoDir.getName());

        mPhotoAdapter = new PhotoAdapter(mContext);
        mPhotoAdapter.setIsShowCamera(false);
        mPhotoAdapter.setMaxNum(DEFAULT_NUM);
        mPhotoAdapter.setSelectMode(MODE_MULTI);
        mPhotoAdapter.setPhotoClickCallBack(this);

        mGridView.setAdapter(mPhotoAdapter);


    }

    @Override
    protected int getLayoutId() {
        return R.layout.picker_activity_photo_picker02;
    }

    @Override
    protected void initWidgetAciotns() {

        List<Photo> albumPhotos = photoDir.getPhotos();
        if (albumPhotos != null && albumPhotos.size() > 0) {
            mPhotoAdapter.setData(albumPhotos);
        }

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvSize.setVisibility(View.VISIBLE);
                    tvSize.setText(getPhotoSize(mSelectList));
                } else {
                    tvSize.setVisibility(View.GONE);
                }
            }
        });

        updateBottomToolbar();
    }

    private void updateBottomToolbar() {
        int selectedCount = mSelectList.size();
        if (selectedCount == 0) {
            buttonPreview.setEnabled(false);
            btnSend.setEnabled(false);
            cb.setEnabled(false);
            btnSend.setText(getString(R.string.send_photo_default));
        } else {
            cb.setEnabled(true);
            buttonPreview.setEnabled(true);
            btnSend.setEnabled(true);
            btnSend.setText(getString(R.string.send_photo_default));
        }

        if (cb.isChecked()) {
            tvSize.setVisibility(View.VISIBLE);
            tvSize.setText(getPhotoSize(mSelectList));
        } else {
            tvSize.setVisibility(View.GONE);
        }

    }

    //adapter回调选中图片
    @Override
    public void onPhotoClick(List<Photo> mSelectedPhotos) {
        mSelectList = (ArrayList) mSelectedPhotos;
        if (mSelectList.size() > 0) {
            updateBottomToolbar();
        }
    }


    @OnClick({R.id.button_preview, R.id.cb, R.id.btn_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_preview:
                Intent intent = new Intent(this, SelectedPreviewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(SelectedPreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectList);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_CODE_PREVIEW);
                break;

            case R.id.btn_send:
                if (mSelectList.size() == 0) {
                    ToastUtil.showMessage("请先选择图片");
                    return;
                }
                GalleryUtil.resultData(mSelectList,cb.isChecked());
//                returnData();
                finish();
                break;
        }
    }


    public String getPhotoSize(ArrayList<Photo> mSelectList) {

        if (mSelectList.size() == 0) {
            return 0 + "kb";
        } else {
            long size = 0;
            for (int i = 0; i < mSelectList.size(); i++) {
                size += new File(mSelectList.get(i).getPath()).length();
            }
            return "("+getFormatSize(size)+")";
        }
    }


    /**
     * 格式化单位
     *
     * @param size size
     * @return size
     */
    private static String getFormatSize(double size) {

        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);

        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

    /**
     * 返回选择图片的路径
     */
    private void returnData() {
        // 返回已选择的图片数据
        Intent data = new Intent();
        data.putParcelableArrayListExtra(KEY_PHOTOS_RESULT, mSelectList);
        setResult(RESULT_OK, data);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_PREVIEW) {
            Bundle resultBundle = data.getExtras();
            ArrayList<Photo> selected = resultBundle.getParcelableArrayList(SelectedPreviewActivity.EXTRA_DEFAULT_BUNDLE);
            if (data.getBooleanExtra(SelectedPreviewActivity.EXTRA_RESULT_APPLY, false)) {
                GalleryUtil.resultData(selected, cb.isChecked());
                finish();
            } else {
                //预览后回调选中图片
                this.mSelectList = selected;
                mPhotoAdapter.setSelectedPhotos(mSelectList);
                mPhotoAdapter.notifyDataSetChanged();
                updateBottomToolbar();
            }
        }
    }
}
