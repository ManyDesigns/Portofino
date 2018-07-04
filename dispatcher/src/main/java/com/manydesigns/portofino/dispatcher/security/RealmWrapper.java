package com.manydesigns.portofino.dispatcher.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * Created by alessio on 26/07/16.
 */
public class RealmWrapper extends AuthorizingRealm {
    
    protected static final Logger logger = LoggerFactory.getLogger(RealmWrapper.class);
    protected AuthorizingRealm realm;
    protected Method getAuthorizationInfoMethod;
    
    public RealmWrapper() {}
    
    public RealmWrapper(AuthorizingRealm realm) {
        setRealm(realm);
    }
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        try {
            AuthorizationInfo info = (AuthorizationInfo) getAuthorizationInfoMethod.invoke(realm, principals);
            if(info.getObjectPermissions() != null) {
                for(Permission p : info.getObjectPermissions()) {
                    if(p instanceof RolesPermission) {
                        return info;
                    }
                }
                info.getObjectPermissions().add(new RolesPermission(info.getRoles()));
            } else if(info instanceof SimpleAuthorizationInfo) {
                ((SimpleAuthorizationInfo) info).setObjectPermissions(
                        Collections.singleton((Permission) new RolesPermission(info.getRoles())));
            } else if(info instanceof SimpleAccount) {
                ((SimpleAccount) info).setObjectPermissions(
                        Collections.singleton((Permission) new RolesPermission(info.getRoles())));
            } else {
                logger.warn("Cannot add RolesPermission to the AuthorizationInfo {}", info);
            }
            return info;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return realm.getAuthenticationInfo(token);
    }

    public AuthorizingRealm getRealm() {
        return realm;
    }

    public void setRealm(AuthorizingRealm realm) {
        Class<? extends AuthorizingRealm> realmClass = realm.getClass();
        getAuthorizationInfoMethod = lookupGetAuthorizationInfoMethod(realmClass);
        this.realm = realm;
    }

    protected Method lookupGetAuthorizationInfoMethod(Class<?> realmClass) {
        if(!AuthorizingRealm.class.isAssignableFrom(realmClass)) {
            throw new RuntimeException("Not an AuthorizingRealm!");
        }
        try {
            Method getAuthorizationInfo = realmClass.getDeclaredMethod("getAuthorizationInfo", PrincipalCollection.class);
            getAuthorizationInfo.setAccessible(true);
            return getAuthorizationInfo;
        } catch (NoSuchMethodException e) {
            return lookupGetAuthorizationInfoMethod(realmClass.getSuperclass());
        }
    }
}
