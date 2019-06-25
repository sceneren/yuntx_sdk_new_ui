// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.group;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.common.view.WrapListview;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GroupGagActivity_ViewBinding<T extends GroupGagActivity> implements Unbinder {
  protected T target;

  @UiThread
  public GroupGagActivity_ViewBinding(T target, View source) {
    this.target = target;

    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.lvAgaMember = Utils.findRequiredViewAsType(source, R.id.lv_aga_member, "field 'lvAgaMember'", WrapListview.class);
    target.siAllGag = Utils.findRequiredViewAsType(source, R.id.si_all_gag, "field 'siAllGag'", SettingItem.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.lvAgaMember = null;
    target.siAllGag = null;

    this.target = null;
  }
}
