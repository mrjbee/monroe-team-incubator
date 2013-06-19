package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.ServiceFeature;
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
@Feature(impl = LoggingFeature.class)
public class DefaultLoggingFeature implements LoggingFeature, ServiceFeature{

    @FeatureInject
    private ApplicationContextFeature applicationContextFeature;

    @Override
    public Log get(String featureName) {
        return applicationContextFeature.getLogFactory().forFeature(featureName);
    }

    @Override
    public void onUp() {
        get("DUMMY").i("DUMMY starting.... please wait");
        new Thread(){
            @Override
            public void run() {
                try {
                    get("DUMMY").i("Thread....sleep");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                get("DUMMY").i("Thread....stop");
                applicationContextFeature.continueMain(3);

            }
        }.start();
    }

    @Override
    public void onDown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
