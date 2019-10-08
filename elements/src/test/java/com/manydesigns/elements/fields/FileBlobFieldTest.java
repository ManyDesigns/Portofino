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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.annotations.FileBlob;
import com.manydesigns.elements.annotations.MaxLength;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.blobs.BlobUtils;
import com.manydesigns.elements.blobs.SimpleBlobManager;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static org.testng.AssertJUnit.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class FileBlobFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    String sampleContent = "This is some content";
    String sampleFilename = "sample.txt";

    Form form;
    FileBlobField field;
    BlobManager blobManager;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        req.makeMultipart();

        File blobsDir = new File(new File(System.getProperty("java.io.tmpdir")), "blobs");
        blobManager = new SimpleBlobManager(blobsDir, "blob-{0}.properties", "blob-{0}.data");
    }

    protected void buildForm() {
        form = new FormBuilder(Bean.class).build();
        field = (FileBlobField) form.get(0).get(0);
    }

    public void testField1() throws Exception {
        buildForm();
        assertNull(field.getValue());
        assertEquals(20, field.blobCodeGenerator.call().length());

        InputStream is =
                new ByteArrayInputStream(sampleContent.getBytes());

        FileItem fileItem =
                new DiskFileItem(field.getInputName(), "text/plain",
                        false, sampleFilename, 0,
                        RandomUtil.getTempDir());
        OutputStream os = fileItem.getOutputStream();
        IOUtils.copy(is, os);

        req.setFileItem(field.getInputName(), fileItem);
        req.setParameter(field.getOperationInputName(), FileBlobField.UPLOAD_MODIFY);
        form.readFromRequest(req);

        Blob blob = field.getValue();
        assertNotNull(blob);
        assertNotNull(blob.getCode());
        assertEquals(sampleFilename, blob.getFilename());
        blobManager.save(blob);
        assertNotNull(blob.getCode());

        //Test keep
        req.setParameter(field.getCodeInputName(), blob.getCode());
        req.setParameter(field.getOperationInputName(), FileBlobField.UPLOAD_KEEP);
        buildForm();
        form.readFromRequest(req);
        assertTrue(form.validate());

        blob = field.getValue();
        BlobUtils.loadBlob(field, blobManager, true);
        blobManager.delete(blob); //Remove old one
        blobManager.save(blob);
        assertNotNull(blob);
        assertEquals(sampleFilename, blob.getFilename());
        assertEquals(sampleContent.length(), blob.getSize());

        //Test that modify with null content keeps the old content
        req.setParameter(field.getOperationInputName(), FileBlobField.UPLOAD_MODIFY);
        req.setFileItem(field.getInputName(), null);
        req.setParameter(field.getCodeInputName(), blob.getCode());
        buildForm();
        form.readFromRequest(req);
        assertTrue(form.validate());
        blob = field.getValue();
        assertNotNull(blob);
        assertEquals(req.getParameter(field.getCodeInputName()), blob.getCode());

        buildForm();
        field.setRequired(true);
        form.readFromRequest(req);
        assertTrue(form.validate());

        //Test delete
        req.setParameter(field.getOperationInputName(), FileBlobField.UPLOAD_DELETE);
        req.setFileItem(field.getInputName(), null);
        buildForm();
        form.readFromRequest(req);
        assertTrue(form.validate());

        blob = field.getValue();
        assertNull(blob);

        buildForm();
        field.setRequired(true);
        form.readFromRequest(req);
        assertFalse(form.validate());
        field.setRequired(false);
    }

    public static class Bean {
        @FileBlob
        @MaxLength(20)
        public String allegato;
    }

}
