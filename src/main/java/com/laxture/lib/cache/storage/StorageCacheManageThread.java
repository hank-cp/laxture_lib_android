package com.laxture.lib.cache.storage;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;

import com.laxture.lib.Configuration;
import com.laxture.lib.util.DateUtil;
import com.laxture.lib.util.FileUtil;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.WakeLocker;

public class StorageCacheManageThread extends Thread {

    private static final Boolean DEBUG = false;

    private static final int MESSAGE_UPDATA_LAST_USED = 0;
    private static final int MESSAGE_INSERT_STORAGE_CACHE = 1;
    private static final int MESSAGE_CLEAR_STORAGE_CACHE_FILE = 2;
    private static final int MESSAGE_UPDATA_LAST_USED_DELAY = 3;
    private static final int MESSAGE_INSERT_DOWNLOAD_STOAGE_CACHE = 4;

    private MyHandler mHandler;

    private static StorageCacheManageThread manageInstance;

    //请求开始清理缓存
    public static final String INTENT_CLEAR_STORAG_CACHE = "intent_clear_storage_cache";

    public static synchronized void init() {
        if (manageInstance == null) {
            manageInstance = new StorageCacheManageThread();
            manageInstance.startStorageCacheClearTimer();
            manageInstance.setPriority(Thread.MIN_PRIORITY);
            manageInstance.start();
        }
    }

    public static synchronized StorageCacheManageThread getInstance() {
        return manageInstance;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new MyHandler(Looper.myLooper());
        Looper.loop();
    }

    public void terminate() {
        mHandler.getLooper().quit();
    }

    /**
     *  当程序切换到后台时把ContentStorage中LRU中的文件最后使用时间同步到数据库
     *  防止程序不正常结束导致文件的最后使用时间不能更新
     */
    public void updateLastUsed() {
        Message.obtain(mHandler, MESSAGE_UPDATA_LAST_USED_DELAY, null)
                .sendToTarget();
    }

    public void updateLastUsed(StorageCacheRecord data){
        Message.obtain(mHandler, MESSAGE_UPDATA_LAST_USED, data).sendToTarget();
    }

    public void clearStorageCache(Long data){
        Message.obtain(mHandler, MESSAGE_CLEAR_STORAGE_CACHE_FILE, data).sendToTarget();
    }

    public void insertStorageCache(StorageCacheRecord data){
        Message.obtain(mHandler, MESSAGE_INSERT_STORAGE_CACHE, data).sendToTarget();
    }

    public void InsertDownloadStorageCache(StorageCacheRecord data){
        Message.obtain(mHandler, MESSAGE_INSERT_DOWNLOAD_STOAGE_CACHE, data).sendToTarget();
    }

