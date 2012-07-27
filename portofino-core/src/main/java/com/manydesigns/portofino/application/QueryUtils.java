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

import com.manydesigns.elements.fields.search.Criterion;
import com.manydesigns.elements.fields.search.TextMatchMode;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlSqlFormat;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.database.TableCriteria;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.Work;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.StringReader;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(sql);
        String formatString = sqlFormat.getFormatString();
        Object[] parameters = sqlFormat.evaluateOgnlExpressions(null);
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
                    PreparedStatement stmt = connection.prepareStatement(queryString);
                    try {
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
                    } finally {
                        stmt.close(); //Chiude anche il result set
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
     * Runs a query, expressed as {@link TableCriteria}, against the database.
     * @param session the session
     * @param criteria the search criteria
     * @param firstResult index of the first result to return
     * @param maxResults maximum number of results to return
     * @return at most <code>maxResults</code> results from the query
     */
    public static List<Object> getObjects(
            Session session, TableCriteria criteria,
            @Nullable Integer firstResult, @Nullable Integer maxResults) {
        QueryStringWithParameters queryStringWithParameters =
                getQueryStringWithParametersForCriteria(criteria);

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
     * access values from the OGNL context, as well as from an (optional) root object.
     * @param session the session
     * @param queryString the query
     * @param rootObject the root object passed to the ognl evaluator (can be null).
     * @param firstResult index of the first result to return
     * @param maxResults maximum number of results to return
     * @return at most <code>maxResults</code> results from the query
     */
    public static List<Object> getObjects(
            Session session, String queryString,
            Object rootObject,
            @Nullable Integer firstResult, @Nullable Integer maxResults) {
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(queryString);
        String formatString = sqlFormat.getFormatString();
        Object[] parameters = sqlFormat.evaluateOgnlExpressions(rootObject);

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
        return getObjects(session, queryString, (TableCriteria) null, null, firstResult, maxResults);
    }

    /**
     * Tranforms a {@link TableCriteria} to a query string with an associated array of parameters.
     * @param criteria the criteria.
     * @return the same criteria encoded as a HQL query with parameters.
     */
    public static QueryStringWithParameters getQueryStringWithParametersForCriteria(
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
                StringBuilder params = new StringBuilder();
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
      * Runs a query against the database. The query is expressed as a {@link TableCriteria} object plus a
      * query string to be merged with it (the typical case of a search in a crud defined by a query).
      * The query string is processed with an {@link OgnlSqlFormat}, so it can access values from the OGNL context,
      * as well as from an (optional) root object.
      * @param session the session
      * @param queryString the query
      * @param criteria the search criteria to merge with the query.
      * @param rootObject the root object passed to the ognl evaluator (can be null).
      * @param firstResult index of the first result to return
      * @param maxResults maximum number of results to return
      * @return at most <code>maxResults</code> results from the query
     */
    public static List<Object> getObjects(
            Session session,
            String queryString,
            TableCriteria criteria,
            @Nullable Object rootObject,
            @Nullable Integer firstResult,
            @Nullable Integer maxResults) {
        QueryStringWithParameters result = mergeQuery(queryString, criteria, rootObject);

        return runHqlQuery(session, result.getQueryString(), result.getParameters(), firstResult, maxResults);
    }

    /**
     * Merges a HQL query string with a {@link TableCriteria} object representing a search. The query string
     * is processed with an {@link OgnlSqlFormat}, so it can access values from the OGNL context, as well as
     * from an (optional) root object.
     * @param queryString the base query
     * @param criteria the criteria to merge with the query
     * @param rootObject the OGNL root object (can be null)
     * @return the merged query
     */
    public static QueryStringWithParameters mergeQuery
            (String queryString, @Nullable TableCriteria criteria, Object rootObject) {
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(queryString);
        String formatString = sqlFormat.getFormatString();
        Object[] parameters = sqlFormat.evaluateOgnlExpressions(rootObject);

        QueryStringWithParameters criteriaQuery =
                getQueryStringWithParametersForCriteria(criteria);
        String criteriaQueryString = criteriaQuery.getQueryString();
        Object[] criteriaParameters = criteriaQuery.getParameters();

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect parsedQueryString;
        PlainSelect parsedCriteriaQuery;
        String queryPrefix = "select __portofino_fake_select__ ";
        try {
            if(!formatString.toLowerCase().trim().startsWith("select")) {
                formatString = queryPrefix + formatString;
            }
            parsedQueryString =
                    (PlainSelect) ((Select) parserManager.parse(new StringReader(formatString)))
                            .getSelectBody();
            if(StringUtils.isEmpty(criteriaQueryString)) {
                parsedCriteriaQuery = new PlainSelect();
            } else {
                if(!criteriaQueryString.toLowerCase().trim().startsWith("select")) {
                    criteriaQueryString = queryPrefix + criteriaQueryString;
                }
                parsedCriteriaQuery =
                        (PlainSelect) ((Select) parserManager.parse
                                (new StringReader(criteriaQueryString)))
                                .getSelectBody();
            }
        } catch (JSQLParserException e) {
            throw new RuntimeException("Couldn't merge query", e);
        }

        Expression whereExpression;
        if(parsedQueryString.getWhere() != null) {
            if(parsedCriteriaQuery.getWhere() != null) {
                whereExpression = parsedQueryString.getWhere();
                if(!(whereExpression instanceof Parenthesis)) {
                    whereExpression = new Parenthesis(whereExpression);
                }
                whereExpression = new AndExpression(whereExpression, parsedCriteriaQuery.getWhere());
            } else {
                whereExpression = parsedQueryString.getWhere();
            }
        } else {
            whereExpression = parsedCriteriaQuery.getWhere();
        }
        parsedQueryString.setWhere(whereExpression);
        if(criteria != null && criteria.getOrderBy() != null) {
            List orderByElements = new ArrayList();
            OrderByElement orderByElement = new OrderByElement();
            orderByElement.setAsc(criteria.getOrderBy().isAsc());
            String propertyName = criteria.getOrderBy().getPropertyAccessor().getName();
            orderByElement.setExpression(
                    new net.sf.jsqlparser.schema.Column(
                            new net.sf.jsqlparser.schema.Table(), propertyName));
            orderByElements.add(orderByElement);
            parsedQueryString.setOrderByElements(orderByElements);
        }
        String fullQueryString = parsedQueryString.toString();
        if(fullQueryString.toLowerCase().startsWith(queryPrefix)) {
            fullQueryString = fullQueryString.substring(queryPrefix.length());
        }

        // merge the parameters
        ArrayList<Object> mergedParametersList = new ArrayList<Object>();
        mergedParametersList.addAll(Arrays.asList(parameters));
        mergedParametersList.addAll(Arrays.asList(criteriaParameters));
        Object[] mergedParameters = new Object[mergedParametersList.size()];
        mergedParametersList.toArray(mergedParameters);

        return new QueryStringWithParameters(fullQueryString, mergedParameters);
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

        Query query = session.createQuery(queryString);
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i, parameters[i]);
            }
        }

        if (firstResult != null) {
            query.setFirstResult(firstResult);
        }

        if(maxResults != null) {
            query.setMaxResults(maxResults);
        }

        //noinspection unchecked
        try {
            List<Object> result = query.list();
            return result;
        } catch (HibernateException e) {
            logger.error("Error running query", e);
            session.getTransaction().rollback();
            session.beginTransaction();
            throw e;
        }
    }

    /**
     * Loads an object by primary key.
     * @param application the application
     * @param database the database (connection provider) to use
     * @param entityName the name of the entity to load - usually the normalized (lowercased, etc.) table name.
     * @param pk the primary key object. Might be an atomic value (String, Integer, etc.) for single-column primary
     * keys, or a composite object for multi-column primary keys.
     * @return the loaded object, or null if an object with that key does not exist.
     */
    public static Object getObjectByPk(
            Application application, String database, String entityName, Serializable pk) {
        Session session = application.getSession(database);
        TableAccessor table = application.getTableAccessor(database, entityName);
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
     * @param application the application
     * @param baseTable the table to query
     * @param pkObject the primary key object.
     * @return the loaded object, or null if an object with that key does not exist.
     */
    public static Object getObjectByPk(Application application, Table baseTable, Serializable pkObject) {
        return getObjectByPk
                (application, baseTable.getDatabaseName(), baseTable.getActualEntityName(), pkObject);
    }

    /**
     * Loads an object by primary key. It also verifies that the object falls within the results of a given query.
     * @param application the application
     * @param baseTable the table to load from
     * @param pkObject the primary key object
     * @param query the query (where condition) that the object must fulfill
     * @param rootObject the OGNL root object against which to evaluate the query string.
     * @return the loaded object, or null if an object with that key does not exist or falls outside the query.
     */
    public static Object getObjectByPk(
            Application application, Table baseTable, Serializable pkObject, String query, Object rootObject) {
        return getObjectByPk
                (application, baseTable.getDatabaseName(), baseTable.getActualEntityName(), pkObject, query, rootObject);
    }

    /**
     * Loads an object by primary key. It also verifies that the object falls within the results of a given query.
     * @param application the application
     * @param database the database (connection provider)
     * @param entityName the name of the entity to load
     * @param pk the primary key object
     * @param hqlQueryString the query (where condition) that the object must fulfill
     * @param rootObject the OGNL root object against which to evaluate the query string.
     * @return the loaded object, or null if an object with that key does not exist or falls outside the query.
     */
    public static Object getObjectByPk(
            Application application, String database, String entityName,
            Serializable pk, String hqlQueryString, Object rootObject) {
        if(!hqlQueryString.toUpperCase().contains("WHERE")) { //TODO
            return getObjectByPk(application, database, entityName, pk);
        }
        TableAccessor table = application.getTableAccessor(database, entityName);
        List<Object> result;
        PropertyAccessor[] keyProperties = table.getKeyProperties();
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(hqlQueryString);
        String formatString = sqlFormat.getFormatString();
        Object[] ognlParameters = sqlFormat.evaluateOgnlExpressions(rootObject);
        int i = keyProperties.length;
        int p = ognlParameters.length;
        Object[] parameters = new Object[p + i];
        System.arraycopy(ognlParameters, 0, parameters, i, p);
        int indexOfWhere = formatString.toUpperCase().indexOf("WHERE") + 5; //5 = "WHERE".length()
        String formatStringPrefix = formatString.substring(0, indexOfWhere);
        String mainEntityAlias = getEntityAlias(entityName, formatString);
        formatString = formatString.substring(indexOfWhere);

        for(PropertyAccessor propertyAccessor : keyProperties) {
            i--;
            formatString = mainEntityAlias + propertyAccessor.getName() + " = ? AND " + formatString;
            parameters[i] = propertyAccessor.get(pk);
        }
        formatString = formatStringPrefix + " " + formatString;
        Session session = application.getSession(database);
        result = runHqlQuery(session, formatString, parameters);
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    private static String getEntityAlias(String entityName, String hqlQueryString) {
        try {
            CCJSqlParserManager parserManager = new CCJSqlParserManager();
            PlainSelect plainSelect =
                (PlainSelect) ((Select) parserManager.parse(new StringReader(hqlQueryString))).getSelectBody();
            FromItem fromItem = plainSelect.getFromItem();
            if (hasEntityAlias(entityName, fromItem)) {
                return fromItem.getAlias() + ".";
            }
            for(Object o : plainSelect.getJoins()) {
                Join join = (Join) o;
                if (hasEntityAlias(entityName, join.getRightItem())) {
                    return join.getRightItem().getAlias() + ".";
                }
            }
            logger.debug("Alias from entity " + entityName + " not found in query " + hqlQueryString);
            return "";
        } catch(Exception e) {
            logger.debug("Couldn't parse query " + hqlQueryString +
                         ", assuming entity " + entityName + " has no alias", e);
            return "";
        }
    }

    private static boolean hasEntityAlias(String entityName, FromItem fromItem) {
        return fromItem instanceof net.sf.jsqlparser.schema.Table &&
               ((net.sf.jsqlparser.schema.Table) fromItem).getName().equals(entityName) &&
               !StringUtils.isBlank(fromItem.getAlias());
    }

    public static void commit(Application application, String databaseName) {
        Session session = application.getSession(databaseName);
        try {
            session.getTransaction().commit();
        } catch (HibernateException e) {
            application.closeSession(databaseName);
            throw e;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static List<Object> getRelatedObjects(
            Application application, String databaseName, String entityName,
            Object obj, String oneToManyRelationshipName) {
        Model model = application.getModel();
        ForeignKey relationship =
                DatabaseLogic.findOneToManyRelationship(model, databaseName,
                        entityName, oneToManyRelationshipName);
        if(relationship == null) {
            throw new IllegalArgumentException("Relationship not defined: " + oneToManyRelationshipName);
        }
        Table fromTable = relationship.getFromTable();
        Session session = application.getSession(fromTable.getDatabaseName());

        ClassAccessor toAccessor = application.getTableAccessor(databaseName, entityName);

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
                    "Cannot access relationship %s on entity %s.%s",
                    oneToManyRelationshipName, databaseName, entityName);
            logger.warn(msg, e);
        }
        return null;
    }

}
