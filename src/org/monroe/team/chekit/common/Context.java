package org.monroe.team.chekit.common;

import org.monroe.team.chekit.services.BackgroundTaskManager;
import org.monroe.team.chekit.services.SuitesCache;
import org.monroe.team.chekit.services.TextRecognitionCore;
import org.monroe.team.chekit.services.UseCaseManager;
import org.monroe.team.chekit.ui.controller.GlobalController;
import org.monroe.team.chekit.ui.controller.TextRecognitionManager;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private static Context instance;
    private final Map<Class,Object> services = new HashMap<>();

    public static Context get(){
        if (instance == null){
            instance = new Context();
            instance.init();
        }
        return instance;
    }

    private void init() {
        //TODO: init services
        services.put(UseCaseManager.class, new UseCaseManager());
        services.put(BackgroundTaskManager.class, new BackgroundTaskManager());
        services.put(TextRecognitionCore.class, new TextRecognitionCore());
        services.put(GlobalController.class, new GlobalController());
        services.put(SuitesCache.class, new SuitesCache());
        services.put(TextRecognitionManager.class,new TextRecognitionManager(using(TextRecognitionCore.class)));
    }

    public<Type> Type using(Class<Type> serviceType){
        return (Type) services.get(serviceType);
    }



}
