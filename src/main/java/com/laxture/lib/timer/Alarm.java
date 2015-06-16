package com.laxture.lib.timer;

public abstract class Alarm {

    private long interval = 10000L;
    private int clockId = -1;
    private AlarmListener listener;

    protected Alarm(int clockId, long interval, AlarmListener listener) {
        setInterval(interval);
        setClockId(clockId);
        setListener(listener);
    }

    public abstract void cancel();

    public long getInterval() {
        return this.interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public int getClockId() {
        return this.clockId;
    }

    private void setClockId(int clockId) {
        this.clockId = clockId;
    }

    public AlarmListener getListener() {
        return this.listener;
    }

    private void setListener(AlarmListener listener) {
        this.listener = listener;
    }

    public interface AlarmListener {
        boolean onAlarmWentOff(Alarm alarm);
    }
}
