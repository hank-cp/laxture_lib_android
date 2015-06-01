package com.laxture.lib.util;

public class UnHandledException extends RuntimeException {

    private static final long serialVersionUID = -7932499350370137349L;

    public UnHandledException() {
        super();
    }

    public UnHandledException(String message) {
        super(message);
    }

    public UnHandledException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnHandledException(Throwable cause) {
        super(cause);
    }
}
