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

package com.manydesigns.portofino.util;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.PortofinoProperties;
import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TempFiles {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static int DEFAULT_RANDOM_CODE_LENGTH = 20;

    public final static Logger logger = LogUtil.getLogger(TempFiles.class);

    protected static final int codeLength;
    protected static final File tmpDir;

    static {
        Properties properties = PortofinoProperties.getProperties();
        String stringValue =
                properties.getProperty(
                        PortofinoProperties.RANDOM_CODE_LENGTH_PROPERTY);
        int tmp;
        try {
            tmp = Integer.parseInt(stringValue);
        } catch (Throwable e) {
            tmp = DEFAULT_RANDOM_CODE_LENGTH;
            LogUtil.finerMF(logger,
                    "Cannot use value ''{0}''. Using default: {2}",
                    stringValue, tmp);
        }
        codeLength = tmp;

        tmpDir = new File(System.getProperty("java.io.tmpdir"));
    }

    public static String createExportFileTemp() {
        return RandomStringUtils.randomAlphanumeric(codeLength);
    }

    public static File getTempFile(String fileNameFormat, String randomCode) {
        String fileName = MessageFormat.format(fileNameFormat, randomCode);
        return new File(tmpDir, fileName);
    }

}
