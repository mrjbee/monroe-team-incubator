package org.monroe.team.jfeature.description;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 9:24 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureDescription {

    public final Class featureClass;
    public final Class implClass;
    public final Map<String, String> registrationDetails;
    public final List<FeatureInjection> featureInjectionList;

    public FeatureDescription(Class featureClass, Class implClass, Map<String, String> registrationDetails, List<FeatureInjection> featureInjectionList) {
        this.featureClass = featureClass;
        this.implClass = implClass;
        this.registrationDetails = Collections.unmodifiableMap(registrationDetails);
        this.featureInjectionList = Collections.unmodifiableList(featureInjectionList);
    }
}
