// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.photopicker;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class FolderAdapter$ViewHolder_ViewBinding<T extends FolderAdapter.ViewHolder> implements Unbinder {
  protected T target;

  @UiThread
  public FolderAdapter$ViewHolder_ViewBinding(T target, View source) {
    this.target = target;

    target.ivIcon = Utils.findRequiredViewAsType(source, R.id.iv_icon, "field 'ivIcon'", ImageView.class);
    target.tvFileName = Utils.findRequiredViewAsType(source, R.id.tv_file_name, "field 'tvFileName'", TextView.class);
    target.tvPicNums = Utils.findRequiredViewAsType(source, R.id.tv_pic_nums, "field 'tvPicNums'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.ivIcon = null;
    target.tvFileName = null;
    target.tvPicNums = null;

    this.target = null;
  }
}
