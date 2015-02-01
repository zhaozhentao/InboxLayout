package com.zzt.inbox.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by zzt on 2015/2/1.
 */
public class InboxBackgroundListView extends ListView {

    public InboxBackgroundListView(Context context) {
        this(context, null);
    }

    public InboxBackgroundListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InboxBackgroundListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
