package com.laxture.lib.test;

import android.app.Application;

import com.laxture.lib.RuntimeConfig;
import com.laxture.lib.RuntimeContext;

public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RuntimeContext.init(this, new RuntimeConfig(new RuntimeConfig.Builder()));
    }

}

