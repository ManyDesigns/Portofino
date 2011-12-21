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

package com.manydesigns.portofino.database;

import com.manydesigns.elements.fields.search.Criterion;
import com.manydesigns.elements.fields.search.TextMatchMode;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlSqlFormat;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.TableCriteria;
import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.reflection.TableAccessor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
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
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class QueryUtils {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected static final String WHERE_STRING = " WHERE ";
    protected static final Pattern FROM_PATTERN =
            Pattern.compile("(SELECT\\s+.*\\s+)?FROM\\s+([a-z_$\\u0080-\\ufffe]{1}[a-z_$0-9\\u0080-\\ufffe]*).*",
                            Pattern.CASE_INSENSITIVE | Pattern.DOTALL); //. (dot) matches newlines

    protected static final Logger logger = LoggerFactory.getLogger(QueryUtils.class);

    public static List<Object[]> runSql(Session session, String sql) {
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

        return result;
    }

    public static List<Object> getObjects(
            Session session, TableCriteria criteria,
            @Nullable Integer firstResult, @Nullable Integer maxResults) {
        QueryStringWithParameters queryStringWithParameters =
                getQueryStringWithParametersForCriteria(criteria);

        return runHqlQuery(
                session,
                queryStringWithParameters.getQueryString(),
                queryStringWithParameters.getParamaters(),
                firstResult,
                maxResults
        );
    }


    public static List<Object> getObjects(
            Session session, String queryString,
            Object rootObject,
            @Nullable Integer firstResult, @Nullable Integer maxResults) {
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(queryString);
        String formatString = sqlFormat.getFormatString();
        Object[] parameters = sqlFormat.evaluateOgnlExpressions(rootObject);

        return runHqlQuery(session, formatString, parameters, firstResult, maxResults);
    }

    public static List<Object> getObjects(
            Session session, String queryString,
            @Nullable Integer firstResult, @Nullable Integer maxResults) {
        return getObjects(session, queryString, (TableCriteria) null, null, firstResult, maxResults);
    }

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

    public static Table getTableFromQueryString(Database database, String queryString) {
        Matcher matcher = FROM_PATTERN.matcher(queryString);
        String entityName;
        if (matcher.matches()) {
            entityName =  matcher.group(2);
        } else {
            return null;
        }

        Table table = DataModelLogic.findTableByEntityName(database, entityName);
        return table;
    }

    public static List<Object> getObjects(
            Session session,
            String queryString,
            TableCriteria criteria,
            @Nullable Object rootObject,
            @Nullable Integer firstResult,
            @Nullable Integer maxResults) {
        QueryStringWithParameters result = mergeQuery(queryString, criteria, rootObject);

        return runHqlQuery(session, result.getQueryString(), result.getParamaters(), firstResult, maxResults);
    }

    public static QueryStringWithParameters mergeQuery
            (String queryString, TableCriteria criteria, Object rootObject) {
        OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(queryString);
        String formatString = sqlFormat.getFormatString();
        Object[] parameters = sqlFormat.evaluateOgnlExpressions(rootObject);

        QueryStringWithParameters criteriaQuery =
                getQueryStringWithParametersForCriteria(criteria);
        String criteriaQueryString = criteriaQuery.getQueryString();
        Object[] criteriaParameters = criteriaQuery.getParamaters();

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect parsedQueryString;
        PlainSelect parsedCriteriaQuery;
        String queryPrefix = "select * ";
        try {
            parsedQueryString =
                    (PlainSelect) ((Select) parserManager.parse(new StringReader(queryPrefix + formatString)))
                            .getSelectBody();
            parsedCriteriaQuery =
                    (PlainSelect) ((Select) parserManager.parse(new StringReader(queryPrefix + criteriaQueryString)))
                            .getSelectBody();
        } catch (JSQLParserException e) {
            throw new RuntimeException("Couldn't merge query", e);
        }

        Expression whereExpression;
        if(parsedQueryString.getWhere() != null) {
            if(parsedCriteriaQuery.getWhere() != null) {
                whereExpression = new AndExpression(parsedQueryString.getWhere(), parsedCriteriaQuery.getWhere());
            } else {
                whereExpression = parsedQueryString.getWhere();
            }
        } else {
            whereExpression = parsedCriteriaQuery.getWhere();
        }
        parsedQueryString.setWhere(whereExpression);
        String fullQueryString = parsedQueryString.toString().substring(queryPrefix.length());

        // merge the parameters
        ArrayList<Object> mergedParametersList = new ArrayList<Object>();
        mergedParametersList.addAll(Arrays.asList(parameters));
        mergedParametersList.addAll(Arrays.asList(criteriaParameters));
        Object[] mergedParameters = new Object[mergedParametersList.size()];
        mergedParametersList.toArray(mergedParameters);

        return new QueryStringWithParameters(fullQueryString, mergedParameters);
    }

    public static List<Object> runHqlQuery(
            Session session,
            String queryString,
            @Nullable Object[] parameters) {
        return runHqlQuery(session, queryString, parameters, null, null);
    }

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
        List<Object> result = query.list();
        return result;
    }

    public static Object getObjectByPk(
            Application application, String database, String entityName, Serializable pk) {
        Session session = application.getSession(database);
        TableAccessor table = application.getTableAccessor(database, entityName);
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

    public static Object getObjectByPk(Application application, Table baseTable, Serializable pkObject) {
        return getObjectByPk
                (application, baseTable.getDatabaseName(), baseTable.getActualEntityName(), pkObject);
    }

    public static Object getObjectByPk(Application application, Table baseTable, Serializable pkObject, String query, Object o) {
        return getObjectByPk
                (application, baseTable.getDatabaseName(), baseTable.getActualEntityName(), pkObject, query, o);
    }

    public static Object getObjectByPk(
            Application application, String database, String entityName,
            Serializable pk, String queryString, Object rootObject) {
        if(queryString.toUpperCase().indexOf("WHERE") == -1) {
            return getObjectByPk(application, database, entityName, pk);
        }
        TableAccessor table = application.getTableAccessor(database, entityName);
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
        Session session = application.getSession(database);
        result = runHqlQuery(session, formatString, parameters);
        if(result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
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
                DataModelLogic.findOneToManyRelationship(model, databaseName,
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
