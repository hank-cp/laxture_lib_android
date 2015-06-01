package com.laxture.lib.task;

/**
 * Callbacks for various Task life cycle
 */
public interface TaskListener<Result> {
    public void onTaskStart();
    public void onTaskProgressUpdated(int totalSize, int currentSize);
    public void onTaskFinished(Result result);
    public void onTaskCancelled(Result result);
    public void onTaskFailed(Result result, TaskException ex);
}
