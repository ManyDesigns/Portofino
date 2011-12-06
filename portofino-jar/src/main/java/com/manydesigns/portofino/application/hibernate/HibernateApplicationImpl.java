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

package com.manydesigns.portofino.application.hibernate;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.connections.Connections;
import com.manydesigns.portofino.database.QueryUtils;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.pages.crud.Crud;
import com.manydesigns.portofino.reflection.CrudAccessor;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.util.ConfigurationResourceBundle;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class HibernateApplicationImpl implements Application {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String changelogFileNameTemplate = "{0}-changelog.xml";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final org.apache.commons.configuration.Configuration portofinoConfiguration;
    protected final DatabasePlatformsManager databasePlatformsManager;
    protected Connections connectionProviders;
    protected Model model;
    protected Map<String, HibernateDatabaseSetup> setups;

    protected final String appId;

    protected final File appDir;
    protected final File appBlobsDir;
    protected final File appConnectionsFile;
    protected final File appDbsDir;
    protected final File appModelFile;
    protected final File appScriptsDir;
    protected final File appStorageDir;
    protected final File appWebDir;


    public static final Logger logger =
            LoggerFactory.getLogger(HibernateApplicationImpl.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public HibernateApplicationImpl(String appId,
                                    org.apache.commons.configuration.Configuration portofinoConfiguration,
                                    DatabasePlatformsManager databasePlatformsManager,
                                    File appDir,
                                    File appBlobsDir,
                                    File appConnectionsFile,
                                    File appDbsDir,
                                    File appModelFile,
                                    File appScriptsDir,
                                    File appStorageDir,
                                    File appWebDir
    ) {
        this.appId = appId;
        this.portofinoConfiguration = portofinoConfiguration;
        this.databasePlatformsManager = databasePlatformsManager;
        this.appDir = appDir;
        this.appBlobsDir = appBlobsDir;
        this.appConnectionsFile = appConnectionsFile;
        this.appDbsDir = appDbsDir;
        this.appModelFile = appModelFile;
        this.appScriptsDir = appScriptsDir;
        this.appStorageDir = appStorageDir;
        this.appWebDir = appWebDir;
    }

    //**************************************************************************
    // Model loading
    //**************************************************************************

    public void loadConnections() {
        logger.info("Loading connections from file: {}", appConnectionsFile);
        try {
            JAXBContext jc = JAXBContext.newInstance(
                    Connections.JAXB_CONNECTIONS_PACKAGES);
            Unmarshaller um = jc.createUnmarshaller();
            connectionProviders = (Connections) um.unmarshal(appConnectionsFile);
            connectionProviders.reset();
            connectionProviders.init(databasePlatformsManager);
        } catch (Exception e) {
            logger.error("Cannot load/parse file: " + appConnectionsFile, e);
            return;
        }

        logger.info("Updating database definitions");
        ResourceAccessor resourceAccessor =
                new FileSystemResourceAccessor();
        for (ConnectionProvider current : connectionProviders.getConnections()) {
            String databaseName = current.getDatabaseName();
            String changelogFileName =
                    MessageFormat.format(
                            changelogFileNameTemplate, databaseName);
            File changelogFile =
                new File(appDbsDir, changelogFileName);
            logger.info("Running changelog file: {}", changelogFile);
            Connection connection = null;
            try {
                connection = current.acquireConnection();
                JdbcConnection jdbcConnection = new JdbcConnection(connection);
                liquibase.database.Database lqDatabase =
                        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
                //XXX temporaneo funziona con uno schema solo
                //lqDatabase.setDefaultSchemaName(current.getIncludeSchemas());
                Liquibase lq = new Liquibase(
                        changelogFile.getAbsolutePath(),
                        resourceAccessor,
                        lqDatabase);
                lq.update(null);
            } catch (Exception e) {
                logger.error("Couldn't update database: " + databaseName, e);
            } finally {
                current.releaseConnection(connection);
            }

        }
    }

    public synchronized void loadXmlModel() {
        logger.info("Loading xml model from file: {}",
                appModelFile.getAbsolutePath());

        try {
            JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
            Unmarshaller um = jc.createUnmarshaller();
            Model loadedModel = (Model) um.unmarshal(appModelFile);
            boolean syncOnStart = false;
            if (syncOnStart) {
                for (ConnectionProvider connectionProvider :
                        connectionProviders.getConnections()) {
                    String databaseName = connectionProvider.getDatabaseName();
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
    }

    //**************************************************************************
    // Database stuff
    //**************************************************************************

    public ConnectionProvider getConnectionProvider(String databaseName) {
        for (ConnectionProvider current : connectionProviders.getConnections()) {
            if (current.getDatabaseName().equals(databaseName)) {
                return current;
            }
        }
        return null;
    }

    public void addConnectionProvider(ConnectionProvider connectionProvider) {
        logger.info("Adding a new connection Provider: {}", connectionProvider);
        connectionProviders.getConnections().add(connectionProvider);
        writeToConnectionFile();
    }

    public void deleteConnectionProvider(String[] connectionProvider) {
        logger.info("Deleting connection Provider: {}", connectionProvider);
        List<ConnectionProvider> toBeRemoved = new ArrayList<ConnectionProvider>();
        for(String databaseName : connectionProvider){
            for(ConnectionProvider current : connectionProviders.getConnections()){
                if(current.getDatabaseName().equals(databaseName)){
                    toBeRemoved.add(current);
                }
            }
        }
        connectionProviders.getConnections().removeAll(toBeRemoved);
        writeToConnectionFile();
    }

     public void deleteConnectionProvider(String connectionProvider) {
        logger.info("Deleting connection Provider: {}", connectionProvider);
        for(ConnectionProvider current : connectionProviders.getConnections()){
            if(current.getDatabaseName().equals(connectionProvider)){
                connectionProviders.getConnections().remove(current);
                break;
            }
        }

        writeToConnectionFile();
    }

    public void updateConnectionProvider(ConnectionProvider connectionProvider) {
        logger.info("Updating connection Provider: {}", 
                connectionProvider.toString());
        for (ConnectionProvider conn : connectionProviders.getConnections()){
            if (conn.getDatabaseName().equals(connectionProvider.getDatabaseName())){
                deleteConnectionProvider(connectionProvider.getDatabaseName());
                addConnectionProvider(connectionProvider);
                return;
            }
        }
        writeToConnectionFile();
    }
    
    public org.apache.commons.configuration.Configuration getPortofinoProperties() {
        return portofinoConfiguration;
    }

    public DatabasePlatformsManager getDatabasePlatformsManager() {
        return databasePlatformsManager;
    }

    //**************************************************************************
    // Modell access
    //**************************************************************************

    public List<ConnectionProvider> getConnectionProviders() {
        return connectionProviders.getConnections();
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
        String databaseName = table.getDatabaseName();
        return getSession(databaseName);
    }

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
    
    public void commit() {
        for (HibernateDatabaseSetup current : setups.values()) {
            Session session = current.getThreadSession();
            if (session != null) {
                Transaction tx = session.getTransaction();
                if (null != tx && tx.isActive()) {
                    try {
                        tx.commit();
                    } catch (HibernateException e) {
                        closeSession(current);
                        throw e;
                    }
                }
            }
        }
    }

    public void rollback() {
        for (HibernateDatabaseSetup current : setups.values()) {
            Session session = current.getThreadSession();
            if (session != null) {
                Transaction tx = session.getTransaction();
                if (null != tx && tx.isActive()) {
                    tx.rollback();
                }
            }
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

    public @NotNull CrudAccessor getCrudAccessor(@NotNull Crud crud) {
        TableAccessor tableAccessor = new TableAccessor(crud.getActualTable());
        return new CrudAccessor(crud, tableAccessor);
    }

    //**************************************************************************
    // User
    //**************************************************************************

    public User findUserByEmail(String email) {
        String qualifiedTableName = SecurityLogic.USERTABLE;
        Session session = getSessionByQualifiedTableName(qualifiedTableName);
        org.hibernate.Criteria criteria = session.createCriteria(SecurityLogic.USER_ENTITY_NAME);
        criteria.add(Restrictions.eq("email", email));
        return (User) criteria.uniqueResult();
    }

    public User findUserByUserName(String username) {
        String qualifiedTableName = SecurityLogic.USERTABLE;
        Session session = getSessionByQualifiedTableName(qualifiedTableName);
        org.hibernate.Criteria criteria = session.createCriteria(SecurityLogic.USER_ENTITY_NAME);
        criteria.add(Restrictions.eq(SessionAttributes.USER_NAME, username));
        return (User) criteria.uniqueResult();
    }

    public User findUserByToken(String token) {
        String qualifiedTableName = SecurityLogic.USERTABLE;
        Session session = getSessionByQualifiedTableName(qualifiedTableName);
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
        TableAccessor table = getTableAccessor(SecurityLogic.GROUPTABLE);
        assert table != null;

        String actualEntityName = table.getTable().getActualEntityName();
        Session session = getSessionByQualifiedTableName(SecurityLogic.GROUPTABLE);
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

    private void writeToConnectionFile() {
        OutputStream os = null;
        try {
            File tempFile = File.createTempFile("portofino", "connections.xml");
            os = new FileOutputStream(tempFile);

            JAXBContext jc = JAXBContext.newInstance(Connections.JAXB_CONNECTIONS_PACKAGES);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(connectionProviders, tempFile);
            os.flush();

            moveFileSafely(tempFile, appConnectionsFile.getAbsolutePath());

            logger.info("Saved connections to file: {}", appConnectionsFile);
        } catch (Throwable e) {
            logger.error("Cannot save connections to file: " + appConnectionsFile, e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    protected void moveFileSafely(File tempFile, String fileName) throws IOException {
        File destination = new File(fileName);
        if(!destination.exists()) {
            FileUtils.moveFile(tempFile, destination);
        } else {
            File backup = File.createTempFile(destination.getName(), ".backup");
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

    public File getAppDir() {
        return appDir;
    }

    public File getAppBlobsDir() {
        return appBlobsDir;
    }

    public File getAppConnectionsFile() {
        return appConnectionsFile;
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

    //**************************************************************************
    // I18n
    //**************************************************************************

    private final ConcurrentMap<Locale, ConfigurationResourceBundle> resourceBundles =
            new ConcurrentHashMap<Locale, ConfigurationResourceBundle>();

    protected String getBundleFileName(String baseName, Locale locale) {
        return getBundleName(baseName, locale) + ".properties";
    }

    protected String getBundleName(String baseName, Locale locale) {
        if(locale == Locale.ROOT) {
            return baseName;
        }

        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();

        if (StringUtils.isBlank(language) && StringUtils.isBlank(country) && StringUtils.isBlank(variant)) {
            return baseName;
        }

        String name = baseName + "_";
        if (!StringUtils.isBlank(variant)) {
            name += language + "_" + country + "_" + variant;
        } else if (!StringUtils.isBlank(country)) {
            name += language + "_" + country;
        } else {
            name += language;
        }
        return name;
    }

    public ResourceBundle getBundle(Locale locale) {
        ConfigurationResourceBundle bundle = resourceBundles.get(locale);
        if(bundle == null) {
            ResourceBundle parentBundle = ResourceBundle.getBundle("portofino-messages", locale);
            PropertiesConfiguration configuration;
            try {
                File bundleFile = getBundleFile(locale);
                if(!bundleFile.exists() && !locale.equals(parentBundle.getLocale())) {
                    bundleFile = getBundleFile(parentBundle.getLocale());
                }
                if(!bundleFile.exists()) {
                    return parentBundle;
                }
                configuration = new PropertiesConfiguration(bundleFile);
                FileChangedReloadingStrategy reloadingStrategy = new FileChangedReloadingStrategy();
                configuration.setReloadingStrategy(reloadingStrategy);
                bundle = new ConfigurationResourceBundle(configuration);
                bundle.setParent(parentBundle);
                resourceBundles.put(locale, bundle);
            } catch (ConfigurationException e) {
                logger.warn("Couldn't load app resource bundle for locale " + locale, e);
                return parentBundle;
            }
        }
        return bundle;
    }

    protected File getBundleFile(Locale locale) {
        String resourceName = getBundleFileName("portofino-messages", locale);
        return new File(appDir, resourceName);
    }

}
