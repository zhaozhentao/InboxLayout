package com.zzt.inboxlayout.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2015/1/24.
 */
public class EmptyView extends View {

    private int mHeight = 0;
    private int iScrollY;
    private ScrollView mScrollView;
    private int fatherScrollViewHeight;
    private ViewGroup.LayoutParams layoutParams;
    private AnimatorSet animatorSet = new AnimatorSet();
    private ObjectAnimator mHeightAnimator;
    private ObjectAnimator mScrollYAnimator;
    private Interpolator mInterpolator = new DecelerateInterpolator();
    private int beginScrollY;
    private boolean shouldDoOnStop;

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setFatherScrollView(ScrollView scrollView){
        mScrollView = scrollView;
    }

    public void startAnim(){
        if(animatorSet.isRunning()){
            animatorSet.cancel();
        }

        if(mHeightAnimator == null){
            mHeightAnimator = ObjectAnimator.ofInt(this, aHeight, 0, mScrollView.getHeight());
            mScrollYAnimator = ObjectAnimator.ofInt(this, aScrollY, mScrollView.getScrollY(), this.getTop());
            mHeightAnimator.setDuration(200);
            mScrollYAnimator.setDuration(200);
            animatorSet.playTogether(mHeightAnimator, mScrollYAnimator);
            animatorSet.setInterpolator(mInterpolator);
        }
        beginScrollY = mScrollView.getScrollY();
        mHeightAnimator.setIntValues(0, mScrollView.getHeight());
        mScrollYAnimator.setIntValues(beginScrollY, this.getTop());
        shouldDoOnStop = true;
        animatorSet.start();
    }

    public void rollBackAnim(){
        shouldDoOnStop = false;
        if(animatorSet.isRunning()){
            animatorSet.cancel();
        }
        mHeightAnimator.setIntValues( mScrollView.getHeight(), 0);
        mScrollYAnimator.setIntValues(mScrollView.getScrollY(), beginScrollY);
        animatorSet.start();
    }

    private void anim(){
        if(null==layoutParams) {
            layoutParams = getLayoutParams();
        }
        layoutParams.height = mHeight;
        setLayoutParams(layoutParams);
    }

    private void scrollAnim(){
        mScrollView.scrollTo(0, iScrollY);
    }

    Property<EmptyView, Integer> aScrollY = new Property<EmptyView, Integer>(Integer.class, "iScrollY"){
        @Override
        public Integer get(EmptyView object) {
            return object.iScrollY;
        }
        @Override
        public void set(EmptyView object, Integer value) {
            object.iScrollY = value;
            scrollAnim();
        }

    };

    Property<EmptyView, Integer> aHeight = new Property<EmptyView, Integer>(Integer.class, "mHeight") {
        @Override
        public Integer get(EmptyView object) {
            return object.mHeight;
        }
        @Override
        public void set(EmptyView object, Integer value) {
            object.mHeight = value;
            anim();
            if( mScrollView.getHeight() == value){
                if(mOnAnimStopListener!=null&&shouldDoOnStop)
                    mOnAnimStopListener.onAnimStop();
            }
        }
    };

    private OnAnimStopListener mOnAnimStopListener;
    public void setOnAnimStopListener( OnAnimStopListener onAnimStopListener){
        mOnAnimStopListener = onAnimStopListener;
    }
    public interface OnAnimStopListener{
        public void onAnimStop();
    }
}
