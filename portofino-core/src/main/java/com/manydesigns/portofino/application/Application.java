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

package com.manydesigns.portofino.application;

import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.i18n.ResourceBundleManager;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.reflection.TableAccessor;
import org.apache.commons.configuration.Configuration;
import org.hibernate.Session;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public interface Application {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    String getAppId();
    String getName();

    //**************************************************************************
    // App directories and files
    //**************************************************************************

    File getAppDir();
    File getAppBlobsDir();
    File getPagesDir();
    File getAppDbsDir();
    File getAppModelFile();
    File getAppScriptsDir();
    File getAppStorageDir();
    File getAppWebDir();


    //**************************************************************************
    // Model loading
    //**************************************************************************

    void loadXmlModel();
    void saveXmlModel() throws IOException, JAXBException;


    //**************************************************************************
    // Database stuff
    //**************************************************************************

    ConnectionProvider getConnectionProvider(String databaseName);

    String getSystemDatabaseName();
    Database getSystemDatabase();

    //**************************************************************************
    // Configuration access
    //**************************************************************************

    Configuration getPortofinoProperties();

    //**************************************************************************
    // DatabasePlatformManager access
    //**************************************************************************

    DatabasePlatformsManager getDatabasePlatformsManager();

    //**************************************************************************
    // Model access
    //**************************************************************************

    Model getModel();
    void syncDataModel(String databaseName) throws Exception;

    //**************************************************************************
    // Persistance
    //**************************************************************************

    Session getSession(String databaseName);

    Session getSystemSession();

    void closeSession(String databaseName);

    void closeSessions();

    //**************************************************************************
    // ClassAccessors management
    //**************************************************************************

    public TableAccessor getTableAccessor(String database, String entityName);

    //**************************************************************************
    // I18n
    //**************************************************************************

    ResourceBundle getBundle(Locale locale);

    ResourceBundleManager getResourceBundleManager();

    //**************************************************************************
    // Misc
    //**************************************************************************

    Configuration getAppConfiguration();

    void initModel();

    void shutdown();
}
