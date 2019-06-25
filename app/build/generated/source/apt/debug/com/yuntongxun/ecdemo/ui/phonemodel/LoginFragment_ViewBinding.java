// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.phonemodel;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LoginFragment_ViewBinding<T extends LoginFragment> implements Unbinder {
  protected T target;

  private View view2131689662;

  private View view2131690385;

  private View view2131690386;

  @UiThread
  public LoginFragment_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.edName = Utils.findRequiredViewAsType(source, R.id.ed_name, "field 'edName'", EditText.class);
    target.edPwd = Utils.findRequiredViewAsType(source, R.id.ed_pwd, "field 'edPwd'", EditText.class);
    view = Utils.findRequiredView(source, R.id.ll_top, "field 'llTop' and method 'onViewClicked'");
    target.llTop = Utils.castView(view, R.id.ll_top, "field 'llTop'", LinearLayout.class);
    view2131689662 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.phone_login, "field 'phoneLogin' and method 'onViewClicked'");
    target.phoneLogin = Utils.castView(view, R.id.phone_login, "field 'phoneLogin'", Button.class);
    view2131690385 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_forget, "field 'tvForget' and method 'onViewClicked'");
    target.tvForget = Utils.castView(view, R.id.tv_forget, "field 'tvForget'", TextView.class);
    view2131690386 = view;
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

    target.edName = null;
    target.edPwd = null;
    target.llTop = null;
    target.phoneLogin = null;
    target.tvForget = null;

    view2131689662.setOnClickListener(null);
    view2131689662 = null;
    view2131690385.setOnClickListener(null);
    view2131690385 = null;
    view2131690386.setOnClickListener(null);
    view2131690386 = null;

    this.target = null;
  }
}
