package org.monroe.team.aas.ui.common.logging;

/**
 * User: MisterJBee
 * Date: 6/26/13 Time: 12:42 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface Logger {
    public void v(String message, Object ... args);
    public void d(String message, Object ... args);
    public void i(String message, Object ... args);
    public void w(Exception e, String message, Object ... args);
    public void e(Exception e, String message, Object ... args);
    public Logger extend(String extendTag);
}
