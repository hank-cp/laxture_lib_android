package com.laxture.lib.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;

import com.laxture.lib.RuntimeContext;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    private static final SimpleDateFormat J_DATA_TIME_FORMATTER =
            new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    private static final SimpleDateFormat J_DATA_FORMATTER =
            new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    private static final SimpleDateFormat J_TIME_FORMATTER =
            new SimpleDateFormat(DEFAULT_TIME_FORMAT);

    private static final DateTimeFormatter JODA_DATE_TIME_FORMATTER =
            DateTimeFormat.forPattern(DEFAULT_DATETIME_FORMAT);
    private static final DateTimeFormatter JODA_DATE_FORMATTER =
            DateTimeFormat.forPattern(DEFAULT_DATE_FORMAT);
    private static final DateTimeFormatter JODA_TIME_FORMATTER =
            DateTimeFormat.forPattern(DEFAULT_TIME_FORMAT);

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
    // Format / Parse java.util.Date
    //*************************************************************************

    public static String formatDate(Date date) {
        if (date == null) return "";
        return J_DATA_FORMATTER.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return J_DATA_TIME_FORMATTER.format(date);
    }

    public static String formatTime(Date date) {
        if (date == null) return "";
        return J_TIME_FORMATTER.format(date);
    }

    public static String formatDate(Date date, String format) {
        if (date == null) return "";
        return new SimpleDateFormat(format).format(date);
    }

    //*************************************************************************
    // Format / Parse java.util.Date
    //*************************************************************************

    public static String formatDate(DateTime date) {
        if (date == null) return "";
        return JODA_DATE_FORMATTER.print(date);
    }

    public static String formatDateTime(DateTime date) {
        if (date == null) return "";
        return JODA_DATE_TIME_FORMATTER.print(date);
    }

    public static String formatTime(DateTime date) {
        if (date == null) return "";
        return JODA_TIME_FORMATTER.print(date);
    }

    public static String formatDate(DateTime date, String format) {
        if (date == null) return "";
        return DateTimeFormat.forPattern(format).print(date);
    }

    //*************************************************************************
    // System
    //*************************************************************************

    public static boolean is24HourFormat() {
        boolean is24Hour = true;

        ContentResolver cv = RuntimeContext.getApplication().getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv,
                android.provider.Settings.System.TIME_12_24);

        if (null != strTimeFormat && !strTimeFormat.equals("24")) {
            is24Hour = false;
        }
        return is24Hour;
    }

}
