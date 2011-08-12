/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.blobs;

import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class BlobManager {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(BlobManager.class);

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final File blobsDirFile;
    protected final String metaFileNamePattern;
    protected final String dataFileNamePattern;

    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************

    public BlobManager(String blobsDir,
                       String metaFileNamePattern,
                       String dataFileNamePattern) {
        blobsDirFile = new File(blobsDir);
        this.metaFileNamePattern = metaFileNamePattern;
        this.dataFileNamePattern = dataFileNamePattern;
    }

    //**************************************************************************
    // Methods
    //**************************************************************************

    public Blob saveBlob(File sourceFile,
                         String fileName,
                         String contentType,
                         String characterEncoding
    ) throws IOException {
        InputStream sourceStream = new FileInputStream(sourceFile);
        return saveBlob(sourceStream, fileName, contentType, characterEncoding);
    }

    public Blob saveBlob(byte[] sourceBytes,
                         String fileName,
                         String contentType,
                         String characterEncoding
    ) throws IOException {
        InputStream sourceStream = new ByteArrayInputStream(sourceBytes);
        return saveBlob(sourceStream, fileName, contentType, characterEncoding);
    }

    public Blob saveBlob(InputStream sourceStream,
                         String fileName,
                         String contentType,
                         @Nullable String characterEncoding
    ) throws IOException {
        String code = RandomUtil.createRandomCode();

        File metaFile =
                RandomUtil.getCodeFile(blobsDirFile, metaFileNamePattern, code);
        File dataFile =
                RandomUtil.getCodeFile(blobsDirFile, dataFileNamePattern, code);
        Blob blob = new Blob(metaFile, dataFile);

        // copy the data
        long size = IOUtils.copyLarge(
                sourceStream, new FileOutputStream(dataFile));

        // set and save the metadata
        blob.setCode(code);
        blob.setFilename(fileName);
        blob.setContentType(contentType);
        blob.setSize(size);
        blob.saveMetaProperties();

        return blob;
    }

    public Blob updateBlob(String code,
                           byte[] sourceBytes,
                           String characterEncoding
    ) throws IOException {
        InputStream sourceStream = new ByteArrayInputStream(sourceBytes);
        return updateBlob(code, sourceStream, characterEncoding);
    }

    public Blob updateBlob(String code, InputStream sourceStream, String characterEncoding)
            throws IOException {
        Blob blob = loadBlob(code);
        File dataFile = blob.getDataFile();

        // copy the data
        long size = IOUtils.copyLarge(
                sourceStream, new FileOutputStream(dataFile));

        // set and save the metadata
        blob.setSize(size);
        blob.setCharacterEncoding(characterEncoding);
        blob.saveMetaProperties();

        return blob;
    }

    public Blob loadBlob(String code) throws IOException {
        File metaFile =
                RandomUtil.getCodeFile(blobsDirFile, metaFileNamePattern, code);
        File dataFile =
                RandomUtil.getCodeFile(blobsDirFile, dataFileNamePattern, code);
        Blob blob = new Blob(metaFile, dataFile);

        blob.loadMetaProperties();

        return blob;
    }

    public boolean deleteBlob(String code) {
        File metaFile =
                RandomUtil.getCodeFile(blobsDirFile, metaFileNamePattern, code);
        File dataFile =
                RandomUtil.getCodeFile(blobsDirFile, dataFileNamePattern, code);
        boolean success = true;
        try {
            success = metaFile.delete() && success;
        } catch (Exception e) {
            logger.warn("Cound not delete meta file", e);
            success = false;
        }
        try {
            success = dataFile.delete() && success;
        } catch (Exception e) {
            logger.warn("Cound not delete data file", e);
            success = false;
        }
        return success;
    }

    //**************************************************************************
    // Getters
    //**************************************************************************

    public File getBlobsDirFile() {
        return blobsDirFile;
    }

    public String getMetaFileNamePattern() {
        return metaFileNamePattern;
    }

    public String getDataFileNamePattern() {
        return dataFileNamePattern;
    }

}
