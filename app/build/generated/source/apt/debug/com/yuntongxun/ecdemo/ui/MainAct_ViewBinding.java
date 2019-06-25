// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.base.CCPCustomViewPager;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainAct_ViewBinding<T extends MainAct> implements Unbinder {
  protected T target;

  private View view2131689600;

  private View view2131689602;

  private View view2131689604;

  private View view2131689605;

  @UiThread
  public MainAct_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.vp = Utils.findRequiredViewAsType(source, R.id.vp, "field 'vp'", CCPCustomViewPager.class);
    view = Utils.findRequiredView(source, R.id.btn_tab_msg, "field 'btnTabMsg' and method 'onViewClicked'");
    target.btnTabMsg = Utils.castView(view, R.id.btn_tab_msg, "field 'btnTabMsg'", TextView.class);
    view2131689600 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.tvUnreadMsgNumber = Utils.findRequiredViewAsType(source, R.id.tv_unread_msg_number, "field 'tvUnreadMsgNumber'", TextView.class);
    view = Utils.findRequiredView(source, R.id.btn_address_list, "field 'btnAddressList' and method 'onViewClicked'");
    target.btnAddressList = Utils.castView(view, R.id.btn_address_list, "field 'btnAddressList'", TextView.class);
    view2131689602 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.tvUnreadAddressNumber = Utils.findRequiredViewAsType(source, R.id.tv_unread_address_number, "field 'tvUnreadAddressNumber'", TextView.class);
    view = Utils.findRequiredView(source, R.id.btn_workbench, "field 'btnWorkbench' and method 'onViewClicked'");
    target.btnWorkbench = Utils.castView(view, R.id.btn_workbench, "field 'btnWorkbench'", TextView.class);
    view2131689604 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_my, "field 'btnMy' and method 'onViewClicked'");
    target.btnMy = Utils.castView(view, R.id.btn_my, "field 'btnMy'", TextView.class);
    view2131689605 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.mainBottom = Utils.findRequiredViewAsType(source, R.id.main_bottom, "field 'mainBottom'", LinearLayout.class);
    target.mainLayout = Utils.findRequiredViewAsType(source, R.id.mainLayout, "field 'mainLayout'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.vp = null;
    target.btnTabMsg = null;
    target.tvUnreadMsgNumber = null;
    target.btnAddressList = null;
    target.tvUnreadAddressNumber = null;
    target.btnWorkbench = null;
    target.btnMy = null;
    target.mainBottom = null;
    target.mainLayout = null;

    view2131689600.setOnClickListener(null);
    view2131689600 = null;
    view2131689602.setOnClickListener(null);
    view2131689602 = null;
    view2131689604.setOnClickListener(null);
    view2131689604 = null;
    view2131689605.setOnClickListener(null);
    view2131689605 = null;

    this.target = null;
  }
}
