package org.monroe.team.jfeature.description;

import java.lang.reflect.Field;
import java.util.List;

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

    public String detailsString() {
        return description.detailsString();
    }

    public void setArray(Object instance, List<Object> injectionInstanceList) throws IllegalAccessException {
        Object[] injections = injectionInstanceList.toArray();
        set(instance,injections);
    }

    public void set(Object featureInstance, Object value) throws IllegalAccessException {
        boolean was = field.isAccessible();
        try{
            field.setAccessible(true);
            field.set(featureInstance, value);
        } finally {
            field.setAccessible(was);
        }
    }
}
