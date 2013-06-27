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

package com.manydesigns.portofino.actions.user;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Action that handles the standard Portofino login form. It supports two login methods: username + password
 * and OpenID.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class LoginAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    //**************************************************************************
    // Request parameters
    //**************************************************************************

    public String userName;
    public String pwd;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    public String returnUrl;
    public String cancelReturnUrl;

    public static final Logger logger =
            LoggerFactory.getLogger(LoginAction.class);

    public LoginAction() {}

    @DefaultHandler
    public Resolution execute() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        return new ForwardResolution(getLoginPage());
    }

    protected String getLoginPage() {
        return "/portofino-base/layouts/user/login.jsp";
    }

    @Button(list = "login-buttons", key = "commons.login", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution login() {
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(new UsernamePasswordToken(userName, pwd));
            logger.info("User {} login", userName);
            String successMsg = ElementsThreadLocals.getText("user.login.success", userName);
            SessionMessages.addInfoMessage(successMsg);
            return redirectToReturnUrl();
        } catch (DisabledAccountException e) {
            String errMsg = ElementsThreadLocals.getText("user.not.active", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            return new ForwardResolution(getLoginPage());
        } catch (AuthenticationException e) {
            String errMsg = ElementsThreadLocals.getText("user.login.failed", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            return new ForwardResolution(getLoginPage());
        }
    }

    protected Resolution redirectToReturnUrl() {
        return redirectToReturnUrl(returnUrl);
    }

    protected Resolution redirectToReturnUrl(String returnUrl) {
        if (StringUtils.isEmpty(returnUrl)) {
            returnUrl = "/";
        } else try {
            URL url = new URL(returnUrl);
            if(!context.getRequest().getServerName().equals(url.getHost())) {
                logger.warn("Forbidding suspicious return URL: " + returnUrl);
                return new RedirectResolution("/");
            }
        } catch (MalformedURLException e) {
            //Ok, if it is not a full URL there's no risk of XSS attacks with returnUrl=http://www.evil.com/hack
        }
        logger.debug("Redirecting to: {}", returnUrl);
        return new RedirectResolution(returnUrl);
    }

    @Button(list = "login-buttons", key = "commons.cancel", order = 2)
    public Resolution cancel() {
        String url = "/";
        if(!StringUtils.isBlank(cancelReturnUrl)) {
            url = cancelReturnUrl;
        } else if(!StringUtils.isBlank(returnUrl)) {
            url = returnUrl;
        }
        return redirectToReturnUrl(url);
    }

    public Resolution logout() {
        String userName = getPrimaryPrincipal(SecurityUtils.getSubject()) + "";
        SecurityUtils.getSubject().logout();
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }

        String msg = ElementsThreadLocals.getText("user.logout");
        SessionMessages.addInfoMessage(msg);
        logger.info("User {} logout", userName);

        return new RedirectResolution("/");
    }

    //TODO copiato da ShiroUtils
    /**
     * Returns the primary principal for a Subject - that is, in Portofino, the username.
     * @param s the subject
     * @return the username.
     */
    public static Object getPrimaryPrincipal(Subject s) {
        return getPrincipal(s, 0);
    }

    /**
     * Returns the nth principal of the given Subject. Custom security.groovy implementations might assign
     * more than one principal to a Subject.
     * @param s the subject
     * @param i the zero-based index of the principal
     * @return the principal
     * @throws IndexOutOfBoundsException if the index is greather than the number of principals associated with the
     * subject.
     */
    public static Object getPrincipal(Subject s, int i) {
        Object principal = s.getPrincipal();
        if(principal instanceof PrincipalCollection) {
            List principals = ((PrincipalCollection) principal).asList();
            return principals.get(i);
        } else {
            if(i == 0) {
                return principal;
            } else {
                throw new IndexOutOfBoundsException("The subject has only 1 principal, index " + i + " is not valid");
            }
        }
    }

    // do not expose this method publicly
    protected HttpSession getSession() {
        return context.getRequest().getSession(false);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public abstract String getApplicationName();

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

    public Configuration getPortofinoConfiguration() {
        return portofinoConfiguration;
    }

}
