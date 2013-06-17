package org.monroe.team.jfeature;

import org.monroe.team.jfeature.logging.Log;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 12:34 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureContext {

    private final Log log;

    public FeatureContext(Log contextLogger) {
         log = contextLogger;
    }

    public void registrateFeatureClass(Class... featureClasses){
        for(Class aClass: featureClasses){
            log.v("Start processing feature class {0}", featureClasses);
        }
    }

    public void init() {

    }

    public void deInit() {

    }
}
