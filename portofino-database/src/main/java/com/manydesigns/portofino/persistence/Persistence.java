/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.persistence;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.cache.CacheResetEvent;
import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import com.manydesigns.portofino.liquibase.VFSResourceAccessor;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.hibernate.HibernateDatabaseSetup;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryAndCodeBase;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryBuilder;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.reflection.ViewAccessor;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Persistence {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String APP_MODEL_DIRECTORY = "portofino-model";
    @Deprecated
    public static final String APP_MODEL_FILE = APP_MODEL_DIRECTORY + ".xml";
    public static final String LIQUIBASE_CONTEXT = "liquibase.context";
    public final static String changelogFileNameTemplate = "liquibase.changelog.xml";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final DatabasePlatformsRegistry databasePlatformsRegistry;
    protected Model model;
    protected final Map<String, HibernateDatabaseSetup> setups;

    protected final FileObject applicationDirectory;
    protected final Configuration configuration;
    protected final FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile;
    public final BehaviorSubject<Status> status = BehaviorSubject.create();
    public final PublishSubject<DatabaseSetupEvent> databaseSetupEvents = PublishSubject.create();

    public enum Status {
        STARTING, STARTED, STOPPING, STOPPED
    }

    public CacheResetListenerRegistry cacheResetListenerRegistry;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(Persistence.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Persistence(FileObject applicationDirectory, Configuration configuration, FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile, DatabasePlatformsRegistry databasePlatformsRegistry) throws FileSystemException {
        this.applicationDirectory = applicationDirectory;
        this.configuration = configuration;
        this.configurationFile = configurationFile;
        this.databasePlatformsRegistry = databasePlatformsRegistry;

        if(getModelFile().exists()) {
            logger.info("Legacy application model file: {}", getModelFile().getName().getPath());
        } else {
            logger.info("Application model directory: {}", getModelDirectory().getName().getPath());
        }

        setups = new HashMap<>();
    }

    //**************************************************************************
    // Model loading
    //**************************************************************************

    public synchronized void loadXmlModel() {
        try {
            JAXBContext jc = createModelJAXBContext();
            Unmarshaller um = jc.createUnmarshaller();
            FileObject appModelFile = getModelFile();
            if(appModelFile.exists()) {
                logger.info("Loading legacy xml model from file: {}", appModelFile.getName().getPath());
                try (InputStream inputStream = appModelFile.getContent().getInputStream()) {
                    model = (Model) um.unmarshal(inputStream);
                } catch (Exception e) {
                    String msg = "Cannot load/parse model: " + appModelFile;
                    logger.error(msg, e);
                }
            } else {
                logger.info("Loading model from directory: {}", getModelDirectory().getName().getPath());
                model = new Model();
            }
            FileObject modelDir = getModelDirectory();
            if(modelDir.exists()) {
                for (FileObject databaseDir : modelDir.getChildren()) {
                    loadXmlDatabase(um, model, databaseDir);
                }
            }
            initModel();
        } catch (Exception e) {
            logger.error("Cannot load/parse model", e);
        }
    }

    public JAXBContext createModelJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(Model.class, View.class);
    }

    protected void loadXmlDatabase(Unmarshaller um, Model model, FileObject databaseDir) throws IOException, JAXBException {
        if(!databaseDir.getType().equals(FileType.FOLDER)) {
            logger.error("Not a directory: " + databaseDir.getName().getPath());
            return;
        }
        String databaseName = databaseDir.getName().getBaseName();
        FileObject databaseFile = databaseDir.resolveFile("database.xml");

        Database database;
        if(databaseFile.exists()) {
            logger.info("Loading database connection from " + databaseFile.getName().getPath());
            try(InputStream inputStream = databaseFile.getContent().getInputStream()) {
                database = (Database) um.unmarshal(inputStream);
                database.afterUnmarshal(um, model);
                if(!databaseName.equals(database.getDatabaseName())) {
                    logger.error("Database named {} defined in directory named {}, skipping", database.getDatabaseName(), databaseName);
                    return;
                }
                model.getDatabases().removeIf(d -> databaseName.equals(d.getDatabaseName()));
                model.getDatabases().add(database);
            }

            FileObject settingsFile = databaseDir.resolveFile("hibernate.properties");
            if(settingsFile.exists()) {
                try(InputStream inputStream = settingsFile.getContent().getInputStream()) {
                    Properties settings = new Properties();
                    settings.load(inputStream);
                    database.setSettings(settings);
                }
            }
        } else {
            database = DatabaseLogic.findDatabaseByName(model, databaseName);
            if(database != null) {
                logger.info("Using legacy database defined in portofino-model.xml: " + databaseName + "; it will be automatically migrated to database.xml upon save.");
            } else {
                logger.warn("No database defined in " + databaseDir.getName().getPath());
                return;
            }
        }

        for(Schema schema : database.getSchemas()) {
            FileObject schemaDir = databaseDir.resolveFile(schema.getSchemaName());
            if(schemaDir.getType() == FileType.FOLDER) {
                logger.debug("Schema directory {} exists", schemaDir);
                FileObject[] tableFiles = schemaDir.getChildren();
                for(FileObject tableFile : tableFiles) {
                    if(!tableFile.getName().getBaseName().endsWith(".table.xml")) {
                        continue;
                    }
                    try(InputStream tableInputStream = tableFile.getContent().getInputStream()) {
                        Table table = (Table) um.unmarshal(tableInputStream);
                        if (!tableFile.getName().getBaseName().equalsIgnoreCase(table.getTableName() + ".table.xml")) {
                            logger.error("Skipping table " + table.getTableName() + " defined in file " + tableFile);
                            continue;
                        }
                        table.afterUnmarshal(um, schema);
                        schema.getTables().add(table);
                    }
                }
            } else {
                logger.debug("Schema directory {} does not exist", schemaDir);
            }
        }
    }

    @Deprecated
    public FileObject getModelFile() throws FileSystemException {
        return applicationDirectory.resolveFile(APP_MODEL_FILE);
    }

    public FileObject getModelDirectory() throws FileSystemException {
        return applicationDirectory.resolveFile(APP_MODEL_DIRECTORY);
    }

    public void runLiquibase(Database database) {
        logger.info("Updating database definitions");
        ResourceAccessor resourceAccessor = new VFSResourceAccessor(applicationDirectory);
        ConnectionProvider connectionProvider = database.getConnectionProvider();
        for(Schema schema : database.getSchemas()) {
            String schemaName = schema.getSchemaName();
            try(Connection connection = connectionProvider.acquireConnection()) {
                FileObject changelogFile = getLiquibaseChangelogFile(schema);
                if(changelogFile.getType() != FileType.FILE) {
                    logger.info("Changelog file does not exist or is not a normal file, skipping: {}", changelogFile);
                    continue;
                }
                logger.info("Running changelog file: {}", changelogFile);
                JdbcConnection jdbcConnection = new JdbcConnection(connection);
                liquibase.database.Database lqDatabase =
                        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
                lqDatabase.setDefaultSchemaName(schema.getActualSchemaName());
                String relativeChangelogPath = applicationDirectory.getName().getRelativeName(changelogFile.getName());
                Liquibase lq = new Liquibase(relativeChangelogPath, resourceAccessor, lqDatabase);

                String[] contexts = configuration.getStringArray(LIQUIBASE_CONTEXT);
                logger.info("Using context {}", Arrays.toString(contexts));
                lq.update(new Contexts(contexts));
            } catch (Exception e) {
                logger.error("Couldn't update database: " + schemaName, e);
            }
        }
    }

    public synchronized void saveXmlModel() throws IOException, JAXBException, ConfigurationException {
        //TODO gestire conflitti con modifiche esterne?
        JAXBContext jc = createModelJAXBContext();
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        FileObject modelDir = getModelDirectory();
        modelDir.createFolder();
        for(Database database : model.getDatabases()) {
            FileObject databaseDir = modelDir.resolveFile(database.getDatabaseName());
            FileObject databaseFile = databaseDir.resolveFile("database.xml");
            databaseFile.createFile();
            try(OutputStream outputStream = databaseFile.getContent().getOutputStream()) {
                m.marshal(database, outputStream);
            }
            for(Schema schema : database.getSchemas()) {
                FileObject schemaDir = databaseDir.resolveFile(schema.getSchemaName());
                if(!schemaDir.exists()) {
                    logger.debug("Schema directory {} does not exist", schemaDir);
                    schemaDir.createFolder();
                }
                FileObject[] tableFiles = schemaDir.getChildren();
                for(FileObject tableFile : tableFiles) {
                    if(tableFile.getName().getBaseName().endsWith(".table.xml")) {
                        if (!tableFile.delete()) {
                            logger.warn("Could not delete table file {}", tableFile.getName().getPath());
                        }
                    }
                }
                for(Table table : schema.getTables()) {
                    FileObject tableFile = schemaDir.resolveFile(table.getTableName() + ".table.xml");
                    try(OutputStream outputStream = tableFile.getContent().getOutputStream()) {
                        m.marshal(table, outputStream);
                    }
                }
            }
            deleteUnusedSchemaDirectories(database, databaseDir);
        }
        deleteUnusedDatabaseDirectories();
        logger.info("Saved xml model to directory: {}", modelDir.getName().getPath());
        if(configurationFile != null) {
            configurationFile.save();
            logger.info("Saved configuration file {}", configurationFile.getFileHandler().getFile().getAbsolutePath());
        }

        FileObject appModelFile = getModelFile();
        if(appModelFile.exists()) {
            appModelFile.delete();
            logger.info("Deleted legacy portofino-model.xml file: {}", appModelFile.getName().getPath());
        }
    }

    /**
     * Delete the directories of the databases that are no longer present in the model
     *
     * @throws FileSystemException if the schema directories cannot be listed.
     */
    protected void deleteUnusedDatabaseDirectories() throws FileSystemException {
        Arrays.stream(getModelDirectory().getChildren()).forEach(dbDir -> {
            String dbDirPath = dbDir.getName().getPath();
            try {
                if(dbDir.getType() == FileType.FOLDER) {
                    String dirName = dbDir.getName().getBaseName();
                    if (model.getDatabases().stream().noneMatch(db -> db.getDatabaseName().equals(dirName))) {
                        logger.info("Deleting unused database directory {}", dbDirPath);
                        try {
                            dbDir.deleteAll();
                        } catch (FileSystemException e) {
                            logger.warn("Could not delete unused database dir " + dbDirPath, e);
                        }
                    }
                }
            } catch (FileSystemException e) {
                logger.error("Unexpected filesystem error when trying to delete schema directory " + dbDirPath, e);
            }
        });
    }

    /**
     * Delete the directories of the schemas that are no longer present in the model
     *
     * @param database the parent database containing the schemas
     * @param databaseDir the database directory
     * @throws FileSystemException if the schema directories cannot be listed.
     */
    protected void deleteUnusedSchemaDirectories(Database database, FileObject databaseDir) throws FileSystemException {
        Arrays.stream(databaseDir.getChildren()).forEach(schemaDir -> {
            String schemaDirPath = schemaDir.getName().getPath();
            try {
                if(schemaDir.getType() == FileType.FOLDER) {
                    String dirName = schemaDir.getName().getBaseName();
                    if (database.getSchemas().stream().noneMatch(schema -> schema.getSchemaName().equals(dirName))) {
                        logger.info("Deleting unused schema directory {}", schemaDirPath);
                        try {
                            schemaDir.deleteAll();
                        } catch (FileSystemException e) {
                            logger.warn("Could not delete unused schema dir " + schemaDirPath, e);
                        }
                    }
                }
            } catch (FileSystemException e) {
                logger.error("Unexpected filesystem error when trying to delete schema directory " + schemaDirPath, e);
            }
        });
    }

    public synchronized void initModel() {
        logger.info("Cleaning up old setups");
        closeSessions();
        for (Map.Entry<String, HibernateDatabaseSetup> current : setups.entrySet()) {
            String databaseName = current.getKey();
            logger.debug("Cleaning up old setup for: {}", databaseName);
            HibernateDatabaseSetup setup = current.getValue();
            try {
                setup.dispose();
            } catch (Throwable t) {
                logger.warn("Cannot close session factory for: " + databaseName, t);
            }
            databaseSetupEvents.onNext(new DatabaseSetupEvent(DatabaseSetupEvent.REMOVED, setup));
        }
        //TODO it would perhaps be preferable if we generated REPLACED events here rather than REMOVED followed by ADDED
        setups.clear();
        model.init(configuration);
        for (Database database : model.getDatabases()) {
            initConnectionProvider(database);
        }
        if(cacheResetListenerRegistry != null) {
            cacheResetListenerRegistry.fireReset(new CacheResetEvent(this));
        }
    }

    protected void initConnectionProvider(Database database) {
        logger.info("Initializing connection provider for database " + database.getDatabaseName());
        try {
            ConnectionProvider connectionProvider = database.getConnectionProvider();
            connectionProvider.init(databasePlatformsRegistry);
            if (connectionProvider.getStatus().equals(ConnectionProvider.STATUS_CONNECTED)) {
                SessionFactoryBuilder builder = new SessionFactoryBuilder(database);
                SessionFactoryAndCodeBase sessionFactoryAndCodeBase = builder.buildSessionFactory();
                HibernateDatabaseSetup setup =
                        new HibernateDatabaseSetup(
                                database, sessionFactoryAndCodeBase.sessionFactory,
                                sessionFactoryAndCodeBase.codeBase, builder.getEntityMode());
                String databaseName = database.getDatabaseName();
                HibernateDatabaseSetup oldSetup = setups.get(databaseName);
                setups.put(databaseName, setup);
                if(oldSetup != null) {
                    oldSetup.dispose();
                    databaseSetupEvents.onNext(new DatabaseSetupEvent(oldSetup, setup));
                } else {
                    databaseSetupEvents.onNext(new DatabaseSetupEvent(DatabaseSetupEvent.ADDED, setup));
                }
            }
        } catch (Exception e) {
            logger.error("Could not create connection provider for " + database, e);
        }
    }

    public void retryFailedConnections() {
        Status currentStatus = status.getValue();
        if(currentStatus != Status.STARTED) {
            throw new IllegalStateException("Persistence not started: " + currentStatus);
        }
        for (Database database : model.getDatabases()) {
            if (!ConnectionProvider.STATUS_CONNECTED.equals(database.getConnectionProvider().getStatus())) {
                logger.info("Retrying failed connection to database " + database.getDatabaseName());
                initConnectionProvider(database);
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

    public Configuration getConfiguration() {
        return configuration;
    }

    public DatabasePlatformsRegistry getDatabasePlatformsRegistry() {
        return databasePlatformsRegistry;
    }

    //**************************************************************************
    // Model access
    //**************************************************************************

    public Model getModel() {
        return model;
    }

    public synchronized void syncDataModel(String databaseName) throws Exception {
        Database sourceDatabase = DatabaseLogic.findDatabaseByName(model, databaseName);
        if(sourceDatabase == null) {
            throw new IllegalArgumentException("Database " + databaseName + " does not exist");
        }
        if(configuration.getBoolean(DatabaseModule.LIQUIBASE_ENABLED, true)) {
            runLiquibase(sourceDatabase);
        } else {
            logger.debug("syncDataModel called, but Liquibase is not enabled");
        }
        ConnectionProvider connectionProvider = sourceDatabase.getConnectionProvider();
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
        HibernateDatabaseSetup setup = getDatabaseSetup(databaseName);
        if (setup == null) {
            throw new Error("No setup exists for database: " + databaseName);
        }
        return setup;
    }

    public HibernateDatabaseSetup getDatabaseSetup(String databaseName) {
        return setups.get(databaseName);
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

    public @NotNull TableAccessor getTableAccessor(String databaseName, String entityName) {
        Database database = DatabaseLogic.findDatabaseByName(model, databaseName);
        if(database == null) {
            throw new IllegalArgumentException("Database " + databaseName + " does not exist");
        }
        Table table = DatabaseLogic.findTableByEntityName(database, entityName);
        if(table == null) {
            throw new IllegalArgumentException("Table " + entityName + " not found in database " + databaseName);
        }
        return getTableAccessor(table);
    }

    @NotNull
    public TableAccessor getTableAccessor(Table table) {
        return table instanceof View ? new ViewAccessor((View) table) : new TableAccessor(table);
    }

    //**************************************************************************
    // User
    //**************************************************************************

    public void start() {
        status.onNext(Status.STARTING);
        loadXmlModel();
        for(Database database : model.getDatabases()) {
            if(ConnectionProvider.STATUS_CONNECTED.equals(database.getConnectionProvider().getStatus())) {
                runLiquibase(database);
            }
        }
        status.onNext(Status.STARTED);
    }

    public void stop() {
        //TODO complete subscriptions?
        status.onNext(Status.STOPPING);
        closeSessions();
        for(HibernateDatabaseSetup setup : setups.values()) {
            setup.dispose();
        }
        for (Database database : model.getDatabases()) {
            ConnectionProvider connectionProvider =
                    database.getConnectionProvider();
            connectionProvider.shutdown();
        }
        status.onNext(Status.STOPPED);
    }

    //**************************************************************************
    // App directories and files
    //**************************************************************************

    public String getName() {
        return getConfiguration().getString(PortofinoProperties.APP_NAME);
    }

    public FileObject getLiquibaseChangelogFile(Schema schema) throws FileSystemException {
        if(schema == null) {
            return null;
        }
        FileObject dbDir = getModelDirectory().resolveFile(schema.getDatabaseName());
        FileObject schemaDir = dbDir.resolveFile(schema.getSchemaName());
        return schemaDir.resolveFile(changelogFileNameTemplate);
    }

    @Deprecated
    public FileObject getAppModelFile() {
        try {
            return getModelFile();
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    public FileObject getApplicationDirectory() {
        return applicationDirectory;
    }

    public static class DatabaseSetupEvent {
        public static final int ADDED = +1, REMOVED = -1, REPLACED = 0;

        public DatabaseSetupEvent(int type, HibernateDatabaseSetup setup) {
            if(type == 0) {
                throw new IllegalArgumentException();
            }
            this.type = type;
            this.setup = setup;
        }

        public DatabaseSetupEvent(HibernateDatabaseSetup setup, HibernateDatabaseSetup oldSetup) {
            this.setup = setup;
            this.oldSetup = oldSetup;
            type = REPLACED;
        }

        public int type;
        public HibernateDatabaseSetup setup;
        public HibernateDatabaseSetup oldSetup;
    }

}
