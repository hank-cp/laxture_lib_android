package com.laxture.lib.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;

import com.laxture.lib.R;
import com.laxture.lib.util.UnHandledException;

public class SimpleDialogController extends DialogController {

    public static final String DIALOG_PROGRESS = "dialog_progress";
    public static final String DIALOG_ALERT = "dialog_alert";

    public SimpleDialogController(Activity activity) {
        super(activity);
    }

    public void showProgressDialog(String progressMessage) {
        showProgressDialog(progressMessage, false);
    }

    public void showProgressDialog(String progressMessage, boolean cancelable) {
        showDialog(DIALOG_PROGRESS, progressMessage, cancelable);
    }

    public void setProgressDialogText(String text) {
        Dialog dialog = getDialog(DIALOG_PROGRESS);
        if (dialog == null || !(dialog instanceof ProgressDialog)) return;
        ProgressDialog progressDialog = (ProgressDialog) dialog;
        progressDialog.setMessage(text);
    }

    public void dismissProgressDialog() {
        dismissDialog(DIALOG_PROGRESS);
    }

    public void showAlertDialog(String alertMessage) {
        showDialog(DIALOG_ALERT, alertMessage);
    }

    public void dismissAlertDialog() {
        dismissDialog(DIALOG_ALERT);
    }

    @Override
    public Dialog prepareDialog(String dialogTag, Object...params) {

        if (DIALOG_PROGRESS.equals(dialogTag)) {
            return getProgressDialog(dialogTag,
                    params[0].toString(),
                    (Boolean) params[1], null);

        } else if (DIALOG_ALERT.equals(dialogTag)) {
            return getAlertDialog(dialogTag,
                    null,
                    params[0].toString(),
                    getString(R.string.label_ok),
                    true, null);

        } else throw new UnHandledException("Unknown dialog tag");
    }

}
