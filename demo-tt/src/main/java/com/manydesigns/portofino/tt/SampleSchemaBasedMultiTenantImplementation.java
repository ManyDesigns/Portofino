package com.manydesigns.portofino.tt;

import com.manydesigns.portofino.persistence.hibernate.multitenancy.SchemaBasedMultiTenancy;
import com.manydesigns.portofino.shiro.PortofinoRealm;
import com.manydesigns.portofino.shiro.ShiroUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SampleSchemaBasedMultiTenantImplementation extends SchemaBasedMultiTenancy {

    @Override
    protected void setSchema(Connection connection, String tenant) throws SQLException {
        //This is just an example to test that Hibernate effectively switches schema.
        try(Statement stmt = connection.createStatement()) {
            if (tenant.equals(getDefaultTenant())) {
                stmt.execute("set search_path to tt;");
            } else {
                stmt.execute("set search_path to tt2;");
            }
        }
    }

    @Override
    public String getTenant() {
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isAuthenticated()) {
            return getDefaultTenant();
        }
        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        return portofinoRealm.getUsername((Serializable) subject.getPrincipal());
    }
}
