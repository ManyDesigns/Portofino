/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.blobs;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class BlobTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    BlobManager manager;
    File blobsDir;

    String sampleContent = "This is some content";
    String sampleFilename = "sample.txt";
    String sampleContentType = "text/plain";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        manager = ElementsThreadLocals.getBlobManager();
        blobsDir = manager.getBlobsDir();
    }

    public void testManager1() {
        assertNotNull(manager);

        assertNotNull(blobsDir);
        assertEquals(System.getProperty("java.io.tmpdir"),
                blobsDir.getAbsolutePath() + File.separator);
        assertEquals("blob-{0}.properties", manager.getMetaFileNamePattern());
        assertEquals("blob-{0}.data", manager.getDataFileNamePattern());
    }

    public void testBlob1() throws IOException {
        byte[] contentBytes = sampleContent.getBytes();
        Blob blob = manager.saveBlob(
                RandomUtil.createRandomId(), contentBytes, sampleFilename, sampleContentType, null);
        assertNotNull(blob);

        String code = blob.getCode();
        assertNotNull(code);
        assertEquals(RandomUtil.RANDOM_CODE_LENGTH, code.length());
        assertEquals(sampleFilename, blob.getFilename());
        assertEquals(sampleContentType, blob.getContentType());
        assertEquals(contentBytes.length, blob.getSize());

        File dataFile = blob.getDataFile();
        File metaFile = blob.getMetaFile();

        String expectedMetaFilename =
                RandomUtil.getCodeFileName(
                        manager.getMetaFileNamePattern(), code);
        assertEquals(expectedMetaFilename, metaFile.getName());

        String expectedDataFilename =
                RandomUtil.getCodeFileName(
                        manager.getDataFileNamePattern(), code);
        assertEquals(expectedDataFilename, dataFile.getName());

        // verifica esistenza e corrispondenza contenuto
        assertTrue(dataFile.exists());
        assertEquals(contentBytes.length, dataFile.length());
        assertTrue(IOUtils.contentEquals(
                new ByteArrayInputStream(contentBytes),
                new FileInputStream(dataFile)));

        // verifica esistenza e corrispondenza metadati
        assertTrue(metaFile.exists());
        // verifica proprietà
        Properties properties = new Properties();
        properties.load(new FileInputStream(metaFile));
        assertEquals(code,
                properties.getProperty(Blob.CODE_PROPERTY));
        assertEquals(blob.getFilename(),
                properties.getProperty(Blob.FILENAME_PROPERTY));
        assertEquals(blob.getContentType(),
                properties.getProperty(Blob.CONTENT_TYPE_PROPERTY));
        assertEquals(Long.toString(blob.getSize()),
                properties.getProperty(Blob.SIZE_PROPERTY));
        assertNotNull(properties.getProperty(Blob.CREATE_TIMESTAMP_PROPERTY));

        // ricarica il blob
        Blob blob2 = manager.loadBlob(code);
        assertNotNull(blob2);
        assertNotSame(blob, blob2);
        assertEquals(blob, blob2);
    }
}
