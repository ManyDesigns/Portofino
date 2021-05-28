package com.manydesigns.portofino.tt;

import com.manydesigns.portofino.persistence.hibernate.multitenancy.SchemaBasedMultiTenancy;

import java.sql.Connection;
import java.sql.SQLException;

public class SampleSchemaBasedMultiTenantImplementation extends SchemaBasedMultiTenancy {

    @Override
    protected void setSchema(Connection connection, String tenant) throws SQLException {
        //This is just an example to test that Hibernate effectively switches schema.
        if(tenant.equals(getDefaultTenant())) {
            connection.createStatement().execute("set search_path to tt;");
        } else {
            connection.createStatement().execute("set search_path to tt2;");
        }
    }

    @Override
    public String getTenant() {
        return null;
    }
}
