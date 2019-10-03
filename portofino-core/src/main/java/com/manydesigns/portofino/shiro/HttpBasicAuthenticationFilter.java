package com.manydesigns.portofino.shiro;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class HttpBasicAuthenticationFilter extends PathMatchingFilter {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(HttpBasicAuthenticationFilter.class);

    /**
     * HTTP Authorization header, equal to <code>Authorization</code>
     */
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    
    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isAuthenticated()) {
            HttpServletRequest httpRequest = WebUtils.toHttp(request);
            String authorizationHeader = httpRequest.getHeader(AUTHORIZATION_HEADER);
            if (!StringUtils.isEmpty(authorizationHeader)) {
                String[] prinCred = getPrincipalsAndCredentials(authorizationHeader);
                UsernamePasswordToken token;
                String host = getHost(request);
                if (prinCred == null || prinCred.length < 2) {
                    // Create an authentication token with an empty password,
                    // since one hasn't been provided in the request.
                    String username = prinCred == null || prinCred.length == 0 ? "" : prinCred[0];
                    token = new UsernamePasswordToken(username, "", false, host);
                } else {
                    String username = prinCred[0];
                    String password = prinCred[1];
                    token = new UsernamePasswordToken(username, password, false, host);
                }
                try {
                    subject.login(token);
                } catch (AuthenticationException e) {
                    logger.warn("Failed HTTP basic authentication to " + httpRequest.getRequestURL(), e);
                    HttpServletResponse httpResponse = WebUtils.toHttp(response);
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Configuration config = (Configuration) request.getServletContext().getAttribute(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION);
                    String authcHeader = HttpServletRequest.BASIC_AUTH + " realm=\"" + config.getString(PortofinoProperties.APP_NAME) + "\"";
                    httpResponse.setHeader("WWW-Authenticate", authcHeader);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>Returns the host name or IP associated with the current subject.  This method is primarily provided for use
     * during construction of an <code>AuthenticationToken</code>.
     * </p>
     * <p>
     * The default implementation merely returns {@link ServletRequest#getRemoteHost()}.
     * </p>
     *
     * @param request the incoming ServletRequest
     * @return the <code>InetAddress</code> to associate with the login attempt.
     */
    protected String getHost(ServletRequest request) {
        return request.getRemoteHost();
    }

    /**
     * <p>Returns the username obtained from the authorization header.</p>
     * <p>
     * Once the header is split per the RFC (based on the space character ' '), the resulting split tokens
     * are translated into the username/password pair.</p>
     *
     * @param authorizationHeader the authorization header obtained from the request.
     * @return the username (index 0)/password pair (index 1) submitted by the user for the given header value and request.
     */
    protected String[] getPrincipalsAndCredentials(String authorizationHeader) {
        String[] authTokens = authorizationHeader.split(" ", 2);
        if (authTokens.length < 2) {
            return null;
        }
        String decoded = Base64.decodeToString(authTokens[1]);
        return decoded.split(":", 2);
    }
}
