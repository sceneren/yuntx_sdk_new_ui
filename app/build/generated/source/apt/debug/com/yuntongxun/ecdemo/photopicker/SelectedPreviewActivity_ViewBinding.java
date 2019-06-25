// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.photopicker;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.photopicker.widgets.CheckView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SelectedPreviewActivity_ViewBinding<T extends SelectedPreviewActivity> implements Unbinder {
  protected T target;

  private View view2131689657;

  private View view2131689658;

  private View view2131689659;

  @UiThread
  public SelectedPreviewActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.pager = Utils.findRequiredViewAsType(source, R.id.pager, "field 'pager'", ViewPager.class);
    view = Utils.findRequiredView(source, R.id.button_back, "field 'buttonBack' and method 'onViewClicked'");
    target.buttonBack = Utils.castView(view, R.id.button_back, "field 'buttonBack'", TextView.class);
    view2131689657 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.button_apply, "field 'buttonApply' and method 'onViewClicked'");
    target.buttonApply = Utils.castView(view, R.id.button_apply, "field 'buttonApply'", TextView.class);
    view2131689658 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.bottomToolbar = Utils.findRequiredViewAsType(source, R.id.bottom_toolbar, "field 'bottomToolbar'", FrameLayout.class);
    view = Utils.findRequiredView(source, R.id.check_view, "field 'checkView' and method 'onViewClicked'");
    target.checkView = Utils.castView(view, R.id.check_view, "field 'checkView'", CheckView.class);
    view2131689659 = view;
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

    target.pager = null;
    target.buttonBack = null;
    target.buttonApply = null;
    target.bottomToolbar = null;
    target.checkView = null;

    view2131689657.setOnClickListener(null);
    view2131689657 = null;
    view2131689658.setOnClickListener(null);
    view2131689658 = null;
    view2131689659.setOnClickListener(null);
    view2131689659 = null;

    this.target = null;
  }
}
