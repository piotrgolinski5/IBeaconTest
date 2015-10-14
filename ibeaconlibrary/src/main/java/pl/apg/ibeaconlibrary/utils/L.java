package pl.apg.ibeaconlibrary.utils;

import android.util.Log;

public class L {
    private static final String TAG = "IBeaconLibraryManager";
    private static boolean ENABLE_DEBUG_LOGGING = true;

    public static void enableDebugLogging(boolean enableDebugLogging) {
        ENABLE_DEBUG_LOGGING = enableDebugLogging;
    }

    public static void v(String msg) {
        if (ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.v(TAG, logMsg);
        }
    }

    public static void d(String msg) {
        if (ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.d(TAG, logMsg);
        }
    }

    public static void i(String msg) {
        if (ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.i(TAG, logMsg);
        }
    }

    public static void w(String msg) {
        if (ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.w(TAG, logMsg);
        }
    }

    public static void e(String msg) {
        String logMsg = debugInfo() + msg;
        Log.e(TAG, logMsg);
    }

    public static void e(String msg, Throwable e) {
        if (ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.e(TAG, logMsg, e);
        }
    }

    public static void wtf(String msg) {
        if (ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.wtf(TAG, logMsg);
        }
    }

    public static void wtf(String msg, Exception exception) {
        if (ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.wtf(TAG, logMsg, exception);
        }
    }

    private static String debugInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String className = stackTrace[4].getClassName();
        String methodName = Thread.currentThread().getStackTrace()[4].getMethodName();
        int lineNumber = stackTrace[4].getLineNumber();
        return className + "." + methodName + ":" + lineNumber + " ";
    }

}