// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.common.view;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GroupSetItemView_ViewBinding<T extends GroupSetItemView> implements Unbinder {
  protected T target;

  @UiThread
  public GroupSetItemView_ViewBinding(T target, View source) {
    this.target = target;

    target.tvTag = Utils.findRequiredViewAsType(source, R.id.tv_tag, "field 'tvTag'", TextView.class);
    target.tvValue = Utils.findRequiredViewAsType(source, R.id.tv_value, "field 'tvValue'", TextView.class);
    target.llRoot = Utils.findRequiredViewAsType(source, R.id.ll_root, "field 'llRoot'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.tvTag = null;
    target.tvValue = null;
    target.llRoot = null;

    this.target = null;
  }
}
