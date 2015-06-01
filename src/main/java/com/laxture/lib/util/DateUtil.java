package com.laxture.lib.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.content.ContentResolver;

import com.laxture.lib.Configuration;

@SuppressLint("SimpleDateFormat")
@SuppressWarnings("deprecation")
public class DateUtil {

    public static final long SECOND_MILLIS = 1000;
    public static final long MINUTE_MILLIS = SECOND_MILLIS * 60;
    public static final long HOUR_MILLIS = MINUTE_MILLIS * 60;
    public static final long DAY_MILLIS = HOUR_MILLIS * 24;
    public static final long WEEK_MILLIS = DAY_MILLIS * 7;
    public static final long MONTH_MILLIS = DAY_MILLIS * 30;
    public static final long YEAR_MILLIS = DAY_MILLIS * 365;

    public final static String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";
    public final static String DEFAULT_TIME_FORMAT = "HH:mm";
    public final static String DEFAULT_DATETIME_FORMAT = "yyyy/MM/dd HH:mm";

    public final static String DATE_FORMAT_WITH_BAR="yyyy-MM-dd";

    private DateUtil() {}

    //*************************************************************************
    // Comparison
    //*************************************************************************

    public static boolean isSameDate(Date date1, Date date2) {
        return date1.getYear() == date2.getYear()
            && date1.getMonth() == date2.getMonth()
            && date1.getDate() == date2.getDate();
    }

    public static boolean isNextDate(Date date1, Date date2) {
        return date1.getYear() == date2.getYear()
            && date1.getMonth() == date2.getMonth()
            && Math.abs(date1.getDate() - date2.getDate()) == 1;
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        return date1.getYear() == date2.getYear()
                && date1.getMonth() == date2.getMonth();
    }

    //*************************************************************************
    // Format / Parse
    //*************************************************************************

    public static String formatDate(Date date) {
        return new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(date);
    }

    public static String formatDateTime(Date date) {
        return new SimpleDateFormat(DEFAULT_DATETIME_FORMAT).format(date);
    }

    public static String formatTime(Date date) {
        return new SimpleDateFormat(DEFAULT_TIME_FORMAT).format(date);
    }

    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static Date parseDate(String dateStr, String format) {
        try {
            return new SimpleDateFormat(format).parse(dateStr);
        } catch (ParseException e) {
            LLog.w("Parse data '%s' with format '%s' failed.", e, dateStr, format);
            return new Date();
        }
    }

    //*************************************************************************
    // Convert
    //*************************************************************************

    public static Long getTimeWithoutMillisecond(Date date) {
        return getTimeWithoutMillisecond(date.getTime());
    }

    public static Long getTimeWithoutMillisecond(Long date) {
        String timeStr = Long.toString(date);
        return (timeStr.length() >= 3)
            ? Long.parseLong(timeStr.substring(0, timeStr.length()-3)) : 0;
    }

    public static int getDateInterval(Date date1, Date date2) {
        return Math.round((date1.getTime()-date2.getTime())/DAY_MILLIS);
    }

    public static final String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    public static String getDayOfWeek(int i){
        return weekDays[i%weekDays.length];
    }

    //*************************************************************************
    // Calculation
    //*************************************************************************

    /**
     * @return days since the given date. at least 1.
     */
    public static int sinceDate(Date date, long currentTimeMillis) {
        if (date == null
                || date.getTime() >= currentTimeMillis) return 0;
        long delta = currentTimeMillis - date.getTime();
        return ((int) Math.floor(delta/(double)DAY_MILLIS)) + 1;
    }

    public static int untilDate(Date date, long currentTimeMillis) {
        if (date == null
                || date.getTime() <= currentTimeMillis) return 0;
        long delta = date.getTime() - currentTimeMillis;
        return ((int) Math.floor(delta/(double)DAY_MILLIS)) + 1;
    }

    public static int sinceLastAnniversary(Date date, long currentTimeMillis) {
        if (date == null) return 0;
        return sinceDate(getLastAnniversary(date, currentTimeMillis), currentTimeMillis);
    }

    public static int untilNextAnniversary(Date date, long currentTimeMillis) {
        if (date == null) return 0;
        return untilDate(getNextAnniversary(date, currentTimeMillis), currentTimeMillis);
    }

    //*************************************************************************
    // Operate
    //*************************************************************************

    public static Date shiftDate(Date date, int days) {
        date.setDate(date.getDate() + days);
        return date;
    }

