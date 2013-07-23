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
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.modules.BaseModule;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

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

        logger.debug("Retrieving configuration");
        Configuration configuration =
                (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);

        try {
            if (!filterForbiddenUrls(configuration, httpRequest)) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendError(404, httpRequest.getRequestURI());
                return;
            }
        } catch (URISyntaxException e) {
            throw new ServletException(e);
        }
        setupApplication(configuration, request);

        chain.doFilter(request, response);
    }

    public static boolean filterForbiddenUrls(
            Configuration configuration, HttpServletRequest httpRequest)
            throws IOException, ServletException, URISyntaxException {
        String encoding = configuration.getString(PortofinoProperties.URL_ENCODING);
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

    protected void setupApplication(Configuration configuration, ServletRequest request)
            throws ServletException {
        //Setup Elements blob manager
        File appBlobsDir;
        if(configuration.containsKey(PortofinoProperties.BLOBS_DIR_PATH)) {
            appBlobsDir = new File(configuration.getString(PortofinoProperties.BLOBS_DIR_PATH));
        } else {
            File appDir = (File) servletContext.getAttribute(BaseModule.APPLICATION_DIRECTORY);
            appBlobsDir = new File(appDir, "blobs");
        }
        logger.debug("Setting blobs directory");
        BlobManager blobManager = ElementsThreadLocals.getBlobManager();
        blobManager.setBlobsDir(appBlobsDir);
    }

    public void destroy() {
        servletContext = null;
    }
}
