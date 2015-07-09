package com.laxture.lib.task;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.laxture.lib.task.TaskListener.*;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;

public abstract class AbstractTask<Result> {

    static final int MESSAGE_POST_START = 0x1;
    static final int MESSAGE_POST_PROGRESS = 0x2;
    static final int MESSAGE_POST_RESULT = 0x3;
    static final int MESSAGE_POST_DATA_CHANGED = 0x4;

    // state
    public enum State {
        NotStart, Pending, Running, Finished, Cancelled, Failed
    }
    volatile State mState = State.NotStart;
    public State getState() { return mState; }
    public void setState(State state) { mState = state; }

    private Result mResult;

    // thread control
    MainHandler mMainHandler;
    final AtomicBoolean mCancelled = new AtomicBoolean();
    final AtomicBoolean mTaskInvoked = new AtomicBoolean();

    // listeners
    private Set<TaskStartListener> mStartListeners = new HashSet<>();
    private Set<TaskProgressUpdatedListener> mProgressUpdatedListeners = new HashSet<>();
    private Set<TaskFinishedListener<Result>> mFinishedListeners = new HashSet<TaskFinishedListener<Result>>();
    private Set<TaskCancelledListener<Result>> mCancelledListeners = new HashSet<TaskCancelledListener<Result>>();
    private Set<TaskFailedListener> mFailedListeners = new HashSet<>();
    private Set<TaskDataChangedListener> mDataChangedListeners = new HashSet<>();

    // error code
    private TaskException mException;
    public TaskException getErrorDetails() { return mException; }
    public void setErrorDetails(TaskException exception) { mException = exception; }

    private String mId;
    public String getId() { return mId; }
    public void setId(String id) { mId = id; }

    private Object mTag;
    public Object getTag() { return mTag; }
    public void setTag(Object tag) { mTag = tag; }

    public boolean mHasPostStart;

    //*************************************************************************
    // These method need to be override in sub class
    //*************************************************************************

    protected abstract Result run();

    public boolean cancel() {
        mCancelled.set(true);
        return true;
    }

    public void setResult(Result result) {
        mResult = result;
    }

    public Result getResult() {
        return mResult;
    }

    //*************************************************************************
    // Public/Protected Method
    //*************************************************************************

    public void addStartListener(TaskStartListener callback) {
        mStartListeners.add(callback);
    }

    public void addProgressUpdatedListener(TaskProgressUpdatedListener callback) {
        mProgressUpdatedListeners.add(callback);
    }

    public void addFinishedListener(TaskFinishedListener<Result> callback) {
        mFinishedListeners.add(callback);
    }

    public void addCancelledListener(TaskCancelledListener<Result> callback) {
        mCancelledListeners.add(callback);
    }

    public void addFailedListener(TaskFailedListener callback) {
        mFailedListeners.add(callback);
    }

    public void addDataChangedListener(TaskDataChangedListener callback) {
        mDataChangedListeners.add(callback);
    }

    public void cloneTaskListeners(AbstractTask<Result> task) {
        mStartListeners.addAll(task.mStartListeners);
        mProgressUpdatedListeners.addAll(task.mProgressUpdatedListeners);
        mFinishedListeners.addAll(task.mFinishedListeners);
        mCancelledListeners.addAll(task.mCancelledListeners);
        mFailedListeners.addAll(task.mFailedListeners);
        mDataChangedListeners.addAll(task.mDataChangedListeners);
    }

    public void removeAllTaskListeners() {
        mStartListeners.clear();
        mProgressUpdatedListeners.clear();
        mFinishedListeners.clear();
        mCancelledListeners.clear();
        mFailedListeners.clear();
        mDataChangedListeners.clear();
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }

    public final boolean isCancelled() {
        return mCancelled.get();
    }

    public void post(Runnable action) {
        mMainHandler.post(action);
    }

    public void postDataChanged(int dataIdentifier, Object changedData) {
        Message.obtain(mMainHandler, MESSAGE_POST_DATA_CHANGED,
                new TaskMessageBody<Result>(this, dataIdentifier, changedData)).sendToTarget();
    }

    //*************************************************************************
    // Internal Implementation
    //*************************************************************************

    public AbstractTask(Looper looper) {
        initHandler(looper);
    }

    public AbstractTask() {
        initHandler(null);
    }

    private void initHandler(Looper looper) {
        Looper callbackLooper = looper;
        // use main looper by default
        if (callbackLooper == null) callbackLooper = Looper.getMainLooper();
        // register handler on callback looper
        if (callbackLooper != null) {
            mMainHandler = new MainHandler(callbackLooper);
        }
    }

    protected final void postStart() {
        if (isCancelled() || mHasPostStart) return;
        if (mMainHandler != null) {
            Message.obtain(getMainHandler(), MESSAGE_POST_START,
                    new TaskMessageBody<Result>(this)).sendToTarget();
        } else {
            onTaskStart();
        }
        mHasPostStart = true;
    }


