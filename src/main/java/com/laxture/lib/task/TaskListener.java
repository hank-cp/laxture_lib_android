package com.laxture.lib.task;

/**
 * Callbacks for various Task life cycle, all these callbacks will be executed
 * on main ui thread by default/
 */
public final class TaskListener {

    /**
     * Fired where task is ready to start in the queue.
     */
    public interface TaskStartListener {
        void onTaskStart();
    }

    /**
     * Fired where {@link AbstractTask#postProgress(int, int)} is called.
     */
    public interface TaskProgressUpdatedListener {
        void onTaskProgressUpdated(int totalSize, int currentSize);
    }

    /**
     * Fired where {@link AbstractTask#run()} is finished. Parameter result must
     * be set by {@link AbstractTask#setResult(Object)} during {@link AbstractTask#run()}
     *
     * @param <Result>
     */
    public interface TaskFinishedListener<Result> {
        void onTaskFinished(Result result);
    }

    /**
     * Fired where {@link AbstractTask#cancel()} is finished. Parameter result will be provided
     * if it's set by {@link AbstractTask#setResult(Object)} during {@link AbstractTask#run()}
     *
     * @param <Result>
     */
    public interface TaskCancelledListener<Result> {
        void onTaskCancelled(Result result);
    }

    /**
     * Fired where {@link AbstractTask#run()} is finished and {@link AbstractTask#getErrorDetails()}
     * is not empty. Parameter result will be provided if it's set by
     * {@link AbstractTask#setResult(Object)} during {@link AbstractTask#run()}
     *
     * @param <Result>
     */
    public interface TaskFailedListener<Result> {
        void onTaskFailed(Result result, TaskException ex);
    }

    /**
     * This callback will be fired when {@link AbstractTask#postDataChanged(int, Object)}
     * is called. You could use this callback to handle out-of-sync data in multithread environment.
     */
    public interface TaskDataChangedListener {
        void onTaskDataChanged(int dataIdentifier, Object changeData);
    }
}
