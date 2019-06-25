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

public class PhoneRegisterUI_ViewBinding<T extends PhoneRegisterUI> implements Unbinder {
  protected T target;

  private View view2131689672;

  private View view2131689673;

  @UiThread
  public PhoneRegisterUI_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.regiPhone = Utils.findRequiredViewAsType(source, R.id.phone_regi_mobile, "field 'regiPhone'", CCPFormInputView.class);
    target.regiSms = Utils.findRequiredViewAsType(source, R.id.phone_regi_sms, "field 'regiSms'", CCPFormInputView.class);
    target.regiPwd = Utils.findRequiredViewAsType(source, R.id.phone_regi_pwd, "field 'regiPwd'", CCPFormInputView.class);
    view = Utils.findRequiredView(source, R.id.phone_sms, "field 'buSms' and method 'butterknifeOnItemClick'");
    target.buSms = Utils.castView(view, R.id.phone_sms, "field 'buSms'", Button.class);
    view2131689672 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.butterknifeOnItemClick(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.phone_regi, "field 'buRegi' and method 'butterknifeOnItemClick'");
    target.buRegi = Utils.castView(view, R.id.phone_regi, "field 'buRegi'", Button.class);
    view2131689673 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.butterknifeOnItemClick(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.regiPhone = null;
    target.regiSms = null;
    target.regiPwd = null;
    target.buSms = null;
    target.buRegi = null;

    view2131689672.setOnClickListener(null);
    view2131689672 = null;
    view2131689673.setOnClickListener(null);
    view2131689673 = null;

    this.target = null;
  }
}
