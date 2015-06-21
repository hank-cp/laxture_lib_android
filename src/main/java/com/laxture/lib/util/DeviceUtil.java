package com.laxture.lib.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Pair;

import com.laxture.lib.PrefKeys;
import com.laxture.lib.RuntimeContext;
import com.laxture.lib.cache.men.BitmapCache;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.UUID;

public class DeviceUtil {

    public static String getIMEICode() {
        return ((TelephonyManager) RuntimeContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    /**
     * Return UUID of this device. Emulator don't have an IEMI code, will
     * return a fake value.
     *
     * ref: http://stackoverflow.com/questions/1972381/how-to-programmatically-get-the-devices-imei-esn-in-android
     * ref: http://stackoverflow.com/questions/2322234/how-to-find-serial-number-of-android-device
     */
    public static String getDeviceId() {
        String deviceId = getIMEICode();
        if (Checker.isEmpty(deviceId)) {
            try {
                deviceId = Settings.System.getString(
                        RuntimeContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {}

            // Android 2.2 bug & emulator
            if ("9774d56d682e549c".equals(deviceId) || Checker.isEmpty(deviceId)) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
                        RuntimeContext.getApplication());
                deviceId = sp.getString(PrefKeys.PREF_KEY_DEVICE_ID, null);
                if (deviceId == null) {
                    String newDeviceId = UUID.randomUUID().toString();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(PrefKeys.PREF_KEY_DEVICE_ID, newDeviceId);
                    editor.commit();
                    deviceId = newDeviceId;
                }
            }
        }
        return deviceId;
    }


    /**
     * get local ip, maybe return null
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            LLog.e("get IP address failed", ex);
        }
        return "";
    }

    public static String getAllowedLocationProviders() {
        return Settings.System.getString(
                RuntimeContext.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
    }

    // ref: http://stackoverflow.com/questions/5832368/tablet-or-phone-android
    public static boolean isTabletDevice() {
        if (android.os.Build.VERSION.SDK_INT >= 11) { // honeycomb
            // test screen size, use reflection because isLayoutSizeAtLeast is
            // only available since 11
            android.content.res.Configuration con = RuntimeContext.getResources().getConfiguration();
            try {
                Method mIsLayoutSizeAtLeast = con.getClass().getMethod(
                        "isLayoutSizeAtLeast", int.class);
                boolean r = (Boolean) mIsLayoutSizeAtLeast.invoke(con,
                        0x04); //RuntimeContext.SCREENLAYOUT_SIZE_XLARGE
                return r;
            } catch (Exception ex) {
                LLog.i("OS version not support tablet.");
                return false;
            }
        }
        return false;
    }

    public static final String DEFAULT_DUMP_LOG_FILE_SIZE_KB = "512";

    public static final String TAG_ACTIVITY_THREAD = "ActivityThread";
    public static final String TAG_ACTIVITY_RUNTIME = "AndroidRuntime";
    public static final String TAG_PROCESS = "Process";
    public static final String TAG_DALVIKVM = "dalvikvm";

    /**
     * Dump logs to file.
     *
     * @param tags Tags to filter
     * @param size unit is KB
     * @param dumpFile
     * @return
     */
    public static File dumpLog(String[] tags, int size, File dumpFile) {
        // ref: http://developer.android.com/guide/developing/debugging/debugging-log.html
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("logcat");
        // Set dump option
        commands.add("-d");
        // Set dump file path
        commands.add("-f");
        commands.add(dumpFile.getAbsolutePath());
        // Set output size
        commands.add("-r");
        commands.add(size == 0 ? DEFAULT_DUMP_LOG_FILE_SIZE_KB : Integer.toString(size));
        // Set format
        commands.add("-v");
        commands.add("time");
        // Filter specified logs
        if (!Checker.isEmpty(tags)) {
            for (String tag : tags) commands.add(tag+":V");
        }
        // Add default logs
        commands.add("*:S"); // Mute other logs
        commands.add(TAG_ACTIVITY_THREAD+":E");
        commands.add(TAG_ACTIVITY_RUNTIME+":E");
        commands.add(TAG_DALVIKVM+":E");
        commands.add(TAG_PROCESS+":E");

        try {
            // Clear old dump file.
            if (dumpFile.exists()) dumpFile.delete();
            dumpFile.createNewFile();

            Process p = Runtime.getRuntime().exec(commands.toArray(new String[0]));
            p.waitFor();

            return dumpFile;
        } catch (InterruptedException e) {
            String msg = "Process is terminated unexpectedly.";
            throw new UnHandledException(msg, e);
        } catch (IOException e) {
            String msg = "Dump log failed.";
            LLog.e(msg);
            throw new UnHandledException(msg, e);
        }
    }

    private static DisplayMetrics sMetrics;
    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        if (sMetrics == null) {
            sMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(sMetrics);
        }
        return sMetrics;
    }

    public static Pair<Integer, Integer> getDeviceResolution() {
        float density = RuntimeContext.getResources().getDisplayMetrics().density;
        int rawWidth = RuntimeContext.getResources().getDisplayMetrics().widthPixels;
        int rawHeight = RuntimeContext.getResources().getDisplayMetrics().heightPixels;
        return new Pair<>(Math.round(rawWidth/density), Math.round(rawHeight/density));
    }

    public static void logHeap() {
        LLog.d("Image cache size %s/%s", BitmapCache.size(), BitmapCache.maxSize());
        LLog.d("Native heap usage :: %s/%s", Debug.getNativeHeapAllocatedSize(), Debug.getNativeHeapSize());
        LLog.d("Memory usage :: %s/%s", Runtime.getRuntime().totalMemory(), Runtime.getRuntime().maxMemory());
    }
}
