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

import com.manydesigns.portofino.cache.CacheResetEvent;
import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import com.manydesigns.portofino.database.model.ForeignKey;
import com.manydesigns.portofino.database.model.PrimaryKey;
import com.manydesigns.portofino.database.model.annotations.JDBCConnection;
import com.manydesigns.portofino.database.model.annotations.JNDIConnection;
import com.manydesigns.portofino.database.model.annotations.SelectionProvider;
import com.manydesigns.portofino.config.ConfigurationSource;
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
import com.manydesigns.portofino.persistence.hibernate.Events;
import com.manydesigns.portofino.persistence.hibernate.DatabaseAccessor;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryAndCodeBase;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryBuilder;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementation;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementationFactory;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyStrategy;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.reflection.ViewAccessor;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.vfs2.*;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
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

    public static final String LIQUIBASE_CONTEXT = "liquibase.context";
    public static final String CHANGELOG_FILE_NAME_TEMPLATE = "liquibase.changelog.";
    public static final String DATABASES_DOMAIN_NAME = "databases";
    public static final String LEGACY_MODEL_DIRECTORY = "portofino-model";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final DatabasePlatformsRegistry databasePlatformsRegistry;
    protected final Map<String, DatabaseAccessor> accessors;
    protected final LinkedList<Database> databases = new LinkedList<>();

    protected final ConfigurationSource configuration;
    protected final ModelService modelService;
    @Autowired
    protected MultiTenancyImplementationFactory multiTenancyImplementationFactory = MultiTenancyImplementationFactory.DEFAULT;
    public final BehaviorSubject<Status> status = BehaviorSubject.create();
    public final PublishSubject<DatabaseSetupEvent> databaseSetupEvents = PublishSubject.create();
    protected boolean convertLegacyModel = true; // If false (during testing), don't delete the legacy model

    public enum Status {
        STARTING, STARTED, STOPPING, STOPPED
    }

    public CacheResetListenerRegistry cacheResetListenerRegistry;
    public static final Logger logger = LoggerFactory.getLogger(Persistence.class);

    public Persistence(
            ModelService modelService, ConfigurationSource configuration,
            DatabasePlatformsRegistry databasePlatformsRegistry)
            throws FileSystemException {
        this.modelService = modelService;
        this.configuration = configuration;
        this.databasePlatformsRegistry = databasePlatformsRegistry;
        accessors = new HashMap<>();
    }

    //**************************************************************************
    // Model initialization
    //**************************************************************************

    protected boolean tryLoadingLegacyModel() {
        synchronized (modelService) {
            try {
                FileObject modelDirectory =
                        modelService.getApplicationDirectory().resolveFile(LEGACY_MODEL_DIRECTORY);
                XMLModel modelIO = new XMLModel(modelDirectory, this);
                Model model = modelIO.load();
                if(model != null) {
                    modelIO.getDatabases().forEach(
                            newDb -> {
                                String databaseName = newDb.getDatabaseName();
                                databases.removeIf(oldDb -> oldDb.getDatabaseName().equals(databaseName));
                                model.getDomains().removeIf(d -> d.getName().equals(databaseName));
                            });
                    modelIO.getDatabases().forEach(
                            newDb -> {
                                getDatabaseDomains().add(newDb.getModelElement());
                                databases.add(newDb);
                                copyLiquibaseChangelogs(modelDirectory, newDb);
                            });
                    logger.info("Loaded legacy XML model");
                    initModel();
                    if (convertLegacyModel) {
                        saveModel();
                        modelIO.delete();
                        logger.info("Converted legacy XML model to the new format");
                    }
                    return true;
                }
            } catch (Exception e) {
                logger.error("Cannot load/parse XML model", e);
            }
        }
        return false;
    }

    protected void copyLiquibaseChangelogs(FileObject modelDirectory, Database db)  {
        try {
            FileObject dbDir = modelDirectory.getChild(db.getName());
            for (Schema schema : db.getSchemas()) {
                FileObject schemaDir = dbDir.getChild(schema.getName());
                if (schemaDir != null && schemaDir.getType() == FileType.FOLDER) {
                    for (FileObject child : schemaDir.getChildren()) {
                        String fileName = child.getName().getBaseName();
                        if (fileName.startsWith(CHANGELOG_FILE_NAME_TEMPLATE)) {
                            FileObject domainDirectory = modelService.getDomainDirectory(schema.getModelElement());
                            if (!domainDirectory.exists()) {
                                domainDirectory.createFolder();
                            }
                            if (domainDirectory.getType() == FileType.FOLDER) {
                                if (convertLegacyModel) {
                                    child.moveTo(domainDirectory.resolveFile(fileName));
                                } else {
                                    domainDirectory.copyFrom(child.getParent(), new FileSelector() {
                                        @Override
                                        public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                                            return fileInfo.getFile().getPath().equals(child.getPath());
                                        }

                                        @Override
                                        public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                                            return true;
                                        }
                                    });
                                }
                            } else {
                                logger.error(
                                        "Could not copy Liquibase changelog " + child.getName().getPath() + " to " +
                                                domainDirectory.getName().getPath() + " because it's not a directory.");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Could not copy liquibase changelog(s) for database " + db.getName(), e);
        }
    }

    public void saveModel() throws IOException {
        modelService.saveDomain(getDatabaseDomain());
    }

    public void saveDatabase(Database database) throws IOException {
        modelService.saveDomain(database.getModelElement());
    }

    protected Domain getDatabaseDomain() {
        try {
            return modelService.ensureTopLevelDomain(DATABASES_DOMAIN_NAME, !convertLegacyModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected EList<Domain> getDatabaseDomains() {
        return getDatabaseDomain().getSubdomains();
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
        logger.info("Setting up database " + domain.getQualifiedName());
        ConnectionProvider connectionProvider = setupConnectionProvider(domain);
        if(connectionProvider != null) {
            Database database = DatabaseLogic.findDatabaseByName(databases, domain.getName());
            boolean alreadyExists = database != null;
            if (alreadyExists) {
                logger.debug("Database " + database.getName() + " already exists");
            } else {
                database = new Database(domain);
                databases.add(database);
            }
            //Can't use getJavaAnnotation as they've not yet been resolved
            EAnnotation ann = domain.getEAnnotation(
                    com.manydesigns.portofino.database.model.annotations.Database.class.getName()
            );
            if(ann != null) {
                database.setEntityMode(ann.getDetails().get("entityMode"));
            }
            connectionProvider.setDatabase(database);
            database.setConnectionProvider(connectionProvider);
            if (!alreadyExists) { // TODO check – should we also refresh the schemas here?
                Database db = database;
                domain.getSubdomains().forEach(subd -> setupSchema(db, subd));
            }
            return database;
        } else {
            return null;
        }
    }

    @Nullable
    protected ConnectionProvider setupConnectionProvider(Domain domain) {
        ConnectionProvider connectionProvider = null;
        //Can't use getJavaAnnotation as they've not yet been resolved
        EAnnotation ann = domain.getEAnnotation(JDBCConnection.class.getName());
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
        return connectionProvider;
    }

    protected Schema setupSchema(Database database, Domain domain) {
        Schema schema = new Schema(domain);
        schema.setDatabase(database);
        database.addSchema(schema);
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
        ResourceAccessor resourceAccessor = new CompositeResourceAccessor(
                new VFSResourceAccessor(appDir), new ClassLoaderResourceAccessor()
        );
        ConnectionProvider connectionProvider = database.getConnectionProvider();
        for(Schema schema : database.getSchemas()) {
            String schemaName = schema.getSchemaName();
            try(Connection connection = connectionProvider.acquireConnection()) {
                FileObject changelogFile = getLiquibaseChangelogFile(schema);
                if(changelogFile == null || changelogFile.getType() != FileType.FILE) {
                    logger.debug("Changelog file does not exist or is not a normal file, skipping: {}", changelogFile);
                    continue;
                }
                logger.info("Running changelog file: {}", changelogFile);
                JdbcConnection jdbcConnection = new JdbcConnection(connection);
                liquibase.database.Database lqDatabase =
                        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
                lqDatabase.setDefaultSchemaName(schema.getActualSchemaName());
                String relativeChangelogPath = appDir.getName().getRelativeName(changelogFile.getName());
                try(Liquibase lq = new Liquibase(relativeChangelogPath, resourceAccessor, lqDatabase)) {
                    String[] contexts = configuration.getProperties().getStringArray(LIQUIBASE_CONTEXT);
                    logger.info("Using context {}", Arrays.toString(contexts));
                    lq.update(new Contexts(contexts));
                }
            } catch (Exception e) {
                logger.error("Couldn't update database: " + schemaName, e);
            }
        }
    }

    public synchronized void initModel() {
        getDatabaseDomains().forEach(this::setupDatabase);
        closeSessions();
        for (Map.Entry<String, DatabaseAccessor> current : accessors.entrySet()) {
            String databaseName = current.getKey();
            logger.debug("Cleaning up old accessor for: {}", databaseName);
            DatabaseAccessor accessor = current.getValue();
            try {
                accessor.dispose();
            } catch (Throwable t) {
                logger.warn("Cannot close session factory for: " + databaseName, t);
            }
            databaseSetupEvents.onNext(new DatabaseSetupEvent(DatabaseSetupEvent.REMOVED, accessor));
        }
        //TODO it would perhaps be preferable that we generated REPLACED events here rather than REMOVED followed by ADDED
        accessors.clear();
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
        getDatabaseDomains().remove(database.getModelElement());
    }

    public void addDatabase(Database database) {
        databases.add(database);
        getDatabaseDomains().add(database.getModelElement());
    }

    protected boolean initDatabase(Database database) {
        initModelObject(database);
        Boolean enabled = database.getJavaAnnotation(Enabled.class).map(Enabled::value).orElse(true);
        if(enabled) {
            initConnectionProvider(database);
            return true;
        } else {
            logger.info("Skipping disabled database " + database.getQualifiedName());
            return false;
        }
    }

    public void initModelObject(ModelObject modelObject) {
        new ResetVisitor().visit(modelObject);
        new InitVisitor(databases, configuration.getProperties()).visit(modelObject);
        new LinkVisitor(databases, configuration.getProperties()).visit(modelObject);
    }

    protected void initConnectionProvider(Database database) {
        logger.info("Initializing connection provider for database " + database.getDatabaseName());
        try {
            ConnectionProvider connectionProvider = database.getConnectionProvider();
            connectionProvider.init(databasePlatformsRegistry);
            if (connectionProvider.getStatus().equals(ConnectionProvider.STATUS_CONNECTED)) {
                MultiTenancyImplementation implementation = getMultiTenancyImplementation(database);
                Events events = new Events();
                SessionFactoryBuilder builder =
                        new SessionFactoryBuilder(database, configuration.getProperties(), events, implementation);
                SessionFactoryAndCodeBase sessionFactoryAndCodeBase = builder.buildSessionFactory();
                DatabaseAccessor accessor =
                        new DatabaseAccessor(
                                database, sessionFactoryAndCodeBase.sessionFactory,
                                sessionFactoryAndCodeBase.codeBase, builder.getEntityMode(),
                                configuration.getProperties(), events, implementation);
                String databaseName = database.getDatabaseName();
                DatabaseAccessor oldAccessor = accessors.get(databaseName);
                accessors.put(databaseName, accessor);
                if(oldAccessor != null) {
                    oldAccessor.dispose();
                    databaseSetupEvents.onNext(new DatabaseSetupEvent(oldAccessor, accessor));
                } else {
                    databaseSetupEvents.onNext(new DatabaseSetupEvent(DatabaseSetupEvent.ADDED, accessor));
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
        checkStarted();
        for (Database database : databases) {
            if (!ConnectionProvider.STATUS_CONNECTED.equals(database.getConnectionProvider().getStatus())) {
                logger.info("Retrying failed connection to database " + database.getDatabaseName());
                initConnectionProvider(database);
            }
        }
    }

    public void checkStarted() {
        Status currentStatus = status.getValue();
        if(currentStatus != Status.STARTED) {
            throw new PersistenceNotStartedException(currentStatus);
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

    public ConfigurationSource getConfiguration() {
        return configuration;
    }

    public DatabasePlatformsRegistry getDatabasePlatformsRegistry() {
        return databasePlatformsRegistry;
    }

    //**************************************************************************
    // Model access
    //**************************************************************************

    public synchronized void syncDataModel(String databaseName) throws Exception {
        Database sourceDatabase = DatabaseLogic.findDatabaseByName(databases, databaseName);
        if(sourceDatabase == null) {
            throw new IllegalArgumentException("Database " + databaseName + " does not exist");
        }
        if(configuration.getProperties().getBoolean(DatabaseModule.LIQUIBASE_ENABLED, true)) {
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
        return ensureDatabaseAccessor(databaseName).getThreadSession();
    }

    protected DatabaseAccessor ensureDatabaseAccessor(String databaseName) {
        DatabaseAccessor accessor = getDatabaseAccessor(databaseName);
        if (accessor == null) {
            //TODO use proper exception
            throw new Error("No accessor exists for database: " + databaseName);
        }
        return accessor;
    }

    @Deprecated
    public DatabaseAccessor getDatabaseSetup(String databaseName) {
        return getDatabaseAccessor(databaseName);
    }

    public DatabaseAccessor getDatabaseAccessor(String databaseName) {
        return accessors.get(databaseName);
    }

    public void closeSessions() {
        for (DatabaseAccessor current : accessors.values()) {
            closeSession(current);
        }
    }

    public void closeSession(String databaseName) {
        closeSession(ensureDatabaseAccessor(databaseName));
    }

    protected void closeSession(DatabaseAccessor current) {
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
        if (!tryLoadingLegacyModel()) {
            initModel();
        }
        for(Database database : databases) {
            if(ConnectionProvider.STATUS_CONNECTED.equals(database.getConnectionProvider().getStatus())) {
                runLiquibase(database);
            }
        }
        status.onNext(Status.STARTED);
    }

    public void stop() {
        status.onNext(Status.STOPPING);
        closeSessions();
        for(DatabaseAccessor accessor : accessors.values()) {
            accessor.dispose();
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

    public FileObject getLiquibaseChangelogFile(Schema schema) throws FileSystemException {
        if(schema == null) {
            return null;
        }
        FileObject schemaDir = modelService.getDomainDirectory(schema.getModelElement());
        if (schemaDir.getType() == FileType.FOLDER) {
            for (FileObject child : schemaDir.getChildren()) {
                if (child.getName().getBaseName().startsWith(CHANGELOG_FILE_NAME_TEMPLATE)) {
                    return child;
                }
            }
        }
        return null;
    }

    public static class DatabaseSetupEvent {
        public static final int ADDED = +1, REMOVED = -1, REPLACED = 0;

        public DatabaseSetupEvent(int type, DatabaseAccessor accessor) {
            if(type == 0) {
                throw new IllegalArgumentException();
            }
            this.type = type;
            this.accessor = accessor;
        }

        public DatabaseSetupEvent(DatabaseAccessor accessor, DatabaseAccessor oldAccessor) {
            this.accessor = accessor;
            this.oldAccessor = oldAccessor;
            type = REPLACED;
        }

        public int type;
        public DatabaseAccessor accessor;
        public DatabaseAccessor oldAccessor;
    }

    public MultiTenancyImplementationFactory getMultiTenancyImplementationFactory() {
        return multiTenancyImplementationFactory;
    }

    public void setMultiTenancyImplementationFactory(MultiTenancyImplementationFactory multiTenancyImplementationFactory) {
        this.multiTenancyImplementationFactory = multiTenancyImplementationFactory;
    }

    public List<Database> getDatabases() {
        return Collections.unmodifiableList(databases);
    }

    public boolean isConvertLegacyModel() {
        return convertLegacyModel;
    }

    public void setConvertLegacyModel(boolean convertLegacyModel) {
        this.convertLegacyModel = convertLegacyModel;
    }
}
