package org.monroe.team.chekit.services;

import org.monroe.team.chekit.uc.UseCase;
import org.monroe.team.chekit.common.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class UseCaseManager {

    private final Map<Class, Object> ucInstanceCache = new HashMap<>();

    public <Response,Request, UseCaseType extends UseCase<Response,Request>> Response execute(Class<UseCaseType> useCase, Request request){
        try {
            UseCaseType useCaseInstance = getInstance(useCase);
            return useCaseInstance.perform(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <Response,Request,UseCaseType extends UseCase<Response, Request>> UseCaseType getInstance(Class<UseCaseType> useCase) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (ucInstanceCache.get(useCase) != null) return (UseCaseType) ucInstanceCache.get(useCase);
        UseCaseType useCaseInstance = useCase.newInstance();
        try {
            Method method = useCase.getMethod("init", Context.class);
            if (method!= null){
                method.invoke(useCaseInstance,Context.get());
            }
        }catch (NoSuchMethodException e){}
        ucInstanceCache.put(useCase,useCaseInstance);
        return useCaseInstance;
    }

}
