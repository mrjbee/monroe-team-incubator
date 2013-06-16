package org.monroe.team.jfeature.shared.api;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 10:43 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface LauncherFeature {
    public void shutdown();
    public void shutdown(int statusCode);
}
