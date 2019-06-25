// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.photopicker;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import java.lang.IllegalStateException;
import java.lang.Override;

public class PhotoPickerActivity02_ViewBinding<T extends PhotoPickerActivity02> implements Unbinder {
  protected T target;

  private View view2131690396;

  private View view2131690397;

  private View view2131690399;

  @UiThread
  public PhotoPickerActivity02_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.titleBar = Utils.findRequiredViewAsType(source, R.id.title_bar, "field 'titleBar'", TitleBar.class);
    target.mGridView = Utils.findRequiredViewAsType(source, R.id.photo_gridview, "field 'mGridView'", GridView.class);
    view = Utils.findRequiredView(source, R.id.button_preview, "field 'buttonPreview' and method 'onViewClicked'");
    target.buttonPreview = Utils.castView(view, R.id.button_preview, "field 'buttonPreview'", TextView.class);
    view2131690396 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.cb, "field 'cb' and method 'onViewClicked'");
    target.cb = Utils.castView(view, R.id.cb, "field 'cb'", CheckBox.class);
    view2131690397 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.tvSize = Utils.findRequiredViewAsType(source, R.id.tv_size, "field 'tvSize'", TextView.class);
    view = Utils.findRequiredView(source, R.id.btn_send, "field 'btnSend' and method 'onViewClicked'");
    target.btnSend = Utils.castView(view, R.id.btn_send, "field 'btnSend'", TextView.class);
    view2131690399 = view;
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
    target.mGridView = null;
    target.buttonPreview = null;
    target.cb = null;
    target.tvSize = null;
    target.btnSend = null;

    view2131690396.setOnClickListener(null);
    view2131690396 = null;
    view2131690397.setOnClickListener(null);
    view2131690397 = null;
    view2131690399.setOnClickListener(null);
    view2131690399 = null;

    this.target = null;
  }
}
