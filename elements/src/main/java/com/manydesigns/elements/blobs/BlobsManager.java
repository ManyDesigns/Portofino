/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.elements.util.RandomUtil;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class BlobsManager {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Static fields
    //**************************************************************************

    protected static final Properties elementsProperties;
    protected static final BlobsManager manager;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LogUtil.getLogger(BlobsManager.class);

    //**************************************************************************
    // Static initialization and methods
    //**************************************************************************

    static {
        elementsProperties = ElementsProperties.getProperties();
        String managerClassName =
                elementsProperties.getProperty(
                        ElementsProperties.BLOBS_MANAGER_PROPERTY);
        InstanceBuilder<BlobsManager> builder =
                new InstanceBuilder<BlobsManager>(
                        BlobsManager.class,
                        BlobsManager.class,
                        logger);
        manager = builder.createInstance(managerClassName);
    }

    public static BlobsManager getManager() {
        return manager;
    }


    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final File blobsDir;
    protected final String metaFileNamePattern;
    protected final String dataFileNamePattern;

    
    //**************************************************************************
    // Constructors and initialization
    //**************************************************************************

    public BlobsManager() {
        String blobsDirPath =
                elementsProperties.getProperty(
                        ElementsProperties.BLOBS_DIR_PROPERTY);
        if (blobsDirPath == null) {
            blobsDirPath = System.getProperty("java.io.tmpdir");
            LogUtil.warningMF(logger,
                    "Blobs dir property ''{0}'' not set. " +
                            "Falling back to ''java.io.tmpdir'': {1}",
                    ElementsProperties.BLOBS_DIR_PROPERTY,
                    blobsDirPath);
        }
        blobsDir = new File(blobsDirPath);
        metaFileNamePattern = elementsProperties.getProperty(
                ElementsProperties.BLOBS_META_FILENAME_PATTERN_PROPERTY);
        dataFileNamePattern = elementsProperties.getProperty(
                ElementsProperties.BLOBS_DATA_FILENAME_PATTERN_PROPERTY);
    }

    //**************************************************************************
    // Methods
    //**************************************************************************

    public Blob saveBlob(File sourceFile, String fileName) throws IOException {
        String code = RandomUtil.createRandomCode();

        File metaFile =
                RandomUtil.getCodeFile(blobsDir, metaFileNamePattern, code);
        File dataFile =
                RandomUtil.getCodeFile(blobsDir, dataFileNamePattern, code);
        Blob blob = new Blob(metaFile, dataFile);

        // copy the data
        long size = IOUtils.copyLarge(
                new FileReader(sourceFile),
                new FileWriter(dataFile));

        // set and save the metadata
        blob.setCode(code);
        blob.setFilename(fileName);
        blob.setSize(size);
        blob.saveMetaProperties();

        return blob;
    }

    public Blob loadBlob(String code) throws IOException {
        File metaFile =
                RandomUtil.getCodeFile(blobsDir, metaFileNamePattern, code);
        File dataFile =
                RandomUtil.getCodeFile(blobsDir, dataFileNamePattern, code);
        Blob blob = new Blob(metaFile, dataFile);

        blob.loadMetaProperties();

        return blob;
    }

    //**************************************************************************
    // Getters
    //**************************************************************************

    public File getBlobsDir() {
        return blobsDir;
    }

    public String getMetaFileNamePattern() {
        return metaFileNamePattern;
    }

    public String getDataFileNamePattern() {
        return dataFileNamePattern;
    }
}
