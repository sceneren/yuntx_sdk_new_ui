package com.yuntongxun.ecdemo.photopicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.photopicker.model.Photo;
import com.yuntongxun.ecdemo.photopicker.utils.Platform;
import com.yuntongxun.ecdemo.photopicker.widgets.CheckView;
import com.yuntongxun.ecdemo.ui.BaseActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by zlk on 2017/8/7.
 */

public class SelectedPreviewActivity extends BaseActivity {
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.button_back)
    TextView buttonBack;
    @BindView(R.id.button_apply)
    TextView buttonApply;
    @BindView(R.id.bottom_toolbar)
    FrameLayout bottomToolbar;
    @BindView(R.id.check_view)
    CheckView checkView;

    public static final String EXTRA_DEFAULT_BUNDLE = "extra_default_bundle";
    public static final String EXTRA_RESULT_BUNDLE = "extra_result_bundle";
    public static final String EXTRA_RESULT_APPLY = "extra_result_apply";

    private PreviewPagerAdapter adapter;
    // //存放已选中的Photo数据
    private ArrayList<Photo> selected;

    @Override
    protected void initView(Bundle savedInstanceState) {

        if (Platform.hasKitKat()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        Bundle bundle = getIntent().getExtras();
        selected = bundle.getParcelableArrayList(EXTRA_DEFAULT_BUNDLE);
        adapter = new PreviewPagerAdapter(getSupportFragmentManager(), selected);
        pager.setAdapter(adapter);

        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Photo item = adapter.getMediaItem(position);
                int checkedNum = checkedNumOf(item);
                checkView.setCheckedNum(checkedNum);
                if (checkedNum > 0) {
                    checkView.setEnabled(true);
                } else {
                    checkView.setEnabled(PhotoAdapter.mMaxNum == selected.size());
                }
            }
        });

        checkView.setCountable(true);
        checkView.setCheckedNum(1);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_media_preview;
    }

    @Override
    protected void initWidgetAciotns() {

        checkView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Photo item = adapter.getMediaItem(pager.getCurrentItem());
                if (selected.contains(item)) {
                    selected.remove(item);
                    checkView.setCheckedNum(CheckView.UNCHECKED);
                } else {
                    if (selected.size() < PhotoAdapter.mMaxNum) {
                        selected.add(item);

                        checkView.setCheckedNum(checkedNumOf(item));
                    }
                }
                updateApplyButton();
            }
        });
        updateApplyButton();
    }


    @OnClick({R.id.button_back, R.id.button_apply, R.id.check_view})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                onBackPressed();
                break;
            case R.id.button_apply:
                sendBackResult(true);
                finish();
                break;
            case R.id.check_view:
                break;
        }
    }
    protected void sendBackResult(boolean apply) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_DEFAULT_BUNDLE, selected);
        intent.putExtra(EXTRA_RESULT_APPLY, apply);
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
    }


    @Override
    public void onBackPressed() {
        sendBackResult(false);
        super.onBackPressed();
    }


    public int checkedNumOf(Photo item) {
        int index = selected.indexOf(item);
        return index == -1 ? CheckView.UNCHECKED : index + 1;
    }


    private void updateApplyButton() {
        int selectedCount = selected.size();
        if (selectedCount == 0) {
            buttonApply.setText(R.string.send_photo_default);
            buttonApply.setEnabled(false);
        } else {
            buttonApply.setEnabled(true);
            buttonApply.setText(getString(R.string.send_photo_apply, selectedCount));
        }
    }


}
