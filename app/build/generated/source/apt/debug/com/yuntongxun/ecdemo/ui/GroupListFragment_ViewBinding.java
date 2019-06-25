// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GroupListFragment_ViewBinding<T extends GroupListFragment> implements Unbinder {
  protected T target;

  @UiThread
  public GroupListFragment_ViewBinding(T target, View source) {
    this.target = target;

    target.groupList = Utils.findRequiredViewAsType(source, R.id.group_list, "field 'groupList'", ListView.class);
    target.llEmpty = Utils.findRequiredViewAsType(source, R.id.ll_empty, "field 'llEmpty'", LinearLayout.class);
    target.loadingTipsArea = Utils.findRequiredViewAsType(source, R.id.loading_tips_area, "field 'loadingTipsArea'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.groupList = null;
    target.llEmpty = null;
    target.loadingTipsArea = null;

    this.target = null;
  }
}
