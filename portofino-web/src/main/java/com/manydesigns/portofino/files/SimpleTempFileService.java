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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SimpleTempFileService extends TempFileService {

    public static final Logger logger = LoggerFactory.getLogger(SimpleTempFileService.class);

    public static class SimpleTempFile extends TempFile {

        public final File file;

        public SimpleTempFile(String mimeType, String name) throws IOException {
            super(mimeType, name);
            file = File.createTempFile("temp." +  name, ".temp");
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new FileOutputStream(file);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public void dispose() {
            try {
                file.delete();
            } catch (Exception e) {
                logger.warn("Could not delete temp file: " + file.getAbsolutePath(), e);
            }
        }
    }

    @Override
    public TempFile newTempFile(String mimeType, String name) throws IOException {
        return new SimpleTempFile(mimeType, name);
    }

}
