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

import com.manydesigns.portofino.dispatcher.DispatcherUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
        logger.debug("Installing the dispatcher in the http current request");
        DispatcherUtil.install(httpRequest);
        chain.doFilter(request, response);
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
            if(attrName.equals("returnUrl")) {
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