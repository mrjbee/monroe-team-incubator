package org.monroe.team.jfeature.description;

import java.lang.reflect.Field;

/**
 * User: MisterJBee
 * Date: 6/17/13 Time: 9:33 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class FeatureInjection {

    public final Field field;
    public final FeatureInjectionDescription description;

    public FeatureInjection(Field field, FeatureInjectionDescription description) {
        this.field = field;
        this.description = description;
    }
}
