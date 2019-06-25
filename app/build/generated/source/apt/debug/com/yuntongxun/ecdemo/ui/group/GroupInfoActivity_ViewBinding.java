// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.group;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.InfoItem;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.common.view.WrapGridView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GroupInfoActivity_ViewBinding<T extends GroupInfoActivity> implements Unbinder {
  protected T target;

  private View view2131690134;

  private View view2131690136;

  private View view2131690137;

  private View view2131690138;

  private View view2131690139;

  private View view2131690140;

  private View view2131690141;

  private View view2131690145;

  private View view2131690146;

  private View view2131690147;

  private View view2131690144;

  @UiThread
  public GroupInfoActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    view = Utils.findRequiredView(source, R.id.info_count, "field 'infoCount' and method 'onViewClicked'");
    target.infoCount = Utils.castView(view, R.id.info_count, "field 'infoCount'", InfoItem.class);
    view2131690134 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.gvMember = Utils.findRequiredViewAsType(source, R.id.gv_member, "field 'gvMember'", WrapGridView.class);
    view = Utils.findRequiredView(source, R.id.name, "field 'name' and method 'onViewClicked'");
    target.name = Utils.castView(view, R.id.name, "field 'name'", InfoItem.class);
    view2131690136 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.qr, "field 'qr' and method 'onViewClicked'");
    target.qr = Utils.castView(view, R.id.qr, "field 'qr'", InfoItem.class);
    view2131690137 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.notice, "field 'notice' and method 'onViewClicked'");
    target.notice = Utils.castView(view, R.id.notice, "field 'notice'", InfoItem.class);
    view2131690138 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.card, "field 'card' and method 'onViewClicked'");
    target.card = Utils.castView(view, R.id.card, "field 'card'", InfoItem.class);
    view2131690139 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.gag, "field 'gag' and method 'onViewClicked'");
    target.gag = Utils.castView(view, R.id.gag, "field 'gag'", InfoItem.class);
    view2131690140 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.set_manager, "field 'setManager' and method 'onViewClicked'");
    target.setManager = Utils.castView(view, R.id.set_manager, "field 'setManager'", InfoItem.class);
    view2131690141 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.infoMsgNotify = Utils.findRequiredViewAsType(source, R.id.info_msg_notify, "field 'infoMsgNotify'", SettingItem.class);
    target.infoMsgPush = Utils.findRequiredViewAsType(source, R.id.info_msg_push, "field 'infoMsgPush'", SettingItem.class);
    view = Utils.findRequiredView(source, R.id.info_dissolve, "field 'infoDissolve' and method 'onViewClicked'");
    target.infoDissolve = Utils.castView(view, R.id.info_dissolve, "field 'infoDissolve'", SettingItem.class);
    view2131690145 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.clear_msg, "field 'clearMsg' and method 'onViewClicked'");
    target.clearMsg = Utils.castView(view, R.id.clear_msg, "field 'clearMsg'", SettingItem.class);
    view2131690146 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_group_quit, "field 'btnGroupQuit' and method 'onViewClicked'");
    target.btnGroupQuit = Utils.castView(view, R.id.btn_group_quit, "field 'btnGroupQuit'", Button.class);
    view2131690147 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.infoContent = Utils.findRequiredViewAsType(source, R.id.info_content, "field 'infoContent'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.info_trans_owner, "field 'infoTransOwner' and method 'onViewClicked'");
    target.infoTransOwner = Utils.castView(view, R.id.info_trans_owner, "field 'infoTransOwner'", SettingItem.class);
    view2131690144 = view;
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
    target.infoCount = null;
    target.gvMember = null;
    target.name = null;
    target.qr = null;
    target.notice = null;
    target.card = null;
    target.gag = null;
    target.setManager = null;
    target.infoMsgNotify = null;
    target.infoMsgPush = null;
    target.infoDissolve = null;
    target.clearMsg = null;
    target.btnGroupQuit = null;
    target.infoContent = null;
    target.infoTransOwner = null;

    view2131690134.setOnClickListener(null);
    view2131690134 = null;
    view2131690136.setOnClickListener(null);
    view2131690136 = null;
    view2131690137.setOnClickListener(null);
    view2131690137 = null;
    view2131690138.setOnClickListener(null);
    view2131690138 = null;
    view2131690139.setOnClickListener(null);
    view2131690139 = null;
    view2131690140.setOnClickListener(null);
    view2131690140 = null;
    view2131690141.setOnClickListener(null);
    view2131690141 = null;
    view2131690145.setOnClickListener(null);
    view2131690145 = null;
    view2131690146.setOnClickListener(null);
    view2131690146 = null;
    view2131690147.setOnClickListener(null);
    view2131690147 = null;
    view2131690144.setOnClickListener(null);
    view2131690144 = null;

    this.target = null;
  }
}
