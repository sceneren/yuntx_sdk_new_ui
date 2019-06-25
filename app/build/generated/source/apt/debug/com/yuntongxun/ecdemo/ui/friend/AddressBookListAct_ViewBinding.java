// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.friend;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class AddressBookListAct_ViewBinding<T extends AddressBookListAct> implements Unbinder {
  protected T target;

  @UiThread
  public AddressBookListAct_ViewBinding(T target, View source) {
    this.target = target;

    target.mTitleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'mTitleBar'", TitleBar.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.mTitleBar = null;

    this.target = null;
  }
}
