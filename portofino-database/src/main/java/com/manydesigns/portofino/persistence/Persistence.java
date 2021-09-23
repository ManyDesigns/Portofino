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
import com.manydesigns.portofino.database.multitenancy.MultiTenant;
import com.manydesigns.portofino.liquibase.VFSResourceAccessor;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.database.annotations.Id;
import com.manydesigns.portofino.model.database.annotations.JDBCConnection;
import com.manydesigns.portofino.model.database.annotations.JNDIConnection;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.model.io.dsl.DefaultModelIO;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.hibernate.HibernateDatabaseSetup;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryAndCodeBase;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryBuilder;
import com.manydesigns.portofino.model.io.ModelIO;
import com.manydesigns.portofino.model.io.xml.XMLModel;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementation;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementationFactory;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.eclipse.emf.ecore.*;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.*;

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
    @Autowired
    protected MultiTenancyImplementationFactory multiTenancyImplementationFactory = MultiTenancyImplementationFactory.DEFAULT;
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

    public Persistence(
            FileObject applicationDirectory, Configuration configuration,
            FileBasedConfigurationBuilder<PropertiesConfiguration> configurationFile,
            DatabasePlatformsRegistry databasePlatformsRegistry) throws FileSystemException {
        this.applicationDirectory = applicationDirectory;
        this.configuration = configuration;
        this.configurationFile = configurationFile;
        this.databasePlatformsRegistry = databasePlatformsRegistry;
        setups = new HashMap<>();
    }

    //**************************************************************************
    // Model loading
    //**************************************************************************

    public synchronized Model loadModel(ModelIO modelIO) throws IOException {
        Model loaded = modelIO.load();
        if(loaded != null) {
            model = loaded;
        }
        return model;
    }

    public synchronized void loadModel() {
        if(!loadLegacyModel()) {
            try {
                loadModel(new DefaultModelIO(getModelDirectory()));
                model.getDomains().forEach(domain -> {
                    Database database = setupDatabase(model, domain);
                    if(database != null) {
                        domain.getESubpackages().forEach(subd -> setupSchema(database, subd));
                    }
                });
                initModel();
            } catch (Exception e) {
                logger.error("Cannot load/parse model", e);
            }
        }
    }

    protected boolean loadLegacyModel() {
        try {
            Model model = loadModel(new XMLModel(getModelDirectory()));
            if(model != null) {
                logger.info("Loaded legacy XML model. It will be converted to the new format upon save.");
                initModel();
                return true;
            }
        } catch (Exception e) {
            logger.error("Cannot load/parse XML model", e);
        }
        return false;
    }

    protected void annotateDatabases(Collection<Database> databases) {
        databases.forEach(db -> {
            ConnectionProvider connectionProvider = db.getConnectionProvider();
            if(connectionProvider instanceof JdbcConnectionProvider) {
                db.removeAnnotation(JNDIConnection.class);
                Annotation annotation = db.ensureAnnotation(JDBCConnection.class);
                annotation.setPropertyValue("url", ((JdbcConnectionProvider) connectionProvider).getUrl());
                annotation.setPropertyValue("driver", ((JdbcConnectionProvider) connectionProvider).getDriver());
                annotation.setPropertyValue("username", ((JdbcConnectionProvider) connectionProvider).getUsername());
                annotation.setPropertyValue("password", ((JdbcConnectionProvider) connectionProvider).getPassword());
            } else {
                db.removeAnnotation(JDBCConnection.class);
                Annotation annotation = db.ensureAnnotation(JNDIConnection.class);
                annotation.setPropertyValue("name", ((JndiConnectionProvider) connectionProvider).getJndiResource());
            }

            db.getAllTables().forEach(table -> {
                if(!table.getTableName().equals(table.getActualEntityName())) {
                    Annotation tableAnn = table.ensureAnnotation(javax.persistence.Table.class);
                    tableAnn.setPropertyValue("name", table.getTableName());
                }
                table.getColumns().forEach(column -> {
                    if(!column.getColumnName().equals(column.getActualPropertyName())) {
                        Annotation colAnn = column.ensureAnnotation(javax.persistence.Column.class);
                        colAnn.setPropertyValue("name", column.getColumnName());
                    }
                });
            });
        });
    }

    protected Database setupDatabase(Model model, EPackage domain) {
        //Can't use getJavaAnnotation as they've not yet been resolved
        EAnnotation ann = domain.getEAnnotation(JDBCConnection.class.getName());
        if(ann != null) {
            Database database = new Database(domain);
            model.getDatabases().add(database);
            JdbcConnectionProvider cp = new JdbcConnectionProvider();
            cp.setDatabase(database);
            cp.setUrl(ann.getDetails().get("url"));
            cp.setDriver(ann.getDetails().get("driver"));
            cp.setUsername(ann.getDetails().get("username"));
            cp.setPassword(ann.getDetails().get("password"));
            database.setConnectionProvider(cp);
            return database;
        } else {
            ann = domain.getEAnnotation(JNDIConnection.class.getName());
            if(ann != null) {
                Database database = new Database(domain);
                model.getDatabases().add(database);
                JndiConnectionProvider cp = new JndiConnectionProvider();
                cp.setDatabase(database);
                cp.setJndiResource(ann.getDetails().get("name"));
                database.setConnectionProvider(cp);
                return database;
            }
        }
        return null;
    }

    protected Schema setupSchema(Database database, EPackage domain) {
        Schema schema = new Schema(domain);
        schema.setDatabase(database);
        database.getSchemas().add(schema);
        EAnnotation schemaAnn =
                domain.getEAnnotation(com.manydesigns.portofino.model.database.annotations.Schema.class.getName());
        if(schemaAnn != null) {
            schema.setActualSchemaName(schemaAnn.getDetails().get("name"));
        }
        domain.getEClassifiers().forEach(entity -> {
            if(entity instanceof EClass) {
                setupTable(schema, (EClass) entity);
            }
        });
        return schema;
    }

    protected void setupTable(Schema schema, EClass entity) {
        Table table = new Table(entity);
        table.setSchema(schema);
        schema.getTables().add(table);
        PrimaryKey pk = new PrimaryKey(table);
        EAnnotation tableAnn = entity.getEAnnotation(javax.persistence.Table.class.getName());
        if(tableAnn != null) {
            table.setTableName(tableAnn.getDetails().get("name"));
        }
        if(StringUtils.isBlank(table.getTableName())) {
            table.setTableName(entity.getName());
        }
        entity.getEAttributes().forEach(property -> setupColumn(table, pk, property));
        if(!pk.getColumns().isEmpty()) {
            pk.getColumns().sort(Comparator.comparingInt(c ->
                    Integer.parseInt(c.getAnnotation(Id.class).get().getPropertyValue("order"))));
            table.setPrimaryKey(pk);
        }
    }

    protected void setupColumn(Table table, PrimaryKey pk, EAttribute property) {
        Column column = new Column(property);
        column.setTable(table);
        table.getColumns().add(column);
        EAnnotation colAnn = property.getEAnnotation(javax.persistence.Column.class.getName());
        if(colAnn != null) {
            column.setColumnName(colAnn.getDetails().get("name"));
        }
        if(StringUtils.isBlank(column.getColumnName())) {
            column.setColumnName(property.getName());
        }
        //TODO column type
        if(property.getEAnnotation(Id.class.getName()) != null) {
            pk.add(column);
        }
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

    public synchronized void saveModel() throws IOException, ConfigurationException {
        new XMLModel(getModelDirectory()).delete();
        new DefaultModelIO(getModelDirectory()).save(model);
        if (configurationFile != null) {
            configurationFile.save();
            logger.info("Saved configuration file {}", configurationFile.getFileHandler().getFile().getAbsolutePath());
        }
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
        annotateDatabases(model.getDatabases());
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
                MultiTenancyImplementation implementation = getMultiTenancyImplementation(database);
                SessionFactoryBuilder builder = new SessionFactoryBuilder(database, configuration, implementation);
                SessionFactoryAndCodeBase sessionFactoryAndCodeBase = builder.buildSessionFactory();
                HibernateDatabaseSetup setup =
                        new HibernateDatabaseSetup(
                                database, sessionFactoryAndCodeBase.sessionFactory,
                                sessionFactoryAndCodeBase.codeBase, builder.getEntityMode(), configuration,
                                implementation);
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

    protected MultiTenancyImplementation getMultiTenancyImplementation(Database database) {
        Optional<MultiTenant> multiTenant = database.getJavaAnnotation(MultiTenant.class);
        if(multiTenant.isPresent()) {
            Class<? extends MultiTenancyImplementation> implClass = multiTenant.get().strategy();
            //TODO injection?
            if(!MultiTenancyImplementation.class.isAssignableFrom(implClass)) {
                throw new ClassCastException(implClass + " does not extend " + MultiTenancyImplementation.class);
            }
            try {
                MultiTenancyImplementation implementation = multiTenancyImplementationFactory.make(implClass);
                MultiTenancyStrategy strategy = implementation.getStrategy();
                if (strategy.requiresMultiTenantConnectionProvider()) {
                    return implementation;
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not instantiate multi tenancy implementation " + implClass + " for " + database, e);
            }
        }
        return null;
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
        model.getDomains().remove(sourceDatabase.getModelElement());
        model.getDatabases().add(targetDatabase);
        model.getDomains().add(targetDatabase.getModelElement());
    }

    //**************************************************************************
    // Persistence
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
        loadModel();
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
        databaseSetupEvents.onComplete();
        status.onComplete();
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

    public MultiTenancyImplementationFactory getMultiTenancyImplementationFactory() {
        return multiTenancyImplementationFactory;
    }

    public void setMultiTenancyImplementationFactory(MultiTenancyImplementationFactory multiTenancyImplementationFactory) {
        this.multiTenancyImplementationFactory = multiTenancyImplementationFactory;
    }
}
