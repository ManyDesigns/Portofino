/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.rest;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.blobs.FileUploadLimitExceededException;
import com.manydesigns.elements.blobs.StreamingCommonsMultipartWrapper;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.servlet.ServletConstants;
import com.manydesigns.portofino.buttons.ButtonsLogic;
import com.manydesigns.portofino.buttons.Guarded;
import com.manydesigns.portofino.cache.ControlsCache;
import com.manydesigns.portofino.operations.Operations;
import com.manydesigns.portofino.pageactions.PageAction;
import com.manydesigns.portofino.security.SecurityLogic;
import com.manydesigns.portofino.shiro.SecurityUtilsBean;
import com.manydesigns.portofino.shiro.ShiroUtils;
import ognl.OgnlContext;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.aop.AnnotationsAuthorizingMethodInterceptor;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import static javax.ws.rs.core.Response.Status.CONFLICT;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class PortofinoFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(PortofinoFilter.class);
    public final static Logger accessLogger = LoggerFactory.getLogger("com.manydesigns.portofino.access");
    public static final String MESSAGE_HEADER = "X-Portofino-Message";

    @Context
    protected ResourceInfo resourceInfo;

    @Context
    protected HttpServletRequest request;

    @Context
    protected ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        UriInfo uriInfo = requestContext.getUriInfo();
        if(uriInfo.getMatchedResources().isEmpty()) {
            return;
        }
        Object resource = uriInfo.getMatchedResources().get(0);
        if(resourceInfo == null || resourceInfo.getResourceClass() == null) {
            return;
        }
        if(resource.getClass() != resourceInfo.getResourceClass()) {
            throw new RuntimeException("Inconsistency: matched resource is not of the right type, " + resourceInfo.getResourceClass());
        }
        fillMDC();
        logger.debug("Publishing securityUtils in OGNL context");
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ognlContext.put("securityUtils", new SecurityUtilsBean());
        if(resource instanceof PageAction) {
            PageAction pageAction = (PageAction) resource;
            pageAction.prepareForExecution();
        }
        checkAuthorizations(requestContext, resource);
        accessLogger.info(requestContext.getMethod());
    }

    protected void addCacheHeaders(ContainerResponseContext responseContext) {
        if(resourceInfo.getResourceMethod() != null && resourceInfo.getResourceMethod().isAnnotationPresent(ControlsCache.class)) {
            return;
        }
        // Avoid caching of dynamic pages
        //HTTP 1.0
        responseContext.getHeaders().putSingle(ServletConstants.HTTP_PRAGMA, ServletConstants.HTTP_PRAGMA_NO_CACHE);
        responseContext.getHeaders().putSingle(ServletConstants.HTTP_EXPIRES, 0);
        //HTTP 1.1
        responseContext.getHeaders().add(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_CACHE);
        responseContext.getHeaders().add(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_STORE);
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        addCacheHeaders(responseContext);
        for(String message : RequestMessages.consumeErrorMessages()) {
            responseContext.getHeaders().add(MESSAGE_HEADER, "error: " + message);
        }
        for(String message : RequestMessages.consumeWarningMessages()) {
            responseContext.getHeaders().add(MESSAGE_HEADER, "warning: " + message);
        }
        for(String message : RequestMessages.consumeInfoMessages()) {
            responseContext.getHeaders().add(MESSAGE_HEADER, "info: " + message);
        }
    }

    protected void checkAuthorizations(ContainerRequestContext requestContext, Object resource) {
        try {
            Method handler = resourceInfo.getResourceMethod();
            AUTH_CHECKER.assertAuthorized(resource, handler);
            logger.debug("Standard Shiro security check passed.");
            if(resource instanceof PageAction) {
                checkPageActionInvocation(requestContext, (PageAction) resource);
            }
        } catch (UnauthenticatedException e) {
            logger.debug("Method required authentication", e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        } catch (AuthorizationException e) {
            logger.warn("Method invocation not authorized", e);
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    protected void fillMDC() {
        logger.debug("Retrieving user");
        Serializable userId = null;
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        if (principal == null) {
            logger.debug("No user found");
        } else {
            userId = ShiroUtils.getUserId(subject);
            logger.debug("Retrieved userId={}", userId);
        }

        logger.debug("Setting up logging MDC");
        MDC.clear();
        if(userId != null) { //Issue #755
            MDC.put("userId", userId.toString());
        }
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        if(request != null) {
            MDC.put("req.requestURI", request.getRequestURI());
        }
    }

    protected void checkPageActionInvocation(ContainerRequestContext requestContext, PageAction pageAction) {
        Method handler = resourceInfo.getResourceMethod();
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        if(!SecurityLogic.isAllowed(request, pageAction.getPageInstance(), pageAction, handler)) {
            Response.Status status =
                    SecurityUtils.getSubject().isAuthenticated() ?
                            Response.Status.FORBIDDEN :
                            Response.Status.UNAUTHORIZED;
            requestContext.abortWith(Response.status(status).build());
        } else if(!Operations.doGuardsPass(pageAction, handler)) {
            if(pageAction instanceof Guarded) {
                Response response = ((Guarded) pageAction).guardsFailed(handler);
                requestContext.abortWith(response);
            } else {
                requestContext.abortWith(
                        Response.status(CONFLICT)
                                .entity("The action couldn't be invoked, a guard did not pass")
                                .build());
            }
        } else {
            logger.debug("Portofino-specific security check passed");
        }
    }

    public static final class AuthChecker extends AnnotationsAuthorizingMethodInterceptor {

        public void assertAuthorized(final Object resource, final Method handler) throws AuthorizationException {
            super.assertAuthorized(new MethodInvocation() {
                @Override
                public Object proceed() {
                    return null;
                }

                @Override
                public Method getMethod() {
                    return handler;
                }

                @Override
                public Object[] getArguments() {
                    return new Object[0];
                }

                @Override
                public Object getThis() {
                    return resource;
                }
            });
        }
    }

    protected static final AuthChecker AUTH_CHECKER = new AuthChecker();

}
