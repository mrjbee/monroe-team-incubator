package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.shared.api.ApplicationContextFeature;
import org.monroe.team.jfeature.shared.api.LauncherFeature;
import org.monroe.team.jfeature.shared.api.LoggingFeature;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 10:44 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Feature(impl = LauncherFeature.class)
public class DefaultLauncherFeature implements LauncherFeature{

    @FeatureInject
    ApplicationContextFeature applicationContextFeature;

    @FeatureInject
    LoggingFeature loggingFeature;

    @Override
    public void shutdown() {
        shutdown(0);
    }

    @Override
    public void shutdown(int statusCode) {
        applicationContextFeature.continueMain(statusCode);
    }
}
