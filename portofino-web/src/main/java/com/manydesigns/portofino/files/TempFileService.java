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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    private static TempFileService IMPL = new SimpleTempFileService();

    public static final Logger logger = LoggerFactory.getLogger(TempFileService.class);

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

    public static void setInstance(TempFileService tempFileService) {
        logger.info("Using temp file service: {}", tempFileService);
        IMPL = tempFileService;
    }

}
