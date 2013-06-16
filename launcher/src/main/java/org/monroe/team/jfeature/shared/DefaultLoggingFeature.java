package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.logging.LogFactory;
import org.monroe.team.jfeature.shared.api.ApplicationContextFeature;
import org.monroe.team.jfeature.shared.api.LoggingFeature;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 11:50 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Feature
public class DefaultLoggingFeature implements LoggingFeature {

    @Feature
    private ApplicationContextFeature applicationContextFeature;

    @Override
    public Log get(String featureName) {
        return applicationContextFeature.getLogFactory().forFeature(featureName);
    }
}
