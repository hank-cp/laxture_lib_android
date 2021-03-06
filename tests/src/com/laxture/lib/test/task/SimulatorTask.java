package com.laxture.lib.test.task;

import java.util.concurrent.CountDownLatch;

import android.widget.Toast;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.task.AbstractAsyncTask;
import com.laxture.lib.task.TaskException;
import com.laxture.lib.task.TaskListener;
import com.laxture.lib.util.LLog;

public abstract class SimulatorTask extends AbstractAsyncTask<String> {

    // test thread control
    public CountDownLatch testThreadLatch;

    // status
    public boolean started;
    public int totalSize;
    public int currentSize;
    public boolean finished;
    public boolean cancelled;
    public boolean failed;

    // switches
    public boolean testCancel;
    public boolean testFail;

    public SimulatorTask() {
        addStartListener(new DefaultSimulatorCallback());
        addProgressUpdatedListener(new DefaultSimulatorCallback());
        addFinishedListener(new DefaultSimulatorCallback());
        addCancelledListener(new DefaultSimulatorCallback());
        addFailedListener(new DefaultSimulatorCallback());
    }

    @Override
    public String run() {
        if (testCancel) {
            setResult("SimulatorTask Cancelled");
            cancel();
        }

        String result = simulateRun();

        if (testFail) {
            setErrorDetails(new TaskException(999, "SimulatorTask Failed"));
            return "SimulatorTask Failed";
        } else {
            return result;
        }
    }

    protected abstract String simulateRun();

    @Override
    public boolean cancel() {
        setResult("SimulatorTask Cancelled");
        return super.cancel();
    }

    public class DefaultSimulatorCallback implements
            TaskListener.TaskStartListener,
            TaskListener.TaskProgressUpdatedListener,
            TaskListener.TaskFinishedListener<String>,
            TaskListener.TaskCancelledListener<String>,
            TaskListener.TaskFailedListener<String> {

        @Override
        public void onTaskStart() {
            started = true;
            LLog.v("Simulator Task Start");
            Toast.makeText(RuntimeContext.getApplication(),
                    "onTaskStart", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskProgressUpdated(int totalSize, int currentSize) {
            SimulatorTask.this.totalSize = totalSize;
            SimulatorTask.this.currentSize = currentSize;
            LLog.v("Simulator Task Progress Updated");
            Toast.makeText(RuntimeContext.getApplication(),
                    "onTaskProgressUpdated :"+totalSize+"/"+currentSize,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskFinished(String result) {
            finished = true;
            LLog.v("Simulator Task Finished");
            Toast.makeText(RuntimeContext.getApplication(),
                    "onTaskFinished : "+result, Toast.LENGTH_SHORT).show();
            if (testThreadLatch != null) testThreadLatch.countDown();
        }

        @Override
        public void onTaskCancelled(String result) {
            cancelled = true;
            LLog.v("Simulator Task Cancelled");
            Toast.makeText(RuntimeContext.getApplication(),
                    "onTaskCancelled : "+result, Toast.LENGTH_SHORT).show();
            if (testThreadLatch != null) testThreadLatch.countDown();
        }

        @Override
        public void onTaskFailed(String result, TaskException ex) {
            failed = true;
            LLog.v("Simulator Task Failed");
            Toast.makeText(RuntimeContext.getApplication(),
                    "onTaskFailed : "+result+"/"+ex.getErrorCode()+"/"+ex.getMessage(),
                    Toast.LENGTH_SHORT).show();
            if (testThreadLatch != null) testThreadLatch.countDown();
        }
    }
}
