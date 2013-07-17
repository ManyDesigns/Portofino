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

package com.manydesigns.elements.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ElementsFileUtils {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ElementsFileUtils.class);

    //**************************************************************************
    // Methods
    //**************************************************************************

    public static String getRelativePath(File ancestor, File file) {
        return getRelativePath(ancestor, file, "/");
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
        if (!ensureDirectoryExists(file)) return false;
        if (!file.canWrite()) {
            logger.warn("Directory not writable: {}", file);
            return false;
        } else {
            logger.debug("Success");
            return true;
        }
    }

    public static boolean ensureDirectoryExists(File file) {
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
            if (safeMkdirs(file)) {
                logger.info("Directory created successfully: {}", file);
            } else {
                logger.warn("Cannot create directory: {}", file);
                return false;
            }
        }
        return true;
    }

    public static boolean ensureDirectoryExistsAndWarnIfNotWritable(File file) {
        logger.debug("Ensure directory exists and writable: {}", file);
        if (!ensureDirectoryExists(file)) return false;
        try {
            if (!file.canWrite()) {
                logger.warn("Directory not writable: {}", file);
            } else {
                logger.debug("Success");
            }
        } catch (Exception e) {
            logger.warn("Directory not writable: " + file, e);
        }
        return true;
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

    public static boolean safeMkdir(File detailDirectory) {
        try {
            return detailDirectory.mkdir();
        } catch (SecurityException e) {
            logger.error("mkdir failed, security exception", e);
            return false;
        }
    }

    public static boolean safeMkdirs(File detailDirectory) {
        try {
            return detailDirectory.mkdirs();
        } catch (SecurityException e) {
            logger.error("mkdir failed, security exception", e);
            return false;
        }
    }

    public static void moveFileSafely(File tempFile, String fileName) throws IOException {
        File destination = new File(fileName);
        if(!destination.exists()) {
            FileUtils.moveFile(tempFile, destination);
        } else {
            File backup = File.createTempFile(destination.getName(), ".backup", destination.getParentFile());
            if (!backup.delete()) {
                logger.warn("Cannot delete: {}", backup);
            }
            FileUtils.moveFile(destination, backup);
            FileUtils.moveFile(tempFile, destination);
            if (!backup.delete()) {
                logger.warn("Cannot delete: {}", backup);
            }
        }
    }
}
