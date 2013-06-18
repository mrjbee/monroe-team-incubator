package org.monroe.team.jfeature.description;

import org.monroe.team.jfeature.FeatureException;

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

    public String detailsString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n Feature class = "+featureClass.getName());
        builder.append("\n implement = "+implClass.getName());
        builder.append("\n details = "+registrationDetails);
        builder.append("\n requires [ ");
        for (FeatureInjection featureInjection : featureInjectionList) {
            builder.append(featureInjection.detailsString());
        }

        return builder.toString();
    }

    public FeatureException issue(Exception e) {
        return new FeatureException(featureClass, implClass, e);
    }
}
