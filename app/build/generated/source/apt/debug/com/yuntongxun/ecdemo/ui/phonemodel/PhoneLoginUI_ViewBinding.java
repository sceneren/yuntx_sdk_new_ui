// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.phonemodel;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.base.CCPFormInputView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class PhoneLoginUI_ViewBinding<T extends PhoneLoginUI> implements Unbinder {
  protected T target;

  private View view2131689668;

  @UiThread
  public PhoneLoginUI_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.ccpPhone = Utils.findRequiredViewAsType(source, R.id.phone_login_mobile, "field 'ccpPhone'", CCPFormInputView.class);
    target.ccpPwd = Utils.findRequiredViewAsType(source, R.id.phone_login_pwd, "field 'ccpPwd'", CCPFormInputView.class);
    view = Utils.findRequiredView(source, R.id.phone_sign_in_button, "field 'buLogin' and method 'onClick'");
    target.buLogin = Utils.castView(view, R.id.phone_sign_in_button, "field 'buLogin'", Button.class);
    view2131689668 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.ccpPhone = null;
    target.ccpPwd = null;
    target.buLogin = null;

    view2131689668.setOnClickListener(null);
    view2131689668 = null;

    this.target = null;
  }
}
