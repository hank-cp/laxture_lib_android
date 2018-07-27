package com.laxture.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.widget.EditText;

import com.laxture.lib.R;

public class TransformableEditText extends EditText {

    /**
     *  This attribute will load from XML.
     *  It it set to ture, the background will be reservered.
     */
    private boolean mReserveBackground;
    /**
     *  This attribute will load from XML.
     *  If it set to true, this EditText will not be editable, but could be clickable.
     *  An exploitable EditText view could be used to receive a click event, like Popup an
     *  Address dialog, Tag dialog.
     */
    private boolean mExploitable;

    private boolean mReadonly = true;
    private Drawable mBackgroundDrawable;
    private CharSequence mHint;
    private KeyListener mKeyListener;
    private Drawable mIcon;
    private OnClickListener mClickListener;

    public TransformableEditText(Context context) {
        super(context);
        init();
    }

    public TransformableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TransformableEditText);
        mReserveBackground = a.getBoolean(R.styleable.TransformableEditText_reserveBackground, false);
        mExploitable = a.getBoolean(R.styleable.TransformableEditText_exploitable, false);
        a.recycle();

        init();
    }

    public void init() {
        mBackgroundDrawable = getBackground();
        mHint = getHint();
        mKeyListener = getKeyListener();
        mIcon = getResources().getDrawable(android.R.drawable.ic_menu_more);

        if (mExploitable) setFocusableInTouchMode(false);

        // set default to display mode
        setReadOnly(mReadonly);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        // in DisplayMode, hold to set OnClickListener, wait until next
        // toogleMode() being called.
        if (!mReadonly) super.setOnClickListener(l);
        else super.setOnClickListener(null);
        if (l != null) mClickListener = l;
    }

    public void setReadOnly(boolean readonly) {
        mReadonly = readonly;
        if (mReadonly) {
            // configure view for display mode;
            setCursorVisible(false);
            setKeyListener(null);
            setHint(null);
            if (mExploitable) {
                setOnClickListener(null);
                setCompoundDrawables(null, null, null, null);
            }
            if (!mReserveBackground) setBackgroundResource(android.R.color.transparent);
        } else {
            // configure view for edit mode;
            if (!mExploitable) {
                setCursorVisible(true);
                setKeyListener(mKeyListener);
                setHint(mHint);
            } else {
                setOnClickListener(mClickListener);
                setCompoundDrawablesWithIntrinsicBounds(null, null, mIcon, null);
            }
            if (!mReserveBackground) setBackgroundDrawable(mBackgroundDrawable);
        }
    }

    public boolean isReadOnly() {
        return mReadonly;
    }
}
