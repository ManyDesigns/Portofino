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

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.fields.search.Criterion;
import com.manydesigns.elements.fields.search.Ordering;
import com.manydesigns.elements.fields.search.TextMatchMode;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlHqlFormat;
import com.manydesigns.elements.text.OgnlSqlFormat;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.database.model.*;
import com.manydesigns.portofino.reflection.TableAccessor;
import jakarta.persistence.criteria.*;
import liquibase.repackaged.net.sf.jsqlparser.statement.select.PlainSelect;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.grammars.hql.HqlLexer;
import org.hibernate.grammars.hql.HqlParser;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;
import org.hibernate.query.hql.internal.HqlParseTreeBuilder;
import org.hibernate.query.hql.internal.SemanticQueryBuilder;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.SortOrder;
import org.hibernate.query.sqm.SqmExpressible;
import org.hibernate.query.sqm.SqmQuerySource;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.from.SqmFromClause;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.hibernate.query.sqm.tree.select.SqmSortSpecification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for running queries (SQL and HQL) against the database. Provides methods for many common cases,
 * but you can always use the Hibernate {@link Session} directly.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class QueryUtils {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected static final String WHERE_STRING = " WHERE ";
    protected static final Pattern FROM_PATTERN =
            Pattern.compile("(SELECT\\s+.*\\s+)?FROM\\s+([a-z_$\\u0080-\\ufffe]{1}[a-z_$0-9\\u0080-\\ufffe]*).*",
                            Pattern.CASE_INSENSITIVE | Pattern.DOTALL); //. (dot) matches newlines

    protected static final Logger logger = LoggerFactory.getLogger(QueryUtils.class);

    /**
     * Runs a SQL query against a session. The query is processed with an {@link OgnlSqlFormat}, so it can
     * access values from the OGNL context.
     * @param session the session
     * @param sql the query string
     * @return the results of the query as an Object[] (an array cell per column)
     */
    public static List<Object[]> runSql(Session session, String sql) {
        OgnlHqlFormat hqlFormat = OgnlHqlFormat.create(sql);
        String formatString = hqlFormat.getFormatString();
        Object[] parameters = hqlFormat.evaluateOgnlExpressions(null);
        return runSql(session, formatString, parameters);
    }

    /**
     * Runs a SQL query against a session. The query can contain placeholders for the parameters, as supported
     * by {@link PreparedStatement}.
     * @param session the session
     * @param queryString the query
     * @param parameters parameters to substitute in the query
     * @return the results of the query as an Object[] (an array cell per column)
     */
    public static List<Object[]> runSql(Session session, final String queryString, final Object[] parameters) {
        final List<Object[]> result = new ArrayList<Object[]>();

        try {
            session.doWork(new Work() {
                public void execute(Connection connection) throws SQLException {
                    try (PreparedStatement stmt = connection.prepareStatement(queryString)) {
                        for (int i = 0; i < parameters.length; i++) {
                            stmt.setObject(i + 1, parameters[i]);
                        }
                        try(ResultSet rs = stmt.executeQuery()) {
                            ResultSetMetaData md = rs.getMetaData();
                            int cc = md.getColumnCount();
                            while (rs.next()) {
                                Object[] current = new Object[cc];
                                for (int i = 0; i < cc; i++) {
                                    current[i] = rs.getObject(i + 1);
                                }
                                result.add(current);
                            }
                        }
                    }
                }
            });
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            session.beginTransaction();
            throw e;
        }

        return result;
    }

    /**
     * Runs a query, expressed as {@link Table} with {@link Criteria}, against the database.
     * @param session the session
     * @param criteria the search criteria
     * @param firstResult index of the first result to return
     * @param maxResults maximum number of results to return
     * @return at most <code>maxResults</code> results from the query
     */
    public static List<Object> getObjects(
            Session session, Table table, Criteria criteria,
            @Nullable Integer firstResult, @Nullable Integer maxResults) {
        QueryStringWithParameters queryStringWithParameters =
                getQueryStringWithParametersForCriteria(table, criteria);

        return runHqlQuery(
                session,
                queryStringWithParameters.getQueryString(),
                queryStringWithParameters.getParameters(),
                firstResult,
                maxResults
        );
    }

    /**
     * Runs a query against the database. The query is processed with an {@link OgnlSqlFormat}, so it can
     * access values from the OGNL context, as well as from an (optional) rootFactory object.
     * @param session the session
     * @param queryString the query
     * @param rootObject the rootFactory object passed to the ognl evaluator (can be null).
     * @param firstResult index of the first result to return
     * @param maxResults maximum number of results to return
     * @return at most <code>maxResults</code> results from the query
     */
    public static List<Object> getObjects(
            Session session, String queryString,
            Object rootObject,
            @Nullable Integer firstResult, @Nullable Integer maxResults) {
        OgnlHqlFormat hqlFormat = OgnlHqlFormat.create(queryString);
        String formatString = hqlFormat.getFormatString();
        Object[] parameters = hqlFormat.evaluateOgnlExpressions(rootObject);

        return runHqlQuery(session, formatString, parameters, firstResult, maxResults);
    }

    /**
     * Runs a query against the database. The query is processed with an {@link OgnlSqlFormat}, so it can
     * access values from the OGNL context.
     * @param session the session
     * @param queryString the query
     * @param firstResult index of the first result to return
     * @param maxResults maximum number of results to return
     * @return at most <code>maxResults</code> results from the query
     */
    public static List<Object> getObjects(
            Session session, String queryString,
            @Nullable Integer firstResult, @Nullable Integer maxResults) {
        return getObjects(session, queryString, null, null, null, null, firstResult, maxResults);
    }

    /**
     * Transforms a {@link Table} with {@link Criteria} to a query string with an associated array of parameters.
     * @param table the table.
     * @param criteria the criteria.
     * @return the same criteria encoded as a HQL query with parameters.
     */
    public static QueryStringWithParameters getQueryStringWithParametersForCriteria(Table table, Criteria criteria) {
        return getQueryStringWithParametersForCriteria(table, criteria, null, 1);
    }

    /**
     * Transforms a {@link Table} with {@link Criteria} to a query string with an associated array of parameters.
     * @param table the table.
     * @param criteria the criteria.
     * @param alias the alias to use for the main entity.
     * @return the same criteria encoded as a HQL query with parameters.
     */
    public static QueryStringWithParameters getQueryStringWithParametersForCriteria(
            Table table, @Nullable Criteria criteria, @Nullable String alias, int initialParameterIndex) {
        if (criteria == null) {
            return new QueryStringWithParameters("", new Object[0]);
        }
        ArrayList<Object> parametersList = new ArrayList<>();
        StringBuilder whereBuilder = new StringBuilder();
        for (Criterion criterion : criteria) {
            PropertyAccessor accessor = criterion.getPropertyAccessor();
            String hqlFormat;
            if (criterion instanceof Criteria.EqCriterion) {
                Criteria.EqCriterion eqCriterion = (Criteria.EqCriterion) criterion;
                Object value = eqCriterion.getValue();
                hqlFormat = "{0} = :p" + (parametersList.size() + initialParameterIndex);
                parametersList.add(value);
            } else if (criterion instanceof Criteria.InCriterion) {
                Criteria.InCriterion inCriterion = (Criteria.InCriterion) criterion;
                Object[] values = inCriterion.getValues();
                StringBuilder params = new StringBuilder();
                if (values != null){
                    boolean first = true;
                    for (Object value : values) {
                        if (!first){
                            params.append(", :p").append(parametersList.size() + initialParameterIndex);
                        } else {
                            params.append(":p").append(parametersList.size() + initialParameterIndex);
                            first = false;
                        }
                        parametersList.add(value);
                    }
                    hqlFormat = "{0} in (" + params + ")";
                } else {
                    hqlFormat = null;
                }
            } else if (criterion instanceof Criteria.NeCriterion) {
                Criteria.NeCriterion neCriterion = (Criteria.NeCriterion) criterion;
                Object value = neCriterion.getValue();
                hqlFormat = "{0} <> :p" + (parametersList.size() + initialParameterIndex);
                parametersList.add(value);
            } else if (criterion instanceof Criteria.BetweenCriterion) {
                Criteria.BetweenCriterion betweenCriterion =
                        (Criteria.BetweenCriterion) criterion;
                Object min = betweenCriterion.getMin();
                Object max = betweenCriterion.getMax();
                hqlFormat =
                        "{0} >= :p" + (parametersList.size() + initialParameterIndex) +
                        " AND {0} <= :p" + (parametersList.size() + initialParameterIndex + 1);
                parametersList.add(min);
                parametersList.add(max);
            } else if (criterion instanceof Criteria.GtCriterion) {
                Criteria.GtCriterion gtCriterion =
                        (Criteria.GtCriterion) criterion;
                Object value = gtCriterion.getValue();
                hqlFormat = "{0} > :p" + (parametersList.size() + initialParameterIndex);
                parametersList.add(value);
            } else if (criterion instanceof Criteria.GeCriterion) {
                Criteria.GeCriterion gtCriterion =
                        (Criteria.GeCriterion) criterion;
                Object value = gtCriterion.getValue();
                hqlFormat = "{0} >= :p" + (parametersList.size() + initialParameterIndex);
                parametersList.add(value);
            } else if (criterion instanceof Criteria.LtCriterion) {
                Criteria.LtCriterion ltCriterion =
                        (Criteria.LtCriterion) criterion;
                Object value = ltCriterion.getValue();
                hqlFormat = "{0} < :p" + (parametersList.size() + initialParameterIndex);
                parametersList.add(value);
            } else if (criterion instanceof Criteria.LeCriterion) {
                Criteria.LeCriterion leCriterion =
                        (Criteria.LeCriterion) criterion;
                Object value = leCriterion.getValue();
                hqlFormat = "{0} <= :p" + (parametersList.size() + initialParameterIndex);
                parametersList.add(value);
            } else if (criterion instanceof Criteria.LikeCriterion) {
                Criteria.LikeCriterion likeCriterion =
                        (Criteria.LikeCriterion) criterion;
                String value = (String) likeCriterion.getValue();
                if(likeCriterion.getTextMatchMode() == TextMatchMode.EQUALS) {
                    hqlFormat = "{0} = :p" + (parametersList.size() + initialParameterIndex);
                    parametersList.add(value);
                } else {
                    String pattern = processTextMatchMode(likeCriterion.getTextMatchMode(), value);
                    hqlFormat = "{0} like :p" + (parametersList.size() + initialParameterIndex);
                    parametersList.add(pattern);
                }
            } else if (criterion instanceof Criteria.IlikeCriterion) {
                Criteria.IlikeCriterion ilikeCriterion =
                        (Criteria.IlikeCriterion) criterion;
                String value = (String) ilikeCriterion.getValue();
                if(ilikeCriterion.getTextMatchMode() == TextMatchMode.EQUALS) {
                    hqlFormat = "lower({0}) = lower(:p" + (parametersList.size() + initialParameterIndex + ")");
                    parametersList.add(value);
                } else {
                    String pattern = processTextMatchMode(ilikeCriterion.getTextMatchMode(), value);
                    hqlFormat = "lower({0}) like lower(:p" + (parametersList.size() + initialParameterIndex) + ")";
                    parametersList.add(pattern);
                }
            } else if (criterion instanceof Criteria.IsNullCriterion) {
                hqlFormat = "{0} is null";
            } else if (criterion instanceof Criteria.IsNotNullCriterion) {
                hqlFormat = "{0} is not null";
            } else {
                logger.error("Unrecognized criterion: {}", criterion);
                throw new InternalError("Unrecognied criterion");
            }

            if (hqlFormat == null) {
                continue;
            }

            String accessorName = accessor.getName();
            if(alias != null) {
                accessorName = alias + "." + accessorName;
            }
            String hql = MessageFormat.format(hqlFormat, accessorName);

            if (whereBuilder.length() > 0) {
                whereBuilder.append(" AND ");
            }
            whereBuilder.append(hql);
        }
        String whereClause = whereBuilder.toString();
        String queryString;
        String actualEntityName = table.getActualEntityName();
        if(alias != null) {
            actualEntityName += " " + alias;
        }
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

    protected static String processTextMatchMode(TextMatchMode textMatchMode,
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

    /**
     * Extracts the name of the main entity from a HQL query string, i.e. the first entity in the
     * from clause.
     * @param database the database containing the table.
     * @param queryString the query to analyze.
     * @return the main entity selected by the query
     */
    public static Table getTableFromQueryString(Database database, String queryString) {
        Matcher matcher = FROM_PATTERN.matcher(queryString);
        String entityName;
        if (matcher.matches()) {
            entityName =  matcher.group(2);
        } else {
            return null;
        }

        Table table = DatabaseLogic.findTableByEntityName(database, entityName);
        return table;
    }

     /**
      * Runs a query against the database. The query is expressed as a {@link Criteria} object plus a
      * query string to be merged with it (the typical case of a search in a crud defined by a query).
      * The query string is processed with an {@link OgnlSqlFormat}, so it can access values from the OGNL context,
      * as well as from an (optional) rootFactory object.
      * @param session the session
      * @param queryString the query
      * @param criteria the search criteria to merge with the query.
      * @param rootObject the rootFactory object passed to the ognl evaluator (can be null).
      * @param firstResult index of the first result to return
      * @param maxResults maximum number of results to return
      * @return at most <code>maxResults</code> results from the query
     */
    public static List<Object> getObjects(
            Session session,
            String queryString,
            @Nullable Table table,
            @Nullable Criteria criteria,
            @Nullable Ordering ordering,
            @Nullable Object rootObject,
            @Nullable Integer firstResult,
            @Nullable Integer maxResults) {
        QueryStringWithParameters result = mergeQuery(session, queryString, table, criteria, ordering, rootObject);

        return runHqlQuery(session, result.getQueryString(), result.getParameters(), firstResult, maxResults);
    }

    /**
     * Merges a HQL query string with a {@link Criteria} object representing a search. The query string
     * is processed with an {@link OgnlSqlFormat}, so it can access values from the OGNL context, as well as
     * from an (optional) rootFactory object.
     * @param queryString the base query
     * @param criteria the criteria to merge with the query
     * @param rootObject the OGNL rootFactory object (can be null)
     * @return the merged query
     */
    public static QueryStringWithParameters mergeQuery
            (Session session, String queryString,
             @Nullable Table table, @Nullable Criteria criteria, @Nullable Ordering ordering,
             Object rootObject) {
        OgnlHqlFormat hqlFormat = OgnlHqlFormat.create(queryString);
        String formatString = hqlFormat.getFormatString();
        Object[] parameters = hqlFormat.evaluateOgnlExpressions(rootObject);

        SqmSelectStatement<Object> parsedQuery;
        SqmSelectStatement<Object> parsedCriteriaQuery;
        parsedQuery = parseQuery(session, formatString);

        String mainEntityAlias = null;
        if(table != null) {
            mainEntityAlias = getEntityAlias(table.getActualEntityName(), parsedQuery);
        }

        QueryStringWithParameters criteriaQuery =
                getQueryStringWithParametersForCriteria(table, criteria, mainEntityAlias,parameters.length + 1);
        String criteriaQueryString = criteriaQuery.getQueryString();
        Object[] criteriaParameters = criteriaQuery.getParameters();

        if(StringUtils.isEmpty(criteriaQueryString)) {
            parsedCriteriaQuery = new SqmSelectStatement<>(getQueryEngine(session).getCriteriaBuilder());
        } else {
            parsedCriteriaQuery = parseQuery(session, criteriaQueryString);
        }

        SqmPredicate[] whereExpression;
        if(parsedQuery.getRestriction() != null) {
            if(parsedCriteriaQuery.getRestriction() != null) {
                whereExpression = new SqmPredicate[] {
                        parsedQuery.getRestriction(),
                        parsedCriteriaQuery.getRestriction()
                };
            } else {
                whereExpression = new SqmPredicate[] { parsedQuery.getRestriction() };
            }
        } else {
            whereExpression = new SqmPredicate[] { parsedCriteriaQuery.getRestriction() };
        }
        parsedQuery.where(whereExpression);
        if(ordering != null) {
            List<Order> orderByElements = new ArrayList<>();
            String propertyName = ordering.getPropertyAccessor().getName();
            if(mainEntityAlias != null) {
                propertyName = mainEntityAlias + "." + propertyName;
            }
            Order order = new SqmSortSpecification(
                    null, //TODO
                    ordering.isAsc() ? SortOrder.ASCENDING : SortOrder.DESCENDING);
            // orderByElements.add(order);
            if(parsedQuery.getOrderList() != null) {
                for(Order toAdd : parsedQuery.getOrderList()) {
                    /* TODO if(toAdd.getExpression() instanceof net.sf.jsqlparser.schema.Column) {
                        net.sf.jsqlparser.schema.Column column = (net.sf.jsqlparser.schema.Column) toAdd.getExpression();
                        if(StringUtils.isEmpty(column.getTable().getName()) && propertyName.equals(column.getColumnName())) {
                            continue; //do not add
                        }
                    }*/
                    orderByElements.add(toAdd);
                }
            }
            parsedQuery.orderBy(orderByElements);
        }
        String fullQueryString = parsedQuery.toHqlString();

        // merge the parameters
        ArrayList<Object> mergedParametersList = new ArrayList<Object>();
        mergedParametersList.addAll(Arrays.asList(parameters));
        mergedParametersList.addAll(Arrays.asList(criteriaParameters));
        Object[] mergedParameters = new Object[mergedParametersList.size()];
        mergedParametersList.toArray(mergedParameters);

        return new QueryStringWithParameters(fullQueryString, mergedParameters);
    }

    public static SqmSelectStatement<Object> parseQuery(Session session, String query) {
        QueryEngine queryEngine = getQueryEngine(session);
        SqmStatement<Object> statement = queryEngine.getHqlTranslator().translate(query, Object.class);
        if (statement instanceof SqmSelectStatement) {
            return (SqmSelectStatement<Object>) statement;
        } else {
            throw new RuntimeException("Not a select query: " + query);
        }
    }

    public static QueryEngine getQueryEngine(Session session) {
        SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) session.getSessionFactory();
        return sessionFactory.getQueryEngine();
    }

    /**
     * Runs a HQL query against the database.
     * @see QueryUtils#runHqlQuery(Session, String, Object[], Integer, Integer)
     * @param session the session
     * @param queryString the query
     * @param parameters the query parameters
     * @return the results of the query
     */
    public static List<Object> runHqlQuery(
            Session session,
            String queryString,
            @Nullable Object[] parameters) {
        return runHqlQuery(session, queryString, parameters, null, null);
    }

    /**
     * Runs a HQL query against the database.
     * @see QueryUtils#runHqlQuery(Session, String, Object[], Integer, Integer)
     * @param session the session
     * @param queryString the query
     * @param parameters the query parameters
     * @param firstResult index of the first result to return
     * @param maxResults maximum number of results to return
     * @return the results of the query
     */
    public static List<Object> runHqlQuery(
            Session session,
            String queryString,
            @Nullable Object[] parameters,
            @Nullable Integer firstResult,
            @Nullable Integer maxResults) {

        Query<Object> query = session.createQuery(queryString, null);
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] instanceof Collection) {
                    query.setParameterList("p" + (i + 1), (Collection) parameters[i]);
                } else
                    query.setParameter("p" + (i + 1), parameters[i]);
            }
        }

        if (firstResult != null) {
            query.setFirstResult(firstResult);
        }

        if(maxResults != null) {
            query.setMaxResults(maxResults);
        }

        try {
            return query.list();
        } catch (HibernateException e) {
            logger.error("Error running query", e);
            session.getTransaction().rollback();
            session.beginTransaction();
            throw e;
        }
    }

    /**
     * Loads an object by primary key.
     * @param persistence the persistence object
     * @param database the database (connection provider) to use
     * @param entityName the name of the entity to load - usually the normalized (lowercased, etc.) table name.
     * @param pk the primary key object. Might be an atomic value (String, Integer, etc.) for single-column primary
     * keys, or a composite object for multi-column primary keys.
     * @return the loaded object, or null if an object with that key does not exist.
     */
    public static Object getObjectByPk(
            Persistence persistence, String database, String entityName, Object pk) {
        Session session = persistence.getSession(database);
        TableAccessor table = persistence.getTableAccessor(database, entityName);
        return getObjectByPk(session, table, pk);
    }

    /**
     * Loads an object by primary key.
     * @param session the Hibernate session
     * @param table the table where to load the object from
     * @param pk the primary key object. Might be an atomic value (String, Integer, etc.) for single-column primary
     * keys, or a composite object for multi-column primary keys.
     * @return the loaded object, or null if an object with that key does not exist.
     */
    public static Object getObjectByPk(Session session, TableAccessor table, Object pk) {
        String actualEntityName = table.getTable().getActualEntityName();
        Object result;
        PropertyAccessor[] keyProperties = table.getKeyProperties();
        int size = keyProperties.length;
        if (size > 1) {
            result = session.get(actualEntityName, pk);
            return result;
        }
        PropertyAccessor propertyAccessor = keyProperties[0];
        Serializable key = (Serializable) propertyAccessor.get(pk);
        result = session.get(actualEntityName, key);
        return result;
    }

    /**
     * Loads an object by primary key.
     * @param persistence the persistence object
     * @param baseTable the table to query
     * @param pkObject the primary key object.
     * @return the loaded object, or null if an object with that key does not exist.
     */
    public static Object getObjectByPk(Persistence persistence, Table baseTable, Serializable pkObject) {
        return getObjectByPk
                (persistence, baseTable.getDatabaseName(), baseTable.getActualEntityName(), pkObject);
    }

    /**
     * Loads an object by primary key. It also verifies that the object falls within the results of a given query.
     * @param persistence the persistence object
     * @param baseTable the table to load from
     * @param pkObject the primary key object
     * @param query the query (where condition) that the object must fulfill
     * @param rootObject the OGNL rootFactory object against which to evaluate the query string.
     * @return the loaded object, or null if an object with that key does not exist or falls outside the query.
     */
    public static Object getObjectByPk(
            Persistence persistence, Table baseTable, Serializable pkObject, String query, Object rootObject) {
        return getObjectByPk
                (persistence, baseTable.getDatabaseName(), baseTable.getActualEntityName(), pkObject, query, rootObject);
    }

    /**
     * Loads an object by primary key. It also verifies that the object falls within the results of a given query.
     * @param persistence the persistence object
     * @param database the database (connection provider)
     * @param entityName the name of the entity to load
     * @param pk the primary key object
     * @param hqlQueryString the query (where condition) that the object must fulfill
     * @param rootObject the OGNL rootFactory object against which to evaluate the query string.
     * @return the loaded object, or null if an object with that key does not exist or falls outside the query.
     */
    public static Object getObjectByPk(
            Persistence persistence, String database, String entityName,
            Serializable pk, String hqlQueryString, Object rootObject) {
        TableAccessor table = persistence.getTableAccessor(database, entityName);
        List<Object> result;
        PropertyAccessor[] keyProperties = table.getKeyProperties();
        OgnlHqlFormat hqlFormat = OgnlHqlFormat.create(hqlQueryString);
        String formatString = hqlFormat.getFormatString();
        Object[] ognlParameters = hqlFormat.evaluateOgnlExpressions(rootObject);
        int p = ognlParameters.length;
        Object[] parameters = new Object[p + keyProperties.length];
        System.arraycopy(ognlParameters, 0, parameters, 0, p);
        SqmSelectStatement<Object> parsedQuery = parseQuery(persistence.getSession(database), formatString);
        if(parsedQuery.getRestriction() == null) {
            return getObjectByPk(persistence, database, entityName, pk);
        }

        String mainEntityAlias = getEntityAlias(entityName, parsedQuery);
        for(int i = 0; i < keyProperties.length; i++) {
            /* TODO PropertyAccessor propertyAccessor = keyProperties[i];
            EqualsTo condition = new EqualsTo();
            parsedQuery.setWhere(
                    new AndExpression(condition, new Parenthesis(parsedQuery.getWhere())));
            net.sf.jsqlparser.schema.Column column =
                    new net.sf.jsqlparser.schema.Column(mainEntityTable, propertyAccessor.getName());
            condition.setLeftExpression(column);
            JdbcNamedParameter jdbcParameter = new JdbcNamedParameter();
            jdbcParameter.setName("p" + (p + i + 1));
            condition.setRightExpression(jdbcParameter);
            parameters[p + i] = propertyAccessor.get(pk);*/
        }

        String fullQueryString = parsedQuery.toString();
        Session session = persistence.getSession(database);
        result = runHqlQuery(session, fullQueryString, parameters);
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    protected static String getEntityAlias(String entityName, SqmSelectStatement<?> query) {
        SqmFromClause fromClause = query.getQuerySpec().getFromClause();
        for (SqmRoot<?> root : fromClause.getRoots()) {
            String alias = root.getAlias();
            if (alias == null) {
                alias = root.getExplicitAlias();
            }
            if (alias != null) {
                if (entityName.equals(root.getEntityName())) {
                    return alias;
                } else {
                    SqmExpressible<?> expressible = root.getExpressible();
                    if (expressible instanceof EntityType && entityName.equals(((EntityType<?>) expressible).getName())) {
                        return alias;
                    }
                }
            }
        }
        logger.debug("Alias from entity " + entityName + " not found in query " + query);
        return null;
    }

    /**
     * Cleanly commits the current (for this thread) transaction of the given database.
     * @param persistence the persistence object
     * @param databaseName the name of the database (connection provider)
     */
    public static void commit(Persistence persistence, String databaseName) {
        Session session = persistence.getSession(databaseName);
        try {
            session.getTransaction().commit();
        } catch (HibernateException e) {
            persistence.closeSession(databaseName);
            throw e;
        }
    }

    /**
     * Navigates a ...-to-many relationship returning the list of objects associated with a given entity.
     * @param persistence the persistence object
     * @param databaseName the name of the database (connection provider)
     * @param entityName the type (entity name) of the master object
     * @param obj the master object
     * @param oneToManyRelationshipName the name of the relationship to navigate
     * @return the list of associated objects
     */
    @SuppressWarnings({"unchecked"})
    public static List<Object> getRelatedObjects(
            Persistence persistence, String databaseName, String entityName,
            Object obj, String oneToManyRelationshipName) {
        ForeignKey relationship =
                DatabaseLogic.findOneToManyRelationship(persistence.getDatabases(), databaseName,
                        entityName, oneToManyRelationshipName);
        if(relationship == null) {
            throw new IllegalArgumentException("Relationship not defined: " + oneToManyRelationshipName);
        }
        Table fromTable = relationship.getFromTable();
        Session session = persistence.getSession(fromTable.getDatabaseName());

        ClassAccessor toAccessor = persistence.getTableAccessor(databaseName, entityName);

        try {
            CriteriaDefinition criteria = createCriteria(session, fromTable);
            List<Predicate> where = new ArrayList<>();
            for (Reference reference : relationship.getReferences()) {
                Column fromColumn = reference.getActualFromColumn();
                Column toColumn = reference.getActualToColumn();
                PropertyAccessor toPropertyAccessor
                        = toAccessor.getProperty(toColumn.getActualPropertyName());
                Object toValue = toPropertyAccessor.get(obj);
                where.add(criteria.builder.equal(criteria.root.get(fromColumn.getActualPropertyName()), toValue));
            }
            return session.createQuery(criteria.query.where(where.toArray(new Predicate[0]))).list();
        } catch (Throwable e) {
            String msg = String.format(
                    "Cannot access relationship %s on entity %s.%s",
                    oneToManyRelationshipName, databaseName, entityName);
            logger.warn(msg, e);
        }
        return null;
    }

    public static CriteriaDefinition createCriteria(Session session, Table table) {
        return createCriteria(session, table.getActualEntityName());
    }

    public static CriteriaDefinition createCriteria(Session session, String entityName) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object> criteria = cb.createQuery();
        Root from = criteria.from(getEntityType(session, entityName));
        return new CriteriaDefinition(criteria.select(from), cb, from);
    }

    public static EntityType<?> getEntityType(Session session, Table table) {
        String entityName = table.getActualEntityName();
        return getEntityType(session, entityName);
    }

    @NotNull
    public static EntityType<?> getEntityType(Session session, String entityName) {
        for(ManagedType<?> managedType : session.getMetamodel().getManagedTypes()) {
            if(managedType instanceof EntityType) {
                EntityType entityType = (EntityType) managedType;
                if (entityType.getName().equals(entityName)) {
                    return entityType;
                }
            }
        }
        throw new RuntimeException("Entity not found: " + entityName);
    }

}
