package org.monroe.team.bonee.config.api;

/**
 * User: MisterJBee
 * Date: 6/12/13 Time: 12:24 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface ConfigComponent {
    boolean isConfigExist(ConfigUri configUri);
    String getConfigValue(ConfigUri configUri) throws ConfigNotFoundException;
    <Type> Type getConfigValue(ConfigUri configUri, Class<Type> asType) throws ConfigNotFoundException,ConfigTranslateException;
    String setConfigValue(ConfigUri configUri, String value);
    <Type> Type setConfigValue(ConfigUri configUri, Type newValue, Class<Type> asType) throws ConfigTranslateException;
}
