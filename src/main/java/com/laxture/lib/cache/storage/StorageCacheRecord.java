package com.laxture.lib.cache.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.laxture.lib.Configuration;
import com.laxture.lib.util.CommonDBHelper;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.WakeLocker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageCacheRecord {

    private final static Boolean DEBUG = false;

    public final static String TABLE_NAME = "StorageCache";

    public final static String COLUMN_URL = "url";
    public String url;

    public final static String COLUMN_PATH = "path";
    public String path;

    //HTTP返回的头部last modify
    public static final String COLUMN_LAST_MODIFY = "lastModify";
    public String lastModify;

    //文件的最后修改时间
    public static final String COLUMN_LAST_USERD = "lastUsed";
    public long lastUsed;

    public static final String COLUMN_RECYLABLE = "recylable";
    public boolean recylable;

    @Override
    public String toString() {
        return "{" +
                " url=" + url +
                " path=" + path +
                " lastModify=" + lastModify +
                " recylable=" + recylable +
                " lastUsed" + lastUsed +
                '}';
    }

    public static void insert(StorageCacheRecord record){
        if (null != getSingleCache(record.url)) {
            ContentStorage.putInCache(record.url, record.path, System.currentTimeMillis());
            return ;
        }

        ContentValues value = new ContentValues();
        value.put(StorageCacheRecord.COLUMN_URL, record.url);
        value.put(StorageCacheRecord.COLUMN_PATH, record.path);
        value.put(StorageCacheRecord.COLUMN_LAST_MODIFY, record.lastModify);
        value.put(StorageCacheRecord.COLUMN_LAST_USERD, record.lastUsed);
        CommonDBHelper.getInstance().getWritableDatabase().insert(
                TABLE_NAME, null, value);
        ContentStorage.putInCache(record.url, record.path, System.currentTimeMillis());
        if (DEBUG) LLog.d("insert cache " + value.toString());
    }

    public static void insert(String url, String path, Boolean recylable){
        ContentValues value = new ContentValues();
        value.put(StorageCacheRecord.COLUMN_URL, url);
        value.put(StorageCacheRecord.COLUMN_PATH, path);
        if (recylable != null)
            value.put(StorageCacheRecord.COLUMN_RECYLABLE, recylable);
        value.put(StorageCacheRecord.COLUMN_LAST_USERD, System.currentTimeMillis());
        CommonDBHelper.getInstance().getWritableDatabase().insertWithOnConflict(
                TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
        ContentStorage.putInCache(url, path, System.currentTimeMillis());
        if (DEBUG)
            LLog.d("insert cache " + value.toString());
    }


    public static void insertDownloadCache(StorageCacheRecord record){
        if (null != getSingleCache(record.url)) {
             updateLastModify(record.url, record.lastModify);
            ContentStorage.putInCache(record.url, record.path, System.currentTimeMillis());
        }
        else {
            ContentValues value = new ContentValues();
            value.put(StorageCacheRecord.COLUMN_URL, record.url);
            value.put(StorageCacheRecord.COLUMN_PATH, record.path);
            value.put(StorageCacheRecord.COLUMN_LAST_MODIFY, record.lastModify);
            value.put(StorageCacheRecord.COLUMN_LAST_USERD, record.lastUsed);
            CommonDBHelper.getInstance().getWritableDatabase().insert(
                    TABLE_NAME, null, value);
            ContentStorage.putInCache(record.url, record.path, System.currentTimeMillis());
            if (DEBUG) LLog.d("insert cache " + value.toString());
        }

    }
    public static void updatePath(String url, String path) {
        CommonDBHelper.getInstance().getWritableDatabase().execSQL(
                "UPDATE " + TABLE_NAME + " SET path=\"" + path + "\"" + "WHERE url=\"" + url + "\"");
        ContentStorage.putInCache(url, path ,System.currentTimeMillis());
        if (DEBUG)
            LLog.d("update cache " + url + "__" + path);
    }

    public static void updateLastModify(String url, String lastModify) {
        CommonDBHelper.getInstance().getWritableDatabase().execSQL(
                "UPDATE " + TABLE_NAME + " SET lastModify=\"" + lastModify + "\" " + "WHERE url=\"" + url + "\"");
        if (DEBUG)
            LLog.d("update updateLastModify " + url + "__lastModify" + lastModify);
    }

    public static void updateLastUsed(StorageCacheRecord cacheRecord) {
        CommonDBHelper.getInstance().getWritableDatabase().beginTransaction();
        CommonDBHelper.getInstance().getWritableDatabase().execSQL(
                "UPDATE " + TABLE_NAME + " SET lastUsed=\"" + cacheRecord.lastUsed + "\" " + "WHERE url=\"" + cacheRecord.url + "\"");
        if (DEBUG) LLog.d("updateLastUsed url = " +  cacheRecord.url + "lastUsed = " + cacheRecord.lastUsed);
        CommonDBHelper.getInstance().getWritableDatabase().setTransactionSuccessful();
        CommonDBHelper.getInstance().getWritableDatabase().endTransaction();
    }

    public static void delete(String url){
        CommonDBHelper.getInstance().getWritableDatabase().execSQL(
                "DELETE  FROM " + TABLE_NAME + "  WHERE url=\"" + url + "\"");
        ContentStorage.clearLruCache(url);
    }

    public static void deleteAll() {
        CommonDBHelper.getInstance().getWritableDatabase().execSQL("DELETE  FROM " + TABLE_NAME);
    }

    public static StorageCacheRecord getSingleCache(String url) {

        Cursor cur = CommonDBHelper.getInstance().getReadableDatabase().query(
                TABLE_NAME,
                null,
                String.format("%s=\"%s\"", StorageCacheRecord.COLUMN_URL ,url),
                null,
                null,
                null,
                null);

        StorageCacheRecord cache = null;
        if (cur.moveToFirst()) {
            cache = new StorageCacheRecord();
            cache.lastModify = cur.getString(cur.getColumnIndex(StorageCacheRecord.COLUMN_LAST_MODIFY));
            cache.path = cur.getString(cur.getColumnIndex(StorageCacheRecord.COLUMN_PATH));
            cache.url = cur.getString(cur.getColumnIndex(StorageCacheRecord.COLUMN_URL));
            cache.recylable = cur.getInt(cur.getColumnIndex(StorageCacheRecord.COLUMN_RECYLABLE)) == 0 ? false : true;
            cache.lastUsed = cur.getLong(cur.getColumnIndex(StorageCacheRecord.COLUMN_LAST_USERD));
        }
        cur.close();
        if (DEBUG && null != cache)
            LLog.d("getSingle cache " + cache.url);
        if (DEBUG && null == cache)
            LLog.d("can not find cache url =" + url);
        return cache;
    }

    public static List<StorageCacheRecord> getAllRecord() {
        Cursor cur = CommonDBHelper.getInstance().getReadableDatabase().query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        List<StorageCacheRecord> cacheRecordList = new ArrayList<StorageCacheRecord>();
        while (cur.moveToNext()) {
            StorageCacheRecord cache = new StorageCacheRecord();
            cache.lastModify = cur.getString(cur.getColumnIndex(StorageCacheRecord.COLUMN_LAST_MODIFY));
            cache.path = cur.getString(cur.getColumnIndex(StorageCacheRecord.COLUMN_PATH));
            cache.url = cur.getString(cur.getColumnIndex(StorageCacheRecord.COLUMN_URL));
            cache.recylable = cur.getInt(cur.getColumnIndex(StorageCacheRecord.COLUMN_RECYLABLE)) == 0 ? false : true;
            cache.lastUsed = cur.getLong(cur.getColumnIndex(StorageCacheRecord.COLUMN_LAST_USERD));
            cacheRecordList.add(cache);
        }
        cur.close();
        return cacheRecordList;
    }

    public static long getRecylableStorageCacheSize() {
        List<StorageCacheRecord> cacheRecordList = StorageCacheRecord.getCacheStorageOrderByLastUsed();
        long cacheSize = 0;
        WakeLocker.acquire(Configuration.getInstance().getAppContext(), false);
        for (StorageCacheRecord storageCacheRecord : cacheRecordList) {
            File file = new File(storageCacheRecord.path);

            //表情包已下载的指针文件长度为0,不用文件长度为0判断
            if (!file.exists() || null == file || !file.isFile()) {
                StorageCacheRecord.delete(storageCacheRecord.url);
                ContentStorage.clearLruCache(storageCacheRecord.url);
                continue;
            }

            if (!storageCacheRecord.recylable)
                continue;

            cacheSize += file.length();
        }
        WakeLocker.release();
        return cacheSize;
    }

    public static String getLastModifyTimestamp(String cacheKey) {
        StorageCacheRecord cRecord = StorageCacheRecord.getSingleCache(cacheKey);
        if (null != cRecord) {
            if (DEBUG) LLog.d("getLastModifyTimestamp,url= " + cRecord.url + "lastModify=" + cRecord.lastModify);
            return cRecord.lastModify == null ? "0" : cRecord.lastModify;
        }
        return null;
    }

    public static List<StorageCacheRecord> getCacheStorageOrderByLastUsed(){
        Cursor cur = CommonDBHelper.getInstance().getReadableDatabase().query(
                 TABLE_NAME,
                 null,
                 null,
                 null,
                 null,
                 null,
                 COLUMN_LAST_USERD + " desc");

        List<StorageCacheRecord> cacheRecordList = new ArrayList<StorageCacheRecord>();
        while (cur.moveToNext()) {
             StorageCacheRecord cache = new StorageCacheRecord();
             cache.lastModify = cur.getString(cur.getColumnIndex(StorageCacheRecord.COLUMN_LAST_MODIFY));
             cache.path = cur.getString(cur.getColumnIndex(StorageCacheRecord.COLUMN_PATH));
             cache.url = cur.getString(cur.getColumnIndex(StorageCacheRecord.COLUMN_URL));
             cache.recylable = cur.getInt(cur.getColumnIndex(StorageCacheRecord.COLUMN_RECYLABLE)) == 0 ? false : true;
             cache.lastUsed = cur.getLong(cur.getColumnIndex(StorageCacheRecord.COLUMN_LAST_USERD));
             cacheRecordList.add(cache);
        }
        cur.close();
        return cacheRecordList;
    }
}
