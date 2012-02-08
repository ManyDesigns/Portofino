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
package com.manydesigns.portofino.actions.user;

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.dispatcher.RequestAttributes;
import groovy.lang.GroovyObject;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding("/actions/user/login")
public class LoginAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";
    public static final String LOGIN_ACTION_NAME = "loginAction";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    //**************************************************************************
    // Request parameters
    //**************************************************************************

    public String userName;
    public String pwd;

    //**************************************************************************
    // Scripting
    //**************************************************************************

    protected GroovyObject groovyObject;
    protected String script;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************
    public boolean recoverPwd;

    public String returnUrl;
    public String cancelReturnUrl;

    public static final Logger logger =
            LoggerFactory.getLogger(LoginAction.class);

    public LoginAction() {}

    @DefaultHandler
    public Resolution execute () {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return new ForwardResolution("/layouts/user/alreadyLoggedIn.jsp");
        }

        recoverPwd = portofinoConfiguration.getBoolean(
                PortofinoProperties.MAIL_ENABLED, false);

        return new ForwardResolution("/layouts/user/login.jsp");
    }

    @Button(list = "login-buttons", key = "commons.login", order = 1)
    public Resolution login() {
        Subject subject = SecurityUtils.getSubject();
        Locale locale = context.getLocale();
        ResourceBundle bundle = application.getBundle(locale);
        try {
            subject.login(new UsernamePasswordToken(userName, pwd));
            logger.info("User {} login", userName);
            String successMsg = MessageFormat.format(
                    bundle.getString("user.login.success"), userName);
            SessionMessages.addInfoMessage(successMsg);
            if (StringUtils.isEmpty(returnUrl)) {
                returnUrl = "/";
            }
            logger.debug("Redirecting to: {}", returnUrl);
            return new RedirectResolution(returnUrl);
        } catch (ExpiredCredentialsException e) {
            String errMsg = MessageFormat.format(bundle.getString("user.not.active"), userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            return new ForwardResolution("/layouts/user/login.jsp");
        } catch (AuthenticationException e) {
            String errMsg = MessageFormat.format(bundle.getString("user.login.failed"), userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            return new ForwardResolution("/layouts/user/login.jsp");
        }
    }

    @Button(list = "login-buttons", key = "commons.cancel", order = 2)
    public Resolution cancel() {
        String url = "/";
        if(!StringUtils.isBlank(cancelReturnUrl)) {
            url = cancelReturnUrl;
        } else if(!StringUtils.isBlank(returnUrl)) {
            url = returnUrl;
        }
        return new RedirectResolution(url);
    }

    public Resolution logout() {
        SecurityUtils.getSubject().logout();
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }

        Locale locale = context.getLocale();
        ResourceBundle bundle = application.getBundle(locale);
        String msg = bundle.getString("user.logout");
        SessionMessages.addInfoMessage(msg);

        return new RedirectResolution("/");
    }

    // do not expose this method publicly
    protected HttpSession getSession() {
        return context.getRequest().getSession(false);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelReturnUrl() {
        return cancelReturnUrl;
    }

    public void setCancelReturnUrl(String cancelReturnUrl) {
        this.cancelReturnUrl = cancelReturnUrl;
    }

    public Application getApplication() {
        return application;
    }

    public Configuration getPortofinoConfiguration() {
        return portofinoConfiguration;
    }
}
