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

package com.manydesigns.portofino.starter.migration.text;

import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.pageactions.text.TextAction;
import com.manydesigns.portofino.pageactions.text.configuration.Attachment;
import com.manydesigns.portofino.pageactions.text.configuration.TextConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MigrateTo408 {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(MigrateTo408.class);

    public static void main(String[] args) {
        String appDir = "";
        if(args.length > 0) {
            appDir = args[0];
        }
        DispatcherLogic.initConfigurationCache(0, 1);
        File appDirFile = new File(appDir);
        migrate(appDirFile);
    }

    public static void migrate(Application app) {
        migrate(app.getAppDir());
    }

    protected static void migrate(File appDirFile) {
        File pagesDirFile = new File(appDirFile, "pages");
        File storageDirFile = new File(appDirFile, "storage");
        if(!storageDirFile.isDirectory()) {
            return;
        }
        logger.info("Storage directory found: \"" + storageDirFile.getAbsolutePath() + "\". Attempting migration to version 4.0.8.");
        if(!pagesDirFile.isDirectory()) {
            throw new RuntimeException("Not a directory: " + pagesDirFile.getAbsolutePath());
        }
        try {
            Set<File> files = migrate(pagesDirFile, storageDirFile);
            for(File file : files) {
                file.delete();
            }
            if(storageDirFile.delete()) {
                logger.info("Storage directory deleted");
            } else {
                logger.warn("Storage directory could not be deleted");
            }
        } catch (Exception e) {
            logger.error("Migration failed", e);
            throw new RuntimeException(e);
        }
        logger.info("Migration of text pages completed. Remember to review permissions.");
    }

    private static Set<File> migrate(File dir, File storageDirFile) throws Exception {
        File textHtml = new File(dir, "text.html");
        Set<File> filesToDelete = new HashSet<File>();
        if(textHtml.exists()) {
            File configurationFile = new File(dir, "configuration.xml");
            if(configurationFile.exists()) {
                logger.info("Found text page: " + dir);
                TextConfiguration configuration =
                        DispatcherLogic.getConfiguration(configurationFile, null, TextConfiguration.class);
                for(Attachment attachment : configuration.getAttachments()) {
                    File attFile = RandomUtil.getCodeFile(
                            storageDirFile, TextAction.ATTACHMENT_FILE_NAME_PATTERN, attachment.getId());
                    if(attFile.exists()) {
                        File destFile = new File(dir, attFile.getName());
                        FileUtils.copyFile(attFile, destFile);
                        filesToDelete.add(attFile);
                        logger.info("Attachment copied: " + destFile);
                    }
                }
            }
        }
        for(File file : dir.listFiles()) {
            if(file.isDirectory()) {
                filesToDelete.addAll(migrate(file, storageDirFile));
            }
        }
        return filesToDelete;
    }

}
