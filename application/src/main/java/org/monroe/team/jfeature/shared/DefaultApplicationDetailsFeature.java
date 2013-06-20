package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.ServiceFeature;
import org.monroe.team.jfeature.shared.api.ApplicationDetailsFeature;
import org.monroe.team.jfeature.shared.api.LoggingFeature;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: MisterJBee
 * Date: 6/20/13 Time: 11:30 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Feature(impl = ApplicationDetailsFeature.class)
public class DefaultApplicationDetailsFeature implements ApplicationDetailsFeature, ServiceFeature {

    @FeatureInject
    LoggingFeature loggingFeature;

    public Properties properties;


    @Override
    public String getAppId() {
        return properties.getProperty("app.id");
    }

    @Override
    public void onUp() {
        InputStream stream = getClass().getResourceAsStream("/application.properties");
        properties = new Properties();
        try {
            properties.load(stream);
        } catch (Exception e) {
            throw new RuntimeException("No application.properties was found", e);
        }
        loggingFeature.get("ApplicationDetails").i("Application properties files loaded. Application id = {0}",getAppId());
    }

    @Override
    public void onDown() {}
}
