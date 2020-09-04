package com.laxture.lib.view;

import androidx.viewpager.widget.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.R;
import com.laxture.lib.util.ViewUtil;

import java.util.List;

/**
 * Created by hank on 9/24/13.
 */
public abstract class PagerGridAdapter<T> extends PagerAdapter {

    private List<T> mData;
    private int mCountPerPage;
    private int mColumnCount;
    private ViewGroup[] mPagerContainViews;
    private int mVerticalSpacing = ViewUtil.dip2px(16);
    private int mHorizontalSpacing = ViewUtil.dip2px(16);

    private AdapterView.OnItemClickListener mItemClickListener;

    public PagerGridAdapter(List<T> data, int countPerPage, int columnCount) {
        mData = data;
        mCountPerPage = countPerPage;
        mColumnCount = columnCount;
        mPagerContainViews = new ViewGroup[getCount()];
    }

    public void setItemClickListener(AdapterView.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setVerticalSpacing(int value) {
        mVerticalSpacing = value;
    }

    public void setHorizontalSpacing(int value) {
        mHorizontalSpacing = value;
    }

    //*************************************************************************
    // General Adapter Methods
    //*************************************************************************

    public abstract View getView(int page, int position, View convertView, ViewGroup parent);

    public int getPageCount() {
        return (int) Math.ceil((float) mData.size() / mCountPerPage);
    }

    public int getPaginalDataCount(int page) {
        int start = page * mCountPerPage;
        return Math.min(mCountPerPage,
                mData.size()-start);
    }

    public T getItem(int page, int position) {
        int start = page * mCountPerPage;
        return mData.get(start + position);
    }

    public long getItemId(int page, int position) {
        return position;
    }

    //*************************************************************************
    // Pager Adapter
    //*************************************************************************

    @Override
    public Object instantiateItem(ViewGroup container, int page) {
        ViewGroup contentView;
        if (mPagerContainViews[page] != null) {
            contentView = mPagerContainViews[page];
            ViewUtil.removeViewFromParent(contentView);
        } else {
            contentView = createPageContentView(mColumnCount, page);
            mPagerContainViews[page] = contentView;
        }
        container.addView(contentView);
        return contentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // do nothing, since data is cached globally
    }

    @Override
    public int getCount() {
        return getPageCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    //*************************************************************************
    // Grid Adapter
    //*************************************************************************

    public ViewGroup createPageContentView(int columnCount, int page) {
        GridView gridView = new GridView(RuntimeContext.getApplication());
        gridView.setGravity(Gravity.CENTER);
        gridView.setNumColumns(columnCount);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setSelector(R.drawable.grid_selector_background);
        gridView.setVerticalSpacing(mVerticalSpacing);
        gridView.setHorizontalSpacing(mHorizontalSpacing);
        GridAdapter adapter = new GridAdapter(page);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(mItemClickListener);
        gridView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return gridView;
    }

    public class GridAdapter extends BaseAdapter {

        private int mPage;

        public GridAdapter(int page) {
            mPage = page;
        }

        @Override
        public int getCount() {
            return getPaginalDataCount(mPage);
        }

        @Override
        public Object getItem(int position) {
            return PagerGridAdapter.this.getItem(mPage, position);
        }

        @Override
        public long getItemId(int position) {
            return PagerGridAdapter.this.getItemId(mPage, position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return PagerGridAdapter.this.getView(mPage, position, convertView, parent);
        }
    }

}
