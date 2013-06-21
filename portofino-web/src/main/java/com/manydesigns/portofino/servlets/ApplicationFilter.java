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
import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.i18n.SimpleTextProvider;
import com.manydesigns.elements.i18n.TextProvider;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.i18n.MultipleTextProvider;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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

        try {
            if (!filterForbiddenUrls(applicationStarter, httpRequest)) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendError(404, httpRequest.getRequestURI());
                return;
            }
        } catch (URISyntaxException e) {
            throw new ServletException(e);
        }
        setupApplication(applicationStarter, request);

        chain.doFilter(request, response);
    }

    public static boolean filterForbiddenUrls(
            ApplicationStarter applicationStarter, HttpServletRequest httpRequest)
            throws IOException, ServletException, URISyntaxException {
        String encoding = applicationStarter.getPortofinoConfiguration().getString(PortofinoProperties.URL_ENCODING);
        String uriString = httpRequest.getRequestURI();
        return filterForbiddenUrls(uriString, encoding);
    }

    public static boolean filterForbiddenUrls(String uriString, String encoding) throws UnsupportedEncodingException, URISyntaxException {
        uriString = URLDecoder.decode(uriString, encoding);
        String path = WebUtils.normalize(uriString);
        if(path == null) {
            return false;
        }
        if(path.startsWith("/app/") && !path.startsWith("/app/web")) {
            return false;
        }
        if(path.startsWith("/apps/")) {
            return false;
        }
        if(path.startsWith("/../") || path.startsWith("../")) {
            return false;
        }
        return true;
    }

    protected void setupApplication(ApplicationStarter applicationStarter, ServletRequest request)
            throws ServletException {
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
            ResourceBundle portofinoResourceBundle = application.getBundle(locale);

            LocalizationContext localizationContext =
                    new LocalizationContext(portofinoResourceBundle, locale);
            request.setAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".request", localizationContext);

            //Setup Elements I18n
            ResourceBundle elementsResourceBundle =
                    ResourceBundle.getBundle(SimpleTextProvider.DEFAULT_MESSAGE_RESOURCE, locale);

            TextProvider textProvider =
                    new MultipleTextProvider(
                            portofinoResourceBundle, elementsResourceBundle);
            ElementsThreadLocals.setTextProvider(textProvider);

            //Setup Elements blob manager
            File appBlobsDir;
            Configuration portofinoConfiguration = application.getConfiguration();
            if(portofinoConfiguration.containsKey(PortofinoProperties.BLOBS_DIR_PATH)) {
                appBlobsDir = new File(portofinoConfiguration.getString(PortofinoProperties.BLOBS_DIR_PATH));
            } else {
                appBlobsDir = new File(application.getAppDir(), "blobs");
            }
            logger.debug("Setting blobs directory");
            BlobManager blobManager = ElementsThreadLocals.getBlobManager();
            //TODO!!! blobManager.setBlobsDir(appBlobsDir);
        }
    }

    public void destroy() {
        servletContext = null;
    }
}
