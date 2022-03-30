package com.manydesigns.portofino.database.model.annotations;

public @interface Table {
    String name() default "";
    String javaClass() default "";
    String idStrategy() default "";
    String shortName() default "";
}
