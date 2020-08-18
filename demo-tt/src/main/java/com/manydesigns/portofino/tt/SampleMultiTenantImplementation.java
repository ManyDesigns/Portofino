package com.manydesigns.portofino.tt;

import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementation;

public class SampleMultiTenantImplementation extends MultiTenancyImplementation {

    @Override
    public String getConnectionURL(String tenant) {
        if(tenant.equals(getDefaultTenant())) {
            return null;
        } else {
            //This is just an example to test that Hibernate effectively switches connection provider.
            return "jdbc:postgresql:tt2";
        }
    }
}
