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
import com.manydesigns.portofino.database.model.ForeignKey;
import com.manydesigns.portofino.database.model.PrimaryKey;
import com.manydesigns.portofino.database.model.annotations.JDBCConnection;
import com.manydesigns.portofino.database.model.annotations.JNDIConnection;
import com.manydesigns.portofino.database.model.annotations.SelectionProvider;
import com.manydesigns.portofino.database.multitenancy.MultiTenant;
import com.manydesigns.portofino.liquibase.VFSResourceAccessor;
import com.manydesigns.portofino.model.*;
import com.manydesigns.portofino.model.annotations.Enabled;
import com.manydesigns.portofino.database.model.*;
import com.manydesigns.portofino.database.model.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.model.annotations.Id;
import com.manydesigns.portofino.model.annotations.KeyMappings;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.hibernate.HibernateDatabaseSetup;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryAndCodeBase;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryBuilder;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementation;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementationFactory;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.reflection.ViewAccessor;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.configuration2.Configuration;
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

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static final String LIQUIBASE_CONTEXT = "liquibase.context";
    public final static String changelogFileNameTemplate = "liquibase.changelog.xml";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final DatabasePlatformsRegistry databasePlatformsRegistry;
    protected final Map<String, HibernateDatabaseSetup> setups;
    protected final LinkedList<Database> databases = new LinkedList<>();

    protected final Configuration configuration;
    protected ModelService modelService;
    @Autowired
    protected MultiTenancyImplementationFactory multiTenancyImplementationFactory = MultiTenancyImplementationFactory.DEFAULT;
    public final BehaviorSubject<Status> status = BehaviorSubject.create();
    public final PublishSubject<DatabaseSetupEvent> databaseSetupEvents = PublishSubject.create();
    protected Disposable modelEventsSubscription;

    public enum Status {
        STARTING, STARTED, STOPPING, STOPPED
    }

    public CacheResetListenerRegistry cacheResetListenerRegistry;
    public static final Logger logger = LoggerFactory.getLogger(Persistence.class);

    public Persistence(
            ModelService modelService, Configuration configuration, DatabasePlatformsRegistry databasePlatformsRegistry)
            throws FileSystemException {
        this.modelService = modelService;
        this.configuration = configuration;
        this.databasePlatformsRegistry = databasePlatformsRegistry;
        setups = new HashMap<>();
    }

    //**************************************************************************
    // Model initialization
    //**************************************************************************

    protected boolean tryLoadingLegacyModel() {
        try {
            XMLModel modelIO = new XMLModel(modelService.getModelDirectory(), this);
            Model model = modelService.loadModel(modelIO);
            if(model != null) {
                modelIO.getDatabases().forEach(
                        newDb -> {
                            String databaseName = newDb.getDatabaseName();
                            databases.removeIf(oldDb -> oldDb.getDatabaseName().equals(databaseName));
                            getModel().getDomains().removeIf(d -> d.getName().equals(databaseName));
                            databases.add(newDb);
                            getModel().getDomains().add(newDb.getModelElement());
                        });
                logger.info("Loaded legacy XML model. It will be converted to the new format upon save.");
                return true;
            }
        } catch (Exception e) {
            logger.error("Cannot load/parse XML model", e);
        }
        return false;
    }

    private void annotateDatabase(Database db) {
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
    }

    protected Database setupDatabase(Domain domain) {
        //Can't use getJavaAnnotation as they've not yet been resolved
        EAnnotation ann = domain.getEAnnotation(JDBCConnection.class.getName());
        ConnectionProvider connectionProvider = null;
        if(ann != null) {
            JdbcConnectionProvider cp = new JdbcConnectionProvider();
            cp.setUrl(ann.getDetails().get("url"));
            cp.setDriver(ann.getDetails().get("driver"));
            cp.setUsername(ann.getDetails().get("username"));
            cp.setPassword(ann.getDetails().get("password"));
            connectionProvider = cp;
        } else {
            ann = domain.getEAnnotation(JNDIConnection.class.getName());
            if(ann != null) {
                JndiConnectionProvider cp = new JndiConnectionProvider();
                cp.setJndiResource(ann.getDetails().get("name"));
                connectionProvider = cp;
            }
        }
        if(connectionProvider != null) {
            Database database = new Database(domain);
            Database existing = DatabaseLogic.findDatabaseByName(databases, database.getName());
            if (existing != null) {
                logger.debug("Database " + database.getName() + " already exists");
                return existing;
            }
            databases.add(database);
            connectionProvider.setDatabase(database);
            database.setConnectionProvider(connectionProvider);
            domain.getSubdomains().forEach(subd -> setupSchema(database, subd));
            return database;
        } else {
            return null;
        }
    }

    protected Schema setupSchema(Database database, Domain domain) {
        Schema schema = new Schema(domain);
        schema.setDatabase(database);
        database.getSchemas().add(schema);
        EAnnotation schemaAnn =
                domain.getEAnnotation(com.manydesigns.portofino.database.model.annotations.Schema.class.getName());
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
        entity.getEAttributes().forEach(property -> setupColumn(table, pk, property));
        if(!pk.getColumns().isEmpty()) {
            pk.getColumns().sort(Comparator.comparingInt(c ->
                    Integer.parseInt(c.getAnnotation(Id.class).get().getPropertyValue("order"))));
            table.setPrimaryKey(pk);
        }
        entity.getEReferences().forEach(reference -> {
            if(!reference.isDerived()) {
                setupForeignKey(table, reference);
            }
        });
        //Copy to avoid ConcurrentModificationException as creating new database selection providers will add
        //annotations to the table
        new ArrayList<>(entity.getEAnnotations()).forEach(a -> {
            if(a.getSource().equals(SelectionProvider.class.getName())) {
                DatabaseSelectionProvider sp = new DatabaseSelectionProvider(table);
                sp.setName(a.getDetails().get("name"));
                sp.setToDatabase(a.getDetails().get("database"));
                String language = a.getDetails().get("language");
                if(language.equalsIgnoreCase("hql")) {
                    sp.setHql(a.getDetails().get("query"));
                } else if(language.equalsIgnoreCase("sql")) {
                    sp.setSql(a.getDetails().get("query"));
                } else {
                    //TODO properly report errors
                    throw new IllegalArgumentException("Invalid selection provider language: " + language);
                }
                for (String property : a.getDetails().get("properties").split(",")) {
                    Reference ref = new Reference(sp);
                    ref.setFromPropertyName(property.trim());
                    sp.getReferences().add(ref);
                }
                table.getSelectionProviders().add(sp);
            }
        });
    }

    protected void setupColumn(Table table, PrimaryKey pk, EAttribute property) {
        Column column = new Column(property);
        column.setTable(table);
        table.getColumns().add(column);
        //TODO column type
        if(property.getEAnnotation(Id.class.getName()) != null) {
            pk.add(column);
        }
    }

    protected void setupForeignKey(Table table, EReference reference) {
        ForeignKey fk = new ForeignKey(table);
        fk.setName(reference.getName());
        fk.setToEntityName(reference.getEType().getName());
        EAnnotation mappings = reference.getEAnnotation(KeyMappings.class.getName());
        if(mappings != null) {
            mappings.getDetails().forEach(e -> {
                Reference ref = new Reference(fk);
                ref.setFromPropertyName(e.getKey());
                ref.setToPropertyName(e.getValue());
                fk.getReferences().add(ref);
            });
        }
        table.getForeignKeys().add(fk);
    }

    public void runLiquibase(Database database) {
        logger.info("Updating database definitions");
        FileObject appDir = modelService.getApplicationDirectory();
        ResourceAccessor resourceAccessor = new VFSResourceAccessor(appDir);
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
                String relativeChangelogPath = appDir.getName().getRelativeName(changelogFile.getName());
                Liquibase lq = new Liquibase(relativeChangelogPath, resourceAccessor, lqDatabase);

                String[] contexts = configuration.getStringArray(LIQUIBASE_CONTEXT);
                logger.info("Using context {}", Arrays.toString(contexts));
                lq.update(new Contexts(contexts));
            } catch (Exception e) {
                logger.error("Couldn't update database: " + schemaName, e);
            }
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
        //TODO it would perhaps be preferable that we generated REPLACED events here rather than REMOVED followed by ADDED
        setups.clear();
        modelService.getModel().init();
        for (Database database : databases) {
            initDatabase(database);
        }
        databases.forEach(this::annotateDatabase);
        if(cacheResetListenerRegistry != null) {
            cacheResetListenerRegistry.fireReset(new CacheResetEvent(this));
        }
    }

    public synchronized void removeDatabase(Database database) {
        databases.remove(database);
        EPackage pkg = database.getModelElement();
        getModel().getDomains().remove(pkg);
    }

    public void addDatabase(Database database) {
        databases.add(database);
        getModel().getDomains().add(database.getModelElement());
    }

    protected boolean initDatabase(Database database) {
        new ResetVisitor().visit(database);
        new InitVisitor(databases, configuration).visit(database);
        new LinkVisitor(databases, configuration).visit(database);
        Boolean enabled = database.getJavaAnnotation(Enabled.class).map(Enabled::value).orElse(true);
        if(enabled) {
            initConnectionProvider(database);
            return true;
        } else {
            logger.info("Skipping disabled database " + database.getQualifiedName());
            return false;
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
        for (Database database : databases) {
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
        for (Database database : databases) {
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
        return modelService.getModel();
    }

    public synchronized void syncDataModel(String databaseName) throws Exception {
        Database sourceDatabase = DatabaseLogic.findDatabaseByName(databases, databaseName);
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
        Database targetDatabase = dbSyncer.syncDatabase(databases);
        removeDatabase(sourceDatabase);
        addDatabase(targetDatabase);
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
            //TODO use proper exception
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
        Database database = DatabaseLogic.findDatabaseByName(databases, databaseName);
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

    public void start() {
        status.onNext(Status.STARTING);
        loadModel();
        AtomicBoolean loading = new AtomicBoolean(false);
        modelEventsSubscription = modelService.modelEvents.subscribe(evt -> {
            if(evt == ModelService.EventType.LOADED) {
                if(!loading.get()) try {
                    loading.set(true);
                    loadModel();
                } finally {
                    loading.set(false);
                }
            } else if (evt == ModelService.EventType.SAVED) {
                new XMLModel(modelService.getModelDirectory(), this).delete();
            }
        });
        for(Database database : databases) {
            if(ConnectionProvider.STATUS_CONNECTED.equals(database.getConnectionProvider().getStatus())) {
                runLiquibase(database);
            }
        }
        status.onNext(Status.STARTED);
    }

    public void loadModel() {
        if (!tryLoadingLegacyModel()) {
            getModel().getDomains().forEach(this::setupDatabase);
        }
        initModel();
    }

    public void stop() {
        status.onNext(Status.STOPPING);
        closeSessions();
        if (modelEventsSubscription != null) {
            modelEventsSubscription.dispose();
        }
        for(HibernateDatabaseSetup setup : setups.values()) {
            setup.dispose();
        }
        for (Database database : databases) {
            ConnectionProvider connectionProvider = database.getConnectionProvider();
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
        FileObject dbDir = modelService.getModelDirectory().resolveFile(schema.getDatabaseName());
        FileObject schemaDir = dbDir.resolveFile(schema.getSchemaName());
        return schemaDir.resolveFile(changelogFileNameTemplate);
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

    public List<Database> getDatabases() {
        return databases;
    }
}
