package com.laxture.lib.test.task;

import com.laxture.lib.util.UnHandledException;

import java.util.concurrent.CountDownLatch;

public class SimulatedLatchTask extends SimulatorTask {

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    protected String simulateRun() {
        try {
            latch.await();
            postProgress(100, 50);
        } catch (InterruptedException e) {
            new UnHandledException("Failed to sleep");
        }
        return "SimulatorTask Finished";
    }

    public void greenLight() {
        latch.countDown();
    }

    @Override
    public boolean cancel() {
        greenLight();
        return super.cancel();
    }

}
