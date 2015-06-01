package com.laxture.lib.view.pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: vincenthu
 * Date: 13-4-7
 * Time: 下午9:13
 * Description:密码锁相关工具类
 */
public class PatternLockUtils {

    private static final String SP_KEY_PATTERN_LOCK_PWD = "pattern_lock_password";

    /**
     * 将字符串转换为手势动作
     *
     * @param string
     * @return
     */
    public static List<PatternLockView.Cell> stringToPattern(String string) {
        List<PatternLockView.Cell> result = new ArrayList<PatternLockView.Cell>();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(PatternLockView.Cell.of(b / 3, b % 3));
        }
        return result;
    }

    /**
     * 将手势动作转换为字符串
     *
     * @param pattern
     * @return
     */
    public static String patternToString(List<PatternLockView.Cell> pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            PatternLockView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        return Arrays.toString(res);
    }

    /**
     * 保存图形密码
     *
     * @param context
     * @param password
     */
    public static void saveLockPattern(Context context, String password) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_KEY_PATTERN_LOCK_PWD, password);
        editor.commit();
    }

    /**
     * 获取图形密码
     *
     * @param context
     * @return
     */
    public static String getLockPatternString(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(SP_KEY_PATTERN_LOCK_PWD, "");
    }
}
