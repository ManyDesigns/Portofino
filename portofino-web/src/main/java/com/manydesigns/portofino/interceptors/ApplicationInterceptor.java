/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.AppProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.pageactions.PageActionLogic;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Intercepts(LifecycleStage.CustomValidation)
public class ApplicationInterceptor implements Interceptor {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(ApplicationInterceptor.class);
    public static final String INVALID_PAGE_INSTANCE = "validDispatchPathLength";

    public Resolution intercept(ExecutionContext context) throws Exception {
        logger.debug("Retrieving Stripes objects");
        ActionBeanContext actionContext = context.getActionBeanContext();

        logger.debug("Retrieving Servlet API objects");
        HttpServletRequest request = actionContext.getRequest();

        logger.debug("Retrieving Portofino application");
        Application application =
                (Application) request.getAttribute(
                        RequestAttributes.APPLICATION);

        logger.debug("Starting page response timer");
        StopWatch stopWatch = new StopWatch();
        // There is no need to stop this timer.
        stopWatch.start();
        request.setAttribute(RequestAttributes.STOP_WATCH, stopWatch);

        logger.debug("Setting skin");
        if(request.getAttribute(RequestAttributes.SKIN) == null) {
            String skin = application.getAppConfiguration().getString(AppProperties.SKIN);
            request.setAttribute(RequestAttributes.SKIN, skin);
        }

        logger.debug("Setting blobs directory");
        BlobManager blobManager = ElementsThreadLocals.getBlobManager();
        blobManager.setBlobsDir(application.getAppBlobsDir());

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
                configureActionBean(actionBean, page, application);
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
                        Locale locale = request.getLocale();
                        ResourceBundle resourceBundle = application.getBundle(locale);
                        String msg = MessageFormat.format
                                (resourceBundle.getString("portlet.exception"), ExceptionUtils.getRootCause(t));
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
            (PageAction actionBean, PageInstance pageInstance, Application application)
            throws JAXBException, IOException {
        if(pageInstance.getConfiguration() != null) {
            logger.debug("Page instance {} is already configured");
            return;
        }
        File configurationFile = new File(pageInstance.getDirectory(), "configuration.xml");
        Class<?> configurationClass = PageActionLogic.getConfigurationClass(actionBean.getClass());
        try {
            Object configuration =
                    DispatcherLogic.getConfiguration(configurationFile, application, configurationClass);
            pageInstance.setConfiguration(configuration);
        } catch (Throwable t) {
            logger.error("Couldn't load configuration from " + configurationFile.getAbsolutePath(), t);
        }
    }

}
