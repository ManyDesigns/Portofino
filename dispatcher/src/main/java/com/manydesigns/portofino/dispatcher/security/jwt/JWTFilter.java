package com.manydesigns.portofino.dispatcher.security.jwt;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by alessio on 02/08/16.
 */
public abstract class JWTFilter extends PathMatchingFilter {
    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated()) {
            subject.logout();
        }
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        String jwt = getToken(httpRequest, httpResponse, mappedValue);
        if(jwt == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        try {
            subject.login(new JSONWebToken(jwt));
            return true;
        } catch (AuthenticationException e) {
            logger.warn("Failed JWT authentication to " + httpRequest.getRequestURL(), e);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    protected abstract String getToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Object mappedValue);

    /**
     * <p>Returns the host name or IP associated with the current subject.  This method is primarily provided for use
     * during construction of an <code>AuthenticationToken</code>.</p>
     * <p>The default implementation merely returns {@link ServletRequest#getRemoteHost()}.</p>
     *
     * @param request the incoming ServletRequest
     * @return the <code>InetAddress</code> to associate with the login attempt.
     */
    protected String getHost(ServletRequest request) {
        return request.getRemoteHost();
    }
}
