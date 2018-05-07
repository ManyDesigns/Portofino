/*
 * Copyright (C) 2005-2018 ManyDesigns srl.  All rights reserved.
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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An Shiro filter to validate a JSON Web Token.
 *
 * @author Alessio Stalla       - alessiostalla@gmail.com
 */
public class JWTFilter extends PathMatchingFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String jwt = getJSONWebToken(httpRequest);
        if(jwt == null) {
            logger.debug("JWT not found, proceeding with the request");
            return true;
        }
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated()) {
            subject.logout();
        }
        try {
            subject.login(new JSONWebToken(jwt));
            return true;
        } catch (AuthenticationException e) {
            logger.warn("Failed JWT authentication to " + httpRequest.getRequestURL(), e);
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return true;
        }
    }

    @Nullable
    protected String getJSONWebToken(HttpServletRequest httpRequest) {
        String jwt = httpRequest.getHeader("Authorization");
        if(jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring("Bearer ".length());
        }
        return jwt;
    }

}
