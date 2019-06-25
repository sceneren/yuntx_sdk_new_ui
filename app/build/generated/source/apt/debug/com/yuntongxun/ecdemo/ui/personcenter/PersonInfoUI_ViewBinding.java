// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.personcenter;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.SettingItem;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class PersonInfoUI_ViewBinding<T extends PersonInfoUI> implements Unbinder {
  protected T target;

  private View view2131690081;

  private View view2131690084;

  private View view2131690090;

  private View view2131690383;

  private View view2131690087;

  @UiThread
  public PersonInfoUI_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.ivMyHeader = Utils.findRequiredViewAsType(source, R.id.iv_my_header, "field 'ivMyHeader'", ImageView.class);
    target.tvMyName = Utils.findRequiredViewAsType(source, R.id.tv_my_name, "field 'tvMyName'", TextView.class);
    target.ivGoo = Utils.findRequiredViewAsType(source, R.id.iv_goo, "field 'ivGoo'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.p_touxiang, "field 'pTouxiang' and method 'onViewClicked'");
    target.pTouxiang = Utils.castView(view, R.id.p_touxiang, "field 'pTouxiang'", RelativeLayout.class);
    view2131690081 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.tvMyNick = Utils.findRequiredViewAsType(source, R.id.tv_my_nick, "field 'tvMyNick'", TextView.class);
    target.ivGo = Utils.findRequiredViewAsType(source, R.id.iv_go, "field 'ivGo'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.p_nickname, "field 'pNickname' and method 'onViewClicked'");
    target.pNickname = Utils.castView(view, R.id.p_nickname, "field 'pNickname'", RelativeLayout.class);
    view2131690084 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.tvMySign = Utils.findRequiredViewAsType(source, R.id.tv_my_sign, "field 'tvMySign'", TextView.class);
    target.ivGo2 = Utils.findRequiredViewAsType(source, R.id.iv_go2, "field 'ivGo2'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.p_sign, "field 'pSign' and method 'onViewClicked'");
    target.pSign = Utils.castView(view, R.id.p_sign, "field 'pSign'", RelativeLayout.class);
    view2131690090 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.tvMySex = Utils.findRequiredViewAsType(source, R.id.tv_my_sex, "field 'tvMySex'", TextView.class);
    target.ivGo3 = Utils.findRequiredViewAsType(source, R.id.iv_go3, "field 'ivGo3'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.p_sex, "field 'pSex' and method 'onViewClicked'");
    target.pSex = Utils.castView(view, R.id.p_sex, "field 'pSex'", RelativeLayout.class);
    view2131690383 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.tvMyAge = Utils.findRequiredViewAsType(source, R.id.tv_my_age, "field 'tvMyAge'", TextView.class);
    target.ivGo4 = Utils.findRequiredViewAsType(source, R.id.iv_go4, "field 'ivGo4'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.p_age, "field 'pAge' and method 'onViewClicked'");
    target.pAge = Utils.castView(view, R.id.p_age, "field 'pAge'", RelativeLayout.class);
    view2131690087 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.infoMsgNotify = Utils.findRequiredViewAsType(source, R.id.info_msg_notify, "field 'infoMsgNotify'", SettingItem.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.ivMyHeader = null;
    target.tvMyName = null;
    target.ivGoo = null;
    target.pTouxiang = null;
    target.tvMyNick = null;
    target.ivGo = null;
    target.pNickname = null;
    target.tvMySign = null;
    target.ivGo2 = null;
    target.pSign = null;
    target.tvMySex = null;
    target.ivGo3 = null;
    target.pSex = null;
    target.tvMyAge = null;
    target.ivGo4 = null;
    target.pAge = null;
    target.infoMsgNotify = null;

    view2131690081.setOnClickListener(null);
    view2131690081 = null;
    view2131690084.setOnClickListener(null);
    view2131690084 = null;
    view2131690090.setOnClickListener(null);
    view2131690090 = null;
    view2131690383.setOnClickListener(null);
    view2131690383 = null;
    view2131690087.setOnClickListener(null);
    view2131690087 = null;

    this.target = null;
  }
}
