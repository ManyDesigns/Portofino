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

import com.manydesigns.elements.fields.search.Criterion;
import com.manydesigns.elements.fields.search.TextMatchMode;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlSqlFormat;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.TableCriteria;
import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.connections.Connections;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.pages.crud.Crud;
import com.manydesigns.portofino.reflection.CrudAccessor;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UserUtils;
import liquibase.Liquibase;
import liquibase.database.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.Dialect;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    protected static final String WHERE_STRING = " WHERE ";
    protected static final Pattern FROM_PATTERN =
            Pattern.compile("(SELECT\\s+.*\\s+)?FROM\\s+([a-z_$\\u0080-\\ufffe]{1}[a-z_$1-9\\u0080-\\ufffe]*).*",
                            Pattern.CASE_INSENSITIVE | Pattern.DOTALL); //. (dot) matches newlines


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
    private static final String PORTOFINO_PUBLIC_USERS = "portofino.public.users";

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
                        new HibernateConfig(connectionProvider);
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

    public Object getObjectByPk(String qualifiedTableName,
                                Serializable pk) {
        Session session = getSession(qualifiedTableName);
        TableAccessor table = getTableAccessor(qualifiedTableName);
        String actualEntityName = table.getTable().getActualEntityName();
        Object result;
        PropertyAccessor[] keyProperties = table.getKeyProperties();
        int size = keyProperties.length;
        if (size > 1) {
            result = session.load(actualEntityName, pk);
            return result;
        }
        PropertyAccessor propertyAccessor = keyProperties[0];
        Serializable key = (Serializable) propertyAccessor.get(pk);
        result = session.load(actualEntityName, key);
        return result;
    }

    public Object getObjectByPk(String qualifiedTableName,
                                Serializable pk, String queryString, Object rootObject) {
        if(queryString.toUpperCase().indexOf("WHERE") == -1) {
            return getObjectByPk(qualifiedTableName, pk);
        }
        TableAccessor table = getTableAccessor(qualifiedTableName);
        List<Object> result;
        PropertyAccessor[] keyProperties = table.getKeyProperties();
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(queryString);
        String formatString = sqlFormat.getFormatString();
        Object[] ognlParameters = sqlFormat.evaluateOgnlExpressions(rootObject);
        int i = keyProperties.length;
        int p = ognlParameters.length;
        Object[] parameters = new Object[p + i];
        System.arraycopy(ognlParameters, 0, parameters, i, p);
        int indexOfWhere = formatString.toUpperCase().indexOf("WHERE") + 5; //5 = "WHERE".length()
        String formatStringPrefix = formatString.substring(0, indexOfWhere);
        formatString = formatString.substring(indexOfWhere);

        for(PropertyAccessor propertyAccessor : keyProperties) {
            i--;
            formatString = propertyAccessor.getName() + " = ? AND " + formatString;
            parameters[i] = propertyAccessor.get(pk);
        }
        formatString = formatStringPrefix + " " + formatString;
        result = runHqlQuery(qualifiedTableName, formatString, parameters);
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public List getAllObjects(String qualifiedTableName) {
        Session session = getSession(qualifiedTableName);

        org.hibernate.Criteria hibernateCriteria;
        Table table = DataModelLogic.findTableByQualifiedName(
                model, qualifiedTableName);
        String actualEntityName = table.getActualEntityName();

        hibernateCriteria = session.createCriteria(actualEntityName);
        //noinspection unchecked
        List result = hibernateCriteria.list();
        return result;
    }

    protected Session getSession(String qualifiedTableName) {
        Table table = DataModelLogic.findTableByQualifiedName(
                model, qualifiedTableName);
        String databaseName = table.getDatabaseName();
        return setups.get(databaseName).getThreadSession();
    }

    public QueryStringWithParameters getQueryStringWithParametersForCriteria(
            TableCriteria criteria) {
        if (criteria == null) {
            return new QueryStringWithParameters("", new Object[0]);
        }
        Table table = criteria.getTable();
        String actualEntityName = table.getActualEntityName();

        ArrayList<Object> parametersList = new ArrayList<Object>();
        StringBuilder whereBuilder = new StringBuilder();
        for (Criterion criterion : criteria) {
            PropertyAccessor accessor = criterion.getPropertyAccessor();
            String hqlFormat;
            if (criterion instanceof TableCriteria.EqCriterion) {
                TableCriteria.EqCriterion eqCriterion =
                        (TableCriteria.EqCriterion) criterion;
                Object value = eqCriterion.getValue();
                hqlFormat = "{0} = ?";
                parametersList.add(value);
            } else if (criterion instanceof TableCriteria.InCriterion) {
                TableCriteria.InCriterion inCriterion =
                        (TableCriteria.InCriterion) criterion;
                Object[] values = inCriterion.getValues();
                StringBuffer params = new StringBuffer();
                if (values != null){
                    boolean first = true;
                    for (Object value : values){
                        if (!first){
                            params.append(", ?");
                        } else {
                            params.append("?");
                            first = false;
                        }
                        parametersList.add(value);
                    }
                    hqlFormat = "{0} in ("+params.toString()+")";
                } else {
                    hqlFormat = null;
                }
            } else if (criterion instanceof TableCriteria.NeCriterion) {
                TableCriteria.NeCriterion neCriterion =
                        (TableCriteria.NeCriterion) criterion;
                Object value = neCriterion.getValue();
                hqlFormat = "{0} <> ?";
                parametersList.add(value);
            } else if (criterion instanceof TableCriteria.BetweenCriterion) {
                TableCriteria.BetweenCriterion betweenCriterion =
                        (TableCriteria.BetweenCriterion) criterion;
                Object min = betweenCriterion.getMin();
                Object max = betweenCriterion.getMax();
                hqlFormat = "{0} >= ? AND {0} <= ?";
                parametersList.add(min);
                parametersList.add(max);
            } else if (criterion instanceof TableCriteria.GtCriterion) {
                TableCriteria.GtCriterion gtCriterion =
                        (TableCriteria.GtCriterion) criterion;
                Object value = gtCriterion.getValue();
                hqlFormat = "{0} > ?";
                parametersList.add(value);
            } else if (criterion instanceof TableCriteria.GeCriterion) {
                TableCriteria.GeCriterion gtCriterion =
                        (TableCriteria.GeCriterion) criterion;
                Object value = gtCriterion.getValue();
                hqlFormat = "{0} >= ?";
                parametersList.add(value);
            } else if (criterion instanceof TableCriteria.LtCriterion) {
                TableCriteria.LtCriterion ltCriterion =
                        (TableCriteria.LtCriterion) criterion;
                Object value = ltCriterion.getValue();
                hqlFormat = "{0} < ?";
                parametersList.add(value);
            } else if (criterion instanceof TableCriteria.LeCriterion) {
                TableCriteria.LeCriterion leCriterion =
                        (TableCriteria.LeCriterion) criterion;
                Object value = leCriterion.getValue();
                hqlFormat = "{0} <= ?";
                parametersList.add(value);
            } else if (criterion instanceof TableCriteria.LikeCriterion) {
                TableCriteria.LikeCriterion likeCriterion =
                        (TableCriteria.LikeCriterion) criterion;
                String value = (String) likeCriterion.getValue();
                String pattern = processTextMatchMode(
                        likeCriterion.getTextMatchMode(), value);
                hqlFormat = "{0} like ?";
                parametersList.add(pattern);
            } else if (criterion instanceof TableCriteria.IlikeCriterion) {
                TableCriteria.IlikeCriterion ilikeCriterion =
                        (TableCriteria.IlikeCriterion) criterion;
                String value = (String) ilikeCriterion.getValue();
                String pattern = processTextMatchMode(
                        ilikeCriterion.getTextMatchMode(), value);
                hqlFormat = "lower({0}) like lower(?)";
                parametersList.add(pattern);
            } else if (criterion instanceof TableCriteria.IsNullCriterion) {
                hqlFormat = "{0} is null";
            } else if (criterion instanceof TableCriteria.IsNotNullCriterion) {
                hqlFormat = "{0} is not null";
            } else {
                logger.error("Unrecognized criterion: {}", criterion);
                throw new InternalError("Unrecognied criterion");
            }

            if (hqlFormat == null) {
                continue;
            }

            String hql = MessageFormat.format(hqlFormat,
                    accessor.getName());

            if (whereBuilder.length() > 0) {
                whereBuilder.append(" AND ");
            }
            whereBuilder.append(hql);
        }
        String whereClause = whereBuilder.toString();
        String queryString;
        if (whereClause.length() > 0) {
            queryString = MessageFormat.format(
                    "FROM {0}" + WHERE_STRING + "{1}",
                    actualEntityName,
                    whereClause);
        } else {
            queryString = MessageFormat.format(
                    "FROM {0}",
                    actualEntityName);
        }

        Object[] parameters = new Object[parametersList.size()];
        parametersList.toArray(parameters);

        return new QueryStringWithParameters(queryString, parameters);
    }

    protected String processTextMatchMode(TextMatchMode textMatchMode,
                                          String value) {
        String pattern;
        switch (textMatchMode) {
            case EQUALS:
                pattern = value;
                break;
            case CONTAINS:
                pattern = "%" + value + "%";
                break;
            case STARTS_WITH:
                pattern = value + "%";
                break;
            case ENDS_WITH:
                pattern = "%" + value;
                break;
            default:
                String msg = MessageFormat.format(
                        "Unrecognized text match mode: {0}",
                        textMatchMode);
                logger.error(msg);
                throw new InternalError(msg);
        }
        return pattern;
    }

    public List<Object> getObjects(TableCriteria criteria) {
        QueryStringWithParameters queryStringWithParameters =
                getQueryStringWithParametersForCriteria(criteria);

        return runHqlQuery(
                queryStringWithParameters.getQueryString(),
                queryStringWithParameters.getParamaters());
    }


    public List<Object> getObjects(String queryString, Object rootObject) {
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(queryString);
        String formatString = sqlFormat.getFormatString();
        Object[] parameters = sqlFormat.evaluateOgnlExpressions(rootObject);

        return runHqlQuery(formatString, parameters);
    }

    public List<Object> getObjects(String qualifiedTableName, String queryString, Object rootObject) {
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(queryString);
        String formatString = sqlFormat.getFormatString();
        Object[] parameters = sqlFormat.evaluateOgnlExpressions(rootObject);
        return runHqlQuery(qualifiedTableName, formatString, parameters);
    }

    public List<Object> getObjects(String queryString) {
        return getObjects(queryString, null);
    }

    public String getQualifiedTableNameFromQueryString(String queryString) {
        Matcher matcher = FROM_PATTERN.matcher(queryString);
        String entityName;
        if (matcher.matches()) {
            entityName =  matcher.group(2);
        } else {
            return null;
        }

        Table table = model.findTableByEntityName(entityName);
        return table.getQualifiedName();
    }

    public List<Object> getObjects(String queryString, TableCriteria criteria) {
        return getObjects(queryString, criteria, null);
    }

    public List<Object> getObjects(String queryString,
                                   TableCriteria criteria,
                                   Object rootObject) {
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(queryString);
        String formatString = sqlFormat.getFormatString();
        Object[] parameters = sqlFormat.evaluateOgnlExpressions(rootObject);
        boolean formatStringContainsWhere = formatString.toUpperCase().contains(WHERE_STRING);

        QueryStringWithParameters criteriaQuery =
                getQueryStringWithParametersForCriteria(criteria);
        String criteriaQueryString = criteriaQuery.getQueryString();
        Object[] criteriaParameters = criteriaQuery.getParamaters();

        // merge the hql strings
        int whereIndex = criteriaQueryString.toUpperCase().indexOf(WHERE_STRING);
        String criteriaWhereClause;
        if (whereIndex >= 0) {
            criteriaWhereClause =
                    criteriaQueryString.substring(
                            whereIndex + WHERE_STRING.length());
        } else {
            criteriaWhereClause = "";
        }

        String fullQueryString;
        if (criteriaWhereClause.length() > 0) {
            if (formatStringContainsWhere) {
                fullQueryString = MessageFormat.format(
                        "{0} AND {1}",
                        formatString,
                        criteriaWhereClause);
            } else {
                fullQueryString = MessageFormat.format(
                        "{0} WHERE {1}",
                        formatString,
                        criteriaWhereClause);
            }
        } else {
            fullQueryString = formatString;
        }

        // merge the parameters
        ArrayList<Object> mergedParametersList = new ArrayList<Object>();
        mergedParametersList.addAll(Arrays.asList(parameters));
        mergedParametersList.addAll(Arrays.asList(criteriaParameters));
        Object[] mergedParameters = new Object[mergedParametersList.size()];
        mergedParametersList.toArray(mergedParameters);

        return runHqlQuery(fullQueryString, mergedParameters);
    }

    public List<Object> runHqlQuery
            (String queryString, Object[] parameters) {
        return runHqlQuery(queryString, parameters, null);
    }

    public List<Object> runHqlQuery
            (String queryString, Object[] parameters, Integer maxResults) {
        String qualifiedName =
                getQualifiedTableNameFromQueryString(queryString);

        return runHqlQuery(qualifiedName, queryString, parameters, maxResults);
    }

    public List<Object> runHqlQuery
            (String qualifiedTableName, String queryString, Object[] parameters) {
        return runHqlQuery(qualifiedTableName, queryString, parameters, null);
    }

    public List<Object> runHqlQuery
            (String qualifiedTableName, String queryString,
             Object[] parameters, Integer maxResults) {
        Session session = getSession(qualifiedTableName);

        Query query = session.createQuery(queryString);
        for (int i = 0; i < parameters.length; i++) {
            query.setParameter(i, parameters[i]);
        }

        //noinspection unchecked
        if(maxResults != null) {
            query.setMaxResults(maxResults);
        }
        List<Object> result = query.list();
        return result;
    }


    public void saveObject(String qualifiedTableName, Object obj) {
        Session session = getSession(qualifiedTableName);
        session.beginTransaction();

        Table table = DataModelLogic.findTableByQualifiedName(
                model, qualifiedTableName);
        String actualEntityName = table.getActualEntityName();

        try {
            session.save(actualEntityName, obj);
            //session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }
    }


    public void updateObject(String qualifiedTableName, Object obj) {
        Session session = getSession(qualifiedTableName);
        session.beginTransaction();
        Table table = DataModelLogic.findTableByQualifiedName(
                model, qualifiedTableName);
        String actualEntityName = table.getActualEntityName();

        try {
            session.update(actualEntityName, obj);
            //session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    public void deleteObject(String qualifiedTableName, Object obj) {
        Session session = getSession(qualifiedTableName);
        session.beginTransaction();
        Table table = DataModelLogic.findTableByQualifiedName(
                model, qualifiedTableName);
        String actualEntityName = table.getActualEntityName();
        try {
            Object obj2 = getObjectByPk(qualifiedTableName, (Serializable) obj);
            session.delete(actualEntityName, obj2);
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    public List<Object[]> runSql(String databaseName, String sql) {
        HibernateDatabaseSetup setup = setups.get(databaseName);
        if (setup == null) {
            throw new Error("No setup exists for database: " + databaseName);
        }
        Session session = setup.getThreadSession();
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(sql);
        final String formatString = sqlFormat.getFormatString();
        final Object[] parameters = sqlFormat.evaluateOgnlExpressions(null);

        /*SQLQuery query = session.createSQLQuery(formatString);
        for (int i = 0; i < parameters.length; i++) {
            query.setParameter(i, parameters[i]);
        }*/

        //noinspection unchecked
        //List<Object[]> result = query.list();

        final List<Object[]> result = new ArrayList<Object[]>();

        session.doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                PreparedStatement stmt = connection.prepareStatement(formatString);
                for (int i = 0; i < parameters.length; i++) {
                    stmt.setObject(i + 1, parameters[i]);
                }
                ResultSet rs = stmt.executeQuery();
                ResultSetMetaData md = rs.getMetaData();
                int cc = md.getColumnCount();
                while(rs.next()) {
                    Object[] current = new Object[cc];
                    for(int i = 0; i < cc; i++) {
                        current[i] = rs.getObject(i + 1);
                    }
                    result.add(current);
                }
                rs.close();
                stmt.close();
            }
        });

        return result;
    }

    public void closeSessions() {
        for (HibernateDatabaseSetup current : setups.values()) {
            Session session = current.getThreadSession(false);
            if (session != null) {
                try {
                    session.close();
                } catch (Throwable e) {
                    logger.warn(ExceptionUtils.getRootCauseMessage(e), e);
                }
            }
            current.removeThreadSession();
        }
    }

    public void commit(String databaseName) {
        Session session = setups.get(databaseName).getThreadSession();
        try {
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    public void rollback(String databaseName) {
        Session session = setups.get(databaseName).getThreadSession();
        session.getTransaction().rollback();
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
                        tx.rollback();
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

    @SuppressWarnings({"unchecked"})
    public List<Object> getRelatedObjects(String qualifiedTableName,
                                          Object obj,
                                          String oneToManyRelationshipName) {
        ForeignKey relationship =
                DataModelLogic.findOneToManyRelationship(model,
                        qualifiedTableName, oneToManyRelationshipName);
        //Table toTable = relationship.getActualToTable();
        Table fromTable = relationship.getFromTable();
        Session session = getSession(fromTable.getQualifiedName());

        ClassAccessor toAccessor = getTableAccessor(qualifiedTableName);

        try {
            org.hibernate.Criteria criteria =
                    session.createCriteria(fromTable.getActualEntityName());
            for (Reference reference : relationship.getReferences()) {
                Column fromColumn = reference.getActualFromColumn();
                Column toColumn = reference.getActualToColumn();
                PropertyAccessor toPropertyAccessor
                        = toAccessor.getProperty(toColumn.getActualPropertyName());
                Object toValue = toPropertyAccessor.get(obj);
                criteria.add(Restrictions.eq(fromColumn.getActualPropertyName(),
                        toValue));
            }
            //noinspection unchecked
            List<Object> result = criteria.list();
            return result;
        } catch (Throwable e) {
            String msg = String.format(
                    "Cannot access relationship %s on table %s",
                    oneToManyRelationshipName, qualifiedTableName);
            logger.warn(msg, e);
        }
        return null;
    }

    //**************************************************************************
    // DDL
    //**************************************************************************

    public List<String> getDDLCreate() {
        List<String> result = new ArrayList<String>();
        for (Database db : model.getDatabases()) {
            result.add("-- DB: " + db.getDatabaseName());
            HibernateDatabaseSetup setup = setups.get(db.getDatabaseName());
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
            HibernateDatabaseSetup setup = setups.get(db.getDatabaseName());
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

    public @NotNull CrudAccessor getCrudAccessor(@NotNull Crud crud) {
        String qualifiedTableName = crud.getTable();
        TableAccessor tableAccessor = getTableAccessor(qualifiedTableName);
        return new CrudAccessor(crud, tableAccessor);
    }

    //**************************************************************************
    // User
    //**************************************************************************
    public User login(String username, String password) {
        String qualifiedTableName = PORTOFINO_PUBLIC_USERS;
        Session session = getSession(qualifiedTableName);
        org.hibernate.Criteria criteria = session.createCriteria("portofino_public_users");
        criteria.add(Restrictions.eq(SessionAttributes.USER_NAME, username));
        criteria.add(Restrictions.eq(UserUtils.PASSWORD, password));

        @SuppressWarnings({"unchecked"})
        List<Object> result = (List<Object>) criteria.list();

        if (result.size() == 1) {
            return (User) result.get(0);
        } else {
            return null;
        }
    }

    public User findUserByEmail(String email) {
        String qualifiedTableName = PORTOFINO_PUBLIC_USERS;
        Session session = getSession(qualifiedTableName);
        org.hibernate.Criteria criteria = session.createCriteria("portofino_public_users");
        criteria.add(Restrictions.eq("email", email));
        @SuppressWarnings({"unchecked"})
        List<Object> result = (List<Object>) criteria.list();

        if (result.size() == 1) {
            return (User) result.get(0);
        } else {
            return null;
        }
    }

    public User findUserByUserName(String username) {
        String qualifiedTableName = PORTOFINO_PUBLIC_USERS;
        Session session = getSession(qualifiedTableName);
        org.hibernate.Criteria criteria = session.createCriteria("portofino_public_users");
        criteria.add(Restrictions.eq(SessionAttributes.USER_NAME, username));
        @SuppressWarnings({"unchecked"})
        List<Object> result = (List<Object>) criteria.list();

        if (result.size() == 1) {
            return (User) result.get(0);
        } else {
            return null;
        }
    }

    public User findUserByToken(String token) {
        String qualifiedTableName = PORTOFINO_PUBLIC_USERS;
        Session session = getSession(qualifiedTableName);
        org.hibernate.Criteria criteria = session.createCriteria("portofino_public_users");
        criteria.add(Restrictions.eq("token", token));
        @SuppressWarnings({"unchecked"})
        List<Object> result = (List<Object>) criteria.list();

        if (result.size() == 1) {
            return (User) result.get(0);
        } else {
            return null;
        }
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
        TableAccessor table = getTableAccessor(UserUtils.GROUPTABLE);
        assert table != null;

        String actualEntityName = table.getTable().getActualEntityName();
        List result = runHqlQuery
                (UserUtils.GROUPTABLE,
                "FROM " + actualEntityName + " WHERE name = ?",
                new Object[] { name });
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
}