    public static Date trimDateByDate(Date date) {
        GregorianCalendar cal = new GregorianCalendar(date.getYear(), date.getMonth(), date.getDate());
        return cal.getTime();
    }

    public static Date getLastAnniversary(Date date, long currentTimeMillis) {
        if (date == null) return null;

        // future date
        if (date.getTime() > currentTimeMillis) return date;

        // before date, move to last anniversary
        Date now = trimDateByDate(new Date(currentTimeMillis));
        Date tmpDate = new Date(date.getTime());
        tmpDate.setYear(now.getYear());
        if (tmpDate.getTime() > currentTimeMillis) {
            tmpDate.setYear(now.getYear()-1);
        }
        return tmpDate;
    }

    public static Date getNextAnniversary(Date date, long currentTimeMillis) {
        if (date == null) return null;
        Date tmpDate = null;

        // future date
        if (date.getTime() > currentTimeMillis) return date;

        // before date, move to next anniversary
        Date now = trimDateByDate(new Date(currentTimeMillis));
        tmpDate = new Date(date.getTime());
        tmpDate.setYear(now.getYear());
        if (tmpDate.getTime() < currentTimeMillis) {
            tmpDate.setYear(now.getYear()+1);
        }

        return tmpDate;
    }

    //*************************************************************************
    // System
    //*************************************************************************

    public static boolean is24HourFormat() {
        boolean is24Hour = true;

        ContentResolver cv = Configuration.getInstance().getAppContext().getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv,
                android.provider.Settings.System.TIME_12_24);

        if (null != strTimeFormat && !strTimeFormat.equals("24")) {
            is24Hour = false;
        }
        return is24Hour;
    }

    //*************************************************************************
    // Deparecated - Should be replaced by JODA (http://joda-time.sourceforge.net/)
    //*************************************************************************

    public static boolean isSameDate(Calendar c1, Calendar c2) {
        return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH));
    }

    public static Integer getDayKey(Calendar date) {
        return date.get(Calendar.YEAR) * 10000
                + (date.get(Calendar.MONTH) + 1) * 100
                + date.get(Calendar.DAY_OF_MONTH);
    }

    public static int compareDate(Calendar first, Calendar second) {
        return getDatePriority(first) - getDatePriority(second);
    }

    private static int getDatePriority(Calendar c) {
        return c.get(Calendar.YEAR) * 10000 + c.get(Calendar.MONTH) * 100 + c.get(Calendar.DAY_OF_MONTH);
    }

    public static int compareAlarm(Calendar first, Calendar second) {
        return getAlarmPriority(first) - getAlarmPriority(second);
    }

    private static int getAlarmPriority(Calendar c) {
        return c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE);
    }

    /**
     * 把某一个时间向后设置一个小时
     *
     * @param calendar 需要设置的时间
     * @return
     */
    public static Calendar getNextHour(Calendar calendar) {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.add(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 取得指定毫秒后的时间
     *
     * @param calendar
     * @param timeAfter
     * @return
     */
    public static Calendar getTimeMillisAfter(Calendar calendar, long timeAfter) {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(calendar.getTimeInMillis() + timeAfter);
        return newCalendar;
    }

    public static Calendar getCalendarBefore(Calendar calendar, int beforeDays) {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                - beforeDays, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        return newCalendar;
    }

    public static Calendar getCalendarAfter(Calendar calendar, int afterDays) {
        // Calendar newCalendar = (Calendar)calendar.clone();
        // newCalendar.add(Calendar.DAY_OF_MONTH, afterDays);

        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                + afterDays, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        return newCalendar;
    }

    public static Calendar newCalendarByDay(Calendar calendar) {
        Calendar newCalendar = (Calendar) calendar.clone();
        newCalendar.set(Calendar.HOUR_OF_DAY, 0);
        newCalendar.set(Calendar.MINUTE, 0);
        newCalendar.set(Calendar.SECOND, 0);
        newCalendar.set(Calendar.MILLISECOND, 0);
        return newCalendar;
    }

    public static Calendar trimCalendarByDay(Calendar calendar) {
        if (calendar == null) return calendar;
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Calendar trimCalendarByMinute(Calendar calendar) {
        if (calendar == null) return null;
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Date keepDate(Date d){
        d.setTime(d.getTime()-d.getTime()%1000);
        d.setHours(0);
        d.setMinutes(0);
        d.setSeconds(0);
        return d;
    }

}
