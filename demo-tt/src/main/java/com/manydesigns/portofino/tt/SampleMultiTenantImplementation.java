package com.manydesigns.portofino.tt;

import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementation;
import com.manydesigns.portofino.shiro.PortofinoRealm;
import com.manydesigns.portofino.shiro.ShiroUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.io.Serializable;

public class SampleMultiTenantImplementation extends MultiTenancyImplementation {

    @Override
    public String getTenant() {
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isAuthenticated()) {
            return getDefaultTenant();
        }
        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        return portofinoRealm.getUsername((Serializable) subject.getPrincipal());
    }

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
