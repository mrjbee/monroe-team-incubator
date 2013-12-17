package org.monroe.team.notification.bridge.usecases.common;

import org.monroe.team.libdroid.commons.Should;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UseCaseContext {

    private Map<Class<?>, Object> mRegistrationBoundariesMap = new HashMap<Class<?>, Object>();
    private Map<Class<?>, Object> mRegistrationStrategiesMap = new HashMap<Class<?>, Object>();
    private boolean started = false;

    public <StrategyType> void installStrategy(StrategyType strategy, Class<StrategyType> asClass){
        Should.beTrue("Not allowed.", !started);
        mRegistrationStrategiesMap.put(asClass, strategy);
    }

    public <BoundaryType> void installBoundary(BoundaryType boundary, Class<BoundaryType> asClass){
        Should.beTrue("Not allowed.", !started);
        mRegistrationBoundariesMap.put(asClass, boundary);
    }

    public <BoundaryType> BoundaryType getBoundary(Class<BoundaryType> boundaryClass){
        return extractEntityFromMap(mRegistrationBoundariesMap,boundaryClass);
    }

    public <StrategyType> StrategyType getStrategy(Class<StrategyType> strategyClass){
       return extractEntityFromMap(mRegistrationStrategiesMap, strategyClass);
    }

    private <EntityType> EntityType extractEntityFromMap(Map<Class<?>,Object> map, Class<EntityType> boundaryClass) {
        Object answer = map.get(boundaryClass);
        Should.beNotNull("Boundary expected for class = " + boundaryClass, answer);
        return (EntityType) answer;
    }

    final synchronized public void startup(){
        started = true;
        doBeforeStart(mRegistrationStrategiesMap, mRegistrationBoundariesMap);
        Set<Object> notifiedBeansSet = new HashSet<Object>();
        for (Map.Entry<Class<?>, Object> strategyEntry : mRegistrationStrategiesMap.entrySet()) {
            if (strategyEntry.getValue() instanceof LifeCycleAware && !notifiedBeansSet.contains(strategyEntry.getValue())){
                ((LifeCycleAware) strategyEntry.getValue()).onStartup();
                notifiedBeansSet.add(strategyEntry.getValue());
            }
        }
        doAfterStart(mRegistrationStrategiesMap, mRegistrationBoundariesMap);
    }

    final synchronized void shutdown(){
        doBeforeShutdown(mRegistrationStrategiesMap, mRegistrationBoundariesMap);
        Set<Object> notifiedBeansSet = new HashSet<Object>();
        for (Map.Entry<Class<?>, Object> strategyEntry : mRegistrationStrategiesMap.entrySet()) {
            if (strategyEntry.getValue() instanceof LifeCycleAware && !notifiedBeansSet.contains(strategyEntry.getValue())){
                ((LifeCycleAware) strategyEntry.getValue()).onShutdown();
                notifiedBeansSet.add(strategyEntry.getValue());
            }
        }
        doAfterShutdown(mRegistrationStrategiesMap, mRegistrationBoundariesMap);
        mRegistrationBoundariesMap.clear();
        mRegistrationBoundariesMap.clear();
        started = false;
    }


    protected void doAfterShutdown(Map<Class<?>, Object> registrationStrategiesMap, Map<Class<?>, Object> registrationBoundariesMap) {}
    protected void doBeforeShutdown(Map<Class<?>, Object> registrationStrategiesMap, Map<Class<?>, Object> registrationBoundariesMap) {}
    protected void doAfterStart(Map<Class<?>, Object> registrationStrategiesMap, Map<Class<?>, Object> registrationBoundariesMap) {};
    protected void doBeforeStart(Map<Class<?>, Object> registrationStrategiesMap, Map<Class<?>, Object> registrationBoundariesMap){};


    public static interface LifeCycleAware {
        public void onStartup();
        public void onShutdown();
    }

}
