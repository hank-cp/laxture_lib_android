package com.laxture.lib;

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;

import com.laxture.lib.util.DateUtil;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.UnHandledException;

/**
 * This Class is designated for two purpose:
 * <ol>
 * <li>Hold ApplicationContext and package information. It could match to App
 * projects that include SnsCommon lib project automatically.  </li>
 * <li>Allow App projects to specify configurations that used in SnsCommon.</li>
 *
 * <br/>
 *
 * NOTE: Concrete Configuration class must be put under root package folder as
 * described as in AndroidManifest.xml.
 */
public abstract class Configuration {

    private static Configuration instance;
    public Configuration() {}

    protected Application mAppContext;
    public Application getAppContext() { return mAppContext; }

    protected String mVersionName;
    public String getVersionName() { return mVersionName; }

    protected int mVersionCode;
    public int getVersionCode() { return mVersionCode; }

    protected String mChannelName;
    public String getChannelName() { return mChannelName; }

    protected int mChannelCode;
    public int getChannelCode() { return mChannelCode; }

    protected String mPackageName;
    public String getPackageName() { return mPackageName; }

    protected File mStorageHomeDir;
    public File getStorageHome() {
        if (!mStorageHomeDir.exists()) mStorageHomeDir.mkdirs();
        return mStorageHomeDir;
    }
    protected boolean mNoMedia;
    public void setNoMedia(boolean nomedia) {
        mNoMedia = nomedia;
    }

    protected abstract void initInstance(Context context);

    @SuppressWarnings("unchecked")
    public static <T extends Configuration> T init(Application appContext) {
        try {
            String packageName = appContext.getPackageName();
            instance = (Configuration) Class.forName(
                    packageName+"."+Configuration.class.getSimpleName()).newInstance();
            PackageInfo packageInfo = appContext.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_META_DATA);
            instance.mAppContext = appContext;
            instance.mVersionName = packageInfo.versionName;
            instance.mVersionCode = packageInfo.versionCode;
            instance.mPackageName = packageName;
            instance.mNoMedia = true;

            instance.initInstance(appContext);

            instance.mStorageHomeDir = new File(Environment.getExternalStorageDirectory(),
                    instance.getStorageHomeName());
            if(instance.mNoMedia) {
                // to prevent scan by MediaScanner
                File nomediaIndecator = new File(instance.mStorageHomeDir, ".nomedia");
                if (!nomediaIndecator.exists()) nomediaIndecator.createNewFile();
            }

        } catch (IOException e) {
            // swallow IOException, this shouldn't block App starting
            LLog.w("Create .nomedia file falied.");

        } catch (Exception e) {
            throw new UnHandledException("Initial Configuration Failed:" + e.getMessage()
            		+ " packageName:" + appContext.getPackageName(), e);
        }
        return (T) instance;
    }

    /**
     * This method should be call in Application or ContentProvider.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Configuration> T getInstance(Application appContext) {
        if (instance == null) return (T) init(appContext);
        return (T) instance;
    }

    /**
     * This method must be called after init().
     */
    @SuppressWarnings("unchecked")
    public static <T extends Configuration> T getInstance() {
        if (instance == null) throw new IllegalArgumentException("Configuration is not initialized.");
        return (T) instance;
    }

    //*************************************************************************
    // Convenient Method to Access Context
    //*************************************************************************

    public static Application getApplicationContext() {
        if (instance == null) throw new IllegalArgumentException("Configuration is not initialized.");
        return instance.getAppContext();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSystemService(String name) {
        if (instance == null) throw new IllegalArgumentException("Configuration is not initialized.");
        return (T) instance.getAppContext().getSystemService(name);
    }

    public static Resources getResources() {
        return getApplicationContext().getResources();
    }

    public static String getString(int resId) {
        return getApplicationContext().getString(resId);
    }

    public static String getString(int resId, Object... formatArgs) {
        return getApplicationContext().getString(resId, formatArgs);
    }

    public static boolean isStorageDisable() {
        return !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    //*************************************************************************
    //  Configuration Methods
    //*************************************************************************

    /**
     * This configuration method should return the folder name on
     * external storage. Storage files will be placed under
     * "/sdcard/Tencent/<name>/
     */
    public abstract String getStorageHomeName();

    /**
     * Specify concurrent task count that is taken from the task management queue.
     */
    public int getTaskQueueCosumingLimit() { return 5; }

    /**
     * 缓存文件夹的容量，用于缓存清理时清理缓存文件的界限，capacity单位为字节
     */
    public long getCacheFolderCapacity() {
        return 100 * 1024 * 1024;
    }

    /**
     * 缓存目录清理的时间间隔,intervalTime单位为秒
     */
    public long getCacheClearIntervalTime() {
        return DateUtil.DAY_MILLIS * 7;
    }
}
