package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.shared.api.ApplicationResourceLoaderFeature;
import org.monroe.team.jfeature.utils.Command;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User: MisterJBee
 * Date: 6/21/13 Time: 12:12 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Feature(impl = ApplicationResourceLoaderFeature.class)
public class DefaultApplicationResourceLoaderFeature implements ApplicationResourceLoaderFeature {

    private final static Map<String,Resources> loadedResourcesCache = new HashMap<String, Resources>();

    @Override
    public synchronized Resources load(String id) throws ResourceNotFoundException {
        Resources answer = loadedResourcesCache.get(id);
        if (answer != null) return answer;

        InputStream stream = getClass().getResourceAsStream("/"+id+".properties");
        Properties props = new Properties();
        try {
            props.load(stream);
            answer = new DefaultResources(props);
            loadedResourcesCache.put(id, answer);
            return answer;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Fail to load resource = "+id, e);
        }
    }

    private static final class DefaultResources implements Resources{

        private final Properties properties;

        private DefaultResources(Properties properties) {
            this.properties = properties;
        }


        @Override
        public <Type> Type get(String resourceId, Class<Type> resultClass, Type defaultValue) {
            String prop = properties.getProperty(resourceId);
            if (prop == null) return defaultValue;
            if (resultClass != String.class) {
                throw new UnsupportedOperationException("Unsupported result class "+ resultClass);
            }
            return (Type) prop;
        }


        @Override
        public <Type> Type get(String resourceId, Command<Type, String> convertCommand) {
            String prop = properties.getProperty(resourceId);
            try {
                return convertCommand.call(prop);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
