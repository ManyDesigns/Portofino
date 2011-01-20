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
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.model.site.SiteNode;
import com.manydesigns.portofino.navigation.Navigation;
import com.manydesigns.portofino.navigation.NavigationNode;
import com.manydesigns.portofino.servlets.PortofinoListener;
import com.manydesigns.portofino.system.model.users.UserUtils;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang.xwork.StringUtils;
import org.apache.struts2.StrutsStatics;
import org.slf4j.MDC;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> session = actionContext.getSession();
        HttpServletRequest req =
                (HttpServletRequest)actionContext.get(StrutsStatics.HTTP_REQUEST);
        ServletContext servletContext =
                (ServletContext)actionContext.get(StrutsStatics.SERVLET_CONTEXT);
        Context context =
                (Context)servletContext.getAttribute(
                        PortofinoListener.CONTEXT_ATTRIBUTE);
        req.setAttribute(STOP_WATCH_ATTRIBUTE, stopWatch);

        Long userId = (Long) session.get(UserUtils.USERID);
        String userName = (String) session.get(UserUtils.USERNAME);

        try{
            MDC.clear();
            String userIdString =
                    (userId == null) ? null : Long.toString(userId);
            MDC.put(UserUtils.USERID, userIdString);
            MDC.put(UserUtils.USERNAME, userName);

            //1. Non ho modello
            if (context == null || context.getModel() == null) {
                return "modelNotFound";
            }
            context.resetDbTimer();
            context.openSession();
            String requestUrl = Util.getAbsoluteUrl(req.getServletPath());
            if (action instanceof ContextAware) {
                ((ContextAware)action).setContext(context);
            }

            List<String> groups=UserUtils.manageGroups(context, userId);

            Navigation navigation = new Navigation(context, requestUrl, groups);
            req.setAttribute(NAVIGATION_ATTRIBUTE, navigation);
            if (action instanceof NavigationAware) {
                ((NavigationAware)action).setNavigation(navigation);
            }
            NavigationNode selectedNode = navigation.getSelectedNavigationNode();

            //2. Se è fuori dall'albero di navigazione e non ho permessi
            if (selectedNode==null ) {
                stopWatch.stop();
                return invocation.invoke();
            }

            SiteNode node = selectedNode.getActualSiteNode();
            //3. Ho i permessi necessari vado alla pagina
            if(node.isAllowed(groups)){
                stopWatch.stop();
                return invocation.invoke();
            } else {
                //4. Non ho i permessi, ma non sono loggato, vado alla pagina di login
                if (userId==null){
                    stopWatch.stop();
                    String returnUrl=req.getServletPath();
                    Map parameters = req.getParameterMap();
                    if (parameters.size()!=0){                        
                        List<String> params = new ArrayList<String>();
                        for (Object key : parameters.keySet()){
                            params.add(key+"="+((String[]) parameters.get(key))[0]);
                        }
                        returnUrl += "?"+ StringUtils.join(params,  "&");
                    }
                    UrlBean bean = new UrlBean(returnUrl);
                    actionContext.getValueStack().getRoot().push(bean);
                    invocation.getStack().push(bean);
                    return LOGIN_ACTION;
                } else {
                    //5. Non ho i permessi, ma sono loggato, errore 404
                    stopWatch.stop();
                    return UNAUTHORIZED;
                }
            }
        } finally {
            MDC.clear();
            if (context!=null)
                context.closeSession();
        }
    }
}
