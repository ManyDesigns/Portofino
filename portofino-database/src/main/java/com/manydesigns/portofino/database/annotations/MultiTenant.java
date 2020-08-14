package com.manydesigns.portofino.database.annotations;

import org.hibernate.MultiTenancyStrategy;

public @interface MultiTenant {

    MultiTenancyStrategy value() default MultiTenancyStrategy.SCHEMA;

}
