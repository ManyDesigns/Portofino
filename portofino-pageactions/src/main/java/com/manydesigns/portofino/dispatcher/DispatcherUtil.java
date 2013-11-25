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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.stripes.ElementsActionBeanContext;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.PageactionsModule;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import org.apache.commons.configuration.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DispatcherUtil {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static Dispatcher get(HttpServletRequest request) {
        if(request == null) {
            return null;
        }
        return (Dispatcher) request.getAttribute(RequestAttributes.DISPATCHER);
    }

    public static Dispatcher install(HttpServletRequest request) {
        ServletContext servletContext = ElementsThreadLocals.getServletContext();
        Configuration configuration =
                (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
        File pagesDir = (File) servletContext.getAttribute(PageactionsModule.PAGES_DIRECTORY);

        Dispatcher dispatcher = new Dispatcher(configuration, pagesDir);
        request.setAttribute(RequestAttributes.DISPATCHER, dispatcher);
        return dispatcher;
    }

    public static Dispatch getDispatch(HttpServletRequest request) {
        Dispatcher dispatcher = get(request);
        return getDispatch(dispatcher, request);
    }

    public static Dispatch getDispatch(HttpServletRequest request, Object actionBean) {
        Dispatcher dispatcher = get(request);
        if(actionBean instanceof AbstractActionBean) {
            String actionPath = ((AbstractActionBean) actionBean).getContext().getActionPath();
            return dispatcher.getDispatch(actionPath);
        } else {
            return getDispatch(request);
        }
    }

    public static Dispatch getDispatch(Dispatcher dispatcher, HttpServletRequest request) {
        //TODO ElementsActionBeanContext
        ElementsActionBeanContext context = new ElementsActionBeanContext();
        context.setRequest(request);
        String originalPath = context.getActionPath();
        return dispatcher.getDispatch(originalPath);
    }

}
