package com.laxture.lib.view.date;

import org.joda.time.DateTime;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.laxture.lib.R;

/***
 * A dialog that prompts the user for the time of day using a {@link TimePicker}
 * .
 * <p>
 * See the <a href="{@docRoot}
 * resources/tutorials/views/hello-timepicker.html">Time Picker tutorial</a>.
 * </p>
 */
public class WheelDatePickerDialog extends AlertDialog implements
        View.OnClickListener, WheelDatePicker.OnTimeChangedListener {

    private static final String DATE_FORMAT_TITLE = "yyyy-MM-dd";

    /***
     * The callback interface used to indicate the user is done filling in the
     * time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {

        /***
         * @param mTimePicker The view associated with this listener.
         * @param dateTime The dateTime that was set.
         */
        void onTimeSet(WheelDatePicker mTimePicker, DateTime dateTime);
    }

    private static final String EXTRA_DATETIME = "extra_datetime";

    private WheelDatePicker mTimePicker = null;

    public WheelDatePicker getTimePicker() {
        return mTimePicker;
    }

    private OnTimeSetListener mCallbackSet = null;

    private View.OnClickListener mCallBackCancel = null;

    private Button mBtnSet = null;
    public Button getSetButton() {
        return mBtnSet;
    }

    private Button mBtnCancel = null;
    public Button getCancelButton() {
        return mBtnCancel;
    }

    public WheelDatePickerDialog(Context context, OnTimeSetListener mCallBackSet,
            View.OnClickListener onCancelClickListener, DateTime dateTime) {
        this(context, mCallBackSet, dateTime);
        this.mCallBackCancel = onCancelClickListener;
    }

    /***
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param dateTime The initial hour.
     */
    public WheelDatePickerDialog(Context context, OnTimeSetListener callBack, DateTime dateTime) {

        super(context);

        mCallbackSet = callBack;

        setCanceledOnTouchOutside(false);
        setIcon(R.drawable.device_access_time);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wheel_date_picker_dialog, null);

        mTimePicker = (WheelDatePicker) view.findViewById(R.id.customDatePicker);
        mBtnSet = (Button)view.findViewById(R.id.btn_dialog_set);
        mBtnCancel = (Button)view.findViewById(R.id.btn_dialog_cancel);
        mBtnSet.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        setTitle(dateTime.toString(DATE_FORMAT_TITLE));

        mTimePicker.setOnTimeChangedListener(this);
        mTimePicker.setCurrentTime(dateTime);

        setView(view);

    }

    public void setCurrentDate(DateTime date) {
        mTimePicker.setCurrentTime(date);
    }

    public void setStartTime(DateTime startTime) {
        mTimePicker.setStartTime(startTime);
    }

    public void setEndTime(DateTime endTime) {
        mTimePicker.setEndTime(endTime);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putSerializable(EXTRA_DATETIME, mTimePicker.getCurrentTime());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        DateTime dateTime = (DateTime) savedInstanceState.getSerializable(EXTRA_DATETIME);
        mTimePicker.setCurrentTime(dateTime);
    }

    public void onClick(View v) {
        if (v == mBtnSet) {
            if (mCallbackSet != null) {
                mTimePicker.clearFocus();
                mCallbackSet.onTimeSet(mTimePicker, mTimePicker.getCurrentTime());
            }
        } else {
            if (mCallBackCancel != null)
                mCallBackCancel.onClick(mBtnCancel);
        }

        this.dismiss();

    }

    public void onTimeChanged(WheelDatePicker view, DateTime dateTime) {
        setTitle(dateTime.toString(DATE_FORMAT_TITLE));
    }

    public void setCurrentTime(DateTime currentTime) {
        if (mTimePicker != null) {
            mTimePicker.setCurrentTime(currentTime);
        }
    }
}
