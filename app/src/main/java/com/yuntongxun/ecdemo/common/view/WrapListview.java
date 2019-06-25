package com.yuntongxun.ecdemo.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by smileklvens on 2017/8/30.
 */

public class WrapListview extends ListView {


    public WrapListview(Context context) {
        super(context);
    }

    public WrapListview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
