package com.laxture.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * There is a bug introduced in JellyBean (API 16). Anywhere TextView
 * using spannable should use this TextView to avoid this bug.
 *
 * To reproduce:
 * <ol>
 * <li>Create a spannable string with multiple paragraphs.</li>
 * <li>In a paragraph other than the first one, add a styling span
 * that starts within a word such that the line will be broken to the start of the word.</li>
 * </ol>
 *
 * http://code.google.com/p/android/issues/detail?id=35466
 * http://code.google.com/p/android/issues/detail?id=35412
 *
 * Created by hank on 7/15/13.
 */
public class TextViewPatchedJB extends TextView {

    public TextViewPatchedJB(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TextViewPatchedJB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewPatchedJB(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (ArrayIndexOutOfBoundsException e) {
            setText(getText().toString());
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void setGravity(int gravity) {
        try {
            super.setGravity(gravity);
        } catch (ArrayIndexOutOfBoundsException e) {
            setText(getText().toString());
            super.setGravity(gravity);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            super.setText(text, type);
        } catch (ArrayIndexOutOfBoundsException e) {
            setText(text.toString());
        }
    }
}
