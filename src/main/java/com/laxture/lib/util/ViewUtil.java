package com.laxture.lib.util;

import java.io.File;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.laxture.lib.RuntimeContext;

public final class ViewUtil {

    private ViewUtil() {} // Hide Constructor.

    public static final ViewGroup.LayoutParams DEFAULT_LAYOUT_PARAM =
            new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    public static final ViewGroup.LayoutParams FILL_LAYOUT_PARAM =
            new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

    public static final RadioGroup.OnCheckedChangeListener TOGGLE_LISTENER =
        new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
            for (int j = 0; j < radioGroup.getChildCount(); j++) {
                final CompoundButton view = (CompoundButton) radioGroup.getChildAt(j);
                view.setChecked(view.getId() == i);
            }
        }
    };

    public static void removeViewFromParent(View view) {
        if (view == null) return;
        ViewGroup parentView = (ViewGroup) view.getParent();
        if (parentView == null) return;
        parentView.removeView(view);
    }

    /**
     * Convenient method to retrieve value from TextView.
     *
     * @param viewId the assigned view must be inherited from TextView
     * @param rootView accept any Object has <code>findViewById()</code> method.
     * @return
     */
    public static String getViewText(int viewId, Object rootView) {
        try {
            TextView view = resolveTextView(viewId, rootView);
            return view.getText().toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Convenient method to retrieve value from TextView.
     *
     * @param viewId the assigned view must be inherited from TextView
     * @param rootView accept any Object has <code>findViewById()</code> method.
     * @param color actual color that will be set. For color from Resource, use
     *              Context.getResources().getColor() to get the real color
     */
    public static void setViewTextColor(int viewId, Object rootView, int color) {
        TextView view = resolveTextView(viewId, rootView);
        if (view != null) view.setTextColor(color);
    }

    /**
     * Convenient method to set Text value for TextView.
     *
     * @param viewId the assigned view must be inherited from TextView
     * @param rootView accept any Object has <code>findViewById()</code> method.
     */
    public static void setViewText(int viewId, Object rootView, CharSequence text) {
        TextView view = resolveTextView(viewId, rootView);
        if (view != null) view.setText(text);
    }

    /**
     * Convenient method to set Text value for TextView.
     *
     * @param viewId the assigned view must be inherited from TextView
     * @param rootView accept any Object has <code>findViewById()</code> method.
     */
    public static void setViewText(int viewId, Object rootView, int textResId) {
        TextView view = resolveTextView(viewId, rootView);
        if (view != null) view.setText(textResId);
    }

    public static void setImageSource(int viewId, Object rootView, int resId) {
        ImageView view = resolveImageView(viewId, rootView);
        if (view != null) view.setImageResource(resId);
    }

    public static void setImageSource(int viewId, Object rootView, Bitmap bitmap) {
        ImageView view = resolveImageView(viewId, rootView);
        if (view != null) view.setImageBitmap(bitmap);
    }

    public static void setImageSource(int viewId, Object rootView, Drawable drawable) {
        ImageView view = resolveImageView(viewId, rootView);
        if (view != null) view.setImageDrawable(drawable);
    }

    public static void setImageSource(int viewId, Object rootView, File file) {
        ImageView view = resolveImageView(viewId, rootView);
        if (view != null) view.setImageURI(Uri.fromFile(file));
    }

    private static TextView resolveTextView(int viewId, Object rootView) {
        Method method;
        TextView view = null;
        try {
            method = rootView.getClass().getMethod("findViewById", int.class);
            view = (TextView) method.invoke(rootView, viewId);
        } catch (Exception e) {
            LLog.w("Cannot find view "+viewId);
        }

        return view;
    }

    private static ImageView resolveImageView(int viewId, Object rootView) {
        Method method;
        ImageView view = null;
        try {
            method = rootView.getClass().getMethod("findViewById", int.class);
            view = (ImageView) method.invoke(rootView, viewId);
        } catch (Exception e) {
            LLog.w("Cannot find view "+viewId);
        }

        return view;
    }

    public static boolean isMultilineInputType(int inputType) {
        return (inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE)) ==
                (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
    }

    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboard(Activity activity) {
        hideKeyboard(activity, activity.getWindow());
    }

    public static void hideKeyboard(Activity activity, Window window) {
        InputMethodManager imm = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusView = (window != null) ? window.getDecorView()
                : activity.getWindow().getCurrentFocus();
        if (focusView != null) imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
    }

    // ref: http://stackoverflow.com/a/7737586/482533
    public static boolean checkIfTouchOutsideOfView(View view, MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_DOWN
                && checkIfMotionOutsideOfView(view, event);
    }

    public static boolean checkIfMotionOutsideOfView(View view, MotionEvent event) {
        if (view == null) return false;
        int scrcoords[] = new int[2];
        view.getLocationOnScreen(scrcoords);
        float x = event.getRawX() + view.getLeft() - scrcoords[0];
        float y = event.getRawY() + view.getTop() - scrcoords[1];

        return x < view.getLeft() || x >= view.getRight() || y < view.getTop() || y > view.getBottom();
    }

    public static void showToast(final Activity activity, final String message) {
        if (activity == null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showToast(final Activity activity, final int resId) {
        showToast(activity, activity.getString(resId));
    }

    public static int getDisplaySize(int pixelSize) {
        return Math.round(pixelSize * RuntimeContext.getResources().getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(float dpValue) {
        final float scale = RuntimeContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static View findViewById(Fragment fragment, int id) {
        return fragment.getActivity().findViewById(id);
    }

    /** 为EditText设置错误提示 */
    public static void setErrorHint(EditText editText, String errMsg) {
        ForegroundColorSpan span = new ForegroundColorSpan(
                RuntimeContext.getResources().getColor(android.R.color.white));
        SpannableStringBuilder sb = new SpannableStringBuilder(errMsg);
        sb.setSpan(span, 0, errMsg.length(), 0);
        editText.setError(sb);
    }

    public static void clearErrorHint(EditText editText) {
        editText.setError(null);
    }

}
