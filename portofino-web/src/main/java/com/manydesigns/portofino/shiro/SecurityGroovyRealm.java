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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SecurityGroovyRealm implements PortofinoRealm {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Logger
    //--------------------------------------------------------------------------

    public static final Logger logger = LoggerFactory.getLogger(SecurityGroovyRealm.class);

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    protected final ApplicationStarter applicationStarter;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public SecurityGroovyRealm(ApplicationStarter applicationStarter) {
        this.applicationStarter = applicationStarter;
    }

    //--------------------------------------------------------------------------
    // Delegation support
    //--------------------------------------------------------------------------

    private PortofinoRealm ensureDelegate() {
        Application application = getApplication();
        File file = new File(application.getAppScriptsDir(), "security.groovy");
        Object groovyObject;
        if(file.exists()) {
            try {
                groovyObject = ScriptingUtil.getGroovyObject(file);
            } catch (Exception e) {
                logger.error("Couldn't load security script", e);
                throw new Error("Security script missing or invalid: " + file.getAbsolutePath(), e);
            }
            Injections.inject(groovyObject,
                    ElementsThreadLocals.getServletContext(),
                    ElementsThreadLocals.getHttpServletRequest()
                    );
            if(groovyObject instanceof PortofinoRealm) {
                return (PortofinoRealm) groovyObject;
            } else {
                 throw new Error("Security object is not an instance of " + PortofinoRealm.class +
                                ": " + groovyObject);
            }
        } else {
            throw new Error("Security object file not found: " + file.getAbsolutePath());
        }
    }

    public Application getApplication() {
        Application application;
        try {
            ServletContext servletContext = ElementsThreadLocals.getServletContext();
            ApplicationStarter applicationStarter =
                    (ApplicationStarter) servletContext.getAttribute(ApplicationAttributes.APPLICATION_STARTER);
            application = applicationStarter.getApplication();
        } catch (Exception e) {
            throw new AuthenticationException("Couldn't get application", e);
        }
        return application;
    }

    //--------------------------------------------------------------------------
    // PortofinoRealm implementation
    //--------------------------------------------------------------------------
    
    @Override
    public void verifyUser(Object user) {
        ensureDelegate().verifyUser(user);
    }

    @Override
    public void approveUser(Object user) {
        ensureDelegate().approveUser(user);
    }

    @Override
    public void rejectUser(Object user) {
        ensureDelegate().rejectUser(user);
    }

    @Override
    public void lockUser(Object user) {
        ensureDelegate().lockUser(user);
    }

    @Override
    public void unlockUser(Object user) {
        ensureDelegate().unlockUser(user);
    }

    @Override
    public void validateToken(Object user, AuthenticationToken token) {
        ensureDelegate().validateToken(user, token);
    }

    @Override
    public void changePassword(Object user, String newPassword) {
        ensureDelegate().changePassword(user, newPassword);
    }

    @Override
    public String generateOneTimeToken(Object user) {
        return ensureDelegate().generateOneTimeToken(user);
    }

    @Override
    public Set<String> getUsers() {
        return ensureDelegate().getUsers();
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
}
