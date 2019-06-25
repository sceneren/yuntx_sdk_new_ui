// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.group;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GroupMemberControlAct_ViewBinding<T extends GroupMemberControlAct> implements Unbinder {
  protected T target;

  private View view2131689661;

  @UiThread
  public GroupMemberControlAct_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.llBottom = Utils.findRequiredViewAsType(source, R.id.ll_bottom, "field 'llBottom'", LinearLayout.class);
    target.tvCount = Utils.findRequiredViewAsType(source, R.id.tv_count, "field 'tvCount'", TextView.class);
    view = Utils.findRequiredView(source, R.id.btn_invite, "field 'btnInvite' and method 'onViewClicked'");
    target.btnInvite = Utils.castView(view, R.id.btn_invite, "field 'btnInvite'", Button.class);
    view2131689661 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked();
      }
    });
    target.contactContainer = Utils.findRequiredViewAsType(source, R.id.contact_container, "field 'contactContainer'", FrameLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.llBottom = null;
    target.tvCount = null;
    target.btnInvite = null;
    target.contactContainer = null;

    view2131689661.setOnClickListener(null);
    view2131689661 = null;

    this.target = null;
  }
}
