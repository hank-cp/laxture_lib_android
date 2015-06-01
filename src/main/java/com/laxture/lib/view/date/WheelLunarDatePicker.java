package com.laxture.lib.view.date;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.laxture.lib.R;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.SLLunarDate;

public class WheelLunarDatePicker extends FrameLayout {

    public WheelLunarDatePicker(Context context) {
        super(context);
    }

    private static int START_YEAR = 1930, END_YEAR = 2048;

    private SLLunarDate mCurrentTime = new SLLunarDate(new Date());

    private SLLunarDate mStartTime = new SLLunarDate(new DateTime(START_YEAR,
            1, 30, 0, 0).toDateMidnight());

    private SLLunarDate mEndTime = new SLLunarDate(new DateTime(END_YEAR + 1,
            1, 1, 0, 0).toDateMidnight());

    private WheelView mYearWheel = null;

    private WheelView mMonthWheel = null;

    private WheelView mDayWheel = null;

    private final static String[] mMonthName = { "正月", "二月", "三月", "四月", "五月",
            "六月", "七月", "八月", "九月", "十月", "冬月", "腊月" };

    private final static String[] mDayName1 = { "初一", "初二", "初三", "初四", "初五",
            "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六",
            "十七", "十八", "十九", "廿十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七",
            "廿八", "廿九", "三十" };

    private final static String[] mDayName2 = { "初一", "初二", "初三", "初四", "初五",
            "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六",
            "十七", "十八", "十九", "廿十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七",
            "廿八", "廿九" };

    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener = null;

    private final ArrayWheelAdapter<String> mAdapterDefaultMonth = new ArrayWheelAdapter<String>(
            mMonthName, 6);

    private final ArrayWheelAdapter<String> mAdapterBigMonthDay = new ArrayWheelAdapter<String>(
            mDayName1, 4);

    private final ArrayWheelAdapter<String> mAdapterSmallMonthDay = new ArrayWheelAdapter<String>(
            mDayName2, 4);

    /***
     * A no-op callback used in the constructor to avoid null checks later in
     * the code.
     */
    public interface OnTimeChangedListener {

        /***
         * @param view
         *            The view associated with this listener.
         * @param lunarDate
         *            The current lunarDate.
         */
        void onTimeChanged(WheelLunarDatePicker view, SLLunarDate lunarDate);
    }

    public WheelLunarDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelLunarDatePicker(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.wheel_lunar_date_picker, this, true); // we
                                                                        // are
                                                                        // the
                                                                        // parent

        // 年
        mYearWheel = (WheelView) findViewById(R.id.wv_year);
        mYearWheel.setAdapter(new LunarYearWheelAdapter(START_YEAR, END_YEAR));

        // 月
        mMonthWheel = (WheelView) findViewById(R.id.wv_month);
        mMonthWheel.setAdapter(mAdapterDefaultMonth);

        // 日
        mDayWheel = (WheelView) findViewById(R.id.wv_day);
        mDayWheel.setAdapter(mAdapterBigMonthDay);

