package org.monroe.team.smooker.app.uc.common;

import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.app.common.Model;

public abstract class UserCaseSupport<RequestType,ResponseType> implements UserCase <RequestType,ResponseType> {

    private final ServiceRegistry serviceRegistry;

    public UserCaseSupport(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    protected final <ServiceType> ServiceType using(Class<ServiceType> serviceId){
        if (!serviceRegistry.contains(serviceId)) throw new IllegalStateException("Unexpected service = "+serviceId);
        return serviceRegistry.get(serviceId);
    }

    protected final Model usingModel(){
        return serviceRegistry.get(Model.class);
    }

}
