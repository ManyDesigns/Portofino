/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.portofino.RequestAttributes;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
        //Application application = (Application) request.getAttribute(RequestAttributes.APPLICATION);

        logger.debug("Installing the dispatcher in the http current request");
        //String oldPath = (String) request.getAttribute(Dispatcher.DISPATCH_PATH);
        //request.removeAttribute(Dispatcher.DISPATCH_PATH);
        DispatcherUtil.install(httpRequest);
        //Dispatch dispatch = DispatcherUtil.getDispatch(dispatcher, httpRequest);

        /*if (dispatch == null) {
            if(oldPath != null) {
                dispatch = dispatcher.getDispatch(httpRequest.getContextPath(), oldPath);
            } else {
                logger.debug("We can't handle this path ({})." +
                    " Let's do the normal filter chain",
                    httpRequest.getServletPath());
                chain.doFilter(request, response);
                return;
            }
        }

        //Map<String, Object> savedAttributes =
        //        saveAndResetRequestAttributes(request);
        request.setAttribute(Dispatcher.DISPATCH_PATH, dispatch.getOriginalPath());
        try {
            //Handle through the ModelActionResolver
            chain.doFilter(request, response);
        } finally {
            //restoreRequestAttributes(request, savedAttributes);
            request.setAttribute(Dispatcher.DISPATCH_PATH, oldPath);
        }*/
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