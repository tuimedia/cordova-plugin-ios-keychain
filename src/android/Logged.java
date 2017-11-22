package com.tuimedia.cordova.keystorage;

import android.util.Log;

public abstract class Logged {
    protected void logException(Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
    }

    protected void logInfo(String message) {
        Log.i(getClass().getSimpleName(), message);
    }

    protected void logDebug(String message) {
        Log.d(getClass().getSimpleName(), message);
    }
}
