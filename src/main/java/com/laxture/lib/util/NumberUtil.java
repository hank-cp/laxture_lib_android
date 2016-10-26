package com.laxture.lib.util;

import java.text.DecimalFormat;

public class NumberUtil {

    public static String format(int num, String pattern) {
        return new DecimalFormat(pattern).format(num);
    }

    public static String format(long num, String pattern) {
        return new DecimalFormat(pattern).format(num);
    }

    public static String format(double num, String pattern) {
        return new DecimalFormat(pattern).format(num);
    }

    public static String format(float num, String pattern) {
        return new DecimalFormat(pattern).format(num);
    }

    public static int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (Exception ignored) {}
        return 0;
    }

    public static long parseLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (Exception ignored) {}
        return 0;
    }

    public static short parseShort(String text) {
        try {
            return Short.parseShort(text);
        } catch (Exception ignored) {}
        return 0;
    }

    public static byte parseByte(String text) {
        try {
            return Byte.parseByte(text);
        } catch (Exception ignored) {}
        return 0;
    }

}
