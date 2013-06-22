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
    private FeatureLifeCycleObserver featureLifeCycleObserver = null;

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
            featureLifeCycleObserver = injector.getInstance(FeatureLifeCycleObserver.class);
        } catch (Exception e) {
            throw new RuntimeException("Issue with creating implementation for = "+ this.getClass(),e);
        }
        featureLifeCycleObserver.onFeatureStart();
    }

    @Override
    public void onDown() {
        setFeatureImpl(null);
        featureLifeCycleObserver.onFeatureStop();
    }

    @Override
    protected void configure() {
        bind(FeatureLifeCycleObserver.class).to(featureLifeCycleObserverClass());
        configureFeature();
    }

    protected Class<? extends FeatureLifeCycleObserver> featureLifeCycleObserverClass() {
       return NoOpFeatureLifeCycleObserver.class;
    }

    protected abstract void configureFeature();
}
