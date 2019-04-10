/*
 * Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.blobs.FileUploadLimitExceededException;
import com.manydesigns.elements.blobs.MultipartWrapper;
import com.manydesigns.elements.blobs.StreamingCommonsMultipartWrapper;
import ognl.OgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ElementsFilter implements Filter {
    public static final String copyright =
            "Copyright (C) 2005-2017 ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public static final String REQUEST_OGNL_ATTRIBUTE = "request";
    public static final String SESSION_OGNL_ATTRIBUTE = "session";
    public static final String SERVLET_CONTEXT_OGNL_ATTRIBUTE = "servletContext";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected FilterConfig filterConfig;
    protected ServletContext servletContext;


    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(ElementsFilter.class);

    //--------------------------------------------------------------------------
    // Filter implementation
    //--------------------------------------------------------------------------

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.servletContext = filterConfig.getServletContext();
        logger.info("ElementsFilter initialized");
    }

    public void doFilter(ServletRequest req,
                         ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {

        if (req instanceof HttpServletRequest
                && res instanceof HttpServletResponse) {
            doHttpFilter((HttpServletRequest) req,
                    (HttpServletResponse)res,
                    filterChain);
        } else {
            filterChain.doFilter(req, res);
        }
    }

    public void destroy() {
        ElementsThreadLocals.destroy();
        logger.info("ElementsFilter destroyed");
    }

    //--------------------------------------------------------------------------
    // methods
    //--------------------------------------------------------------------------

    protected void doHttpFilter(HttpServletRequest req,
                                HttpServletResponse res,
                                FilterChain filterChain)
            throws IOException, ServletException {
        ServletContext context = filterConfig.getServletContext();

        try {
            logger.debug("Setting up default OGNL context");
            ElementsThreadLocals.setupDefaultElementsContext();
            OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();

            logger.debug("Creating request attribute mapper");
            AttributeMap requestAttributeMap =
                    AttributeMap.createAttributeMap(req);
            ognlContext.put(REQUEST_OGNL_ATTRIBUTE, requestAttributeMap);

            logger.debug("Creating session attribute mapper");
            HttpSession session = req.getSession(false);
            AttributeMap sessionAttributeMap =
                    AttributeMap.createAttributeMap(session);
            ognlContext.put(SESSION_OGNL_ATTRIBUTE, sessionAttributeMap);

            logger.debug("Creating servlet context attribute mapper");
            AttributeMap servletContextAttributeMap =
                    AttributeMap.createAttributeMap(servletContext);
            ognlContext.put(SERVLET_CONTEXT_OGNL_ATTRIBUTE,
                    servletContextAttributeMap);

            String contentType = req.getContentType();
            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                try {
                    MultipartWrapper multipartWrapper = buildMultipart(req);
                    ElementsThreadLocals.setMultipart(multipartWrapper);
                    req = multipartWrapper.wrapRequest(req);
                } catch (FileUploadLimitExceededException e) {
                    logger.warn("File upload limit exceeded", e);
                }
            }

            ElementsThreadLocals.setHttpServletRequest(req);
            ElementsThreadLocals.setHttpServletResponse(res);
            ElementsThreadLocals.setServletContext(context);

            filterChain.doFilter(req, res);
        } finally {
            ElementsThreadLocals.removeElementsContext();
        }
    }

    protected MultipartWrapper buildMultipart(HttpServletRequest request) throws IOException, FileUploadLimitExceededException {
        StreamingCommonsMultipartWrapper multipart = new StreamingCommonsMultipartWrapper();
        // Figure out where the temp directory is, and store that info
        File tempDir = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        if (tempDir == null) {
            String tmpDir = System.getProperty("java.io.tmpdir");
            if (tmpDir != null) {
                tempDir = new File(tmpDir).getAbsoluteFile();
            } else {
                logger.warn("The tmpdir system property was null! File uploads will probably fail.");
            }
        }
        long maxPostSize = Long.MAX_VALUE;
        multipart.build(request, tempDir, maxPostSize);
        return multipart;
    }



}
