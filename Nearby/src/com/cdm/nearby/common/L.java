package com.cdm.nearby.common;

import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/13/13
 * Time: 9:42 PM
 */

public class L {
    private static final String TAG = "NearBy";

    public static void d(String str) {
        String appendedMsg = appendMsgAndInfo(str, getCurrentInfo());
        Log.d(TAG, appendedMsg);
    }

    public static void e(String str, Throwable throwable) {
        String appendedMsg = appendMsgAndInfo(str, getCurrentInfo());
        Log.e(TAG, appendedMsg, throwable);
    }

    private static String getCurrentInfo() {

        StackTraceElement[] eles = Thread.currentThread().getStackTrace();
        StackTraceElement targetEle = eles[5];
        String info = "(" + targetEle.getClassName() + "."
                + targetEle.getMethodName() + ":" + targetEle.getLineNumber()
                + ")";

        return info;
    }

    private static String appendMsgAndInfo(String msg, String info) {
        return msg + " " + getCurrentInfo();
    }
}
