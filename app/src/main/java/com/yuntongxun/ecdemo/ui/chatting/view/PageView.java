package com.yuntongxun.ecdemo.ui.chatting.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Scroller;

import com.yuntongxun.ecdemo.common.utils.LogUtil;

public class PageView extends HorizontalScrollView {  
    private int mBaseScrollX;//滑动基线。也就是点击并滑动之前的x值，以此值计算相对滑动距离。  
    private int mScreenWidth;  
    private int mScreenHeight;  
      
    private LinearLayout mContainer;  
    private boolean flag;  
    private int mPageCount;//页面数量  
      
    private int mScrollX = 200;//滑动多长距离翻页
    private RadioGroup radioGroup;
    private float distance;
    private final Scroller mScroller;

    public PageView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
  
        DisplayMetrics dm = context.getApplicationContext().getResources()  
                .getDisplayMetrics();  
        mScreenWidth = dm.widthPixels;  
        mScreenHeight = dm.heightPixels;

        mScroller = new Scroller(context);


    }  
      
    /** 
     * 添加一个页面到最后。 
     * @param page 
     */  
    public void addPage(View page) {  
        addPage(page, -1);  
    }  
      
    /** 
     * 添加一个页面。 
     * @param page 
     */  
    public void addPage(View page, int index) {  
        if(!flag) {  
            mContainer = (LinearLayout) getChildAt(0);  
            flag = true;  
        }  
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mScreenWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);  
        if(index == -1) {  
            mContainer.addView(page, params);  
        } else {  
            mContainer.addView(page, index, params);  
        }  
        mPageCount++;  
    }  
      
    /** 
     * 移除一个页面。 
     * @param index 
     */  
    public void removePage(int index) {  
        if(mPageCount < 1) {  
            return;  
        }  
        if(index<0 || index>mPageCount-1) {  
            return;  
        }  
        mContainer.removeViewAt(index);  
        mPageCount--;  
    }  
      
    /** 
     * 移除所有的页面 
     */  
    public void removeAllPages() {  
        if(mPageCount > 0) {  
            mContainer.removeAllViews();  
        }  
    }  
      
    /** 
     * 获取页面数量 
     * @return 
     */  
    public int getPageCount() {  
        return mPageCount;  
    }  
      
    /** 
     * 获取相对滑动位置。由右向左滑动，返回正值；由左向右滑动，返回负值。 
     * @return 
     */  
    private int getBaseScrollX() {  
        return getScrollX() - mBaseScrollX;  
    }  
      
    /** 
     * 使相对于基线移动x距离。 
     * @param x x为正值时右移；为负值时左移。 
     */  
    private void baseSmoothScrollTo(int x) {  
    	
    	
        smoothScrollTo(x + mBaseScrollX, 0);  
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent ev) {  
        int action = ev.getAction();  
        LogUtil.e("bb","pageview move");
        if(CCPChattingFooter2.isRecodering){
        	return false;
        }
        switch (action) {  
        case MotionEvent.ACTION_UP:  
            int scrollX = getBaseScrollX();  
            //左滑，大于一半，移到下一页  
            if (scrollX > mScrollX) {  
                baseSmoothScrollTo(mScreenWidth);
                radiogSmoothScrollTo(50);

                mBaseScrollX += mScreenWidth;  
            }   
            //左滑，不到一半，返回原位  
            else if (scrollX > 0) {
                baseSmoothScrollTo(0);
                radiogSmoothScrollTo(0);
            }   
            //右滑，不到一半，返回原位  
            else if(scrollX > -mScrollX) {
                baseSmoothScrollTo(0);
                radiogSmoothScrollTo(0);
            }   
            //右滑，大于一半，移到下一页  
            else {  
                baseSmoothScrollTo(-mScreenWidth);
                radiogSmoothScrollTo(-50);
                mBaseScrollX -= mScreenWidth;

            }  
            return true;  
        }  
        return super.onTouchEvent(ev);  
    }




    public void radiogSmoothScrollTo(int destX ){

        if (radioGroup !=null){
            //设置mScroller的滚动偏移量
            int scrollX = getScrollX();
            int dx = destX - scrollX;
            mScroller.startScroll(scrollX,0,dx,0,1000);
            invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
        }
    }


    public void setIndicrot(RadioGroup radioGroup) {
        this.radioGroup = radioGroup;
        if (radioGroup !=null){
            distance = radioGroup.getChildAt(0).getX() - radioGroup.getChildAt(1).getX();

        }

    }


    @Override
    public void computeScroll() {

        //先判断mScroller滚动是否完成,true说明滚动尚未完成
        if (mScroller.computeScrollOffset()) {

            //这里调用View的scrollTo()完成实际的滚动
            radioGroup.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }


//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//    	
//    	if(CCPChattingFooter2.isRecodering){
//    		return true;
//    	}
//    	return false;
//    }
    
    
}  