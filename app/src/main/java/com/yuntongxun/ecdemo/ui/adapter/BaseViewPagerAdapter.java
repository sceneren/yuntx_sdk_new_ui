package com.yuntongxun.ecdemo.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.List;

public class BaseViewPagerAdapter extends PagerAdapter {

    private List<View> mViews;
    private Context mContext;


    public BaseViewPagerAdapter(List<View> views, Context context) {

        this.mViews = views;
        this.mContext = context;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {

        ((ViewPager) container).removeView(mViews.get(position));
    }

    @Override
    public Object instantiateItem(View container, int position) {

        ((ViewPager) container).addView(mViews.get(position));

        return mViews.get(position);

    }

    @Override
    public int getCount() {
        if (mViews != null) {
            return mViews.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

}
