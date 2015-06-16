package com.laxture.lib.timer;

import android.app.PendingIntent;

public class ClockAlarm extends Alarm {

    private PendingIntent pendingIntent;
    private String name;

    public ClockAlarm(String name, long interval, AlarmListener listener) {
        super(-1, interval, listener);

        setName(name);
    }

    public void cancel() {
        ClockAlarmService.cancel(this);
    }

    public PendingIntent getPendingIntent() {
        return this.pendingIntent;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
