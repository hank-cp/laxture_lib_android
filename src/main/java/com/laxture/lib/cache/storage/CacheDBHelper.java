package com.laxture.lib.cache.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.laxture.lib.RuntimeContext;

public class CacheDBHelper extends SQLiteOpenHelper {

    protected static String DB_NAME = "cache.db";

    protected static final int DB_VERSION = 1;

    private static CacheDBHelper instance = null;

    public synchronized static CacheDBHelper getInstance() {
        if (instance == null) {
            instance = new CacheDBHelper(RuntimeContext.getApplication());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CacheStorage.TABLE_NAME + "("
                + CacheStorage.COLUMN_KEY + " VARCHAR(100) PRIMARY KEY, "
                + CacheStorage.COLUMN_LAST_MODIFY + " VARCHAR(50) ,"
                + CacheStorage.COLUMN_LAST_VISIT+ " INTEGER"
                + ")");
    }

    public CacheDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

}
