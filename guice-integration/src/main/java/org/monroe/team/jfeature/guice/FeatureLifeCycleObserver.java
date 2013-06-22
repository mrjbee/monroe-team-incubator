package org.monroe.team.jfeature.guice;

/**
 * User: MisterJBee
 * Date: 6/22/13 Time: 9:45 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface FeatureLifeCycleObserver {
    void onFeatureStart();
    void onFeatureStop();
}
