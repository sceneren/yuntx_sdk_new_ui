// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.friend;

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
import com.yuntongxun.ecdemo.common.view.SearchEditText;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class AddFriendActivity_ViewBinding<T extends AddFriendActivity> implements Unbinder {
  protected T target;

  private View view2131689699;

  @UiThread
  public AddFriendActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.mSearch = Utils.findRequiredViewAsType(source, R.id.search, "field 'mSearch'", SearchEditText.class);
    view = Utils.findRequiredView(source, R.id.rl_add_phone, "field 'mRlAddContact' and method 'onViewClicked'");
    target.mRlAddContact = Utils.castView(view, R.id.rl_add_phone, "field 'mRlAddContact'", RelativeLayout.class);
    view2131689699 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked();
      }
    });
    target.mTvEmpty = Utils.findRequiredViewAsType(source, R.id.tv_empty, "field 'mTvEmpty'", TextView.class);
    target.mIvPhoneTag = Utils.findRequiredViewAsType(source, R.id.iv_phone_tag, "field 'mIvPhoneTag'", ImageView.class);
    target.mTvPhoneTag = Utils.findRequiredViewAsType(source, R.id.tv_phone_tag, "field 'mTvPhoneTag'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.mSearch = null;
    target.mRlAddContact = null;
    target.mTvEmpty = null;
    target.mIvPhoneTag = null;
    target.mTvPhoneTag = null;

    view2131689699.setOnClickListener(null);
    view2131689699 = null;

    this.target = null;
  }
}
