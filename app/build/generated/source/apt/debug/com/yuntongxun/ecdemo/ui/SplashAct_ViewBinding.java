// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SplashAct_ViewBinding<T extends SplashAct> implements Unbinder {
  protected T target;

  @UiThread
  public SplashAct_ViewBinding(T target, View source) {
    this.target = target;

    target.splashRoot = Utils.findRequiredViewAsType(source, R.id.splash_root, "field 'splashRoot'", FrameLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.splashRoot = null;

    this.target = null;
  }
}
