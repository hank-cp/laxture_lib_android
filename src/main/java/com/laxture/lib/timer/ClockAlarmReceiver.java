package com.laxture.lib.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.laxture.lib.timer.Alarm.AlarmListener;

public class ClockAlarmReceiver extends BroadcastReceiver {

    public final void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (action == null) return;

        ClockAlarm clock = ClockAlarmService.getClock(action);
        if (clock == null) return;

        AlarmListener listener = clock.getListener();
        if (listener == null) return;

        boolean proceed = listener.onAlarmWentOff(clock);

        if (proceed) {
            ClockAlarmService.set(clock);
        } else {
            ClockAlarmService.cancelWhenArrived(clock);
        }
    }
}
