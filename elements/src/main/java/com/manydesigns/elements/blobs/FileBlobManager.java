/*
 * Copyright (C) 2005-2014 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class FileBlobManager implements BlobManager {
    public static final String copyright =
            "Copyright (c) 2005-2014, ManyDesigns srl";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(FileBlobManager.class);

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected File blobsDir;
    protected String metaFileNamePattern;
    protected String dataFileNamePattern;
    protected BlobManager temporaryBlobManager;

    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************

    public FileBlobManager(File blobsDir,
                           String metaFileNamePattern,
                           String dataFileNamePattern) {
        this.blobsDir = blobsDir;
        if(!blobsDir.isDirectory() && !blobsDir.mkdirs()) {
            logger.warn("Invalid blobs directory: {}", blobsDir.getAbsolutePath());
        }
        this.metaFileNamePattern = metaFileNamePattern;
        this.dataFileNamePattern = dataFileNamePattern;
    }

    //**************************************************************************
    // Methods
    //**************************************************************************

    @Override
    public FileBlob save(String code,
                               InputStream sourceStream,
                               String fileName,
                               String contentType,
                               @Nullable String characterEncoding
    ) throws IOException {
        ensureValidCode(code);
        File metaFile = getMetaFile(code);
        File dataFile = getDataFile(code);
        FileBlob blob = new FileBlob(code, metaFile, dataFile);

        // copy the data
        long size = IOUtils.copyLarge(
                sourceStream, new FileOutputStream(dataFile));

        // set and save the metadata
        blob.setFilename(fileName);
        blob.setContentType(contentType);
        blob.setSize(size);
        blob.saveMetaProperties();

        return blob;
    }

    protected File getMetaFile(String code) {
        return RandomUtil.getCodeFile(blobsDir, metaFileNamePattern, code);
    }

    protected File getDataFile(String code) {
        return RandomUtil.getCodeFile(blobsDir, dataFileNamePattern, code);
    }

    public void ensureValidCode(String code) {
        if (!StringUtils.isAlphanumeric(code)) {
            throw new IllegalArgumentException(
                    "Code is not alphanumeric: " + code);
        }
    }

    @Override
    public FileBlob load(String code) throws IOException {
        ensureValidCode(code);
        File metaFile = getMetaFile(code);
        File dataFile = getDataFile(code);
        FileBlob blob = new FileBlob(code, metaFile, dataFile);

        blob.loadMetaProperties();

        return blob;
    }

    @Override
    public boolean delete(String code) {
        ensureValidCode(code);
        File metaFile = getMetaFile(code);
        File dataFile = getDataFile(code);
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

    @Override
    public BlobManager getTemporaryBlobManager() {
        return temporaryBlobManager;
    }

    @Override
    public void setTemporaryBlobManager(BlobManager temporaryBlobManager) {
        this.temporaryBlobManager = temporaryBlobManager;
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
