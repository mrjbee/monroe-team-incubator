package org.monroe.team.bonee.config.api;

/**
 * User: MisterJBee
 * Date: 6/12/13 Time: 1:18 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ConfigTranslateException extends Exception {

    public final ConfigUri configUri;

    public ConfigTranslateException(ConfigUri configUri) {
        super("Could not translate: "+ configUri.asPlaintString());
        this.configUri = configUri;
    }

    public ConfigTranslateException(ConfigUri configUri, Throwable cause) {
        super("Could not translate: "+ configUri.asPlaintString(), cause);
        this.configUri = configUri;
    }
}