        mYearWheel.addChangingListener(new OnWheelChangedListener() {

            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mCurrentTime.setYear(newValue + mStartTime.getYear());
                updateAdapters();
                onTimeChanged();
                updateMonthDisplay();
                updateDayDisplay();
                if (mCurrentTime.isLeapMonth() && newValue != oldValue) {
                    mCurrentTime.setMonth(mCurrentTime.getMonth());
                    mCurrentTime.setLeapMonth(false);
                }
                int bit = SLLunarDate.getLunarYearBit(newValue + mStartTime.getYear());
                int leapbit = ((bit >> 20) & 15);
                if ((leapbit != 0 && mCurrentTime.getMonth() >= leapbit))
                    mMonthWheel.setCurrentItem(mMonthWheel.getCurrentItem() + 1);

                if (mCurrentTime.getMonth() + 1 == leapbit)
                     mMonthWheel.setCurrentItem(mMonthWheel.getCurrentItem() + 1);
            }
        });

        mMonthWheel.addChangingListener(new OnWheelChangedListener() {

            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int bit = SLLunarDate.getLunarYearBit(mCurrentTime.getYear());
                int leapbit = ((bit >> 20) & 15);
                // 没有闰月
                if (leapbit == 0) {
                    if (mCurrentTime.getYear() == mStartTime.getYear())
                        mCurrentTime.setMonth(newValue+mStartTime.getMonth());
                    else
                        mCurrentTime.setMonth(newValue + 1);

                    // 选中闰月之前的月份
                } else if (leapbit + 1 > newValue + 1) {
                    if( mCurrentTime.getYear() == mStartTime.getYear())
                        mCurrentTime.setMonth(newValue+mStartTime.getMonth());
                    else
                        mCurrentTime.setMonth(newValue + 1);
                    mCurrentTime.setLeapMonth(false);

                    // 选中闰月
                } else if (leapbit + 1 == newValue + 1) {
                    if (mCurrentTime.getYear() == mStartTime.getYear())
                        mCurrentTime.setMonth(newValue + mStartTime.getMonth() - 1);
                    else
                        mCurrentTime.setMonth(newValue);
                    mCurrentTime.setLeapMonth(true);

                    // 选中闰月之后的月份
                } else if (leapbit + 1 < newValue + 1) {
                    if (mCurrentTime.getYear() == mStartTime.getYear())
                        mCurrentTime.setMonth(newValue + mStartTime.getMonth() - 1);
                    else
                        mCurrentTime.setMonth(newValue);
                    mCurrentTime.setLeapMonth(false);
                }

                updateAdapters();
                onTimeChanged();
                updateDayDisplay();
            }
        });

        mDayWheel.addChangingListener(new OnWheelChangedListener() {

            public void onChanged(WheelView wheel, int oldValue, int newValue) {

                if(mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonth() == mStartTime.getMonth()) {
                     mCurrentTime.setDay(newValue+mStartTime.getDay());
                }
                else
                    mCurrentTime.setDay(newValue + 1);
                onTimeChanged();
            }
        });

        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.DateTimePicker, 0, 0);
        int textSize = array.getDimensionPixelOffset(
                R.styleable.DateTimePicker_textSize, 18);
        mYearWheel.TEXT_SIZE = textSize;
        mMonthWheel.TEXT_SIZE = textSize;
        mDayWheel.TEXT_SIZE = textSize;
        array.recycle();
    }

    private void updateAdapters() {
        int bit = SLLunarDate.getLunarYearBit(mCurrentTime.getYear());

        // 判断这个年是否是闰年
        // 0~4共5bit 春节日分
        // 5~6共2bit 春节月份
        // 7~19共13bit 13个月的大小情况(如无闰月,最后位无效),大月为1,小月为0
        // 20~23共4bit 记录闰月的月份,如果没有则闰月为0
        int leapbit = ((bit >> 20) & 15);
        if (leapbit == 0) {
            // 不是闰年
            mMonthWheel.setAdapter(mAdapterDefaultMonth);
            // 防止越界
            if (mCurrentTime.getMonth() == 13) {
                mMonthWheel.setCurrentItem(11);
                mCurrentTime.setMonth(12);
            }

        } else {
            // 是闰年，月份就是leapbit，leapbit=4，则是闰四月
            ArrayList<String> ml = new ArrayList<String>(
                    Arrays.asList(mMonthName));
            ml.add(leapbit, "闰" + mMonthName[leapbit - 1]);
            String[] str = ml.toArray(new String[1]);
            mMonthWheel.setAdapter(new ArrayWheelAdapter<String>(str, 6));
        }

        // 看当前月是大月还是小月
        int bigbit = ((bit >> 7) & (1 << mCurrentTime.getMonth() + 1));
        if (bigbit == 0) {
            mDayWheel.setAdapter(mAdapterSmallMonthDay);
            if (mCurrentTime.getDay() == 30) { // 如果是三十，就选到29
                mDayWheel.setCurrentItem(28);
                mCurrentTime.setDay(29);
            }
        } else {
            mDayWheel.setAdapter(mAdapterBigMonthDay);
        }
    }

    /***
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener
     *            the callback, should not be null.
     */
    public void setOnTimeChangedListener(
            OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * Set the current time
     */
    private void setTime() {
        // 沒有开始和结束的任何限制，用默认的限制
        if (mCurrentTime.getYear() < START_YEAR) {
            mCurrentTime.setYear(START_YEAR);
            mOnTimeChangedListener.onTimeChanged(this, mCurrentTime);
        } else if (mCurrentTime.getYear() > END_YEAR) {
            mCurrentTime.setYear(END_YEAR);
            mOnTimeChangedListener.onTimeChanged(this, mCurrentTime);
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
           mYearWheel.setAdapter(new LunarYearWheelAdapter(mStartTime.getYear(),
                    mEndTime.getYear()));

        LLog.d("Year Range :: %s ~ %s",
                mYearWheel.getAdapter().getItem(mStartTime.getYear()), mYearWheel
                        .getAdapter().getItem(mEndTime.getYear()));
    }

    private void updateMonthRange() {
        // 不允许超过现在时间限制最大月
        if (mCurrentTime.getYear() == mEndTime.getYear()){
            int bit = SLLunarDate.getLunarYearBit(mCurrentTime.getYear());
            int leapbit = ((bit >> 20) & 15);
            String[] endStr = new String[mEndTime.getMonth()];
            if (leapbit == 0) {
                // 不是闰年
                mMonthWheel.setAdapter(mAdapterDefaultMonth);
                for (int i = 0; i < mEndTime.getMonth(); i++)
                    endStr[i] = mMonthName[i];
                // 防止越界
                if (mCurrentTime.getMonth() == 13) {
                    mMonthWheel.setCurrentItem(11);
                    mCurrentTime.setMonth(12);
                }

            } else {
                // 是闰年，月份就是leapbit，leapbit=4，则是闰四月
                ArrayList<String> ml = new ArrayList<String>(
                        Arrays.asList(mMonthName));
                ml.add(leapbit, "闰" + mMonthName[leapbit - 1]);
                String[] str = ml.toArray(new String[1]);
                for (int i = 0; i < mEndTime.getMonth(); i++)
                    endStr[i] = str[i];
            }
            mMonthWheel.setAdapter(new ArrayWheelAdapter<String>(endStr, 6));
        }

        if (mCurrentTime.getYear() == mStartTime.getYear()) {
            int bit = SLLunarDate.getLunarYearBit(mStartTime.getYear());
            int leapbit = ((bit >> 20) & 15);
            if (leapbit == 0) {
                // 不是闰年
                String[] startStr = new String[13 - mStartTime.getMonth()];
                mMonthWheel.setAdapter(mAdapterDefaultMonth);
                for (int i = mStartTime.getMonth(); i < 13 ; i++)
                    startStr[i - mStartTime.getMonth()] = mMonthName[i - 1];
                // 防止越界
                if (mCurrentTime.getMonth() == 13) {
                    mMonthWheel.setCurrentItem(11);
                    mCurrentTime.setMonth(12);
                }
                mMonthWheel.setAdapter(new ArrayWheelAdapter<String>(startStr, 6));
            } else {
                // 是闰年，月份就是leapbit，leapbit=4，则是闰四月
                String[] startStr = new String[14 - mStartTime.getMonth()];
                ArrayList<String> ml = new ArrayList<String>(
                        Arrays.asList(mMonthName));
                ml.add(leapbit, "闰" + mMonthName[leapbit - 1]);
                String[] str = ml.toArray(new String[1]);
                for (int i = mStartTime.getMonth() - 1; i < 13 ; i++)
                        startStr[i - mStartTime.getMonth() + 1] = str[i];
                mMonthWheel.setAdapter(new ArrayWheelAdapter<String>(startStr, 6));
            }
        }
    }

    private void updateDayRange() {
       //不允许超过现在时间限制最大日
       if (mCurrentTime.getYear() == mEndTime.getYear() && mEndTime.getMonth() == mCurrentTime.getMonth()) {
           String[] endStr = new String[mEndTime.getDay()];
           for(int i = 0 ; i < mEndTime.getDay(); i++)
                 endStr[i] = mDayName1[i];
           mDayWheel.setAdapter(new ArrayWheelAdapter<String>(endStr, 4));
       }

       //不允许少于最小时间限制最小日
       if (mCurrentTime.getYear() == mStartTime.getYear() && mStartTime.getMonth() == mCurrentTime.getMonth()) {
           int bit = SLLunarDate.getLunarYearBit(mCurrentTime.getYear());
           // 看当前月是大月还是小月
           int bigbit = ((bit >> 7) & (1 << mCurrentTime.getMonth() + 1));
           if (bigbit == 0) {
               String[] endStr = new String[30 - mStartTime.getDay()];
               for(int i = mStartTime.getDay() ; i < 30; i++)
                   endStr[i - mStartTime.getDay()] = mDayName2[i - 1];
               mDayWheel.setAdapter(new ArrayWheelAdapter<String>(endStr, 4));
               if (mCurrentTime.getDay() == 30) { // 如果是三十，就选到29
                   mDayWheel.setCurrentItem(28);
                   mCurrentTime.setDay(29);
               }
           } else {
               String[] endStr = new String[31 - mStartTime.getDay()];
               for(int i = mStartTime.getDay() ; i < 31; i++)
                   endStr[i - mStartTime.getDay()] = mDayName1[i - 1];
               mDayWheel.setAdapter(new ArrayWheelAdapter<String>(endStr, 4));
           }
       }
    }

    public void setCurrentTime(SLLunarDate lunarDate) {
        int bit = SLLunarDate.getLunarYearBit(lunarDate.getYear());
        int leapbit = ((bit >> 20) & 15);

        if((leapbit != 0 && lunarDate.getMonth() > leapbit) || lunarDate.isLeapMonth()) {
            mCurrentTime = new SLLunarDate(lunarDate.getYear(),
                lunarDate.getMonth() + 1, lunarDate.getDay(),
                lunarDate.isLeapMonth());
        }
        else {
            mCurrentTime = new SLLunarDate(lunarDate.getYear(),
                    lunarDate.getMonth(), lunarDate.getDay(),
                    lunarDate.isLeapMonth());
        }
        setTime();
    }

    public SLLunarDate getCurrentTime() {
        return new SLLunarDate(mCurrentTime.getYear(), mCurrentTime.getMonth(),
                mCurrentTime.getDay(), mCurrentTime.isLeapMonth());
    }

    public void setStartTime(SLLunarDate startTime) {
        mStartTime = startTime;
        if (mCurrentTime.getDate().before(startTime.getDate())) {
            setCurrentTime(startTime);
        }
        setTime();
    }

    public void setEndTime(SLLunarDate endTime) {
        if (mStartTime == null && mStartTime.getDate().after(endTime.getDate())) {
            return;
        }
        mEndTime = endTime;

        if (mCurrentTime.getDate().after(endTime.getDate())) {
            setCurrentTime(endTime);
        }
        setTime();
    }

    /***
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateYearDisplay() {
        mYearWheel.setCurrentItem(mCurrentTime.getYear() - mStartTime.getYear());
    }

    /***
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateMonthDisplay() {
        if (mCurrentTime.getYear() == mEndTime.getYear() && mCurrentTime.getMonth() >= mEndTime.getMonth()) {
            mMonthWheel.setCurrentItem(mMonthWheel.getAdapter().getItemsCount()-1);
        } else if (mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonth() <= mStartTime.getMonth()) {
            mMonthWheel.setCurrentItem(0);
        } else if (mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonth() > mStartTime.getMonth()) {
           mMonthWheel.setCurrentItem(mCurrentTime.getMonth() - mStartTime.getMonth());
        } else {
          mMonthWheel.setCurrentItem(mCurrentTime.getMonth() - 1);
          int bit = SLLunarDate.getLunarYearBit(mCurrentTime.getYear());
          int leapbit = ((bit >> 20) & 15);
          if ((leapbit != 0 && mCurrentTime.getMonth() >= leapbit))
              mCurrentTime.setMonth(mMonthWheel.getCurrentItem());
        }

    }

    /***
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateDayDisplay() {
         if (mCurrentTime.getYear() == mEndTime.getYear() && mCurrentTime.getMonth() == mEndTime.getMonth() && mCurrentTime.getDay() >= mEndTime.getDay()) {
             mDayWheel.setCurrentItem(mDayWheel.getAdapter().getItemsCount() - 1);
         } else if (mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonth() == mStartTime.getMonth() && mCurrentTime.getDay() <= mStartTime.getDay()) {
             mDayWheel.setCurrentItem(0);
         } else if (mCurrentTime.getYear() == mStartTime.getYear() && mCurrentTime.getMonth() == mStartTime.getMonth() && mCurrentTime.getDay() > mStartTime.getDay()) {
             mDayWheel.setCurrentItem(mCurrentTime.getDay() - mStartTime.getDay());
         } else {
             mDayWheel.setCurrentItem(mCurrentTime.getDay() - 1);
         }
    }

    private void onTimeChanged() {
        updateMonthRange();
        updateDayRange();

        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, mCurrentTime);
        }
    }

}
