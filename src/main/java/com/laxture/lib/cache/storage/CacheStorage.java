package com.laxture.lib.cache.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.DigestUtils;
import com.laxture.lib.util.WakeLocker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CacheStorage implements Storage {

    private static CachePolicy sPolicy = new DefaultPolicy();
    public static void setCachePolicy(CachePolicy policy) { sPolicy = policy; }
    public static CachePolicy getCachePolicy() { return sPolicy; }

    private String mKey;
    private String mLastModify;
    private long mLastVisit;
    private File mCacheFile;
    private File mLocalFile; // override cache file
    private boolean mSavedToDB;

    /**
     * Mostly for downloading files.
     *
     * @param key
     */
    CacheStorage(String key) {
        if (Checker.isEmpty(key))
            throw new IllegalArgumentException("cache key cannot be empty");
        mKey = key;
        readFromDB();
    }

    /**
     * Allow using localPath to override cache path, suitable for uploading files,
     * which don't have an key for key yet.
     *
     * @param key
     * @param localPath
     */
    CacheStorage(String key, String localPath) {
        if (Checker.isEmpty(key) && Checker.isEmpty(localPath))
            throw new IllegalArgumentException("cache key cannot be empty");
        mKey = key;
        if (!Checker.isEmpty(localPath)) mLocalFile = new File(localPath);
        readFromDB();
    }

    public String getKey() { return mKey; }

    public String getLastModify() { return mLastModify; }

    @Override
    public File getFile() {
        File file = mLocalFile;
        if (Checker.isEmpty(file)) file = getCacheFile();
        return file;
    }

    public File getCacheFile() {
        File file = mCacheFile;
        if (file == null) {
            mCacheFile = new File(sPolicy.getCacheHome(), sPolicy.getCacheFileName(mKey));
        }
        return mCacheFile;
    }

    public void moveLocalFileToCache() {
        if (Checker.isEmpty(mLocalFile)) return;
        mLocalFile.renameTo(getCacheFile());
    }

    public void setLastModify(String LastModify) {
        mLastModify = LastModify;
        CacheStorageManager.getInstance().saveToDB(this);
    }

    public void setLastVisit(long timestamp) {
        mLastVisit = timestamp;
        CacheStorageManager.getInstance().putInUpdatedPool(this);
    }

    //*************************************************************************
    // Cache Policy
    //*************************************************************************

    public interface CachePolicy {
        File getCacheHome();
        String getCacheFileName(String key);
    }

    public static class DefaultPolicy implements CachePolicy {

        @Override
        public File getCacheHome() {
            File cacheHome = new File(RuntimeContext.getStorageHome(), ".cache");
            if (!cacheHome.exists()) cacheHome.mkdirs();
            return cacheHome;
        }

        @Override
        public String getCacheFileName(String key) {
            String md5 = DigestUtils.md5Hex(key);
            return TextUtils.join(File.separator, new String[]{
                    md5.substring(0, 2),
                    md5.substring(2, 4), md5});
        }
    }

    //*************************************************************************
    // DB Management
    //*************************************************************************

    public final static String TABLE_NAME = "CacheRecord";
    public final static String COLUMN_KEY = "key";
    public static final String COLUMN_LAST_MODIFY = "lastModify";
    public static final String COLUMN_LAST_VISIT = "lastVisit";

    public void saveToDB() {
        if (mSavedToDB) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LAST_MODIFY, mLastModify);
            values.put(COLUMN_LAST_VISIT, mLastVisit);

            CacheDBHelper.getInstance().getWritableDatabase().update(TABLE_NAME, values,
                    String.format("key = '%s'", mKey), null);

        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_KEY, mKey);
            values.put(COLUMN_LAST_MODIFY, mLastModify);
            values.put(COLUMN_LAST_VISIT, mLastVisit);

            CacheDBHelper.getInstance().getWritableDatabase().insertWithOnConflict(
                    TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            mSavedToDB = true;
        }
    }

    public void deleteFromDB() {
        CacheDBHelper.getInstance().getWritableDatabase().execSQL(
                String.format("DELETE FROM %s WHERE key = %s", TABLE_NAME, mKey));
        mSavedToDB = false;
    }

    public static void deleteAllFromDB() {
        CacheDBHelper.getInstance().getWritableDatabase().execSQL("DELETE FROM " + TABLE_NAME);
    }

    private boolean readFromDB() {
        Cursor cur = CacheDBHelper.getInstance().getReadableDatabase().query(
                TABLE_NAME,
                null,
                String.format("%s='%s'", COLUMN_KEY, mKey),
                null,
                null,
                null,
                null);

        if (cur.moveToFirst()) {
            mLastModify = cur.getString(cur.getColumnIndex(COLUMN_LAST_MODIFY));
            mLastVisit = cur.getLong(cur.getColumnIndex(COLUMN_LAST_VISIT));
            mSavedToDB = true;
        }
        cur.close();
        return mSavedToDB;
    }

    public static long getCacheSize() {
        List<CacheStorage> cacheRecords = getAllCaches();
        long cacheSize = 0;
        WakeLocker.acquire(RuntimeContext.getApplication(), false);
        for (CacheStorage cache : cacheRecords) {
            File file = cache.getCacheFile();
            if (!file.exists() || !file.isFile() || file.length() <= 0) {
                cache.deleteFromDB();
                continue;
            }
            cacheSize += file.length();
        }
        WakeLocker.release();
        return cacheSize;
    }

    public static List<CacheStorage> getAllCaches(){
        Cursor cur = CacheDBHelper.getInstance().getReadableDatabase().query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                COLUMN_LAST_VISIT + " DESC");

        List<CacheStorage> cacheRecords = new ArrayList<>();
        while (cur.moveToNext()) {
            String key = cur.getString(cur.getColumnIndex(COLUMN_KEY));
            CacheStorage cache = new CacheStorage(key);
            cache.mLastModify = cur.getString(cur.getColumnIndex(COLUMN_LAST_MODIFY));
            cache.mLastVisit = cur.getLong(cur.getColumnIndex(COLUMN_LAST_VISIT));
            cacheRecords.add(cache);
        }
        cur.close();
        return cacheRecords;
    }

}
