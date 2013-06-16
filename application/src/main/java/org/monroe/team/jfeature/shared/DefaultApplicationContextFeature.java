package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeaturePriority;
import org.monroe.team.jfeature.application.Main;
import org.monroe.team.jfeature.logging.LogFactory;
import org.monroe.team.jfeature.shared.api.ApplicationContextFeature;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 12:06 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */

@Feature(impl = ApplicationContextFeature.class, startPriority = FeaturePriority.HIGH)
public class DefaultApplicationContextFeature implements ApplicationContextFeature {

    @Override
    public LogFactory getLogFactory() {
        return Main.getApplication().getLogFactory();
    }

    @Override
    public void continueMain(int statusCode) {
        Main.getApplication().continueMain(statusCode);
    }
}
