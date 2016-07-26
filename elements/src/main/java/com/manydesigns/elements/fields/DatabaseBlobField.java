/*
 * Copyright (C) 2005-2016 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.DatabaseBlob;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DatabaseBlobField extends AbstractBlobField {
    public static final String copyright =
            "Copyright (C) 2005-2016, ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBlobField.class);

    protected final PropertyAccessor contentTypeAccessor;
    protected final PropertyAccessor fileNameAccessor;
    protected final PropertyAccessor timestampAccessor;

    public DatabaseBlobField(
            @NotNull ClassAccessor classAccessor, @NotNull PropertyAccessor accessor, @NotNull Mode mode,
            @Nullable String prefix) throws NoSuchFieldException {
        super(accessor, mode, prefix);
        DatabaseBlob databaseBlob = accessor.getAnnotation(DatabaseBlob.class);
        if(databaseBlob != null && !StringUtils.isEmpty(databaseBlob.contentTypeProperty())) {
            this.contentTypeAccessor = classAccessor.getProperty(databaseBlob.contentTypeProperty());
        } else {
            this.contentTypeAccessor = null;
        }
        if(databaseBlob != null && !StringUtils.isEmpty(databaseBlob.fileNameProperty())) {
            this.fileNameAccessor = classAccessor.getProperty(databaseBlob.fileNameProperty());
        } else {
            this.fileNameAccessor = null;
        }
        if(databaseBlob != null && !StringUtils.isEmpty(databaseBlob.timestampProperty())) {
            this.timestampAccessor = classAccessor.getProperty(databaseBlob.timestampProperty());
        } else {
            this.timestampAccessor = null;
        }
    }

    @Override
    public boolean isSaveBlobOnObject() {
        return true;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        if (obj == null) {
            forgetBlob();
        } else {
            byte[] value = (byte[]) accessor.get(obj);
            if(value == null) {
                forgetBlob();
            } else {
                blob = new Blob(null);
                blob.setSize(value.length);
                blob.setInputStream(new ByteArrayInputStream(value));
                if(fileNameAccessor != null) {
                    blob.setFilename((String) fileNameAccessor.get(obj));
                } else {
                    blob.setFilename("binary.blob");
                }
                if(contentTypeAccessor != null) {
                    blob.setContentType((String) contentTypeAccessor.get(obj));
                } else {
                    blob.setContentType("application/octet-stream");
                }
                if(timestampAccessor != null) {
                    DateTime dt = OgnlUtils.convertValue(timestampAccessor.get(obj), DateTime.class);
                    blob.setCreateTimestamp(dt != null ? dt : new DateTime());
                } else {
                    blob.setCreateTimestamp(new DateTime());
                }
            }
        }
    }

    public void writeToObject(Object obj) {
        if (blob == null) {
            writeToObject(obj, null);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                IOUtils.copyLarge(blob.getInputStream(), baos);
                writeToObject(obj, baos.toByteArray());
                if(fileNameAccessor != null) {
                    writeToObject(fileNameAccessor, obj, blob.getFilename());
                }
                if(contentTypeAccessor != null) {
                    writeToObject(contentTypeAccessor, obj, blob.getContentType());
                }
                if(timestampAccessor != null) {
                    Object ts = OgnlUtils.convertValue(blob.getCreateTimestamp(), timestampAccessor.getType());
                    if(ts == null) {
                        ts = OgnlUtils.convertValue(new DateTime(), timestampAccessor.getType());
                    }
                    writeToObject(timestampAccessor, obj, ts);
                }
            } catch (IOException e) {
                logger.error("Could not save blob", e);
                blobError = getText("elements.error.field.databaseblob.couldntSaveBlob");
            }
        }
    }

    @Override
    protected void loadBlob(BlobManager blobManager, Blob blob, boolean loadContents) throws IOException {
        if(!blob.isPropertiesLoaded()) {
            blobManager.loadMetadata(blob);
        }
        if(loadContents || blob.getInputStream() == null) {
            blobManager.openStream(blob);
        }
    }

    @Override
    public String generateNewCode() {
        return RandomUtil.createRandomId(25);
    }

    public PropertyAccessor getContentTypeAccessor() {
        return contentTypeAccessor;
    }

    public PropertyAccessor getFileNameAccessor() {
        return fileNameAccessor;
    }
}
