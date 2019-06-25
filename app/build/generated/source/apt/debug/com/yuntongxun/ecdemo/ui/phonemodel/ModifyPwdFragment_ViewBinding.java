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

public class ModifyPwdFragment_ViewBinding<T extends ModifyPwdFragment> implements Unbinder {
  protected T target;

  private View view2131690339;

  private View view2131690341;

  @UiThread
  public ModifyPwdFragment_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.edName = Utils.findRequiredViewAsType(source, R.id.ed_name, "field 'edName'", EditText.class);
    target.edYanzhengma = Utils.findRequiredViewAsType(source, R.id.ed_yanzhengma, "field 'edYanzhengma'", EditText.class);
    view = Utils.findRequiredView(source, R.id.tv_getcode, "field 'tvGetcode' and method 'onViewClicked'");
    target.tvGetcode = Utils.castView(view, R.id.tv_getcode, "field 'tvGetcode'", TextView.class);
    view2131690339 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.llYzm = Utils.findRequiredViewAsType(source, R.id.ll_yzm, "field 'llYzm'", LinearLayout.class);
    target.edPwd = Utils.findRequiredViewAsType(source, R.id.ed_pwd, "field 'edPwd'", EditText.class);
    target.llTop = Utils.findRequiredViewAsType(source, R.id.ll_top, "field 'llTop'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.phone_reset_pwd, "field 'phoneResetPwd' and method 'onViewClicked'");
    target.phoneResetPwd = Utils.castView(view, R.id.phone_reset_pwd, "field 'phoneResetPwd'", Button.class);
    view2131690341 = view;
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
    target.tvGetcode = null;
    target.llYzm = null;
    target.edPwd = null;
    target.llTop = null;
    target.phoneResetPwd = null;

    view2131690339.setOnClickListener(null);
    view2131690339 = null;
    view2131690341.setOnClickListener(null);
    view2131690341 = null;

    this.target = null;
  }
}
