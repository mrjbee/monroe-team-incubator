package org.monroe.team.jfeature.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.monroe.team.jfeature.ServiceFeature;

/**
 * User: MisterJBee
 * Date: 6/22/13 Time: 7:28 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class AbstractGuiceFeature<FeatureImplType> extends AbstractModule implements ServiceFeature{

    private FeatureImplType featureImpl = null;
    private FeatureLifeCycleManager featureLifeCycleManager = null;

    protected abstract Class<FeatureImplType> featureImplClass();

    protected synchronized FeatureImplType getImpl(){
        if (featureImpl == null) throw new IllegalStateException("Invalid usage");
        return featureImpl;
    }

    private synchronized void setFeatureImpl(FeatureImplType featureImpl) {
        this.featureImpl = featureImpl;
    }

    @Override
    public void onUp() {
        try {
            Injector injector = Guice.createInjector(this);
            setFeatureImpl(injector.getInstance(featureImplClass()));
            featureLifeCycleManager = injector.getInstance(FeatureLifeCycleManager.class);
        } catch (Exception e) {
            throw new RuntimeException("Issue with creating feature entry for feature = "+ this.getClass());
        }
        featureLifeCycleManager.onFeatureStart();
    }

    @Override
    public void onDown() {
        setFeatureImpl(null);
        featureLifeCycleManager.onFeatureStop();
    }

    @Override
    protected void configure() {
        bind(FeatureLifeCycleManager.class);
        configureFeature();
    }

    protected abstract void configureFeature();
}
