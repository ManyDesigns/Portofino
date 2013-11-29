package com.manydesigns.portofino.pageactions.openid;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.login.DefaultLoginAction;
import com.manydesigns.portofino.shiro.PortofinoRealm;
import com.manydesigns.portofino.shiro.ShiroUtils;
import com.manydesigns.portofino.shiro.openid.OpenIDToken;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UnknownAccountException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@ScriptTemplate("script_template_openid.groovy")
@PageActionName("OpenID Login")
public class OpenIdLoginAction extends DefaultLoginAction implements PageAction {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final String OPENID_DISCOVERED = "openID.discovered";
    public static final String OPENID_CONSUMER_MANAGER = "openID.consumerManager";
    public static final String OPENID_IDENTIFIER = "openID.identifier";

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    /**
     * The dispatch property. Injected.
     */
    public Dispatch dispatch;

    /**
     * The PageInstance property. Injected.
     */
    public PageInstance pageInstance;

    /**
     * The global configuration object. Injected.
     */
    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    public String openIdUrl;
    public String openIdDestinationUrl;
    public Map openIdParameterMap;

    protected String getLoginPage() {
        return "/m/openid/pageactions/openid/openIdLogin.jsp";
    }

    public Resolution showOpenIDForm()
            throws ConsumerException, MessageException, DiscoveryException, MalformedURLException {
        ConsumerManager manager = new ConsumerManager();

        // perform discovery on the user-supplied identifier
        List discoveries = manager.discover(openIdUrl);

        // attempt to associate with the OpenID provider
        // and retrieve one service endpoint for authentication
        DiscoveryInformation discovered = manager.associate(discoveries);

        UrlBuilder urlBuilder = new UrlBuilder(context.getLocale(), context.getActionPath(), false);
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
                return new ForwardResolution("/m/openid/pageactions/openid/openIDFormRedirect.jsp");
            } else {
                SessionMessages.addErrorMessage("Cannot login, payload too big and OpenID version 2 not supported.");
                return new ForwardResolution(getLoginPage());
            }
        } else {
            return new RedirectResolution(destinationUrl, false);
        }
    }

    public Resolution handleOpenIDLogin() throws DiscoveryException, AssociationException, MessageException {
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
        Identifier identifier = verification.getVerifiedId();
        Locale locale = context.getLocale();

        if (identifier != null) {
            // success, use the verified identifier to identify the user
            // OpenID authentication failed
            Subject subject = SecurityUtils.getSubject();
            try {
                subject.login(new OpenIDToken(identifier, null));
                String name = ShiroUtils.getPortofinoRealm().getUserPrettyName((Serializable) subject.getPrincipal());
                logger.info("User {} login", identifier.getIdentifier());
                String successMsg = MessageFormat.format(
                        ElementsThreadLocals.getText("user._.logged.in.successfully"), name);
                SessionMessages.addInfoMessage(successMsg);
                if (StringUtils.isEmpty(returnUrl)) {
                    returnUrl = "/";
                }
                session.removeAttribute(OPENID_DISCOVERED);
                session.removeAttribute(OPENID_CONSUMER_MANAGER);
                return redirectToReturnUrl(returnUrl);
            } catch (UnknownAccountException e) {
                //The user is not present in the system
                session.removeAttribute(OPENID_DISCOVERED);
                session.removeAttribute(OPENID_CONSUMER_MANAGER);
                session.setAttribute(OPENID_IDENTIFIER, identifier);
                return handleUnknownAccount(e);
            } catch (AuthenticationException e) {
                String errMsg = MessageFormat.format(
                        ElementsThreadLocals.getText("login.failed.for.user._"), identifier.getIdentifier());
                SessionMessages.addErrorMessage(errMsg);
                logger.warn(errMsg, e);
                return new ForwardResolution(getLoginPage());
            }
        } else {
            String errMsg = MessageFormat.format(
                    ElementsThreadLocals.getText("login.failed.for.user._"), "(failed OpenId authentication)");
            SessionMessages.addErrorMessage(errMsg);
            return new ForwardResolution(getLoginPage());
        }
    }

    /**
     * Handles the case when a user has successfully completed their OpenID login, but was rejected by Security.groovy
     * with UnknownAccountException, meaning that the user is not known to the application and cannot log in. By default,
     * the user is redirected to a sign-up page; however, you may want to adopt a different behavior.
     * @param e the exception thrown by Security.groovy (which may contain additional information).
     * @return a Resolution that controls how the request should be handled.
     */
    protected Resolution handleUnknownAccount(UnknownAccountException e) {
        logger.debug("Unkown user logged in with OpenID", e);
        return signUp();
    }

    public Resolution signUp2() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        setupSignUpForm(portofinoRealm);
        signUpForm.readFromRequest(context.getRequest());
        if (signUpForm.validate() && validateCaptcha()) {
            try {
                Object user = portofinoRealm.getSelfRegisteredUserClassAccessor().newInstance();
                signUpForm.writeToObject(user);
                String token = portofinoRealm.saveSelfRegisteredUser(user);
                HttpSession session = context.getRequest().getSession();
                Identifier identifier = (Identifier) session.getAttribute(OPENID_IDENTIFIER);

                OpenIDToken openIDToken = new OpenIDToken(identifier, token);
                subject.login(openIDToken);
                session.removeAttribute(OPENID_IDENTIFIER);
                return redirectToReturnUrl();
            } catch (Exception e) {
                logger.error("Error during sign-up", e);
                SessionMessages.addErrorMessage("Sign-up failed.");
                return new ForwardResolution(getSignUpPage());
            }
        } else {
            SessionMessages.addErrorMessage("Correct the errors before proceding");
            return new ForwardResolution(getSignUpPage());
        }
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

    @Override
    public PageInstance getPageInstance() {
        return pageInstance;
    }

    @Override
    public void setPageInstance(PageInstance pageInstance) {
        this.pageInstance = pageInstance;
    }

}
