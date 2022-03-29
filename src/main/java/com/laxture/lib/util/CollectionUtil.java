package com.laxture.lib.util;

import android.os.Build;
import android.util.SparseArray;

import com.laxture.lib.java8.Function;
import com.laxture.lib.java8.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CollectionUtil {

    public static <T> int find(T[] array, T target) {
        if (target == null) return -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 从数组/原生数组中查找目标，对象使用equals方法，其它使用==号
     *
     * @param array
     * @param target
     * @return
     */
    public static <T> int find(List<T> array, T target) {
        if (target == null) return -1;
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(target)) {
                return i;
            }
        }
        return -1;
    }

    public static int find(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    public static int find(int[] array, long target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    /**
     * 倒序
     *
     * @param list
     * @return
     */
    public static <T> List<T> reverse(List<T> list) {
        if (list == null || list.size() == 0) {
            return list;
        }
        int lastIndex = list.size() - 1;
        for (int i = 0; i < list.size() / 2; i++) {
            T t = list.get(i);
            T temp = list.get(lastIndex - i);
            list.set(i, temp);
            list.set(lastIndex - i, t);
        }
        return list;
    }

    /**
     * 排序一个list数组
     *
     * @param list
     * @param cmp
     */
    public static <T> void sort(List<T> list, Comparator<T> cmp) {
        if (list == null || list.size() == 0) {
            return;
        }
        @SuppressWarnings("unchecked")
        T[] newlist = (T[]) list.toArray();
        Arrays.sort(newlist, cmp);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, newlist[i]);
        }
    }

    public static <T> ArrayList<T> convertToArrayList(T[] src) {
        ArrayList<T> t = new ArrayList<T>();
        for (int i = 0; i < src.length; i++) {
            t.add(src[i]);
        }
        return t;
    }

    public static <T> List<List<T>> pagenate(List<T> data, int pageSize) {
        List<List<T>> pages = new ArrayList<List<T>>();
        if (!Checker.isEmpty(data)) {
            int index;
            int dataSize = data.size();
            for (int i = 0; i < dataSize; i += pageSize) {
                index = (i + pageSize) > dataSize ? dataSize : (i + pageSize);
                pages.add(data.subList(i, index));
            }
        }
        return pages;
    }

    public static <T> List<T> wrapByList(T item) {
        List<T> list = new ArrayList<T>();
        list.add(item);
        return list;
    }

    public static <E> SparseArray<E> cloneSparseArray(SparseArray<E> array) {
        if (array == null) return null;
        synchronized (array) {
            if (Build.VERSION.SDK_INT > 14) return array.clone();
            SparseArray<E> clone = new SparseArray<E>(array.size());
            if (array.size() > 0) {
                for (int i = 0; i < array.size(); i++) {
                    clone.put(array.keyAt(i), array.valueAt(i));
                }
            }
            return clone;
        }
    }

    public static <K, V> void copyMap(Map<K, V> from, Map<K, V> to) {
        for (K key : from.keySet()) {
            to.put(key, from.get(key));
        }
    }

    //*************************************************************************
    // Collections Utils
    //*************************************************************************

    public static <T> T any(Collection<T> list) {
        if (Checker.isEmpty(list)) return null;
        return list.iterator().next();
    }

    public static <T> boolean contains(Collection<T> list, Predicate<T> func) {
        if (Checker.isEmpty(list)) return false;
        for (T item : list) {
            if (func.test(item)) return true;
        }
        return false;
    }

    public static <T> int indexOf(List<T> list, Predicate<T> func) {
        if (Checker.isEmpty(list)) return -1;

        for (int i = 0; i < list.size(); i++) {
            if (func.test(list.get(i))) return i;
        }
        return -1;
    }

    public static <T> int lastIndexOf(List<T> list, Predicate<T> func) {
        if (Checker.isEmpty(list)) return -1;

        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (func.test(list.get(i))) index = i;
        }
        return index;
    }

    public static <T> boolean addIfNotExisted(List<T> list, T item, Predicate<T> func) {
        if (contains(list, func)) return false;
        return list.add(item);
    }

    public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
        if (list == null) return null;
        List<R> mapResults = new ArrayList<>();
        for (T item : list) {
            mapResults.add(mapper.apply(item));
        }
        return mapResults;
    }

    public static <T> List<T> filter(List<T> list, Predicate<T> filter) {
        if (list == null) return null;
        List<T> filterResults = new ArrayList<>();
        for (T item : list) {
            if (filter.test(item)) filterResults.add(item);
        }
        return filterResults;
    }

    public static <T> String join(List<T> list, Function<T, String> mapper, String delimiter) {
        if (list == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<list.size(); i++) {
            sb.append(mapper.apply(list.get(i)));
            if (i != list.size()-1) sb.append(delimiter);
        }
        return sb.toString();
    }

}