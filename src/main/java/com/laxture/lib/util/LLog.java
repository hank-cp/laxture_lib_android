package com.laxture.lib.util;

import android.util.Log;

import com.laxture.lib.BuildConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper of {@link Log}
 */
public class LLog {

    public static boolean enableLogCat = BuildConfig.DEBUG;

    public static final int TOP_STACK = 5;

    public static int LEVEL = Log.VERBOSE;

    private static final String LOG_FORMAT = "[%s:%d] %s";

    // 1st elements is "findTagByDepth", 2nd is log method, 3rd is what we are looking for.
    private static final int STACK_DEPTH_TO_FIND = 2;

    public static class DebugMeta {
        public String tag;
        public String msg;

        public DebugMeta(String tag, String msg) {
            this.tag = tag;
            this.msg = msg;
        }
    }

    public static void setLogLevel(int level) {
        if (level >= Log.VERBOSE) {
            LEVEL = level;
        }
    }

    public static DebugMeta formetMsg(final String msg, Object... args) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();
        if (elements == null || STACK_DEPTH_TO_FIND >= elements.length) {
            return new DebugMeta("Error", "Cannot find debug meta info!");
        }
        StackTraceElement element = elements[STACK_DEPTH_TO_FIND];
        String className = element.getClassName();
        String callerClassName = className.substring(className.lastIndexOf('.') + 1);
        String callerMethodName = element.getMethodName();
        int callerMethodLineNumber = element.getLineNumber();
        String formattedMsg = msg;
        try {
             formattedMsg = String.format(LOG_FORMAT, callerMethodName, callerMethodLineNumber,
                !Checker.isEmpty(args) ? String.format(msg, args) : msg);
        } catch (Exception e) {}
        return new DebugMeta(callerClassName, formattedMsg);
    }

    public static List<StackTraceElement> getTopStackTraceElement(Throwable cause, String filterPackageName) {
        List<StackTraceElement> topStack = new ArrayList<>(TOP_STACK);
        for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
            if (stackTraceElement.getLineNumber() <= 0
                    || !stackTraceElement.getClassName().startsWith(filterPackageName)) continue;
            topStack.add(stackTraceElement);
            if (topStack.size() >= 5) break;
        }
        return topStack;
    }

    public static String summaryTopStack(Throwable cause, String filterPackageName) {
        List<StackTraceElement> stack = getTopStackTraceElement(cause, filterPackageName);
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<stack.size(); i++) {
            sb.append(String.format("%s:%s(%s)", stack.get(i).getClassName(), stack.get(i).getMethodName(), stack.get(i).getLineNumber()));
            if (i != stack.size()-1) sb.append("\n");
        }
        return sb.toString();
    }

    public static void v(String msg, Object... args) {
        if (LEVEL <= Log.VERBOSE) {
            DebugMeta debugMeta = formetMsg(msg, args);
            if (enableLogCat) Log.v(debugMeta.tag, debugMeta.msg);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.VERBOSE, debugMeta.tag, debugMeta.msg, null);
        }
    }

    public static void v(String msg, Throwable t, Object... args) {
        if (LEVEL <= Log.VERBOSE) {
            DebugMeta debugMeta = formetMsg(msg, args);
            if (enableLogCat) Log.v(debugMeta.tag, debugMeta.msg, t);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.VERBOSE, debugMeta.tag, debugMeta.msg, t);
        }
    }

    public static void d(String msg, Object... args) {
        if (LEVEL <= Log.DEBUG) {
            DebugMeta debugMeta = formetMsg(msg, args);
            if (enableLogCat) Log.d(debugMeta.tag, debugMeta.msg);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.DEBUG, debugMeta.tag, debugMeta.msg, null);
        }
    }

    public static void d(String msg, Throwable t, Object... args) {
        if (LEVEL <= Log.DEBUG) {
            DebugMeta debugMeta = formetMsg(msg, args);
            if (enableLogCat) Log.d(debugMeta.tag, debugMeta.msg, t);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.DEBUG, debugMeta.tag, debugMeta.msg, t);
        }
    }

    public static void i(String msg, Object... args) {
        DebugMeta debugMeta = formetMsg(msg, args);
        if (LEVEL <= Log.INFO) {
            if (enableLogCat) Log.i(debugMeta.tag, debugMeta.msg);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.INFO, debugMeta.tag, debugMeta.msg, null);
        }
    }

    public static void i(String msg, Throwable t, Object... args) {
        DebugMeta debugMeta = formetMsg(msg, args);
        if (LEVEL <= Log.INFO) {
            if (enableLogCat) Log.i(debugMeta.tag, debugMeta.msg, t);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.INFO, debugMeta.tag, debugMeta.msg, t);
        }
    }

    public static void w(String msg, Object... args) {
        DebugMeta debugMeta = formetMsg(msg, args);
        if (LEVEL <= Log.WARN) {
            if (enableLogCat) Log.w(debugMeta.tag, debugMeta.msg);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.WARN, debugMeta.tag, debugMeta.msg, null);
        }
    }

    public static void w(String msg, Throwable t, Object... args) {
        DebugMeta debugMeta = formetMsg(msg, args);
        if (LEVEL <= Log.WARN) {
            if (enableLogCat) Log.w(debugMeta.tag, debugMeta.msg, t);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.WARN, debugMeta.tag, debugMeta.msg, t);
        }
    }

    public static void e(String msg, Object... args) {
        DebugMeta debugMeta = formetMsg(msg, args);
        if (LEVEL <= Log.ERROR) {
            if (enableLogCat) Log.e(debugMeta.tag, debugMeta.msg);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.ERROR, debugMeta.tag, debugMeta.msg, null);
        }
    }

    public static void e(String msg, Throwable t, Object... args) {
        DebugMeta debugMeta = formetMsg(msg, args);
        if (LEVEL <= Log.ERROR) {
            if (enableLogCat) Log.e(debugMeta.tag, debugMeta.msg, t);
            if (null != sByPassLogger) sByPassLogger.onLog(Log.ERROR, debugMeta.tag, debugMeta.msg, t);
        }
    }

    public interface ByPassLogger {
        void onLog(int level, String tag, String msg, Throwable t);
    }

    private static ByPassLogger sByPassLogger;

    public synchronized static void setByPassLogger(ByPassLogger logger) {
        sByPassLogger = logger;
    }

    public synchronized static ByPassLogger getByPassLogger() {
        return sByPassLogger;
    }
}
