# InboxLayout
模拟Google Inbox邮箱的上下拉返回效果

# ScreenShot
![image](https://raw.githubusercontent.com/zhaozhentao/InboxLayout/master/screenshot/pic.gif)

# Usage
###step 1
实现类似的布局, 具体可参考demo

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <com.zzt.inbox.widget.InboxScrollView
            android:scrollbars="none"
            android:id="@+id/scroll"
            android:layout_width="match_parent"
            android:layout_height="match_parent">
            <LinearLayout
                android:orientation="vertical"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">
                <!--在这里布局主界面要显示的内容-->
            </LinearLayout>
        </com.zzt.inbox.widget.InboxScrollView>
        
        <com.zzt.inbox.widget.InboxLayout
            android:id="@+id/myframelayout"
            android:visibility="invisible"
            android:layout_width="match_parent"
            android:layout_height="match_parent">
            <ListView  <!--目前InboxLayout只支持ListView作为子View, 后续版本会扩大适用范围-->
                android:background="#ffffffff"
                android:id="@+id/list"
                android:layout_width="match_parent"
                android:layout_height="match_parent">
            </ListView>
        </com.zzt.inbox.widget.InboxLayout>
    </FrameLayout>

###step 2

    final InboxScrollView inboxScrollView = (InboxScrollView)findViewById(R.id.scroll);
    inboxLayout = (InboxLayout)findViewById(R.id.inboxlayout);             
    inboxLayout.seBackgroundScrollView(inboxScrollView); //将inboxScrollView 与 inboxlayout绑定,由inboxlayout带动inboxScrollView滚动
    
    
###step 3
 
    final LinearLayout dingdan = (LinearLayout)findViewById(R.id.ding_dan);
    dingdan.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        inboxLayout.openWithAnim(dingdan);//给inboxLayout传入open时需要隐藏的view
      }
    });

# Last
目前InboxLayout仍处于初级阶段,使用上有较多的限制,后续版本将不断丰富,扩大适用范围
