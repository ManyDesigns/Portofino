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

import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.annotations.*;
import com.manydesigns.portofino.breadcrumbs.Breadcrumbs;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.context.ServerInfo;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.SiteNodeInstance;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.site.SiteNode;
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
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        Object action = context.getActionBean();
        ActionBeanContext actionContext = context.getActionBeanContext();

        logger.debug("Retrieving and injecting Servlet API objects");
        HttpServletRequest request = actionContext.getRequest();
        HttpServletResponse response = actionContext.getResponse();
        ServletContext servletContext = actionContext.getServletContext();
        HttpSession session = request.getSession(false);
        injectAnnotatedFields(action, InjectHttpRequest.class, request);
        injectAnnotatedFields(action, InjectHttpResponse.class, response);
        injectAnnotatedFields(action, InjectHttpSession.class, session);

        logger.debug("Retrieving and injecting Portofino long-lived objects");
        Application application =
                (Application)servletContext.getAttribute(
                        ApplicationAttributes.APPLICATION);
        Model model = application.getModel();
        ServerInfo serverInfo =
                (ServerInfo) servletContext.getAttribute(
                        ApplicationAttributes.SERVER_INFO);
        Configuration portofinoConfiguration =
                application.getPortofinoProperties();
        injectAnnotatedFields(action, InjectApplication.class, application);
        injectAnnotatedFields(action, InjectModel.class, model);
        injectAnnotatedFields(action, InjectServerInfo.class, serverInfo);
        injectAnnotatedFields(action, InjectPortofinoProperties.class,
                portofinoConfiguration);

        logger.debug("Starting page response timer");
        StopWatch stopWatch = new StopWatch();
        // Non è necessario stopparlo
        stopWatch.start();
        request.setAttribute(RequestAttributes.STOP_WATCH, stopWatch);

        logger.debug("Retrieving user");
        Long userId = null;
        String userName = null;
        if (session == null) {
            logger.debug("No session found");
        } else {
            userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
            userName = (String) session.getAttribute(SessionAttributes.USER_NAME);
            logger.debug("Retrieved userId={} userName={}", userId, userName);
        }

        logger.debug("Setting up logging MDC");
        MDC.clear();
        MDC.put(SessionAttributes.USER_ID, ObjectUtils.toString(userId));
        MDC.put(SessionAttributes.USER_NAME, userName);

        List<String> groups=UserUtils.manageGroups(application, userId);

        logger.debug("Setting default skin");
        if(request.getAttribute("skin") == null) {
            request.setAttribute("skin", "default");
        }

        Dispatch dispatch = (Dispatch) request.getAttribute(RequestAttributes.DISPATCH);
        if (dispatch != null) {
            logger.debug("Creating navigation");
            Navigation navigation = new Navigation(dispatch, groups);
            request.setAttribute(RequestAttributes.NAVIGATION, navigation);

            SiteNodeInstance[] siteNodeInstances = dispatch.getSiteNodeInstancePath();
            for(SiteNodeInstance node : siteNodeInstances) {
                node.realize();
            }
            SiteNodeInstance siteNodeInstance =
                    siteNodeInstances[siteNodeInstances.length-1];

            logger.debug("Creating breadcrumbs");
            Breadcrumbs breadcrumbs = new Breadcrumbs(dispatch);
            request.setAttribute(RequestAttributes.BREADCRUMBS, breadcrumbs);

            injectAnnotatedFields(action, InjectDispatch.class, dispatch);
            injectAnnotatedFields(action, InjectSiteNodeInstance.class,
                    siteNodeInstance);
            injectAnnotatedFields(action, InjectNavigation.class, navigation);

            SiteNode node = siteNodeInstance.getSiteNode();
            //3. Ho i permessi necessari vado alla pagina
            if(node.isAllowed(groups)){
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
                    return new ForwardResolution("/skins/default/user/login.jsp");
                } else {
                    //5. Non ho i permessi, ma sono loggato, errore 401
                    return new ErrorResolution(UNAUTHORIZED);
                }
            }
        } else {
            return context.proceed();
        }
    }

    public void injectAnnotatedFields(Object object,
                                       Class<? extends Annotation> annotation,
                                       Object value) {
        Class clazz = object.getClass();
        Field[] annotatedFields = findAnnotatedFields(clazz, annotation);
        for (Field field : annotatedFields) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                String msg = String.format(
                        "Cannot inject object %s, field %s, annotation %s with value %s",
                        ObjectUtils.toString(object),
                        field.getName(),
                        annotation,
                        ObjectUtils.toString(value));
                logger.warn(msg, e);
            }
        }
    }

    public Field[] findAnnotatedFields(Class clazz,
                                       Class<? extends Annotation> annotation) {
        Map<Class<? extends Annotation>, Field[]> annotationMap =
                annotationCache.get(clazz);
        if (annotationMap == null) {
            annotationMap = new ConcurrentHashMap<Class<? extends Annotation>, Field[]>();
            annotationCache.put(clazz, annotationMap);
        }

        Field[] result = annotationMap.get(annotation);

        if (result != null) {
            return result;
        }

        List<Field> foundFields = new ArrayList<Field>();
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(annotation)) {
                field.setAccessible(true);
                foundFields.add(field);
            }
        }
        result = new Field[foundFields.size()];
        foundFields.toArray(result);
        return result;
    }


}
