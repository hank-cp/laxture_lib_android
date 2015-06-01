package com.laxture.lib.test.task;

import com.laxture.lib.util.UnHandledException;

public class SimulatedTimerTask extends SimulatorTask {

    private long mTime;

    public SimulatedTimerTask(long time) {
        mTime = time;
    }

    @Override
    protected String simulateRun() {
        try {
            Thread.sleep(mTime);
            postProgress(100, 50);
        } catch (InterruptedException e) {
            new UnHandledException("Failed to sleep");
        }
        return "SimulatorTask Finished";
    }

}
