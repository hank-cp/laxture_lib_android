package com.laxture.lib.view.date;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.laxture.lib.R;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.SLLunarDate;

public class WheelLunarDatePickerDialog extends AlertDialog implements
        View.OnClickListener, WheelLunarDatePicker.OnTimeChangedListener {

    private static final String EXTRA_LUNAR_DATE = "extra_lunar_date";

    /***
     * The callback interface used to indicate the user is done filling in the
     * time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {

        /***
         * @param mTimePicker The view associated with this listener.
         * @param lunar The lunarDate that was set.
         */
        void onTimeSet(WheelLunarDatePicker mTimePicker, SLLunarDate lunar);
    }

    private WheelLunarDatePicker mTimePicker = null;

    private OnTimeSetListener mCallback = null;

    private Button mBtnSet = null;
    public Button getSetButton() {
        return mBtnSet;
    }

    private Button mBtnCancel = null;
    public Button getCancelButton() {
        return mBtnCancel;
    }

    public WheelLunarDatePickerDialog(Context context, OnTimeSetListener callBack, SLLunarDate lunar) {

        super(context);

        mCallback = callBack;

        setCanceledOnTouchOutside(false);
        setIcon(R.drawable.device_access_time);

        setTitle("请选择日期");

        // setButton(BUTTON_POSITIVE, "set", this);
        // setButton(BUTTON_NEGATIVE, "cancel", (OnClickListener) null);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.wheel_lunar_date_picker_dialog, null);

        mTimePicker = (WheelLunarDatePicker)view.findViewById(R.id.customDatePicker);
        mBtnSet = (Button)view.findViewById(R.id.btn_dialog_set);
        mBtnCancel = (Button)view.findViewById(R.id.btn_dialog_cancel);
        mBtnSet.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        mTimePicker.setOnTimeChangedListener(this);
        mTimePicker.setCurrentTime(lunar);
        setView(view);
    }

    public void setCurrentDate(SLLunarDate date) {
        mTimePicker.setCurrentTime(date);
    }

    public void setStartTime(SLLunarDate startTime) {
        mTimePicker.setStartTime(startTime);
    }

    public void setEndTime(SLLunarDate endTime) {
        mTimePicker.setEndTime(endTime);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putSerializable(EXTRA_LUNAR_DATE, mTimePicker.getCurrentTime());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SLLunarDate lunarDate = (SLLunarDate) savedInstanceState.getSerializable(EXTRA_LUNAR_DATE);
        mTimePicker.setCurrentTime(lunarDate);
    }

    public void onClick(View v) {

        if (v == mBtnSet) {

            SLLunarDate lunar = mTimePicker.getCurrentTime();
            LLog.d("" + lunar.getYear() + " " + lunar.getMonth() + " " + lunar.getDay() + " " + lunar.isLeapMonth());
            if (mCallback != null) {
                mTimePicker.clearFocus();
                mCallback.onTimeSet(mTimePicker, lunar);
            }
        }

        this.dismiss();
    }

    @Override
    public void onTimeChanged(WheelLunarDatePicker view, SLLunarDate lunarDate) {}

}
