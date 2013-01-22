/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.reflection.TableAccessor;
import org.apache.commons.configuration.Configuration;
import org.hibernate.Session;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Facade to access application configuration, resources, and persistence.
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface Application {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    /**
     * Returns the id of the application.
     */
    String getAppId();

    /**
     * Returns the pretty name of the application.
     */
    String getName();

    //**************************************************************************
    // App directories and files
    //**************************************************************************

    /**
     * Returns the root directory of the application.
     */
    File getAppDir();
    /**
     * Returns the blobs directory of the application.
     */
    File getAppBlobsDir();
    /**
     * Returns the pages directory of the application.
     */
    File getPagesDir();
    /**
     * Returns the databases (Liquibase xml files) directory of the application.
     */
    File getAppDbsDir();
    /**
     * Returns the model file used by the application.
     */
    File getAppModelFile();
    /**
     * Returns the scripts directory of the application.
     */
    File getAppScriptsDir();
    /**
     * Returns the web pages directory of the application.
     */
    File getAppWebDir();


    //**************************************************************************
    // Model loading
    //**************************************************************************

    /**
     * Loads the model from its XML file.
     */
    void loadXmlModel();
    /**
     * Saves the model to its XML file.
     * @throws IOException if there is an error writing to the file
     * @throws  JAXBException if there is an error marhalling to XML.
     */
    void saveXmlModel() throws IOException, JAXBException;


    //**************************************************************************
    // Database stuff
    //**************************************************************************

    /**
     * Returns the ConnectionProvider associated to the given database.
     * @param databaseName the name of the database connection
     * @return the associated ConnectionProvider, or null if the database does not exist
     */
    ConnectionProvider getConnectionProvider(String databaseName);

    //**************************************************************************
    // Configuration access
    //**************************************************************************

    /**
     * Returns the global Configuration object for the entire Portofino instance.
     */
    Configuration getPortofinoProperties();

    //**************************************************************************
    // DatabasePlatformManager access
    //**************************************************************************

    /**
     * Returns the database platforms manager.
     */
    DatabasePlatformsManager getDatabasePlatformsManager();

    //**************************************************************************
    // Model access
    //**************************************************************************

    /**
     * Returns the model associated with this application.
     */
    Model getModel();

    /**
     * Attempts to synchronize the model with a database. Changes from the database
     * are propagated to the model, trying to preserve existing additional information
     * (e.g., custom property names, annotations, etc.)
     * @param databaseName the name of the database to synchronize.
     * @throws Exception if there is an error while syncing
     */
    void syncDataModel(String databaseName) throws Exception;

    //**************************************************************************
    // Persistance
    //**************************************************************************

    /**
     * Returns a Hibernate Session for the given database. The Session might
     * be freshly created or cached, depending on the implementation.
     * @param databaseName the name of the database.
     * @return a Session.
     */
    Session getSession(String databaseName);

    /**
     * Explicitly closes the current session, if any.
     * @param databaseName the name of the database to which the session to close
     * is associated.
     */
    void closeSession(String databaseName);

    /**
     * Closes all &quot;current&quot; (within the same request, thread, etc.) open sessions.
     */
    void closeSessions();

    //**************************************************************************
    // ClassAccessors management
    //**************************************************************************

    /**
     * Returns an accessor for a persistent entity.
     * @param database the name of the database. Must name an existing database.
     * @param entityName the name of the entity. Must name a mapped entity.
     * @return the table accessor
     */
    public TableAccessor getTableAccessor(String database, String entityName);

    //**************************************************************************
    // I18n
    //**************************************************************************

    /**
     * Returns a resource bundle for the given locale, on a best-effort basis: if
     * no resource bundle exists for the locale, the closest one is returned.
     * @param locale the target locale
     * @return the resource bundle best suited for that locale
     */
    ResourceBundle getBundle(Locale locale);

    /**
     * Returns the resource bundle manager.
     */
    ResourceBundleManager getResourceBundleManager();

    //**************************************************************************
    // Misc
    //**************************************************************************

    /**
     * Returns the application's {@link Configuration}.
     */
    Configuration getAppConfiguration();

    /**
     * Resets and initializes the model.
     */
    void initModel();

    /**
     * Shuts down the application, freeing all associated resources and performing the
     * necessary cleanup.
     */
    void shutdown();
}
