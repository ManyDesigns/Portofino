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
package com.manydesigns.portofino.actions.user;

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.AppProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.shiro.ShiroUtils;
import com.manydesigns.portofino.shiro.openid.OpenIDToken;
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
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Action that handles the standard Portofino login form. It supports two login methods: username + password
 * and OpenID.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding(LoginAction.URL_BINDING)
public class LoginAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/user/login";
    public static final String OPENID_DISCOVERED = "openID.discovered";
    public static final String OPENID_CONSUMER_MANAGER = "openID.consumerManager";

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
    public String openIdUrl;
    public String openIdDestinationUrl;
    public Map openIdParameterMap;

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
            return redirectToReturnUrl();
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

    protected Resolution redirectToReturnUrl() {
        return redirectToReturnUrl(returnUrl);
    }

    protected Resolution redirectToReturnUrl(String returnUrl) {
        if (StringUtils.isEmpty(returnUrl)) {
            returnUrl = "/";
        } else try {
            new URL(returnUrl);
            logger.warn("Invalid return URL: " + returnUrl);
            return new RedirectResolution("/");
        } catch (MalformedURLException e) {
            //Ok, it must not be a URL to avoid possible XSS attacks with returnUrl=http://www.evil.com/hack
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
        String userName = ShiroUtils.getPrimaryPrincipal(SecurityUtils.getSubject()) + "";
        SecurityUtils.getSubject().logout();
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }

        Locale locale = context.getLocale();
        ResourceBundle bundle = application.getBundle(locale);
        String msg = bundle.getString("user.logout");
        SessionMessages.addInfoMessage(msg);
        logger.info("User {} logout", userName);

        return new RedirectResolution("/");
    }

    public Resolution showOpenIDForm()
            throws ConsumerException, MessageException, DiscoveryException, MalformedURLException {
        if(!isOpenIdEnabled()) {
            return new ErrorResolution(403);
        }
        ConsumerManager manager = new ConsumerManager();

        // perform discovery on the user-supplied identifier
        List discoveries = manager.discover(openIdUrl);

        // attempt to associate with the OpenID provider
        // and retrieve one service endpoint for authentication
        DiscoveryInformation discovered = manager.associate(discoveries);

        UrlBuilder urlBuilder = new UrlBuilder(context.getLocale(), URL_BINDING, false);
        urlBuilder.setEvent("handleOpenIDLogin");
        urlBuilder.addParameter("returnUrl", returnUrl);
        urlBuilder.addParameter("cancelReturnUrl", cancelReturnUrl);

        URL url = new URL(context.getRequest().getRequestURL().toString());
        String port = url.getPort() > 0 ? ":" + url.getPort() : "";
        String baseUrl = url.getProtocol() + "://" + url.getHost() + port;
        String urlString = baseUrl + context.getRequest().getContextPath() + urlBuilder;
        // obtain a AuthRequest message to be sent to the OpenID provider
        AuthRequest authReq = manager.authenticate(discovered, urlString, baseUrl);

        // store the discovery information in the user's session for later use
        // leave out for stateless operation / if there is no session
        HttpSession session = context.getRequest().getSession();
        session.setAttribute(OPENID_DISCOVERED, discovered);
        session.setAttribute(OPENID_CONSUMER_MANAGER, manager);

        String destinationUrl = authReq.getDestinationUrl(true);

        if(destinationUrl.length() > 2000) {
            if(authReq.isVersion2()) {
                openIdDestinationUrl = authReq.getDestinationUrl(false);
                openIdParameterMap = authReq.getParameterMap();
                return new ForwardResolution("/layouts/user/openIDFormRedirect.jsp");
            } else {
                SessionMessages.addErrorMessage("Cannot login, payload too big and OpenID version 2 not supported.");
                return new ForwardResolution("/layouts/user/login.jsp");
            }
        } else {
            return new RedirectResolution(destinationUrl, false);
        }
    }

    public Resolution handleOpenIDLogin() throws DiscoveryException, AssociationException, MessageException {
        if(!isOpenIdEnabled()) {
            return new ErrorResolution(403);
        }
        // extract the parameters from the authentication response
        // (which comes in as a HTTP request from the OpenID provider)
        HttpServletRequest request = context.getRequest();
        ParameterList openidResp = new ParameterList(request.getParameterMap());

        // retrieve the previously stored discovery information
        HttpSession session = request.getSession();
        DiscoveryInformation discovered =
            (DiscoveryInformation) session.getAttribute(OPENID_DISCOVERED);
        ConsumerManager manager =
                (ConsumerManager) session.getAttribute(OPENID_CONSUMER_MANAGER);

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
                String successMsg = MessageFormat.format(bundle.getString("user.login.success"), userId);
                SessionMessages.addInfoMessage(successMsg);
                if (StringUtils.isEmpty(returnUrl)) {
                    returnUrl = "/";
                }
                session.removeAttribute(OPENID_DISCOVERED);
                session.removeAttribute(OPENID_CONSUMER_MANAGER);
                return redirectToReturnUrl(returnUrl);
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

    public boolean isOpenIdEnabled() {
        return getApplication().getAppConfiguration().getBoolean(AppProperties.OPENID_ENABLED, false);
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

    public String getOpenIdUrl() {
        return openIdUrl;
    }

    public void setOpenIdUrl(String openIdUrl) {
        this.openIdUrl = openIdUrl;
    }

    public String getOpenIdDestinationUrl() {
        return openIdDestinationUrl;
    }

    public Map getOpenIdParameterMap() {
        return openIdParameterMap;
    }

    //For openID selector
    public void setOpenid_identifier(String openIdUrl) {
        this.openIdUrl = openIdUrl;
    }
}
