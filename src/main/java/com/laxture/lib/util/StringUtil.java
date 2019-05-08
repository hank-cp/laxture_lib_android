package com.laxture.lib.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

public class StringUtil {

    /**
     * 省略号字符
     */
    private static final String ELIPSE_CHARACTOR="…";

    /**
     * 后边加省略号
     * @param rs   源字符串
     * @param length 最大字符长度，超过这个长度之后后面的显示为一个省略号
     * @return
     */
    public static String getElipseString(String rs, int length) {
        if (Checker.isEmpty(rs)) return "";
        if (rs.length() <= length) return rs;
        return rs.substring(0, length)+ELIPSE_CHARACTOR;
    }

    public static String join(Collection<?> stringList, String joiner) {
        if (Checker.isEmpty(stringList)) return "";
        StringBuilder sb = new StringBuilder();
        for (Object str : stringList) {
            sb.append(str.toString()).append(joiner);
        }
        if (sb.length() > 0) sb.delete(sb.length()-1, sb.length());
        return sb.toString();
    }

    public static boolean isChinese(char c) {

        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    /**
     * 字符按照宽字符计算，一个英文字符为0.5个
     * @param rs
     * @param length  显示出来的有用信息不超过的长度
     * @return
     */
    public static String getElipseStringWide(String rs,int length){
          if(TextUtils.isEmpty(rs)){
              return "";
          }
          int totalCount=length*2;
          int count=0;
          int i=0;
          boolean needCut=false;
          for(i=0;i<rs.length();i++){
              count+=(isChinese(rs.charAt(i))?2:1);
              if(count>totalCount){
                   needCut=true;
                   break;
              }
          }
          if(needCut){
              return rs.substring(0,i)+ ELIPSE_CHARACTOR;
          }
          return rs;
    }

    /**
     * 字符按照宽字符计算，一个英文字符为0.666个
     * @param rs
     * @param length  显示出来的有用信息不超过的长度
     * @return
     */
    public static String getElipseStringWide2(String rs, int length){
        if(TextUtils.isEmpty(rs)){
            return "";
        }
        int totalCount=length*2;
        float count=0;
        int i=0;
        boolean needCut=false;
        for(i=0;i<rs.length();i++){
            count+=(isChinese(rs.charAt(i))
                    || rs.charAt(i) == 'W' || rs.charAt(i) == 'w') ? 2 : 1.5;
            if(count>totalCount){
                 needCut=true;
                 break;
            }
        }
        if(needCut){
            return rs.substring(0,i)+ ELIPSE_CHARACTOR;
        }
        return rs;
    }

    /**
    *
    * @param inputStream
    * @return
    */
    public static String getFromStream(InputStream inputStream){
        return getFromStream(inputStream, "UTF-8");
    }

    /**
     *
     * @return
     */
    public static String getFromStream(InputStream is, String codec) {
        try {
            InputStreamReader isr;
            if (TextUtils.isEmpty(codec)) {
                isr=new InputStreamReader(is);
            } else {
                isr=new InputStreamReader(is, codec);
            }
            BufferedReader in = new BufferedReader(isr);

            StringBuilder buffer = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
            return buffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * fix JSON with null like {"abc":null}
     * @param object
     * @param key
     * @return
     */
    public String optStringFromJSON(JSONObject object, String key) {
        if (object == null || object.isNull(key)) return null;
        return object.optString(key);
    }

    /**
     * fix JSON with null like [null]
     * @param object
     * @param index
     * @return
     */
    public String optStringFromJSON(JSONArray object, int index) {
        if (object == null || object.isNull(index)) return null;
        return object.optString(index);
    }

}
