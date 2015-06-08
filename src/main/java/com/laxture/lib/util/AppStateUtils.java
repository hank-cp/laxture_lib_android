package com.laxture.lib.util;

import static com.laxture.lib.PrefKeys.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.laxture.lib.RuntimeContext;

public class AppStateUtils {

    private static final boolean DEBUG = false;

    private static UncaughtExceptionHandler mDefaultExceptionHandler;

    private static int LSAT_TASK_ID;

    /**
     * 判断应用是否在前台, 应该在onResume调用这个方法
     */
    public static boolean isAppInBackground() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
                RuntimeContext.getApplication());
        return sp.getBoolean(PREF_KEY_APP_TO_BACKGROUND, false);
    }

    /**
     * 应当在每个onResume和onPause的时候调用这个方法记录状态
     */
    public static boolean checkAndRecordApplicationState(Activity activity,
                                                         OnAppSendToBackgroundCallback callback) {
        boolean isToBackground = isAppSentToBackgroundByLockKey(activity);
        setAppToBackground(isToBackground);
        if (isToBackground && callback != null) callback.onAppSendToBackground();
        return isToBackground;
    }

    public static boolean checkAndRecordApplicationStateForDestory(Activity activity,
                                                                   OnAppSendToBackgroundCallback callback) {
        if (isAppInBackground()) return true;
        boolean isToBackground = isAppSentToBackgroundByBackKey(activity);
        setAppToBackground(isToBackground);
        if (isToBackground && callback != null) callback.onAppSendToBackground();
        return isToBackground;
    }

    public static boolean checkAndRecordApplicationStateForStop(Activity activity,
                                                                OnAppSendToBackgroundCallback callback) {
        if (isAppInBackground()) return true;
        boolean isToBackground = isAppSentToBackgroundByHomeKey(activity);
        setAppToBackground(isToBackground);
        if (isToBackground && callback != null) callback.onAppSendToBackground();
        return isToBackground;
    }

    /**
     * 判断应用是否推到后台, 应该在onResume和onPause调用这个方法，否则不能正确判断锁屏
     * @return
     */
    private static boolean isAppSentToBackgroundByLockKey(Activity activity) {
        // Check if screen is off
        boolean isScreenOff=false;
        PowerManager pm = RuntimeContext.getSystemService(Context.POWER_SERVICE);
        isScreenOff = !pm.isScreenOn();

        if (DEBUG) {
            LLog.d("isScreenOff=%s", isScreenOff);
        }
        return isScreenOff;
    }

    private static boolean isAppSentToBackgroundByBackKey(Activity activity) {
        // Check if back key pressed
        ActivityManager am = RuntimeContext.getSystemService(Context.ACTIVITY_SERVICE);
        boolean isLastActivity = false;
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (null != tasks && !tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            isLastActivity = (activity.isTaskRoot() &&
                    topActivity.getClassName().equals(tasks.get(0).baseActivity.getClassName())) ||
                    activity.getTaskId() != LSAT_TASK_ID;
        }
        if (DEBUG) {
            LLog.d("isLastActivity=" + isLastActivity);
        }
        return isLastActivity;
    }

    private static boolean isAppSentToBackgroundByHomeKey(Activity activity) {
        // Check if home key pressed
        boolean isHomeKeyPressed=false;
        ActivityManager am = RuntimeContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (null != tasks && !tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
             isHomeKeyPressed = activity.getTaskId() != tasks.get(0).id
                     && !topActivity.getPackageName().equals(RuntimeContext.getPackageName());
             LSAT_TASK_ID = tasks.get(0).id;
        }
        if (DEBUG) {
            LLog.d("isHomeKeyPressed=" + isHomeKeyPressed );
        }
        return isHomeKeyPressed;
    }

    /**
     * 保存应用当前状态
     *
     * @param isToBackground
     */
    public static void setAppToBackground(boolean isToBackground) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
                RuntimeContext.getApplication());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_KEY_APP_TO_BACKGROUND, isToBackground);
        editor.putLong(PREF_KEY_APP_TO_BACKGROUND_TIME, System.currentTimeMillis());
        editor.commit();
    }

    /**
     * 设置异常接收机制
     */
    public static void setUncaughtExceptionHandler() {
        if (Thread.getDefaultUncaughtExceptionHandler() instanceof ExceptionHandler)
            return;

        // 设置自己的异常接收机制，接着调用RQD和MTA的上报机制
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
    }

    static class ExceptionHandler implements UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            AppStateUtils.setAppToBackground(true);
            if (mDefaultExceptionHandler != null) {
                mDefaultExceptionHandler.uncaughtException(thread, ex);
            }
        }
    }

    public interface OnAppSendToBackgroundCallback {
        void onAppSendToBackground();
    }
}