    private static class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_UPDATA_LAST_USED:
                StorageCacheRecord data = (StorageCacheRecord) msg.obj;
                StorageCacheRecord.updateLastUsed(data);
                break;
            case MESSAGE_CLEAR_STORAGE_CACHE_FILE:
                long capacity = (Long) msg.obj;
                clearCache(capacity);
                break;
            case MESSAGE_INSERT_STORAGE_CACHE:
                StorageCacheRecord storageCacheRecord = (StorageCacheRecord) msg.obj;
                StorageCacheRecord.insert(storageCacheRecord);
                break;
            case MESSAGE_UPDATA_LAST_USED_DELAY:
                updateStorageCacheLastUsed();
                break;
            case MESSAGE_INSERT_DOWNLOAD_STOAGE_CACHE:
                StorageCacheRecord downloadData = (StorageCacheRecord) msg.obj;
                StorageCacheRecord.insertDownloadCache(downloadData);
                break;
            default: //do nothing
            }
        }
    }

    // 定时清除缓存
    public void startStorageCacheClearTimer() {
        LLog.i("Start StorageCache Clear Timer");
        Time time = new Time();
        time.setToNow();
        // 设置首次清理时间，首次清理时间区间为凌晨4点左右
        long startTime = DateUtil.HOUR_MILLIS * (24 - time.hour + 4);

        // TODO
//        AlarmClock alarmClock = new AlarmClock(INTENT_CLEAR_STORAG_CACHE,
//                startTime, new OnClockListener() {
//                    @Override
//                    public boolean onClockArrived(Clock sender) {
//                        clearStorageCache(Configuration.getInstance().getCacheFolderCapacity());
//                        // 首次清理缓存后设置清理时间间隔
//                        sender.setInterval(Configuration.getInstance().getCacheClearIntervalTime());
//                        return true;
//                    }
//                });
//        AlarmClockService.set(alarmClock);
    }

    public static void cancelStorageCacheClearTimer() {
//        AlarmClockService.getClock(INTENT_CLEAR_STORAG_CACHE).cancel();
    }

    public static void cancelStorageCacheUpdateTimer() {
//        AlarmClockService.getClock(INTENT_CLEAR_STORAG_CACHE).cancel();
    }

    public static void clearCache(long capacity) {
        long startTime = System.currentTimeMillis();
        LLog.i("Start to clear cacheStorage at Time " + new Date(startTime));
        
        WakeLocker.acquire(Configuration.getInstance().getAppContext(), false);
        long cacheSize = 0;
        long clearSize = 0;
        List<StorageCacheRecord> cacheRecordList =
                StorageCacheRecord.getCacheStorageOrderByLastUsed();

        List<File> cacheFiles = FileUtil.listFiles(
                new File(Configuration.getInstance().getStorageHome(), ".cache"));

        Map<String, String> pathUrlMap = new HashMap<String, String>();

        //删除超出定义大小的文件
        for (StorageCacheRecord storageCacheRecord : cacheRecordList) {
            File file = new File(storageCacheRecord.path);
            pathUrlMap.put(storageCacheRecord.path, storageCacheRecord.url);

            //表情包已下载的指针文件长度为0,不用文件长度为0判断
            if (!file.exists() || null == file || !file.isFile()) {
                StorageCacheRecord.delete(storageCacheRecord.url);
                ContentStorage.clearLruCache(storageCacheRecord.url);
                continue;
            }

            if (!storageCacheRecord.recylable ||
                    null != ContentStorage.getFromCache(storageCacheRecord.url))
                continue;

            cacheSize += file.length();

            if (cacheSize < capacity) continue;

            //达到临界值就开始删除缓存
            clearSize += file.length();
            if (file.delete()) {
                ContentStorage.clearLruCache(storageCacheRecord.url);
                StorageCacheRecord.delete(storageCacheRecord.url);
                if (DEBUG) LLog.d("Delete cacheStorage" + storageCacheRecord.toString());
            }
        }

        //删除.cache文件夹中不在缓存表的文件
        for (File file : cacheFiles) {
            if (null == pathUrlMap.get(file.getAbsolutePath())) {
                if (file.delete()) {
                    if (DEBUG)
                        LLog.d("Delete cacheStorage" + file.getAbsolutePath());
                }
            }
        }


        WakeLocker.release();
        long endTime = System.currentTimeMillis();
        LLog.i("Clear cacheStorage Completed at Time " + new Date(endTime));
        LLog.i("Delete FileSize:" + clearSize + " After clear cacheFileSize:" + (cacheSize - clearSize));
    }

    private static void updateStorageCacheLastUsed() {
        if (DEBUG) LLog.d("Start to update StorageCacheLastUsed");

        List<StorageCacheRecord> cacheRecordList = StorageCacheRecord
                .getAllRecord();
        for (StorageCacheRecord storageCacheRecord : cacheRecordList) {
            if (null != ContentStorage.getFromCache(storageCacheRecord.url)) {
                StorageCacheRecord cRecord = new StorageCacheRecord();
                cRecord.url = storageCacheRecord.url;
                cRecord.lastUsed = ContentStorage
                        .getFromMyCache(storageCacheRecord.url);
                StorageCacheRecord.updateLastUsed(cRecord);
                if (DEBUG) LLog.d("updateLastUsedupdateLastUsedupdateLastUsed url = "
                            + cRecord.url + "lastUsed = " + cRecord.lastUsed);
            }
        }
    }

}