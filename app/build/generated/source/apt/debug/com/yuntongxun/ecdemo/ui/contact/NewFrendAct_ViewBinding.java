// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.contact;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.common.view.WrapListview;
import java.lang.IllegalStateException;
import java.lang.Override;

public class NewFrendAct_ViewBinding<T extends NewFrendAct> implements Unbinder {
  protected T target;

  private View view2131689608;

  @UiThread
  public NewFrendAct_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.tvTagNotice = Utils.findRequiredViewAsType(source, R.id.tv_tag_notice, "field 'tvTagNotice'", TextView.class);
    target.lvFriendsNotice = Utils.findRequiredViewAsType(source, R.id.lv_friends_notice, "field 'lvFriendsNotice'", WrapListview.class);
    target.tvTagRecommend = Utils.findRequiredViewAsType(source, R.id.tv_tag_recommend, "field 'tvTagRecommend'", TextView.class);
    target.lvFriendsRecommend = Utils.findRequiredViewAsType(source, R.id.lv_friends_recommend, "field 'lvFriendsRecommend'", WrapListview.class);
    view = Utils.findRequiredView(source, R.id.tv_more, "field 'mTvMore' and method 'onViewClicked'");
    target.mTvMore = Utils.castView(view, R.id.tv_more, "field 'mTvMore'", TextView.class);
    view2131689608 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.tvTagNotice = null;
    target.lvFriendsNotice = null;
    target.tvTagRecommend = null;
    target.lvFriendsRecommend = null;
    target.mTvMore = null;

    view2131689608.setOnClickListener(null);
    view2131689608 = null;

    this.target = null;
  }
}
