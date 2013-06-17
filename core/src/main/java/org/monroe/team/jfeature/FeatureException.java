package org.monroe.team.jfeature;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 11:26 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureException extends Exception {
    public FeatureException(Class featureClass, Class featureImpl, Throwable cause) {
        super("Invalid feature "+featureClass.getName()+" = "+featureImpl.getName(), cause);
    }
}
