// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MyFrament_ViewBinding<T extends MyFrament> implements Unbinder {
  protected T target;

  private View view2131690058;

  private View view2131690062;

  private View view2131690063;

  private View view2131690064;

  private View view2131690065;

  private View view2131690069;

  private View view2131690057;

  private View view2131690066;

  private View view2131690067;

  private View view2131690068;

  @UiThread
  public MyFrament_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    view = Utils.findRequiredView(source, R.id.iv_my_header, "field 'ivMyHeader' and method 'onViewClicked'");
    target.ivMyHeader = Utils.castView(view, R.id.iv_my_header, "field 'ivMyHeader'", ImageView.class);
    view2131690058 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.tvMyName = Utils.findRequiredViewAsType(source, R.id.tv_my_name, "field 'tvMyName'", TextView.class);
    target.tvMyPhone = Utils.findRequiredViewAsType(source, R.id.tv_my_phone, "field 'tvMyPhone'", TextView.class);
    view = Utils.findRequiredView(source, R.id.tv_my_wallte, "field 'tvMyWallte' and method 'onViewClicked'");
    target.tvMyWallte = Utils.castView(view, R.id.tv_my_wallte, "field 'tvMyWallte'", TextView.class);
    view2131690062 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_my_emo, "field 'tvMyEmo' and method 'onViewClicked'");
    target.tvMyEmo = Utils.castView(view, R.id.tv_my_emo, "field 'tvMyEmo'", TextView.class);
    view2131690063 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_my_about, "field 'tvMyAbout' and method 'onViewClicked'");
    target.tvMyAbout = Utils.castView(view, R.id.tv_my_about, "field 'tvMyAbout'", TextView.class);
    view2131690064 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_my_recommend, "field 'tvMySuggestion' and method 'onViewClicked'");
    target.tvMySuggestion = Utils.castView(view, R.id.tv_my_recommend, "field 'tvMySuggestion'", TextView.class);
    view2131690065 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_my_setting, "field 'tvMySetting' and method 'onViewClicked'");
    target.tvMySetting = Utils.castView(view, R.id.tv_my_setting, "field 'tvMySetting'", TextView.class);
    view2131690069 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.self_info, "field 'tvInfo' and method 'onViewClicked'");
    target.tvInfo = Utils.castView(view, R.id.self_info, "field 'tvInfo'", RelativeLayout.class);
    view2131690057 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_my_app_recommend, "method 'onViewClicked'");
    view2131690066 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_my_official_accounts, "method 'onViewClicked'");
    view2131690067 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_my_update, "method 'onViewClicked'");
    view2131690068 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.ivMyHeader = null;
    target.tvMyName = null;
    target.tvMyPhone = null;
    target.tvMyWallte = null;
    target.tvMyEmo = null;
    target.tvMyAbout = null;
    target.tvMySuggestion = null;
    target.tvMySetting = null;
    target.tvInfo = null;

    view2131690058.setOnClickListener(null);
    view2131690058 = null;
    view2131690062.setOnClickListener(null);
    view2131690062 = null;
    view2131690063.setOnClickListener(null);
    view2131690063 = null;
    view2131690064.setOnClickListener(null);
    view2131690064 = null;
    view2131690065.setOnClickListener(null);
    view2131690065 = null;
    view2131690069.setOnClickListener(null);
    view2131690069 = null;
    view2131690057.setOnClickListener(null);
    view2131690057 = null;
    view2131690066.setOnClickListener(null);
    view2131690066 = null;
    view2131690067.setOnClickListener(null);
    view2131690067 = null;
    view2131690068.setOnClickListener(null);
    view2131690068 = null;

    this.target = null;
  }
}
