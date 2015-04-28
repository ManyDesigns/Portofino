/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.security.Principal;

/**
 * Add this to shiro.ini to manage user authentication with the servlet container. Note: also requires support in
 * Security.groovy and the login action.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ServletContainerSecurityFilter extends PathMatchingFilter {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(ServletContainerSecurityFilter.class);

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        HttpServletRequest req = (HttpServletRequest) request;
        boolean shiroAuthenticated = subject.isAuthenticated();

        //Returns: a java.security.Principal containing the name of the user making this request;
        //null if the user has not been authenticated
        Principal containerPrincipal = getContainerPrincipal(req);
        boolean containerAuthenticated = containerPrincipal != null;
        logger.debug("User authenticated by Shiro? {} User authenticated by the container? {}",
                shiroAuthenticated, containerAuthenticated);
        if (!shiroAuthenticated && containerAuthenticated) {
            logger.debug("User is known to the servlet container, but not to Shiro, attempting programmatic login");
            try {
                subject.login(new ServletContainerToken(req));
                Serializable userId = ShiroUtils.getUserId(SecurityUtils.getSubject());
                logger.info("User {} login", userId);
            } catch (AuthenticationException e) {
                HttpSession session = req.getSession(false);
                String attrName = ServletContainerSecurityFilter.class.getName() + ".shiroLoginFailedErrorLogged";
                String msg =
                        "User " + containerPrincipal + " is known to the servlet container, " +
                        "but not to Shiro, and programmatic login failed!";
                if (session == null || session.getAttribute(attrName) == null) {
                    logger.error(msg, e);
                } else {
                    logger.debug(msg, e);
                }
                if (session != null) {
                    session.setAttribute(attrName, true);
                }
            }
        } else if (shiroAuthenticated && !containerAuthenticated) {
            logger.debug("User is authenticated to Shiro, but not to the servlet container; logging out of Shiro.");
            Serializable userId = ShiroUtils.getUserId(SecurityUtils.getSubject());
            subject.logout();
            logger.info("User {} logout", userId);
        }
        return true;
    }

    protected Principal getContainerPrincipal(HttpServletRequest req) {
        if (req instanceof ShiroHttpServletRequest) {

            ServletRequest request = ((ShiroHttpServletRequest) req).getRequest();
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                return httpRequest.getUserPrincipal();
            }
        }
        return req.getUserPrincipal();
    }
}
