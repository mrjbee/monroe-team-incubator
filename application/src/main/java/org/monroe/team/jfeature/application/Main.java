package org.monroe.team.jfeature.application;

import org.monroe.team.jfeature.application.logging.LogFactoryLog4Impl;
import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.logging.LogFactory;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 10:41 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Main {

    private static final Object applicationMainThreadWaitObject = new Object();
    private static Application application;
    static Log log;

    public static void main(String[] args) {
        log = getApplication().getLogFactory().forFeature("Application");
        log.i("Starting application...");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                gracefulShutdown();
            }
        });
        try{
            getApplication().start();
            waitUnlessExit();
        } catch (Exception e){
            log.e(e, "Exception during startup application.");
            continueMain(2);
        }
    }

    private static void waitUnlessExit() {
        synchronized (applicationMainThreadWaitObject){
            try {
                log.i("Hold main thread unless application quit");
                applicationMainThreadWaitObject.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static void continueMain(int exitStatus){
       log.i("Going to continue with main thread");
       synchronized (applicationMainThreadWaitObject){
           applicationMainThreadWaitObject.notify();
       }
       System.exit(exitStatus);
    }

    private static void gracefulShutdown() {
        log.i("Graceful shutdown...");
        try{
            getApplication().stop();
        }catch (Exception e){
            log.e(e, "Error during graceful shutdown");
        }
    }

    public static Application getApplication() {
        if (application == null){
            application = new Application(initLogFactory());
        }
        return application;
    }

    private static LogFactory initLogFactory() {
        //TODO: load logging setup
        return new LogFactoryLog4Impl();
    }
}
