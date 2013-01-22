/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
* http://www.manydesigns.com/
*
* Unless you have purchased a commercial license agreement from ManyDesigns srl,
* the following license terms apply:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3 as published by
* the Free Software Foundation.
*
* There are special exceptions to the terms and conditions of the GPL
* as it is applied to this software. View the full text of the
* exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
* software distribution.
*
* This program is distributed WITHOUT ANY WARRANTY; and without the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
* or write to:
* Free Software Foundation, Inc.,
* 59 Temple Place - Suite 330,
* Boston, MA  02111-1307  USA
*
*/

package com.manydesigns.portofino.shiro;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class GAEApplicationRealmDelegate extends AbstractApplicationRealmDelegate {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    @Override
    protected AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, String userName, String password) {
        throw new UnsupportedOperationException("Login with username and password is not supported");
    }

    @Override
    protected AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, ServletContainerToken token) {
        //On GAE, if the user was logged by the container, it is also known to the UserService
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        if(user == null) {
            throw new AuthenticationException("User is authenticated to the container, but is not known to the UserService");
        }
        SimplePrincipalCollection coll = new SimplePrincipalCollection();
        String realmName = realm.getName();
        coll.add(user.getEmail(), realmName);
        coll.add(user, realmName);
        return new SimpleAuthenticationInfo(coll, token.getCredentials(), realmName);
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(ApplicationRealm realm, PrincipalCollection principalCollection) {
        Set<String> authz = new HashSet<String>(super.loadAuthorizationInfo(realm, principalCollection));
        User user = principalCollection.oneByType(User.class);
        UserService userService = UserServiceFactory.getUserService();
        if(user != null &&
           userService.isUserAdmin() && 
           StringUtils.equals(userService.getCurrentUser().getUserId(), user.getUserId())) {
            authz.add(getAdministratorsGroup(realm));
        }
        return authz;
    }

    public Set<String> getUsers(ApplicationRealm realm) {
        return new HashSet<String>();
    }
}
