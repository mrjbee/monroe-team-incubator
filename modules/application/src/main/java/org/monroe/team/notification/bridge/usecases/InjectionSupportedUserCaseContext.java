package org.monroe.team.notification.bridge.usecases;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InjectionSupportedUserCaseContext extends UserCaseContext {

    @Override
    protected void doBeforeStart(Map<Class<?>, Object> registrationStrategiesMap, Map<Class<?>, Object> registrationBoundariesMap) {
        Set<Object> notifiedBeansSet = new HashSet<Object>();
        for (Map.Entry<Class<?>, Object> strategyEntry : registrationStrategiesMap.entrySet()) {
            if (strategyEntry.getValue() instanceof InjectionAwareStrategy && !notifiedBeansSet.contains(strategyEntry.getValue())){
                ((InjectionAwareStrategy) strategyEntry.getValue()).injectUsing(this);
                notifiedBeansSet.add(strategyEntry.getValue());
            }
        }
    }

    public static interface InjectionAwareStrategy {
        public void injectUsing(UserCaseContext context);
    }
}
