package org.monroe.team.aas.ui.common;

import org.monroe.team.libdroid.logging.AndroidLogger;
import org.monroe.team.libdroid.logging.Logger;
import org.monroe.team.libdroid.logging.LoggerSetup;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class Logs {

    public final static Logger UI = LoggerSetup.createLogger("UI");
    public static final Logger MODEL = LoggerSetup.createLogger("MODEL");
    public static final Logger SERVICE = LoggerSetup.createLogger("SERVICE");

    private Logs() {}
}
