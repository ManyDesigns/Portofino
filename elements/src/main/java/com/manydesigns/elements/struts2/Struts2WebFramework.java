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

package com.manydesigns.elements.struts2;

import com.manydesigns.elements.servlet.Upload;
import com.manydesigns.elements.servlet.WebFramework;
import org.apache.struts2.dispatcher.StrutsRequestWrapper;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Struts2WebFramework extends WebFramework {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // CONSTANTS
    //**************************************************************************

    public final static String STRUTS2_NAME = "Apache Struts2 web framework";

    //**************************************************************************
    // WebFramework overridden methods
    //**************************************************************************

    @Override
    public String getName() {
        return STRUTS2_NAME;
    }

    @Override
    public Upload getUpload(HttpServletRequest req, String parameterName) {
        Class reqClass = req.getClass();
        if (StrutsRequestWrapper.class.equals(reqClass)) {
            logger.warn("Request of type {} does not support uploads. " +
                    "Make sure the form uses enctype=\"multipart/form-data\".",
                    reqClass.getName());
            return null;
        }
        if (!(req instanceof MultiPartRequestWrapper)) {
            logger.warn("Request is not an instance of {}. Actual type: {}",
                    MultiPartRequestWrapper.class.getName(),
                    reqClass.getName());
            return null;
        }

        MultiPartRequestWrapper mprw = (MultiPartRequestWrapper) req;

        File[] files = mprw.getFiles(parameterName);
        String[] fileNames = mprw.getFileNames(parameterName);
        String[] contentTypes = mprw.getContentTypes(parameterName);
        if (files == null || files.length == 0) {
            return null;
        }
        File file = files[0];
        String fileName = fileNames[0];
        String contentType = contentTypes[0];
        String characterEncoding = null;
        try {
            InputStream is = new FileInputStream(file);
            return new Upload(is, fileName, contentType, characterEncoding);
        } catch (FileNotFoundException e) {
            logger.warn("Cannot read upload file: {}", file.getAbsolutePath());
            return null;
        }
    }

    @Override
    public HttpServletRequest wrapRequest(HttpServletRequest request)
            throws ServletException {
        return request;
    }
}