    /**
     * Sub-class call this method to publish progress info to UI.
     */
    protected final void postProgress(int totalSize, int currentSize) {
        if (isCancelled()) return;
        if (mMainHandler != null) {
            Message.obtain(mMainHandler, MESSAGE_POST_PROGRESS,
                    new TaskMessageBody<Result>(this, totalSize, currentSize)).sendToTarget();
        } else {
            onTaskProgressUpdate(totalSize, currentSize);
        }

    }

    /**
     * If cancel, try to call cancel callback
     */
    void postResultIfNotInvoked(Result result) {
        if (!mTaskInvoked.get()) {
            postResult(result);
        }
    }

    /**
     * This method must be called somewhere to trigger TaskListener callback.
     *
     * @param result
     * @return
     */
    protected Result postResult(Result result) {
        if (mMainHandler != null) {
            Message.obtain(mMainHandler, MESSAGE_POST_RESULT,
                    new TaskMessageBody<Result>(this, result, getErrorDetails())).sendToTarget();
        } else {
            finish(result, getErrorDetails());
        }
        return result;
    }

    void finish(Result result, TaskException exception) {
        if (isCancelled()) {
            LLog.d("%s%s is cancelled.", getClass().getSimpleName(),
                    Checker.isEmpty(mId) ? "" : " " + mId);
            onTaskCancelled(result);
            setState(State.Cancelled);

        } else if (mException != null) {
            LLog.w("%s%s quit by error %s.", getClass().getSimpleName(),
                    Checker.isEmpty(mId) ? "" : " "+mId, mException.getErrorCode());
            onTaskFailed(result, mException);
            setState(State.Failed);

        } else {
            LLog.d("%s%s is completed.", getClass().getSimpleName(),
                    Checker.isEmpty(mId) ? "" : " "+mId);
            onTaskFinished(result);
            setState(State.Finished);
        }
    }

    private static class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void handleMessage(Message msg) {
            TaskMessageBody body = (TaskMessageBody) msg.obj;
            switch (msg.what) {
            case MESSAGE_POST_START:
                body.task.onTaskStart();
                break;
            case MESSAGE_POST_PROGRESS:
                body.task.onTaskProgressUpdate(
                        body.totalSize, body.currentSize);
                break;
            case MESSAGE_POST_RESULT:
                body.task.finish(body.result, body.exception);
                break;
            case MESSAGE_POST_DATA_CHANGED:
                body.task.onTaskDataChanged(
                        body.dataIndentifier, body.changedData);
                break;
            }
        }
    }

    static class TaskMessageBody<Result> {
        final AbstractTask<?> task;
        Result result = null;
        int totalSize = -1;
        int currentSize = -1;
        int dataIndentifier = -1;
        Object changedData = null;
        TaskException exception;

        TaskMessageBody(AbstractTask<?> task) {
            this.task = task;
        }

        TaskMessageBody(AbstractTask<?> task, Result result, TaskException exception) {
            this.task = task;
            this.result = result;
            this.exception = exception;
        }

        TaskMessageBody(AbstractTask<?> task, int totalSize, int currentSize) {
            this.task = task;
            this.totalSize = totalSize;
            this.currentSize = currentSize;
        }

        TaskMessageBody(AbstractTask<?> task, int dataIndentifier, Object changedData) {
            this.task = task;
            this.dataIndentifier = dataIndentifier;
            this.changedData = changedData;
        }
    }

    //*************************************************************************
    // TaskCallback
    //
    // These methods will be delegated to UI thread, make sure they are only used
    // to update UI.
    //*************************************************************************

    public void onTaskStart() {
        if (Checker.isEmpty(mStartListeners)) return;
        for (final TaskStartListener callback : mStartListeners) {
            callback.onTaskStart();
        }
    }

    public void onTaskProgressUpdate(final int totalSize, final int currentSize) {
        if (Checker.isEmpty(mProgressUpdatedListeners)) return;
        for (final TaskProgressUpdatedListener callback : mProgressUpdatedListeners) {
            callback.onTaskProgressUpdated(totalSize, currentSize);
        }
    }

    public void onTaskFinished(final Result returnObj) {
        if (mFinishedListeners.size() == 0) return;
        for (final TaskFinishedListener<Result> callback : mFinishedListeners) {
            callback.onTaskFinished(returnObj);
        }
    }

    public void onTaskCancelled(final Result result) {
        if (mCancelledListeners.size() == 0) return;
        for (final TaskCancelledListener<Result> callback : mCancelledListeners) {
            callback.onTaskCancelled(result);
        }
    }

    public void onTaskFailed(final Result result, final TaskException ex) {
        if (mFailedListeners.size() == 0) return;
        for (final TaskFailedListener<Result> callback : mFailedListeners) {
            callback.onTaskFailed(result, ex);
        }
    }

    public void onTaskDataChanged(final int dataIndentifier, final Object changedData) {
        if (mDataChangedListeners.size() == 0) return;
        for (final TaskDataChangedListener callback : mDataChangedListeners) {
            callback.onTaskDataChanged(dataIndentifier, changedData);
        }
    }

}
