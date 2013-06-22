package org.monroe.team.jfeature.shared.config.fs;

import com.google.inject.Inject;
import org.monroe.team.jfeature.guice.FeatureLifeCycleObserver;
import org.monroe.team.jfeature.logging.Log;

import javax.inject.Named;

/**
 * User: MisterJBee
 * Date: 6/22/13 Time: 9:52 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FileSystemConfigFeatureLifeCycleObserver implements FeatureLifeCycleObserver {

    @Inject Log log;

    @Override
    public void onFeatureStart() {
        log.i("onFeatureStart");
    }

    @Override
    public void onFeatureStop() {
        log.i("onFeatureStop");
    }
}
