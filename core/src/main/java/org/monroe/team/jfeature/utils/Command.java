package org.monroe.team.jfeature.utils;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 11:38 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface Command <ResultType,ArgType> {
    public ResultType call(ArgType arg) throws Exception;
}
