package com.laxture.lib.task;

/**
 * Extra callbacks for various Task life cycle. This interface is created to
 * protected {@link TaskListener}.
 */
public interface TaskListener2<Result> {

    /**
     * This callback will be fired when {@link AbstractTask#postDataChanged(int, Object)}
     * is called.
     *
     * @param dataIndentifier
     * @param changeData
     */
    public void onTaskDataChanged(int dataIndentifier, Object changeData);
}
