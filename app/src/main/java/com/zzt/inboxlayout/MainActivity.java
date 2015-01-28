package com.zzt.inboxlayout;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.zzt.inboxlayout.widget.InboxLayout;
import com.zzt.inboxlayout.widget.InboxScrollView;


/**
 * Created by zzt on 2015/1/19.
 */
public class MainActivity extends ActionBarActivity {
    InboxLayout inboxLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final InboxScrollView inboxScrollView = (InboxScrollView)findViewById(R.id.scroll);
        inboxLayout = (InboxLayout)findViewById(R.id.myframelayout);
        ListView listView = (ListView)findViewById(R.id.list);
        inboxLayout.setBackgroundScrollView(inboxScrollView);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 20;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.item, null);
                return view;
            }
        });
        init();
    }


    private void init(){
        final LinearLayout dingdan = (LinearLayout)findViewById(R.id.ding_dan);
        dingdan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayout.startAnim(dingdan);
            }
        });

        final LinearLayout yuding = (LinearLayout)findViewById(R.id.yuding);
        yuding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayout.startAnim(yuding);
            }
        });

        final LinearLayout tuijian = (LinearLayout)findViewById(R.id.tuijian);
        tuijian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayout.startAnim(tuijian);
            }
        });

        final LinearLayout member = (LinearLayout)findViewById(R.id.member);
        member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayout.startAnim(member);
            }
        });

        final LinearLayout choujiang = (LinearLayout)findViewById(R.id.choujiang);
        choujiang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayout.startAnim(choujiang);
            }
        });

        final LinearLayout diyongquan = (LinearLayout)findViewById(R.id.diyongquan);
        diyongquan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inboxLayout.startAnim(diyongquan);
            }
        });
    }


}
