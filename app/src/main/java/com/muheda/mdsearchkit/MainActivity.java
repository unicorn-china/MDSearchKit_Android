package com.muheda.mdsearchkit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.muheda.mdsearchview.icallback.ICallBack;
import com.muheda.mdsearchview.model.SearchDataDto;
import com.muheda.mdsearchview.view.SearchView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = (SearchView) findViewById(R.id.search_view);

        // 4. 设置点击搜索按键后的操作（通过回调接口）
        // 参数 = 搜索框输入的内容
        searchView.setOnClickSearch(new ICallBack() {
            @Override
            public void SearchAciton(String string) {
                Toast.makeText(MainActivity.this, "我收到了" + string, Toast.LENGTH_SHORT).show();
            }
        }).setFuzzyData(initData())
                .addOtherView("1", CustomView.class, new SearchDataDto())
                .addOtherView("2", CustomView.class, new SearchDataDto())
                .setCurrentPageShow(false);
    }

    private List<String> initData() {
        List<String> list = new ArrayList<>();
        list.add("啦啦啦啦啦啦啦哈哈哈哈哈");
        list.add("你好吗");
        list.add("啦啦哈哈");
        list.add("你");
        list.add("哈哈六六六");
        list.add("我");
        list.add("aaa");
        list.add("abc");
        list.add("abcdfghr");
        list.add("bdbjdbcjs");
        list.add("cdslmcdl");
        list.add("dkjflksflas");
        list.add("efdfdfdlk");
        return list;
    }
}
