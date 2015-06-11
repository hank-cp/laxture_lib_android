package com.laxture.lib.task;

/**
 * Callbacks for various Task life cycle
 */
public final class TaskListener {

    public interface TaskStartListener {
        void onTaskStart();
    }

    public interface TaskProgressUpdatedListener {
        void onTaskProgressUpdated(int totalSize, int currentSize);
    }

    public interface TaskFinishedListener<Result> {
        void onTaskFinished(Result result);
    }

    public interface TaskCancelledListener<Result> {
        void onTaskCancelled(Result result);
    }

    public interface TaskFailedListener<Result> {
        void onTaskFailed(Result result, TaskException ex);
    }

    public interface TaskDataChangedListener {
        /**
         * This callback will be fired when {@link AbstractTask#postDataChanged(int, Object)}
         * is called.
         */
        void onTaskDataChanged(int dataIdentifier, Object changeData);
    }
}
