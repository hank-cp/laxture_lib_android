package com.laxture.lib.view.date;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.laxture.lib.R;
import com.laxture.lib.util.ArrayUtil;
import com.laxture.lib.util.LLog;

public class WheelDatePicker extends FrameLayout {

    public WheelDatePicker(Context context) {
        super(context);
    }

    private static int START_YEAR = 1930, END_YEAR = 2048;

    private MutableDateTime mCurrentTime = new MutableDateTime();

    private DateTime mStartTime = new DateTime(START_YEAR, 1, 30, 0, 0);

    private DateTime mEndTime = new DateTime(END_YEAR+1, 1, 1, 0, 0);

    private WheelView mYearWheel = null;

    private WheelView mMonthWheel = null;

    private WheelView mDayWheel = null;

    private final static int[] sBigMonthList = {
        1, 3, 5, 7, 8, 10, 12
    };

    private final static int[] sSmallMonthList = {
        4, 6, 9, 11
    };

    private final NumericWheelAdapter mAdapterBigMonth =
            new NumericWheelAdapter(1, 31, "%02d", "日");

    private final NumericWheelAdapter mAdapterSmallMonth =
            new NumericWheelAdapter(1, 30, "%02d", "日");

    private final NumericWheelAdapter mAdapterBigFebMonth =
            new NumericWheelAdapter(1, 29, "%02d", "日");

    private final NumericWheelAdapter mAdapterSmallFebMonth =
            new NumericWheelAdapter(1, 28, "%02d", "日");

    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener = null;

    /***
     * A no-op callback used in the constructor to avoid null checks later in
     * the code.
     */
    public interface OnTimeChangedListener {

        /***
         * @param view The view associated with this listener.
         * @param dateTime current dateTime
         */
        void onTimeChanged(WheelDatePicker view, DateTime dateTime);
    }

