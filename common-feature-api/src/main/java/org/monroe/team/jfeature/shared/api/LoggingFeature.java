package org.monroe.team.jfeature.shared.api;

import org.monroe.team.jfeature.logging.Log;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 11:47 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface LoggingFeature {
    Log get(String featureName);
}
