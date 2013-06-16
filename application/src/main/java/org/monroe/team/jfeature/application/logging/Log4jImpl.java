package org.monroe.team.jfeature.application.logging;

import org.apache.log4j.Logger;
import org.monroe.team.jfeature.logging.Log;

import java.text.MessageFormat;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 12:40 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Log4jImpl implements Log{

    private final Logger logger;

    public Log4jImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void v(String msgPatter, Object... args) {
        if (logger.isTraceEnabled()){
            String msg = buildMessage(msgPatter,args);
            logger.trace(msg);
        }
    }

    @Override
    public void d(String msgPatter, Object... args) {
        if (logger.isDebugEnabled()){
            String msg = buildMessage(msgPatter,args);
            logger.debug(msg);
        }
    }

    @Override
    public void i(String msgPatter, Object... args) {
        if (logger.isInfoEnabled()){
            String msg = buildMessage(msgPatter, args);
            logger.info(msg);
        }
    }

    @Override
    public void w(String msgPatter, Object... args) {
        String msg = buildMessage(msgPatter, args);
        logger.warn(msg);
    }

    @Override
    public void e(String msgPatter, Object... args) {
        String msg = buildMessage(msgPatter, args);
        logger.error(msg);
    }

    @Override
    public void w(Exception exception, String msgPatter, Object... args) {
        String msg = buildMessage(msgPatter, args);
        logger.warn(msg, exception);
    }

    @Override
    public void e(Exception exception, String msgPatter, Object... args) {
        String msg = buildMessage(msgPatter, args);
        logger.error(msg, exception);
    }

    private String buildMessage(String msgPatter, Object[] args) {
        return MessageFormat.format(msgPatter,args);
    }

}
