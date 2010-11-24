/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.user.LoginUnAware;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.navigation.Navigation;
import com.manydesigns.portofino.servlets.PortofinoListener;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.apache.commons.lang.time.StopWatch;
import org.apache.struts2.StrutsStatics;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PortofinoInterceptor implements Interceptor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String STOP_WATCH_ATTRIBUTE = "stopWatch";
    public final static String NAVIGATION_ATTRIBUTE = "navigation";
    private static final String LOGIN_ACTION = "login";
    private static final String UNAUTHORIZED = "unauthorized";

    public void destroy() {}

    public void init() {}

    public String intercept(ActionInvocation invocation) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object action = invocation.getAction();
        ActionContext actionContext = invocation.getInvocationContext();
        HttpServletRequest req =
                (HttpServletRequest)actionContext.get(StrutsStatics.HTTP_REQUEST);
        ServletContext servletContext =
                (ServletContext)actionContext.get(StrutsStatics.SERVLET_CONTEXT);
        Context context =
                (Context)servletContext.getAttribute(
                        PortofinoListener.CONTEXT_ATTRIBUTE);

        if (context == null || context.getModel() == null) {
            return "modelNotFound";
        }
        context.openSession();
        req.setAttribute(STOP_WATCH_ATTRIBUTE, stopWatch);

        String requestUrl = Util.getAbsoluteUrl(req.getServletPath());
        Navigation navigation = new Navigation(context, requestUrl);
        req.setAttribute(NAVIGATION_ATTRIBUTE, navigation);

        if (action instanceof ContextAware) {
            ((ContextAware)action).setContext(context);
        }

        if (action instanceof NavigationAware) {
            ((NavigationAware)action).setNavigation(navigation);
        }

        String result;
        try {
            context.resetDbTimer();


            boolean userEnabled = Boolean.parseBoolean(
                    PortofinoProperties.getProperties()
                    .getProperty("user.enabled", "false"));
            if (userEnabled &&
                    context.getCurrentUserId()==null
                    && !(invocation.getAction() instanceof LoginUnAware)) {
                return LOGIN_ACTION;
            }
            result = invocation.invoke();

        } finally {
            context.closeSession();
        }

        stopWatch.stop();

        return result;
        /*
        try {
            context.resetDbTimer();
            context.openSession();
            String requestUrl = Util.getAbsoluteUrl(req.getServletPath());
            Navigation navigation = new Navigation(context, requestUrl);
            req.setAttribute(NAVIGATION_ATTRIBUTE, navigation);

            if (action instanceof ContextAware) {
                ((ContextAware)action).setContext(context);
            }

            if (action instanceof NavigationAware) {
                ((NavigationAware)action).setNavigation(navigation);
            }

            NavigationNode selectedNode = navigation.getSelectedNavigationNode();

            if (!(invocation.getAction() instanceof PortofinoAction)
                    ||selectedNode==null) {
                stopWatch.stop();
                return invocation.invoke();
            }

            List<String> groups = new ArrayList<String>();

            SiteNode node =
                        selectedNode.getActualSiteNode();

            if (context.getCurrentUser()==null) {
                groups.add(Group.ANONYMOUS);
                if (node.isAllowed(groups)) {
                    stopWatch.stop();
                    return invocation.invoke();
                } else {
                    stopWatch.stop();
                    return LOGIN_ACTION;
                }
            } else {
                if(node.isAllowed(UserUtils.manageGroups(context))){
                    stopWatch.stop();
                    return invocation.invoke();
                } else {
                    stopWatch.stop();
                    return UNAUTHORIZED;
                }
            }


        } finally {
            context.closeSession();
        }*/

    }
}
