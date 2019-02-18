package com.laxture.lib.util;

import java.text.DecimalFormat;

public class NumberUtil {

    public static String format(Integer num, String pattern) {
        if (num == null) return "";
        return new DecimalFormat(pattern).format(num);
    }

    public static String format(Long num, String pattern) {
        if (num == null) return "";
        return new DecimalFormat(pattern).format(num);
    }

    public static String format(Double num, String pattern) {
        if (num == null) return "";
        return new DecimalFormat(pattern).format(num);
    }

    public static String format(float num, String pattern) {
        return new DecimalFormat(pattern).format(num);
    }

    public static int parseInt(String text) {
        try {
            if (text.contains(".")) return (int) parseDouble(text);
            return Integer.parseInt(text);
        } catch (Exception ignored) {}
        return 0;
    }

    public static long parseLong(String text) {
        try {
            if (text.contains(".")) return (long) parseDouble(text);
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

    public static float parseFloat(String text) {
        try {
            return Float.parseFloat(text);
        } catch (Exception ignored) {}
        return 0;
    }

    public static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (Exception ignored) {}
        return 0;
    }

    public static boolean isSameDouble(Double a, Double b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return Math.abs(a - b) < 1.0E-6;
    }

}
