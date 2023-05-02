package com.manydesigns.portofino.persistence;

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.fields.search.Ordering;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.database.model.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import org.hibernate.Session;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class SingleTableQueryCollection {

    protected final Persistence persistence;
    protected final Table table;
    protected final String query;
    protected final Criteria criteria;
    protected Ordering ordering;

    private static final Logger logger = LoggerFactory.getLogger(SingleTableQueryCollection.class);

    public SingleTableQueryCollection(
            Persistence persistence, Table baseTable, String query, Criteria criteria, Ordering ordering) {
        this.persistence = persistence;
        this.table = baseTable;
        this.query = query;
        this.criteria = criteria;
        this.ordering = ordering;
    }

    public SingleTableQueryCollection(Persistence persistence, Table baseTable, String query) {
        this(persistence, baseTable, query, new Criteria(), null);
    }

    public long count() {
        return count(null);
    }

    public long count(Object contextObject) {
        QueryStringWithParameters query =
                QueryUtils.mergeQuery(getSession(), this.query, table, criteria, ordering, contextObject);

        String queryString = query.getQueryString();
        String totalRecordsQueryString = generateCountQuery(queryString);
        if (totalRecordsQueryString == null) {
            return -1;
        }
        List<Object> result = QueryUtils.runHqlQuery(getSession(), totalRecordsQueryString, query.getParameters());
        return ((Number) result.get(0)).longValue();
    }

    protected String generateCountQuery(String queryString) {
        try {
            SqmSelectStatement<Object> parsedQuery = QueryUtils.parseQuery(getSession(), queryString);
            JpaSelection<Object> selection = parsedQuery.getSelection();
            if (selection.isCompoundSelection()) {
                logger.warn("I don't know how to generate a count query for {}", queryString);
                return null;
            }
            String entityName = this.table.getActualEntityName();
            SqmSelectStatement<Object> templateQuery =
                    QueryUtils.parseQuery(getSession(), "select count(*) from " + entityName);
            parsedQuery.select(templateQuery.getSelection());

            // For some reason, Hibernate loses the "*" when printing back to a string
            return parsedQuery.toHqlString().replace("count()", "count(*)");
        } catch(Exception e) {
            /* TODO queryString = "SELECT count(*) " + queryString;
            PlainSelect plainSelect =
                    (PlainSelect) ((Select) parserManager.parse(new StringReader(queryString))).getSelectBody();
            plainSelect.setOrderByElements(null);
            return plainSelect.toString();*/
        }
        logger.warn("I don't know how to generate a count query for {}", queryString);
        return null;
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

    public SingleTableQueryCollection where(Criteria criteria) {
        Criteria merged = new Criteria();
        merged.addAll(this.criteria);
        merged.addAll(criteria);
        return new SingleTableQueryCollection(persistence, table, query, merged, ordering);
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
}
