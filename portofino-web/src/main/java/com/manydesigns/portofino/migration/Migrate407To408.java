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

package com.manydesigns.portofino.migration;

import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.pageactions.text.TextAction;
import com.manydesigns.portofino.pageactions.text.configuration.Attachment;
import com.manydesigns.portofino.pageactions.text.configuration.TextConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Migrate407To408 {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static void main(String[] args) {
        String appDir = "";
        if(args.length > 0) {
            appDir = args[0];
        }
        File appDirFile = new File(appDir);
        File pagesDirFile = new File(appDirFile, "pages");
        File storageDirFile = new File(appDirFile, "storage");
        if(!pagesDirFile.isDirectory()) {
            System.err.println("Not a directory: " + pagesDirFile.getAbsolutePath());
            System.exit(1);
        }
        DispatcherLogic.initConfigurationCache(0, 1);
        try {
            migrate(pagesDirFile, storageDirFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Migration failed.");
            System.exit(2);
        }
    }

    private static void migrate(File dir, File storageDirFile) throws Exception {
        File textHtml = new File(dir, "text.html");
        if(textHtml.exists()) {
            File configurationFile = new File(dir, "configuration.xml");
            if(configurationFile.exists()) {
                System.out.println("Found text page: " + dir);
                TextConfiguration configuration =
                        DispatcherLogic.getConfiguration(configurationFile, null, TextConfiguration.class);
                for(Attachment attachment : configuration.getAttachments()) {
                    File attFile = RandomUtil.getCodeFile(
                            storageDirFile, TextAction.ATTACHMENT_FILE_NAME_PATTERN, attachment.getId());
                    if(attFile.exists()) {
                        File destFile = new File(dir, attFile.getName());
                        FileUtils.moveFile(attFile, destFile);
                        System.out.println("Attachment moved: " + destFile);
                    }
                }
            }
        }
        for(File file : dir.listFiles()) {
            if(file.isDirectory()) {
                migrate(file, storageDirFile);
            }
        }
    }

}
