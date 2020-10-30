/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.shiro;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.code.CodeBase;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;
import org.apache.shiro.util.LifecycleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Realm implementation that delegates to another class, written in Groovy and dynamically reloaded.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SecurityClassRealm implements PortofinoRealm, Initializable, Destroyable {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Logger
    //--------------------------------------------------------------------------

    public static final Logger logger = LoggerFactory.getLogger(SecurityClassRealm.class);

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    protected final CodeBase codeBase;
    protected final String className;
    protected final ApplicationContext applicationContext;
    protected volatile PortofinoRealm security;
    protected volatile boolean destroyed = false;

    protected CacheManager cacheManager;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public SecurityClassRealm(CodeBase codeBase, String className, ApplicationContext applicationContext) {
        this.codeBase = codeBase;
        this.className = className;
        this.applicationContext = applicationContext;
    }

    //--------------------------------------------------------------------------
    // Delegation support
    //--------------------------------------------------------------------------

    public void init() {
        try {
            doEnsureDelegate();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private synchronized PortofinoRealm ensureDelegate() {
        if(destroyed) {
            throw new IllegalStateException("This realm has been destroyed.");
        }
        try {
            return doEnsureDelegate();
        } catch (Exception e) {
            throw new Error("Security.groovy not found or not loadable", e);
        }
    }

    private PortofinoRealm doEnsureDelegate() throws Exception {
        Class<?> scriptClass = codeBase.loadClass(className);
        if(scriptClass.isInstance(security)) { //Class did not change
            return security;
        } else {
            logger.info("Refreshing Portofino Realm Delegate instance (Security.groovy)");
            if(security != null) {
                logger.debug("Script class changed: from " + security.getClass() + " to " + scriptClass);
            }
            Object securityTemp = scriptClass.getConstructor().newInstance();
            if(securityTemp instanceof PortofinoRealm) {
                PortofinoRealm realm = (PortofinoRealm) securityTemp;
                configureDelegate(realm);
                PortofinoRealm oldSecurity = security;
                security = realm;
                LifecycleUtils.destroy(oldSecurity);
                return realm;
            } else {
                 throw new ClassCastException(
                         "Security object is not an instance of " + PortofinoRealm.class + ": " + securityTemp +
                         " (" + securityTemp.getClass().getSuperclass() + " " +
                         Arrays.asList(securityTemp.getClass().getInterfaces()) + ")");
            }
        }
    }

    protected void configureDelegate(PortofinoRealm security) {
        AutowireCapableBeanFactory bf = applicationContext.getAutowireCapableBeanFactory();
        bf.autowireBean(security);
        bf.initializeBean(security, "Security.groovy");
        security.setCacheManager(cacheManager);
        LifecycleUtils.init(security);
    }

    //--------------------------------------------------------------------------
    // PortofinoRealm implementation
    //--------------------------------------------------------------------------
    
    @Override
    public void verifyUser(Serializable user) {
        ensureDelegate().verifyUser(user);
    }

    @Override
    public void changePassword(Serializable user, String oldPassword, String newPassword) {
        ensureDelegate().changePassword(user, oldPassword, newPassword);
    }

    @Override
    public String generateOneTimeToken(Serializable user) {
        return ensureDelegate().generateOneTimeToken(user);
    }

    @Override
    public String encryptPassword(String password) {
        return ensureDelegate().encryptPassword(password);
    }

    @Override
    public Map<Serializable, String> getUsers() {
        return ensureDelegate().getUsers();
    }

    @Override
    public Serializable getUserById(String encodedUserId) {
        return ensureDelegate().getUserById(encodedUserId);
    }

    @Override
    public Serializable getUserByEmail(String email) {
        return ensureDelegate().getUserByEmail(email);
    }

    @Override
    public ClassAccessor getSelfRegisteredUserClassAccessor() {
        return ensureDelegate().getSelfRegisteredUserClassAccessor();
    }

    @Override
    public String[] saveSelfRegisteredUser(Object user) {
        return ensureDelegate().saveSelfRegisteredUser(user);
    }

    @Override
    public String getUserPrettyName(Serializable user) {
        return ensureDelegate().getUserPrettyName(user);
    }

    @Override
    public Serializable getUserId(Serializable user) {
        return ensureDelegate().getUserId(user);
    }

    @Override
    public String getUsername(Serializable user) {
        return ensureDelegate().getUsername(user);
    }

    @Override
    public String getEmail(Serializable user) {
        return ensureDelegate().getEmail(user);
    }

    @Override
    public Set<String> getGroups() {
        return ensureDelegate().getGroups();
    }

    @Override
    public Set<String> getGroups(Object principal) {
        return ensureDelegate().getGroups(principal);
    }

    @Override
    public String generateWebToken(Object principal) {
        return ensureDelegate().generateWebToken(principal);
    }

    //--------------------------------------------------------------------------
    // Realm implementation
    //--------------------------------------------------------------------------

    @Override
    public String getName() {
        return ensureDelegate().getName();
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return ensureDelegate().supports(token);
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        return ensureDelegate().getAuthenticationInfo(token);
    }

    //--------------------------------------------------------------------------
    // Authorizer implementation
    //--------------------------------------------------------------------------

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        return ensureDelegate().isPermitted(principals, permission);
    }

    @Override
    public boolean isPermitted(PrincipalCollection subjectPrincipal, Permission permission) {
        return ensureDelegate().isPermitted(subjectPrincipal, permission);
    }

    @Override
    public boolean[] isPermitted(PrincipalCollection subjectPrincipal, String... permissions) {
        return ensureDelegate().isPermitted(subjectPrincipal, permissions);
    }

    @Override
    public boolean[] isPermitted(PrincipalCollection subjectPrincipal, List<Permission> permissions) {
        return ensureDelegate().isPermitted(subjectPrincipal, permissions);
    }

    @Override
    public boolean isPermittedAll(PrincipalCollection subjectPrincipal, String... permissions) {
        return ensureDelegate().isPermittedAll(subjectPrincipal, permissions);
    }

    @Override
    public boolean isPermittedAll(PrincipalCollection subjectPrincipal, Collection<Permission> permissions) {
        return ensureDelegate().isPermittedAll(subjectPrincipal, permissions);
    }

    @Override
    public void checkPermission(PrincipalCollection subjectPrincipal, String permission) throws AuthorizationException {
        ensureDelegate().checkPermission(subjectPrincipal, permission);
    }

    @Override
    public void checkPermission(PrincipalCollection subjectPrincipal, Permission permission) throws AuthorizationException {
        ensureDelegate().checkPermission(subjectPrincipal, permission);
    }

    @Override
    public void checkPermissions(PrincipalCollection subjectPrincipal, String... permissions) throws AuthorizationException {
        ensureDelegate().checkPermissions(subjectPrincipal, permissions);
    }

    @Override
    public void checkPermissions(PrincipalCollection subjectPrincipal, Collection<Permission> permissions) throws AuthorizationException {
        ensureDelegate().checkPermissions(subjectPrincipal, permissions);
    }

    @Override
    public boolean hasRole(PrincipalCollection subjectPrincipal, String roleIdentifier) {
        return ensureDelegate().hasRole(subjectPrincipal, roleIdentifier);
    }

    @Override
    public boolean[] hasRoles(PrincipalCollection subjectPrincipal, List<String> roleIdentifiers) {
        return ensureDelegate().hasRoles(subjectPrincipal, roleIdentifiers);
    }

    @Override
    public boolean hasAllRoles(PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers) {
        return ensureDelegate().hasAllRoles(subjectPrincipal, roleIdentifiers);
    }

    @Override
    public void checkRole(PrincipalCollection subjectPrincipal, String roleIdentifier) throws AuthorizationException {
        ensureDelegate().checkRole(subjectPrincipal, roleIdentifier);
    }

    @Override
    public void checkRoles(PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers) throws AuthorizationException {
        ensureDelegate().checkRoles(subjectPrincipal, roleIdentifiers);
    }

    @Override
    public void checkRoles(PrincipalCollection subjectPrincipal, String... roleIdentifiers) throws AuthorizationException {
        ensureDelegate().checkRoles(subjectPrincipal, roleIdentifiers);
    }

    @Override
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        if(security != null) {
            security.setCacheManager(cacheManager);
        }
    }

    @Override
    public void destroy() {
        boolean wasDestroyed = destroyed;
        destroyed = true;
        if(!wasDestroyed) {
            logger.info("Destroying realm delegate");
            LifecycleUtils.destroy(security);
        }
    }

}
