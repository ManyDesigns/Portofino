/*
 * Copyright (C) 2016 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.dispatcher.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class ShiroResourceFilter implements ContainerRequestFilter {

    protected final static Logger logger = LoggerFactory.getLogger(ShiroResourceFilter.class);

    @Context
    protected ResourceInfo resourceInfo;

    @Context
    protected HttpServletResponse response;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        if(uriInfo.getMatchedResources().isEmpty()) {
            logger.debug("No resources matched {}", uriInfo.getPath());
            return;
        }
        if(resourceInfo == null || resourceInfo.getResourceClass() == null) {
            logger.debug("No resource info: {}", resourceInfo);
            return;
        }
        Object resource = uriInfo.getMatchedResources().get(0);

        checkAuthorizations(requestContext, resource);
    }

    protected void checkAuthorizations(ContainerRequestContext requestContext, Object resource) {
        try {
            Method handler = resourceInfo.getResourceMethod();
            AUTH_CHECKER.assertAuthorized(resource, handler);
            logger.debug("Security check passed.");
        } catch (AuthorizationException e) {
            logger.warn("Method invocation not authorized", e);
            if(SecurityUtils.getSubject().isAuthenticated()) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            } else {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        }
    }

    public static final class AuthChecker extends AnnotationsAuthorizingMethodInterceptor {

        public AuthChecker() {
            methodInterceptors = new ArrayList<>(5);
            methodInterceptors.add(new RoleAnnotationMethodInterceptor());
            methodInterceptors.add(new ResourceMethodInterceptor());
            methodInterceptors.add(new AuthenticatedAnnotationMethodInterceptor());
            methodInterceptors.add(new UserAnnotationMethodInterceptor());
            methodInterceptors.add(new GuestAnnotationMethodInterceptor());
        }

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
                    return new Object[handler.getParameterTypes().length];
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
