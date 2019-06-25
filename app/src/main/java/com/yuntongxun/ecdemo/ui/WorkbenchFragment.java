package com.yuntongxun.ecdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.interphone.InterPhoneListActivity;
import com.yuntongxun.ecdemo.ui.livechatroom.ECLIveChatRoomListUI;
import com.yuntongxun.ecdemo.ui.meeting.MeetingListActivity;
import com.yuntongxun.ecdemo.ui.videomeeting.VideoconferenceConversation;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by zlk on 2017/7/24.
 */

public class WorkbenchFragment extends LazyFrament {

    @BindView(R.id.iv_workbench_banner)
    ImageView ivWorkbenchBanner;
    @BindView(R.id.tv_audio_video)
    TextView tvAudioVideo;
    @BindView(R.id.tv_video_conference)
    TextView tvVideoConference;
    @BindView(R.id.tv_poc_client)
    TextView tvPocClient;
    @BindView(R.id.tv_live)
    TextView tvLive;
    @BindView(R.id.tv_jiqiren)
    TextView tvJq;
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.ll_im)
    LinearLayout llIm;
    Unbinder unbinder;


    public static WorkbenchFragment newInstance() {
        WorkbenchFragment fragment = new WorkbenchFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_workbench;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        titleBar.setMyCenterTitle("发现");

        if(RestServerDefines.IM){
            llIm.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initWidgetActions() {

    }


    @OnClick({R.id.iv_workbench_banner, R.id.tv_audio_video, R.id.tv_video_conference, R.id.tv_poc_client, R.id.tv_live,R.id.tv_jiqiren})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_workbench_banner:
                break;
            case R.id.tv_audio_video:
                startActivity(MeetingListActivity.class);
                break;
            case R.id.tv_video_conference:
                startActivity(VideoconferenceConversation.class);
                break;
            case R.id.tv_poc_client:
                startActivity(InterPhoneListActivity.class);
                break;
            case R.id.tv_live:
                startActivity(new Intent(getActivity(), ECLIveChatRoomListUI.class));
                break;
            case R.id.tv_jiqiren:
                CCPAppManager.startChattingAction(getActivity(), RestServerDefines.ROBOT, "智能机器人");
                break;
        }
    }


    @Override
    public void fetchData() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
