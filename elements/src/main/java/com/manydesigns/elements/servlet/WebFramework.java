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

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.util.InstanceBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class WebFramework {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // CONSTANTS
    //**************************************************************************

    public final static String PLAIN_SERVLET_API_NAME = "Plain servlet API";

    //**************************************************************************
    // Static fields
    //**************************************************************************

    protected static final Configuration elementsConfiguration;
    protected static WebFramework webFramework;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(WebFramework.class);

    //**************************************************************************
    // Static initialization
    //**************************************************************************

    static {
        elementsConfiguration = ElementsProperties.getConfiguration();
        resetSingleton();
    }

    public static void resetSingleton() {
        String managerClassName =
                elementsConfiguration.getString(
                        ElementsProperties.WEB_FRAMEWORK);
        InstanceBuilder<WebFramework> builder =
                new InstanceBuilder<WebFramework>(
                        WebFramework.class,
                        WebFramework.class,
                        logger);
        webFramework = builder.createInstance(managerClassName);
    }

    public static WebFramework getDefaultWebFramework() {
        return webFramework;
    }

    //**************************************************************************
    // (Overridable) methods
    //**************************************************************************

    public String  getName() {
        return PLAIN_SERVLET_API_NAME;
    }

    public Upload getUpload(HttpServletRequest req, String parameterName) {
        if (!(req instanceof MultipartRequest)) {
            logger.warn("Request is not an instance of {}. Actual type: {}",
                    MultipartRequest.class.getName(),
                    req.getClass().getName());
            return null;
        }

        MultipartRequest request = (MultipartRequest)req;

        FileItem fileItem = request.getFileItem(parameterName);
        if (fileItem == null) {
            return null;
        } else {
            try {
                InputStream fis = fileItem.getInputStream();
                String fileName = FilenameUtils.getName(fileItem.getName());
                String contentType = fileItem.getContentType();
                String characterEncoding = null;
                return new Upload(fis, fileName, contentType, characterEncoding);
            } catch (IOException e) {
                logger.warn("Cannot read upload file.");
                return null;
            }
        }
    }

    public HttpServletRequest wrapRequest(HttpServletRequest request)
            throws ServletException {
        try {
            return new MultipartRequestWrapper(request);
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }

    }
}
