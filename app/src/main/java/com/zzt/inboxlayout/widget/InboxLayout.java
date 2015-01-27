package com.zzt.inboxlayout.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * Created by zzt on 2015/1/19.
 */
public class InboxLayout extends FrameLayout {

    protected static final int MAX_MENU_OVERLAY_ALPHA = 185;
    private View topView;
    private boolean mIsBeingDragged = false;
    private float mLastMotionX, mLastMotionY;
    private float mInitialMotionX, mInitialMotionY;
    private ListView mRefreshableView;
    private int mTouchSlop;
    private boolean mFilterTouchEvents = true;
    private Mode mMode = Mode.getDefault();
    private boolean shouldRollback;
    protected Drawable mMenuOverlay = new ColorDrawable(0xff000000);;

    public InboxLayout(Context context) {
        this(context, null);
    }

    public InboxLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InboxLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
            mIsBeingDragged = false;//返回false 此次触摸事件会继续向深层的view传递 mIsBeingDragged false listview没有被拉下
            return false;
        }

        if(action != MotionEvent.ACTION_DOWN && mIsBeingDragged){
            return true;//返回true 触摸事件不再传递
        }
        switch (action){
            case MotionEvent.ACTION_DOWN:
                if (isReadyForPull()) {
                    mLastMotionY = mInitialMotionY = ev.getY();
                    mLastMotionX = mInitialMotionX = ev.getX();
                    mIsBeingDragged = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isReadyForPull()){
                    final float y = ev.getY(), x = ev.getX();
                    final float diff, oppositeDiff, absDiff;

                    diff = y - mLastMotionY;
                    oppositeDiff = x - mLastMotionX;

                    absDiff = Math.abs(diff);

                    if (absDiff > mTouchSlop && (!mFilterTouchEvents || absDiff > Math.abs(oppositeDiff))) {
                        if (mMode.showHeaderLoadingLayout() && diff >= 1f && isReadyForPullStart()) {
                            mLastMotionY = y;
                            mLastMotionX = x;
                            mIsBeingDragged = true;//这里返回了true 本次触摸就本类拦截了, 接下来的触摸事件由本类的onTouchEvent处理
                            //mCurrentMode = Mode.PULL_FROM_START;
                            if (mMode == Mode.BOTH) {
                                mCurrentMode = Mode.PULL_FROM_START;
                            }
                        }else if (mMode.showFooterLoadingLayout() && diff <= -1f && isReadyForPullEnd()) {
                            mLastMotionY = y;
                            mLastMotionX = x;
                            mIsBeingDragged = true;
                            if (mMode == Mode.BOTH) {
                                mCurrentMode = Mode.PULL_FROM_END;
                            }
                        }
                    }
                }
                break;
        }
        return mIsBeingDragged;
    }

    private Mode mCurrentMode;
    private static enum Mode{
        DISABLED(0x0),
        PULL_FROM_START(0x1),
        PULL_FROM_END(0x2),
        BOTH(0x3);
        private int mIntValue;
        Mode(int modeInt) {
            mIntValue = modeInt;
        }
        static Mode getDefault() {
            return BOTH;
        }
        public boolean showHeaderLoadingLayout() {
            return this == PULL_FROM_START || this == BOTH;
        }
        public boolean showFooterLoadingLayout() {
            return this == PULL_FROM_END || this == BOTH;
        }

    }

    private boolean isReadyForPull(){
        if(mRefreshableView == null){
            mRefreshableView = (ListView)getChildAt(0);
        }

        switch(mMode){
            case PULL_FROM_START:
                return isReadyForPullStart();
            case PULL_FROM_END:
                return isReadyForPullEnd();
            case BOTH:
                return isReadyForPullEnd()||isReadyForPullStart();
            default:
                return false;
        }
    }

    private boolean isReadyForPullStart(){
        final Adapter adapter = mRefreshableView.getAdapter();
        if(null == adapter || adapter.isEmpty()){
            return true;
        }else{
            if( mRefreshableView.getFirstVisiblePosition()<=1 ){
                final View firstVisibleChild = mRefreshableView.getChildAt(0);
                if(firstVisibleChild != null){
                    return firstVisibleChild.getTop() >= mRefreshableView.getTop();
                }
            }
        }
        return false;
    }

    private boolean isReadyForPullEnd(){
        final Adapter adapter = mRefreshableView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }
        else {
            final int lastItemPosition = mRefreshableView.getCount() - 1;
            final int lastVisiblePosition = mRefreshableView.getLastVisiblePosition();
            if (lastVisiblePosition >= lastItemPosition - 1) {
                final int childIndex = lastVisiblePosition - mRefreshableView.getFirstVisiblePosition();
                final View lastVisibleChild = mRefreshableView.getChildAt(childIndex);
                if (lastVisibleChild != null) {
                    return lastVisibleChild.getBottom() <= mRefreshableView.getBottom();
                }
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }

        switch(event.getAction()){
            case MotionEvent.ACTION_MOVE:
                if(mIsBeingDragged){
                    mLastMotionY = event.getY();
                    mLastMotionX = event.getX();
                    pullEvent();
                    return true;
                }
                break;
            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPull()) {
                    mLastMotionY = mInitialMotionY = event.getY();
                    mLastMotionX = mInitialMotionX = event.getX();
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    if(true){
                        smoothScrollTo(0, 200, 0);
                        PrevOffSetY = 0;//清零
                        return true;
                    }
                    return true;
                }
                break;
            }
        }
        return false;
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        final int offsetPixels = (int)realOffsetY;
        if(offsetPixels!=0){
                //drawOverlay(canvas);
        }
    }

    protected void drawOverlay(Canvas canvas){
        final int width = getWidth();
        final int height= getHeight();
        final int offsetPixels=(int)this.realOffsetY;
        final float openRatio = Math.abs(this.realOffsetY)/200;
        this.mMenuOverlay.setBounds(0,0, width, -offsetPixels);
        this.mMenuOverlay.setAlpha((int)(MAX_MENU_OVERLAY_ALPHA*(1.f-openRatio)));
        this.mMenuOverlay.draw(canvas);
    }

    static final float FRICTION = 2.0f;
    private void pullEvent() {
        final int newScrollValue;
        final int itemDimension;
        final float initialMotionValue, lastMotionValue;

        initialMotionValue = mInitialMotionY;//vertical 就这样计算
        lastMotionValue = mLastMotionY;

        switch(mCurrentMode){
            case PULL_FROM_END:
                newScrollValue = Math.round(Math.max(initialMotionValue - lastMotionValue, 0) / FRICTION);
                break;
            case PULL_FROM_START:
            default:
                newScrollValue = Math.round(Math.min(initialMotionValue - lastMotionValue, 0) / FRICTION);
                break;
        }
        moveContent(newScrollValue);
    }

    private int realOffsetY;
    private int PrevOffSetY = 0;
    private int dy;
    private int moveContent(int offsetY){
        realOffsetY = (int)(offsetY/1.4f);
        scrollTo(0, realOffsetY);
        dy = PrevOffSetY - realOffsetY;
        PrevOffSetY = realOffsetY;
        mScrollView.scrollBy(0, -dy);
        return realOffsetY;
    }

    private SmoothScrollRunnable mCurrentSmoothScrollRunnable;
    private final void smoothScrollTo(int newScrollValue, long duration, long delayMillis) {
        if (null != mCurrentSmoothScrollRunnable) {
            mCurrentSmoothScrollRunnable.stop();
        }
        final int oldScrollValue;
        oldScrollValue = getScrollY();

        if(oldScrollValue<-200||oldScrollValue>200){
            setVisibility(View.INVISIBLE);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    rollBackAnim();
                }
            }, 100);
            shouldRollback = false;
        }else{
            shouldRollback = true;
        }

        if (oldScrollValue != newScrollValue) {
            if (null == mScrollAnimationInterpolator) {
                mScrollAnimationInterpolator = new DecelerateInterpolator();
            }
            mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(oldScrollValue, newScrollValue, duration);

            if (delayMillis > 0) {
                postDelayed(mCurrentSmoothScrollRunnable, delayMillis);
            } else {
                post(mCurrentSmoothScrollRunnable);
            }
        }
    }

    private Interpolator mScrollAnimationInterpolator;
    final class SmoothScrollRunnable implements Runnable {
        private final Interpolator mInterpolator;
        private final int mScrollToY;
        private final int mScrollFromY;
        private final long mDuration;

        private boolean mContinueRunning = true;
        private long mStartTime = -1;
        private int mCurrentY = -1;
        private int PrevY = 0;
        private int offsetY = 0;

        public SmoothScrollRunnable(int fromY, int toY, long duration) {
            mScrollFromY = fromY;
            mScrollToY = toY;
            mInterpolator = mScrollAnimationInterpolator;
            mDuration = duration;
        }

        @Override
        public void run() {
            /**
             * Only set mStartTime if this is the first time we're starting,
             * else actually calculate the Y delta
             */
            if (mStartTime == -1) {
                mStartTime = System.currentTimeMillis();
            } else {
                /**
                 * We do do all calculations in long to reduce software float
                 * calculations. We use 1000 as it gives us good accuracy and
                 * small rounding errors
                 */
                long normalizedTime = (1000 * (System.currentTimeMillis() - mStartTime)) / mDuration;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaY = Math.round((mScrollFromY - mScrollToY)
                        * mInterpolator.getInterpolation(normalizedTime / 1000f));
                mCurrentY = mScrollFromY - deltaY;
                if(PrevY == 0){ /*the PrevY will be 0 at first time */
                    PrevY = mScrollFromY;
                }
                offsetY = PrevY - mCurrentY;
                PrevY = mCurrentY;
                scrollTo(0, mCurrentY);
                if(shouldRollback) {
                    mScrollView.scrollBy(0, -offsetY);
                }
                //setHeaderScroll(mCurrentY);
            }
            // If we're not at the target Y, keep going...
            if (mContinueRunning && mScrollToY != mCurrentY) {
                InboxLayout.this.postDelayed(this, 16);
            } else {
                //Finish
            }
        }
        public void stop() {
            mContinueRunning = false;
            removeCallbacks(this);
        }
    }

    public void setParentScrollView(ScrollView scrollView){
        mScrollView = scrollView;
    }

    private int mHeight = 0;
    private int iScrollY;
    private ScrollView mScrollView;
    private ViewGroup.LayoutParams layoutParams;
    private AnimatorSet animatorSet = new AnimatorSet();
    private ObjectAnimator mHeightAnimator;
    private ObjectAnimator mScrollYAnimator;
    private Interpolator mInterpolator = new DecelerateInterpolator();
    private int beginScrollY;
    private boolean shouldDoOnStop;

    public void startAnim(View topView){
        this.topView = topView;
        layoutParams = topView.getLayoutParams();
        topView.setAlpha(0);
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
        mHeightAnimator.setIntValues(0, mScrollView.getHeight()-topView.getHeight());
        mScrollYAnimator.setIntValues(beginScrollY, topView.getTop());
        shouldDoOnStop = true;
        animatorSet.start();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.VISIBLE);
            }
        }, 200);
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

    Property<InboxLayout, Integer> aHeight = new Property<InboxLayout, Integer>(Integer.class, "mHeight") {
        @Override
        public Integer get(InboxLayout object) {
            return object.mHeight;
        }
        @Override
        public void set(InboxLayout object, Integer value) {
            object.mHeight = value;
            anim();
            if(0==value&&!shouldDoOnStop){
                topView.setAlpha(1);
            }
            if(mScrollView.getHeight() == value){
                if(mOnAnimStopListener!=null&&shouldDoOnStop)
                    mOnAnimStopListener.onAnimStop();
            }
        }
    };

    private void scrollAnim(){
        mScrollView.scrollTo(0, iScrollY);
    }

    Property<InboxLayout, Integer> aScrollY = new Property<InboxLayout, Integer>(Integer.class, "iScrollY"){
        @Override
        public Integer get(InboxLayout object) {
            return object.iScrollY;
        }
        @Override
        public void set(InboxLayout object, Integer value) {
            object.iScrollY = value;
            scrollAnim();
        }
    };

    private void anim(){
        ((LinearLayout.LayoutParams)layoutParams).bottomMargin = mHeight;
        topView.setLayoutParams(layoutParams);
    }

    private OnAnimStopListener mOnAnimStopListener;
    public void setOnAnimStopListener( OnAnimStopListener onAnimStopListener){
        mOnAnimStopListener = onAnimStopListener;
    }
    public interface OnAnimStopListener{
        public void onAnimStop();
    }
}
