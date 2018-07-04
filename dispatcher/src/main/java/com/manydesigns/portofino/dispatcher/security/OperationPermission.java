package com.manydesigns.portofino.dispatcher.security;

import org.apache.shiro.authz.Permission;

import java.util.List;
import java.util.Map;

/**
 * Created by alessio on 7/21/16.
 */
public class OperationPermission implements Permission {

    protected final Map<String, List<Permission>> allow;
    protected final Map<String, List<Permission>> deny;
    protected final Permission permission;

    public OperationPermission(Map<String, List<Permission>> allow, Map<String, List<Permission>> deny, Permission permission) {
        this.allow = allow;
        this.deny = deny;
        this.permission = permission;
    }

    @Override
    public boolean implies(Permission p) {
        return false;
    }

    public Map<String, List<Permission>> getAllow() {
        return allow;
    }

    public Map<String, List<Permission>> getDeny() {
        return deny;
    }

    public Permission getPermission() {
        return permission;
    }
}
