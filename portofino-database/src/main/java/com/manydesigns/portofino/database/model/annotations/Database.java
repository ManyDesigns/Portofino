package com.manydesigns.portofino.database.model.annotations;

public @interface Database {
    String entityMode() default "MAP"; // TODO explicitly refer to EntityMode.MAP
}
