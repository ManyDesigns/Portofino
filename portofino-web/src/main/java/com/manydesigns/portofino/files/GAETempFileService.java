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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

/**
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class GAETempFileService extends TempFileService {

    public static final Logger logger = LoggerFactory.getLogger(GAETempFileService.class);

    @Override
    public TempFile newTempFile(String mimeType, String name) throws IOException {
        return new GAETempFile(mimeType, name);
    }

    public static class GAETempFile extends TempFile {

        protected AppEngineFile file;
        protected FileWriteChannel writeChannel;
        protected FileReadChannel readChannel;
        protected OutputStream outputStream;
        protected InputStream inputStream;

        public GAETempFile(String mimeType, String name) throws IOException {
            super(mimeType, name);
            FileService fileService = FileServiceFactory.getFileService();
            file = fileService.createNewBlobFile(mimeType, name);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            if(outputStream == null) {
                FileService fileService = FileServiceFactory.getFileService();
                writeChannel = fileService.openWriteChannel(file, true);
                outputStream = Channels.newOutputStream(writeChannel);
            }
            return outputStream;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if(outputStream == null) {
                throw new IOException("The file has no associated OutputStream");
            }
            if(inputStream == null) {
                FileService fileService = FileServiceFactory.getFileService();
                writeChannel.closeFinally();
                readChannel = fileService.openReadChannel(file, false);
                inputStream = Channels.newInputStream(readChannel);
            }
            return inputStream;
        }

        @Override
        public void dispose() {
            FileService fileService = FileServiceFactory.getFileService();
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
            try {
                if(readChannel != null) {
                    readChannel.close();
                }
                //fileService.delete(file); non funziona (UnsupportedOperationException)
                BlobKey blobKey = fileService.getBlobKey(file);
                BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
                blobstoreService.delete(blobKey);
            } catch (Exception e) {
                logger.warn("Could not delete temporary file " + file.getFullPath(), e);
            }
        }
    }
}
