package com.manydesigns.portofino.persistence;

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.fields.search.Ordering;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.database.model.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

/**
 * A collection of persistent objects, defined by an HQL query returning a single entity (i.e. rows from a single table,
 * generally). Doesn't actually hold the objects, but offers methods to retrieve them.
 *
 * @author Alessio Stalla – alessiostalla@gmail.com
 */
public class SingleEntityQueryCollection {

    protected final Persistence persistence;
    protected final Table table;
    protected final String query;
    protected final Criteria criteria;
    protected Ordering ordering;

    private static final Logger logger = LoggerFactory.getLogger(SingleEntityQueryCollection.class);

    public SingleEntityQueryCollection(
            Persistence persistence, Table baseTable, String query, Criteria criteria, Ordering ordering) {
        this.persistence = persistence;
        this.table = baseTable;
        this.query = query;
        this.criteria = criteria;
        this.ordering = ordering;
    }

    public SingleEntityQueryCollection(Persistence persistence, Table baseTable, String query) {
        this(persistence, baseTable, query, new Criteria(), null);
    }

    public long count() {
        return count(null);
    }

    public long count(Object contextObject) {
        QueryStringWithParameters query = QueryUtils.mergeQuery(this.query, table, criteria, ordering, contextObject);

        String queryString = query.getQueryString();
        String totalRecordsQueryString;
        try {
            totalRecordsQueryString = generateCountQuery(queryString);
        } catch (JSQLParserException e) {
            throw new Error(e);
        }
        //TODO gestire count non disponibile (totalRecordsQueryString == null)
        List<Object> result = QueryUtils.runHqlQuery(getSession(), totalRecordsQueryString, query.getParameters());
        return ((Number) result.get(0)).longValue();
    }

    protected String generateCountQuery(String queryString) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try {
            PlainSelect plainSelect =
                    (PlainSelect) ((Select) parserManager.parse(new StringReader(queryString))).getSelectBody();
            List<SelectItem> items = plainSelect.getSelectItems();
            if(items.size() != 1) {
                logger.error("I don't know how to generate a count query for {}", queryString);
                return null;
            }
            SelectExpressionItem item = (SelectExpressionItem) items.get(0);
            Function function = new Function();
            function.setName("count");
            function.setParameters(new ExpressionList(Collections.singletonList(item.getExpression())));
            item.setExpression(function);
            plainSelect.setOrderByElements(null);
            return plainSelect.toString();
        } catch(Exception e) {
            queryString = "SELECT count(*) " + queryString;
            PlainSelect plainSelect =
                    (PlainSelect) ((Select) parserManager.parse(new StringReader(queryString))).getSelectBody();
            plainSelect.setOrderByElements(null);
            return plainSelect.toString();
        }
    }
    public Object load(Object pkObject) {
        return load(pkObject, null);
    }

    public Object load(Object pkObject, Object contextObject) {
        return QueryUtils.getObjectByPk(persistence, table, (Serializable) pkObject, query, contextObject);
    }

    public List<Object> load(Integer firstResult, Integer maxResults) {
        return load(firstResult, maxResults, null);
    }

    public List<Object> load(Integer firstResult, Integer maxResults, Object contextObject) {
        return QueryUtils.getObjects(
                getSession(), query, table, criteria, ordering, contextObject, firstResult, maxResults);
    }

    public void save(Object object) {
        getSession().persist(table.getActualEntityName(), object);
    }

    public void update(Object object) {
        getSession().merge(table.getActualEntityName(), object);
    }

    public void delete(Object object) {
        getSession().remove(object);
    }

    public Session getSession() {
        return persistence.getSession(table.getDatabaseName());
    }

    public SingleEntityQueryCollection where(Criteria criteria) {
        Criteria merged = new Criteria();
        merged.addAll(this.criteria);
        merged.addAll(criteria);
        return new SingleEntityQueryCollection(persistence, table, query, merged, ordering);
    }

    public Table getTable() {
        return table;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public TableAccessor getClassAccessor() {
        return persistence.getTableAccessor(table);
    }

    public Ordering getOrdering() {
        return ordering;
    }

    public void setOrdering(Ordering ordering) {
        this.ordering = ordering;
    }

    public boolean contains(Object id) {
        return load(id) != null;
    }
}
