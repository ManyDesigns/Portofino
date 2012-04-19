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
