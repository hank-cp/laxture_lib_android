package com.laxture.lib.timer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class SimpleAlarm extends Alarm {

    private static final String CLOCK_SERVICE_NAME = "simple.alarm";
    private static final int CLOCK_MAX_COUNT = 32;
    private static SimpleAlarm[] clocks;
    private static HandlerThread clockThread;
    private static Handler clockHandler;

    public static SimpleAlarm set(long interval, long delay, AlarmListener listener) {
        synchronized (SimpleAlarm.class) {
            initClockService();

            int id = -1;

            for (int i = 0; i < clocks.length; i++) {
                if (clocks[i] != null)
                    continue;
                id = i;
                break;
            }

            if (id < 0) {
                return null;
            }

            SimpleAlarm clock = new SimpleAlarm(id, interval, listener);
            clocks[id] = clock;

            prepareNextInterval(id, delay);

            return clock;
        }
    }

    public static void cancel(SimpleAlarm clock) {
        synchronized (SimpleAlarm.class) {
            if (clock == null) {
                return;
            }

            int clockId = clock.getClockId();

            if ((clockId < 0) || (clockId >= clocks.length)) {
                return;
            }

            SimpleAlarm theClock = clocks[clockId];

            if ((theClock != null) && (theClock == clock)) {
                clocks[clockId] = null;
            }
        }
    }

    private static void initClockService() {
        synchronized (SimpleAlarm.class) {
            if (clocks == null) {
                clocks = new SimpleAlarm[CLOCK_MAX_COUNT];
            }

            if (clockThread == null) {
                clockThread = new HandlerThread(CLOCK_SERVICE_NAME);
            }

            if (!clockThread.isAlive()) {
                clockThread.start();
            }

            if (clockThread.isAlive()) {
                if (clockHandler == null) {
                    clockHandler = new Handler(clockThread.getLooper()) {
                        public void handleMessage(Message msg) {
                            SimpleAlarm.handleClockMessage(msg.what);
                        }
                    };
                }
            }
        }
    }

    private static void handleClockMessage(int clockId) {
        if ((clockId < 0) || (clockId >= clocks.length)) {
            return;
        }

        SimpleAlarm clock = clocks[clockId];

        if (clock != null) {
            AlarmListener listener = clock.getListener();

            if (listener != null) {
                boolean proceed = listener.onAlarmWentOff(clock);

                if (proceed) {
                    prepareNextInterval(clockId, clock.getInterval());
                } else {
                    cancel(clock);
                }
            }
        }
    }

    private static void prepareNextInterval(int clockId, long delay) {
        if (clockHandler != null) {
            if (delay > 0L) {
                clockHandler.sendEmptyMessageDelayed(clockId, delay);
            } else {
                clockHandler.sendEmptyMessage(clockId);
            }
        }
    }

    protected SimpleAlarm(int clockId, long interval, AlarmListener listener) {
        super(clockId, interval, listener);
    }

    public void cancel() {
        cancel(this);
    }
}
