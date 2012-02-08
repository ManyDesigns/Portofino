/*
* Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ApplicationRealm extends AuthorizingRealm {
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
        Application application = getApplication();
        Configuration portofinoConfiguration = application.getPortofinoProperties();

        boolean enc = portofinoConfiguration.getBoolean(
                PortofinoProperties.PWD_ENCRYPTED, true);

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        String password = new String(upToken.getPassword());

        if (enc) {
            password = SecurityLogic.encryptPassword(password);
        }

        return ensureDelegate().getAuthenticationInfo(this, username, password);
    }

    public List<Object[]> getUsers() {
        return ensureDelegate().getUsers(this);
    }

    public List<Object[]> getGroups() {
        return ensureDelegate().getGroups(this);
    }

    private ApplicationRealmDelegate ensureDelegate() {
        ApplicationRealmDelegate delegate = null; //TODO gestire reload
        if(delegate == null) {
            Application application = getApplication();
            File file = new File(application.getAppScriptsDir(), "security.groovy");
            Object groovyObject;
            if(file.exists()) {
                try {
                    FileReader fr = new FileReader(file);
                    String script = IOUtils.toString(fr);
                    IOUtils.closeQuietly(fr);
                    groovyObject = ScriptingUtil.getGroovyObject(script, file.getAbsolutePath());
                } catch (Exception e) {
                    logger.error("Couldn't load security script", e);
                    throw new Error("Security script missing or invalid: " + file.getAbsolutePath());
                }
                if(groovyObject instanceof ApplicationRealmDelegate) {
                    delegate = (ApplicationRealmDelegate) groovyObject;
                } else {
                    throw new Error("Security object is not an instance of " + ApplicationRealmDelegate.class +
                                    ": " + groovyObject);
                }
            }
        }
        return delegate;
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
}
