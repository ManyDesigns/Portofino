/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.shiro;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletContext;
import java.io.File;
import java.security.MessageDigest;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ApplicationRealm extends AuthorizingRealm implements UsersGroupsDAO {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRealm.class);

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //null usernames are invalid
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }

        Object userId = getAvailablePrincipal(principals);
        return ensureDelegate().getAuthorizationInfo(this, userId);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        if(upToken.getUsername() == null || upToken.getPassword() == null) {
            throw new IncorrectCredentialsException("Username or password is null");
        }

        String username = upToken.getUsername();
        String password = new String(upToken.getPassword());

        Application application = getApplication();
        Configuration portofinoConfiguration = application.getPortofinoProperties();

        boolean enc = portofinoConfiguration.getBoolean(
                PortofinoProperties.PWD_ENCRYPTED, true);

        if (enc) {
            password = encryptPassword(password);
        }

        return ensureDelegate().getAuthenticationInfo(this, username, password);
    }

    public Set<String> getUsers() {
        return ensureDelegate().getUsers(this);
    }

    public Set<String> getGroups() {
        return ensureDelegate().getGroups(this);
    }

    private ApplicationRealmDelegate ensureDelegate() {
        Application application = getApplication();
        File file = new File(application.getAppScriptsDir(), "security.groovy");
        Object groovyObject;
        if(file.exists()) {
            try {
                groovyObject = ScriptingUtil.getGroovyObject(file);
            } catch (Exception e) {
                logger.error("Couldn't load security script", e);
                throw new Error("Security script missing or invalid: " + file.getAbsolutePath());
            }
            if(groovyObject instanceof ApplicationRealmDelegate) {
                return (ApplicationRealmDelegate) groovyObject;
            } else {
                throw new Error("Security object is not an instance of " + ApplicationRealmDelegate.class +
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

    public static String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(password.getBytes("UTF-8"));
            byte raw[] = md.digest();
            return (new BASE64Encoder()).encode(raw);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
