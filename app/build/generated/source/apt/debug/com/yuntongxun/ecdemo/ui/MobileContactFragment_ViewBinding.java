// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.SearchEditText;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.ui.contact.BladeView;
import com.yuntongxun.ecdemo.ui.contact.PinnedHeaderListView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MobileContactFragment_ViewBinding<T extends MobileContactFragment> implements Unbinder {
  protected T target;

  @UiThread
  public MobileContactFragment_ViewBinding(T target, View source) {
    this.target = target;

    target.mListView = Utils.findRequiredViewAsType(source, R.id.address_contactlist, "field 'mListView'", PinnedHeaderListView.class);
    target.mLetterListView = Utils.findRequiredViewAsType(source, R.id.mLetterListView, "field 'mLetterListView'", BladeView.class);
    target.loadingTipsArea = Utils.findRequiredViewAsType(source, R.id.loading_tips_area, "field 'loadingTipsArea'", LinearLayout.class);
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.searchView = Utils.findRequiredViewAsType(source, R.id.search, "field 'searchView'", SearchEditText.class);
    target.empty = Utils.findRequiredViewAsType(source, R.id.empty, "field 'empty'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.mListView = null;
    target.mLetterListView = null;
    target.loadingTipsArea = null;
    target.titleBar = null;
    target.searchView = null;
    target.empty = null;

    this.target = null;
  }
}
