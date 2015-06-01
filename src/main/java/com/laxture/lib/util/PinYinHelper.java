package com.laxture.lib.util;

import com.laxture.lib.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * 简化拼音helper，减少内存占用
 */
public class PinYinHelper {

    // 以unicode为key pinyin为value
    private static SoftReference<HashMap<String, String>> sMapRef;

    private static HashMap<String, String> getMap() {
        if (sMapRef == null || sMapRef.get() == null) {
            byte[] cachedData = AssetLoader.readAssetToBytes("pinyin/pinyin.dat");

            // try load from serialized cache first
            if (!Checker.isEmpty(cachedData)) {
                HashMap<String, String> map = SerializationHelper.deserialize(cachedData);
                sMapRef = new SoftReference<HashMap<String,String>>(map);
                return map;
            }

            // load from raw data. be careful that here will be extremely slow
            HashMap<String, String> map = new HashMap<String, String>();
            sMapRef = new SoftReference<HashMap<String,String>>(map);
            InputStream is = null;
            try {
                is = Configuration.getInstance().getAppContext()
                        .getResources().getAssets().open("pinyin/unicode_to_hanyu_pinyin.txt");
                BufferedReader breader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = breader.readLine()) != null && line.length()>0) {
                    // 提取文件流每行中的unicode码以及pinyin字符串
                    char unicode = (char) Integer.parseInt(line.substring(0, 4), 16);
                    //去掉声调
                    String pinyin = line.replaceAll("(\\d$|\\d,)", ",");
                    //有可能为none
                    if(!pinyin.equals("none"))
                        map.put(String.valueOf(unicode), pinyin);
                }
            } catch (IOException e) {
                LLog.e("Failed to load Pinyin data", e);
            } finally {
                StreamUtil.closeStream(is);
            }
        }
        return sMapRef.get();
    }

    /**
     * This method could be very slow when loading Pinyin data. Make sure it's
     * called in background thread.
     *
     * @param c
     * @return
     */
    public static String getHanYuPinYin(char c) {
        // alphabetic char, return directly.
        if ((c >= 48 && c <= 57) // 0-9
                || (c >= 65 && c <= 90) // A-Z
                || (c >= 97 && c <= 112)) { // a-z
            return Character.toString(c);
        }
        // not Chinese char, return directly
        if (!Character.toString(c).matches("[\\u4E00-\\u9FA5]")) return null;
        return getMap().get(String.valueOf(c));
    }

}
