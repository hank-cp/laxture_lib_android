package com.laxture.lib.view.dialog;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.laxture.lib.R;
import com.laxture.lib.util.ViewUtil;

/**
 * Overcome AlertDialog style issue prior and after Honeycomb
 *
 * ref: http://stackoverflow.com/a/5392617/482533
 */
public class AlertDialogBuilder extends AlertDialog.Builder {

    private Context mContext;
    private View mTitleBar;
    private TextView mTitle;
    private ImageView mIcon;
    private TextView mMessage;

    private boolean mTitleBarEnabled = true;

    private AlertDialogBuilder(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private AlertDialogBuilder(Context context, int themeId) {
        super(context, themeId);
    }

    public static AlertDialogBuilder builderWithTitle(Context context) {
        AlertDialogBuilder builder = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
            ? new AlertDialogBuilder(new ContextThemeWrapper(context,
                    R.style.CommonTheme_AlertDialogTheme))
            : new AlertDialogBuilder(context,
                    R.style.CommonTheme_AlertDialogTheme);
        builder.mTitleBarEnabled = true;
        builder.init(context);
        return builder;
    }

    public static AlertDialogBuilder builderWithoutTitle(Context context) {
        AlertDialogBuilder builder = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
            ? new AlertDialogBuilder(new ContextThemeWrapper(context,
                    R.style.CommonTheme_AlertDialogTheme_NoTitle))
            : new AlertDialogBuilder(context,
                    R.style.CommonTheme_AlertDialogTheme_NoTitle);
        builder.mTitleBarEnabled = false;
        builder.init(context);
        return builder;
    }

    private void init(Context context) {
        mContext = context;

        if (mTitleBarEnabled) {
            mTitleBar = View.inflate(mContext, R.layout.alert_dialog_title, null);
            mTitle = (TextView) mTitleBar.findViewById(R.id.alertTitle);
            mIcon = (ImageView) mTitleBar.findViewById(R.id.icon);
            setCustomTitle(mTitleBar);
        }

        View customMessage = View.inflate(mContext, R.layout.alert_dialog_message, null);
        mMessage = (TextView) customMessage.findViewById(R.id.message);
        setView(customMessage);
    }

    @Override
    public AlertDialogBuilder setTitle(int textResId) {
        mTitle.setText(textResId);
        return this;
    }
    @Override
    public AlertDialogBuilder setTitle(CharSequence text) {
        mTitle.setText(text);
        return this;
    }

    @Override
    public AlertDialogBuilder setMessage(int textResId) {
        mMessage.setText(textResId);
        return this;
    }

    @Override
    public AlertDialogBuilder setMessage(CharSequence text) {
        mMessage.setText(text);
        return this;
    }

    @Override
    public AlertDialogBuilder setIcon(int drawableResId) {
        mIcon.setImageResource(drawableResId);
        return this;
    }

    @Override
    public AlertDialogBuilder setIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
        return this;
    }

    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();

        // disable window title bar if needed
        if (mTitleBarEnabled)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    public static void setTitle(AlertDialog dialog, CharSequence title) {
        ViewUtil.setViewText(R.id.alertTitle, dialog, title);
    }

    public static void setMessage(AlertDialog dialog, CharSequence message) {
        ViewUtil.setViewText(R.id.message, dialog, message);
    }

}