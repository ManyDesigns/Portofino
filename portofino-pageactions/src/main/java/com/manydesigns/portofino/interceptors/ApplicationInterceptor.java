/*
 * Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.i18n.TextProviderBean;
import com.manydesigns.portofino.pageactions.PageActionLogic;
import com.manydesigns.portofino.shiro.SecurityUtilsBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import ognl.OgnlContext;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Intercepts(LifecycleStage.CustomValidation)
public class ApplicationInterceptor implements Interceptor {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(ApplicationInterceptor.class);

    public static Resolution dispatch(ActionBeanContext actionContext) throws Exception {
        logger.debug("Publishing textProvider in OGNL context");
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ognlContext.put("textProvider", new TextProviderBean(ElementsThreadLocals.getTextProvider()));

        Dispatch dispatch = DispatcherUtil.getDispatch(actionContext);
        if (dispatch != null) {
            HttpServletRequest request = actionContext.getRequest();
            logger.debug("Preparing PageActions");
            for(PageInstance page : dispatch.getPageInstancePath()) {
                if(page.getParent() == null) {
                    logger.debug("Not preparing root");
                    continue;
                }
                if(page.isPrepared()) {
                    continue;
                }
                logger.debug("Preparing PageAction {}", page);
                PageAction actionBean = page.getActionBean();
                try {
                    actionBean.setContext(actionContext);
                    Resolution resolution = actionBean.preparePage();
                    if(resolution != null) {
                        logger.debug("PageAction prepare returned a resolution: {}", resolution);
                        request.setAttribute(DispatcherLogic.INVALID_PAGE_INSTANCE, page);
                        return resolution;
                    }
                    page.setPrepared(true);
                } catch (Throwable t) {
                    request.setAttribute(DispatcherLogic.INVALID_PAGE_INSTANCE, page);
                    logger.error("PageAction prepare failed for " + page, t);
                    if(!PageActionLogic.isEmbedded(actionBean)) {
                        String msg = MessageFormat.format
                                (ElementsThreadLocals.getText("this.page.has.thrown.an.exception.during.execution"), ExceptionUtils.getRootCause(t));
                        SessionMessages.addErrorMessage(msg);
                    }
                    request.setAttribute("http-error-code", 500);
                    return new ForwardResolution("/m/pageactions/redirect-to-last-working-page.jsp");
                }
            }
            PageInstance pageInstance = dispatch.getLastPageInstance();
            request.setAttribute(RequestAttributes.PAGE_INSTANCE, pageInstance);
        }
        return null;
    }

    public Resolution intercept(ExecutionContext context) throws Exception {
        logger.debug("Retrieving Stripes objects");
        ActionBeanContext actionContext = context.getActionBeanContext();

        logger.debug("Retrieving Servlet API objects");
        HttpServletRequest request = actionContext.getRequest();

        if (request.getDispatcherType() == DispatcherType.REQUEST) {
            logger.debug("Starting page response timer");
            StopWatch stopWatch = new StopWatch();
            // There is no need to stop this timer.
            stopWatch.start();
            request.setAttribute(RequestAttributes.STOP_WATCH, stopWatch);
        }

        Resolution resolution = dispatch(actionContext);
        return resolution != null ? resolution : context.proceed();
    }

}
