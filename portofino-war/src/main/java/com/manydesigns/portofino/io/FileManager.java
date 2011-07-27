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
package com.manydesigns.portofino.io;

import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.commons.transaction.util.LoggerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class FileManager {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected FileResourceManager frm;
    protected final String storeDir;
    protected final String workDir;



    public static final Logger logger =
            LoggerFactory.getLogger(FileManager.class);

    //**************************************************************************
    // Constructor
    //**************************************************************************
    public FileManager(String storeDir, String workDir) {
        LoggerFacade loggerFacade = new Slf4jLoggerFacade(logger);
        this.storeDir=storeDir;
        this.workDir=workDir;        
        frm = new FileResourceManager(storeDir, workDir, false, loggerFacade);
    }

    public void start() throws Exception{
        frm.start();
    }

    public void stop() throws Exception{
        frm.stop(FileResourceManager.SHUTDOWN_MODE_NORMAL);
    }

    //**************************************************************************
    // Getter and Setter
    //**************************************************************************
    public FileResourceManager getFrm() {
        return frm;
    }

    public String getStoreDir() {
        return storeDir;
    }

    public String getWorkDir() {
        return workDir;
    }
}
