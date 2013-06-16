package org.monroe.team.jfeature.logging;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 11:41 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface Log {
    public void v(String msgPatter, Object... args);
    public void d(String msgPatter, Object... args);
    public void i(String msgPatter, Object... args);
    public void w(String msgPatter, Object... args);
    public void e(String msgPatter, Object... args);
    public void w(Exception exception, String msgPatter, Object... args);
    public void e(Exception exception, String msgPatter, Object... args);
}
