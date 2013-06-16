package org.monroe.team.jfeature.application;

import org.monroe.team.jfeature.logging.LogFactory;
import org.monroe.team.jfeature.shared.api.ApplicationContextFeature;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 11:23 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Application implements ApplicationContextFeature{

    public void start() {

    }
    public void stop() {

    }

    @Override
    public LogFactory getLogFactory() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void continueMain(int statusCode) {
        Main.continueMain(statusCode);
    }
}
