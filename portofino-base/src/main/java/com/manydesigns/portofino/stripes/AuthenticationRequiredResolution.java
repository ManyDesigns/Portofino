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

package com.manydesigns.portofino.stripes;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.stripes.ElementsActionBeanContext;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.modules.BaseModule;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class AuthenticationRequiredResolution implements Resolution {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(AuthenticationRequiredResolution.class);

    public static final int STATUS = 401;

    private String errorMessage;
    public static final String LOGIN_PAGE_HEADER = "X-Portofino-Login-Page";

    public AuthenticationRequiredResolution() {}

    public AuthenticationRequiredResolution(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if(request.getParameter("__portofino_quiet_auth_failure") != null) {
            return;
        }
        ServletContext servletContext = request.getServletContext();
        Configuration configuration =
                (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
        String loginPage = configuration.getString(PortofinoProperties.LOGIN_PAGE);
        if (response.getContentType() == null || response.getContentType().contains("text/html")) {
            ElementsActionBeanContext context = new ElementsActionBeanContext();
            context.setRequest(request);
            String originalPath = context.getActionPath();
            UrlBuilder urlBuilder =
                    new UrlBuilder(Locale.getDefault(), originalPath, false);
            Map<?, ?> parameters = request.getParameterMap();
            urlBuilder.addParameters(parameters);
            String returnUrl = urlBuilder.toString();
            logger.info("Anonymous user not allowed to see {}. Redirecting to login.", originalPath);
            RedirectResolution redirectResolution =
                    new RedirectResolution(loginPage, true);
            redirectResolution.addParameter("returnUrl", returnUrl);
            redirectResolution.execute(request, response);
        } else {
            logger.debug("AJAX call while user disconnected");
            UrlBuilder loginUrlBuilder =
                    new UrlBuilder(request.getLocale(), loginPage, false);
            response.setHeader(LOGIN_PAGE_HEADER, loginUrlBuilder.toString());
            new ErrorResolution(STATUS, errorMessage).execute(request, response);
        }
    }
}
