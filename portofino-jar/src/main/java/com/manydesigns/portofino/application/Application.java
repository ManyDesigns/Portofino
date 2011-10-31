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

import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.crud.Crud;
import com.manydesigns.portofino.reflection.CrudAccessor;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import org.apache.commons.configuration.Configuration;
import org.hibernate.Session;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.List;


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

    //**************************************************************************
    // App directories and files
    //**************************************************************************

    File getAppDir();
    File getAppBlobsDir();
    File getAppConnectionsFile();
    File getAppDbsDir();
    File getAppModelFile();
    File getAppScriptsDir();
    File getAppStorageDir();
    File getAppWebDir();


    //**************************************************************************
    // Model loading
    //**************************************************************************

    void loadConnections();
    void loadXmlModel();
    void saveXmlModel();


    //**************************************************************************
    // Database stuff
    //**************************************************************************

    List<ConnectionProvider> getConnectionProviders();
    ConnectionProvider getConnectionProvider(String databaseName);
    void addConnectionProvider(ConnectionProvider connectionProvider);
    void deleteConnectionProvider(String[] connectionProvider);
    void deleteConnectionProvider(String connectionProvider);
    void updateConnectionProvider(ConnectionProvider connectionProvider);

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

    Session getSessionByDatabaseName(String databaseName);

    Session getSession(String qualifiedTableName);

    Object getObjectByPk(String qualifiedTableName, Serializable pk);

    Object getObjectByPk(String qualifiedTableName, Serializable pk, String queryString, Object rootObject);

    List getAllObjects(String qualifiedTableName);

    List<Object> getObjects(TableCriteria criteria, @Nullable Integer firstResult, @Nullable Integer maxResults);

    List<Object> getObjects(String queryString, Object rootObject, @Nullable Integer firstResult, @Nullable Integer maxResults);

    List<Object> getObjects(String queryString, @Nullable Integer firstResult, @Nullable Integer maxResults);

    List<Object> getObjects(String queryString, TableCriteria criteria, Object rootObject, @Nullable Integer firstResult, @Nullable Integer maxResults);

    void saveObject(String qualifiedTableName, Object obj);

    void updateObject(String qualifiedTableName, Object obj);

    void deleteObject(String qualifiedTableName, Object obj);

    List<Object[]> runSql(String databaseName, String sql);

    void closeSessions();

    void commit(String databaseName);

    void rollback(String databaseName);

    void commit();

    void rollback();

    String getQualifiedTableNameFromQueryString(String queryString);

    List<Object> getRelatedObjects(String qualifiedTableName,
            Object obj, String oneToManyRelationshipName);

    List<String> getDDLCreate();

    List<String> getDDLUpdate();

    //**************************************************************************
    // ClassAccessors management
    //**************************************************************************

    public TableAccessor getTableAccessor(String qualifiedTableName);
    public CrudAccessor getCrudAccessor(Crud crud);

    //**************************************************************************
    // User
    //**************************************************************************

    public User findUserByEmail(String email);

    public User findUserByUserName(String username);

    public User findUserByToken(String token);

    Group getAnonymousGroup();

    Group getRegisteredGroup();

    Group getAdministratorsGroup();

    void shutdown();

    List<Object> runHqlQuery(
            String queryString,
            @Nullable Object[] parameters
    );

    List<Object> runHqlQuery(
            String queryString,
            @Nullable Object[] parameters,
            @Nullable Integer firstResult,
            @Nullable Integer maxResults
    );

    List<Object> runHqlQuery(String qualifiedTableName,
                             String queryString,
                             @Nullable Object[] parameters
    );

    List<Object> runHqlQuery(
            String qualifiedTableName,
            String queryString,
            @Nullable Object[] parameters,
            @Nullable Integer firstResult,
            @Nullable Integer maxResults
    );

    QueryStringWithParameters mergeQuery
            (String queryString, TableCriteria criteria, Object rootObject);
}
