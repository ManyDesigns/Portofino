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
import com.manydesigns.portofino.application.AppProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.shiro.openid.OpenIDToken;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ApplicationRealm extends AuthorizingRealm implements UsersGroupsDAO {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(ApplicationRealm.class);

    protected final ApplicationStarter applicationStarter;

    public ApplicationRealm(ApplicationStarter applicationStarter) {
        this.applicationStarter = applicationStarter;
    }

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
        return ensureDelegate().getAuthenticationInfo(this, token);
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
                throw new Error("Security script missing or invalid: " + file.getAbsolutePath(), e);
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

    @Override
    public boolean supports(AuthenticationToken token) {
        if(token instanceof OpenIDToken) {
            return isOpenIDEnabled();
        }
        return true;
    }

    public boolean isOpenIDEnabled() {
        return getApplication().getConfiguration().getBoolean(AppProperties.OPENID_ENABLED, false);
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

    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }
}
