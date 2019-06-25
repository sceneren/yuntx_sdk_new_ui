package com.yuntongxun.ecdemo.common.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;

import butterknife.BindView;

/**
 * Created by zlk on 2017/7/28.
 */

public class GroupSetItemView extends LinearLayout {
    @BindView(R.id.tv_tag)
    TextView tvTag;
    @BindView(R.id.tv_value)
    TextView tvValue;
    @BindView(R.id.ll_root)
    LinearLayout llRoot;

    public GroupSetItemView(Context context) {
        super(context);
        intView();
    }

    public GroupSetItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        intView();
    }

    public GroupSetItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intView();
    }

    private void intView() {
        inflate(getContext(), R.layout.group_setitem_view, null);
    }

    public void setValues(String tagStr,String valueStr){
        if (tvTag!=null&& !TextUtils.isEmpty(tagStr)){
            tvTag.setText(tagStr);
        }
        if (tvValue!=null&& !TextUtils.isEmpty(valueStr)){
            tvValue.setText(valueStr);
        }
    }


    public  void setOnItemClick(View.OnClickListener onclickListener){
        llRoot.setOnClickListener(onclickListener);
    }
}
