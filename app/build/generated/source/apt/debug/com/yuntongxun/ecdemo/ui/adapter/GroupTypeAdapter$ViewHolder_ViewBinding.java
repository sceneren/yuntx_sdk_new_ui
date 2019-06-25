// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.adapter;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GroupTypeAdapter$ViewHolder_ViewBinding<T extends GroupTypeAdapter.ViewHolder> implements Unbinder {
  protected T target;

  @UiThread
  public GroupTypeAdapter$ViewHolder_ViewBinding(T target, View source) {
    this.target = target;

    target.tvItemGroupType = Utils.findRequiredViewAsType(source, R.id.tv_item_group_type, "field 'tvItemGroupType'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.tvItemGroupType = null;

    this.target = null;
  }
}
