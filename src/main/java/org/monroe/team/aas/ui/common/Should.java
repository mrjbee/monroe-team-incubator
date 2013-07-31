package org.monroe.team.aas.ui.common;

/**
 * User: MisterJBee
 * Date: 6/26/13 Time: 10:48 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Should {

    public static Object doThrowInvalidUsage(String msg) {
        throw new UnsupportedOperationException(msg);
    }

    public static Object fail(String msg) {
        throw new IllegalStateException(msg);
    }

    public static Object fail(String msg, Exception e) {
        throw new RuntimeException(msg,e);
    }

    public static void beTrue(boolean b) {
        beTrue("Should be true", b);
    }

    public static Object reThrow(Exception e) {
        throw new IllegalStateException(e);
    }

    public static void beTrue(String msg, boolean b) {
        if (b == false){
            throw new RuntimeException(msg);
        }
    }
}
