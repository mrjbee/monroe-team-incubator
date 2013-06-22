package org.monroe.team.jfeature.guice;

import com.google.inject.Inject;

import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/22/13 Time: 8:47 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureLifeCycleManager {

    @Inject (optional = true)
    List<FeatureStartAware> startAwares;

    @Inject (optional = true)
    List<FeatureStopAware> stopAwares;

    void onFeatureStart() {
        if (startAwares == null) return;
        for (FeatureStartAware startAware : startAwares) {
            startAware.start();
        }
    }

    public void onFeatureStop() {
        if (stopAwares == null) return;
        for (FeatureStopAware stopAware : stopAwares) {
            try{
                stopAware.stop();
            } catch (Exception e){
               //TODO: find what should I do if exception appears here
            }
        }

    }
}
