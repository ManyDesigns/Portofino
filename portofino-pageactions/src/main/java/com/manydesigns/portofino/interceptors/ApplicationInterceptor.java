/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.AppProperties;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.pageactions.PageActionLogic;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(ApplicationInterceptor.class);
    public static final String INVALID_PAGE_INSTANCE = "validDispatchPathLength";

    public Resolution intercept(ExecutionContext context) throws Exception {
        logger.debug("Retrieving Stripes objects");
        ActionBeanContext actionContext = context.getActionBeanContext();

        logger.debug("Retrieving Servlet API objects");
        HttpServletRequest request = actionContext.getRequest();

        logger.debug("Retrieving Portofino configuration");
        ServletContext servletContext = request.getServletContext();
        Configuration configuration =
                (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);

        logger.debug("Starting page response timer");
        StopWatch stopWatch = new StopWatch();
        // There is no need to stop this timer.
        stopWatch.start();
        request.setAttribute(RequestAttributes.STOP_WATCH, stopWatch);

        logger.debug("Setting skin");
        if(request.getAttribute(RequestAttributes.SKIN) == null) {
            String skin = configuration.getString(AppProperties.SKIN);
            request.setAttribute(RequestAttributes.SKIN, skin);
        }

        Dispatch dispatch = DispatcherUtil.getDispatch(request);
        if (dispatch != null) {
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
                PageAction actionBean = ensureActionBean(page);
                configureActionBean(actionBean, page, request);
                try {
                    actionBean.setContext(actionContext);
                    actionBean.setDispatch(dispatch);
                    actionBean.setPageInstance(page);
                    Resolution resolution = actionBean.preparePage();
                    if(resolution != null) {
                        logger.debug("PageAction prepare returned a resolution: {}", resolution);
                        request.setAttribute(INVALID_PAGE_INSTANCE, page);
                        return resolution;
                    }
                    page.setPrepared(true);
                } catch (Throwable t) {
                    request.setAttribute(INVALID_PAGE_INSTANCE, page);
                    logger.error("PageAction prepare failed for " + page, t);
                    if(!actionBean.isEmbedded()) {
                        String msg = MessageFormat.format
                                (ElementsThreadLocals.getText("portlet.exception"), ExceptionUtils.getRootCause(t));
                        SessionMessages.addErrorMessage(msg);
                    }
                    return new ForwardResolution("/layouts/redirect-to-last-working-page.jsp");
                }
            }
            PageInstance pageInstance = dispatch.getLastPageInstance();
            request.setAttribute(RequestAttributes.PAGE_INSTANCE, pageInstance);
        }

        return context.proceed();
    }

    protected PageAction ensureActionBean(PageInstance page) throws IllegalAccessException, InstantiationException {
        PageAction action = page.getActionBean();
        if(action == null) {
            action = page.getActionClass().newInstance();
            page.setActionBean(action);
        }
        return action;
    }

    protected void configureActionBean
            (PageAction actionBean, PageInstance pageInstance, HttpServletRequest request)
            throws JAXBException, IOException {
        Injections.inject(actionBean, request.getServletContext(), request);

        if(pageInstance.getConfiguration() != null) {
            logger.debug("Page instance {} is already configured");
            return;
        }
        File configurationFile = new File(pageInstance.getDirectory(), "configuration.xml");
        Class<?> configurationClass = PageActionLogic.getConfigurationClass(actionBean.getClass());
        try {
            Object configuration =
                    DispatcherLogic.getConfiguration(configurationFile, configurationClass);
            pageInstance.setConfiguration(configuration);
        } catch (Throwable t) {
            logger.error("Couldn't load configuration from " + configurationFile.getAbsolutePath(), t);
        }
    }

}
