/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Blob {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(Blob.class);

    public final static String FILENAME_PROPERTY = "filename";
    public final static String CONTENT_TYPE_PROPERTY = "content.type";
    public final static String SIZE_PROPERTY = "size";
    public final static String CREATE_TIMESTAMP_PROPERTY = "create.timestamp";
    public final static String CHARACTER_ENCODING_PROPERTY = "character.encoding";
    public final static String REPOSITORY_PROPERTY = "repository";
    public final static String REPOSITORY_ID_PROPERTY = "repository.id";
    public final static String ENCRYPTION_PROPERTY = "encryption.type";
    public final static String CUSTOM_DATA_PROPERTY = "custom.data";

    protected final DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

    protected final String code;
    protected String contentType;
    protected String filename;
    protected long size;
    protected DateTime createTimestamp;
    protected String characterEncoding;
    protected InputStream inputStream;
    protected boolean propertiesLoaded;
    protected String encryptionType;
    protected String repository;
    protected String repositoryId;
    protected Map customData;

    public Blob(String code) {
        this.code = code;
    }

    protected void safeSetProperty(Properties metaProperties, String key, String value) {
        if (value == null) {
            metaProperties.remove(key);
        } else {
            metaProperties.setProperty(key, value);
        }
    }

    public Properties getMetaProperties() throws IOException {
        Properties metaProperties = new Properties();
        if(createTimestamp == null) {
            createTimestamp = new DateTime();
        }

        safeSetProperty(metaProperties, FILENAME_PROPERTY, filename);
        safeSetProperty(metaProperties, CONTENT_TYPE_PROPERTY, contentType);
        safeSetProperty(metaProperties, SIZE_PROPERTY, Long.toString(size));
        safeSetProperty(metaProperties, CREATE_TIMESTAMP_PROPERTY, formatter.print(createTimestamp));
        safeSetProperty(metaProperties, CHARACTER_ENCODING_PROPERTY, characterEncoding);
        safeSetProperty(metaProperties, REPOSITORY_PROPERTY, repository);
        safeSetProperty(metaProperties, REPOSITORY_ID_PROPERTY, repositoryId);
        safeSetProperty(metaProperties, ENCRYPTION_PROPERTY, encryptionType);
        safeSetProperty(metaProperties, CUSTOM_DATA_PROPERTY, getSerializedCustomData());

        return metaProperties;
    }

    public void setMetaProperties(Properties metaProperties) throws IOException {
        filename = metaProperties.getProperty(FILENAME_PROPERTY);
        contentType = metaProperties.getProperty(CONTENT_TYPE_PROPERTY);
        size = Long.parseLong(metaProperties.getProperty(SIZE_PROPERTY));
        createTimestamp = formatter.parseDateTime(metaProperties.getProperty(CREATE_TIMESTAMP_PROPERTY));
        characterEncoding = metaProperties.getProperty(CHARACTER_ENCODING_PROPERTY);
        repository = metaProperties.getProperty(REPOSITORY_PROPERTY);
        repositoryId = metaProperties.getProperty(REPOSITORY_ID_PROPERTY);
        encryptionType=metaProperties.getProperty(ENCRYPTION_PROPERTY);
        setCustomData(metaProperties.getProperty(CUSTOM_DATA_PROPERTY));
        propertiesLoaded = true;
    }

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

    public void setCreateTimestamp(DateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
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

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
    }

    public boolean isPropertiesLoaded() {
        return propertiesLoaded;
    }

    public void setPropertiesLoaded(boolean propertiesLoaded) {
        this.propertiesLoaded = propertiesLoaded;
    }

    public void dispose() {
        try {
            if(inputStream!=null)
                inputStream.close();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public String getRepository() {
        return repository;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public Boolean isEncrypted(){
        return this.encryptionType!=null;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public Map getCustomData() {
        return customData;
    }

    public void setCustomData(Map customData) {
        this.customData = customData;
    }

    public String getSerializedCustomData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(customData);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public void setCustomData(String customData) {
        if (customData != null && !customData.equals("")) {
            try {
                logger.debug("Reading customData from: "+customData);
                ObjectMapper mapper = new ObjectMapper();
                this.customData = mapper.readValue(customData, Map.class);
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Blob blob = (Blob) o;

        return code != null ? code.equals(blob.code) : blob.code == null;
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}