    public WheelDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.wheel_date_pick, this, true); // we are the parent


        mYearWheel = (WheelView) findViewById(R.id.wv_year);
        mYearWheel.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR,
                "%04d", "年"));

        mMonthWheel = (WheelView) findViewById(R.id.wv_month);
        mMonthWheel.setAdapter(new NumericWheelAdapter(1, 12, "%02d", "月"));

        mDayWheel = (WheelView) findViewById(R.id.wv_day);
        mDayWheel.setAdapter(new NumericWheelAdapter(1, 31, "%02d", "日"));

        mYearWheel.addChangingListener(new OnWheelChangedListener() {

            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mCurrentTime.setYear(newValue + mStartTime.getYear());
                updateAdapters();
                onTimeChanged();
                updateMonthDisplay();
                updateDayDisplay();
            }
        });

        mMonthWheel.addChangingListener(new OnWheelChangedListener() {

            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (mCurrentTime.getYear() == mStartTime.getYear())
                    mCurrentTime.setMonthOfYear(newValue+mStartTime.getMonthOfYear());
                else
                    mCurrentTime.setMonthOfYear(newValue+1);
                updateAdapters();
                onTimeChanged();
                updateDayDisplay();

                if(mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonthOfYear() == mStartTime.getMonthOfYear() && mCurrentTime.getDayOfMonth() < mStartTime.getDayOfMonth()) {
                    mCurrentTime.setDayOfMonth(mStartTime.getDayOfMonth());
                }
            }
        });

        mDayWheel.addChangingListener(new OnWheelChangedListener() {

            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonthOfYear() == mStartTime.getMonthOfYear())
                    mCurrentTime.setDayOfMonth(newValue+mStartTime.getDayOfMonth());
                else
                    mCurrentTime.setDayOfMonth(newValue+1);
                onTimeChanged();
            }
        });

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DateTimePicker, 0, 0);
        int textSize = array.getDimensionPixelOffset(R.styleable.DateTimePicker_textSize, 18);
        mYearWheel.TEXT_SIZE = textSize;
        mMonthWheel.TEXT_SIZE = textSize;
        mDayWheel.TEXT_SIZE = textSize;
        array.recycle();
    }

    public void updateAdapters() {
        // 判断大小月及是否闰年,用来确定"日"的数据
        if (ArrayUtil.find(sBigMonthList, mCurrentTime.getMonthOfYear()) != -1) {
            mDayWheel.setAdapter(mAdapterBigMonth);

        } else if (ArrayUtil.find(sSmallMonthList, mCurrentTime.getMonthOfYear()) != -1) {
            mDayWheel.setAdapter(mAdapterSmallMonth);

        } else {
            // 2月的特殊情況
            if ((mCurrentTime.getYear() % 4 == 0 && mCurrentTime.getYear() % 100 != 0)
                    || mCurrentTime.getYear() % 400 == 0) {
                mDayWheel.setAdapter(mAdapterBigFebMonth);
            } else {
                mDayWheel.setAdapter(mAdapterSmallFebMonth);
            }
        }
    }

    /***
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener
     *            the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * Set the current time
     */
    private void setTime() {
        //沒有开始和结束的任何限制，用默认的限制
        if (mCurrentTime.getYear() < START_YEAR) {
            mCurrentTime.setYear(START_YEAR);
            mOnTimeChangedListener.onTimeChanged(this, mCurrentTime.toDateTime());
        } else if (mCurrentTime.getYear() > END_YEAR) {
            mCurrentTime.setYear(END_YEAR);
            mOnTimeChangedListener.onTimeChanged(this, mCurrentTime.toDateTime());
        }

        updateYearRange();
        updateMonthRange();
        updateDayRange();
        updateYearDisplay();
        updateMonthDisplay();
        updateDayDisplay();
        onTimeChanged();
    }

    private void updateYearRange() {
        //不允许超过现在时间限制最大年
        mYearWheel.setAdapter(new NumericWheelAdapter(mStartTime.getYear(), mEndTime.getYear(),
                    "%04d", "年"));
        LLog.d("Year Range :: %s ~ %s", mYearWheel.getAdapter().getItem(mStartTime.getYear()), mYearWheel.getAdapter().getItem(mEndTime.getYear()));
    }

    private void updateMonthRange() {

        //不允许超过上下时间限制最大月
        if (mCurrentTime.getYear() == mEndTime.getYear()) {
            mMonthWheel.setAdapter(new NumericWheelAdapter(1, mEndTime.getMonthOfYear(), "%02d", "月"));
        }
        else if(mCurrentTime.getYear() == mStartTime.getYear()) {
            mMonthWheel.setAdapter(new NumericWheelAdapter(mStartTime.getMonthOfYear(), 12, "%02d", "月"));
        }
        else {
            mMonthWheel.setAdapter(new NumericWheelAdapter(1, 12, "%02d", "月"));
        }
    }

    private void updateDayRange() {
        if (mCurrentTime.getYear() == mStartTime.getYear()
                && mCurrentTime.getMonthOfYear() == mStartTime.getMonthOfYear()) {
            int startDayIndex = mStartTime.getDayOfMonth();
          //不允许超过下限时间限制最小日
            if (ArrayUtil.find(sBigMonthList, mCurrentTime.getMonthOfYear()) != -1) {
                mDayWheel.setAdapter(new NumericWheelAdapter(mStartTime
                        .getDayOfMonth(), 31, "%02d", "日"));

            } else if (ArrayUtil.find(sSmallMonthList,
                    mCurrentTime.getMonthOfYear()) != -1) {
                mDayWheel.setAdapter(new NumericWheelAdapter(mStartTime
                        .getDayOfMonth(), 30, "%02d", "日"));

            } else {
                // 2月的特殊情況
                if ((mCurrentTime.getYear() % 4 == 0 && mCurrentTime.getYear() % 100 != 0)
                        || mCurrentTime.getYear() % 400 == 0) {
                    mDayWheel.setAdapter(new NumericWheelAdapter(mStartTime
                            .getDayOfMonth(), 29, "%02d", "日"));
                } else {
                    mDayWheel.setAdapter(new NumericWheelAdapter(mStartTime
                            .getDayOfMonth(), 28, "%02d", "日"));
                }
            }
                startDayIndex = -1;
            LLog.d("Set Day Range start = %s, %s", mDayWheel.getAdapter().getItem(startDayIndex), startDayIndex);
        }

       if (mCurrentTime.getYear() == mEndTime.getYear()
               && mCurrentTime.getMonthOfYear() == mEndTime.getMonthOfYear()) {
         //不允许超过现在时间限制最大日
           mDayWheel.setAdapter(new NumericWheelAdapter(1, mEndTime.getDayOfMonth(), "%02d", "日"));
       }
    }

    public void setCurrentTime(DateTime dateTime) {
        mCurrentTime = dateTime.toDateMidnight().toMutableDateTime();
        setTime();
    }

    public DateTime getCurrentTime() {
        return mCurrentTime.toDateTime();
    }

    public void setStartTime(DateTime startTime) {
        mStartTime = startTime;
        if (mCurrentTime.isBefore(startTime)) {
            mCurrentTime = startTime.toMutableDateTime();
        }
        setTime();
    }


    public void setEndTime(DateTime endTime)
    {
        if (mStartTime == null && mStartTime.isAfter(endTime)) {
            return;
        }
        mEndTime  = endTime;

        if (mCurrentTime.isAfter(endTime)) {
            mCurrentTime = endTime.toMutableDateTime();
        }

        setTime();
    }

    /***
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateYearDisplay() {
        mYearWheel
                .setCurrentItem(mCurrentTime.getYear() - mStartTime.getYear());
    }

    /***
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateMonthDisplay() {
        if (mCurrentTime.getYear() == mEndTime.getYear() && mCurrentTime.getMonthOfYear() >= mEndTime.getMonthOfYear()) {
            mMonthWheel.setCurrentItem(mMonthWheel.getAdapter().getItemsCount()-1);
        }
        else if (mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonthOfYear() <= mStartTime.getMonthOfYear()) {
             mMonthWheel.setCurrentItem(0);
        }
        else if (mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonthOfYear() > mStartTime.getMonthOfYear()) {
            mMonthWheel.setCurrentItem(mCurrentTime.getMonthOfYear() - mStartTime.getMonthOfYear());
        }
        else {
            mMonthWheel.setCurrentItem(mCurrentTime.getMonthOfYear()-1);
        }

    }

    /***
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateDayDisplay() {
        if (mCurrentTime.getYear() == mEndTime.getYear() && mCurrentTime.getMonthOfYear() == mEndTime.getMonthOfYear() && mCurrentTime.getDayOfMonth() >= mEndTime.getDayOfMonth()) {
            mDayWheel.setCurrentItem(mDayWheel.getAdapter().getItemsCount()-1);
        }
        else if (mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonthOfYear() == mStartTime.getMonthOfYear() && mCurrentTime.getDayOfMonth() <= mStartTime.getDayOfMonth()) {
            mDayWheel.setCurrentItem(0);
        }
        else if (mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonthOfYear() == mStartTime.getMonthOfYear() && mCurrentTime.getDayOfMonth() > mStartTime.getDayOfMonth()) {
            mDayWheel.setCurrentItem(mCurrentTime.getDayOfMonth() - mStartTime.getDayOfMonth());
        }
        else {
            mDayWheel.setCurrentItem(mCurrentTime.getDayOfMonth()-1);
        }
    }

    private void onTimeChanged() {
        updateMonthRange();
        updateDayRange();

        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, mCurrentTime.toDateTime());
        }
    }

}
