package com.laxture.lib.util;

import java.security.InvalidKeyException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.laxture.lib.RuntimeContext;

public class SecurityUtil {

    private final static byte[] SALT_KEY = new byte[] {92,3,5,28,9,1,26,49,2,26,127,101,89,73,49,39};

    public static final String SECURITY_KEY = "security_key";

    private static final int XOR_KEY_MIN_LENGTH = 4;
    private static char[] sXORKey = null;

    /**
     * 加密用的key 先用imei
     */
    private static void prepareXorKey() {
        if (sXORKey != null) return;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
                RuntimeContext.getApplication());
        String secKey = pref.getString(SECURITY_KEY, null); //纯碎为兼容以前版本
        if (secKey == null || secKey.length() < XOR_KEY_MIN_LENGTH) {
            secKey = DeviceUtil.getIMEICode(RuntimeContext.getApplication());
            pref.edit().putString(SECURITY_KEY, secKey).commit();
        }
        sXORKey = secKey.toCharArray();
    }

    public static String xor(String str) {
        if (str == null) return null;

        prepareXorKey();
        char[] input = str.toCharArray();
        char[] output = new char[input.length];

        if (sXORKey.length >= input.length) {
            for (int i = 0; i < input.length; i++) {
                output[i] = (char) (input[i] ^ sXORKey[i]);
            }
        } else {
            for (int i = 0; i < input.length; i++) {
                output[i] = (char) (input[i] ^ sXORKey[i % sXORKey.length]);
            }
        }

        if (output.length == 0) {
            return "";
        }
        return new String(output);
    }


    public static byte[] xor(byte[] input) {
        if (input != null) {
            int len = input.length;
            byte[] out = new byte[len];
            for (int i = 0; i < len; i++) {
                out[i] = (byte) (input[i] ^ sXORKey[i % sXORKey.length]);
            }
            return out;
        }
        return input;
    }

    public static byte[] encrypt(byte[] data) {
        return encrypt(data, SALT_KEY);
    }

    public static byte[] encrypt(byte[] data, byte[] key) {
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            // 从原始密匙数据创建一个DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);
            // Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES");
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, sr);
            // 正式执行加密操作
            return cipher.doFinal(data);
        } catch (InvalidKeyException e) {
            throw new UnHandledException("Invalid key", e);
        } catch (Exception e) {
            LLog.w("Encrypt data failed.", e);
            return null;
        }
    }

    public static byte[] decrypt(byte[] encryptedData) {
        return decrypt(encryptedData, SALT_KEY);
    }

    public static byte[] decrypt(byte[] encryptedData, byte[] key) {
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            // 从原始密匙数据创建一个DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);
            // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);
            // Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance("DES");
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);
            // 正式执行解密操作
            return cipher.doFinal(encryptedData);
        } catch (InvalidKeyException e) {
            throw new UnHandledException("Invalid key", e);
        } catch (Exception e) {
            LLog.w("Encrypt data failed.", e);
            return null;
        }
    }

}