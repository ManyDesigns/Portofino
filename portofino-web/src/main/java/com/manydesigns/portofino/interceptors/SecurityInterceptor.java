/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.interceptors;

import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.portofino.actions.user.LoginAction;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.RequestAttributes;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.pages.Permissions;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@Intercepts(LifecycleStage.CustomValidation)
public class
        SecurityInterceptor implements Interceptor {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private static final int UNAUTHORIZED = 403;

    public final static Logger logger =
            LoggerFactory.getLogger(SecurityInterceptor.class);

    public Resolution intercept(ExecutionContext context) throws Exception {
        logger.debug("Retrieving Stripes objects");
        ActionBeanContext actionContext = context.getActionBeanContext();
        ActionBean actionBean = context.getActionBean();
        Method handler = context.getHandler();

        logger.debug("Retrieving Servlet API objects");
        HttpServletRequest request = actionContext.getRequest();

        logger.debug("Retrieving Portofino application");
        Application application =
                (Application) request.getAttribute(
                        RequestAttributes.APPLICATION);

        logger.debug("Retrieving user");
        String userId = null;
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            userId = subject.getPrincipal().toString();
            logger.debug("Retrieved userId={}", userId);
        } else {
            logger.debug("No user found");
        }

        logger.debug("Setting up logging MDC");
        MDC.clear();
        MDC.put("userId", userId);
        //MDC.put(SessionAttributes.USER_NAME, userName); TODO

        if (!SecurityLogic.satisfiesRequiresAdministrator(request, actionBean, handler)) {
            return handleAnonymousOrUnauthorized(userId, request);
        }

        logger.debug("Checking page permissions");
        boolean isNotAdmin = !SecurityLogic.isAdministrator(request);
        if (isNotAdmin) {
            Permissions permissions;
            Dispatch dispatch =
                (Dispatch) request.getAttribute(RequestAttributes.DISPATCH);
            String resource;
            boolean allowed;
            if(dispatch != null) {
                resource = dispatch.getLastPageInstance().getPath();
                allowed = SecurityLogic.hasPermissions(dispatch, subject, handler);
            } else {
                resource = request.getRequestURI();
                permissions = new Permissions();
                allowed = SecurityLogic.hasPermissions
                        (application, permissions, subject, handler, actionBean.getClass());
            }
            if(!allowed) {
                logger.info("User {} is not allowed for {}", userId, resource);
                return handleAnonymousOrUnauthorized(userId, request);
            }
        }

        logger.debug("Security check passed.");
        return context.proceed();
    }

    private Resolution handleAnonymousOrUnauthorized(
            String userId, HttpServletRequest request) {
        if (userId == null){
            logger.info("Anonymous user not allowed. Redirecting to login.");
            String originalPath = ServletUtils.getOriginalPath(request);
            UrlBuilder urlBuilder =
                    new UrlBuilder(Locale.getDefault(), originalPath, false);
            Map parameters = request.getParameterMap();
            urlBuilder.addParameters(parameters);
            String returnUrl = urlBuilder.toString();

            return new RedirectResolution(LoginAction.class)
                    .addParameter("returnUrl", returnUrl)
                    .addParameter("cancelReturnUrl", "/");
        } else {
            logger.warn("User {} not authorized.", userId);
            return new ErrorResolution(UNAUTHORIZED);
        }
    }
}
