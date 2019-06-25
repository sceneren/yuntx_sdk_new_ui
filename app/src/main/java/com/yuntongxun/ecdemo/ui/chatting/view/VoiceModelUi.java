package com.yuntongxun.ecdemo.ui.chatting.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CommonPoPWindow;
import com.yuntongxun.ecdemo.ui.adapter.BaseViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * Created by zlk on 2017/8/16.
 *
 * 抽取voice controller 控制录音和变声
 */

public class VoiceModelUi extends FrameLayout {
    @BindView(R.id.gv_change_voice)
    GridView gvChangeVoice;
    @BindView(R.id.layout_call_divid)
    ImageView layoutCallDivid;
    @BindView(R.id.layout_cancel_changevoice)
    TextView layoutCancelChangevoice;
    @BindView(R.id.layout_send_changevoice)
    TextView layoutSendChangevoice;
    @BindView(R.id.call_mute_container)
    RelativeLayout callMuteContainer;
    @BindView(R.id.ll_biansheng_contain)
    LinearLayout llBianshengContain;
    @BindView(R.id.vp_foot_voice)
    ViewPager vpFootVoice;
    @BindView(R.id.dot)
    ImageView dot;
    @BindView(R.id.rb_talk_back)
    AppCompatRadioButton rbTalkBack;
    @BindView(R.id.rb_change)
    AppCompatRadioButton rbChange;
    @BindView(R.id.rg)
    RadioGroup rg;
    @BindView(R.id.rl_controller)
    RelativeLayout rlController;
    @BindView(R.id.ll_rootView)
    LinearLayout llRootView;

    private List<View> views;

    private Unbinder unbinder;
    private OnVPImageClickListener listener;
    int[] ids = new int[]{R.id.voice_record_imgbtn, R.id.voice_record_imgbtn_biansheng};
    private OnClickListener onRecordClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ib_play:
                    updatePopUI(1, R.layout.pop_play_ui);
                    break;
                case R.id.ib_record:

                    break;
                case R.id.ib_cancle:
                    poPWindow.dismiss();
                    break;
            }
        }
    };
    private TextView tvTime;
    private OnClickListener onPlayListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_play_send:
                    //发送
                    break;
                case R.id.tv_play_cancle:
                    //取消
                    break;
            }
        }
    };

    /**
     * model 分为1录音和2变声的
     *
     * @param model
     * @param layoutResID
     */
    private void updatePopUI(int model, @LayoutRes int layoutResID) {
        View contentView = LayoutInflater.from(getContext()).inflate(layoutResID, null);
        tvTime = (TextView) contentView.findViewById(R.id.tv_play_time);
        ImageButton ib_play_paly = (ImageButton) contentView.findViewById(R.id.ib_play_paly);
        contentView.findViewById(R.id.tv_play_send).setOnClickListener(onPlayListener);
        contentView.findViewById(R.id.tv_play_cancle).setOnClickListener(onPlayListener);

        poPWindow.setContentView(contentView);
        poPWindow.update();

    }

    private CommonPoPWindow poPWindow;


    public VoiceModelUi(Context context) {
        super(context);
        init(context, null);
    }

    public VoiceModelUi(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        afterSetUpViews();
    }

    private void afterSetUpViews() {
        initInflaterViews();
        setModelViceController();
    }


    private void initInflaterViews() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.chat_voice_normal, null));
        views.add(inflater.inflate(R.layout.chat_voice_biansheng, null));
    }


    /**
     * 设置控制器
     */
    public void setModelViceController() {

        llRootView.setVisibility(VISIBLE);
        rlController.setVisibility(VISIBLE);
        llBianshengContain.setVisibility(GONE);

        BaseViewPagerAdapter vpAdapter = new BaseViewPagerAdapter(views, getContext());
        vpFootVoice.setAdapter(vpAdapter);

        vpFootVoice.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset > 0.2) {
                    smooscrollRg(positionOffset);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    ((RadioButton) rg.getChildAt(0)).setChecked(true);
                } else {
                    ((RadioButton) rg.getChildAt(1)).setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        int size = views.size();
        for (int i = 0; i < size; i++) {
            final int finalI = i;
            views.get(i).findViewById(ids[i]).setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onVPImageClick(finalI);
                    }

                    if (finalI == 0) {//录音
                        showRecordUI();
                    } else if (finalI == 1) {//变声

                    }
                    return true;
                }
            });
        }
    }

    private void showRecordUI() {

        poPWindow = new CommonPoPWindow(getContext(), new CommonPoPWindow.PopCallback() {
            @Override
            public View getPopWindowChildView(View mMenuView) {
                mMenuView.findViewById(R.id.ib_play).setOnClickListener(onRecordClickListener);
                mMenuView.findViewById(R.id.ib_record).setOnClickListener(onRecordClickListener);
                mMenuView.findViewById(R.id.ib_cancle).setOnClickListener(onRecordClickListener);
                return null;
            }
        }, R.layout.pop_record_ui);

        poPWindow.showAtLocation(llRootView, Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View rootViewInf = inflater.inflate(R.layout.voice_model_ui_layout, this);
        unbinder = ButterKnife.bind(rootViewInf);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unbinder.unbind();
    }

    @OnClick({R.id.layout_call_divid, R.id.layout_cancel_changevoice, R.id.layout_send_changevoice
            , R.id.call_mute_container, R.id.ll_biansheng_contain, R.id.rb_talk_back, R.id.rb_change, R.id.rg
    ,R.id.iv_capture})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_call_divid:
                break;
            case R.id.layout_cancel_changevoice:
                break;
            case R.id.layout_send_changevoice:
                break;
            case R.id.call_mute_container:
                break;
            case R.id.ll_biansheng_contain:
                break;
            case R.id.rb_talk_back:
                break;
            case R.id.rb_change:
                break;
            case R.id.rg:
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        distance = rbTalkBack.getLeft() - rbChange.getLeft();
    }

    int distance;

    private void smooscrollRg(float position) {
        if (rg == null) {
            return;
        }
        float translationX = 0;
        float v = 1 - Math.abs(position);
        if (position < 0 && position > -1) {
            translationX = v * distance;
            rg.setTranslationX(translationX);
        } else if (position > 0 && position < 1) {
            translationX = v * distance;
            rg.setTranslationX(translationX);
        }

    }


    public void setOnVPImageClickListener(OnVPImageClickListener l) {
        this.listener = l;
    }


    public interface OnVPImageClickListener {
        void onVPImageClick(int position);
    }

    public interface OnSendClickListener {
        void onSendClick(int position);
    }

    public interface OnCancleClickListener {
        void onCancleClickL(int position);
    }

}
