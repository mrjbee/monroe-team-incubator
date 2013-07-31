package org.monroe.team.aas.ui.common.logging;

import android.util.Log;

import java.text.MessageFormat;

/**
h * User: MisterJBee
 * Date: 6/26/13 Time: 12:43 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class AndroidLogger implements Logger {

    //TODO: think about Log.isLoggable(TAG, Log.VERBOSE)
    private static final boolean LOGGING_ENABLED = true;

    private final String TAG;

    public AndroidLogger(String TAG) {
        this.TAG = TAG;
    }

    @Override
    public void v(String message, Object... args) {
        if (isLoggingDisabled()) return;
        String outMsg = messageProcessing(message, args);
        Log.v(TAG, outMsg);
    }

    @Override
    public void d(String message, Object... args) {
        if (isLoggingDisabled()) return;
        String outMsg = messageProcessing(message, args);
        Log.d(TAG, outMsg);
    }

    @Override
    public void i(String message, Object... args) {
        if (isLoggingDisabled()) return;
        String outMsg = messageProcessing(message, args);
        Log.i(TAG, outMsg);
    }

    @Override
    public void w(Exception e, String message, Object... args) {
        if (isLoggingDisabled()) return;
        String outMsg = messageProcessing(message, args);
        Log.w(TAG, outMsg, e);
    }

    @Override
    public void e(Exception e, String message, Object... args) {
        if (isLoggingDisabled()) return;
        String outMsg = messageProcessing(message, args);
        Log.e(TAG, outMsg, e);
    }

    @Override
    public Logger extend(String extendTag) {
        return new AndroidLogger(TAG+"."+extendTag);
    }


    private String messageProcessing(String message, Object[] args) {
        String outMsg = MessageFormat.format(message, args);
        outMsg = "["+Thread.currentThread().getId()+"] " + outMsg;
        return outMsg;
    }

    private boolean isLoggingDisabled() {
        return !LOGGING_ENABLED;
    }

}
