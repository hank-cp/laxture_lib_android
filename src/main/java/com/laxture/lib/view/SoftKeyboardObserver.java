package com.laxture.lib.view;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.laxture.lib.R;

public class SoftKeyboardObserver implements OnGlobalLayoutListener {

    private View mRootView;
    private OnSoftKeyboardToggledListener mListener;
    private boolean mIsSoftKeyboardShown;

    public SoftKeyboardObserver(View rootView, OnSoftKeyboardToggledListener listener) {
        mRootView = rootView;
        mListener = listener;
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        mRootView.getWindowVisibleDisplayFrame(r);

        int heightDiff = mRootView.getRootView().getHeight() - (r.bottom - r.top);
        if (mListener != null) {
            boolean isShown = heightDiff > mRootView.getResources().getDimensionPixelSize(R.dimen.keyboard_min_heights);
            if (isShown != mIsSoftKeyboardShown) {
                mIsSoftKeyboardShown = isShown;
                mListener.onSoftKeyboardToggled(isShown, r.right, r.bottom);
            }
        }
    }

    public boolean isSoftKeyboardShown() {
        return mIsSoftKeyboardShown;
    }

    public interface OnSoftKeyboardToggledListener {
       public void onSoftKeyboardToggled(boolean keyboardShown, int visibleWidth, int visibleHeight);
    }
}
