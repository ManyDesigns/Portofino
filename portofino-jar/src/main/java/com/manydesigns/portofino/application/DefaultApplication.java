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

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.application.hibernate.HibernateConfig;
import com.manydesigns.portofino.application.hibernate.HibernateDatabaseSetup;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.i18n.ResourceBundleManager;
import com.manydesigns.portofino.model.datamodel.DataModelLogic;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.Dialect;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class DefaultApplication implements Application {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String changelogFileNameTemplate = "{0}-changelog.xml";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final org.apache.commons.configuration.Configuration portofinoConfiguration;
    protected final org.apache.commons.configuration.Configuration appConfiguration;
    protected final DatabasePlatformsManager databasePlatformsManager;
    protected List<ConnectionProvider> connectionProviders;
    protected Model model;
    protected Map<String, HibernateDatabaseSetup> setups;

    protected final String appId;

    protected final File appDir;
    protected final File appBlobsDir;
    protected final File appConnectionsFile;
    protected final File appDbsDir;
    protected final File appModelFile;
    protected final File appScriptsDir;
    protected final File appTextDir;
    protected final File appStorageDir;
    protected final File appWebDir;

    protected final ResourceBundleManager resourceBundleManager;


    public static final Logger logger =
            LoggerFactory.getLogger(DefaultApplication.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public DefaultApplication(String appId,
                              org.apache.commons.configuration.Configuration portofinoConfiguration,
                              DatabasePlatformsManager databasePlatformsManager,
                              File appDir,
                              File appBlobsDir,
                              File appConnectionsFile,
                              File appDbsDir,
                              File appModelFile,
                              File appScriptsDir,
                              File appTextDir,
                              File appStorageDir,
                              File appWebDir
    ) throws ConfigurationException {
        this.appId = appId;
        this.portofinoConfiguration = portofinoConfiguration;
        this.databasePlatformsManager = databasePlatformsManager;
        this.appDir = appDir;
        this.appBlobsDir = appBlobsDir;
        this.appConnectionsFile = appConnectionsFile;
        this.appDbsDir = appDbsDir;
        this.appModelFile = appModelFile;
        this.appScriptsDir = appScriptsDir;
        this.appTextDir = appTextDir;
        this.appStorageDir = appStorageDir;
        this.appWebDir = appWebDir;

        resourceBundleManager = new ResourceBundleManager(appDir);
        File appConfigurationFile = new File(appDir, AppProperties.PROPERTIES_RESOURCE);
        appConfiguration = new PropertiesConfiguration(appConfigurationFile);
    }

    //**************************************************************************
    // Model loading
    //**************************************************************************

    public synchronized void loadXmlModel() {
        logger.info("Loading xml model from file: {}",
                appModelFile.getAbsolutePath());

        try {
            JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
            Unmarshaller um = jc.createUnmarshaller();
            Model loadedModel = (Model) um.unmarshal(appModelFile);
            initConnections(loadedModel);
            boolean syncOnStart = false;
            if (syncOnStart) {
                for (ConnectionProvider connectionProvider :
                        connectionProviders) {
                    String databaseName = connectionProvider.getDatabase().getDatabaseName();
                    Database sourceDatabase =
                            DataModelLogic.findDatabaseByName(loadedModel, databaseName);
                    DatabaseSyncer dbSyncer = new DatabaseSyncer(connectionProvider);
                    Database targetDatabase = dbSyncer.syncDatabase(loadedModel);
                    loadedModel.getDatabases().remove(sourceDatabase);
                    loadedModel.getDatabases().add(targetDatabase);
                }
            }
            loadedModel.init();
            installDataModel(loadedModel);
        } catch (Exception e) {
            logger.error("Cannot load/parse model: " + appModelFile, e);
        }
    }

    protected void initConnections(Model loadedModel) {
        loadedModel.initDatabases(getDatabasePlatformsManager(), getAppDir());
        connectionProviders = new ArrayList<ConnectionProvider>();
        for(Database db : loadedModel.getDatabases()) {
            ConnectionProvider connectionProvider = db.getConnectionProvider();
            if(connectionProvider != null) {
                connectionProviders.add(connectionProvider);
            }
        }
        logger.info("Updating database definitions");
        File appsDir = appDir.getParentFile();
        ResourceAccessor resourceAccessor =
                new FileSystemResourceAccessor(appsDir.getAbsolutePath());
        for (ConnectionProvider current : connectionProviders) {
            Database database = current.getDatabase();
            String databaseName = database.getDatabaseName();
            for(Schema schema : database.getSchemas()) {
                String schemaName = schema.getSchemaName();
                String changelogFileName =
                        MessageFormat.format(
                                changelogFileNameTemplate, databaseName + "-" + schemaName);
                File changelogFile =
                    new File(appDbsDir, changelogFileName);
                logger.info("Running changelog file: {}", changelogFile);
                Connection connection = null;
                try {
                    connection = current.acquireConnection();
                    JdbcConnection jdbcConnection = new JdbcConnection(connection);
                    liquibase.database.Database lqDatabase =
                            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
                    lqDatabase.setDefaultSchemaName(schemaName);
                    String relativeChangelogPath =
                            com.manydesigns.portofino.util.FileUtils.getRelativePath(appsDir, changelogFile);
                    if(new File(relativeChangelogPath).isAbsolute()) {
                        logger.warn("The application dbs dir {} is not inside the apps dir {}; using an absolute path for Liquibase update",
                                appDbsDir, appsDir);
                    }
                    Liquibase lq = new Liquibase(
                            relativeChangelogPath,
                            resourceAccessor,
                            lqDatabase);
                    lq.update(null);
                } catch (Exception e) {
                    logger.error("Couldn't update database: " + schemaName, e);
                } finally {
                    current.releaseConnection(connection);
                }
            }
        }
    }

    public void saveXmlModel() {
        try {
            File tempFile = File.createTempFile(appModelFile.getName(), "");

            JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(model, tempFile);

            moveFileSafely(tempFile, appModelFile.getAbsolutePath());

            logger.info("Saved xml model to file: {}", appModelFile);
        } catch (Throwable e) {
            logger.error("Cannot save xml model to file: " + appModelFile, e);
        }
    }

    private synchronized void installDataModel(Model newModel) {
        Map<String, HibernateDatabaseSetup> oldSetups = setups;
        HashMap<String, HibernateDatabaseSetup> newSetups =
                new HashMap<String, HibernateDatabaseSetup>();
        for (Database database : newModel.getDatabases()) {
            String databaseName = database.getDatabaseName();

            ConnectionProvider connectionProvider =
                    getConnectionProvider(databaseName);
            if (connectionProvider.getStatus()
                    .equals(ConnectionProvider.STATUS_CONNECTED)) {
                HibernateConfig builder =
                        new HibernateConfig(connectionProvider, portofinoConfiguration);
                String trueString = database.getTrueString();
                if(trueString != null) {
                    builder.setTrueString(
                            "null".equalsIgnoreCase(trueString) ? null : trueString);
                }
                String falseString = database.getFalseString();
                if(falseString != null) {
                    builder.setFalseString(
                            "null".equalsIgnoreCase(falseString) ? null : falseString);
                }
                Configuration configuration =
                        builder.buildSessionFactory(database);
                SessionFactoryImpl sessionFactory =
                        (SessionFactoryImpl) configuration
                                .buildSessionFactory();

                HibernateDatabaseSetup setup =
                        new HibernateDatabaseSetup(
                                configuration, sessionFactory);
                newSetups.put(databaseName, setup);
            }
        }
        setups = newSetups;
        model = newModel;

        if (oldSetups != null) {
            logger.info("Cleaning up old setups");
            for (Map.Entry<String, HibernateDatabaseSetup> current : oldSetups.entrySet()) {
                String databaseName = current.getKey();
                logger.info("Cleaning up old setup for: {}", databaseName);
                HibernateDatabaseSetup hibernateDatabaseSetup =
                        current.getValue();
                try {
                    SessionFactory sessionFactory =
                            hibernateDatabaseSetup.getSessionFactory();
                    sessionFactory.close();
                } catch (Throwable t) {
                    logger.warn("Cannot close session factory for: " + databaseName, t);
                }
            }
        }
    }

    //**************************************************************************
    // Database stuff
    //**************************************************************************

    public ConnectionProvider getConnectionProvider(String databaseName) {
        for (ConnectionProvider current : connectionProviders) {
            if (current.getDatabase().getDatabaseName().equals(databaseName)) {
                return current;
            }
        }
        return null;
    }

    public void addDatabase(Database database) {
        logger.info("Adding a new database: {}", database);
        model.getDatabases().add(database);
        connectionProviders.add(database.getConnectionProvider());
        saveXmlModel();
    }

    public void deleteDatabases(String[] databases) {
        logger.info("Deleting databases: {}", databases);
        List<Database> toBeRemoved = new ArrayList<Database>();
        for(String databaseName : databases) {
            for(Database current : model.getDatabases()){
                if(current.getDatabaseName().equals(databaseName)){
                    toBeRemoved.add(current);
                    connectionProviders.remove(current.getConnectionProvider());
                }
            }
        }
        model.getDatabases().removeAll(toBeRemoved);
        saveXmlModel();
    }

     public void deleteDatabase(String databaseName) {
        logger.info("Deleting database: {}", databaseName);
        for(Database current : model.getDatabases()){
            if(current.getDatabaseName().equals(databaseName)){
                model.getDatabases().remove(current);
                connectionProviders.remove(current.getConnectionProvider());
                saveXmlModel();
                break;
            }
        }
    }

    public void updateDatabase(Database database) {
        logger.info("Updating database: {}", database);
        for (Database db : model.getDatabases()){
            if (db.getDatabaseName().equals(database.getDatabaseName())){
                deleteDatabase(database.getDatabaseName());
                addDatabase(db); //TODO salva il modello 2 volte
                return;
            }
        }
    }

    public String getSystemDatabaseName() {
        return portofinoConfiguration.getString(PortofinoProperties.SYSTEM_DATABASE);
    }

    public Database getSystemDatabase() {
        return DataModelLogic.findDatabaseByName(model, getSystemDatabaseName());
    }

    public org.apache.commons.configuration.Configuration getPortofinoProperties() {
        return portofinoConfiguration;
    }

    public DatabasePlatformsManager getDatabasePlatformsManager() {
        return databasePlatformsManager;
    }

    //**************************************************************************
    // Model access
    //**************************************************************************

    public List<ConnectionProvider> getConnectionProviders() {
        return connectionProviders;
    }

    public Model getModel() {
        return model;
    }

    public void syncDataModel(String databaseName) throws Exception {
        ConnectionProvider connectionProvider =
                getConnectionProvider(databaseName);
        Database sourceDatabase =
                DataModelLogic.findDatabaseByName(model, databaseName);
        DatabaseSyncer dbSyncer = new DatabaseSyncer(connectionProvider);
        Database targetDatabase = dbSyncer.syncDatabase(model);
        model.getDatabases().remove(sourceDatabase);
        model.getDatabases().add(targetDatabase);
        model.init();
        installDataModel(model);
        saveXmlModel();
    }

    //**************************************************************************
    // Persistance
    //**************************************************************************

    public Session getSessionByQualifiedTableName(String qualifiedTableName) {
        Table table = DataModelLogic.findTableByQualifiedName(
                model, qualifiedTableName);
        if(table == null) {
            throw new Error("Table not found: " + qualifiedTableName);
        }
        String databaseName = table.getDatabaseName();
        return getSession(databaseName);
    }

    public Session getSession(String databaseName) {
        return ensureDatabaseSetup(databaseName).getThreadSession();
    }

    public Session getSystemSession() {
        return getSession(getSystemDatabaseName());
    }

    protected HibernateDatabaseSetup ensureDatabaseSetup(String databaseName) {
        HibernateDatabaseSetup setup = setups.get(databaseName);
        if (setup == null) {
            throw new Error("No setup exists for database: " + databaseName);
        }
        return setup;
    }

    public void closeSessions() {
        for (HibernateDatabaseSetup current : setups.values()) {
            closeSession(current);
        }
    }

    public void closeSessionByQualifiedTableName(String qualifiedTableName) {
        Table table = DataModelLogic.findTableByQualifiedName(
                model, qualifiedTableName);
        String databaseName = table.getDatabaseName();
        closeSession(databaseName);
    }

    public void closeSession(String databaseName) {
        closeSession(ensureDatabaseSetup(databaseName));
    }

    protected void closeSession(HibernateDatabaseSetup current) {
        Session session = current.getThreadSession(false);
        if (session != null) {
            try {
                Transaction transaction = session.getTransaction();
                if(transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                session.close();
            } catch (Throwable e) {
                logger.warn(ExceptionUtils.getRootCauseMessage(e), e);
            }
            current.removeThreadSession();
        }
    }

    //**************************************************************************
    // DDL
    //**************************************************************************

    public List<String> getDDLCreate() {
        List<String> result = new ArrayList<String>();
        for (Database db : model.getDatabases()) {
            result.add("-- DB: " + db.getDatabaseName());
            HibernateDatabaseSetup setup = ensureDatabaseSetup(db.getDatabaseName());
            ConnectionProvider connectionProvider =
                    getConnectionProvider(db.getDatabaseName());
            DatabasePlatform platform = connectionProvider.getDatabasePlatform();
            Dialect dialect = platform.getHibernateDialect();
            Configuration conf = setup.getConfiguration();
            String[] ddls = conf.generateSchemaCreationScript(dialect);
            result.addAll(Arrays.asList(ddls));
        }
        return result;
    }

    public List<String> getDDLUpdate() {
        List<String> result = new ArrayList<String>();


        for (Database db : model.getDatabases()) {
            HibernateDatabaseSetup setup = ensureDatabaseSetup(db.getDatabaseName());
            DatabaseMetadata databaseMetadata;
            ConnectionProvider provider =
                    getConnectionProvider(db.getDatabaseName());
            DatabasePlatform platform = provider.getDatabasePlatform();
            Dialect dialect = platform.getHibernateDialect();
            Connection conn = null;
            try {
                conn = provider.acquireConnection();

                databaseMetadata = new DatabaseMetadata(conn, dialect);

                result.add("-- DB: " + db.getDatabaseName());

                Configuration conf = setup.getConfiguration();
                String[] ddls = conf.generateSchemaUpdateScript(
                        dialect, databaseMetadata);
                result.addAll(Arrays.asList(ddls));

            } catch (Throwable e) {
                logger.warn("Cannot retrieve DDLs for update DB for DB: " +
                        db.getDatabaseName(), e);
            } finally {
                provider.releaseConnection(conn);
            }

        }
        return result;
    }

    public @NotNull TableAccessor getTableAccessor(String qualifiedTableName) {
        Table table = DataModelLogic.findTableByQualifiedName(
                model, qualifiedTableName);
        assert table != null;
        return new TableAccessor(table);
    }

    public @NotNull TableAccessor getTableAccessor(String databaseName, String entityName) {
        Database database = DataModelLogic.findDatabaseByName(model, databaseName);
        assert database != null;
        Table table = DataModelLogic.findTableByEntityName(database, entityName);
        assert table != null;
        return new TableAccessor(table);
    }

    //**************************************************************************
    // User
    //**************************************************************************

    public User findUserByEmail(String email) {
        Session session = getSystemSession();
        org.hibernate.Criteria criteria = session.createCriteria(SecurityLogic.USER_ENTITY_NAME);
        criteria.add(Restrictions.eq("email", email));
        return (User) criteria.uniqueResult();
    }

    public User findUserByUserName(String username) {
        Session session = getSystemSession();
        org.hibernate.Criteria criteria = session.createCriteria(SecurityLogic.USER_ENTITY_NAME);
        criteria.add(Restrictions.eq(SessionAttributes.USER_NAME, username));
        return (User) criteria.uniqueResult();
    }

    public User findUserByToken(String token) {
        Session session = getSystemSession();
        org.hibernate.Criteria criteria = session.createCriteria(SecurityLogic.USER_ENTITY_NAME);
        criteria.add(Restrictions.eq("token", token));
        return (User) criteria.uniqueResult();
    }

    public Group getAllGroup() {
        String name = portofinoConfiguration.getString(PortofinoProperties.GROUP_ALL);
        return getGroup(name);
    }

    public Group getAnonymousGroup() {
        String name = portofinoConfiguration.getString(PortofinoProperties.GROUP_ANONYMOUS);
        return getGroup(name);
    }

    public Group getRegisteredGroup() {
        String name = portofinoConfiguration.getString(PortofinoProperties.GROUP_REGISTERED);
        return getGroup(name);
    }

    public Group getAdministratorsGroup() {
        String name = portofinoConfiguration.getString(PortofinoProperties.GROUP_ADMINISTRATORS);
        return getGroup(name);
    }

    public void shutdown() {
        for(HibernateDatabaseSetup setup : setups.values()) {
            //TODO It is the responsibility of the application to ensure that there are no open Sessions before calling close().
            //http://ajava.org/online/hibernate3api/org/hibernate/SessionFactory.html#close%28%29
            setup.getSessionFactory().close();
        }
        for (ConnectionProvider current : getConnectionProviders()) {
            current.shutdown();
        }
    }

    protected Group getGroup(String name) {
        TableAccessor table = getTableAccessor(getSystemDatabaseName(), SecurityLogic.GROUP_ENTITY_NAME);
        assert table != null;

        String actualEntityName = table.getTable().getActualEntityName();
        Session session = getSystemSession();
        List result = QueryUtils.runHqlQuery
                (session,
                        "FROM " + actualEntityName + " WHERE name = ?",
                        new Object[]{name});
        if(result.isEmpty()) {
            throw new IllegalStateException("Group " + name + " not found");
        } else if(result.size() == 1) {
            return (Group) result.get(0);
        } else {
            throw new IllegalStateException("Multiple groups named " + name + " found");
        }
    }


    //**************************************************************************
    // File Access
    //**************************************************************************

    protected void moveFileSafely(File tempFile, String fileName) throws IOException {
        File destination = new File(fileName);
        if(!destination.exists()) {
            FileUtils.moveFile(tempFile, destination);
        } else {
            File backup = File.createTempFile(destination.getName(), ".backup", destination.getParentFile());
            backup.delete();
            FileUtils.moveFile(destination, backup);
            FileUtils.moveFile(tempFile, destination);
            backup.delete();
        }
    }

    //**************************************************************************
    // App directories and files
    //**************************************************************************


    public String getAppId() {
        return appId;
    }

    public String getName() {
        return getAppConfiguration().getString(AppProperties.APPLICATION_NAME);
    }

    public File getAppDir() {
        return appDir;
    }

    public File getAppBlobsDir() {
        return appBlobsDir;
    }

    public File getAppConnectionsFile() {
        return appConnectionsFile;
    }

    public File getPagesDir() {
        //TODO!!
        return new File(getAppDir(), "pages");
    }

    public File getAppDbsDir() {
        return appDbsDir;
    }

    public File getAppModelFile() {
        return appModelFile;
    }

    public File getAppScriptsDir() {
        return appScriptsDir;
    }

    public File getAppTextDir() {
        return appTextDir;
    }

    public File getAppStorageDir() {
        return appStorageDir;
    }

    public File getAppWebDir() {
        return appWebDir;
    }

    public org.apache.commons.configuration.Configuration getAppConfiguration() {
        return appConfiguration;
    }

    //**************************************************************************
    // I18n
    //**************************************************************************

    public ResourceBundle getBundle(Locale locale) {
        return resourceBundleManager.getBundle(locale);
    }
}
