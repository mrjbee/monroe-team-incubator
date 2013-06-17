package org.monroe.team.jfeature.description;

import java.util.regex.Pattern;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 9:29 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureInjectionCondition {

    public static final FeatureInjectionCondition ANY = new FeatureInjectionCondition(null,null);


    public String name;
    public Pattern valuePattern;

    public FeatureInjectionCondition(String name, Pattern valuePattern) {
        this.name = name;
        this.valuePattern = valuePattern;
    }
}
