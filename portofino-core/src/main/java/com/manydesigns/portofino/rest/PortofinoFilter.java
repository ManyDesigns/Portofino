/*
 * Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.elements.servlet.ServletConstants;
import com.manydesigns.portofino.buttons.ButtonsLogic;
import com.manydesigns.portofino.buttons.Guarded;
import com.manydesigns.portofino.cache.ControlsCache;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.dispatcher.PageActionContext;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
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
import java.util.ArrayList;
import java.util.List;

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
            "Copyright (C) 2005-2017 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(PortofinoFilter.class);

    @Context
    protected ResourceInfo resourceInfo;

    @Context
    protected HttpServletRequest request;

    @Context
    protected HttpServletResponse response;

    @Context
    protected ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
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
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            try {
                buildMultipart();
            } catch (FileUploadLimitExceededException e) {
                logger.warn("File upload limit exceeded", e);
            }
        }
        fillMDC();
        logger.debug("Publishing securityUtils in OGNL context");
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ognlContext.put("securityUtils", new SecurityUtilsBean());
        checkAuthorizations(requestContext, resource);
        addHeaders();
        if(resource instanceof PageAction) {
            PageAction pageAction = (PageAction) resource;
            pageAction.prepareForExecution();
        }
    }

    protected void addHeaders() {
        if(resourceInfo.getResourceMethod() != null && resourceInfo.getResourceMethod().isAnnotationPresent(ControlsCache.class)) {
            return;
        }
        // Avoid caching of dynamic pages
        //HTTP 1.0
        response.setHeader(ServletConstants.HTTP_PRAGMA, ServletConstants.HTTP_PRAGMA_NO_CACHE);
        response.setDateHeader(ServletConstants.HTTP_EXPIRES, 0);

        //HTTP 1.1
        response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_CACHE);
        response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_STORE);
        //response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_MUST_REVALIDATE);
        //response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_MAX_AGE + 0);
    }

    protected void buildMultipart() throws IOException, FileUploadLimitExceededException {
        StreamingCommonsMultipartWrapper multipart = new StreamingCommonsMultipartWrapper();
        // Figure out where the temp directory is, and store that info
        File tempDir = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        if (tempDir == null) {
            String tmpDir = System.getProperty("java.io.tmpdir");
            if (tmpDir != null) {
                tempDir = new File(tmpDir).getAbsoluteFile();
            } else {
                logger.warn("The tmpdir system property was null! File uploads will probably fail.");
            }
        }
        long maxPostSize = Long.MAX_VALUE;
        multipart.build(request, tempDir, maxPostSize);
        ElementsThreadLocals.setMultipart(multipart);
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
//        UriInfo uriInfo = requestContext.getUriInfo();
//        if(uriInfo.getMatchedResources().isEmpty()) {
//            return;
//        }
//        Object resource = uriInfo.getMatchedResources().get(0);
//        try {
//            if(resourceInfo == null || resourceInfo.getResourceClass() == null) {
//                return;
//            }
//        } catch (Exception e) {
//            logger.debug("Could not get resourceInfo (can happen under RestEasy)", e);
//            return;
//        }
//        if(resource.getClass() != resourceInfo.getResourceClass()) {
//            throw new RuntimeException("Inconsistency: matched resource is not of the right type, " + resourceInfo.getResourceClass());
//        }
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
        List<PageInstance> pageInstancePath = new ArrayList<>();
        PageInstance last = pageAction.getPageInstance();
        while(last != null) {
            pageInstancePath.add(0, last);
            last = last.getParent();
        }
        Dispatch dispatch = new Dispatch(pageInstancePath.toArray(new PageInstance[pageInstancePath.size()]));
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        if(!SecurityLogic.isAllowed(request, dispatch, pageAction, handler)) {
            Response.Status status =
                    SecurityUtils.getSubject().isAuthenticated() ?
                            Response.Status.FORBIDDEN :
                            Response.Status.UNAUTHORIZED;
            requestContext.abortWith(Response.status(status).build());
        } else if(!ButtonsLogic.doGuardsPass(pageAction, handler)) {
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
                public Object proceed() throws Throwable {
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
