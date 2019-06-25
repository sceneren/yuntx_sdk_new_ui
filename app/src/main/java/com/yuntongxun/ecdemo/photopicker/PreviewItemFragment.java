package com.yuntongxun.ecdemo.photopicker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.photopicker.model.Photo;


public class PreviewItemFragment extends Fragment {

    private static final String ARGS_ITEM = "args_item";

    public static PreviewItemFragment newInstance(Photo item) {
        PreviewItemFragment fragment = new PreviewItemFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_ITEM, item);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Photo item = getArguments().getParcelable(ARGS_ITEM);
        if (item == null) {
            return;
        }
        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
        Glide.with(this)
                .load(item.getPath())
                .asBitmap()
                .centerCrop()
                .into(imageView);
    }


}
