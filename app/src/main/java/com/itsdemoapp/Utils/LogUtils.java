package com.itsdemoapp.Utils;

import android.util.Log;

/**
 * @author Ankur Khandelwal on 30/05/17.
 */

public class LogUtils {

    private static final boolean isLogEnabled = true;

    public static void printLog(String tag, String message) {
        if (isLogEnabled) {
            Log.d(tag, message);
        }
    }
}
