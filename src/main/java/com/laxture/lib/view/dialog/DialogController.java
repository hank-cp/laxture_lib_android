package com.laxture.lib.view.dialog;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.WindowManager;
import android.widget.Button;

import com.laxture.lib.Configuration;
import com.laxture.lib.R;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.UnHandledException;

/**
 * Activity and Fragment use DialogController to control Dialog showing and
 * dismissing.
 */
public abstract class DialogController {

    private Activity mActivity;

    private int mButtonBackgroundResId;
    private int mButtonTextColor;
    private int mButtonTextSize;

    private int mPositiveButtonBackgroundResId;
    private int mPositiveButtonTextColor;

    protected HashMap<String, Dialog> mManagedDialogs = new HashMap<String, Dialog>();

    //*************************************************************************
    //  Constructor
    //*************************************************************************

    public DialogController(Activity activity) {
        setActivity(activity);

        TypedArray a = Configuration.getApplicationContext().getTheme().obtainStyledAttributes(
                R.style.CommonTheme_AlertDialogTheme_Button, new int[] {
                    android.R.attr.background,
                    android.R.attr.textColor,
                    android.R.attr.textSize,
                    R.attr.positiveButtonBackground,
                    R.attr.positiveButtonTextColor,
        });

        mButtonBackgroundResId = a.getResourceId(0, 0);
        mButtonTextColor = a.getColor(1, Color.BLACK);
        mButtonTextSize = a.getDimensionPixelSize(2, 0);
        mPositiveButtonBackgroundResId = a.getResourceId(3, 0);
        mPositiveButtonTextColor = a.getColor(4, Color.BLACK);

        a.recycle();
    }

    public void setActivity(Activity activity) {
        if (activity == null) throw new UnHandledException("Activity cannot be null.");
        mActivity = activity;
    }

    public void onShowDialogFailed(String dialogTag) {}

    /**
     * Use Below XML to specify Dialog button style:
     * <pre>{@code
     *     <style name="CommonTheme.AlertDialogTheme.Button" parent="@style/CommonTheme.Button">
     *         <item name="android:background">@drawable/btn_dialog</item>
     *         <item name="android:textColor">@color/btn_default</item>
     *         <item name="positiveButtonBackground">@drawable/btn_dialog_positive</item>
     *         <item name="positiveButtonTextColor">@color/white</item>
     *         <item name="android:textSize">18dp</item>
     *     </style>
     * }</pre>
     *
     * @param backgroundResId
     * @param color
     */
    @Deprecated
    public void setPositiveButtonStyle(int backgroundResId, int color) {
        mPositiveButtonBackgroundResId = backgroundResId;
        mPositiveButtonTextColor = color;
    }

    /**
     * Use Below XML to specify Dialog button style:
     * <pre>{@code
     *     <style name="CommonTheme.AlertDialogTheme.Button" parent="@style/CommonTheme.Button">
     *         <item name="android:background">@drawable/btn_dialog</item>
     *         <item name="android:textColor">@color/btn_default</item>
     *         <item name="positiveButtonBackground">@drawable/btn_dialog_positive</item>
     *         <item name="positiveButtonTextColor">@color/white</item>
     *         <item name="android:textSize">18dp</item>
     *     </style>
     * }</pre>
     *
     * @param backgroundResId
     * @param color
     */
    public void setButtonStyle(int backgroundResId, int color) {
        mButtonBackgroundResId = backgroundResId;
        mButtonTextColor = color;
    }

    //*************************************************************************
    //  Dialog Controller Method
    //*************************************************************************

    public abstract Dialog prepareDialog(String dialogTag, Object...params);

