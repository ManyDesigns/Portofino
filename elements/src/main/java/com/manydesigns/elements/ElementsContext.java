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

package com.manydesigns.elements;

import com.manydesigns.elements.blobs.MultipartWrapper;
import com.manydesigns.elements.i18n.TextProvider;
import ognl.OgnlContext;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ElementsContext {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected TextProvider textProvider;
    protected HttpServletRequest httpServletRequest;
    protected HttpServletResponse httpServletResponse;
    protected ServletContext servletContext;
    protected OgnlContext ognlContext;
    protected MultipartWrapper multipart;

    //**************************************************************************
    // Constructors
    //**************************************************************************
    public ElementsContext() {}

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public TextProvider getTextProvider() {
        return textProvider;
    }

    public void setTextProvider(TextProvider textProvider) {
        this.textProvider = textProvider;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public OgnlContext getOgnlContext() {
        return ognlContext;
    }

    public void setOgnlContext(OgnlContext ognlContext) {
        this.ognlContext = ognlContext;
    }

    public MultipartWrapper getMultipart() {
        return multipart;
    }

    public void setMultipart(MultipartWrapper multipart) {
        this.multipart = multipart;
    }
}
