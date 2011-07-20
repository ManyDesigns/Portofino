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

import com.manydesigns.portofino.annotations.*;
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
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String STOP_WATCH_ATTRIBUTE = "stopWatch";
    public final static String NAVIGATION_ATTRIBUTE = "navigation";
    private static final String LOGIN_ACTION = "login";
    private static final int UNAUTHORIZED = 401;

    public final static Logger logger =
            LoggerFactory.getLogger(PortofinoInterceptor.class);

    protected Map<Class, Map<Class<? extends Annotation>, Field[]>> annotationCache =
            new ConcurrentHashMap<Class, Map<Class<? extends Annotation>, Field[]>>();

    public Resolution intercept(ExecutionContext context) throws Exception {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();


        Object action = context.getActionBean();
        ActionBeanContext actionContext = context.getActionBeanContext();
        HttpServletRequest req = actionContext.getRequest();
        HttpSession session = req.getSession(false);
        ServletContext servletContext = actionContext.getServletContext();
        Application application = (Application)servletContext.getAttribute(Application.KEY);
        req.setAttribute(STOP_WATCH_ATTRIBUTE, stopWatch);
        if(req.getAttribute("skin") == null) {
            req.setAttribute("skin", "default");
        }

        Long userId = null;
        String userName = null;
        if (session != null) {
            userId = (Long) session.getAttribute(UserUtils.USERID);
            userName = (String) session.getAttribute(UserUtils.USERNAME);
        }

        MDC.clear();
        String userIdString =
                (userId == null) ? null : Long.toString(userId);
        MDC.put(UserUtils.USERID, userIdString);
        MDC.put(UserUtils.USERNAME, userName);

        //1. Non ho modello
        if (application == null || application.getModel() == null) {
            return new ForwardResolution("/errors/model-not-found.jsp");
        }
        application.resetDbTimer();
        application.openSession();
        Model model = application.getModel();

        List<String> groups=UserUtils.manageGroups(application, userId);

        Dispatch dispatch = (Dispatch) req.getAttribute(Dispatch.KEY);
        Navigation navigation = new Navigation(application, dispatch, groups);
        req.setAttribute(NAVIGATION_ATTRIBUTE, navigation);

        ServerInfo serverInfo =
                (ServerInfo) servletContext.getAttribute(ServerInfo.KEY);

        /* injections */
        injectAnnotatedFields(action, InjectApplication.class, application);
        injectAnnotatedFields(action, InjectModel.class, model);
        injectAnnotatedFields(action, InjectDispatch.class, dispatch);
        SiteNodeInstance siteNodeInstance;
        if(dispatch != null) {
            SiteNodeInstance[] siteNodeInstances = dispatch.getSiteNodeInstancePath();
            for(SiteNodeInstance node : siteNodeInstances) {
                node.realize();
            }
            siteNodeInstance =  siteNodeInstances[siteNodeInstances.length-1];
            injectAnnotatedFields(action, InjectSiteNodeInstance.class,
                    siteNodeInstance);
        } else {
            siteNodeInstance = null;
        }
        injectAnnotatedFields(action, InjectNavigation.class, navigation);
        injectAnnotatedFields(action, InjectServerInfo.class, serverInfo);
        injectAnnotatedFields(action, InjectHttpRequest.class, req);
        injectAnnotatedFields(action, InjectHttpSession.class, session);


        //2. Se è fuori dall'albero di navigazione e non ho permessi
        if (siteNodeInstance == null ) {
            stopWatch.stop();
            return context.proceed();
        }

        SiteNode node = siteNodeInstance.getSiteNode();
        //3. Ho i permessi necessari vado alla pagina
        if(node.isAllowed(groups)){
            stopWatch.stop();
            return context.proceed();
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
                stopWatch.stop();
                return new ErrorResolution(UNAUTHORIZED);
            }
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
