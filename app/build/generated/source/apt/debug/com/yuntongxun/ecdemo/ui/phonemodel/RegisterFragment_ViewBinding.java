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

public class RegisterFragment_ViewBinding<T extends RegisterFragment> implements Unbinder {
  protected T target;

  private View view2131690339;

  private View view2131689673;

  @UiThread
  public RegisterFragment_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.edName = Utils.findRequiredViewAsType(source, R.id.ed_name, "field 'edName'", EditText.class);
    target.edYanzhengma = Utils.findRequiredViewAsType(source, R.id.ed_yanzhengma, "field 'edYanzhengma'", EditText.class);
    target.edPwd = Utils.findRequiredViewAsType(source, R.id.ed_pwd, "field 'edPwd'", EditText.class);
    target.llTop = Utils.findRequiredViewAsType(source, R.id.ll_top, "field 'llTop'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.tv_getcode, "field 'tvCode' and method 'onViewClicked'");
    target.tvCode = Utils.castView(view, R.id.tv_getcode, "field 'tvCode'", TextView.class);
    view2131690339 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.phone_regi, "field 'phoneRegi' and method 'onViewClicked'");
    target.phoneRegi = Utils.castView(view, R.id.phone_regi, "field 'phoneRegi'", Button.class);
    view2131689673 = view;
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
    target.edYanzhengma = null;
    target.edPwd = null;
    target.llTop = null;
    target.tvCode = null;
    target.phoneRegi = null;

    view2131690339.setOnClickListener(null);
    view2131690339 = null;
    view2131689673.setOnClickListener(null);
    view2131689673 = null;

    this.target = null;
  }
}
