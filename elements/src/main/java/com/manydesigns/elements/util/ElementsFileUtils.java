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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
