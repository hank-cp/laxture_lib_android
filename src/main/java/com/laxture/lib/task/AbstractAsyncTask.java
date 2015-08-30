package com.laxture.lib.task;

import android.os.Process;

import com.laxture.lib.BuildConfig;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.UnHandledException;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractAsyncTask<Result> extends AbstractTask<Result> {

    private final Callable<Result> mWorker;
    private final MyFutureTask<Result> mFuture;

    // debugging
    private AtomicLong mStartTime;
    private AtomicLong mEndTime;

    //*************************************************************************
    // These method need to be override in sub class
    //*************************************************************************

    /**
     * Attempt to cancel this task.
     *
     * Override this method in sub-class if custom cancel action is needed,
     * e.g. HttpConnection.abort()
     */
    public boolean cancel() {
        super.cancel();
        // call cancelled callback immediately if task is still pending to execute.
        if (mState == State.Pending) onTaskCancelled(null);
        return mFuture.cancel(true);
    }

    //*************************************************************************
    // Public/Protected Method
    //*************************************************************************

    @Override
    public void setResult(Result result) {
        mFuture.set(result);
    }

    @Override
    public Result getResult() {
        try {
            return mFuture.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new UnHandledException("Failed to get task result. "+e.getCause(), e);
        }
    }

    /**
     * Only worked under debug mode.
     */
    public long getUsedTime() {
        if (BuildConfig.DEBUG) return mEndTime.get() - mStartTime.get();
        return 0;
    }

    /**
     * add to specific Executor
     *
     * @param exec
     * @return
     */
    protected MyFutureTask<Result> executeOnExecutor(Executor exec) {
        if (mState == State.Pending || mState == State.Running) {
            LLog.w("Cannot start task:"
                    + " the task is already running or pending in queue.");
            return mFuture;

        } else if (mState != State.NotStart) {
            LLog.e("Restart task:"
                    + " the task has been already executed or cancelled.");
        }

        setState(State.Pending);
        exec.execute(mFuture);
        return mFuture;
    }

    //*************************************************************************
    // Internal Implementation
    //*************************************************************************

    /**
     * Task must be created on Main thread.
     */
    public AbstractAsyncTask() {
        mWorker = new Callable<Result>() {
            public Result call() throws Exception {
                postStart();

                mTaskInvoked.set(true);
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                } catch (Exception e) {
                    LLog.w("Failed to set thread priority.");
                }
                setState(State.Running);

                if (BuildConfig.DEBUG) mStartTime = new AtomicLong(System.currentTimeMillis());
                Result result = run();
                if (BuildConfig.DEBUG) mEndTime = new AtomicLong(System.currentTimeMillis());

                return postResult(result);
            }
        };

        mFuture = new MyFutureTask<Result>(this, mWorker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    LLog.w("Thread Interrupted error.", e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occured while executing run()",
                            e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                    // cancel failed, wait to finished.
                }
            }
        };
    }

    //*************************************************************************
    // FutureTask
    //*************************************************************************

    public static class MyFutureTask<V> extends FutureTask<V> {

        private AbstractAsyncTask<V> mTask;
        AbstractAsyncTask<V> getTask() { return mTask; }

        public MyFutureTask(AbstractAsyncTask<V> task, Callable<V> callable) {
            super(callable);
            mTask = task;
        }

        // expose to AbstractAsyncTask
        @Override
        protected void set(V v) {
            super.set(v);
        }
    }

}
