package org.monroe.team.libdroid.logging;

/**
 * User: MisterJBee
 * Date: 6/30/13 Time: 1:25 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class Debug {

    public static Logger DEF;

    public static void v(String message, Object ... args){
        getDef().v(message, args);
    };

    public static void d(String message, Object ... args){
        getDef().d(message, args);
    };

    public static void i(String message, Object ... args){
        getDef().i(message, args);
    };

    public static void w(Exception e, String message, Object ... args){
        getDef().w(e, message, args);
    };

    public static void e(Exception e, String message, Object ... args){
        getDef().e(e, message, args);
    };

    private synchronized static Logger getDef() {
        if (DEF == null){
            DEF = LoggerSetup.createLogger("DEBUG");
        }
        return DEF;
    }
}
