package com.manydesigns.portofino.dispatcher.security;

import org.apache.shiro.authz.Permission;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alessio on 7/21/16.
 */
public class RolesPermission implements Permission {

    protected final Collection<String> roles;

    public RolesPermission(Collection<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean implies(Permission p) {
        if(p instanceof OperationPermission) {
            boolean allowed = false;
            for(String role : roles) {
                List<Permission> permissions = getPermissions(role, ((OperationPermission) p).getAllow());
                if(permissions != null) {
                    for(Permission permission : permissions) {
                        allowed |= permission.implies(((OperationPermission) p).getPermission());
                        if(allowed) {
                            break;
                        }
                    }
                }
                permissions = getPermissions(role, ((OperationPermission) p).getDeny());
                if(permissions != null) {
                    for(Permission permission : permissions) {
                        if(permission.implies(((OperationPermission) p).getPermission())) {
                            return false;
                        }
                    }
                }
            }
            return allowed;
        }
        return false;
    }

    protected List<Permission> getPermissions(String role, Map<String, List<Permission>> permissionMap) {
        List<Permission> permissions = permissionMap.get(role);
        if(permissions == null) {
            permissions = permissionMap.get("*");
        }
        return permissions;
    }
}
