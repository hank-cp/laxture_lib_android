package com.laxture.lib;

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.laxture.lib.util.DateUtil;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.UnHandledException;

/**
 * This Class is designated for two purpose:
 * <ol>
 * <li>Hold ApplicationContext and package information. It could match to App
 * projects that include LaxtureLib project automatically.  </li>
 * <li>Allow App projects to specify configurations that used in LaxtureLib.</li>
 * </ol>
 * <br/>
 *
 * NOTE: Concrete RuntimeContext class must be put under root package folder as
 * described as in AndroidManifest.xml.
 */
public class RuntimeContext {

    private static RuntimeContext instance;
    public RuntimeContext() {}

    private Application mAppContext;
    private String mVersionName;
    private int mVersionCode;
    private String mChannelName;
    private int mChannelCode;
    private String mPackageName;
    private File mStorageHomeDir;
    private RuntimeConfig mConfig;

    /**
     * This method should be called in {@link Application#onCreate()}
     *
     * @param application
     * @param config
     * @return
     */
    public static void init(Application application, RuntimeConfig config) {
        try {
            String packageName = application.getPackageName();
            instance = new RuntimeContext();
            PackageInfo packageInfo = application.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_META_DATA);
            instance.mAppContext = application;
            instance.mVersionName = packageInfo.versionName;
            instance.mVersionCode = packageInfo.versionCode;
            instance.mPackageName = packageName;
            instance.mConfig = config;

            ApplicationInfo applicationInfo = application.getPackageManager()
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (applicationInfo.metaData != null) {
                instance.mChannelName = applicationInfo.metaData.getString("InstallChannel");
                instance.mChannelCode = applicationInfo.metaData.getInt("InstallChannelCode");
            }

            LLog.i("InstallChannel=" + instance.mChannelName + "(" + instance.mChannelCode + ")");

            instance.mStorageHomeDir = new File(Environment.getExternalStorageDirectory(),
                    instance.mConfig.getStorageHomeName());
            if(instance.mConfig.isNoMedia()) {
                // to prevent scan by MediaScanner
                File nomediaIndecator = new File(instance.mStorageHomeDir, ".nomedia");
                if (!nomediaIndecator.exists()) nomediaIndecator.createNewFile();
            }

        } catch (IOException e) {
            // swallow IOException, this shouldn't block App starting
            LLog.w("Create .nomedia file falied.");

        } catch (Exception e) {
            throw new UnHandledException("Initial RuntimeContext Failed:" + e.getMessage()
            		+ " packageName:" + application.getPackageName(), e);
        }
    }

    //*************************************************************************
    // Convenient Method to Access Context
    //*************************************************************************

    public static Application getApplication() {
        if (instance == null) throw new IllegalArgumentException("RuntimeContext is not initialized.");
        return instance.mAppContext;
    }

    public static String getVersionName() {
        if (instance == null) throw new IllegalArgumentException("RuntimeContext is not initialized.");
        return instance.mVersionName;
    }

    public static int getVersionCode() {
        if (instance == null) throw new IllegalArgumentException("RuntimeContext is not initialized.");
        return instance.mVersionCode;
    }

    public static int getChannelCode() {
        if (instance == null) throw new IllegalArgumentException("RuntimeContext is not initialized.");
        return instance.mChannelCode;
    }

    public static String getChannelName() {
        if (instance == null) throw new IllegalArgumentException("RuntimeContext is not initialized.");
        return instance.mChannelName;
    }

    public static String getPackageName() {
        if (instance == null) throw new IllegalArgumentException("RuntimeContext is not initialized.");
        return instance.mPackageName;
    }

    public static File getStorageHome() {
        if (instance == null) throw new IllegalArgumentException("RuntimeContext is not initialized.");
        if (!instance.mStorageHomeDir.exists()) instance.mStorageHomeDir.mkdirs();
        return instance.mStorageHomeDir;
    }

    public static RuntimeConfig getConfig() {
        if (instance == null) throw new IllegalArgumentException("RuntimeContext is not initialized.");
        return instance.mConfig;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSystemService(String name) {
        if (instance == null) throw new IllegalArgumentException("RuntimeContext is not initialized.");
        return (T) instance.mAppContext.getSystemService(name);
    }

    public static Resources getResources() {
        return getApplication().getResources();
    }

    public static String getString(int resId) {
        return getApplication().getString(resId);
    }

    public static String getString(int resId, Object... formatArgs) {
        return getApplication().getString(resId, formatArgs);
    }

    public static boolean isStorageDisable() {
        return !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

}
