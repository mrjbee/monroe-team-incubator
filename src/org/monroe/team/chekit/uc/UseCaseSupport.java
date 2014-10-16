package org.monroe.team.chekit.uc;

import org.monroe.team.chekit.common.Context;

public abstract class UseCaseSupport<Response, Request> implements UseCase<Response, Request>{
    private Context context;

    public void init(Context context){
        this.context = context;
    }

    final<Type> Type using(Class<Type> serviceClass){
        return context.using(serviceClass);
    }
}
