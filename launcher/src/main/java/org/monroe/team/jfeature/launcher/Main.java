package org.monroe.team.jfeature.launcher;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 10:41 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Main {

    private static final Object applicationMainThreadWaitObject = new Object();
    private static ConsoleLog log = new ConsoleLog();
    private static Application application = new Application();

    public static void main(String[] args) {
        log.i("Starting application...");
        Runtime.getRuntime().addShutdownHook(new Thread(){
           @Override
           public void run() {
               gracefulShutdown();
           }
        });
        application.start();
        waitUnlessExit();
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
       synchronized (applicationMainThreadWaitObject){
           log.i("Going to continue with main thread");
           applicationMainThreadWaitObject.notify();
       }
       System.exit(exitStatus);
    }

    private static void gracefulShutdown() {
        log.i("Graceful shutdown...");
        application.stop();
    }

}
