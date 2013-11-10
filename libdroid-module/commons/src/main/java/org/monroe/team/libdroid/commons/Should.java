package org.monroe.team.libdroid.commons;

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


}
