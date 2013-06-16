package org.monroe.team.jfeature.application.logging;

import org.apache.log4j.Logger;
import org.monroe.team.jfeature.logging.Log;
import org.monroe.team.jfeature.logging.LogFactory;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 12:46 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class LogFactoryLog4Impl implements LogFactory{
    @Override
    public Log forFeature(String featureName) {
        return new Log4jImpl(Logger.getLogger("feature."+featureName));
    }
}
