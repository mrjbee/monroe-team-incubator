package org.monroe.team.smooker.app.uc.common;

import org.monroe.team.smooker.app.common.Registry;

public abstract class UserCaseSupport<RequestType,ResponseType> implements UserCase <RequestType,ResponseType> {

    private final Registry registry;

    public UserCaseSupport(Registry registry) {
        this.registry = registry;
    }

    protected final <ServiceType> ServiceType using(Class<ServiceType> serviceId){
        if (!registry.contains(serviceId)) throw new IllegalStateException("Unexpected service = "+serviceId);
        return registry.get(serviceId);
    }

}
