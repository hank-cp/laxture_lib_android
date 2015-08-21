package com.laxture.lib.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.laxture.lib.R;
import com.laxture.lib.RuntimeContext;

/***
 * A dialog that prompts the user to input text
 */
public class TextInputDialog extends AlertDialog implements View.OnClickListener {

    /***
     * The callback interface used to indicate the user is done filling in the
     * text (they clicked on the 'Set' button).
     */
    public interface OnTextSetListener {

        /***
         * @param editText The view associated with this listener.
         * @param text The text that was set.
         */
        void onTextSet(EditText editText, String text);
    }

    private static final String EXTRA_TEXT = "extra_text";

    private EditText mEditText = null;

    public EditText getEditText() {
        return mEditText;
    }

    private OnTextSetListener mCallbackSet = null;

    private View.OnClickListener mCallBackCancel = null;

    private Button mBtnSet = null;
    public Button getSetButton() {
        return mBtnSet;
    }

    private Button mBtnCancel = null;
    public Button getCancelButton() {
        return mBtnCancel;
    }

    public TextInputDialog(Context context, OnTextSetListener mCallBackSet,
                           View.OnClickListener onCancelClickListener, String text) {
        this(context, mCallBackSet, text);
        this.mCallBackCancel = onCancelClickListener;
    }

    /***
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param text The initial hour.
     */
    public TextInputDialog(Context context, OnTextSetListener callBack, String text) {

        super(context);

        mCallbackSet = callBack;

        setCanceledOnTouchOutside(false);
        setIcon(android.R.drawable.ic_dialog_info);

        LayoutInflater inflater = RuntimeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.text_input_dialog, null);

        mEditText = (EditText) view.findViewById(R.id.input_text);
        mBtnSet = (Button)view.findViewById(R.id.btn_dialog_set);
        mBtnCancel = (Button)view.findViewById(R.id.btn_dialog_cancel);
        mBtnSet.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        setText(text);
        setView(view);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putString(EXTRA_TEXT, mEditText.getText().toString());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String text = savedInstanceState.getString(EXTRA_TEXT);
        mEditText.setText(text);
    }

    public void onClick(View v) {
        if (v == mBtnSet) {
            if (mCallbackSet != null) {
                mEditText.clearFocus();
                mCallbackSet.onTextSet(mEditText, mEditText.getText().toString());
            }
        } else {
            if (mCallBackCancel != null)
                mCallBackCancel.onClick(mBtnCancel);
        }

        this.dismiss();

    }

    public void setText(String text) {
        if (mEditText != null) {
            mEditText.setText(text);
        }
    }
}
