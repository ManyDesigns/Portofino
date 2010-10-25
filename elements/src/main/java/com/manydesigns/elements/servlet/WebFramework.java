/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.servlet;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.InstanceBuilder;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class WebFramework {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // CONSTANTS
    //**************************************************************************

    public final static String PLAIN_SERVLET_API_NAME = "Plain servlet API";

    //**************************************************************************
    // Static fields
    //**************************************************************************

    protected static final Properties elementsProperties;
    protected static WebFramework webFramework;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LogUtil.getLogger(WebFramework.class);

    //**************************************************************************
    // Static initialization
    //**************************************************************************

    static {
        elementsProperties = ElementsProperties.getProperties();
        resetSingleton();
    }

    public static void resetSingleton() {
        String managerClassName =
                elementsProperties.getProperty(
                        ElementsProperties.WEB_FRAMEWORK_PROPERTY);
        InstanceBuilder<WebFramework> builder =
                new InstanceBuilder<WebFramework>(
                        WebFramework.class,
                        WebFramework.class,
                        logger);
        webFramework = builder.createInstance(managerClassName);
    }

    public static WebFramework getWebFramework() {
        return webFramework;
    }

    //**************************************************************************
    // (Overridable) methods
    //**************************************************************************

    public String  getName() {
        return PLAIN_SERVLET_API_NAME;
    }

    public Upload getUpload(HttpServletRequest req, String parameter) {
        if (!(req instanceof MultipartRequest)) {
            LogUtil.warningMF(logger,
                    "Request is not an instance of {0}. Actual type: {1}",
                    MultipartRequest.class.getName(),
                    req.getClass().getName());
            return null;
        }

        MultipartRequest request = (MultipartRequest)req;

        FileItem fileItem = request.getFileItem(parameter);
        if (fileItem == null) {
            return null;
        } else {
            try {
                InputStream fis = fileItem.getInputStream();
                String fileName = FilenameUtils.getName(fileItem.getName());
                String contentType = fileItem.getContentType();
                return new Upload(fis, fileName, contentType);
            } catch (IOException e) {
                logger.warning("Cannot read upload file.");
                return null;
            }
        }
    }

}
