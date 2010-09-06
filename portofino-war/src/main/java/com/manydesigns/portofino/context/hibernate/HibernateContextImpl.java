/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.context.hibernate;

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.fields.search.TextMatchMode;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.reflection.helpers.ClassAccessorManager;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.database.ConnectionProvider;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.io.ConnectionsParser;
import com.manydesigns.portofino.model.io.ModelParser;
import com.manydesigns.portofino.model.site.SiteNode;
import com.manydesigns.portofino.reflection.ColumnAccessor;
import com.manydesigns.portofino.users.User;
import org.apache.commons.lang.time.StopWatch;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Settings;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.Dialect;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import java.io.Serializable;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class HibernateContextImpl implements Context {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected List<ConnectionProvider> connectionProviders;
    protected Model model;
    protected Map<String, HibernateDatabaseSetup> setups;
    protected final ThreadLocal<StopWatch> stopWatches;
    protected final ThreadLocal<User> threadUsers;
    protected final List<SiteNode> siteNodes;

    public static final Logger logger =
            LogUtil.getLogger(HibernateContextImpl.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public HibernateContextImpl() {
        stopWatches = new ThreadLocal<StopWatch>();
        siteNodes = new ArrayList<SiteNode>();
        threadUsers = new ThreadLocal<User>();
    }

    //**************************************************************************
    // Model loading
    //**************************************************************************

    public void loadConnectionsAsResource(String resource) {
        LogUtil.entering(logger, "loadConnectionsAsResource", resource);

        ConnectionsParser parser = new ConnectionsParser();
        try {
            connectionProviders = parser.parse(resource);
            for (ConnectionProvider current : connectionProviders) {
                current.test();
            }
        } catch (Exception e) {
            LogUtil.severeMF(logger, "Cannot load/parse connection: {0}", e,
                    resource);
        }

        LogUtil.exiting(logger, "loadConnectionsAsResource");
    }

    public void loadXmlModelAsResource(String resource) {
        LogUtil.entering(logger, "loadXmlModelAsResource", resource);

        ModelParser parser = new ModelParser();
        try {
            Model loadedModel = parser.parse(resource);
            installDataModel(loadedModel);
        } catch (Exception e) {
            LogUtil.severeMF(logger, "Cannot load/parse model: {0}", e,
                    resource);
        }

        LogUtil.exiting(logger, "loadXmlModelAsResource");
    }

    private synchronized void installDataModel(Model newModel) {
        try {
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
                    Settings settings = sessionFactory.getSettings();

                    HibernateDatabaseSetup setup =
                            new HibernateDatabaseSetup(
                                    configuration, sessionFactory);
                    newSetups.put(databaseName, setup);
                }
            }
            setups = newSetups;
            model = newModel;
        } catch (Exception e) {
            LogUtil.severe(logger, "Cannot install model", e);
        }
    }

    //**************************************************************************
    // Database stuff
    //**************************************************************************

    public ConnectionProvider getConnectionProvider(String databaseName) {
        for (ConnectionProvider current : connectionProviders) {
            if (current.getDatabaseName().equals(databaseName)) {
                return current;
            }
        }
        return null;
    }

    //**************************************************************************
    // Modell access
    //**************************************************************************

    public List<ConnectionProvider> getConnectionProviders() {
        return connectionProviders;
    }

    public Model getModel() {
        return model;
    }

    public void syncDataModel() {
        Model syncModel = new Model();
        for (ConnectionProvider current : connectionProviders) {
            Database syncDatabase = current.readModel();
            syncModel.getDatabases().add(syncDatabase);
        }
        installDataModel(syncModel);
    }

    //**************************************************************************
    // Persistance
    //**************************************************************************

    public Object getObjectByPk(String qualifiedTableName,
                                Serializable pk) {
        Session session = getSession(qualifiedTableName);
        Table table = model.findTableByQualifiedName(qualifiedTableName);

        int size = table.getPrimaryKey().getColumns().size();
        if (pk instanceof  Map){
            HashMap<String, Object> key = (HashMap<String, Object>) pk;
            if (size > 1) {
                startTimer();
                @SuppressWarnings({"unchecked"}) Map<String, Object> result =
                        (Map<String, Object>)
                                session.load(qualifiedTableName, key);
                stopTimer();
                return result;
            } else {
                startTimer();
                for (Map.Entry entry : key.entrySet()) {
                    if (((String) entry.getKey()).startsWith("$")) {
                        continue;
                    }
                    @SuppressWarnings({"unchecked"}) Map<String, Object> result =
                            (Map<String, Object>) session.load(qualifiedTableName,
                                    (Serializable) entry.getValue());
                    stopTimer();
                    return result;
                }
            }
        } else {
            @SuppressWarnings({"unchecked"}) Object result;
            startTimer();
            result=null;
            if (size>1) {
                result = session.load(qualifiedTableName, pk);
            } else {
                String propertyName =
                        table.getPrimaryKey().getColumns().get(0).getPropertyName();
                ClassAccessor accessor = ClassAccessorManager.getManager()
                    .tryToInstantiateFromClass(table);
                try {
                    Serializable key = (Serializable) accessor.getProperty(propertyName).get(pk);
                    result = session.load(qualifiedTableName, key);
                } catch (Throwable e) {
                   LogUtil.warningMF(logger,
                        "Cannot invoke property accessor for {0} on class {1}",
                        e, propertyName, table.getClassName());
                }
            }
            stopTimer();
            return result;

        }
        return null;
    }


    public List<Object> getAllObjects(String qualifiedTableName) {
        Session session = getSession(qualifiedTableName);

        org.hibernate.Criteria hibernateCriteria;
        Table table = model.findTableByQualifiedName(qualifiedTableName);

        if (table.getClassName()==null) {
            hibernateCriteria = session.createCriteria(qualifiedTableName);
        } else {
            hibernateCriteria = session.createCriteria
                    (ReflectionUtil.loadClass(table.getClassName()));
        }

        startTimer();
        //noinspection unchecked
        List<Object> result = hibernateCriteria.list();
        stopTimer();
        return result;
    }

    protected Session getSession(String qualifiedTableName) {
        Table table = model.findTableByQualifiedName(qualifiedTableName);
        String databaseName = table.getDatabaseName();
        return setups.get(databaseName).getThreadSession();
    }

    public Query getQueryForCriteria(Criteria criteria) {
        ArrayList<Object> parameters = new ArrayList<Object>();
        StringBuilder sb = new StringBuilder();
        for (Criteria.Criterion criterion : criteria) {
            ColumnAccessor accessor =
                    (ColumnAccessor)criterion.getPropertyAccessor();
            String hqlFormat;
            if (criterion instanceof Criteria.EqCriterion) {
                Criteria.EqCriterion eqCriterion =
                        (Criteria.EqCriterion)criterion;
                Object value = eqCriterion.getValue();
                hqlFormat = "{0} = ?";
            } else if (criterion instanceof Criteria.NeCriterion) {
                Criteria.NeCriterion neCriterion =
                        (Criteria.NeCriterion)criterion;
                Object value = neCriterion.getValue();
                hqlFormat = "{0} <> ?";
            } else if (criterion instanceof Criteria.BetweenCriterion) {
                Criteria.BetweenCriterion betweenCriterion =
                        (Criteria.BetweenCriterion)criterion;
                Object min = betweenCriterion.getMin();
                Object max = betweenCriterion.getMax();
                hqlFormat = "{0} >= ? AND < {0} <= ?";
            } else if (criterion instanceof Criteria.GtCriterion) {
                Criteria.GtCriterion gtCriterion =
                        (Criteria.GtCriterion)criterion;
                Object value = gtCriterion.getValue();
                hqlFormat = "{0} > ?";
            } else if (criterion instanceof Criteria.GeCriterion) {
                Criteria.GeCriterion gtCriterion =
                        (Criteria.GeCriterion)criterion;
                Object value = gtCriterion.getValue();
                hqlFormat = "{0} >= ?";
            } else if (criterion instanceof Criteria.LtCriterion) {
                Criteria.LtCriterion ltCriterion =
                        (Criteria.LtCriterion)criterion;
                Object value = ltCriterion.getValue();
                hqlFormat = "{0} < ?";
            } else if (criterion instanceof Criteria.LeCriterion) {
                Criteria.LeCriterion leCriterion =
                        (Criteria.LeCriterion)criterion;
                Object value = leCriterion.getValue();
                hqlFormat = "{0} <= ?";
            } else if (criterion instanceof Criteria.LikeCriterion) {
                Criteria.LikeCriterion likeCriterion =
                        (Criteria.LikeCriterion)criterion;
                Object value = likeCriterion.getValue();
                TextMatchMode textMatchMode = likeCriterion.getTextMatchMode();
                hqlFormat = "{0} like ?";
            } else if (criterion instanceof Criteria.IlikeCriterion) {
                Criteria.IlikeCriterion ilikeCriterion =
                        (Criteria.IlikeCriterion)criterion;
                Object value = ilikeCriterion.getValue();
                TextMatchMode textMatchMode = ilikeCriterion.getTextMatchMode();
                hqlFormat = "lower({0}) like lower(?)";
            } else {
                LogUtil.severeMF(logger, "Unrecognized criterion: ", criterion);
                throw new InternalError("Unrecognied criterion");
            }

            String hql = MessageFormat.format(hqlFormat,
                    accessor.getColumn().getColumnName());

            if (sb.length() > 0) {
                sb.append(" AND ");
            }
            sb.append(hql);
        }
    }


    public List<Object> getObjects(Criteria criteria) {
        Query query = getQueryForCriteria(criteria);

        startTimer();
        //noinspection unchecked
        List<Object> result = query.list();
        stopTimer();
        return result;
    }

    public List<Object> getObjects(String qualifiedTableName, String query) {
        Session session = getSession(qualifiedTableName);

        startTimer();
        //noinspection unchecked
        List<Object> result = session.createQuery(query).list();
        stopTimer();
        return result;
    }

    public List<Object> getObjects(String queryString, Criteria criteria) {
        SessionFactoryImpl factory =
                (SessionFactoryImpl) session.getSessionFactory();
        String alias = "product";


        StringBuilder sb = new StringBuilder();
        sb.append(queryString);
        if (criteriaWhereCondition.length() > 0) {
            sb.append(" AND ");
            sb.append(criteriaWhereCondition);
        }
        String fullQueryString = sb.toString();
        Query query = session.createQuery(fullQueryString);
        query.setParameters(
                criteriaQueryParameters.getPositionalParameterValues(),
                criteriaQueryParameters.getPositionalParameterTypes());

        startTimer();
        //noinspection unchecked
        List<Object> result = query.list();
        stopTimer();
        return result;
    }

    public void saveOrUpdateObject(String qualifiedTableName, Object obj) {
        Session session = getSession(qualifiedTableName);

        try {
            startTimer();
            session.beginTransaction();
            session.saveOrUpdate(qualifiedTableName, obj);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }finally {
            stopTimer();
        }
    }

    public void saveObject(String qualifiedTableName, Object obj) {
        Session session = getSession(qualifiedTableName);
        session.beginTransaction();

        try {
            startTimer();
            session.save(qualifiedTableName, obj);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            stopTimer();
        }
    }


    public void updateObject(String qualifiedTableName, Object obj) {
        Session session = getSession(qualifiedTableName);
        session.beginTransaction();
        try {
            startTimer();
            session.update(qualifiedTableName, obj);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }finally {
            stopTimer();
        }
    }

    public void deleteObject(String qualifiedTableName, Object obj) {
        Session session = getSession(qualifiedTableName);
        session.beginTransaction();
        try {
            startTimer();
            session.delete(qualifiedTableName, obj);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }finally{
            stopTimer();
        }

    }

    public List<Object[]> runSql(String databaseName, String sql) {
        Session session = setups.get(databaseName).getThreadSession();
        startTimer();
        //noinspection unchecked
        List<Object[]> result = session.createSQLQuery(sql).list();
        stopTimer();
        return result;
    }

    public void openSession() {
        for (HibernateDatabaseSetup current : setups.values()) {
            SessionFactory sessionFactory = current.getSessionFactory();
            Session session = sessionFactory.openSession();
            current.setThreadSession(session);
        }
    }


    public void closeSession() {
        for (HibernateDatabaseSetup current : setups.values()) {
            Session session = current.getThreadSession();
            if (session != null) {
                session.close();
            }
            current.setThreadSession(null);
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<Object> getRelatedObjects(String qualifiedTableName,
                                          Object obj,
                                          String oneToManyRelationshipName) {
        Relationship relationship =
                model.findOneToManyRelationship(
                        qualifiedTableName, oneToManyRelationshipName);
        Table fromTable = relationship.getFromTable();

        Class clazz = obj.getClass();


        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            if (map.get(oneToManyRelationshipName) instanceof List) {
                return (List<Object>)
                        map.get(oneToManyRelationshipName);
            }


            Session session =
                    setups.get(fromTable.getDatabaseName()).getThreadSession();
            org.hibernate.Criteria criteria =
                    session.createCriteria(fromTable.getQualifiedName());
            for (Reference reference : relationship.getReferences()) {
                Column fromColumn = reference.getFromColumn();
                Column toColumn = reference.getToColumn();
                criteria.add(Restrictions.eq(fromColumn.getColumnName(),
                        map.get(toColumn.getColumnName())));
            }
            startTimer();
            //noinspection unchecked
            List<Object> result = criteria.list();
            stopTimer();
            return result;
        } else {
            JavaClassAccessor classAccessor = new JavaClassAccessor(clazz);
                String propertyName = relationship.getManyPropertyName();
            try {

                PropertyAccessor propertyAccessor
                        = classAccessor.getProperty(propertyName);
                Object list = propertyAccessor.get(obj);

                if (list instanceof List) {
                    return (List<Object>) list;
                }
                Session session =
                        setups.get(fromTable.getDatabaseName()).getThreadSession();
                org.hibernate.Criteria criteria =
                        session.createCriteria(fromTable.getQualifiedName());
                for (Reference reference : relationship.getReferences()) {
                    Column fromColumn = reference.getFromColumn();
                    Column toColumn = reference.getToColumn();
                    PropertyAccessor propertyAccessor2
                        = classAccessor.getProperty(toColumn.getPropertyName());
                    Object critObj = propertyAccessor2.get(obj);

                    criteria.add(Restrictions.eq(fromColumn.getColumnName(),
                            critObj));
                }
                startTimer();
                //noinspection unchecked
                List<Object> result = criteria.list();
                stopTimer();
                return result;
            } catch (Throwable e) {
                LogUtil.warningMF(logger,
                        "Cannot invoke property accessor for {0} on class {1}",
                        e, propertyName, clazz.getName());
            }
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
                //TODO
            } finally {
                provider.releaseConnection(conn);
            }

        }
        return result;
    }

    //**************************************************************************
    // User
    //**************************************************************************
    public User authenticate(String email, String password) {
        String qualifiedTableName = "portofino.public.user_";
        Session session = getSession(qualifiedTableName);
        org.hibernate.Criteria criteria = session.createCriteria(qualifiedTableName);
        criteria.add(Restrictions.eq("emailaddress", email));
        criteria.add(Restrictions.eq("pwd", password));
        startTimer();

        List<Object> result = criteria.list();
        stopTimer();

        if (result.size() == 1) {
            User authUser = (User) result.get(0);
            setCurrentUser(authUser);
            return authUser;
        } else {
            return null;
        }
    }

    public User getCurrentUser() {
        return threadUsers.get();
    }

    public void setCurrentUser(User user) {
        threadUsers.set(user);
    }


    //**************************************************************************
    // Timers
    //**************************************************************************

    public void resetDbTimer() {
        stopWatches.set(null);
    }

    public long getDbTime() {
        StopWatch stopWatch = stopWatches.get();
        if (stopWatch != null) {
            return stopWatch.getTime();
        }
        return 0L;
    }

    private void startTimer() {
        StopWatch stopWatch = stopWatches.get();
        if (stopWatch == null) {
            stopWatch = new StopWatch();
            stopWatches.set(stopWatch);
            stopWatch.start();
        } else {
            stopWatch.resume();           
        }
    }

    private void stopTimer() {
        StopWatch stopWatch = stopWatches.get();
        if (stopWatch != null) {
            stopWatch.suspend();
        }
    }

}
