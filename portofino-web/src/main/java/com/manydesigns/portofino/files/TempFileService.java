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

package com.manydesigns.portofino.files;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class TempFileService {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private static final TempFileService IMPL;

    public static final Logger logger = LoggerFactory.getLogger(TempFileService.class);

    static {
        TempFileService impl;
        try {
            Class.forName("com.google.appengine.api.files.FileServiceFactory");
            impl = new GAETempFileService();
            logger.info("Using Google App Engine FileService API for temporary files");
        } catch (Throwable e) {
            impl = new SimpleTempFileService();
            logger.info("Using the java.io API for temporary files");
        }
        IMPL = impl;
    }

    public abstract TempFile newTempFile(String mimeType, String name) throws IOException;

    public Resolution stream(final TempFile tempFile) throws IOException {
        return new StreamingResolution(tempFile.mimeType, tempFile.getInputStream()) {
            @Override
            protected void stream(HttpServletResponse response) throws Exception {
                super.stream(response);
                tempFile.dispose();
            }
        }.setFilename(tempFile.name);
    }

    public static TempFileService getInstance() {
        return IMPL;
    }

}
