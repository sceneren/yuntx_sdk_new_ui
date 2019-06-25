// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.adapter;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GagMemberAdapter$ViewHolder_ViewBinding<T extends GagMemberAdapter.ViewHolder> implements Unbinder {
  protected T target;

  @UiThread
  public GagMemberAdapter$ViewHolder_ViewBinding(T target, View source) {
    this.target = target;

    target.tvGagAvatar = Utils.findRequiredViewAsType(source, R.id.tv_gag_avatar, "field 'tvGagAvatar'", TextView.class);
    target.mIvAvatar = Utils.findRequiredViewAsType(source, R.id.iv_avatar, "field 'mIvAvatar'", ImageView.class);
    target.tvGagMemberName = Utils.findRequiredViewAsType(source, R.id.tv_gag_member_name, "field 'tvGagMemberName'", TextView.class);
    target.tvGagTime = Utils.findRequiredViewAsType(source, R.id.tv_gag_time, "field 'tvGagTime'", TextView.class);
    target.btnClearGag = Utils.findRequiredViewAsType(source, R.id.btn_clear_gag, "field 'btnClearGag'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.tvGagAvatar = null;
    target.mIvAvatar = null;
    target.tvGagMemberName = null;
    target.tvGagTime = null;
    target.btnClearGag = null;

    this.target = null;
  }
}
