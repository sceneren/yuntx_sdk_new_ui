package com.yuntongxun.ecdemo.photopicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.photopicker.model.PhotoDirectory;
import com.yuntongxun.ecdemo.photopicker.utils.MediaStoreHelper;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import java.util.List;

import butterknife.BindView;

/**
 * Created by zlk on 2017/8/5.
 */

public class PhotoDirectoryPickerActivity extends BaseActivity {
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.listview_floder)
    ListView mFloderListView;

    //图片文件夹集合
    public List<PhotoDirectory> dirs;
    private FolderAdapter adapter;

    @Override
    protected void initView(Bundle savedInstanceState) {

        initTooleBar(titleBar, false, "照片");
        titleBar.setMySettingText("取消").setSettingTextOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        adapter = new FolderAdapter(PhotoDirectoryPickerActivity.this);
        mFloderListView.setAdapter(adapter);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.picker_fload_list_layout_stub;
    }

    @Override
    protected void initWidgetAciotns() {

        MediaStoreHelper.getPhotoDirs(this, new Bundle(),
                new MediaStoreHelper.PhotosResultCallback() {
                    @Override
                    public void onResultCallback(List<PhotoDirectory> dirs) {
                        PhotoDirectoryPickerActivity.this.dirs = dirs;
                        adapter.setData(dirs);
                    }
                });


        mFloderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dirs == null){
                    return;
                }

                PhotoDirectory folder = dirs.get(position);

                if (folder == null){
                    return;
                }
                Intent intent = new Intent(mContext, PhotoPickerActivity02.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(PhotoPickerActivity02.EXTRA_ALBUM,folder);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.e("dire" + "onActivityResult);");
    }
}
