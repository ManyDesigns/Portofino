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
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.actions.user.LoginAction;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.pages.Page;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.List;
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

    private static final int UNAUTHORIZED = 401;

    public final static Logger logger =
            LoggerFactory.getLogger(SecurityInterceptor.class);

    public Resolution intercept(ExecutionContext context) throws Exception {
        logger.debug("Retrieving Stripes objects");
        ActionBeanContext actionContext = context.getActionBeanContext();

        logger.debug("Retrieving Servlet API objects");
        HttpServletRequest request = actionContext.getRequest();
        HttpSession session = request.getSession(false);

        logger.debug("Retrieving Portofino application");
        Application application =
                (Application) request.getAttribute(
                        RequestAttributes.APPLICATION);

        logger.debug("Retrieving user");
        String userId = null;
        String userName = null;
        if (session == null) {
            logger.debug("No session found");
        } else {
            userId = (String) session.getAttribute(SessionAttributes.USER_ID);
            userName = (String) session.getAttribute(SessionAttributes.USER_NAME);
            logger.debug("Retrieved userId={} userName={}", userId, userName);
        }

        logger.debug("Setting up logging MDC");
        MDC.clear();
        MDC.put(SessionAttributes.USER_ID, ObjectUtils.toString(userId));
        MDC.put(SessionAttributes.USER_NAME, userName);

        logger.debug("Retrieving groups");
        List<String> groups = SecurityLogic.manageGroups(application, userId);
        request.setAttribute(RequestAttributes.GROUPS, groups);

        logger.debug("Checking if action or method required administrator");
        boolean requiresAdministrator = false;
        Method handler = context.getHandler();
        if (handler.isAnnotationPresent(RequiresAdministrator.class)) {
            logger.debug("Action method requires administrator: {}", handler);
            requiresAdministrator = true;
        } else {
            Class actionClass = context.getActionBean().getClass();
            while (actionClass != null) {
                if (actionClass.isAnnotationPresent(RequiresAdministrator.class)) {
                    logger.debug("Action class requires administrator: {}",
                    actionClass);
                    requiresAdministrator = true;
                    break;
                }
                actionClass = actionClass.getSuperclass();
            }
        }

        if (requiresAdministrator && !SecurityLogic.isAdministrator(request)) {
            logger.info("User is not an administrator");
            return handleAnonymousOrUnauthorized(userId, request);
        }

        logger.debug("Checking page permissions");
        Dispatch dispatch =
                (Dispatch) request.getAttribute(RequestAttributes.DISPATCH);
        if (dispatch != null) {
            PageInstance pageInstance = dispatch.getLastPageInstance();
            Page page = pageInstance.getPage();
            if (!page.isAllowed(groups)){
                logger.info("User does not match page permissions. User's groups: {}",
                        ArrayUtils.toString(groups));
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
                    .addParameter("returnUrl", returnUrl);
        } else {
            logger.warn("User not authorized.");
            return new ErrorResolution(UNAUTHORIZED);
        }
    }
}
