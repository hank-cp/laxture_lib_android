package com.laxture.lib.test;

import android.app.Application;

public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Configuration.init(this);
    }

}

