package org.monroe.team.jfeature.utils;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 11:38 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class Command <ResultType,ArgType> {
    public abstract ResultType call(ArgType arg) throws Exception;
    public ResultType relaxCall(ArgType arg){
        try {
            return call(arg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };
}
