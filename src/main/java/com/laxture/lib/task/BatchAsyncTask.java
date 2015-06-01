package com.laxture.lib.task;

import java.util.ArrayList;
import java.util.List;

public abstract class BatchAsyncTask<Result> extends AbstractAsyncTask<Result> {

    private List<AbstractAsyncTask<Result>> mTasks = new ArrayList<AbstractAsyncTask<Result>>();

    public void addTask(AbstractAsyncTask<Result> task) {
        mTasks.add(task);
    }

    @Override
    protected Result run() {
        Result result = null;
        for (AbstractAsyncTask<Result> task : mTasks) {
            result = task.run();

            if (task.getErrorDetails() != null) {
                setErrorDetails(task.getErrorDetails());
                return result;
            }
        }
        return result; // return last result
    }

    // TODO set internal ProgressListener

}
