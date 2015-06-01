package com.laxture.lib.task;

public class TaskException extends Exception {

    private static final long serialVersionUID = -1053643297931040840L;

    public static final int ERROR_CODE_SUCCESSFUL = 0;

    public static final int ERROR_CODE_THREAD_MANAGEMENT_ERROR = 6000001;

    private int mErrorCode;

    public TaskException(int errorCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        mErrorCode = errorCode;
    }

    public TaskException(int errorCode, String detailMessage) {
        super(detailMessage);
        mErrorCode = errorCode;
    }

    public TaskException(int errorCode, Throwable throwable) {
        super(throwable);
        mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

}
