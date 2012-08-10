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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.i18n.SimpleTextProvider;
import com.manydesigns.elements.i18n.TextProvider;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.i18n.MultipleTextProvider;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ApplicationFilter implements Filter {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected ServletContext servletContext;

        public final static Logger logger =
            LoggerFactory.getLogger(ApplicationFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // cast to http type
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        logger.debug("Retrieving application starter");
        ApplicationStarter applicationStarter =
                (ApplicationStarter) servletContext.getAttribute(
                        ApplicationAttributes.APPLICATION_STARTER);

        logger.debug("Retrieving application");
        Application application;
        try {
            application = applicationStarter.getApplication();
        } catch (Exception e) {
            throw new ServletException(e);
        }
        request.setAttribute(RequestAttributes.APPLICATION, application);
        if (application != null) {
            Model model = application.getModel();
            request.setAttribute(RequestAttributes.MODEL, model);

            //I18n
            Locale locale = request.getLocale();
            LocalizationContext localizationContext =
                    new LocalizationContext(application.getBundle(locale), locale);
            request.setAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".request", localizationContext);

            //Setup Elements I18n
            ResourceBundle elementsResourceBundle =
                    ResourceBundle.getBundle(SimpleTextProvider.DEFAULT_MESSAGE_RESOURCE, locale);
            ResourceBundle portofinoResourceBundle = application.getBundle(locale);

            TextProvider textProvider =
                    new MultipleTextProvider(
                            portofinoResourceBundle, elementsResourceBundle);
            ElementsThreadLocals.setTextProvider(textProvider);

            String appPathPrefix = httpRequest.getContextPath() + "/app/";
            if(httpRequest.getRequestURI().startsWith(appPathPrefix)) {
                String internalPath =
                        "/apps/" + application.getAppId() + "/" +
                        httpRequest.getRequestURI().substring(appPathPrefix.length());
                RequestDispatcher requestDispatcher = request.getRequestDispatcher(internalPath);
                requestDispatcher.forward(request, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    public void destroy() {
        servletContext = null;
    }
}
