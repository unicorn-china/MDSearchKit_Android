package com.muheda.mdsearchview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muheda.mdsearchview.adapter.DataAdapter;
import com.muheda.mdsearchview.adapter.FuzzyCheckAdapter;
import com.muheda.mdsearchview.helper.AssembleDataUtil;
import com.muheda.mdsearchview.helper.RecordSQLiteOpenHelper;
import com.muheda.mdsearchview.icallback.ICallBack;
import com.muheda.mdsearchview.model.SearchDataDto;
import com.muheda.mdsearchview.model.SearchItem;
import com.muheda.mdsearchview.model.SearchModelDto;

import java.util.ArrayList;
import java.util.List;

import searchcut.airr.searchview.R;


public class SearchView extends LinearLayout {

    private Context context;

    // 搜索框组件
    private ClearEditText et_search; // 搜索布局
    private LinearLayout search_block; // 搜索框布局

    // 数据库变量
    // 用于存放历史搜索记录
    private RecordSQLiteOpenHelper helper;
    private SQLiteDatabase db;

    // 回调接口
    private ICallBack mCallBack;// 搜索按键回调接口

    // 自定义属性设置
    // 1. 搜索字体属性设置：大小、颜色 & 默认提示
    private Float textSizeSearch;
    private int textColorSearch;
    private String textHintSearch;
    private int searchLeftImg;
    private boolean fuzzyShow = true;
    private boolean currentPageShow = true;

    // 2. 搜索框设置：高度 & 颜色
    private int searchBlockHeight;
    private int searchBackground;
    // 搜索栏设置：背景
    private int topBackground;
    private int bottomBackground;

    private RecyclerView rv_date;
    List<String> list;
    List<String> listFuzzy;
    List<SearchItem> listOtherData;
    private DataAdapter dataAdapter;
    private TextView tv_search;
    //模糊搜索
    private RecyclerView rv_fuzzy_check;
    private FuzzyCheckAdapter fuzzyCheckAdapter;
    private SearchDataDto searchDataDto;

