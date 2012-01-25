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
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import org.apache.commons.configuration.Configuration;
import org.hibernate.Session;

import java.io.File;
import java.util.List;
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
    File getAppConnectionsFile();
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
    void saveXmlModel();


    //**************************************************************************
    // Database stuff
    //**************************************************************************

    List<ConnectionProvider> getConnectionProviders();
    ConnectionProvider getConnectionProvider(String databaseName);
    void addDatabase(Database database);
    void deleteDatabases(String[] databases);
    void deleteDatabase(String database);
    void updateDatabase(Database database);
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

    Session getSessionByQualifiedTableName(String qualifiedTableName);

    void closeSessionByQualifiedTableName(String qualifiedTableName);

    void closeSession(String databaseName);

    void closeSessions();

    //**************************************************************************
    // ClassAccessors management
    //**************************************************************************

    public TableAccessor getTableAccessor(String qualifiedTableName);
    public TableAccessor getTableAccessor(String database, String entityName);

    //**************************************************************************
    // User
    //**************************************************************************

    public User findUserByEmail(String email);

    public User findUserByUserName(String username);

    public User findUserByToken(String token);

    Group getAllGroup();

    Group getAnonymousGroup();

    Group getRegisteredGroup();

    Group getAdministratorsGroup();

    void shutdown();

    //**************************************************************************
    // I18n
    //**************************************************************************

    ResourceBundle getBundle(Locale locale);

    File getAppTextDir();

    Configuration getAppConfiguration();
}
