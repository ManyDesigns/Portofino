package com.manydesigns.portofino.persistence;

import com.manydesigns.elements.fields.search.Criteria;
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

public class SingleTableQueryCollection {

    protected final Persistence persistence;
    protected final Table table;
    protected final String query;
    protected final TableCriteria criteria;
    
    private static final Logger logger = LoggerFactory.getLogger(SingleTableQueryCollection.class);

    public SingleTableQueryCollection(
            Persistence persistence, Table baseTable, String query, TableCriteria criteria) {
        this.persistence = persistence;
        this.table = baseTable;
        this.query = query;
        this.criteria = criteria;
    }

    public SingleTableQueryCollection(Persistence persistence, Table baseTable, String query) {
        this(persistence, baseTable, query, new TableCriteria(baseTable));
    }

    public long count() {
        return count(null);
    }
    
    public long count(Object contextObject) {
        QueryStringWithParameters query = QueryUtils.mergeQuery(this.query, criteria, contextObject);

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
        return QueryUtils.getObjects(getSession(), query, criteria, contextObject, firstResult, maxResults);
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

    public SingleTableQueryCollection where(TableCriteria criteria) {
        TableCriteria merged = new TableCriteria(table);
        merged.addAll(this.criteria);
        merged.addAll(criteria);
        return new SingleTableQueryCollection(persistence, table, query, merged);
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
}
