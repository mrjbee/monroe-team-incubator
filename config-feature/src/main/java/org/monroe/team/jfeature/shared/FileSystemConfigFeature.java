package org.monroe.team.jfeature.shared;

import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.shared.api.ApplicationDetailsFeature;
import org.monroe.team.jfeature.shared.api.ConfigFeature;
import org.monroe.team.jfeature.shared.api.LoggingFeature;

/**
 * User: MisterJBee
 * Date: 6/20/13 Time: 12:35 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Feature(impl = ConfigFeature.class)
public class FileSystemConfigFeature implements ConfigFeature {
    @FeatureInject
    LoggingFeature loggingFeature;

    @FeatureInject
    ApplicationDetailsFeature applicationDetailsFeature;

    @Override
    public Class getProperty(String name) {
        loggingFeature.get("sda").e(new RuntimeException("Test exception:"+applicationDetailsFeature.getAppId()),"");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
