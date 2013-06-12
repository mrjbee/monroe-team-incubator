package org.monroe.team.bonee.config.api;

/**
 * User: MisterJBee
 * Date: 6/12/13 Time: 1:15 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ConfigNotFoundException extends Exception{

    public final ConfigUri configUri;

    public ConfigNotFoundException(ConfigUri configUri) {
        super(configUri.asPlaintString());
        this.configUri = configUri;
    }
}
