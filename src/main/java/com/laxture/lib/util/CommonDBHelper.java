package com.laxture.lib.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.cache.storage.StorageCacheRecord;

public class CommonDBHelper extends SQLiteOpenHelper{

    protected static String DB_NAME = "laxturelib.db";

    /**
     * 1.增加StorageCache表
     */
    protected static final int DB_VERSION = 1;

    private static CommonDBHelper instance = null;

    public synchronized static CommonDBHelper getInstance() {
        if (instance == null) {
            instance = new CommonDBHelper(
                    RuntimeContext.getApplication());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + StorageCacheRecord.TABLE_NAME + "("
                + StorageCacheRecord.COLUMN_URL + " VARCHAR(100) PRIMARY KEY, "
                + StorageCacheRecord.COLUMN_PATH + "  VARCHAR(200) ,"
                + StorageCacheRecord.COLUMN_RECYLABLE + " INTEGER not null default 1,"
                + StorageCacheRecord.COLUMN_LAST_MODIFY + " VARCHAR(50) ,"
                + StorageCacheRecord.COLUMN_LAST_USERD + " INTEGER"
                + ")");
    }

    public CommonDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public CommonDBHelper(Context context, String name,
            SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void terminate() {
        getWritableDatabase().close();
    }

}
