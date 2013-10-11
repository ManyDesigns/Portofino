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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.logic.SecurityLogic;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;

import java.io.Serializable;
import java.util.*;

/**
 * Abstract realm that leverages Google App Engine's UserService. This realm is only able to authenticate users; CRUD
 * operations are not supported.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class GAEPortofinoRealm extends AbstractPortofinoRealm {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        if(!(token instanceof ServletContainerToken)) {
            throw new UnsupportedTokenException("Token not supported: " + token);
        }
        //On GAE, if the user was logged by the container, it is also known to the UserService
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        if(user == null) {
            throw new UnknownAccountException("User is authenticated to the container, but is not known to the UserService");
        }
        //TODO verifica utilizzo User come principal direttamente
        return new SimpleAuthenticationInfo(user, token.getCredentials(), getName());
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        Set<String> authz = new HashSet<String>(super.loadAuthorizationInfo(principal));
        User user = (User) principal;
        UserService userService = UserServiceFactory.getUserService();
        if(user != null &&
           userService.isUserAdmin() && 
           StringUtils.equals(userService.getCurrentUser().getUserId(), user.getUserId())) {
            authz.add(SecurityLogic.getAdministratorsGroup(portofinoConfiguration));
        }
        return authz;
    }

    public Map<Serializable, String> getUsers() {
        return new HashMap<Serializable, String>();
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

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof ServletContainerToken;
    }

    @Override
    public Serializable getUserByEmail(String email) {
        throw new UnsupportedOperationException(); //TODO verificare
    }

    @Override
    public Serializable getUserId(Serializable user) {
        return ((User) user).getUserId();
    }

    @Override
    public String getUserPrettyName(Serializable user) {
        return ((User) user).getNickname();
    }
}
