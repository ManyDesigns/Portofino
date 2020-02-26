/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SimpleBlobManager implements BlobManager {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(SimpleBlobManager.class);

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected File blobsDir;
    protected String metaFileNamePattern;
    protected String dataFileNamePattern;

    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************

    public SimpleBlobManager(File blobsDir,
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
    public void loadMetadata(Blob blob) throws IOException {
        ensureValidCode(blob.getCode());
        blob.setMetaProperties(loadMetaProperties(getMetaFile(blob.getCode())));
    }

    public Properties loadMetaProperties(File metaFile) throws IOException {
        Properties metaProperties = new Properties();
        try(InputStream metaStream = new FileInputStream(metaFile)) {
            metaProperties.load(metaStream);
        }
        return metaProperties;
    }

    @Override
    public InputStream openStream(Blob blob) throws IOException {
        ensureValidCode(blob.getCode());
        blob.setInputStream(new FileInputStream(getDataFile(blob.getCode())));
        return blob.getInputStream();
    }

    @Override
    public void save(Blob blob) throws IOException {
        ensureValidCode(blob.getCode());
        File dataFile = getDataFile(blob.getCode());
        if(!dataFile.getParentFile().isDirectory()) {
            dataFile.getParentFile().mkdirs();
        }
        InputStream encryptInputStream;
        try(FileOutputStream out = new FileOutputStream(dataFile)) {
            InputStream inputStream = blob.getInputStream();
            if(blob.isEncrypted()){
                 encryptInputStream = BlobUtils.encrypt(inputStream,blob.getEncryptionType());
                IOUtils.copyLarge(encryptInputStream, out);
            } else {
                blob.setSize(IOUtils.copyLarge(inputStream, out));
            }
        }
        File metaFile = getMetaFile(blob.getCode());
        if(!metaFile.getParentFile().isDirectory()) {
            metaFile.getParentFile().mkdirs();
        }
        try(OutputStream out = new FileOutputStream(metaFile)) {
            blob.getMetaProperties().store(out, "Blob code #" + blob.getCode());
        }
        blob.dispose();
    }

    @Override
    public boolean delete(Blob blob) {
        String code = blob.getCode();
        ensureValidCode(code);
        File metaFile = getMetaFile(code);
        File dataFile = getDataFile(code);
        boolean success;
        try {
            success = metaFile.delete();
        } catch (Exception e) {
            logger.warn("Cound not delete meta file", e);
            success = false;
        }
        try {
            success = success && dataFile.delete();
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
