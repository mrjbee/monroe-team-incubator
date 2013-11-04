package org.monroe.team.libdroid.commons;

/**
 * User: MisterJBee
 * Date: 11/4/13 Time: 11:59 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface Closure <OutType, InType> {
    public OutType call(InType in);
}
