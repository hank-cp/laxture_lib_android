package com.laxture.lib.util;

import android.content.Context;
import android.os.PowerManager;

public abstract class WakeLocker {

    private static PowerManager.WakeLock wakeLock;
    private static long MAX_TIMEOUT = 120000;

    public synchronized static void acquire(Context ctx) {
        acquire(ctx, true);
    }

    public synchronized static void acquire(Context ctx, boolean autoRelease) {
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null) {
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, ctx.getClass().getCanonicalName());
            if (wakeLock != null) wakeLock.setReferenceCounted(false);
        }
        try {
            if (autoRelease) wakeLock.acquire(MAX_TIMEOUT);
            else wakeLock.acquire();
        } catch (Exception e) {}
    }

    public synchronized static boolean isHeld() {
        return wakeLock != null && wakeLock.isHeld();
    }

    public synchronized static void release() {
        if (isHeld()) {
            try {
                wakeLock.release();
            } catch (RuntimeException e) {}
            wakeLock = null;
        }
    }
}
