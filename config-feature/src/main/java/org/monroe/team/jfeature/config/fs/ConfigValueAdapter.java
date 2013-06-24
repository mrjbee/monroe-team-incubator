package org.monroe.team.jfeature.config.fs;

/**
 * User: MisterJBee
 * Date: 6/23/13 Time: 4:54 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ConfigValueAdapter {

    public <Type> Type convertToObject(String configStringValue, Class<Type> requiredType) {
        if (requiredType == String.class){
            return (Type) configStringValue;
        }
        if (requiredType == Boolean.class){
            return (Type)(Boolean)Boolean.parseBoolean(configStringValue);
        }
        if (requiredType == Integer.class){
            return (Type)(Integer)Integer.parseInt(configStringValue);
        }
        if (requiredType == Float.class){
            return (Type)(Float)Float.parseFloat(configStringValue);
        }
        throw new IllegalArgumentException("Unsupported requested type = "+requiredType);
  }

    public String convertToString(Object value) {
        if (value.getClass() != String.class ||
            value.getClass() != Boolean.class ||
            value.getClass() != Integer.class ||
            value.getClass() != Float.class){
            throw new IllegalArgumentException("Unsupported requested type = "+value.getClass());
        }
        return String.valueOf(value);
    }
}
