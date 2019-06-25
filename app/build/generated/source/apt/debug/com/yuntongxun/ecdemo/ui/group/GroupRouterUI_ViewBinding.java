// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.group;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.RelativeLayout;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GroupRouterUI_ViewBinding<T extends GroupRouterUI> implements Unbinder {
  protected T target;

  private View view2131690194;

  private View view2131690170;

  private View view2131690172;

  @UiThread
  public GroupRouterUI_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.title_bar, "field 'titleBar' and method 'onViewClicked'");
    target.titleBar = Utils.castView(view, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    view2131690194 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.p_list, "field 'pList' and method 'onViewClicked'");
    target.pList = Utils.castView(view, R.id.p_list, "field 'pList'", RelativeLayout.class);
    view2131690170 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.p_search, "field 'pSearch' and method 'onViewClicked'");
    target.pSearch = Utils.castView(view, R.id.p_search, "field 'pSearch'", RelativeLayout.class);
    view2131690172 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.pList = null;
    target.pSearch = null;

    view2131690194.setOnClickListener(null);
    view2131690194 = null;
    view2131690170.setOnClickListener(null);
    view2131690170 = null;
    view2131690172.setOnClickListener(null);
    view2131690172 = null;

    this.target = null;
  }
}
