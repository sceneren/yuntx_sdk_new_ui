// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class WorkbenchFragment_ViewBinding<T extends WorkbenchFragment> implements Unbinder {
  protected T target;

  private View view2131690071;

  private View view2131690073;

  private View view2131690074;

  private View view2131690075;

  private View view2131690076;

  private View view2131690077;

  @UiThread
  public WorkbenchFragment_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.iv_workbench_banner, "field 'ivWorkbenchBanner' and method 'onViewClicked'");
    target.ivWorkbenchBanner = Utils.castView(view, R.id.iv_workbench_banner, "field 'ivWorkbenchBanner'", ImageView.class);
    view2131690071 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_audio_video, "field 'tvAudioVideo' and method 'onViewClicked'");
    target.tvAudioVideo = Utils.castView(view, R.id.tv_audio_video, "field 'tvAudioVideo'", TextView.class);
    view2131690073 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_video_conference, "field 'tvVideoConference' and method 'onViewClicked'");
    target.tvVideoConference = Utils.castView(view, R.id.tv_video_conference, "field 'tvVideoConference'", TextView.class);
    view2131690074 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_poc_client, "field 'tvPocClient' and method 'onViewClicked'");
    target.tvPocClient = Utils.castView(view, R.id.tv_poc_client, "field 'tvPocClient'", TextView.class);
    view2131690075 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_live, "field 'tvLive' and method 'onViewClicked'");
    target.tvLive = Utils.castView(view, R.id.tv_live, "field 'tvLive'", TextView.class);
    view2131690076 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_jiqiren, "field 'tvJq' and method 'onViewClicked'");
    target.tvJq = Utils.castView(view, R.id.tv_jiqiren, "field 'tvJq'", TextView.class);
    view2131690077 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.llIm = Utils.findRequiredViewAsType(source, R.id.ll_im, "field 'llIm'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.ivWorkbenchBanner = null;
    target.tvAudioVideo = null;
    target.tvVideoConference = null;
    target.tvPocClient = null;
    target.tvLive = null;
    target.tvJq = null;
    target.titleBar = null;
    target.llIm = null;

    view2131690071.setOnClickListener(null);
    view2131690071 = null;
    view2131690073.setOnClickListener(null);
    view2131690073 = null;
    view2131690074.setOnClickListener(null);
    view2131690074 = null;
    view2131690075.setOnClickListener(null);
    view2131690075 = null;
    view2131690076.setOnClickListener(null);
    view2131690076 = null;
    view2131690077.setOnClickListener(null);
    view2131690077 = null;

    this.target = null;
  }
}
