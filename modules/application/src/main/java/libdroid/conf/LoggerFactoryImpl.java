package libdroid.conf;

import org.monroe.team.libdroid.logging.ApplicationBasedLoggerFactory;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 2:49 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class LoggerFactoryImpl extends ApplicationBasedLoggerFactory{

    public LoggerFactoryImpl() {
        super("notif_bridge");
    }
}
