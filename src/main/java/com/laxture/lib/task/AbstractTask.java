package com.laxture.lib.task;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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

    // listener
    private Set<TaskListener<Result>> mListeners = new HashSet<TaskListener<Result>>();
    public Set<TaskListener<Result>> getTaskListeners() { return mListeners; }

    // add new listener interface to reduce changes to old code
    private Set<TaskListener2<Result>> mListeners2 = new HashSet<TaskListener2<Result>>();
    public Set<TaskListener2<Result>> getTaskListeners2() { return mListeners2; }

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

    public void addTaskListener(TaskListener<Result> callback) {
        mListeners.add(callback);
    }

    public void addTaskListener2(TaskListener2<Result> callback) {
        mListeners2.add(callback);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addAllTaskListeners(Set listeners) {
        mListeners.addAll(listeners);
    }

    public void removeTaskListener(TaskListener<Result> callback) {
        mListeners.remove(callback);
    }

    public void removeTaskListener2(TaskListener2<Result> callback) {
        mListeners2.remove(callback);
    }

    public void removeAllTaskListeners() {
        mListeners.clear();
        mListeners2.clear();
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

    public void postDataChanged(int dataIndentifier, Object changedData) {
        Message.obtain(mMainHandler, MESSAGE_POST_DATA_CHANGED,
                new TaskMessageBody<Result>(this, dataIndentifier, changedData)).sendToTarget();
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
        if (mListeners.size() == 0) return;
        for (final TaskListener<Result> callback : mListeners) {
            callback.onTaskStart();
        }
    }

    public void onTaskProgressUpdate(final int totalSize, final int currentSize) {
        if (mListeners.size() == 0) return;
        for (final TaskListener<Result> callback : mListeners) {
            callback.onTaskProgressUpdated(totalSize, currentSize);
        }
    }

    public void onTaskFinished(final Result returnObj) {
        if (mListeners.size() == 0) return;
        for (final TaskListener<Result> callback : mListeners) {
            callback.onTaskFinished(returnObj);
        }
    }

    public void onTaskCancelled(final Result result) {
        if (mListeners.size() == 0) return;
        for (final TaskListener<Result> callback : mListeners) {
            callback.onTaskCancelled(result);
        }
    }

    public void onTaskFailed(final Result result, final TaskException ex) {
        if (mListeners.size() == 0) return;
        for (final TaskListener<Result> callback : mListeners) {
            callback.onTaskFailed(result, ex);
        }
    }

    public void onTaskDataChanged(final int dataIndentifier, final Object changedData) {
        if (mListeners2.size() == 0) return;
        for (final TaskListener2<Result> callback : mListeners2) {
            callback.onTaskDataChanged(dataIndentifier, changedData);
        }
    }

}
