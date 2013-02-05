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

    public static String getOriginalPath(HttpServletRequest request) {
        String originalPath =
                (String) request.getAttribute(
                        "javax.servlet.include.servlet_path");
        if (originalPath == null) {
            originalPath = (String) request.getAttribute(
                "javax.servlet.forward.servlet_path");
        }
        if (originalPath == null) {
            originalPath = request.getRequestURI();
            String contextPath = request.getContextPath();
            if(!"".equals(contextPath) &&
               originalPath.startsWith(contextPath)) {
                originalPath = originalPath.substring(contextPath.length());
            }
        }
        return originalPath;
    }

    public static void dumpRequestAttributes(HttpServletRequest request) {
        Enumeration attNames = request.getAttributeNames();
        while (attNames.hasMoreElements()) {
            String attrName = (String) attNames.nextElement();
            Object attrValue = request.getAttribute(attrName);
            logger.info("{} = {}", attrName, attrValue);
        }

    }

}