    /**
     * 构造函数
     * 对搜索框进行初始化
     */
    public SearchView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initAttrs(context, attrs);
        init();
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initAttrs(context, attrs);
        init();
    }

    /**
     * 作用：初始化自定义属性
     */
    private void initAttrs(Context context, AttributeSet attrs) {

        // 控件资源名称
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Search_View);

        // 搜索框字体大小（dp）
        textSizeSearch = typedArray.getDimension(R.styleable.Search_View_textSizeSearch, 20);

        // 搜索框字体颜色（使用十六进制代码，如#333、#8e8e8e）
        int defaultTextSearchColor = context.getResources().getColor(R.color.colorText); // 默认颜色 = 灰色
        textColorSearch = typedArray.getColor(R.styleable.Search_View_textColorSearch, defaultTextSearchColor);

        // 搜索框提示内容（String）
        textHintSearch = typedArray.getString(R.styleable.Search_View_textHintSearch);

        // 搜索框高度
        searchBlockHeight = typedArray.getInteger(R.styleable.Search_View_searchBlockHeight, 150);

        // 搜索框背景
        int defaultEditColor = context.getResources().getColor(R.color.colorDefault); // 默认颜色 = 白色
        searchBackground = typedArray.getColor(R.styleable.Search_View_searchBackground, defaultEditColor);

        // 上方搜索条背景
        int defaultTopColor = context.getResources().getColor(R.color.colorDefault); // 默认颜色 = 白色
        topBackground = typedArray.getColor(R.styleable.Search_View_topBackground, defaultTopColor);
        // 下方搜索条背景
        bottomBackground = typedArray.getColor(R.styleable.Search_View_topBackground, Color.parseColor("#00000000")); // 默认颜色 = 透明
        // 模糊搜索是否开启
        fuzzyShow = typedArray.getBoolean(R.styleable.Search_View_fuzzyShow, true);
        // 更换搜索框左侧图标
        searchLeftImg = typedArray.getResourceId(R.styleable.Search_View_searchLeftImg, R.drawable.search);
        // 是否在当前页展示结果
        currentPageShow = typedArray.getBoolean(R.styleable.Search_View_currentPageShow, true);

        // 释放资源
        typedArray.recycle();
    }


    /**
     * 作用：初始化搜索框
     */
    private void init() {
        //初始化所需list
        list = new ArrayList<>();
        listFuzzy = new ArrayList<>();
        listOtherData = new ArrayList<>();
        // 1. 初始化UI组件
        initView();

        // 2. 实例化数据库SQLiteOpenHelper子类对象
        helper = new RecordSQLiteOpenHelper(context);

        // 3. 第1次进入时查询所有的历史搜索记录
        queryData();

        /**
         * 监听输入键盘更换后的搜索按键
         * 调用时刻：点击键盘上的搜索键时
         */
        et_search.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    toSearch();
                }
                return false;
            }
        });

        et_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_date.setVisibility(VISIBLE);
            }
        });

        et_search.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) rv_date.setVisibility(VISIBLE);
            }
        });

        tv_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toSearch();
            }
        });

        /**
         * 搜索框的文本变化实时监听
         */
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            // 输入文本后调用该方法
            @Override
            public void afterTextChanged(Editable editable) {
                // 每次输入后，模糊查询数据库 & 显示
                // 注：若搜索框为空,则模糊搜索空字符 = 显示所有的搜索历史
                String tempName = et_search.getText().toString();
                queryFuzzyData(tempName);
                rv_fuzzy_check.setVisibility(fuzzyShow ? VISIBLE : GONE);
                if ("".equals(tempName)) {
                    rv_fuzzy_check.setVisibility(GONE);
                }
            }
        });

    }

    public void toSearch() {
        // 1. 点击搜索按键后，根据输入的搜索字段进行查询
        // 注：由于此处需求会根据自身情况不同而不同，所以具体逻辑由开发者自己实现，此处仅留出接口
        if (!(mCallBack == null)) {
            mCallBack.SearchAciton(et_search.getText().toString());
        }

        if ("".equals(et_search.getText().toString().trim())) return;
        // 2. 点击搜索键后，对该搜索字段在数据库是否存在进行检查（查询）->> 关注1
        boolean hasData = hasData(et_search.getText().toString().trim());
        // 3. 若存在，则不保存；若不存在，则将该搜索字段保存（插入）到数据库，并作为历史搜索记录
        if (!hasData) {
            insertData(et_search.getText().toString().trim());
            queryData();
        }

        rv_date.postDelayed(new Runnable() {
            @Override
            public void run() {
                rv_date.setVisibility(currentPageShow ? GONE : VISIBLE);
                rv_fuzzy_check.setVisibility(GONE);
            }
        }, 100);
    }


    /**
     * 绑定搜索框xml视图
     */
    private void initView() {

        // 1. 绑定R.layout.search_layout作为搜索框的xml文件
        LayoutInflater.from(context).inflate(R.layout.search_layout, this);

        // 2. 绑定搜索框EditText
        et_search = (ClearEditText) findViewById(R.id.et_search);
        et_search.setTextSize(textSizeSearch);
        et_search.setTextColor(textColorSearch);
        et_search.setHint(textHintSearch);
        et_search.setBackgroundColor(searchBackground);
        Drawable drawable = getResources().getDrawable(searchLeftImg);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        et_search.setCompoundDrawables(drawable, null, null, null);

        // 3. 搜索框背景颜色
        search_block = (LinearLayout) findViewById(R.id.search_block);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) search_block.getLayoutParams();
        params.height = searchBlockHeight;
        search_block.setBackgroundColor(topBackground);
        search_block.setLayoutParams(params);

        //下方历史搜索等数据列表
        rv_date = (RecyclerView) findViewById(R.id.rv_data);
        dataAdapter = new DataAdapter(R.layout.item_search);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_date.setLayoutManager(linearLayoutManager);
        // 4. 历史搜索记录 = ListView显示
        rv_date.setAdapter(dataAdapter);
        rv_date.setBackgroundColor(bottomBackground);
        // 输入框后搜索
        tv_search = findViewById(R.id.tv_search);
        //模糊查找
        rv_fuzzy_check = findViewById(R.id.rv_fuzzy_check);
        fuzzyCheckAdapter = new FuzzyCheckAdapter(R.layout.item_fuzzy, new ICallBack() {
            @Override
            public void SearchAciton(String string) {
                rv_fuzzy_check.setVisibility(GONE);
                toSearchChangeUI(string);
            }
        });
        LinearLayoutManager linearLayout = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_fuzzy_check.setLayoutManager(linearLayout);
        rv_fuzzy_check.setAdapter(fuzzyCheckAdapter);
    }

    /**
     * 模糊查询数据 & 显示到ListView列表上
     */
    private void queryFuzzyData(String tempName) {

        // 1. 模糊搜索
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "select * from fuzzys where name like '%" + tempName + "%'", null);
        // 2. 创建adapter适配器对象 & 装入模糊搜索的结果
        listFuzzy.clear();
        while (cursor.moveToNext()) {
            String data = cursor.getString(cursor.getColumnIndex("name"));
            listFuzzy.add(data);
        }
        fuzzyCheckAdapter.addList(listFuzzy);
    }

    private void queryData() {
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "select id as _id,name from records where name like '%" + "" + "%' order by id desc ", null);
        list.clear();
        while (cursor.moveToNext()) {
            String data = cursor.getString(cursor.getColumnIndex("name"));
            list.add(data);
        }
        searchDataDto = new SearchDataDto();
        searchDataDto.setTitle("历史记录");
        searchDataDto.setData(list);
        searchDataDto.setOnCallBackListener(new ICallBack() {
            @Override
            public void SearchAciton(String string) {
                toSearchChangeUI(string);
            }
        });
        searchDataDto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清空数据库->>关注2
                deleteData();
                // 模糊搜索空字符 = 显示所有的搜索历史（此时是没有搜索记录的）
                queryData();
                rv_date.setVisibility(currentPageShow ? GONE : VISIBLE);
            }
        });

        searchDataDto.setCallBack(new ICallBack() {
            @Override
            public void SearchAciton(String string) {
                toSearchChangeUI(string);
            }
        });

        dataAdapter.addList(AssembleDataUtil.getViews(listOtherData, searchDataDto));

    }

    private void toSearchChangeUI(String string) {
        et_search.setText(string);
        rv_date.setVisibility(currentPageShow ? GONE : VISIBLE);
        toSearch();
    }

    /**
     * 清空数据库
     */
    private void deleteData() {

        db = helper.getWritableDatabase();
        db.execSQL("delete from records");
        db.close();
    }

    /**
     * 检查数据库中是否已经有该搜索记录
     */
    private boolean hasData(String tempName) {
        // 从数据库中Record表里找到name=tempName的id
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "select id as _id,name from records where name =?", new String[]{tempName});
        //  判断是否有下一个
        return cursor.moveToNext();
    }

    /**
     * 插入数据到数据库，即写入搜索字段到历史搜索记录
     */
    private void insertData(String tempName) {
        db = helper.getWritableDatabase();
        db.execSQL("insert into records(name) values('" + tempName + "')");
        db.close();
    }

    /**
     * 点击键盘中搜索键后的操作，用于接口回调
     */
    public SearchView setOnClickSearch(ICallBack mCallBack) {
        this.mCallBack = mCallBack;
        return this;
    }

    /**
     * 设置搜索框的背景
     */
    public SearchView setAllBackground(int background) {
        search_block.setBackgroundResource(background);
        return this;
    }

    /**
     * 设置输入框的背景
     */
    public SearchView setEdtBackground(int background) {
        et_search.setBackgroundResource(background);
        return this;
    }

    /**
     * 自定义输入框右侧布局
     *
     * @param view
     */
    public SearchView setRightLayout(View view) {
        replaceView(R.id.ll_end_view, view);
        return this;
    }


    /**
     * 设置右侧固定布局的margin
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public SearchView setRightMargin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//宽高
        layoutParams.setMargins(left, top, right, bottom);//4个参数按顺序分别是左上右下
        tv_search.setLayoutParams(layoutParams);
        return this;
    }

    /**
     * 模糊搜索是否开启
     *
     * @param show
     */
    public SearchView setFuzzyShow(boolean show) {
        fuzzyShow = show;
        rv_fuzzy_check.setVisibility(show ? VISIBLE : GONE);
        return this;
    }

    /**
     * 设置模糊搜索数据源
     *
     * @param list
     */
    public SearchView setFuzzyData(List<String> list) {
        db = helper.getWritableDatabase();
        db.execSQL("delete from fuzzys");

        for (String s : list) {
            db.execSQL("insert into fuzzys(name) values('" + s + "')");
        }

        db.close();
        return this;
    }

    /**
     * 注入其他view，可多次注入 不含data
     *
     * @param key
     * @param view
     * @return
     */
    public SearchView addOtherView(String key, Class view) {
        addOtherView(key, view, null);
        return this;
    }

    /**
     * 注入其他view，可多次注入
     *
     * @param key
     * @param view
     * @param data
     * @return
     */

    public SearchView addOtherView(String key, Class view, SearchModelDto data) {
        dataAdapter.initMap(key, view);
        if (data != null) {
            data.setCallBack(new ICallBack() {
                @Override
                public void SearchAciton(String string) {
                    toSearchChangeUI(string);
                }
            });
        }
        listOtherData.add(new SearchItem(key, data));
        dataAdapter.addList(AssembleDataUtil.getViews(listOtherData, searchDataDto));
        return this;
    }

    /**
     * 获取搜索控件
     *
     * @return
     */
    public ClearEditText getEditText() {
        return et_search;
    }

    /**
     * 是否在当前页展示搜索结果
     *
     * @param currentPageShow
     */
    public SearchView setCurrentPageShow(boolean currentPageShow) {
        this.currentPageShow = currentPageShow;
        return this;
    }

    private void replaceView(int id, View view) {
        ViewGroup viewGroup = findViewById(id);
        viewGroup.removeAllViews();
        viewGroup.addView(view);
    }
}
