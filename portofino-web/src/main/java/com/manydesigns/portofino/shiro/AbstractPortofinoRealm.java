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

import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.AppProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.di.Inject;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.openid4java.discovery.Identifier;

import java.util.*;

/**
 * Default implementation of PortofinoRealm. Provides convenient implementations of the interface methods.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class AbstractPortofinoRealm extends AuthorizingRealm implements PortofinoRealm {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Injections
    //--------------------------------------------------------------------------

    @Inject(RequestAttributes.APPLICATION)
    protected Application application;

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    protected Configuration portofinoConfiguration;

    //--------------------------------------------------------------------------
    // Authorization
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>This default implementation handles built-in groups (all, anonymous, registered, etc.), delegating
     * to loadAuthorizationInfo methods the actual loading of application-specific groups.</p>
     *
     * @return
     */
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Object principal = principals.getPrimaryPrincipal();
        Set<String> groups = new HashSet<String>();
        groups.add(portofinoConfiguration.getString(AppProperties.GROUP_ALL));
        if (principal == null) {
            groups.add(portofinoConfiguration.getString(AppProperties.GROUP_ANONYMOUS));
        } else if(principal instanceof PrincipalCollection) {
            groups.add(portofinoConfiguration.getString(AppProperties.GROUP_REGISTERED));
            groups.addAll(loadAuthorizationInfo((PrincipalCollection) principal));
        } else if(principal instanceof String) {
            groups.add(portofinoConfiguration.getString(AppProperties.GROUP_REGISTERED));
            groups.addAll(loadAuthorizationInfo((String) principal));
//        } else if(isOpenIDEnabled() && (principal instanceof Identifier)) {
//            groups.add(portofinoConfiguration.getString(AppProperties.GROUP_EXTERNALLY_AUTHENTICATED));
//            groups.addAll(loadAuthorizationInfo((Identifier) principal));
        } else {
            groups.addAll(loadAuthorizationInfo(principal));
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(groups);
        if(groups.contains(getAdministratorsGroup())) {
            info.addStringPermission("*");
        }
        Permission permission = new GroupPermission(groups);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    /**
     * Loads the groups associated to a PrincipalCollection. This might include, besides the username, other
     * principals like an internal user ID.
     * @param principalCollection
     * @return
     */
    protected Collection<String> loadAuthorizationInfo(PrincipalCollection principalCollection) {
        return Collections.emptySet();
    }

    /**
     * Loads the groups associated to a given username.
     * @param principal
     * @return
     */
    protected Collection<String> loadAuthorizationInfo(String principal) {
        return Collections.emptySet();
    }

    /**
     * Loads the groups associated to an OpenID identifier.
     * @param principal
     * @return
     */
    protected Collection<String> loadAuthorizationInfo(Identifier principal) {
        return Collections.emptySet();
    }

    /**
     * Loads the groups associated to a principal of an unknown type. This implementation throws an
     * AuthorizationException, but you can override it to handle your custom principal type.
     * @param principal
     * @return
     */
    protected Collection<String> loadAuthorizationInfo(Object principal) {
        throw new AuthorizationException("Invalid principal: " + principal);
    }

    /**
     * Returns the name of the administrators group as defined in app.properties.
     * @return
     */
    protected String getAdministratorsGroup() {
        return portofinoConfiguration.getString(AppProperties.GROUP_ADMINISTRATORS);
    }

    /**
     * {@inheritDoc}
     * <p>This default implementation returns the built-in groups
     * (all, anonymous, registered, administrators, externally authenticated).
     * You can override it to add custom groups for your application.</p>
     * @return
     */
    public Set<String> getGroups() {
        Set<String> groups = new LinkedHashSet<String>();
        String group = portofinoConfiguration.getString(AppProperties.GROUP_ALL);
        groups.add(group);
        group = portofinoConfiguration.getString(AppProperties.GROUP_ANONYMOUS);
        groups.add(group);
        group = portofinoConfiguration.getString(AppProperties.GROUP_REGISTERED);
        groups.add(group);
        group = portofinoConfiguration.getString(AppProperties.GROUP_ADMINISTRATORS);
        groups.add(group);
//        if(isOpenIDEnabled()) {
//            groups.add(portofinoConfiguration.getString(AppProperties.GROUP_EXTERNALLY_AUTHENTICATED));
//        }
        return groups;
    }

    @Override
    public void verifyUser(Object user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void approveUser(Object user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rejectUser(Object user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lockUser(Object user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlockUser(Object user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validateToken(Object user, AuthenticationToken token) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void changePassword(Object user, String newPassword) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String generateOneTimeToken(Object user) {
        throw new UnsupportedOperationException();
    }
}
