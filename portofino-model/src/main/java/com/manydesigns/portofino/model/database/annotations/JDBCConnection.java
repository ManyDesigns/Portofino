package com.manydesigns.portofino.model.database.annotations;

public @interface JDBCConnection {
    String url();
    String driver() default "";
    String username() default "";
    String password() default "";
}
