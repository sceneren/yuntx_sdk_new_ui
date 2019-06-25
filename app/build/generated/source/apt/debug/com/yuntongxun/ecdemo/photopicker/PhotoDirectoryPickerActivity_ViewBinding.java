// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.photopicker;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ListView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class PhotoDirectoryPickerActivity_ViewBinding<T extends PhotoDirectoryPickerActivity> implements Unbinder {
  protected T target;

  @UiThread
  public PhotoDirectoryPickerActivity_ViewBinding(T target, View source) {
    this.target = target;

    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.mFloderListView = Utils.findRequiredViewAsType(source, R.id.listview_floder, "field 'mFloderListView'", ListView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.titleBar = null;
    target.mFloderListView = null;

    this.target = null;
  }
}
