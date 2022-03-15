package com.manydesigns.portofino.model.database.annotations;

public @interface Table {
    String name() default "";
    String javaClass() default "";
    String idStrategy() default "";
    String shortName() default "";
}
