/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.di.Injections;
import groovy.lang.GroovyClassLoader;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.LifecycleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SecurityGroovyRealm implements PortofinoRealm, Destroyable {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Logger
    //--------------------------------------------------------------------------

    public static final Logger logger = LoggerFactory.getLogger(SecurityGroovyRealm.class);

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    protected final GroovyClassLoader classLoader;
    protected final ServletContext servletContext;
    protected volatile PortofinoRealm security;
    protected volatile boolean destroyed = false;

    protected CacheManager cacheManager;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public SecurityGroovyRealm(GroovyClassLoader classLoader, ServletContext servletContext)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException {
        this.classLoader = classLoader;
        this.servletContext = servletContext;
        doEnsureDelegate();
    }

    //--------------------------------------------------------------------------
    // Delegation support
    //--------------------------------------------------------------------------

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

    private PortofinoRealm doEnsureDelegate()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        Class<?> scriptClass = classLoader.loadClass("Security", true, false, true);
        if(scriptClass.isInstance(security)) { //Class did not change
            return security;
        } else {
            logger.info("Refreshing Portofino Realm Delegate instance (Security.groovy)");
            if(security != null) {
                logger.debug("Script class changed: from " + security.getClass() + " to " + scriptClass);
            }
            Object securityTemp = scriptClass.newInstance();
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
        Injections.inject(
                security,
                servletContext,
                null);
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
    public Map<Serializable, String> getUsers() {
        return ensureDelegate().getUsers();
    }

    @Override
    public Serializable getUserById(String encodedUserId) {
        return ensureDelegate().getUserById(encodedUserId);
    }

    @Override
    public Serializable saveUser(Serializable user) {
        return ensureDelegate().saveUser(user);
    }

    @Override
    public Serializable updateUser(Serializable user) {
        return ensureDelegate().updateUser(user);
    }

    @Override
    public ClassAccessor getUserClassAccessor() {
        return ensureDelegate().getUserClassAccessor();
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
    public String saveSelfRegisteredUser(Object user) {
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
    public Set<String> getGroups() {
        return ensureDelegate().getGroups();
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
