package com.zzt.inboxlayout.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.zzt.inbox.interfaces.OnDragStateChangeListener;
import com.zzt.inbox.widget.InboxLayoutBase;
import com.zzt.inbox.widget.InboxLayoutScrollView;
import com.zzt.inbox.widget.InboxBackgroundScrollView;
import com.zzt.inboxlayout.R;

/**
 * Created by zzt on 2015/1/31.
 */
public class ScrollViewActivity extends ActionBarActivity {
    InboxLayoutScrollView inboxLayoutScrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrollview_activity);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xdd000000));

        final InboxBackgroundScrollView inboxBackgroundScrollView = (InboxBackgroundScrollView)findViewById(R.id.scroll);
        inboxLayoutScrollView = (InboxLayoutScrollView)findViewById(R.id.inboxlayout);
        inboxLayoutScrollView.seBackgroundScrollView(inboxBackgroundScrollView);//绑定scrollview
        inboxLayoutScrollView.setCloseDistance(50);
        inboxLayoutScrollView.setOnDragStateChangeListener(new OnDragStateChangeListener() {
            @Override
            public void dragStateChange(InboxLayoutBase.DragState state) {
                switch (state) {
                    case CANCLOSE:
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff5e5e5e));
                        getSupportActionBar().setTitle("back");
                        break;
                    case CANNOTCLOSE:
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xdd000000));
                        getSupportActionBar().setTitle("InboxLayout");
                        break;
                }
            }
        });

        init();
    }


    private void init() {
        final LinearLayout dingdan = (LinearLayout)findViewById(R.id.ding_dan);
        dingdan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayoutScrollView.openWithAnim(dingdan);
            }
        });

        final LinearLayout yuding = (LinearLayout)findViewById(R.id.yuding);
        yuding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayoutScrollView.openWithAnim(yuding);
            }
        });

        final LinearLayout tuijian = (LinearLayout)findViewById(R.id.tuijian);
        tuijian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayoutScrollView.openWithAnim(tuijian);
            }
        });

        final LinearLayout member = (LinearLayout)findViewById(R.id.member);
        member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayoutScrollView.openWithAnim(member);
            }
        });

        final LinearLayout choujiang = (LinearLayout)findViewById(R.id.choujiang);
        choujiang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayoutScrollView.openWithAnim(choujiang);
            }
        });

        final LinearLayout diyongquan = (LinearLayout)findViewById(R.id.diyongquan);
        diyongquan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayoutScrollView.openWithAnim(diyongquan);
            }
        });
    }

}
