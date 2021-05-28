/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.servlet.ServletConstants;
import com.manydesigns.portofino.cache.ControlsCache;
import com.manydesigns.portofino.operations.Guarded;
import com.manydesigns.portofino.operations.Operations;
import com.manydesigns.portofino.resourceactions.ResourceAction;
import com.manydesigns.portofino.resourceactions.log.LogAccesses;
import com.manydesigns.portofino.security.SecurityFacade;
import com.manydesigns.portofino.security.noop.NoSecurity;
import ognl.OgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
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
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public static final String ACCESS_LOGGER_NAME = "com.manydesigns.portofino.access";
    public static final String MESSAGE_HEADER = "X-Portofino-Message";
    public static final String PORTOFINO_API_VERSION_HEADER = "X-Portofino-API-Version";
    private static final Logger logger = LoggerFactory.getLogger(PortofinoFilter.class);
    private static final Logger accessLogger = LoggerFactory.getLogger(ACCESS_LOGGER_NAME);
    public static final String PORTOFINO_API_VERSION = "5.2";

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


        logger.debug("Setting up logging MDC");
        MDC.clear();
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        if(request != null) {
            MDC.put("req.requestURI", request.getRequestURI());
        }
        if(resource instanceof ResourceAction) {
            ResourceAction resourceAction = (ResourceAction) resource;
            logger.debug("Retrieving user");
            Object userId = resourceAction.getSecurity().getUserId();
            if(userId != null) { //Issue #755
                MDC.put("userId", userId.toString());
            }
            OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
            ognlContext.put("securityUtils", resourceAction.getSecurity().getSecurityUtilsBean());
            resourceAction.prepareForExecution();
        }
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        SecurityFacade facade = NoSecurity.AT_ALL;
        if(context != null) {
            try {
                facade = context.getBean(SecurityFacade.class);
            } catch (NoSuchBeanDefinitionException e) {
                //Not found, let's use default
            }
        }
        facade.checkWebResourceIsAccessible(requestContext, resource, resourceInfo.getResourceMethod());
        if(resource instanceof ResourceAction) {
            checkResourceActionInvocation(requestContext, (ResourceAction) resource);
        }
        Method resourceMethod = resourceInfo.getResourceMethod();
        if(isAccessToBeLogged(resource, resourceMethod)) {
            accessLogger.info(
                    requestContext.getMethod() + " " + resourceMethod.getName() +
                    ", queryString " + request.getQueryString());
        }
    }

    public static boolean isAccessToBeLogged(Object resource, Method handler) {
        if (resource != null) {
            Boolean log = null;
            Class<?> resourceClass = resource.getClass();

            LogAccesses annotation;
            if(handler != null) {
                annotation = handler.getAnnotation(LogAccesses.class);
                if(annotation != null) {
                    log = annotation.value();
                }
            }
            if(log == null) {
                annotation = resourceClass.getAnnotation(LogAccesses.class);
                log = (annotation != null && annotation.value());
            }
            return log;
        }
        return false;
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
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        for(String message : RequestMessages.consumeErrorMessages()) {
            headers.add(MESSAGE_HEADER, "error: " + message);
        }
        for(String message : RequestMessages.consumeWarningMessages()) {
            headers.add(MESSAGE_HEADER, "warning: " + message);
        }
        for(String message : RequestMessages.consumeInfoMessages()) {
            headers.add(MESSAGE_HEADER, "info: " + message);
        }
        if(!headers.containsKey(PORTOFINO_API_VERSION_HEADER)) { //Give a chance to actions to declare a different version
            headers.putSingle(PORTOFINO_API_VERSION_HEADER, PORTOFINO_API_VERSION);
        }
    }



    protected void checkResourceActionInvocation(ContainerRequestContext requestContext, ResourceAction resourceAction) {
        Method handler = resourceInfo.getResourceMethod();
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        if(!resourceAction.getSecurity().isOperationAllowed(request, resourceAction.getActionInstance(), resourceAction, handler) ||
           !resourceAction.isAccessible()) {
            logger.warn("Request not allowed: " + request.getMethod() + " " + request.getRequestURI());
            Response.Status status =
                    resourceAction.getSecurity().isUserAuthenticated() ?
                            Response.Status.FORBIDDEN :
                            Response.Status.UNAUTHORIZED;
            requestContext.abortWith(Response.status(status).build());
        } else if(!Operations.doGuardsPass(resourceAction, handler)) {
            if(resourceAction instanceof Guarded) {
                Response response = ((Guarded) resourceAction).guardsFailed(handler);
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

}
