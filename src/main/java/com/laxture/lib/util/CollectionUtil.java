package com.laxture.lib.util;

import android.os.Build;
import android.util.SparseArray;

import java.util.*;

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
        int lastIndex = list.size()-1;
        for(int i=0; i<list.size()/2; i++){
            T t = list.get(i);
            T temp = list.get(lastIndex-i);
            list.set(i, temp);
            list.set(lastIndex-i, t);
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
        T[] newlist = (T[])list.toArray();
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

    public static <T> List<List<T>> pagenate(List<T> data, int pageSize){
        List<List<T>> pages=new ArrayList<List<T>>();
        if(!Checker.isEmpty(data)){
            int index;
            int dataSize=data.size();
            for(int i=0;i<dataSize;i+=pageSize){
                index=(i+pageSize)>dataSize?dataSize:(i+pageSize);
                pages.add(data.subList(i,index));
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
                for (int i=0; i<array.size(); i++) {
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
}
