package com.manydesigns.portofino.dispatcher.security;

import org.apache.shiro.authz.Permission;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alessio on 7/20/16.
 */
public class ResourcePermissions {

    protected final SecureResource resource;
    protected final Map<String, List<Permission>> allow;
    protected final Map<String, List<Permission>> deny;

    public ResourcePermissions(
            SecureResource resource, Map<String, List<Permission>> allow, Map<String, List<Permission>> deny) {
        this.resource = resource;
        this.allow = new ConcurrentHashMap<>();
        this.deny = new ConcurrentHashMap<>();
        if(resource.getParent() instanceof SecureResource) {
            this.allow.putAll(((SecureResource) resource.getParent()).getPermissions().getAllow());
            this.deny.putAll(((SecureResource) resource.getParent()).getPermissions().getDeny());
        }
        if(allow != null) {
            this.allow.putAll(allow);
        }
        if(deny != null) {
            this.deny.putAll(deny);
        }
    }

    public OperationPermission getPermission(Permission permission) {
        return new OperationPermission(allow, deny, permission);
    }

    public SecureResource getResource() {
        return resource;
    }

    public Map<String, List<Permission>> getAllow() {
        return allow;
    }

    public Map<String, List<Permission>> getDeny() {
        return deny;
    }

}
