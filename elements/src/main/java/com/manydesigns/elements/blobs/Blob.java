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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class Blob {
    public static final String copyright =
            "Copyright (c) 2005-2014, ManyDesigns srl";

    public final static String FILENAME_PROPERTY = "filename";
    public final static String CONTENT_TYPE_PROPERTY = "content.type";
    public final static String SIZE_PROPERTY = "size";
    public final static String CREATE_TIMESTAMP_PROPERTY = "create.timestamp";
    public final static String CHARACTER_ENCODING_PROPERTY = "character.encoding";

    protected final DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

    protected final String code;
    protected String contentType;
    protected String filename;
    protected long size;
    protected DateTime createTimestamp;
    protected String characterEncoding;

    public Blob(String code) {
        this.code = code;
    }

    protected void safeSetProperty(Properties metaProperties,
                                   String key, String value) {
        if (value == null) {
            metaProperties.remove(key);
        } else {
            metaProperties.setProperty(key, value);
        }
    }

    public void saveMetaProperties() throws IOException {
        Properties metaProperties = new Properties();
        createTimestamp = new DateTime();

        safeSetProperty(metaProperties, FILENAME_PROPERTY, filename);
        safeSetProperty(metaProperties, CONTENT_TYPE_PROPERTY, contentType);
        safeSetProperty(metaProperties, SIZE_PROPERTY, Long.toString(size));
        safeSetProperty(metaProperties, CREATE_TIMESTAMP_PROPERTY, formatter.print(createTimestamp));
        safeSetProperty(metaProperties, CHARACTER_ENCODING_PROPERTY, characterEncoding);

        storeMetaProperties(metaProperties);
    }

    protected abstract void storeMetaProperties(Properties metaProperties) throws IOException;

    public void loadMetaProperties() throws IOException {
        Properties metaProperties = readMetaProperties();
        filename = metaProperties.getProperty(FILENAME_PROPERTY);
        contentType = metaProperties.getProperty(CONTENT_TYPE_PROPERTY);
        size = Long.parseLong(metaProperties.getProperty(SIZE_PROPERTY));
        createTimestamp = formatter.parseDateTime(metaProperties.getProperty(CREATE_TIMESTAMP_PROPERTY));
        characterEncoding = metaProperties.getProperty(CHARACTER_ENCODING_PROPERTY);
    }

    protected abstract Properties readMetaProperties() throws IOException;

    public abstract InputStream getInputStream() throws IOException;

    //**************************************************************************
    // Getter/setter
    //**************************************************************************


    public String getCode() {
        return code;
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

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        Blob blob = (Blob) o;
        return code.equals(blob.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
