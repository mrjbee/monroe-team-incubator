package org.monroe.team.jfeature;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * User: MisterJBee
 * Date: 6/16/13 Time: 11:55 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RUNTIME)
@Documented
public @interface FeatureInject {
    String[] value() default {};
}
