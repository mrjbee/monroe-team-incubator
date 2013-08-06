package org.monroe.team.aas.ui.common.logging;

import org.monroe.team.aas.ui.common.logging.AndroidLogger;
import org.monroe.team.aas.ui.common.logging.Logger;

/**
 * User: MisterJBee
 * Date: 6/30/13 Time: 1:25 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class Debug {

    public static final Logger DEF = new AndroidLogger("DEBUG");

    public static void v(String message, Object ... args){
        DEF.v(message, args);
    };

    public static void d(String message, Object ... args){
        DEF.d(message, args);
    };

    public static void i(String message, Object ... args){
        DEF.i(message, args);
    };

    public static void w(Exception e, String message, Object ... args){
        DEF.w(e, message, args);
    };

    public static void e(Exception e, String message, Object ... args){
        DEF.e(e, message, args);
    };
}
