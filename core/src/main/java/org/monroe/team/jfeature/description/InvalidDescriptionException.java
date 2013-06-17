package org.monroe.team.jfeature.description;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 11:01 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class InvalidDescriptionException extends Exception {

    public InvalidDescriptionException(Class featureClass, Throwable cause) {
        super("Invalid description detected in "+featureClass.getName(),cause);
    }

}
