// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.adapter;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class NewFrendAdapter$ViewHolder_ViewBinding<T extends NewFrendAdapter.ViewHolder> implements Unbinder {
  protected T target;

  @UiThread
  public NewFrendAdapter$ViewHolder_ViewBinding(T target, View source) {
    this.target = target;

    target.ivMyHeader = Utils.findRequiredViewAsType(source, R.id.iv_my_header, "field 'ivMyHeader'", ImageView.class);
    target.tvName = Utils.findRequiredViewAsType(source, R.id.item_name, "field 'tvName'", TextView.class);
    target.tvPhone = Utils.findRequiredViewAsType(source, R.id.item_phone, "field 'tvPhone'", TextView.class);
    target.buAdd = Utils.findRequiredViewAsType(source, R.id.item_bu, "field 'buAdd'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.ivMyHeader = null;
    target.tvName = null;
    target.tvPhone = null;
    target.buAdd = null;

    this.target = null;
  }
}
