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

package com.manydesigns.elements.blobs;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(BlobManager.class);

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected File blobsDir;
    protected String metaFileNamePattern;
    protected String dataFileNamePattern;

    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************


    public BlobManager() {}

    public BlobManager(File blobsDir,
                       String metaFileNamePattern,
                       String dataFileNamePattern) {
        this.blobsDir = blobsDir;
        this.metaFileNamePattern = metaFileNamePattern;
        this.dataFileNamePattern = dataFileNamePattern;
    }

    public static BlobManager createDefaultBlobManager() {
        Configuration elementsConfiguration =
                ElementsProperties.getConfiguration();
        String blobsDirPath =
                elementsConfiguration.getString(
                        ElementsProperties.BLOBS_DIR);
        File blobsDir;
        if (blobsDirPath == null) {
            blobsDir = null;
        } else {
            blobsDir = new File(blobsDirPath);
        }
        String metaFilenamePattern =
                elementsConfiguration.getString(
                        ElementsProperties.BLOBS_META_FILENAME_PATTERN);
        String dataFilenamePattern =
                elementsConfiguration.getString(
                        ElementsProperties.BLOBS_DATA_FILENAME_PATTERN);
        return new BlobManager(
                blobsDir, metaFilenamePattern, dataFilenamePattern);
    }


    //**************************************************************************
    // Methods
    //**************************************************************************

    public Blob saveBlob(String code,
                         File sourceFile,
                         String fileName,
                         String contentType,
                         String characterEncoding
    ) throws IOException {
        InputStream sourceStream = new FileInputStream(sourceFile);
        return saveBlob(code, sourceStream, fileName, contentType, characterEncoding);
    }

    public Blob saveBlob(String code,
                         byte[] sourceBytes,
                         String fileName,
                         String contentType,
                         String characterEncoding
    ) throws IOException {
        InputStream sourceStream = new ByteArrayInputStream(sourceBytes);
        return saveBlob(code, sourceStream, fileName, contentType, characterEncoding);
    }

    public Blob saveBlob(String code,
                         InputStream sourceStream,
                         String fileName,
                         String contentType,
                         @Nullable String characterEncoding
    ) throws IOException {
        ensureValidCode(code);

        File metaFile =
                RandomUtil.getCodeFile(blobsDir, metaFileNamePattern, code);
        File dataFile =
                RandomUtil.getCodeFile(blobsDir, dataFileNamePattern, code);
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
        ensureValidCode(code);

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

    public void ensureValidCode(String code) {
        if (!StringUtils.isAlphanumeric(code)) {
            throw new IllegalArgumentException(
                    "Code is not alphanumeric: " + code);
        }
    }

    public Blob loadBlob(String code) throws IOException {
        ensureValidCode(code);

        File metaFile =
                RandomUtil.getCodeFile(blobsDir, metaFileNamePattern, code);
        File dataFile =
                RandomUtil.getCodeFile(blobsDir, dataFileNamePattern, code);
        Blob blob = new Blob(metaFile, dataFile);

        blob.loadMetaProperties();

        return blob;
    }

    public boolean deleteBlob(String code) {
        ensureValidCode(code);

        File metaFile =
                RandomUtil.getCodeFile(blobsDir, metaFileNamePattern, code);
        File dataFile =
                RandomUtil.getCodeFile(blobsDir, dataFileNamePattern, code);
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
    // Getters/setters
    //**************************************************************************


    public File getBlobsDir() {
        return blobsDir;
    }

    public void setBlobsDir(File blobsDir) {
        this.blobsDir = blobsDir;
    }

    public String getMetaFileNamePattern() {
        return metaFileNamePattern;
    }

    public void setMetaFileNamePattern(String metaFileNamePattern) {
        this.metaFileNamePattern = metaFileNamePattern;
    }

    public String getDataFileNamePattern() {
        return dataFileNamePattern;
    }

    public void setDataFileNamePattern(String dataFileNamePattern) {
        this.dataFileNamePattern = dataFileNamePattern;
    }
}
