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

package com.manydesigns.elements.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ElementsFileUtils {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ElementsFileUtils.class);

    //**************************************************************************
    // Methods
    //**************************************************************************

    public static String getRelativePath(File ancestor, File file) {
	return getRelativePath(ancestor, file, System.getProperty("file.separator"));
    }

    public static String getRelativePath(File ancestor, File file, String separator) {
        String path = file.getName();
        File parent = file.getParentFile();
        while (parent != null && !parent.equals(ancestor)) {
            path = parent.getName() + separator + path;
            parent = parent.getParentFile();
        }
        return path;
    }

    public static boolean ensureDirectoryExistsAndWritable(File file) {
        logger.debug("Ensure directory exists and writable: {}", file);
        if (file.exists()) {
            logger.debug("File esists");
            if (file.isDirectory()) {
                logger.debug("File is a directory");
            } else {
                logger.warn("Not a directory: {}", file);
                return false;
            }
        } else {
            logger.debug("File does not exist");
            if (file.mkdirs()) {
                logger.info("Directory created successfully: {}", file);
            } else {
                logger.warn("Cannot create directory: {}", file);
                return false;
            }
        }
        if (!file.canWrite()) {
            logger.warn("Directory not writable: {}", file);
            return false;
        } else {
            logger.debug("Success");
            return true;
        }
    }


    public static boolean setReadable(File file, boolean readable) {
        String perms = readable ? "u+rx" : "a-rx";
        return chmod(file, perms);
    }

    public static boolean setWritable(File file, boolean writable) {
        String perms = writable ? "u+w" : "a-w";
        return chmod(file, perms);
    }

    public static boolean chmod(File file, String perms) {
        logger.debug("chmod {} {}", perms, file.getAbsolutePath());
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(new String[]{
                    "chmod",
                    perms,
                    file.getAbsolutePath()
            });
            int result = process.waitFor();
            return result == 0;
        } catch (Exception e) {
            return false;
        }
    }


}
