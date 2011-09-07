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

import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.breadcrumbs.Breadcrumbs;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.Page;
import com.manydesigns.portofino.navigation.Navigation;
import com.manydesigns.portofino.system.model.users.UserUtils;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Intercepts(LifecycleStage.CustomValidation)
public class PortofinoInterceptor implements Interceptor {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private static final String LOGIN_ACTION = "login";
    private static final int UNAUTHORIZED = 401;

    public final static Logger logger =
            LoggerFactory.getLogger(PortofinoInterceptor.class);

    protected Map<Class, Map<Class<? extends Annotation>, Field[]>> annotationCache =
            new ConcurrentHashMap<Class, Map<Class<? extends Annotation>, Field[]>>();

    public Resolution intercept(ExecutionContext context) throws Exception {
        logger.debug("Retrieving Stripes objects");
        ActionBeanContext actionContext = context.getActionBeanContext();

        logger.debug("Retrieving Servlet API objects");
        HttpServletRequest request = actionContext.getRequest();
        ServletContext servletContext = actionContext.getServletContext();
        HttpSession session = request.getSession(false);

        logger.debug("Retrieving Portofino long-lived objects");
        Application application =
                (Application) request.getAttribute(
                        RequestAttributes.APPLICATION);
        Model model = application.getModel();
        request.setAttribute(RequestAttributes.MODEL, model);
        
        logger.debug("Starting page response timer");
        StopWatch stopWatch = new StopWatch();
        // Non è necessario stopparlo
        stopWatch.start();
        request.setAttribute(RequestAttributes.STOP_WATCH, stopWatch);

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

        List<String> groups = UserUtils.manageGroups(application, userId);
        request.setAttribute(RequestAttributes.GROUPS, groups);

        logger.debug("Setting skin");
        if(request.getAttribute("skin") == null) {
            request.setAttribute("skin", model.getRootPage().getSkin());
        }

        Dispatch dispatch = (Dispatch) request.getAttribute(RequestAttributes.DISPATCH);
        if (dispatch != null) {
            logger.debug("Creating navigation");
            Navigation navigation = new Navigation(application, dispatch, groups);
            request.setAttribute(RequestAttributes.NAVIGATION, navigation);

            for(PageInstance page : dispatch.getPageInstancePath()) {
                page.realize();
            }
            PageInstance pageInstance = dispatch.getLastPageInstance();
            request.setAttribute(RequestAttributes.PAGE_INSTANCE, pageInstance);

            logger.debug("Creating breadcrumbs");
            Breadcrumbs breadcrumbs = new Breadcrumbs(dispatch);
            request.setAttribute(RequestAttributes.BREADCRUMBS, breadcrumbs);

            Page page = pageInstance.getPage();
            //3. Ho i permessi necessari vado alla pagina
            if(page.isAllowed(groups) && UserUtils.isAllowed(context.getHandler(), request)){
                return context.proceed();
            } else {
                //4. Non ho i permessi, ma non sono loggato, vado alla pagina di login
                if (userId==null){
                    String returnUrl=request.getServletPath();
                    Map parameters = request.getParameterMap();
                    if (parameters.size()!=0){
                        List<String> params = new ArrayList<String>();
                        for (Object key : parameters.keySet()){
                            params.add(key+"="+((String[]) parameters.get(key))[0]);
                        }
                        returnUrl += "?"+ StringUtils.join(params, "&");
                    }
                    UrlBean bean = new UrlBean(returnUrl);
                    /*
                    actionContext.getValueStack().getRoot().push(bean);
                    invocation.getStack().push(bean);
                    */
                    return new ForwardResolution("/user/login.action");
                } else {
                    //5. Non ho i permessi, ma sono loggato, errore 401
                    return new ErrorResolution(UNAUTHORIZED);
                }
            }
        } else {
            return context.proceed();
        }
    }
}
