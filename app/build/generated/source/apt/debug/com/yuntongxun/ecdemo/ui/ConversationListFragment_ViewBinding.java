// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ConversationListFragment_ViewBinding<T extends ConversationListFragment> implements Unbinder {
  protected T target;

  private View view2131690194;

  @UiThread
  public ConversationListFragment_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.title_bar, "field 'titleBar' and method 'onViewClicked'");
    target.titleBar = Utils.castView(view, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    view2131690194 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked();
      }
    });
    target.mainChattingLv = Utils.findRequiredViewAsType(source, R.id.main_chatting_lv, "field 'mainChattingLv'", ListView.class);
    target.emptyConversationTv = Utils.findRequiredViewAsType(source, R.id.empty_conversation_tv, "field 'emptyConversationTv'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.mainChattingLv = null;
    target.emptyConversationTv = null;

    view2131690194.setOnClickListener(null);
    view2131690194 = null;

    this.target = null;
  }
}
