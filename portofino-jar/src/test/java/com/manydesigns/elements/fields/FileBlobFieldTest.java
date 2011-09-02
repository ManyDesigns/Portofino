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

package com.manydesigns.elements.fields;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.annotations.FileBlob;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class FileBlobFieldTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    BlobManager manager;

    String sampleContent = "This is some content";
    String sampleFilename = "sample.txt";

    Form form;
    FileBlobField field;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // use plain servlet api
        elementsConfiguration.setProperty(
                ElementsProperties.WEB_FRAMEWORK,
                com.manydesigns.elements.servlet.WebFramework.class.getName());
        setUpSingletons();

        manager = FileBlobField.createBlobsManager();

        form = new FormBuilder(Bean.class)
                .build();
        field = (FileBlobField) form.get(0).get(0);
    }

    public void testField1() throws IOException {
        assertNull(field.getBlob());

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

        Blob blob = field.getBlob();
        assertNotNull(blob);
        assertEquals(sampleFilename, blob.getFilename());
        assertEquals(sampleContent.length(), blob.getSize());
    }

    public static class Bean {
        @FileBlob
        public String allegato;
    }
}
