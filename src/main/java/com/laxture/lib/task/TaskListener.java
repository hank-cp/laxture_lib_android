package com.laxture.lib.task;

/**
 * Callbacks for various Task life cycle
 */
public interface TaskListener<Result> {
    void onTaskStart();
    void onTaskProgressUpdated(int totalSize, int currentSize);
    void onTaskFinished(Result result);
    void onTaskCancelled(Result result);
    void onTaskFailed(Result result, TaskException ex);
}
