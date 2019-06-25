// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.personcenter;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class FriendInfoUI_ViewBinding<T extends FriendInfoUI> implements Unbinder {
  protected T target;

  private View view2131689709;

  private View view2131690094;

  private View view2131690095;

  private View view2131690084;

  private View view2131690087;

  private View view2131690090;

  @UiThread
  public FriendInfoUI_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.ivMyHeader = Utils.findRequiredViewAsType(source, R.id.iv_my_header, "field 'ivMyHeader'", ImageView.class);
    target.tvRemark = Utils.findRequiredViewAsType(source, R.id.tv_remark, "field 'tvRemark'", TextView.class);
    target.tvMyPhone = Utils.findRequiredViewAsType(source, R.id.tv_my_phone, "field 'tvMyPhone'", TextView.class);
    target.tvMyNick = Utils.findRequiredViewAsType(source, R.id.tv_my_nick, "field 'tvMyNick'", TextView.class);
    target.pTouxiang = Utils.findRequiredViewAsType(source, R.id.p_touxiang, "field 'pTouxiang'", LinearLayout.class);
    target.tvFriendBeizhu = Utils.findRequiredViewAsType(source, R.id.tv_my_beizhu, "field 'tvFriendBeizhu'", TextView.class);
    target.tvFriendAge = Utils.findRequiredViewAsType(source, R.id.tv_my_age, "field 'tvFriendAge'", TextView.class);
    target.tvFriendSign = Utils.findRequiredViewAsType(source, R.id.tv_my_sign, "field 'tvFriendSign'", TextView.class);
    view = Utils.findRequiredView(source, R.id.btn_add, "field 'btnAdd' and method 'onViewClicked'");
    target.btnAdd = Utils.castView(view, R.id.btn_add, "field 'btnAdd'", Button.class);
    view2131689709 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.phone_chat, "field 'phoneChat' and method 'onViewClicked'");
    target.phoneChat = Utils.castView(view, R.id.phone_chat, "field 'phoneChat'", Button.class);
    view2131690094 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.phone_voip, "field 'phoneVoip' and method 'onViewClicked'");
    target.phoneVoip = Utils.castView(view, R.id.phone_voip, "field 'phoneVoip'", Button.class);
    view2131690095 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.p_nickname, "field 're_beizhu' and method 'onViewClicked'");
    target.re_beizhu = Utils.castView(view, R.id.p_nickname, "field 're_beizhu'", RelativeLayout.class);
    view2131690084 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.p_age, "method 'onViewClicked'");
    view2131690087 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.p_sign, "method 'onViewClicked'");
    view2131690090 = view;
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
    target.ivMyHeader = null;
    target.tvRemark = null;
    target.tvMyPhone = null;
    target.tvMyNick = null;
    target.pTouxiang = null;
    target.tvFriendBeizhu = null;
    target.tvFriendAge = null;
    target.tvFriendSign = null;
    target.btnAdd = null;
    target.phoneChat = null;
    target.phoneVoip = null;
    target.re_beizhu = null;

    view2131689709.setOnClickListener(null);
    view2131689709 = null;
    view2131690094.setOnClickListener(null);
    view2131690094 = null;
    view2131690095.setOnClickListener(null);
    view2131690095 = null;
    view2131690084.setOnClickListener(null);
    view2131690084 = null;
    view2131690087.setOnClickListener(null);
    view2131690087 = null;
    view2131690090.setOnClickListener(null);
    view2131690090 = null;

    this.target = null;
  }
}
