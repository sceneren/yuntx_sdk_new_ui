// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.personcenter;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SetInfoUI_ViewBinding<T extends SetInfoUI> implements Unbinder {
  protected T target;

  @UiThread
  public SetInfoUI_ViewBinding(T target, View source) {
    this.target = target;

    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.etSingle = Utils.findRequiredViewAsType(source, R.id.et_single, "field 'etSingle'", EditText.class);
    target.etMult = Utils.findRequiredViewAsType(source, R.id.et_mult, "field 'etMult'", EditText.class);
    target.cbNan = Utils.findRequiredViewAsType(source, R.id.cb_nan, "field 'cbNan'", CheckBox.class);
    target.reNan = Utils.findRequiredViewAsType(source, R.id.re_nan, "field 'reNan'", RelativeLayout.class);
    target.cbNv = Utils.findRequiredViewAsType(source, R.id.cb_nv, "field 'cbNv'", CheckBox.class);
    target.reNv = Utils.findRequiredViewAsType(source, R.id.re_nv, "field 'reNv'", RelativeLayout.class);
    target.llSex = Utils.findRequiredViewAsType(source, R.id.ll_sex, "field 'llSex'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.etSingle = null;
    target.etMult = null;
    target.cbNan = null;
    target.reNan = null;
    target.cbNv = null;
    target.reNv = null;
    target.llSex = null;

    this.target = null;
  }
}
