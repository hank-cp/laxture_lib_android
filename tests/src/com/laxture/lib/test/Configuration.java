package com.laxture.lib.test;

import android.content.Context;

import com.laxture.lib.util.LLog;

public class Configuration extends com.laxture.lib.Configuration {

    @Override
    protected void initInstance(Context context) {
        LLog.LEVEL = android.util.Log.VERBOSE;
    }

    //*************************************************************************
    //  Configuration Methods
    //*************************************************************************

    @Override
    public String getStorageHomeName() { return "LaxtureLib-Test"; }

    @Override
    public int getTaskQueueCosumingLimit() { return 3; }

    @Override
    public long getCacheFolderCapacity() {
        return 0;
    }

    @Override
    public long getCacheClearIntervalTime() {
        return 0;
    }

}
