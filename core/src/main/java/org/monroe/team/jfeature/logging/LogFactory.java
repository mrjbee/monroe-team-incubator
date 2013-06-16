package org.monroe.team.jfeature.logging;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 11:44 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface LogFactory {
    public Log forFeature(String featureName);
}
