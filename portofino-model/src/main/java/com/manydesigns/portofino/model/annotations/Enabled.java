package com.manydesigns.portofino.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controls whether a model object is "enabled" or not. If it's not enabled, the application should ignore it as if it
 * wasn't there, but it should keep it in the model.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface Enabled {
    boolean value() default true;
}
