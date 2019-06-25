// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.group;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class CreateGroupActivity_ViewBinding<T extends CreateGroupActivity> implements Unbinder {
  protected T target;

  private View view2131690364;

  private View view2131690210;

  private View view2131690366;

  @UiThread
  public CreateGroupActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.edGroupName = Utils.findRequiredViewAsType(source, R.id.ed_group_name, "field 'edGroupName'", EditText.class);
    target.edProvince = Utils.findRequiredViewAsType(source, R.id.ed_province, "field 'edProvince'", EditText.class);
    target.edCity = Utils.findRequiredViewAsType(source, R.id.ed_city, "field 'edCity'", EditText.class);
    view = Utils.findRequiredView(source, R.id.tv_notice_select, "field 'tvNoticeSelect' and method 'onViewClicked'");
    target.tvNoticeSelect = Utils.castView(view, R.id.tv_notice_select, "field 'tvNoticeSelect'", TextView.class);
    view2131690364 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.llGroupNotice = Utils.findRequiredViewAsType(source, R.id.ll_group_notice, "field 'llGroupNotice'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.tv_notice, "field 'tvNotice' and method 'onViewClicked'");
    target.tvNotice = Utils.castView(view, R.id.tv_notice, "field 'tvNotice'", TextView.class);
    view2131690210 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.tv_group_type, "field 'tvGroupType' and method 'onViewClicked'");
    target.tvGroupType = Utils.castView(view, R.id.tv_group_type, "field 'tvGroupType'", TextView.class);
    view2131690366 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.llGroupType = Utils.findRequiredViewAsType(source, R.id.ll_group_type, "field 'llGroupType'", LinearLayout.class);
    target.siPublic = Utils.findRequiredViewAsType(source, R.id.si_public, "field 'siPublic'", SettingItem.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.edGroupName = null;
    target.edProvince = null;
    target.edCity = null;
    target.tvNoticeSelect = null;
    target.llGroupNotice = null;
    target.tvNotice = null;
    target.tvGroupType = null;
    target.llGroupType = null;
    target.siPublic = null;

    view2131690364.setOnClickListener(null);
    view2131690364 = null;
    view2131690210.setOnClickListener(null);
    view2131690210 = null;
    view2131690366.setOnClickListener(null);
    view2131690366 = null;

    this.target = null;
  }
}
