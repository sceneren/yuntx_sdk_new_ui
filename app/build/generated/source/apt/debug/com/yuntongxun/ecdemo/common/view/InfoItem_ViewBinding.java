// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.common.view;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class InfoItem_ViewBinding<T extends InfoItem> implements Unbinder {
  protected T target;

  @UiThread
  public InfoItem_ViewBinding(T target, View source) {
    this.target = target;

    target.tvLeftTitle = Utils.findRequiredViewAsType(source, R.id.tv_left_title, "field 'tvLeftTitle'", TextView.class);
    target.tvRight = Utils.findRequiredViewAsType(source, R.id.tv_right, "field 'tvRight'", TextView.class);
    target.ivRight = Utils.findRequiredViewAsType(source, R.id.iv_right, "field 'ivRight'", ImageView.class);
    target.llRoot = Utils.findRequiredViewAsType(source, R.id.ll_root, "field 'llRoot'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.tvLeftTitle = null;
    target.tvRight = null;
    target.ivRight = null;
    target.llRoot = null;

    this.target = null;
  }
}
