// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.group;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.common.view.WrapListview;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GroupManagerAct_ViewBinding<T extends GroupManagerAct> implements Unbinder {
  protected T target;

  @UiThread
  public GroupManagerAct_ViewBinding(T target, View source) {
    this.target = target;

    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.tvanagerMCount = Utils.findRequiredViewAsType(source, R.id.tv_manager_count, "field 'tvanagerMCount'", TextView.class);
    target.lvMember = Utils.findRequiredViewAsType(source, R.id.lv_manager_member, "field 'lvMember'", WrapListview.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.tvanagerMCount = null;
    target.lvMember = null;

    this.target = null;
  }
}
