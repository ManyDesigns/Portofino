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

package com.manydesigns.elements.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ServletUtils {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public final static Logger logger =
            LoggerFactory.getLogger(ServletUtils.class);

    public static void dumpRequestAttributes(HttpServletRequest request) {
        Enumeration attNames = request.getAttributeNames();
        while (attNames.hasMoreElements()) {
            String attrName = (String) attNames.nextElement();
            Object attrValue = request.getAttribute(attrName);
            logger.info("{} = {}", attrName, attrValue);
        }
    }

    /**
     * Returns the requested path, without the context path. E.g. webapp deployed under /foo, GET /foo/bar/baz?q=1&k=2,
     * getPath() returns /bar/baz.
     * @param request the HTTP request
     * @return the path of the requested resource as a path internal to the webapp.
     */
    public static String getPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if(path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path;
    }

    public static String getApplicationBaseUrl(HttpServletRequest req) {
        String scheme = req.getScheme();
        int port = req.getServerPort();
        String portString;
        if((scheme.equals("http") && port == 80) ||
           (scheme.equals("https") && port == 443)) {
            portString = "";
        } else {
            portString = ":" + port;
        }
        return scheme + "://" + req.getServerName() + portString + req.getContextPath();
    }

    /**
     * Marks the resource returned to a web client to be stored in cache for a very long time.
     * The resource is marked to be cached privately, i.e. on the client only, not in intermediate caches like proxies.
     * @param response the HTTP response whose headers are set.
     */
    public static void markCacheableForever(HttpServletResponse response) {
        int expiresAfter = 365 * 24 * 60 * 60; //1 year
        response.setHeader(ServletConstants.HTTP_PRAGMA, "");
        response.setDateHeader(ServletConstants.HTTP_EXPIRES, expiresAfter);
        response.setHeader(ServletConstants.HTTP_CACHE_CONTROL, "");
        //Private - only authorized users can cache the content
        response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_PRIVATE);
        response.addHeader(
                ServletConstants.HTTP_CACHE_CONTROL,
                ServletConstants.HTTP_CACHE_CONTROL_MAX_AGE + expiresAfter);
    }

}
