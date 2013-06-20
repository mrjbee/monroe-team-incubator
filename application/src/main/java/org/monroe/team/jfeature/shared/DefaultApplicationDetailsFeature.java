package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.ServiceFeature;
import org.monroe.team.jfeature.shared.api.ApplicationDetailsFeature;
import org.monroe.team.jfeature.shared.api.ApplicationResourceLoaderFeature;
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

    @FeatureInject LoggingFeature loggingFeature;
    @FeatureInject ApplicationResourceLoaderFeature applicationResourceLoaderFeature;

    ApplicationResourceLoaderFeature.Resources applicationResources;

    @Override
    public String getAppId() {
        return applicationResources.get("app.id", String.class, null);
    }

    @Override
    public void onUp() {
        try {
            applicationResources = applicationResourceLoaderFeature.load("application");
            loggingFeature.get("ApplicationDetails").i("Application properties files loaded. Application id = {0}",getAppId());
        } catch (ApplicationResourceLoaderFeature.ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDown() {}
}
