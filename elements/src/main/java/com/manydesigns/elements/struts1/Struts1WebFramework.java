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
