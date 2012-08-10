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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
