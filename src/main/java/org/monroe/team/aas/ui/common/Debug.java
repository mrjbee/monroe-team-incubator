package org.monroe.team.aas.ui.common;

import org.monroe.team.aas.ui.common.logging.AndroidLogger;

/**
 * User: MisterJBee
 * Date: 6/30/13 Time: 1:25 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class Debug extends AndroidLogger{

    public static final Debug DEF = new Debug();

    private Debug() {
        super("Debug");
    }
}
