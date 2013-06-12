package org.monroe.team.bonee.config;

import org.monroe.team.bonee.config.api.ConfigComponent;
import org.monroe.team.bonee.config.api.ConfigNotFoundException;
import org.monroe.team.bonee.config.api.ConfigUri;

import java.util.HashMap;
import java.util.Map;

/**
 * User: MisterJBee
 * Date: 6/12/13 Time: 12:24 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ConfigComponentImpl implements ConfigComponent {

    private Map<String,String> inCacheProperty = new HashMap<String, String>();

    @Override
    public boolean isConfigExist(ConfigUri configUri) {
        return inCacheProperty.containsKey(configUri.asPlaintString());
    }

    @Override
    public String getConfigValue(ConfigUri configUri) throws ConfigNotFoundException{
        String answer = inCacheProperty.get(configUri.asPlaintString());
        if (answer == null) throw new ConfigNotFoundException(configUri);
        return answer;
    }

    @Override
    public <Type> Type getConfigValue(ConfigUri configUri, Class<Type> asType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String setConfigValue(ConfigUri configUri, String value) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <Type> Type setConfigValue(ConfigUri configUri, Type newValue, Class<Type> asType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
