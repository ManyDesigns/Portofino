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

package com.manydesigns.elements.struts1;

import com.manydesigns.elements.servlet.Upload;
import com.manydesigns.elements.servlet.WebFramework;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.upload.FormFile;
import org.apache.struts.upload.MultipartRequestHandler;
import org.apache.struts.upload.MultipartRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Struts1WebFramework extends WebFramework {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // CONSTANTS
    //**************************************************************************

    public final static String STRUTS1_NAME = "Apache Struts1 web framework";

    //**************************************************************************
    // WebFramework overridden methods
    //**************************************************************************

    @Override
    public String getName() {
        return STRUTS1_NAME;
    }


    @Override
    public Upload getUpload(HttpServletRequest req, String parameterName) {
        Class reqClass = req.getClass();
        if (!(req instanceof MultipartRequestWrapper)) {
            logger.warn("Request is not an instance of {}. Actual type: {}",
                    MultipartRequestWrapper.class.getName(),
                    reqClass.getName());
            return null;
        }

        ActionConfig actionConfig =
                (ActionConfig) req.getAttribute(Globals.MAPPING_KEY);

        String name = actionConfig.getName();
        String scope = actionConfig.getScope();

        ActionForm actionForm;
        if ("request".equals(scope)) {
            actionForm = (ActionForm) req.getAttribute(name);
        } else if ("session".equals(scope)) {
            HttpSession session = req.getSession();
            actionForm = (ActionForm) session.getAttribute(name);
        } else {
            return null;
        }

        MultipartRequestHandler handler =
                actionForm.getMultipartRequestHandler();

        Hashtable fileElements = handler.getFileElements();
        FormFile formFile = (FormFile) fileElements.get(parameterName);
        if (formFile == null) {
            return null;
        }

        int fileSize= formFile.getFileSize();
        if (fileSize <= 0) {
            return null;
        }

        String fileName = formFile.getFileName();
        String contentType = formFile.getContentType();
        String characterEncoding = null;
        try {
            InputStream is = formFile.getInputStream();
            return new Upload(is, fileName, contentType, null);
        } catch (IOException e) {
            logger.warn("Cannot read upload file: {}", fileName);
            return null;
        }
    }
}
