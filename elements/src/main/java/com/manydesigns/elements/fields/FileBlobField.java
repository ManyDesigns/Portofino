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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

/**
 * Blob field that saves blobs on files using a {@link com.manydesigns.elements.blobs.BlobManager}. Blob loading and
 * saving must be performed manually outside of the Elements lifecycle.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class FileBlobField extends AbstractBlobField {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public final Callable<String> DEFAULT_CODE_GENERATOR = () -> RandomUtil.createRandomId(size);
    protected Callable<String> blobCodeGenerator = DEFAULT_CODE_GENERATOR;

    public FileBlobField(@NotNull PropertyAccessor accessor,
                         @NotNull Mode mode,
                         @Nullable String prefix) {
        super(accessor, mode, prefix);
    }

    public FileBlobField(@NotNull PropertyAccessor accessor,
                         @NotNull Mode mode,
                         @Nullable String prefix,@Nullable String encryptionType) {
        super(accessor, mode, prefix);
        this.encryptionType = encryptionType;
    }

    @Override
    public boolean isSaveBlobOnObject() {
        return false;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        if (obj == null) {
            forgetBlob();
        } else {
            String code  = (String) accessor.get(obj);
            if(StringUtils.isBlank(code)) {
                forgetBlob();
            } else {
                blob = new Blob(code);
                blob.setEncryptionType(encryptionType);
            }
        }
    }

    public void writeToObject(Object obj) {
        if (blob == null) {
            writeToObject(obj, null);
        } else {
            writeToObject(obj, blob.getCode());
        }
    }

    public String generateNewCode() {
        try {
            return blobCodeGenerator.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Callable<String> getBlobCodeGenerator() {
        return blobCodeGenerator;
    }

    public void setBlobCodeGenerator(Callable<String> blobCodeGenerator) {
        this.blobCodeGenerator = blobCodeGenerator;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }
}
