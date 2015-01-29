package com.zzt.inbox.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by zzt on 2015/1/27.
 */
public class InboxScrollView extends ScrollView{

    public boolean needToDrawSmallShadow = false;
    public boolean needToDrawShadow = false;
    protected static final int MAX_MENU_OVERLAY_ALPHA = 185;
    private Drawable mTopSmallShadowDrawable;
    private Drawable mBottomSmallShadowDrawable;
    private Drawable mTopShadow = new ColorDrawable(0xff000000);
    private Drawable mBottomShadow = new ColorDrawable(0xff000000);

    public InboxScrollView(Context context) {
        this(context, null);
    }

    public InboxScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InboxScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTopSmallShadowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0x66303030, 0});
        mBottomSmallShadowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0x66303030, 0});
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        final int offsetPixels = (int)200;
        if(offsetPixels!=0){
            drawOverlay(canvas);
        }
    }

    protected void drawOverlay(Canvas canvas){
        final int width = getWidth();
        final int height= getHeight();
        final int offsetPixels= 200;//(int)this.realOffsetY;
        final float openRatio = Math.abs(100)/200;
        if(needToDrawShadow) {
            mTopShadow.draw(canvas);
            mBottomShadow.draw(canvas);
        }
        if(needToDrawSmallShadow){
            mTopSmallShadowDrawable.draw(canvas);
            mBottomSmallShadowDrawable.draw(canvas);
        }
    }

   public void drawShadow(int topTop, int topHeight, int bottomTop, int bottomHeight){
       //drawTopShadow(topTop, topHeight);
       //drawBottomShadow(bottomTop, bottomHeight);
       invalidate();
   }

    public void drawTopShadow(int top, int height, int alpha){
        mTopShadow.setBounds(0, top, getWidth(), top+height);
        mTopShadow.setAlpha(alpha);
        if(needToDrawSmallShadow) {
            mTopSmallShadowDrawable.setBounds(0, top + height - 50, getWidth(), top + height);
        }
    }

    public void drawBottomShadow(int top, int bottom, int alpha){
        mBottomShadow.setBounds(0, top, getWidth(), bottom);
        mBottomShadow.setAlpha(alpha);
        if(needToDrawSmallShadow) {
            mBottomSmallShadowDrawable.setBounds(0, top, getWidth(), top + 50);
        }
        invalidate();
    }

}
