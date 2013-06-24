package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.config.fs.FileSystemConfigManager;
import org.monroe.team.jfeature.guice.AbstractGuiceFeature;
import org.monroe.team.jfeature.guice.FeatureLifeCycleObserver;
import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.shared.api.ApplicationDetailsFeature;
import org.monroe.team.jfeature.shared.api.ConfigFeature;
import org.monroe.team.jfeature.shared.api.LoggingFeature;
import org.monroe.team.jfeature.config.fs.FileSystemConfigFeatureLifeCycleObserver;

import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/20/13 Time: 12:35 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Feature(impl = ConfigFeature.class)
public class FileSystemConfigFeature extends AbstractGuiceFeature<FileSystemConfigManager> implements ConfigFeature {

    @FeatureInject
    LoggingFeature loggingFeature;

    @FeatureInject
    ApplicationDetailsFeature applicationDetailsFeature;

    @Override
    protected Class featureImplClass() {
        return FileSystemConfigManager.class;
    }

    @Override
    protected Class<? extends FeatureLifeCycleObserver> featureLifeCycleObserverClass() {
        return FileSystemConfigFeatureLifeCycleObserver.class;
    }

    @Override
    public <Type> Type getValue(String uri, Class<Type> answerType) {
        return getImpl().getValue(uri, answerType);
    }

    @Override
    public void setValue(String uri, Object value) {
        getImpl().setValue(uri,value);
    }

    @Override
    public List<String> discoverUri(String uriBase) {
        return getImpl().discoverUri(uriBase);
    }


    @Override
    protected void configureFeature() {
        bind(Log.class).toInstance(loggingFeature.get("FSCONFIG"));
    }
}
