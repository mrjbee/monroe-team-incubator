package org.monroe.team.jfeature.guice;

/**
 * User: MisterJBee
 * Date: 6/22/13 Time: 9:47 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class NoOpFeatureLifeCycleObserver implements FeatureLifeCycleObserver {
    @Override
    public void onFeatureStart() {}

    @Override
    public void onFeatureStop() {}
}
