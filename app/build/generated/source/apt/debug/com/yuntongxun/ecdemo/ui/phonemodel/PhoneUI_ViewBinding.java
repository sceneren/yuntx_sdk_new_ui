// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.phonemodel;

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
import java.lang.IllegalStateException;
import java.lang.Override;

public class PhoneUI_ViewBinding<T extends PhoneUI> implements Unbinder {
  protected T target;

  private View view2131689664;

  private View view2131690387;

  @UiThread
  public PhoneUI_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.bu_login, "field 'tvLogin' and method 'onViewClicked'");
    target.tvLogin = Utils.castView(view, R.id.bu_login, "field 'tvLogin'", TextView.class);
    view2131689664 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.bu_register, "field 'tvRegister' and method 'onViewClicked'");
    target.tvRegister = Utils.castView(view, R.id.bu_register, "field 'tvRegister'", TextView.class);
    view2131690387 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.ll = Utils.findRequiredViewAsType(source, R.id.ll_bu, "field 'll'", LinearLayout.class);
    target.phoneTop = Utils.findRequiredViewAsType(source, R.id.phone_top, "field 'phoneTop'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.tvLogin = null;
    target.tvRegister = null;
    target.ll = null;
    target.phoneTop = null;

    view2131689664.setOnClickListener(null);
    view2131689664 = null;
    view2131690387.setOnClickListener(null);
    view2131690387 = null;

    this.target = null;
  }
}
