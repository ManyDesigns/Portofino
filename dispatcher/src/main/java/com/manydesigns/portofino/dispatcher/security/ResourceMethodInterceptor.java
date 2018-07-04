package com.manydesigns.portofino.dispatcher.security;

import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.aop.PermissionAnnotationMethodInterceptor;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Created by alessio on 7/21/16.
 */
public class ResourceMethodInterceptor extends PermissionAnnotationMethodInterceptor {
    
    protected static final Logger logger = LoggerFactory.getLogger(PermissionAnnotationMethodInterceptor.class);

    @Override
    public void assertAuthorized(MethodInvocation mi) throws AuthorizationException {
        if(mi.getThis() instanceof SecureResource) {
            RequiresPermissions annotation = (RequiresPermissions) getAnnotation(mi);
            String[] value = annotation.value();
            boolean allowed = annotation.logical() == Logical.AND || value.length == 0;
            ResourcePermissions resourcePermissions = ((SecureResource) mi.getThis()).getPermissions();
            for(String permission : value) {
                OperationPermission op =
                    resourcePermissions.getPermission(new WildcardPermission(permission));
                if(annotation.logical() == Logical.AND) {
                    if(!isPermitted(op)) {
                        notPermitted(mi);
                    }
                } else {
                    allowed |= isPermitted(op);
                }
            }
            if(!allowed) {
                notPermitted(mi);
            }
        } else {
            super.assertAuthorized(mi);
        }
    }

    protected void notPermitted(MethodInvocation mi) {
        //Subject might have direct explicit permissions instead of, or in addition to, RolesPermission
        logger.debug("Subject not authorized according to resource and role permissions. Checking default permissions.");
        super.assertAuthorized(mi);
    }

    protected boolean isPermitted(OperationPermission op) {
        if(getSubject().isAuthenticated()) {
            return getSubject().isPermitted(op);
        } else {
            return new RolesPermission(Collections.singleton("")).implies(op);
        }
    }
}
