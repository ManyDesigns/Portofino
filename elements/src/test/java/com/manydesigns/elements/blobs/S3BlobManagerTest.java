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

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Properties;

import static org.testng.Assert.*;

/*
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class S3BlobManagerTest extends AbstractElementsTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    S3BlobManager manager;

    String sampleContent = "This is some content";
    String sampleFilename = "sample.txt";
    String sampleContentType = "text/plain";


    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        createManager();
    }

    protected void createManager() {
        manager = new S3BlobManager("AKIAXSHNNED3R6ZGQSAQ", "+QtASC/A462EHyHWPnx2B7PX0up+HPRQcJVv3owc", "eu-west-1", "test-portofino-s3-noencrypt-bucket");
    }

    @Test
    public void testManager1() {
        assertNotNull(manager);
    }

    @Test
    public void testBlob1() throws IOException {
        byte[] contentBytes = sampleContent.getBytes();
        Blob blob = new Blob(RandomUtil.createRandomId());
        blob.setInputStream(new ByteArrayInputStream(contentBytes));
        blob.setFilename(sampleFilename);
        blob.setContentType(sampleContentType);
        manager.save(blob);
        assertNotNull(blob);

        String code = blob.getCode();
        assertNotNull(code);
        assertEquals(RandomUtil.RANDOM_CODE_LENGTH, code.length());
        assertEquals(sampleFilename, blob.getFilename());
        assertEquals(sampleContentType, blob.getContentType());
        

        InputStream dataFile = manager.getDataFile(blob.getCode());
        Properties metaFile = manager.loadMetaProperties(blob.getCode());


        // verifica esistenza e corrispondenza contenuto
        assertTrue(dataFile.available()==1);
        // verifica esistenza e corrispondenza metadati
        System.out.println(metaFile);
        
        // ricarica il blob
        Blob blob2 = new Blob(code);
        manager.loadMetadata(blob2);
        assertNotNull(blob2);
        assertNotSame(blob, blob2);
        assertEquals(blob, blob2);
    }
    

}
