// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.chatting.view;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class VoiceModelUi_ViewBinding<T extends VoiceModelUi> implements Unbinder {
  protected T target;

  private View view2131689779;

  private View view2131689780;

  private View view2131689781;

  private View view2131689778;

  private View view2131689776;

  private View view2131690808;

  private View view2131690809;

  private View view2131689785;

  private View view2131689999;

  @UiThread
  public VoiceModelUi_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.gvChangeVoice = Utils.findRequiredViewAsType(source, R.id.gv_change_voice, "field 'gvChangeVoice'", GridView.class);
    view = Utils.findRequiredView(source, R.id.layout_call_divid, "field 'layoutCallDivid' and method 'onViewClicked'");
    target.layoutCallDivid = Utils.castView(view, R.id.layout_call_divid, "field 'layoutCallDivid'", ImageView.class);
    view2131689779 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.layout_cancel_changevoice, "field 'layoutCancelChangevoice' and method 'onViewClicked'");
    target.layoutCancelChangevoice = Utils.castView(view, R.id.layout_cancel_changevoice, "field 'layoutCancelChangevoice'", TextView.class);
    view2131689780 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.layout_send_changevoice, "field 'layoutSendChangevoice' and method 'onViewClicked'");
    target.layoutSendChangevoice = Utils.castView(view, R.id.layout_send_changevoice, "field 'layoutSendChangevoice'", TextView.class);
    view2131689781 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.call_mute_container, "field 'callMuteContainer' and method 'onViewClicked'");
    target.callMuteContainer = Utils.castView(view, R.id.call_mute_container, "field 'callMuteContainer'", RelativeLayout.class);
    view2131689778 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.ll_biansheng_contain, "field 'llBianshengContain' and method 'onViewClicked'");
    target.llBianshengContain = Utils.castView(view, R.id.ll_biansheng_contain, "field 'llBianshengContain'", LinearLayout.class);
    view2131689776 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.vpFootVoice = Utils.findRequiredViewAsType(source, R.id.vp_foot_voice, "field 'vpFootVoice'", ViewPager.class);
    target.dot = Utils.findRequiredViewAsType(source, R.id.dot, "field 'dot'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.rb_talk_back, "field 'rbTalkBack' and method 'onViewClicked'");
    target.rbTalkBack = Utils.castView(view, R.id.rb_talk_back, "field 'rbTalkBack'", AppCompatRadioButton.class);
    view2131690808 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.rb_change, "field 'rbChange' and method 'onViewClicked'");
    target.rbChange = Utils.castView(view, R.id.rb_change, "field 'rbChange'", AppCompatRadioButton.class);
    view2131690809 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.rg, "field 'rg' and method 'onViewClicked'");
    target.rg = Utils.castView(view, R.id.rg, "field 'rg'", RadioGroup.class);
    view2131689785 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.rlController = Utils.findRequiredViewAsType(source, R.id.rl_controller, "field 'rlController'", RelativeLayout.class);
    target.llRootView = Utils.findRequiredViewAsType(source, R.id.ll_rootView, "field 'llRootView'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.iv_capture, "method 'onViewClicked'");
    view2131689999 = view;
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

    target.gvChangeVoice = null;
    target.layoutCallDivid = null;
    target.layoutCancelChangevoice = null;
    target.layoutSendChangevoice = null;
    target.callMuteContainer = null;
    target.llBianshengContain = null;
    target.vpFootVoice = null;
    target.dot = null;
    target.rbTalkBack = null;
    target.rbChange = null;
    target.rg = null;
    target.rlController = null;
    target.llRootView = null;

    view2131689779.setOnClickListener(null);
    view2131689779 = null;
    view2131689780.setOnClickListener(null);
    view2131689780 = null;
    view2131689781.setOnClickListener(null);
    view2131689781 = null;
    view2131689778.setOnClickListener(null);
    view2131689778 = null;
    view2131689776.setOnClickListener(null);
    view2131689776 = null;
    view2131690808.setOnClickListener(null);
    view2131690808 = null;
    view2131690809.setOnClickListener(null);
    view2131690809 = null;
    view2131689785.setOnClickListener(null);
    view2131689785 = null;
    view2131689999.setOnClickListener(null);
    view2131689999 = null;

    this.target = null;
  }
}
