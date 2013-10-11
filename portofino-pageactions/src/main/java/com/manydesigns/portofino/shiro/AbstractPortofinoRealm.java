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
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.modules.BaseModule;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.io.Serializable;
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

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    protected Configuration portofinoConfiguration;

    //--------------------------------------------------------------------------
    // Authorization
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>This default implementation handles built-in groups (all, anonymous, registered, etc.), delegating
     * to loadAuthorizationInfo method the actual loading of application-specific groups.</p>
     *
     * @return
     */
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Object principal = principals.getPrimaryPrincipal();
        Set<String> groups = new HashSet<String>();
        groups.add(SecurityLogic.getAllGroup(portofinoConfiguration));
        if (principal == null) {
            groups.add(SecurityLogic.getAnonymousGroup(portofinoConfiguration));
        } else if (principal instanceof Serializable) {
            groups.add(SecurityLogic.getRegisteredGroup(portofinoConfiguration));
            groups.addAll(loadAuthorizationInfo((Serializable) principal));
        } else {
            throw new AuthorizationException("Invalid principal: " + principal);
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(groups);
        if(groups.contains(SecurityLogic.getAdministratorsGroup(portofinoConfiguration))) {
            info.addStringPermission("*");
        }
        Permission permission = new GroupPermission(groups);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    /**
     * Loads the groups associated to a given user.
     * @param principal the user object.
     * @return the groups as a collection of strings.
     */
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        return Collections.emptySet();
    }

    //--------------------------------------------------------------------------
    // Groups CRUD
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>This default implementation returns the built-in groups
     * (all, anonymous, registered, administrators).
     * You can override it to add custom groups for your application.</p>
     * @return
     */
    public Set<String> getGroups() {
        Set<String> groups = new LinkedHashSet<String>();
        groups.add(SecurityLogic.getAllGroup(portofinoConfiguration));
        groups.add(SecurityLogic.getAnonymousGroup(portofinoConfiguration));
        groups.add(SecurityLogic.getRegisteredGroup(portofinoConfiguration));
        groups.add(SecurityLogic.getAdministratorsGroup(portofinoConfiguration));
        return groups;
    }

    //--------------------------------------------------------------------------
    // Users CRUD
    //--------------------------------------------------------------------------

    @Override
    public Serializable getUserById(String encodedUserId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Serializable getUserByEmail(String email) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassAccessor getSelfRegisteredUserClassAccessor() {
        return JavaClassAccessor.getClassAccessor(User.class);
    }

    @Override
    public String getUserPrettyName(Serializable user) {
        return user.toString();
    }

    @Override
    public Serializable saveUser(Serializable user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Serializable updateUser(Serializable user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassAccessor getUserClassAccessor() {
        throw new UnsupportedOperationException();
    }

    //--------------------------------------------------------------------------
    // User workflow
    //--------------------------------------------------------------------------

    @Override
    public void verifyUser(Serializable user) {
        throw new UnsupportedOperationException();
    }

    //--------------------------------------------------------------------------
    // User password management
    //--------------------------------------------------------------------------

    @Override
    public void changePassword(Serializable user, String oldPassword, String newPassword) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String generateOneTimeToken(Serializable user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String saveSelfRegisteredUser(Object user) {
        throw new UnsupportedOperationException();
    }
}
