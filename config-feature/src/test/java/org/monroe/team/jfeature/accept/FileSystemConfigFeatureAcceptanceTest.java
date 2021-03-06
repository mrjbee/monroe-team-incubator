package org.monroe.team.jfeature.accept;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.monroe.team.jfeature.Feature;
import org.monroe.team.jfeature.FeatureInject;
import org.monroe.team.jfeature.shared.FileSystemConfigFeature;
import org.monroe.team.jfeature.shared.api.ApplicationDetailsFeature;
import org.monroe.team.jfeature.shared.api.ConfigFeature;
import org.monroe.team.jfeature.shared.api.LoggingFeature;
import org.monroe.team.jfeature.test.support.FeatureTSupport;

/**
 * User: MisterJBee
 * Date: 6/20/13 Time: 1:32 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FileSystemConfigFeatureAcceptanceTest extends FeatureTSupport  {

    ConfigFeature testFeature;

    @Mock
    ApplicationDetailsFeature applicationDetailsFeature;

    @Test
    public void should(){
        Mockito.when(applicationDetailsFeature.getAppId()).thenReturn("testApp");
        testFeature.getProperty("asd");
        should(testFeature != null);
    }

    @Override
    protected Class getTestedImplClass() {
        return FileSystemConfigFeature.class;
    }
}
