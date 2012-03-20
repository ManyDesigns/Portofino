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

package com.manydesigns.portofino.application;

import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.i18n.ResourceBundleManager;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.ConnectionProvider;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
