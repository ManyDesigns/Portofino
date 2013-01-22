/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
