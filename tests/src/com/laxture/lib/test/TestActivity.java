package com.laxture.lib.test;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.connectivity.push.PushEvent;
import com.laxture.lib.connectivity.push.PushListener;
import com.laxture.lib.util.LLog;

public class TestActivity extends Activity implements PushListener {

    // push handling control
    public AtomicBoolean pushHandled = new AtomicBoolean();
    public boolean passPushHandling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onPushReceived(PushEvent pushEvent) {
        LLog.i("SilentPushListener -- receive push data <%s> from <%s>",
                pushEvent.getData(), pushEvent.getPushProvider().getClass().getName());
        Toast.makeText(RuntimeContext.getApplication(),
                "onPushReceived handled by Activity", Toast.LENGTH_SHORT).show();
        if (!passPushHandling) {
            pushHandled.set(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean test(PushEvent pushEvent) {
        return true;
    }

    @Override
    public boolean shouldPostToMainThread() {
        return true;
    }

}
