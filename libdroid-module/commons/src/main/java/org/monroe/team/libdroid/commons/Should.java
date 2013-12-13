package org.monroe.team.libdroid.commons;

import android.os.Looper;
import org.monroe.team.libdroid.logging.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 11/4/13 Time: 11:59 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class Should {

    private Should() {}

    public static void beTrue(String msg, boolean value){
        if (!value) throw new RuntimeException(msg);
    }

    public static void beNotNull(Object ... objects){
      beNotNull("Not allowed.", objects);
    }

    public static void beNotNull(String msg, Object ... objects){
        List<Object> list = Arrays.asList(objects);
        for (Object obj : list) {
            if (obj == null) throw new NullPointerException(msg + " "+list);
        }
    }

    public static void fails(String msg){
        throw new RuntimeException(msg);
    }

    public static void beImplemented(String msg){
        throw new UnsupportedOperationException(msg);
    }

    public static Object reThrow(String msg, Exception e){
        throw new RuntimeException(msg, e);
    }

    public static Object reThrow(String msg, Logger logger, Exception e){
        logger.e(e, msg);
        throw new RuntimeException(msg, e);
    }


    public static void runOnUI() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new RuntimeException("Should run on UI");
        }
    }

    public static RuntimeException failsHere(String s) {
        throw new RuntimeException(s);
    }

    public static RuntimeException failsHere(String s, Exception e) {
        return new RuntimeException(s,e);
    }
}
