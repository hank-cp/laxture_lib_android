package com.laxture.lib.util;

import android.os.Build;
import android.util.SparseArray;

import java.util.*;

public class ArrayUtil {

    public static <T> int len(T[] array){
        if(array==null){
            return 0;
        }
        return array.length;
    }

    public static <E> int len(List<E> list){
        if(list==null){
            return 0;
        }
        return list.size();
    }

    public static <T> int find(T[] array, T target) {
        if (target == null) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 查找按照op计算出来的最小的元素
     * @param array
     * @param op
     * @param <T>
     * @return
     */
    public static <T> T findMinItem(T[] array,ValueOP<T> op){
        if(Checker.isEmpty(array) || op==null){
            return null;
        }
        List<T> listData=Arrays.asList(array);
        return findMinItem(listData,op);
    }

    /**
     * 查找最小值，如果没有元素，返回Long的最大值
     * @param array
     * @param op
     * @param <T>
     * @return
     */
    public static <T> long findMinValue(List<T> array,ValueOP<T> op){
        T item=findMinItem(array,op);
        if(item==null){
            return Long.MAX_VALUE;
        }
        return op.getValue(item);
    }

    /**
     * 查找按照op计算出来的最小的元素
     * @param array
     * @param <T>
     * @return
     */
    public static <T> T findMinItem(List<T> array,ValueOP<T> op){
        if(Checker.isEmpty(array) || op==null){
            return null;
        }
        long value=Long.MAX_VALUE;
        long temp;
        int index=-1;
        for (int i = 0; i < array.size(); i++) {
            temp=op.getValue(array.get(i));
             if(temp<value){
                 value=temp;
                 index=i;
             }
        }
        if(index==-1){
            return null;
        }
        return array.get(index);
    }

    /**
     * 查找按照op计算出来的最小的元素
     * @param array
     * @param <T>
     * @return
     */
    public static <T> T findMaxItem(List<T> array,ValueOP<T> op){
        if(Checker.isEmpty(array) || op==null){
            return null;
        }
        long value=Long.MIN_VALUE;
        long temp;
        int index=-1;
        for (int i = 0; i < array.size(); i++) {
            temp=op.getValue(array.get(i));
            if(temp>value){
                value=temp;
                index=i;
            }
        }
        if(index==-1){
            return null;
        }
        return array.get(index);
    }

    /**
     * 从数组/原生数组中查找目标，对象使用equals方法，其它使用==号
     *
     * @param array
     * @param target
     * @return
     */
    public static <T> int find(List<T> array, T target) {
        if (target == null) {
            return -1;
        }
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(target)) {
                return i;
            }
        }
        return -1;
    }

    public static int find(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static int find(int[] array, long target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static <T> String join(T[] array, String sp){
        if(array==null || array.length==0){
            return "";
        }
        return join(Arrays.asList(array), sp,null);
    }

    /**
     * 把数组对象join成一个字符串，调用toString方法
     *
     * @param list
     * @param sp
     * @return
     */
    public static <T> String join(Collection<T> list, String sp, StringValue<T> op) {

        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i=0;
        for (T item : list) {
            sb.append(op==null?item.toString():op.toString(item));
            if (++i < list.size()) {
                sb.append(sp);
            }
        }
        return sb.toString();
    }

    public static <T> T findFirst(List<T> array, EqualeOP<T> cmp) {
        if (cmp == null) {
            return null;
        }
        for (int i = 0; i < array.size(); i++) {
            if (cmp.test(array.get(i),i)) {
                return array.get(i);
            }
        }
        return null;
    }

    /**
     * 从list中过滤出通过cmp.test方法为true的内容，生成另外一个数组
     *
     * @param list
     * @param cmp
     * @return
     */
    public static <T> List<T> filter(List<T> list, EqualeOP<T> cmp) {
        if (list == null || list.size() == 0) {
            return list;
        }
        List<T> result = new ArrayList<T>();
        for (int i = 0; i < list.size(); i++) {
            if (cmp.test(list.get(i),i)) {
                result.add(list.get(i));
            }
        }
        return result;
    }

    public static <T> void each(List<T> list, Processor<T> p){

        if (list == null || list.size() == 0) {
            return;
        }
        for(int i=list.size()-1;i>=0;i--){
            T object=list.get(i);
            p.process(object,i);
        }
    }

    /**
     * 从list中去除通过cmp.test方法为true的内容，在原来数组上面操作
     *
     * @param list
     * @param cmp
     * @return
     */
    public static <T> List<T> remove(List<T> list, EqualeOP<T> cmp) {
        if (list == null || list.size() == 0) {
            return list;
        }
        for(int i=list.size()-1;i>=0;i--){
            T object=list.get(i);
            if (cmp.test(object,i)) {
                list.remove(i);
            }
        }
        /*
        for (Iterator<T> i = list.iterator(); i.hasNext();) {
            T object = i.next();
            if (cmp.test(object,0)) {
                i.remove();
            }
        }
        */
        return list;
    }

    /**
     * 从list中保留调用cmp.test方法为true的item，在原来数组上面操作
     *
     * @param list
     * @param cmp
     * @return
     */
    public static <T> List<T> keep(List<T> list, EqualeOP<T> cmp) {
        if (list == null || list.size() == 0) {
            return list;
        }
        for(int i=list.size()-1;i>=0;i--){
            T object=list.get(i);
            if (!cmp.test(object,i)) {
                list.remove(i);
            }
        }
        /*
        for (Iterator<T> i = list.iterator(); i.hasNext();) {
            T object = i.next();
            if (!cmp.test(object)) {
                i.remove();
            }
        }
        */
        return list;
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
        int lastIndex=list.size()-1;
        for(int i=0;i<list.size()/2;i++){
            T t=list.get(i);
            T temp=list.get(lastIndex-i);
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

    /**
     * 从原生的一个数组转换为list
     *
     * @param src
     * @return
     */
    public static <T> ArrayList<T> from(T[] src) {
        ArrayList<T> t = new ArrayList<T>();
        for (int i = 0; i < src.length; i++) {
            t.add(src[i]);
        }
        return t;
    }

    /**
     *  这个应该可以用Comparator，Comparator里面有一个equals
     */
    public static class EqualeOP<T> {
        public boolean test(T src,int index){
            return test(src);
        }
        public boolean test(T src){
            return false;
        }
    }

    /**
     * 不用排序的功能，因为排序是对比两个，
     * 所以相对来说需要对每个元素运算的次数多一些
     * @param <T>
     */
    public static interface ValueOP<T> {
        public long getValue(T src);
    }

    public static interface Processor<T> {
        public void process(T obj, int i);
    }


    public static interface StringValue<T>{
        public String toString(T obj);
    }

    public static <T> List<List<T>> pagenate(List<T> data,int pageSize){
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
}
