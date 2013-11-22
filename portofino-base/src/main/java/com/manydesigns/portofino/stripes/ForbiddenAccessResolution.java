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

package com.manydesigns.portofino.stripes;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.stripes.ElementsActionBeanContext;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.modules.BaseModule;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ForbiddenAccessResolution implements Resolution {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(ForbiddenAccessResolution.class);

    public static final int UNAUTHORIZED = 403;

    private String errorMessage;

    public ForbiddenAccessResolution() {}

    public ForbiddenAccessResolution(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        //TODO ElementsActionBeanContext
        ElementsActionBeanContext context = new ElementsActionBeanContext();
        context.setRequest(request);
        String originalPath = context.getActionPath();
        UrlBuilder urlBuilder =
                new UrlBuilder(Locale.getDefault(), originalPath, false);
        Map<?, ?> parameters = request.getParameterMap();
        urlBuilder.addParameters(parameters);
        String returnUrl = urlBuilder.toString();
        boolean ajax = "true".equals(request.getParameter("ajax"));
        ServletContext servletContext = ElementsThreadLocals.getServletContext();
        Configuration configuration =
                (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
        if (!subject.isAuthenticated() && !ajax) {
            logger.info("Anonymous user not allowed. Redirecting to login.");
            String loginPage = configuration.getString(PortofinoProperties.LOGIN_PAGE);
            RedirectResolution redirectResolution =
                    new RedirectResolution(loginPage, true);
            redirectResolution.addParameter("returnUrl", returnUrl);
            redirectResolution.execute(request, response);
        } else {
            if(ajax) {
                logger.debug("AJAX call while user disconnected");
                //TODO where to redirect?
                String loginPage = configuration.getString(PortofinoProperties.LOGIN_PAGE);
                UrlBuilder loginUrlBuilder =
                        new UrlBuilder(Locale.getDefault(), loginPage, false);
                response.setStatus(UNAUTHORIZED);
                new StreamingResolution("text/plain", loginUrlBuilder.toString()).execute(request, response);
            } else {
                logger.warn("User not authorized for url {}.", returnUrl);
                new ErrorResolution(UNAUTHORIZED, errorMessage).execute(request, response);
            }
        }
    }
}