    public final void showDialog(final String dialogTag, final Object...params) {
        if (getActivity() == null) onShowDialogFailed(dialogTag);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // generate dialog
                    Dialog dialog = prepareDialog(dialogTag, params);

                    // find re-using dialog that showing with another tag
                    String duplicateDialogTag = null;
                    for (String tag : mManagedDialogs.keySet()) {
                        if (mManagedDialogs.get(tag) == dialog)
                            duplicateDialogTag = tag;
                    }
                    // dismiss it.
                    if (duplicateDialogTag != null) {
                        mManagedDialogs.remove(dialogTag);
                    }

                    mManagedDialogs.put(dialogTag, dialog);
                    dialog.show();

                } catch (WindowManager.BadTokenException e) {
                    // Activity is finished.
                    LLog.w("Failed to show dialog. %s", e.getMessage());
                    onShowDialogFailed(dialogTag);
                }
             }
        });
    }

    public final void dismissDialog(String dialogTag) {
        Dialog dialog = mManagedDialogs.get(dialogTag);
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch(IllegalArgumentException e) {}
            // If Activity close, Exception will be thrown here. eat it silently.
            mManagedDialogs.remove(dialogTag);
        }
    }

    //*************************************************************************
    //  Util Method
    //*************************************************************************

    public Activity getActivity() {
        return mActivity;
    }

    public String getString(int resId) {
        return mActivity.getString(resId);
    }

    public final String getString(int resId, Object... formatArgs) {
        return mActivity.getString(resId, formatArgs);
    }

    public Dialog getDialog(String dialogTag) {
        return mManagedDialogs.get(dialogTag);
    }

    //*************************************************************************
    //  Util Dialog
    //*************************************************************************

    private HashMap<String, UtilDialogCallback> mCallbacks
            = new HashMap<String, UtilDialogCallback>();

    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;
    private AlertDialog mYesNoDialog;
    private AlertDialog mYesNoCancelDialog;

    public ProgressDialog getProgressDialog(String dialogTag,
                                            String message,
                                            boolean cancelable,
                                            UtilDialogCallback callback) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setIndeterminate(true);
        }
        // Update dialog conf
        mCallbacks.put(dialogTag, callback);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setOnCancelListener(callback);

        return mProgressDialog;
    }

    public AlertDialog getAlertDialog(String dialogTag,
                                      String title,
                                      String message,
                                      String yesLabel,
                                      boolean cancelable,
                                      UtilDialogCallback callback) {
        if (mAlertDialog == null) {
            mAlertDialog = generateAlertDialog(dialogTag, title, message,
                    yesLabel, null, null, cancelable);
            mAlertDialog.setOnShowListener(mAlertButtonStyleUpdater);
        } else {
            // Dialog button reference cannot retrieve until show()
            // ref: http://code.google.com/p/android/issues/detail?id=6360
            AlertDialogBuilder.setTitle(mAlertDialog, title);
            AlertDialogBuilder.setMessage(mAlertDialog, message);
            mAlertDialog.setCancelable(cancelable);
            if (mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null)
                mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(yesLabel);
        }

        mAlertDialog.setOnCancelListener(callback);
        mCallbacks.put(dialogTag, callback);
        return mAlertDialog;
    }

    public AlertDialog getYesNoDialog(String dialogTag,
                                      String title,
                                      String message,
                                      String yesLabel,
                                      String noLabel,
                                      boolean cancelable,
                                      UtilDialogCallback callback) {
        if (mYesNoDialog == null) {
            mYesNoDialog = generateAlertDialog(dialogTag, title, message, yesLabel,
                    noLabel, null, cancelable);
            mYesNoDialog.setOnShowListener(mPositiveButtonStyleUpdater);
        } else {
            AlertDialogBuilder.setTitle(mYesNoDialog, title);
            AlertDialogBuilder.setMessage(mYesNoDialog, message);
            mYesNoDialog.setCancelable(cancelable);
            if (mYesNoDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null)
                mYesNoDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(yesLabel);
            if (mYesNoDialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null)
                mYesNoDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(noLabel);
        }

        mYesNoDialog.setOnCancelListener(callback);
        mCallbacks.put(dialogTag, callback);
        return mYesNoDialog;
    }

    public AlertDialog getYesNoCancelDialog(String dialogTag,
                                            String title,
                                            String message,
                                            String yesLabel,
                                            String noLabel,
                                            String cancelLabel,
                                            boolean cancelable,
                                            UtilDialogCallback callback) {
        if (mYesNoCancelDialog == null) {
            mYesNoCancelDialog = generateAlertDialog(dialogTag, title, message,
                    yesLabel, noLabel, cancelLabel, cancelable);
            mYesNoCancelDialog.setOnShowListener(mPositiveButtonStyleUpdater);
        } else {
            AlertDialogBuilder.setTitle(mYesNoCancelDialog, title);
            AlertDialogBuilder.setMessage(mYesNoCancelDialog, message);
            mYesNoCancelDialog.setCancelable(cancelable);
            if (mYesNoCancelDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null)
                mYesNoCancelDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(yesLabel);
            if (mYesNoCancelDialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null)
                mYesNoCancelDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(noLabel);
            if (mYesNoCancelDialog.getButton(DialogInterface.BUTTON_NEUTRAL) != null)
                mYesNoCancelDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setText(cancelLabel);
        }

        mYesNoCancelDialog.setOnCancelListener(callback);
        mCallbacks.put(dialogTag, callback);
        return mYesNoCancelDialog;
    }

    private AlertDialog generateAlertDialog(String dialogTag,
                                            String title,
                                            String message,
                                            String yesLabel,
                                            String noLabel,
                                            String cancelLabel,
                                            boolean cancelable) {
        AlertDialogBuilder builder = AlertDialogBuilder.builderWithTitle(mActivity);
        builder.setMessage(message)
               .setTitle(title)
               .setIcon(android.R.drawable.ic_dialog_info)
               .setCancelable(cancelable)
               .setPositiveButton(yesLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String dialogTag = null;
                        for (String tag : mManagedDialogs.keySet()) {
                            if (mManagedDialogs.get(tag) == dialog)
                                dialogTag = tag;
                        }
                        if (dialogTag != null) {
                            dismissDialog(dialogTag);
                            UtilDialogCallback callback = mCallbacks.get(dialogTag);
                            if (callback != null) callback.onYes(dialog);
                        }
                    }
                });
        if (!Checker.isEmpty(noLabel))
            builder.setNegativeButton(noLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String dialogTag = null;
                        for (String tag : mManagedDialogs.keySet()) {
                            if (mManagedDialogs.get(tag) == dialog)
                                dialogTag = tag;
                        }
                        if (dialogTag != null) {
                            dismissDialog(dialogTag);
                            UtilDialogCallback callback = mCallbacks.get(dialogTag);
                            if (callback != null) callback.onNo(dialog);
                        }
                    }
             });

        if (!Checker.isEmpty(cancelLabel))
            builder.setNeutralButton(cancelLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String dialogTag = null;
                        for (String tag : mManagedDialogs.keySet()) {
                            if (mManagedDialogs.get(tag) == dialog)
                                dialogTag = tag;
                        }
                        if (dialogTag != null) {
                            dismissDialog(dialogTag);
                            UtilDialogCallback callback = mCallbacks.get(dialogTag);
                            if (callback != null) callback.onCancel(dialog);
                        }
                    }
             });

        return builder.create();
    }

    private OnShowListener mPositiveButtonStyleUpdater = new OnShowListener() {

        @Override
        public void onShow(DialogInterface dialog) {
            if (android.os.Build.VERSION.SDK_INT <= 10) {
                Button negativeButton =
                        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negativeButton != null) {
                    negativeButton.setBackgroundResource(mButtonBackgroundResId);
                    negativeButton.setTextColor(mButtonTextColor);
                    negativeButton.setTextSize(mButtonTextSize);
                }

                Button neutralButton =
                        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEUTRAL);
                if (neutralButton != null) {
                    neutralButton.setBackgroundResource(mButtonBackgroundResId);
                    neutralButton.setTextColor(mButtonTextColor);
                    neutralButton.setTextSize(mButtonTextSize);
                }

                Button positiveButton =
                        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positiveButton != null) {
                    positiveButton.setBackgroundResource(mButtonBackgroundResId);
                    positiveButton.setTextColor(mButtonTextColor);
                    positiveButton.setTextSize(mButtonTextSize);
                }
            }

            if (mPositiveButtonBackgroundResId > 0) {
                Button positiveButton =
                        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positiveButton != null) {
                    positiveButton.setBackgroundResource(mPositiveButtonBackgroundResId);
                    positiveButton.setTextColor(mPositiveButtonTextColor);
                    positiveButton.setTypeface(positiveButton.getTypeface(), Typeface.BOLD);
                }
            }
        }
    };

    private OnShowListener mAlertButtonStyleUpdater = new OnShowListener() {

        @Override
        public void onShow(DialogInterface dialog) {
            if (android.os.Build.VERSION.SDK_INT <= 10) {
                Button positiveButton =
                        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positiveButton != null) {
                    positiveButton.setBackgroundResource(mButtonBackgroundResId);
                    positiveButton.setTextColor(mButtonTextColor);
                    positiveButton.setTextSize(mButtonTextSize);
                }
            }
        }
    };

    public static abstract class UtilDialogCallback
            implements DialogInterface.OnCancelListener {
        public void onYes(DialogInterface dialog) {}
        public void onNo(DialogInterface dialog) {}
        public void onCancel(DialogInterface dialog) {}
    }

}