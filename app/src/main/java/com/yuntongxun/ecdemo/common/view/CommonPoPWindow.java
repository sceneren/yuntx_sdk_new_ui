package com.yuntongxun.ecdemo.common.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.yuntongxun.ecdemo.R;


/**
 * 自定义的popwindow.
 */
public class CommonPoPWindow extends PopupWindow {

    private View mMenuView;
    private PopCallback popCallback;


    public CommonPoPWindow(Context context, PopCallback callback, int layout) {
        super(context);
        this.popCallback = callback;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mMenuView = inflater.inflate(layout, null);
        //把mMenuView回调出去，供使用
        popCallback.getPopWindowChildView(mMenuView);

        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);

        // 设置SelectPicPopupWindow弹出窗体动画效果


		   this.setAnimationStyle(R.style.AnimTools);
        // 实例化一个ColorDrawable颜色为透明
        ColorDrawable dw = new ColorDrawable(00000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        setOutsideTouchable(true);
//        mMenuView.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (y < height) {
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });

    }

    /**
     * popwindow回调填充接口
     *
     * @author zhuleike
     */
    public interface PopCallback {

        /**
         * 从popwindow获取子View
         *
         * @param mMenuView
         */
        View getPopWindowChildView(View mMenuView);
    }

}
