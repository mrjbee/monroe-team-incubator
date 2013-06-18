package org.monroe.team.jfeature.description;

import java.lang.reflect.Array;
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

    public void setArray(Object instance, Class clazz, List<Object> injectionInstanceList) throws IllegalAccessException {
        Object objects = (Object[]) Array.newInstance(clazz, injectionInstanceList.size());
        for (int i = 0; i < injectionInstanceList.size(); i++) {
            Array.set(objects,i,injectionInstanceList.get(0));
        }
        set(instance,objects);
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
