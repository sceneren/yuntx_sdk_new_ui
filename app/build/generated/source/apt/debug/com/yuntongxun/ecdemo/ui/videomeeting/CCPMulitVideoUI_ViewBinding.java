// Generated code from Butter Knife. Do not modify!
package com.yuntongxun.ecdemo.ui.videomeeting;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.yuntongxun.ecdemo.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class CCPMulitVideoUI_ViewBinding<T extends CCPMulitVideoUI> implements Unbinder {
  protected T target;

  @UiThread
  public CCPMulitVideoUI_ViewBinding(T target, View source) {
    this.target = target;

    target.subviTopLeft = Utils.findRequiredViewAsType(source, R.id.subvi_top_left, "field 'subviTopLeft'", SubVideoSurfaceView.class);
    target.subviTopMidlet = Utils.findRequiredViewAsType(source, R.id.subvi_top_midlet, "field 'subviTopMidlet'", SubVideoSurfaceView.class);
    target.subviTopRight = Utils.findRequiredViewAsType(source, R.id.subvi_top_right, "field 'subviTopRight'", SubVideoSurfaceView.class);
    target.subviBotomLeft = Utils.findRequiredViewAsType(source, R.id.subvi_botom_left, "field 'subviBotomLeft'", SubVideoSurfaceView.class);
    target.subviBotomMidle = Utils.findRequiredViewAsType(source, R.id.subvi_botom_midle, "field 'subviBotomMidle'", SubVideoSurfaceView.class);
    target.subviBotomRight = Utils.findRequiredViewAsType(source, R.id.subvi_botom_right, "field 'subviBotomRight'", SubVideoSurfaceView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.subviTopLeft = null;
    target.subviTopMidlet = null;
    target.subviTopRight = null;
    target.subviBotomLeft = null;
    target.subviBotomMidle = null;
    target.subviBotomRight = null;

    this.target = null;
  }
}
