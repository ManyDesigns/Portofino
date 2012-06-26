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
package com.manydesigns.portofino.actions.user;

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.shiro.openid.OpenIDToken;
import groovy.lang.GroovyObject;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding(LoginAction.URL_BINDING)
public class LoginAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";
    public static final String LOGIN_ACTION_NAME = "loginAction";
    public static final String URL_BINDING = "/actions/user/login";

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
            if(StringUtils.isEmpty(returnUrl)) {
                logger.debug("Already logged in");
                return new ForwardResolution("/layouts/user/alreadyLoggedIn.jsp");
            } else {
                logger.debug("Already logged in, redirecting to {}", returnUrl);
                return new RedirectResolution(returnUrl);
            }
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
        } catch (DisabledAccountException e) {
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

    public Resolution showOpenIDForm() throws ConsumerException, MessageException, DiscoveryException, MalformedURLException { //TODO
        String openIDUrl = "https://www.google.com/accounts/o8/id"; //TODO

        ConsumerManager manager = new ConsumerManager();

        // perform discovery on the user-supplied identifier
        List discoveries = manager.discover(openIDUrl);

        // attempt to associate with the OpenID provider
        // and retrieve one service endpoint for authentication
        DiscoveryInformation discovered = manager.associate(discoveries);

        UrlBuilder urlBuilder = new UrlBuilder(context.getLocale(), URL_BINDING, false);
        urlBuilder.setEvent("handleOpenIDLogin");
        urlBuilder.addParameter("returnUrl", returnUrl);
        urlBuilder.addParameter("cancelReturnUrl", cancelReturnUrl);

        URL url = new URL(context.getRequest().getRequestURL().toString());
        String port = url.getPort() > 0 ? ":" + url.getPort() : "";
        String urlString =
                url.getProtocol() + "://" + url.getHost() + port +
                context.getRequest().getContextPath() + urlBuilder;
        // obtain a AuthRequest message to be sent to the OpenID provider
        AuthRequest authReq = manager.authenticate(discovered, urlString);

        // store the discovery information in the user's session for later use
        // leave out for stateless operation / if there is no session
        HttpSession session = context.getRequest().getSession();
        session.setAttribute("discovered", discovered);
        session.setAttribute("consumerManager", manager);

        return new RedirectResolution(authReq.getDestinationUrl(true));
    }

    public Resolution handleOpenIDLogin() throws DiscoveryException, AssociationException, MessageException {
    // extract the parameters from the authentication response
    // (which comes in as a HTTP request from the OpenID provider)
        HttpServletRequest request = context.getRequest();
        ParameterList openidResp = new ParameterList(request.getParameterMap());

    // retrieve the previously stored discovery information
        HttpSession session = request.getSession();
        DiscoveryInformation discovered =
            (DiscoveryInformation) session.getAttribute("discovered");
        session.removeAttribute("discovered");
        ConsumerManager manager =
                (ConsumerManager) session.getAttribute("consumerManager");
        session.removeAttribute("consumerManager");

        // extract the receiving URL from the HTTP request
        StringBuffer receivingURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null && queryString.length() > 0)
            receivingURL.append("?").append(request.getQueryString());

        // verify the response
        VerificationResult verification = manager.verify(receivingURL.toString(), openidResp, discovered);

        // examine the verification result and extract the verified identifier
        Identifier verified = verification.getVerifiedId();
        Locale locale = context.getLocale();
        ResourceBundle bundle = application.getBundle(locale);

        if (verified != null) {
            // success, use the verified identifier to identify the user
            // OpenID authentication failed
            Subject subject = SecurityUtils.getSubject();
            try {
                subject.login(new OpenIDToken(verification));
                String userId = verification.getVerifiedId().getIdentifier();
                logger.info("User {} login", userId);
                String successMsg = MessageFormat.format(
                        bundle.getString("user.login.success"), userId);
                SessionMessages.addInfoMessage(successMsg);
                if (StringUtils.isEmpty(returnUrl)) {
                    returnUrl = "/";
                }
                logger.debug("Redirecting to: {}", returnUrl);
                return new RedirectResolution(returnUrl);
            } catch (AuthenticationException e) {
                String errMsg = MessageFormat.format(bundle.getString("user.login.failed"), userName);
                SessionMessages.addErrorMessage(errMsg);
                logger.warn(errMsg, e);
                return new ForwardResolution("/layouts/user/login.jsp");
            }
        } else {
            String errMsg = MessageFormat.format(bundle.getString("user.login.failed"), userName);
            SessionMessages.addErrorMessage(errMsg);
            return new ForwardResolution("/layouts/user/login.jsp");
        }
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
