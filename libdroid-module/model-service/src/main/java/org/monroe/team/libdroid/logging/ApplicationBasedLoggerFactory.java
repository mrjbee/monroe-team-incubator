package org.monroe.team.libdroid.logging;

/**
 * User: MisterJBee
 * Date: 8/24/13 Time: 2:17 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class ApplicationBasedLoggerFactory implements LoggerFactory {

    private final String mApplicationName;

    protected ApplicationBasedLoggerFactory(String mApplicationName) {
        this.mApplicationName = mApplicationName;
    }

    @Override
    final public Logger get(String featureName) {
        return new AndroidLogger(mApplicationName+"."+featureName);
    }
}
