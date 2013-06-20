package org.monroe.team.jfeature.shared.api;

import org.monroe.team.jfeature.utils.Command;

import java.util.Map;
import java.util.Properties;

/**
 * User: MisterJBee
 * Date: 6/21/13 Time: 12:11 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface ApplicationResourceLoaderFeature {

    Resources load(String id) throws ResourceNotFoundException;

    public static interface Resources {
         public <Type> Type get(String resourceId, Class<Type> resultClass, Type defaultValue);
         public <Type> Type get(String resourceId, Command<Type,String> convertCommand);
    }

    public static class ResourceNotFoundException extends Exception {

        public ResourceNotFoundException(String message) {
            super(message);
        }

        public ResourceNotFoundException(String message,  Throwable cause) {
            super(message, cause);
        }
    }
}
