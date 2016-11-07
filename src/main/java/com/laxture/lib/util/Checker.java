package com.laxture.lib.util;

import android.util.SparseArray;

import com.laxture.lib.java8.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public final class Checker {

    private Checker() {}

    //********************* Empty Object Validation ***************************

    public static boolean isZero(Number n) {
        return n == null || n.intValue() == 0;
    }

    public static <T extends Number> boolean isZero(Optional<T> n) {
        return n.isPresent() && isZero(n.get());
    }

    public static boolean isPositive(Integer integer) {
        return integer != 0 && integer > 0;
    }

    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() <= 0 || "null".equals(text);
    }

    public static boolean isEmpty(Optional<? extends CharSequence> text) {
        return !text.isPresent() || isEmpty(text.get());
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isExistedFile(File file) {
        return file != null && file.exists();
    }

    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(File file) {
        return file == null || !file.exists() || file.length() <= 0;
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(SparseArray array) {
        return array == null || array.size() <= 0;
    }

    public static boolean isEmpty(Date date) {
        return date == null || date.getTime() == 0;
    }


    public static boolean isEmpty(JSONObject json) {
        return json == null || json.length() == 0;
    }

    public static boolean isEmpty(JSONArray json) {
        return json == null || json.length() == 0;
    }

}
