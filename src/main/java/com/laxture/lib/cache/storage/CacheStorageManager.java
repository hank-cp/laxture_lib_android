package com.laxture.lib.cache.storage;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.timer.Alarm;
import com.laxture.lib.timer.ClockAlarm;
import com.laxture.lib.timer.ClockAlarmService;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.DateUtil;
import com.laxture.lib.util.FileUtil;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.WakeLocker;

import org.joda.time.DateTime;

public class CacheStorageManager extends Thread {

    private static final Boolean DEBUG = false;

    private static final int FLUSH_POOL_THRESHOLD = 50;

    private static final int MESSAGE_UPDATE_CACHE = 0;
    private static final int MESSAGE_UPDATE_CACHE_IN_BATCH = 1;
    private static final int MESSAGE_RECYCLE_CACHE_FILE = 2;

    private MyHandler mHandler;

    private static CacheStorageManager instance;
    private static HashMap<String, CacheStorage> sMemCache = new HashMap<>();
    private static HashMap<String, CacheStorage> sUpdatedPool = new HashMap<>();

    // Alarm Intent to schedule recycle cache
    public static final String INTENT_RECYCLE_CACHE = "recycle_cache_storage";

    public static synchronized void init() {
        if (instance == null) {
            instance = new CacheStorageManager();
            instance.setPriority(Thread.MIN_PRIORITY);
            instance.start();
            instance.scheduleRecycling();
        }
    }

    public static synchronized CacheStorageManager getInstance() {
        return instance;
    }

    //*************************************************************************
    // Management API
    //*************************************************************************

    public CacheStorage getCache(String key) {
        return getCache(key, null);
    }

    public CacheStorage getCache(String key, String localPath) {
        if (Checker.isEmpty(key)) return null;

        // look up in pathCache
        CacheStorage cache = sMemCache.get(key);

        if (cache == null) {
            // path not cached, generate path and put it to pathCache
            cache = new CacheStorage(key, localPath);
            sMemCache.put(key, cache);
        }

        // update last use time
        cache.setLastVisit(System.currentTimeMillis());

        return cache;
    }

    void putInUpdatedPool(CacheStorage cache) {
        sUpdatedPool.put(cache.getKey(), cache);
        if (sUpdatedPool.size() >= FLUSH_POOL_THRESHOLD) flushUpdatedPool();
    }

    void saveToDB(CacheStorage cache) {
        Message.obtain(mHandler, MESSAGE_UPDATE_CACHE, cache).sendToTarget();
    }

    public void flushUpdatedPool() {
        Message.obtain(mHandler, MESSAGE_UPDATE_CACHE_IN_BATCH, null).sendToTarget();
    }

    public void recycleCacheStorage(Long maximumCapacity) {
        Message.obtain(mHandler, MESSAGE_RECYCLE_CACHE_FILE, maximumCapacity).sendToTarget();
    }

    //*************************************************************************
    // Thread Management
    //*************************************************************************

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new MyHandler(Looper.myLooper());
        Looper.loop();
    }

    public void terminate() {
        mHandler.getLooper().quit();
    }

    private class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_CACHE:
                    CacheStorage cache = (CacheStorage) msg.obj;
                    cache.saveToDB();
                    sUpdatedPool.remove(cache.getKey());
                    break;

                case MESSAGE_UPDATE_CACHE_IN_BATCH:
                    CacheDBHelper.getInstance().getWritableDatabase().beginTransaction();
                    for (CacheStorage item : sUpdatedPool.values()) {
                        item.saveToDB();
                    }
                    CacheDBHelper.getInstance().getWritableDatabase().setTransactionSuccessful();
                    CacheDBHelper.getInstance().getWritableDatabase().endTransaction();
                    sUpdatedPool.clear();
                    break;

                case MESSAGE_RECYCLE_CACHE_FILE:
                    internalRecycle((Long) msg.obj);
                    break;

                default: //do nothing
            }
        }
    }

    //*************************************************************************
    // Cache Management
    //*************************************************************************

    private void scheduleRecycling() {
        final long interval = RuntimeContext.getConfig().getRecycleCacheInterval();
        ClockAlarm alarmClock = new ClockAlarm(INTENT_RECYCLE_CACHE,
                interval, new Alarm.AlarmListener() {
            @Override
            public boolean onAlarmWentOff(Alarm alarm) {
                recycleCacheStorage(RuntimeContext.getConfig().getCacheStorageMaxCapacity());
                return true;
            }
        });
        ClockAlarmService.set(alarmClock);
    }

    private void internalRecycle(long maxCapacity) {
        long startTime = System.currentTimeMillis();
        LLog.i("Start to clear cacheStorage at Time " + new Date(startTime));

        WakeLocker.acquire(RuntimeContext.getApplication(), false);
        long cacheSize = 0;
        long clearSize = 0;

        List<CacheStorage> caches = CacheStorage.getAllCaches();

        HashSet<String> validateCacheFiles = new HashSet<>();

        // Delete all cache files exceed defined max capacity
        for (CacheStorage cache : caches) {
            File file = cache.getCacheFile();

            // delete non-existed file
            if (!file.exists()) {
                cache.deleteFromDB();
                sMemCache.remove(cache.getKey());
                sUpdatedPool.remove(cache.getKey());
                continue;
            }

            // delete empty file
            if (file.exists() && (!file.isFile() || file.length() <= 0)) {
                file.delete();
                cache.deleteFromDB();
                sMemCache.remove(cache.getKey());
                sUpdatedPool.remove(cache.getKey());
                continue;
            }

            cacheSize += file.length();

            if (cacheSize < maxCapacity) {
                validateCacheFiles.add(file.getAbsolutePath());

            } else {
                // exceed max capacity, delete it
                clearSize += file.length();
                if (file.delete()) {
                    file.delete();
                    cache.deleteFromDB();
                    sMemCache.remove(cache.getKey());
                    sUpdatedPool.remove(cache.getKey());
                }
            }
        }

        // delete unknown files which is not managed by ourselves.
        List<File> cacheFiles = FileUtil.listFiles(CacheStorage.getCachePolicy().getCacheHome());
        for (File file : cacheFiles) {
            if (validateCacheFiles.contains(file.getAbsolutePath())) continue;
            file.delete();
        }

        WakeLocker.release();
        long endTime = System.currentTimeMillis();
        LLog.i("Clear cacheStorage Completed at Time " + new Date(endTime));
        LLog.i("Delete FileSize:" + clearSize + " After clear cacheFileSize:" + (cacheSize - clearSize));
    }

}