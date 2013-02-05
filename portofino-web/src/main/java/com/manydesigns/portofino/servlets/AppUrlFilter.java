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
public class AppUrlFilter implements Filter {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected ServletContext servletContext;

        public final static Logger logger =
            LoggerFactory.getLogger(AppUrlFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // cast to http type
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        logger.debug("Retrieving application");
        Application application =
                (Application) request.getAttribute(RequestAttributes.APPLICATION);
        if (application != null) {
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
