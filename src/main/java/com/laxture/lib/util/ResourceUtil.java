package com.laxture.lib.util;

import java.io.File;

import android.os.Environment;

public class ResourceUtil {

    public static final int UPP_SIZE_WIDTH_MAX = 800;
//    public static final int UPP_SIZE_HEIGHT_MAX = 9000;
    public static final int UPP_SIZE_HEIGHT_MAX = 800;

    public static final int UPP_SIZE_WIDTH_LARGE = 400;
//    public static final int UPP_SIZE_HEIGHT_LARGE = 4000;
    public static final int UPP_SIZE_HEIGHT_LARGE = 400;

    public static final int UPP_SIZE_WIDTH_MEDIUM = 200;
//    public static final int UPP_SIZE_HEIGHT_MEDIUM = 2000;
    public static final int UPP_SIZE_HEIGHT_MEDIUM = 200;

    public static final int UPP_SIZE_WIDTH_SMALL = 160;
//    public static final int UPP_SIZE_HEIGHT_SMALL = 800;
    public static final int UPP_SIZE_HEIGHT_SMALL = 160;

    public static final int UPP_SIZE_WIDTH_THUMBNAIL = 100;
    public static final int UPP_SIZE_HEIGHT_THUMBNAIL = 100;

    private ResourceUtil() {}

    public static String getQZoneAvatarUrlBig(String uin) {
        return String.format("http://qlogo1.store.qq.com/qzone/%s/%s/100", uin, uin);
    }

    public static String getQZoneAvatarUrlMedium(String uin) {
        return String.format("http://qlogo1.store.qq.com/qzone/%s/%s/50", uin, uin);
    }

    public static String getQZoneAvatarUrlSmall(String uin) {
        return String.format("http://qlogo1.store.qq.com/qzone/%s/%s/30", uin, uin);
    }

    public static String getUppImageUrlBig(String baseUrl) {
        return String.format(baseUrl.endsWith("/") ? "%s800" : "%s/800", baseUrl);
    }

    public static String getUppImageUrlMedium(String baseUrl) {
        return String.format(baseUrl.endsWith("/") ? "%s400" : "%s/400", baseUrl);
    }

    public static String getUppImageUrlSmall(String baseUrl) {
        return String.format(baseUrl.endsWith("/") ? "%s200" : "%s/200", baseUrl);
    }

    public static String getUppImageUrlTiny(String baseUrl) {
        return String.format(baseUrl.endsWith("/") ? "%s100" : "%s/100", baseUrl);
    }

    //处理同一张图片的不同规格
    public static boolean isUppImage(String url) {
        return url.startsWith("http://group.store.qq.com/");
    }

    public static boolean isExtStorageAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static boolean prepareParentDir(File file) {
        if (!file.getParentFile().exists()) {
            return file.getParentFile().mkdirs();
        }
        return true;
    }

}
