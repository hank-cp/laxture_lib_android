package com.laxture.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TabHost;

public class TabHostExt extends TabHost {

    private OnTabClickListener mOnTabClickListener;
    public void setOnTabClickListener(OnTabClickListener listener) {
        mOnTabClickListener = listener;
    }

    public TabHostExt(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabHostExt(Context context) {
        super(context);
    }

    @Override
    public void setCurrentTab(int index) {
        super.setCurrentTab(index);

        if (mOnTabClickListener != null)
            mOnTabClickListener.onTabClick(index);
    }

    public interface OnTabClickListener {
        void onTabClick(int index);
    }

    @Override
    public void onTouchModeChanged(boolean isInTouchMode) {
        // do nothing
    }

}
