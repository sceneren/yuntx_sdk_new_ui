// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.settings;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class WebQrActivity_ViewBinding<T extends WebQrActivity> implements Unbinder {
  protected T target;

  @UiThread
  public WebQrActivity_ViewBinding(T target, View source) {
    this.target = target;

    target.tvName = Utils.findRequiredViewAsType(source, R.id.tv_group_name, "field 'tvName'", TextView.class);
    target.tvCount = Utils.findRequiredViewAsType(source, R.id.tv_group_count, "field 'tvCount'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.tvName = null;
    target.tvCount = null;

    this.target = null;
  }
}
