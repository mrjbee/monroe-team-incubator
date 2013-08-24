package org.monroe.team.libdroid.logging;

/**
 * User: MisterJBee
 * Date: 8/24/13 Time: 2:19 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class LoggerSetup {

    private static LoggerFactory sLoggerFactory;
    private LoggerSetup() {}

    public static synchronized Logger createLogger(String featureName){
       return getLoggerFactory().get(featureName);
    }

    public static LoggerFactory getLoggerFactory() {
        if (sLoggerFactory == null){
            Class aClass = null;
            try {
                aClass = Class.forName("libdroid.conf.LoggerFactoryImpl");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("No LogFactory implementation found", e);
            }
            try {
                sLoggerFactory = (LoggerFactory) aClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("LogFactory implementation could not be used", e);
            }
        }
        return sLoggerFactory;
    }
}
