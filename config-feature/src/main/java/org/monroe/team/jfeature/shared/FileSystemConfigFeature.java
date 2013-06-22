package org.monroe.team.jfeature.shared;

import com.google.inject.TypeLiteral;
import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.guice.AbstractGuiceFeature;
import org.monroe.team.jfeature.guice.FeatureLifeCycleObserver;
import org.monroe.team.jfeature.shared.api.ApplicationDetailsFeature;
import org.monroe.team.jfeature.shared.api.ConfigFeature;
import org.monroe.team.jfeature.shared.api.LoggingFeature;
import org.monroe.team.jfeature.shared.config.fs.FileSystemConfigFeatureLifeCycleObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/20/13 Time: 12:35 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Feature(impl = ConfigFeature.class)
public class FileSystemConfigFeature extends AbstractGuiceFeature<String> implements ConfigFeature {

    @FeatureInject
    LoggingFeature loggingFeature;

    @FeatureInject
    ApplicationDetailsFeature applicationDetailsFeature;

    @Override
    public Class getProperty(String name) {
        loggingFeature.get("sda").e(new RuntimeException("Test exception:"+applicationDetailsFeature.getAppId()+":"+getImpl()),"");
        return null;
    }

    @Override
    protected Class featureImplClass() {
        return String.class;
    }

    @Override
    protected void configureFeature() {
        bind(String.class).toInstance("FeatureConfig");
    }

    @Override
    protected Class<? extends FeatureLifeCycleObserver> featureLifeCycleObserverClass() {
        return FileSystemConfigFeatureLifeCycleObserver.class;
    }
}
