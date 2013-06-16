package org.monroe.team.jfeature.shared.api;

import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.logging.LogFactory;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 12:02 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface ApplicationContextFeature {
    LogFactory getLogFactory();
    void continueMain(int statusCode);
}
