package org.monroe.team.aas.ui.common;

import org.monroe.team.aas.ui.common.logging.AndroidLogger;
import org.monroe.team.aas.ui.common.logging.Logger;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class Logs {

    public final static Logger UI = new AndroidLogger("aas.UI");
    public static final Logger MODEL = new AndroidLogger("aas.MODEL");
    public static final Logger SERVICE = new AndroidLogger("aas.SERVICE");

    private Logs() {}
}
