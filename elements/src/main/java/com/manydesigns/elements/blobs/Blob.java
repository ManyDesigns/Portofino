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

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Blob {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String CODE_PROPERTY = "code";
    public final static String FILENAME_PROPERTY = "filename";
    public final static String CONTENT_TYPE_PROPERTY = "content.type";
    public final static String SIZE_PROPERTY = "size";
    public final static String CREATE_TIMESTAMP_PROPERTY = "create.timestamp";
    public final static String CHARACTER_ENCODING_PROPERTY = "character.encoding";

    public final static String COMMENT  = "Blob metadata";

    
    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final static DateTimeFormatter formatter =
            ISODateTimeFormat.dateTime();

    protected final File metaFile;
    protected final File dataFile;

    protected String code;
    protected String contentType;
    protected String filename;
    protected long size;
    protected DateTime createTimestamp;
    protected String characterEncoding;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public final static Logger logger = LoggerFactory.getLogger(Blob.class);

    //**************************************************************************
    // Constructor
    //**************************************************************************

    public Blob(@NotNull File metaFile, @NotNull File dataFile) {
        this.metaFile = metaFile;
        this.dataFile = dataFile;
    }

    //**************************************************************************
    // Methods
    //**************************************************************************

    public boolean createFiles() throws IOException {
        if (dataFile.createNewFile()) {
            logger.warn("Blob data file already exists: {}",
                    metaFile.getAbsolutePath());
            return false;
        }
        if (metaFile.createNewFile()) {
            logger.warn("Blob meta file already exists: {}",
                    metaFile.getAbsolutePath());
            return false;
        }
        return true;
    }

    public void saveMetaProperties() throws IOException {
        Properties metaProperties = new Properties();
        createTimestamp = new DateTime();

        safeSetProperty(metaProperties, CODE_PROPERTY, code);
        safeSetProperty(metaProperties, FILENAME_PROPERTY, filename);
        safeSetProperty(metaProperties, CONTENT_TYPE_PROPERTY, contentType);
        safeSetProperty(metaProperties, SIZE_PROPERTY, Long.toString(size));
        safeSetProperty(metaProperties, CREATE_TIMESTAMP_PROPERTY,
                formatter.print(createTimestamp));
        safeSetProperty(metaProperties, CHARACTER_ENCODING_PROPERTY, characterEncoding);

        OutputStream metaStream = null;
        try {
            metaStream = new FileOutputStream(metaFile);
            metaProperties.store(metaStream, COMMENT);
        } finally {
            IOUtils.closeQuietly(metaStream);
        }
    }

    protected void safeSetProperty(Properties metaProperties,
                                   String key, String value) {
        if (value == null) {
            metaProperties.remove(key);
        } else {
            metaProperties.setProperty(key, value);
        }
    }


    public void loadMetaProperties() throws IOException {
        Properties metaProperties = new Properties();

        InputStream metaStream = null;
        try {
            metaStream = new FileInputStream(metaFile);
            metaProperties.load(metaStream);

            code = metaProperties.getProperty(CODE_PROPERTY);
            filename = metaProperties.getProperty(FILENAME_PROPERTY);
            contentType = metaProperties.getProperty(CONTENT_TYPE_PROPERTY);
            size = Long.parseLong(metaProperties.getProperty(SIZE_PROPERTY));
            createTimestamp = formatter.parseDateTime(
                    metaProperties.getProperty(CREATE_TIMESTAMP_PROPERTY));
            characterEncoding =
                    metaProperties.getProperty(CHARACTER_ENCODING_PROPERTY);
        } finally {
            IOUtils.closeQuietly(metaStream);
        }
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************

    public File getMetaFile() {
        return metaFile;
    }

    public File getDataFile() {
        return dataFile;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public DateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    //**************************************************************************
    // Overrides
    //**************************************************************************


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blob)) return false;

        Blob blob = (Blob) o;

        if (size != blob.size) return false;
        if (characterEncoding != null ? !characterEncoding.equals(blob.characterEncoding) : blob.characterEncoding != null)
            return false;
        if (code != null ? !code.equals(blob.code) : blob.code != null)
            return false;
        if (contentType != null ? !contentType.equals(blob.contentType) : blob.contentType != null)
            return false;
        if (createTimestamp != null ? !createTimestamp.equals(blob.createTimestamp) : blob.createTimestamp != null)
            return false;
        if (dataFile != null ? !dataFile.equals(blob.dataFile) : blob.dataFile != null)
            return false;
        if (filename != null ? !filename.equals(blob.filename) : blob.filename != null)
            return false;
        if (metaFile != null ? !metaFile.equals(blob.metaFile) : blob.metaFile != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = metaFile != null ? metaFile.hashCode() : 0;
        result = 31 * result + (dataFile != null ? dataFile.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (createTimestamp != null ? createTimestamp.hashCode() : 0);
        result = 31 * result + (characterEncoding != null ? characterEncoding.hashCode() : 0);
        return result;
    }
}
