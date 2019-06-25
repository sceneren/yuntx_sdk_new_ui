// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.chatting;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.InfoItem;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ChatingDetailAct_ViewBinding<T extends ChatingDetailAct> implements Unbinder {
  protected T target;

  private View view2131689817;

  private View view2131689818;

  @UiThread
  public ChatingDetailAct_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.mTvAvatar = Utils.findRequiredViewAsType(source, R.id.tv_avatar, "field 'mTvAvatar'", TextView.class);
    target.mTvName = Utils.findRequiredViewAsType(source, R.id.tv_name, "field 'mTvName'", TextView.class);
    target.mRobot = Utils.findRequiredViewAsType(source, R.id.tv_robot, "field 'mRobot'", TextView.class);
    target.mSetTop = Utils.findRequiredViewAsType(source, R.id.set_top, "field 'mSetTop'", SettingItem.class);
    target.mNoDisturbing = Utils.findRequiredViewAsType(source, R.id.no_disturbing, "field 'mNoDisturbing'", SettingItem.class);
    view = Utils.findRequiredView(source, R.id.set_bg, "field 'mSetBg' and method 'onViewClicked'");
    target.mSetBg = Utils.castView(view, R.id.set_bg, "field 'mSetBg'", InfoItem.class);
    view2131689817 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_clear_histroy, "field 'mTvClearHistroy' and method 'onViewClicked'");
    target.mTvClearHistroy = Utils.castView(view, R.id.tv_clear_histroy, "field 'mTvClearHistroy'", TextView.class);
    view2131689818 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.mTitleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'mTitleBar'", TitleBar.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.mTvAvatar = null;
    target.mTvName = null;
    target.mRobot = null;
    target.mSetTop = null;
    target.mNoDisturbing = null;
    target.mSetBg = null;
    target.mTvClearHistroy = null;
    target.mTitleBar = null;

    view2131689817.setOnClickListener(null);
    view2131689817 = null;
    view2131689818.setOnClickListener(null);
    view2131689818 = null;

    this.target = null;
  }
}
