
package com.laxture.lib.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.laxture.lib.RuntimeContext;

public class NetworkUtil {

    /**
     * 获取激活的网络类型
     */
    private static NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connManager = RuntimeContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getActiveNetworkInfo();
    }

    /**
     * 检查wifi是否激活
     */
    public static boolean isWiFiActive() {
        ConnectivityManager connManager = RuntimeContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi != null && mWifi.isConnected();
    }

    /**
     * 检查当前网络是否是wifi
     */
    public static boolean isWifi() {
        NetworkInfo networkInfo = getActiveNetworkInfo();
        if (networkInfo != null
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                && networkInfo.isAvailable()) {
            return true;
        }

        return false;
    }

    /**
     * 检查当前网络是否是2G信号
     */
    public static boolean isMobile() {
        NetworkInfo networkInfo = getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                && networkInfo.isAvailable()) {
            return true;
        }
        return false;
    }

    /**
     * 检查当前网络是否是3G信号
     */
    public static boolean is3G() {
        TelephonyManager telManager = RuntimeContext.getSystemService(Context.TELEPHONY_SERVICE);
        int type = telManager.getNetworkType();

        if (!isMobile()) return false;

        switch (type){
        case TelephonyManager.NETWORK_TYPE_UMTS:   // ~ 400-7000 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
        case TelephonyManager.NETWORK_TYPE_HSDPA:  // ~ 2-14 Mbps
        case TelephonyManager.NETWORK_TYPE_HSPA:   // ~ 700-1700 kbps
        case TelephonyManager.NETWORK_TYPE_HSUPA:  // ~ 1-23 Mbps
        case TelephonyManager.NETWORK_TYPE_EVDO_B:
        case TelephonyManager.NETWORK_TYPE_LTE:
        case TelephonyManager.NETWORK_TYPE_EHRPD:
        case TelephonyManager.NETWORK_TYPE_HSPAP:
            return true; // ~ 400-7000 kbps
        default:
            return false;
        }
    }

    public static boolean is2G() {
        TelephonyManager telManager = RuntimeContext.getSystemService(Context.TELEPHONY_SERVICE);
        int type = telManager.getNetworkType();

        if (!isMobile()) return false;

        switch (type) {
        case TelephonyManager.NETWORK_TYPE_EDGE:  // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_GPRS:  // ~ 100 kbps
        case TelephonyManager.NETWORK_TYPE_CDMA:  // ~ 14-64 kbps
        case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_IDEN:  // ~25 kbps
            return true;
        default:
            return false;
        }
    }

    /**
     * 检查当前网络是否可用
     */
    public static boolean isNetworkAvailable() {
        NetworkInfo networkInfo = getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

}
