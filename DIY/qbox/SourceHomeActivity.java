package com.github.tvbox.osc.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.AbsSortXml;
import com.github.tvbox.osc.bean.MovieSort;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.ui.adapter.HomePageAdapter;
import com.github.tvbox.osc.ui.adapter.SortAdapter;
import com.github.tvbox.osc.ui.fragment.GridFragment;
import com.github.tvbox.osc.ui.tv.widget.DefaultTransformer;
import com.github.tvbox.osc.ui.tv.widget.FixedSpeedScroller;
import com.github.tvbox.osc.ui.tv.widget.NoScrollViewPager;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import me.jessyan.autosize.utils.AutoSizeUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 根据参数 直接打开对应 源主页数据
 */
public class SourceHomeActivity extends BaseActivity {
    private LinearLayout contentLayout;

    private TvRecyclerView mGridView;
    private NoScrollViewPager mViewPager;
    private SourceViewModel sourceViewModel;
    private SortAdapter sortAdapter;
    private HomePageAdapter pageAdapter;
    private List<BaseLazyFragment> fragments = new ArrayList<>();
    private boolean isDownOrUp = false;
    private boolean sortChange = false;
    private int currentSelected = 0;
    private int sortFocused = 0;
    public View sortFocusView = null;
    private Handler mHandler = new Handler();
    private String sourceKey = null;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_source_home;
    }
    
    @Override
    protected void init() {
        initView();
        Bundle bundle = getIntent().getExtras();
        String[] sourceList = bundle.getStringArray("sourceKey");
        if (!loadSourceKeyData(sourceList)) return;
        initViewModel();
        initData();
    }

    private void initView() {
        this.contentLayout = findViewById(R.id.ty_contentLayout);
        //this.zyGridView = findViewById(R.id.ty_zyGridView);
        this.mGridView = findViewById(R.id.ty_mGridView);
        this.mViewPager = findViewById(R.id.ty_mViewPager);
        this.sortAdapter = new SortAdapter();
        
        this.mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 0, false));
        this.mGridView.setSpacingWithMargins(0, AutoSizeUtils.dp2px(this.mContext, 10.0f));
        this.mGridView.setAdapter(this.sortAdapter);
        this.mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            public void onItemPreSelected(TvRecyclerView tvRecyclerView, View view, int position) {
                if (view != null && !SourceHomeActivity.this.isDownOrUp) {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start();
                    view.findViewById(R.id.tvFilter).setVisibility(View.GONE);
                }
            }
        
            public void onItemSelected(TvRecyclerView tvRecyclerView, View view, int position) {
                if (view != null) {
                    SourceHomeActivity.this.isDownOrUp = false;
                    SourceHomeActivity.this.sortChange = true;
                    view.animate().scaleX(1.1f).scaleY(1.1f).setInterpolator(new BounceInterpolator()).setDuration(300).start();
                    TextView textView = view.findViewById(R.id.tvTitle);
                    textView.getPaint().setFakeBoldText(true);
                    textView.setTextColor(SourceHomeActivity.this.getResources().getColor(R.color.color_FFFFFF));
                    textView.invalidate();
                    MovieSort.SortData item = sortAdapter.getItem(position);
                    if (item != null && !item.filters.isEmpty())
                        view.findViewById(R.id.tvFilter).setVisibility(View.VISIBLE);
                    SourceHomeActivity.this.sortFocusView = view;
                    SourceHomeActivity.this.sortFocused = position;
                    mHandler.removeCallbacks(mDataRunnable);
                    mHandler.postDelayed(mDataRunnable, 200);
                }
            }
        
            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                if (itemView != null && currentSelected == position) {
                    BaseLazyFragment baseLazyFragment = fragments.get(currentSelected);
                    if ((baseLazyFragment instanceof GridFragment) && !sortAdapter.getItem(position).filters.isEmpty()) {// 弹出筛选
                        ((GridFragment) baseLazyFragment).showFilter();
                    }
                }
            }
        });
        this.mGridView.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
            public final boolean onInBorderKeyEvent(int direction, View view) {
                if (direction != View.FOCUS_DOWN) {
                    return false;
                }
                isDownOrUp = true;
                BaseLazyFragment baseLazyFragment = fragments.get(sortFocused);
                if (!(baseLazyFragment instanceof GridFragment)) {
                    return false;
                }
                if (!((GridFragment) baseLazyFragment).isLoad()) {
                    return true;
                }
                return false;
            }
        });
        setLoadSir(this.contentLayout);
        //mHandler.postDelayed(mFindFocus, 500);
    }

    private void initViewModel() {
        sourceViewModel = new ViewModelProvider(this).get(SourceViewModel.class);
        sourceViewModel.sortResult.observe(this, new Observer<AbsSortXml>() {
            @Override
            public void onChanged(AbsSortXml absXml) {
                showSuccess();
                if (absXml != null && absXml.classes != null && absXml.classes.sortList != null) {
                    sortAdapter.setNewData(DefaultConfig.adjustSort(sourceKey, absXml.classes.sortList, false));
                } else {
                    sortAdapter.setNewData(DefaultConfig.adjustSort(sourceKey, new ArrayList<>(), false));
                }
                initViewPager(absXml);
            }
        });
    }

    private void initData() {
        showLoading();
        sourceViewModel.getSort(sourceKey);
    }

    private List<SourceBean> sourceBeanList;
    
    private boolean loadSourceKeyData(String[] sourceArray) {
        if (sourceArray != null && sourceArray.length == 1) {
            sourceKey = sourceArray[0];
            sourceBeanList = null;
            return true;
        }
        boolean hasDfKey = false;
        List<SourceBean> sites = ApiConfig.get().getSourceBeanList();
        sourceBeanList = new ArrayList<>();
        List<String> sourceList = sourceArray == null ? null : Arrays.asList(sourceArray);
        for (SourceBean bean : sites) {
            if (sourceList != null && sourceList.contains(bean.getKey())) continue;
            sourceBeanList.add(bean);
            if (!hasDfKey && bean.getKey().equals(sourceKey)) hasDfKey = true;
        }
        if (sourceBeanList.size() == 0) return false;
        if (!hasDfKey) {
            sourceKey = sourceBeanList.get(0).getKey();
        }
        int size = sourceBeanList.size();
        return size > 0;
    }

    private void initViewPager(AbsSortXml absXml) {
        this.mGridView.setSelection(sortFocused);
        if (sortAdapter.getData().size() > 0) {
            for (MovieSort.SortData data : sortAdapter.getData()) {
                fragments.add(GridFragment.newInstance(data, sourceKey));
            }
            pageAdapter = new HomePageAdapter(getSupportFragmentManager(), fragments);
            try {
                Field field = ViewPager.class.getDeclaredField("mScroller");
                field.setAccessible(true);
                FixedSpeedScroller scroller = new FixedSpeedScroller(mContext, new AccelerateInterpolator());
                field.set(mViewPager, scroller);
                scroller.setmDuration(300);
            } catch (Exception e) {
            }
            mViewPager.setPageTransformer(true, new DefaultTransformer());
            mViewPager.setAdapter(pageAdapter);
            mViewPager.setCurrentItem(currentSelected, false);
        }
    }

    @Override
    public void onBackPressed() {
        int i;
        if (this.fragments.size() == 0 || this.sortFocused >= this.fragments.size() || (i = this.sortFocused) < 0) {
            super.onBackPressed();
            return;
        }
        BaseLazyFragment baseLazyFragment = this.fragments.get(i);
        if (baseLazyFragment instanceof GridFragment) {
            View view = this.sortFocusView;
            GridFragment grid = (GridFragment) baseLazyFragment;
            if (grid.restoreView()) {// 还原上次保存的UI内容
                return;
            }
            if (view != null && !view.isFocused()) {
                view.requestFocus();
            } else {
                super.onBackPressed();
            }
            //super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    private Runnable mDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (sortChange) {
                sortChange = false;
                if (sortFocused != currentSelected) {
                    currentSelected = sortFocused;
                    mViewPager.setCurrentItem(sortFocused, false);
                }
            }
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            return super.dispatchKeyEvent(event);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isLongPress()) {
            if (this.sortFocusView != null && !this.sortFocusView.isFocused()) {
                this.sortFocusView.requestFocus();
            } else {
                super.onBackPressed();
            }
            return false;
        }
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU://按下菜单键 选择首页 或者是筛选
                    BaseLazyFragment baseLazyFragment = fragments.get(this.sortFocused);
                    if ((baseLazyFragment instanceof GridFragment) && !sortAdapter.getItem(this.sortFocused).filters.isEmpty()) {// 弹出筛选
                        ((GridFragment) baseLazyFragment).showFilter();
                    }
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
