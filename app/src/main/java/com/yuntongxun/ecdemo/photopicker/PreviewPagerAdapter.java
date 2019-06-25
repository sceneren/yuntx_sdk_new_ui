package com.yuntongxun.ecdemo.photopicker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yuntongxun.ecdemo.photopicker.model.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlk on 2017/8/7.
 */

public class PreviewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Photo> mItems = new ArrayList<Photo>();


    public PreviewPagerAdapter(FragmentManager manager,List<Photo> items) {
        super(manager);
        mItems.addAll(items);
    }

    @Override
    public Fragment getItem(int position) {
        return PreviewItemFragment.newInstance(mItems.get(position));
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    public Photo getMediaItem(int position) {
        return mItems.get(position);
    }

}
