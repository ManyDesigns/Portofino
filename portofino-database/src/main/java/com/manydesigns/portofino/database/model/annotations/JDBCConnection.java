package com.manydesigns.portofino.database.model.annotations;

public @interface JDBCConnection {
    String url() default "";
    String driver() default "";
    String username() default "";
    String password() default "";
}
