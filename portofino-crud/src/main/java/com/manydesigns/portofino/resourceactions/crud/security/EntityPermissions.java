package com.manydesigns.portofino.resourceactions.crud.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EntityPermissions {
    String EVERYONE = "*";

    String[] create() default EVERYONE;
    String[] delete() default EVERYONE;
    String[] edit() default EVERYONE;
    String[] read() default EVERYONE;
}
