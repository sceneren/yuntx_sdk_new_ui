// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.livechatroom;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LiveChatUI_ViewBinding<T extends LiveChatUI> implements Unbinder {
  protected T target;

  @UiThread
  public LiveChatUI_ViewBinding(T target, View source) {
    this.target = target;

    target.giftNumView = Utils.findRequiredViewAsType(source, R.id.live_gift_num, "field 'giftNumView'", GiftItemView.class);
    target.iv_aixin = Utils.findRequiredViewAsType(source, R.id.iv_aixin, "field 'iv_aixin'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.giftNumView = null;
    target.iv_aixin = null;

    this.target = null;
  }
}
