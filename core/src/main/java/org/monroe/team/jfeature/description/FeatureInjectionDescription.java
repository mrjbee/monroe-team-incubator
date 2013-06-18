package org.monroe.team.jfeature.description;

import java.util.Collections;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 9:26 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureInjectionDescription {

    public final Class dependencyClass;
    public final boolean isMultiple;
    public final List<FeatureInjectionCondition> conditionListFeature;

    public FeatureInjectionDescription(Class dependencyClass, boolean multiple, List<FeatureInjectionCondition> conditionListFeature) {
        this.dependencyClass = dependencyClass;
        isMultiple = multiple;
        this.conditionListFeature = Collections.unmodifiableList(conditionListFeature);
    }

    public String detailsString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n interface = "+dependencyClass.getName());
        builder.append("\n isMultiple = "+isMultiple);
        return builder.toString();
    }
}
