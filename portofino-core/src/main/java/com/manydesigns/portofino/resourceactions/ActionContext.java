/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.resourceactions;

import com.manydesigns.elements.ElementsContext;
import com.manydesigns.elements.ElementsThreadLocals;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>Useful context objects for resource actions.</p>
 *
 * @author Alessio Stalla       - alessiostalla@gmail
 */
public class ActionContext {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected ServletContext servletContext;
    protected String actionPath;
    protected ElementsContext elementsContext = ElementsThreadLocals.getElementsContext();

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String getActionPath() {
        return actionPath;
    }

    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }

    public ElementsContext getElementsContext() {
        return elementsContext;
    }

    public void setElementsContext(ElementsContext elementsContext) {
        this.elementsContext = elementsContext;
    }

    //I18n

    /**
     * @see com.manydesigns.elements.i18n.TextProvider#getText(String, Object...)
     */
    String getText(String key, Object... args) {
        return elementsContext.getTextProvider().getText(key, args);
    }

    /**
     * @see com.manydesigns.elements.i18n.TextProvider#getTextOrNull(String, Object...)
     */
    String getTextOrNull(String key, Object... args) {
        return elementsContext.getTextProvider().getTextOrNull(key, args);
    }
}
