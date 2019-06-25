// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.personcenter;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class DeleteFriendUI_ViewBinding<T extends DeleteFriendUI> implements Unbinder {
  protected T target;

  private View view2131689936;

  @UiThread
  public DeleteFriendUI_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    view = Utils.findRequiredView(source, R.id.bu_delete, "field 'buDelete' and method 'onViewClicked'");
    target.buDelete = Utils.castView(view, R.id.bu_delete, "field 'buDelete'", Button.class);
    view2131689936 = view;
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
    target.buDelete = null;

    view2131689936.setOnClickListener(null);
    view2131689936 = null;

    this.target = null;
  }
}
