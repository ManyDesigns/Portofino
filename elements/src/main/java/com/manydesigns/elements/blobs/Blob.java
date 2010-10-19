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

import com.manydesigns.elements.logging.LogUtil;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Blob {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String CODE_PROPERTY = "code";
    public final static String FILENAME_PROPERTY = "filename";
    public final static String SIZE_PROPERTY = "size";
    public final static String CREATE_TIMESTAMP_PROPERTY = "create.timestamp";

    
    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final DateTimeFormatter formatter = ISODateTimeFormat.dateTime();


    protected final File metaFile;
    protected final File dataFile;

    protected String code;
    protected String filename;
    protected long size;
    protected DateTime createTimestamp;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public final static Logger logger = LogUtil.getLogger(Blob.class);

    //**************************************************************************
    // Constructor
    //**************************************************************************

    public Blob(File metaFile, File dataFile) {
        this.metaFile = metaFile;
        this.dataFile = dataFile;
    }

    //**************************************************************************
    // Methods
    //**************************************************************************

    public boolean createFiles() throws IOException {
        if (dataFile.createNewFile()) {
            LogUtil.warningMF(logger, "Blob data file already exists: {0}",
                    metaFile.getAbsolutePath());
            return false;
        }
        if (metaFile.createNewFile()) {
            LogUtil.warningMF(logger, "Blob meta file already exists: {0}",
                    metaFile.getAbsolutePath());
            return false;
        }
        return true;
    }

    public void saveMetaProperties() throws IOException {
        Properties metaProperties = new Properties();
        createTimestamp = new DateTime();

        metaProperties.setProperty(CODE_PROPERTY, code);
        metaProperties.setProperty(FILENAME_PROPERTY, filename);
        metaProperties.setProperty(SIZE_PROPERTY, Long.toString(size));
        metaProperties.setProperty(CREATE_TIMESTAMP_PROPERTY,
                formatter.print(createTimestamp));

        OutputStream metaStream = null;
        try {
            metaStream = new FileOutputStream(metaFile);
            metaProperties.store(metaStream, "comments");
        } finally {
            IOUtils.closeQuietly(metaStream);
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
            size = Long.parseLong(metaProperties.getProperty(SIZE_PROPERTY));
            createTimestamp = formatter.parseDateTime(
                    metaProperties.getProperty(CREATE_TIMESTAMP_PROPERTY));
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
}
