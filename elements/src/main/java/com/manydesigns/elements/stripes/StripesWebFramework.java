/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.stripes;

import com.manydesigns.elements.servlet.Upload;
import com.manydesigns.elements.servlet.WebFramework;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.StripesRequestWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class StripesWebFramework extends WebFramework {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // CONSTANTS
    //**************************************************************************

    public final static String STRIPES_NAME = "Stripes web framework";

    //**************************************************************************
    // WebFramework overridden methods
    //**************************************************************************

    @Override
    public String getName() {
        return STRIPES_NAME;
    }

    @Override
    public HttpServletRequest wrapRequest(HttpServletRequest request)
            throws ServletException {
        return request;
    }

    @Override
    public Upload getUpload(HttpServletRequest req, String parameterName) {
        StripesRequestWrapper stripesRequest =
                StripesRequestWrapper.findStripesWrapper(req);
        FileBean fileBean = stripesRequest.getFileParameterValue(parameterName);

        if (fileBean == null) {
            logger.debug("Parameter not found: {}", parameterName);
            return null;
        }

        String fileName = fileBean.getFileName();
        String contentType = fileBean.getContentType();
        String characterEncoding = null;
        try {
            InputStream is = fileBean.getInputStream();
            return new Upload(is, fileName, contentType, characterEncoding);
        } catch (IOException e) {
            logger.warn("Cannot read upload file", e);
            return null;
        }
    }
}
