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

import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.cache.CacheManagerAware;
import org.apache.shiro.realm.Realm;

import java.io.Serializable;
import java.util.Set;

/**
 * Delegate interface used by Portofino's default Shiro realm implementation.
 * Key functionality is delegated to instances of this interface to allow
 * arbitrary implementations by users (security.groovy) without touching the core framework.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface PortofinoRealm extends Realm, Authorizer, CacheManagerAware {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";



    //--------------------------------------------------------------------------
    // User workflow
    //--------------------------------------------------------------------------

    /**
     * Marks the user as verified as a consequence of a user's action, e.g.
     * clicking on a verification email or entering a verification code.
     * This method is called in during the user's self-registration process.
     * @param user the user object.
     */
    void verifyUser(Serializable user);

    //--------------------------------------------------------------------------
    // User password management
    //--------------------------------------------------------------------------

    /**
     * Changes a user's password
     * @param user the user object.
     * @param newPassword the new password.
     */
    void changePassword(Serializable user, String newPassword);

    /**
     * Generates a one-time token, for use in email validation and password reset
     * Unlike a password, which needs to be associated to a login to be a ShiroToken
     * a one-time token is valid ShiroToken.
     * @param user the user object.
     * @return the one-time token
     */
    String generateOneTimeToken(Serializable user);

    //--------------------------------------------------------------------------
    // Users CRUD
    //--------------------------------------------------------------------------

    /**
     * Returns the list of users known to the system. This is used by the framework when presenting a list of
     * possible users, e.g. when configuring permissions for a page.
     * @return a set of users, represented as strings (the usernames used to log in).
     */
    Set<String> getUsers();
    //TODO: List<Serializable> getUsers();

    //--------------------------------------------------------------------------
    // Groups CRUD
    //--------------------------------------------------------------------------

    /**
     * Returns the list of groups known to the system. This is used by the framework when presenting a list of
     * possible groups, e.g. when configuring permissions for a page.
     * @return a set of groups.
     */
    Set<String> getGroups();

    String getAllGroup();

    String getAnonymousGroup();

    String getRegisteredGroup();

    String getAdministratorsGroup();
}
