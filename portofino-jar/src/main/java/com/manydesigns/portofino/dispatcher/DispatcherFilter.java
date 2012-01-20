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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.i18n.TextProvider;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.ApplicationStarter;
import com.manydesigns.portofino.i18n.PortofinoTextProvider;
import com.manydesigns.portofino.model.datamodel.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.IOException;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DispatcherFilter implements Filter {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(DispatcherFilter.class);

    protected FilterConfig filterConfig;
    protected ServletContext servletContext;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        servletContext = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain
    ) throws IOException, ServletException {
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
            TextProvider textProvider = new PortofinoTextProvider(application, locale);
            ElementsThreadLocals.setTextProvider(textProvider);
        }

        logger.debug("Invoking the dispatcher to create a dispatch");
        Dispatcher dispatcher = new Dispatcher(application);
        Dispatch dispatch = dispatcher.createDispatch(httpRequest);

        if (dispatch == null) {
            logger.debug("We can't handle this path ({})." +
                    " Let's do the normal filter chain",
                    httpRequest.getServletPath());
            chain.doFilter(request, response);
            return;
        }

        Map<String, Object> savedAttributes =
                saveAndResetRequestAttributes(request);
        request.setAttribute(RequestAttributes.DISPATCH, dispatch);
        try {
            //Handle through the ModelActionResolver
            chain.doFilter(request, response);
        } finally {
            restoreRequestAttributes(request, savedAttributes);
        }
    }

    private void restoreRequestAttributes(ServletRequest request,
                                          Map<String, Object> savedAttributes) {
        List<String> attrNamesToRemove = new ArrayList<String>();
        Enumeration attrNames = request.getAttributeNames();
        while(attrNames.hasMoreElements()) {
            attrNamesToRemove.add((String) attrNames.nextElement());
        }
        for(String attrName : attrNamesToRemove) {
            request.removeAttribute(attrName);
        }
        for(Map.Entry<String, Object> entry : savedAttributes.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, Object> saveAndResetRequestAttributes(ServletRequest request) {
        Map<String,Object> savedAttributes = new HashMap<String, Object>();
        logger.debug("--- start req dump ---");
        Enumeration attrNames = request.getAttributeNames();
        while(attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();
            Object attrValue = request.getAttribute(attrName);
            logger.debug("{} = {}", attrName, attrValue);
            savedAttributes.put(attrName, attrValue);
        }
        for(String attrName : savedAttributes.keySet()) {
            if(attrName.startsWith("javax.servlet")) {
                continue;
            }
            if(attrName.equals("cancelReturnUrl")) {
                continue;
            }
            if(attrName.equals(RequestAttributes.APPLICATION)) {
                continue;
            }
            if(attrName.equals(RequestAttributes.MODEL)) {
                continue;
            }
            request.removeAttribute(attrName);
        }
        logger.debug("--- end req dump ---");
        return savedAttributes;
    }

    public void destroy() {
        filterConfig = null;
        servletContext = null;
    }
}