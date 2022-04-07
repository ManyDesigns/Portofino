package com.manydesigns.portofino.persistence.hibernate.multitenancy;

public enum MultiTenancyStrategy {

    SEPARATE_DATABASE, SEPARATE_SCHEMA, PARTITIONED_DATA;

    public boolean requiresMultiTenantConnectionProvider() {
        return this != PARTITIONED_DATA;
    }

}
