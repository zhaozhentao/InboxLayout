package com.zzt.inbox.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by zzt on 2015/1/31.
 */
public class InboxLayoutListView extends InboxLayoutBase <ListView>{

    private ListView dragableView;

    public InboxLayoutListView(Context context) {
        this(context, null);
    }

    public InboxLayoutListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InboxLayoutListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected ListView createDragableView(Context context, AttributeSet attrs) {
        dragableView = new ListView(context);
        dragableView.setId(android.R.id.list);
        return dragableView;
    }

    public void setAdapter(ListAdapter adapter){
        dragableView.setAdapter(adapter);
    }

    protected boolean isReadyForDragStart(){
        final Adapter adapter = dragableView.getAdapter();
        if(null == adapter || adapter.isEmpty()){
            return true;
        }else{
            if( dragableView.getFirstVisiblePosition()<=1 ){
                final View firstVisibleChild = dragableView.getChildAt(0);
                if(firstVisibleChild != null){
                    return firstVisibleChild.getTop() >= dragableView.getTop();
                }
            }
        }
        return false;
    }

    protected boolean isReadyForDragEnd(){
        final Adapter adapter = dragableView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }
        else {
            final int lastItemPosition = dragableView.getCount() - 1;
            final int lastVisiblePosition = dragableView.getLastVisiblePosition();
            if (lastVisiblePosition >= lastItemPosition - 1) {
                final int childIndex = lastVisiblePosition - dragableView.getFirstVisiblePosition();
                final View lastVisibleChild = dragableView.getChildAt(childIndex);
                if (lastVisibleChild != null) {
                    return lastVisibleChild.getBottom() <= dragableView.getBottom();
                }
            }
        }
        return false;
    }

}
