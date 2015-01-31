package com.zzt.inbox.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.zzt.library.R;

/**
 * Created by zzt on 2015/1/31.
 */
public class InboxLayoutScrollView extends InboxLayoutBase<ScrollView> {

    private ScrollView mScrollView;

    public InboxLayoutScrollView(Context context) {
        this(context, null);
    }

    public InboxLayoutScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InboxLayoutScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected ScrollView createDragableView(Context context, AttributeSet attrs) {
        mScrollView = new ScrollView(context);
        mScrollView.setId(R.id.scrollview);
        return mScrollView;
    }

    @Override
    protected boolean isReadyForDragStart() {
        return mDragableView.getScrollY() == 0;
    }

    @Override
    protected boolean isReadyForDragEnd() {
        View scrollViewChild = mDragableView.getChildAt(0);
        if (null != scrollViewChild) {
            return mDragableView.getScrollY() >= (scrollViewChild.getHeight() - getHeight());
        }
        return false;
    }
}
