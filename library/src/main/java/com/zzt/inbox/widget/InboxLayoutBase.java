package com.zzt.inbox.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.zzt.inbox.interfaces.OnDragStateChangeListener;

/**
 * Created by zzt on 2015/1/19.
 */
public abstract class  InboxLayoutBase <T extends View> extends FrameLayout {

    private View topView;
    public final static int LINEARPARAMS = 1;
    public final static int RELATIVEPARAMS = 2;
    private int params = 0;
    private float mLastMotionX, mLastMotionY;
    private float mInitialMotionX, mInitialMotionY;
    protected T mDragableView;
    private int mTouchSlop;
    private int ANIMDURA = 300;
    private int closeDistance;
    private boolean mIsBeingDragged = false;
    private boolean mFilterTouchEvents = true;
    private boolean shouldRollback;
    private Mode mMode = Mode.getDefault();
    private OnDragStateChangeListener onDragStateChangeListener;
    private Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            closeWithAnim();
        }
    };

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
    }

    private DragState dragState = DragState.CANNOTCLOSE;
    public static enum DragState{
        CANCLOSE(0x0),
        CANNOTCLOSE(0X1);
        DragState(int value){}
    };

    public InboxLayoutBase(Context context) {
        this(context, null);
    }

    public InboxLayoutBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InboxLayoutBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
        closeDistance = dp2px(60);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        mHeightAnimator = ObjectAnimator.ofInt(this, aHeight, 0, 0);
        mScrollYAnimator = ObjectAnimator.ofInt(this, aScrollY, 0, 0);
        mHeightAnimator.setDuration(ANIMDURA);
        mScrollYAnimator.setDuration(ANIMDURA);
        animatorSet.playTogether(mHeightAnimator, mScrollYAnimator);
        animatorSet.setInterpolator(mInterpolator);

        mDragableView = createDragableView(context, attrs);
        addDragableView(mDragableView);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mScrollView.getHeight()!=0 && mScrollView.getHeight()>mScrollView.getChildAt(0).getHeight()){
            View view = mScrollView.getChildAt(0).findViewWithTag("empty_view");
            if(view == null)
                return ;
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = mScrollView.getHeight() - mScrollView.getChildAt(0).getHeight();
            view.setLayoutParams(layoutParams);
            view.requestLayout();
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
                        if (diff >= 1f && isReadyForDragStart()) {
                            mLastMotionY = y;
                            mLastMotionX = x;
                            mIsBeingDragged = true;//这里返回了true 本次触摸就本类拦截了, 接下来的触摸事件由本类的onTouchEvent处理
                            //mCurrentMode = Mode.PULL_FROM_START;
                            if (mMode == Mode.BOTH) {
                                mCurrentMode = Mode.PULL_FROM_START;
                            }
                        }else if (diff <= -1f && isReadyForDragEnd()) {
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

    private boolean isReadyForPull(){
        switch(mMode){
            case PULL_FROM_START:
                return isReadyForDragStart();
            case PULL_FROM_END:
                return isReadyForDragEnd();
            case BOTH:
                return isReadyForDragEnd()||isReadyForDragStart();
            default:
                return false;
        }
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
                        prevOffSetY = 0;//清零
                        return true;
                    }
                    return true;
                }
                break;
            }
        }
        return false;
    }

    static final float FRICTION = 2.0f;
    private void pullEvent() {
        final int newScrollValue;
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

    private void addDragableView(T DragableView) {
        addView(DragableView,ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    protected abstract T createDragableView(Context context, AttributeSet attrs);

    protected abstract boolean isReadyForDragStart();

    protected abstract boolean isReadyForDragEnd();

    public final T getDragableView() {
        return mDragableView;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        Log.d("addview", "addView: " + child.getClass().getSimpleName());
        final T refreshableView = getDragableView();
        if(child == refreshableView){
            super.addView(child, index, params);
            return ;
        }

        if (refreshableView instanceof ViewGroup) {
            ((ViewGroup) refreshableView).addView(child, index, params);
        } else {
            throw new UnsupportedOperationException("Dragable View is not a ViewGroup so can't addView");
        }
    }


    private int realOffsetY;
    private int prevOffSetY = 0;
    private int dy;
    private int moveContent(int offsetY){

        realOffsetY = (int)(offsetY/1.4f);
        scrollTo(0, realOffsetY);
        dy = prevOffSetY - realOffsetY;
        prevOffSetY = realOffsetY;
        mScrollView.scrollBy(0, -dy);

        if(realOffsetY<-closeDistance||realOffsetY>closeDistance&&onDragStateChangeListener!=null){
            onDragStateChangeListener.dragStateChange(DragState.CANCLOSE);
            dragState = DragState.CANCLOSE;
        }else if(dragState == DragState.CANCLOSE && realOffsetY<closeDistance && realOffsetY >-closeDistance){
            onDragStateChangeListener.dragStateChange(DragState.CANNOTCLOSE);
            dragState = DragState.CANNOTCLOSE;
        }
        /*
        * Draw Shadow
        * */
        switch(mCurrentMode){
            case PULL_FROM_END:
                mScrollView.drawBottomShadow(mScrollView.getScrollY()+mScrollView.getHeight()-realOffsetY,
                        mScrollView.getScrollY()+mScrollView.getHeight(), 60);
                break;
            case PULL_FROM_START:
            default:
                mScrollView.drawTopShadow(mScrollView.getScrollY(), -realOffsetY, 60);
                break;
        }
        mScrollView.invalidate();
        return realOffsetY;
    }

    private SmoothScrollRunnable mCurrentSmoothScrollRunnable;
    private Interpolator mScrollAnimationInterpolator = new DecelerateInterpolator();
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
            if (mStartTime == -1) {
                mStartTime = System.currentTimeMillis();
            } else {
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
            }
            // keep going...
            if (mContinueRunning && mScrollToY != mCurrentY) {
                InboxLayoutBase.this.postDelayed(this, 16);
            } else {
                //Finish
            }
        }
        public void stop() {
            mContinueRunning = false;
            removeCallbacks(this);
        }
    }

    private final void smoothScrollTo(int newScrollValue, long duration, long delayMillis) {
        if (null != mCurrentSmoothScrollRunnable) {
            mCurrentSmoothScrollRunnable.stop();
        }
        final int oldScrollValue;
        oldScrollValue = getScrollY();

        if(oldScrollValue<-closeDistance||oldScrollValue>closeDistance){
            setVisibility(View.INVISIBLE);
            postDelayed(closeRunnable, 100);
            shouldRollback = false;
        }else{
            shouldRollback = true;
        }

        if (oldScrollValue != newScrollValue) {
            mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(oldScrollValue, newScrollValue, duration);
            if (delayMillis > 0) {
                postDelayed(mCurrentSmoothScrollRunnable, delayMillis);
            } else {
                post(mCurrentSmoothScrollRunnable);
            }
        }
    }

    public void setBackgroundScrollView(InboxBackgroundScrollView scrollView){
        mScrollView = scrollView;
    }

    public void setOnDragStateChangeListener(OnDragStateChangeListener listener){
        onDragStateChangeListener = listener;
    }

    public void setCloseDistance(int dp){
        closeDistance = dp2px(dp);
    }

    private int mHeight = 0;
    private int iScrollY;
    private InboxBackgroundScrollView mScrollView;
    private ViewGroup.LayoutParams layoutParams;
    private LinearLayout.LayoutParams linearLayoutParams;
    private RelativeLayout.LayoutParams relativeLayoutParams;
    private AnimatorSet animatorSet = new AnimatorSet();
    private ObjectAnimator mHeightAnimator;
    private ObjectAnimator mScrollYAnimator;
    private Interpolator mInterpolator = new DecelerateInterpolator();
    private int beginScrollY, endScrollY;
    private int beginBottomMargin;
    private int heightRange;
    private boolean IsStartAnim = false;
    private Runnable showRunnable = new Runnable(){
        @Override
        public void run() {
            setVisibility(View.VISIBLE);
        }
    };

    public void openWithAnim(View topView) {
        this.topView = topView;
        /*
         *  eat the touch event when anim start
         */
        mScrollView.setTouchable(false);

        layoutParams = topView.getLayoutParams();
        if(layoutParams instanceof LinearLayout.LayoutParams){
            params = LINEARPARAMS;
            linearLayoutParams = (LinearLayout.LayoutParams)layoutParams;
            heightRange = linearLayoutParams.bottomMargin;
        }else if(layoutParams instanceof RelativeLayout.LayoutParams){
            params = RELATIVEPARAMS;
            relativeLayoutParams = (RelativeLayout.LayoutParams)layoutParams;
            heightRange = relativeLayoutParams.bottomMargin;
        }else{
            Log.e("error", "topView's parent should be linearlayout");
            return ;
        }

        IsStartAnim = true;
        mScrollView.needToDrawShadow = true;
        beginBottomMargin = heightRange;
        topView.setAlpha(0);

        if(animatorSet.isRunning()){
            animatorSet.cancel();
        }

        int scrollViewHeight = mScrollView.getHeight();
        endScrollY = topView.getTop();
        beginScrollY = mScrollView.getScrollY();
        heightRange = scrollViewHeight - topView.getHeight();
        mHeightAnimator.setIntValues(beginBottomMargin, heightRange);
        mScrollYAnimator.setIntValues(beginScrollY, endScrollY);
        mScrollView.drawTopShadow(beginScrollY, endScrollY - beginScrollY, 0);
        mScrollView.drawBottomShadow(topView.getBottom(), beginScrollY + scrollViewHeight, 0);
        animatorSet.start();
        postDelayed(showRunnable, ANIMDURA+10);//将顶层的view显示出来
    }

    public void closeWithAnim(){
        topView.setAlpha(1);
        mScrollView.needToDrawSmallShadow = false;
        IsStartAnim = false;
        dragState = DragState.CANNOTCLOSE;
        if(onDragStateChangeListener!=null){
            onDragStateChangeListener.dragStateChange(dragState);
        }
        if(animatorSet.isRunning()){
            animatorSet.cancel();
        }

        mHeightAnimator.setIntValues(heightRange, beginBottomMargin);
        mScrollYAnimator.setIntValues(mScrollView.getScrollY(), beginScrollY);
        animatorSet.start();
    }

    private void heightChangeAnim(){
        switch (params){
            case LINEARPARAMS:
                linearLayoutParams.bottomMargin = mHeight;
                break;
            case RELATIVEPARAMS:
                relativeLayoutParams.bottomMargin = mHeight;
                break;
        }
        topView.setLayoutParams(layoutParams);
    }

    private int alpha;
    private void scrollYChangeAnim(){
        alpha = 60 * mHeight/heightRange;
        mScrollView.scrollTo(0, iScrollY);
        mScrollView.drawTopShadow(iScrollY, topView.getTop()-iScrollY, alpha);
        mScrollView.drawBottomShadow(topView.getBottom() + mHeight, mScrollView.getScrollY() + mScrollView.getHeight(), alpha);
        mScrollView.invalidate();
    }

    Property<InboxLayoutBase, Integer> aHeight = new Property<InboxLayoutBase, Integer>(Integer.class, "mHeight") {
        @Override
        public Integer get(InboxLayoutBase object) {
            return object.mHeight;
        }
        @Override
        public void set(InboxLayoutBase object, Integer value) {
            object.mHeight = value;
            heightChangeAnim();
            if(value == heightRange && IsStartAnim){
                //Open Anim Stop
                mScrollView.needToDrawSmallShadow = true;
            }else if(value == beginBottomMargin && !IsStartAnim){
                //Close Anim Stop
                /*
                 * enable touch event when top view close
                 */
                mScrollView.setTouchable(true);
            }
        }
    };

    Property<InboxLayoutBase, Integer> aScrollY = new Property<InboxLayoutBase, Integer>(Integer.class, "iScrollY"){
        @Override
        public Integer get(InboxLayoutBase object) {
            return object.iScrollY;
        }
        @Override
        public void set(InboxLayoutBase object, Integer value) {
            object.iScrollY = value;
            scrollYChangeAnim();
        }
    };

    private int dp2px(float dp){
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

}
