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

package com.manydesigns.portofino.interceptors;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.shiro.SecurityUtilsBean;
import com.manydesigns.portofino.shiro.ShiroUtils;
import com.manydesigns.portofino.stripes.AuthenticationRequiredResolution;
import com.manydesigns.portofino.stripes.ForbiddenAccessResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
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

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Intercepts(LifecycleStage.BindingAndValidation)
public class ShiroInterceptor implements Interceptor {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(ShiroInterceptor.class);

    public Resolution intercept(final ExecutionContext context) throws Exception {
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

        logger.debug("Publishing securityUtils in OGNL context");
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ognlContext.put("securityUtils", new SecurityUtilsBean());

        logger.debug("Setting up logging MDC");
        MDC.clear();
        if(userId != null) { //Issue #755
            MDC.put("userId", userId.toString());
        }
        if(context.getActionBeanContext() != null && context.getActionBeanContext().getRequest() != null) {
            MDC.put("req.requestURI", context.getActionBeanContext().getRequest().getRequestURI());
        }

        try {
            AUTH_CHECKER.assertAuthorized(context);
            logger.debug("Security check passed.");
            return context.proceed();
        } catch (UnauthenticatedException e) {
            logger.debug("Method required authentication", e);
            return new AuthenticationRequiredResolution();
        } catch (AuthorizationException e) {
            logger.warn("Method invocation not authorized", e);
            return new ForbiddenAccessResolution(e.getMessage());
        }
    }

    public static final class AuthChecker extends AnnotationsAuthorizingMethodInterceptor {

        public void assertAuthorized(final ExecutionContext context) throws AuthorizationException {
            super.assertAuthorized(new MethodInvocation() {
                @Override
                public Object proceed() throws Throwable {
                    return null;
                }

                @Override
                public Method getMethod() {
                    return context.getHandler();
                }

                @Override
                public Object[] getArguments() {
                    return new Object[0];
                }

                @Override
                public Object getThis() {
                    return context.getActionBean();
                }
            });
        }
    }

    protected static final AuthChecker AUTH_CHECKER = new AuthChecker();

}
