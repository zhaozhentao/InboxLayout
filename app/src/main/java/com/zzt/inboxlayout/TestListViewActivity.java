package com.zzt.inboxlayout;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;


/**
 * Created by zzt on 2015/2/1.
 */
public class TestListViewActivity extends ActionBarActivity{

    private boolean fisrt = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_activity);
        final ListView listView = (ListView)findViewById(R.id.listview);
        Button btn = (Button)findViewById(R.id.btn);
        //listView.setAdapter(new ExpandableAdapter<Integer>(TestListViewActivity.this));


    }
}
