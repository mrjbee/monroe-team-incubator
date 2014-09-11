package org.monroe.team.smooker.app.common;

import java.util.HashMap;
import java.util.Map;

public class Registry {

    private final Map<Class, Object> model = new HashMap<Class, Object>();

    final public <ServiceType> boolean contains(Class<ServiceType> serviceId) {
        return model.containsKey(serviceId);
    }

    final public <ServiceType> ServiceType get(Class<ServiceType> serviceId) {
        return (ServiceType) model.get(serviceId);
    }

    final public <ServiceType> void registrate(Class<ServiceType> serviceId, ServiceType service){
        model.put(serviceId, service);
    }

}
