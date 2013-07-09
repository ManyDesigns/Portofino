package com.manydesigns.portofino.pageactions.login;

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.AppProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.shiro.openid.OpenIDToken;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@ScriptTemplate("script_template.groovy")
@PageActionName("OpenID Login")
public class OpenIdLoginAction extends DefaultLoginAction implements PageAction {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final String OPENID_DISCOVERED = "openID.discovered";
    public static final String OPENID_CONSUMER_MANAGER = "openID.consumerManager";

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
     * The application object. Injected.
     */
    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    /**
     * The model object. Injected.
     */
    @Inject(RequestAttributes.MODEL)
    public Model model;

    /**
     * The global configuration object. Injected.
     */
    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    public String openIdUrl;
    public String openIdDestinationUrl;
    public Map openIdParameterMap;

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

        UrlBuilder urlBuilder = new UrlBuilder(context.getLocale(), getOriginalPath(), false);
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

    public Application getApplication() {
        return application;
    }

    public boolean isOpenIdEnabled() {
        return application.getConfiguration().getBoolean(AppProperties.OPENID_ENABLED, false);
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
    public String getApplicationName() {
        return application.getName();
    }

    @Override
    public Resolution preparePage() {
        return null;
    }

    @Override
    public String getDescription() {
        return pageInstance.getName();
    }

    @Override
    public PageInstance getPageInstance() {
        return pageInstance;
    }

    @Override
    public void setPageInstance(PageInstance pageInstance) {
        this.pageInstance = pageInstance;
    }

    @Override
    public void setDispatch(Dispatch dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    public Dispatch getDispatch() {
        return dispatch;
    }

    @Override
    public boolean isEmbedded() {
        return false;
    }
}
