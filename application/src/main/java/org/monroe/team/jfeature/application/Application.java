package org.monroe.team.jfeature.application;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureContext;
import org.monroe.team.jfeature.FeatureException;
import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.logging.LogFactory;
import org.monroe.team.jfeature.shared.api.ApplicationContextFeature;
import org.reflections.Reflections;

import java.util.Set;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 11:23 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Application implements ApplicationContextFeature{

    private final FeatureContext featureContext;
    private final LogFactory logFactory;

    public Application(LogFactory logFactory) {
        this.logFactory = logFactory;
        featureContext = new FeatureContext(logFactory.forFeature("FeaturesContext"));
    }

    public void start() {
        Reflections reflections = new Reflections("org.monroe.team.jfeature.shared");
        Set<Class<?>> sharedFeatures =  reflections.getTypesAnnotatedWith(Feature.class);
        for (Class<?> sharedFeature : sharedFeatures) {
            try {
                featureContext.registrateFeatureClass(sharedFeature);
            } catch (FeatureException e) {
                Main.log.e(e, "Invalid feature description.");
                throw new RuntimeException(e);
            }
        }
        try {
            featureContext.init();
        } catch (FeatureException e) {
            Main.log.e(e, "Features init fail.");
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            featureContext.deInit();
        } catch (FeatureException e) {
            Main.log.e(e, "Features deInit fail.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public LogFactory getLogFactory() {
        return logFactory;
    }

    @Override
    public void continueMain(int statusCode) {
        Main.continueMain(statusCode);
    }
}
