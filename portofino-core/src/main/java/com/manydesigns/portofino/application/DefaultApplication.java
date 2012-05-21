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

import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.application.hibernate.HibernateConfig;
import com.manydesigns.portofino.application.hibernate.HibernateDatabaseSetup;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.i18n.ResourceBundleManager;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String APP_BLOBS_DIR = "blobs";
    public static final String APP_DBS_DIR = "dbs";
    public static final String APP_MODEL_FILE = "portofino-model.xml";
    public static final String APP_SCRIPTS_DIR = "groovy";
    public static final String APP_PAGES_DIR = "pages";
    public static final String APP_STORAGE_DIR = "storage";
    public static final String APP_WEB_DIR = "web";

    public final static String changelogFileNameTemplate = "{0}-changelog.xml";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final org.apache.commons.configuration.Configuration portofinoConfiguration;
    protected final org.apache.commons.configuration.Configuration appConfiguration;
    protected final DatabasePlatformsManager databasePlatformsManager;
    protected Model model;
    protected final Map<String, HibernateDatabaseSetup> setups;

    protected final String appId;

    protected final File appDir;
    protected final File appBlobsDir;
    protected final File appDbsDir;
    protected final File appModelFile;
    protected final File appScriptsDir;
    protected final File appPagesDir;
    protected final File appStorageDir;
    protected final File appWebDir;

    protected final ResourceBundleManager resourceBundleManager;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(DefaultApplication.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public DefaultApplication(String appId,
                              org.apache.commons.configuration.Configuration portofinoConfiguration,
                              DatabasePlatformsManager databasePlatformsManager,
                              File appDir
    ) throws Exception {
        this.appId = appId;
        this.portofinoConfiguration = portofinoConfiguration;
        this.databasePlatformsManager = databasePlatformsManager;
        this.appDir = appDir;

        resourceBundleManager = new ResourceBundleManager(appDir);
        File appConfigurationFile =
                new File(appDir, AppProperties.PROPERTIES_RESOURCE);
        appConfiguration = new PropertiesConfiguration(appConfigurationFile);

        appBlobsDir = new File(appDir, APP_BLOBS_DIR);
        logger.info("Application blobs dir: {}", appBlobsDir.getAbsolutePath());
        boolean result = ElementsFileUtils.ensureDirectoryExistsAndWritable(appBlobsDir);

        appDbsDir = new File(appDir, APP_DBS_DIR);
        logger.info("Application dbs dir: {}",
                appDbsDir.getAbsolutePath());
        result &= ElementsFileUtils.ensureDirectoryExistsAndWritable(appDbsDir);

        appModelFile = new File(appDir, APP_MODEL_FILE);
        logger.info("Application model file: {}",
                appModelFile.getAbsolutePath());

        appScriptsDir = new File(appDir, APP_SCRIPTS_DIR);
        logger.info("Application scripts dir: {}",
                appScriptsDir.getAbsolutePath());
        result &= ElementsFileUtils.ensureDirectoryExistsAndWritable(appScriptsDir);

        appPagesDir = new File(appDir, APP_PAGES_DIR);
        logger.info("Application pages dir: {}",
                appPagesDir.getAbsolutePath());
        result &= ElementsFileUtils.ensureDirectoryExistsAndWritable(appPagesDir);

        appStorageDir = new File(appDir, APP_STORAGE_DIR);
        logger.info("Application storage dir: {}",
                appStorageDir.getAbsolutePath());
        result &= ElementsFileUtils.ensureDirectoryExistsAndWritable(appStorageDir);

        appWebDir = new File(appDir, APP_WEB_DIR);
        logger.info("Application web dir: {}",
                appWebDir.getAbsolutePath());
        result &= ElementsFileUtils.ensureDirectoryExistsAndWritable(appWebDir);

        if (!result) {
            throw new Exception("Could not initialize application");
        }

        setups = new HashMap<String, HibernateDatabaseSetup>();
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
            model = (Model) um.unmarshal(appModelFile);
            boolean syncOnStart = false;
            initModel();
            runLiquibaseScripts();
            if (syncOnStart) {
                List<String> databaseNames = new ArrayList<String>();
                for (Database sourceDatabase : model.getDatabases()) {
                    String databaseName = sourceDatabase.getDatabaseName();
                    databaseNames.add(databaseName);
                }
                for (String databaseName : databaseNames) {
                    syncDataModel(databaseName);
                }
                initModel();
            }
        } catch (Exception e) {
            String msg = "Cannot load/parse model: " + appModelFile;
            logger.error(msg, e);
        }
    }

    protected void runLiquibaseScripts() {
        logger.info("Updating database definitions");
        ResourceAccessor resourceAccessor =
                new FileSystemResourceAccessor(appDir.getAbsolutePath());
        for (Database database : model.getDatabases()) {
            ConnectionProvider connectionProvider =
                    database.getConnectionProvider();
            String databaseName = database.getDatabaseName();
            for(Schema schema : database.getSchemas()) {
                String schemaName = schema.getSchemaName();
                String changelogFileName =
                        MessageFormat.format(
                                changelogFileNameTemplate, databaseName + "-" + schemaName);
                File changelogFile = new File(appDbsDir, changelogFileName);
                logger.info("Running changelog file: {}", changelogFile);
                Connection connection = null;
                try {
                    connection = connectionProvider.acquireConnection();
                    JdbcConnection jdbcConnection = new JdbcConnection(connection);
                    liquibase.database.Database lqDatabase =
                            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
                    lqDatabase.setDefaultSchemaName(schemaName);
                    String relativeChangelogPath =
                            ElementsFileUtils.getRelativePath(appDir, changelogFile);
                    if(new File(relativeChangelogPath).isAbsolute()) {
                        logger.warn("The application dbs dir {} is not inside the apps dir {}; using an absolute path for Liquibase update",
                                appDbsDir, appDir);
                    }
                    Liquibase lq = new Liquibase(
                            relativeChangelogPath,
                            resourceAccessor,
                            lqDatabase);
                    lq.update(null);
                } catch (Exception e) {
                    String msg = "Couldn't update database: " + schemaName;
                    logger.error(msg, e);
                } finally {
                    connectionProvider.releaseConnection(connection);
                }
            }
        }
    }

    public void saveXmlModel() throws IOException, JAXBException {
        File tempFile = File.createTempFile(appModelFile.getName(), "");

        JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(model, tempFile);

        moveFileSafely(tempFile, appModelFile.getAbsolutePath());

        logger.info("Saved xml model to file: {}", appModelFile);
    }

    public synchronized void initModel() {
        if (setups != null) {
            logger.info("Cleaning up old setups");
            for (Map.Entry<String, HibernateDatabaseSetup> current : setups.entrySet()) {
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

        setups.clear();
        model.init();
        for (Database database : model.getDatabases()) {
            ConnectionProvider connectionProvider =
                    database.getConnectionProvider();
            connectionProvider.init(databasePlatformsManager, appDir);
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
                String databaseName = database.getDatabaseName();
                setups.put(databaseName, setup);
            }
        }
    }

    //**************************************************************************
    // Database stuff
    //**************************************************************************

    public ConnectionProvider getConnectionProvider(String databaseName) {
        for (Database database : model.getDatabases()) {
            if (database.getDatabaseName().equals(databaseName)) {
                return database.getConnectionProvider();
            }
        }
        return null;
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

    public Model getModel() {
        return model;
    }

    public void syncDataModel(String databaseName) throws Exception {
        Database sourceDatabase =
            DatabaseLogic.findDatabaseByName(model, databaseName);
        ConnectionProvider connectionProvider =
                sourceDatabase.getConnectionProvider();
        DatabaseSyncer dbSyncer = new DatabaseSyncer(connectionProvider);
        Database targetDatabase = dbSyncer.syncDatabase(model);
        model.getDatabases().remove(sourceDatabase);
        model.getDatabases().add(targetDatabase);
    }

    //**************************************************************************
    // Persistance
    //**************************************************************************

    public Session getSession(String databaseName) {
        return ensureDatabaseSetup(databaseName).getThreadSession();
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
                logger.warn("Couldn't close session: " + ExceptionUtils.getRootCauseMessage(e), e);
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

    public @NotNull TableAccessor getTableAccessor(String databaseName, String entityName) {
        Database database = DatabaseLogic.findDatabaseByName(model, databaseName);
        assert database != null;
        Table table = DatabaseLogic.findTableByEntityName(database, entityName);
        assert table != null;
        return new TableAccessor(table);
    }

    //**************************************************************************
    // User
    //**************************************************************************

    public void shutdown() {
        for(HibernateDatabaseSetup setup : setups.values()) {
            //TODO It is the responsibility of the application to ensure that there are no open Sessions before calling close().
            //http://ajava.org/online/hibernate3api/org/hibernate/SessionFactory.html#close%28%29
            setup.getSessionFactory().close();
        }
        for (Database database : model.getDatabases()) {
            ConnectionProvider connectionProvider =
                    database.getConnectionProvider();
            connectionProvider.shutdown();
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

    public File getPagesDir() {
        return appPagesDir;
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

    public ResourceBundleManager getResourceBundleManager() {
        return resourceBundleManager;
    }
}
