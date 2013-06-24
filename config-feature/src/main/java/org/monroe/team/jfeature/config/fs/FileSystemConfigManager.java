package org.monroe.team.jfeature.config.fs;

import com.google.inject.Inject;
import org.monroe.team.jfeature.shared.api.ConfigFeature;
import org.monroe.team.jfeature.utils.Command;

import java.util.*;

/**
 * User: MisterJBee
 * Date: 6/22/13 Time: 11:39 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FileSystemConfigManager implements ConfigFeature {

    ConfigUriParser configUriParser;
    PropertiesProvider propertiesProvider;
    ConfigValueAdapter valueAdapter;

    @Override
    public <Type> Type getValue(String uri, Class<Type> answerType){
        ConfigUri configUri = configUriParser.parse(uri);
        if (!configUri.isPropertyDefined()) throw new RuntimeException("URI should define property path");
        Properties properties =  propertiesProvider.get(configUri);
        String property = properties.getProperty(configUri.propertyId);
        if (property == null && answerType != Map.class){
            return null;
        }
        if (property == null && answerType == Map.class){
            throw new UnsupportedOperationException("Map fetching not supported");
        }
        return valueAdapter.convertToObject(property, answerType);
    }

    @Override
    public void setValue(String uri, Object value) {
        ConfigUri configUri = configUriParser.parse(uri);
        if (!configUri.isPropertyDefined()) throw new RuntimeException("URI should define property path");
        Properties properties =  propertiesProvider.get(configUri);
        if (value == null){
            properties.remove(configUri.propertyId);
        }  else {
            properties.setProperty(configUri.propertyId, valueAdapter.convertToString(value));
        }
        propertiesProvider.fetch(properties);
    }

    @Override
    public List<String> discoverUri(String uriBase) {
        if (uriBase == null){
            return propertiesProvider.getAllAvailableProperties();
        }
        ConfigUri configUri = configUriParser.parse(uriBase);
        Properties properties = propertiesProvider.get(configUri);
        return collectKeys(properties, new Command<Boolean, String>(){

            @Override
            public Boolean call(String arg) throws Exception {
                return false;
            }
        });
    }

    private List<String> collectKeys(Properties properties, Command<Boolean, String> command) {
        List<String> answer = new ArrayList<String>(5);
        Enumeration keys = properties.propertyNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (command.relaxCall(key)){
                answer.add(key);
            }
        }
        return answer;
    }
}
