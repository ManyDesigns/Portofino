/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.elements.util;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class RandomUtil {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public final static int RANDOM_CODE_LENGTH = 25;

    public final static Logger logger =
            LoggerFactory.getLogger(RandomUtil.class);

    protected static final File tempDir;

    static {
        tempDir = new File(System.getProperty("java.io.tmpdir"));
    }

    public static File getTempDir() {
        return tempDir;
    }

    public static String createRandomId() {
        return createRandomId(RANDOM_CODE_LENGTH);
    }

    public static String createRandomId(int length) {
        return RandomStringUtils.random(length, "abcdefghijklmnopqrstuvwxyz0123456789");
    }

    public static File getTempCodeFile(String fileNameFormat,
                                       String randomCode) {
        return getCodeFile(tempDir, fileNameFormat, randomCode);
    }

    public static File getCodeFile(File dir,
                                   String fileNameFormat,
                                   String code) {
        return new File(dir, getCodeFileName(fileNameFormat, code));
    }

    public static String getCodeFileName(String fileNameFormat,
                                         String randomCode) {
        return MessageFormat.format(fileNameFormat, randomCode);
    }

}
