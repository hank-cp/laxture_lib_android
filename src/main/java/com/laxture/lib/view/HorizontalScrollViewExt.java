package com.laxture.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

public class HorizontalScrollViewExt extends HorizontalScrollView {

    private ScrollViewListener mScrollViewListener = null;

    public HorizontalScrollViewExt(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public HorizontalScrollViewExt(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollViewExt(Context context) {
        super(context);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.mScrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

//        LLog.d("left=%s, top=%s, oldLeft=%s, oldTop=%s", l, t, oldl, oldt);

        // 0 => no change
        // >0 => swipe to right, scroll to left
        // <0 => swipe to left, scroll to right
        int direction = l - oldl;
        if (direction == 0) return;

        if (mScrollViewListener != null) {
            mScrollViewListener.onScrollToHead(this, l == 0);

            View contentView = getChildAt(0);
            int diff = (contentView.getRight() - getWidth() - getScrollX());
            mScrollViewListener.onScrollToEnd(this, l != 0 && diff == 0);

            if (contentView instanceof ViewGroup) {
                ViewGroup contentViewGroup = (ViewGroup) contentView;
                for (int i=0; i<contentViewGroup.getChildCount(); i++) {
                    View view = contentViewGroup.getChildAt(i);
                    int r = l+getWidth();
                    int oldr = oldl+getWidth();
                    if ((view.getLeft() > oldr && view.getLeft() <= r)
                            || (view.getRight() < oldl && view.getRight() >= l)) {
                        mScrollViewListener.onChildViewWillAppear(view);
                    }
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mScrollViewListener != null) {
            mScrollViewListener.onLayoutCompleted(this, changed, l, t, r, b,
                    computeHorizontalScrollRange());

            View contentView = getChildAt(0);
            if (contentView instanceof ViewGroup) {
                ViewGroup contentViewGroup = (ViewGroup) contentView;
                for (int i=0; i<contentViewGroup.getChildCount(); i++) {
                    View view = contentViewGroup.getChildAt(i);
                    if (view.getLeft() < r) {
                        mScrollViewListener.onChildViewWillAppear(view);
                    }
                }
            }
        }
    }

    public interface ScrollViewListener {
        void onLayoutCompleted(HorizontalScrollViewExt scrollView,
                               boolean changed, int l, int t, int r, int b, int totalWidth);

        void onChildViewWillAppear(View childView);

        void onScrollToEnd(HorizontalScrollViewExt scrollView, boolean reachEnd);

        void onScrollToHead(HorizontalScrollViewExt scrollView, boolean reachHead);
    }

}
