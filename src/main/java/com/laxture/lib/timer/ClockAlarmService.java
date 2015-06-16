package com.laxture.lib.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.laxture.lib.RuntimeContext;

import java.util.Hashtable;

public class ClockAlarmService {

    private static Hashtable<String, ClockAlarm> ALARM_CLOCK_MAP = new Hashtable<>();

    public static void set(ClockAlarm clock) {
        try {
            long duration = SystemClock.elapsedRealtime() + clock.getInterval();
            Intent intentToFire = new Intent(clock.getName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    RuntimeContext.getApplication(), 0, intentToFire,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            clock.setPendingIntent(pendingIntent);

            AlarmManager alarmManager = RuntimeContext.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    duration, pendingIntent);

            ALARM_CLOCK_MAP.put(clock.getName(), clock);
        } catch (Exception ex) {}
    }

    public static void cancel(ClockAlarm clock) {
        if (clock.getPendingIntent() != null) {
            AlarmManager alarmManager = RuntimeContext.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(clock.getPendingIntent());
            clock.setPendingIntent(null);
        }

        ALARM_CLOCK_MAP.remove(clock.getName());
    }

    public static void cancelWhenArrived(ClockAlarm clock) {
        synchronized (ClockAlarmService.class) {
            clock.setPendingIntent(null);

            ALARM_CLOCK_MAP.remove(clock.getName());
        }
    }

    public static ClockAlarm getClock(String name) {
        return ALARM_CLOCK_MAP.get(name);
    }

}
