/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.shiro.openid.OpenIDToken;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.Identifier;

import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class AbstractApplicationRealmDelegate implements ApplicationRealmDelegate {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected abstract AuthenticationInfo getAuthenticationInfo(
            ApplicationRealm realm, String userName, String password);

    public AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, AuthenticationToken token) {
        if(token instanceof UsernamePasswordToken) {
            UsernamePasswordToken upToken = (UsernamePasswordToken) token;

            if(upToken.getUsername() == null || upToken.getPassword() == null) {
                throw new IncorrectCredentialsException("Username or password is null");
            }

            String username = upToken.getUsername();
            String password = new String(upToken.getPassword());
            return getAuthenticationInfo(realm, username, password);
        } else if(token instanceof OpenIDToken) {
            if(realm.isOpenIDEnabled()) {
                return getAuthenticationInfo(realm, ((OpenIDToken) token).getPrincipal());
            } else {
                throw new UnsupportedOperationException("OpenID authentication not supported");
            }
        } else {
            return doGetAuthenticationInfo(realm, token);
        }
    }

    public AuthorizationInfo getAuthorizationInfo(ApplicationRealm realm, Object principal) {
        Application application = realm.getApplication();
        Set<String> groups = new HashSet<String>();
        Configuration conf = application.getPortofinoProperties();
        groups.add(conf.getString(PortofinoProperties.GROUP_ALL));
        if (principal == null) {
            groups.add(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
        } else if(principal instanceof PrincipalCollection) {
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));
            groups.addAll(loadAuthorizationInfo(realm, (PrincipalCollection) principal));
        } else if(principal instanceof String) {
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));
            groups.addAll(loadAuthorizationInfo(realm, (String) principal));
        } else if(realm.isOpenIDEnabled() && (principal instanceof Identifier)) {
            groups.add(conf.getString(PortofinoProperties.GROUP_EXTERNALLY_AUTHENTICATED));
            groups.addAll(loadAuthorizationInfo(realm, (Identifier) principal));
        } else {
            groups.addAll(loadAuthorizationInfo(realm, principal));
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(groups);
        Permission permission = new GroupPermission(groups);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    protected Collection<String> loadAuthorizationInfo(
            ApplicationRealm realm, PrincipalCollection principalCollection) {
        return Collections.emptySet();
    }

    protected Collection<String> loadAuthorizationInfo(ApplicationRealm realm, String principal) {
        return Collections.emptySet();
    }

    protected Collection<String> loadAuthorizationInfo(ApplicationRealm realm, Identifier principal) {
        return Collections.emptySet();
    }

    protected Collection<String> loadAuthorizationInfo(ApplicationRealm realm, Object principal) {
        throw new AuthorizationException("Invalid principal: " + principal);
    }

    protected String getAdministratorsGroup(ApplicationRealm realm) {
        return realm.getApplication().getPortofinoProperties().getString(PortofinoProperties.GROUP_ADMINISTRATORS);
    }

    protected AuthenticationInfo getAuthenticationInfo(ApplicationRealm realm, VerificationResult principal) {
        return new SimpleAuthenticationInfo(
                principal.getVerifiedId(), OpenIDToken.NO_CREDENTIALS, realm.getName());
    }

    protected AuthenticationInfo doGetAuthenticationInfo(ApplicationRealm realm, AuthenticationToken token) {
        throw new UnsupportedOperationException("Authentication token " + token + " not supported");
    }

    public Set<String> getGroups(ApplicationRealm realm) {
        Application application = realm.getApplication();
        Set<String> groups = new LinkedHashSet<String>();
        Configuration conf = application.getPortofinoProperties();
        String group = conf.getString(PortofinoProperties.GROUP_ALL);
        groups.add(group);
        group = conf.getString(PortofinoProperties.GROUP_ANONYMOUS);
        groups.add(group);
        group = conf.getString(PortofinoProperties.GROUP_REGISTERED);
        groups.add(group);
        group = conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS);
        groups.add(group);
        if(realm.isOpenIDEnabled()) {
            groups.add(conf.getString(PortofinoProperties.GROUP_EXTERNALLY_AUTHENTICATED));
        }
        return groups;
    }
}
