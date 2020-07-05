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

package com.manydesigns.portofino.resourceactions.crud;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.Insertable;
import com.manydesigns.elements.annotations.Updatable;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.ForeignKey;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.persistence.TableCriteria;
import com.manydesigns.portofino.resourceactions.ActionInstance;
import com.manydesigns.portofino.resourceactions.ResourceActionName;
import com.manydesigns.portofino.resourceactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.resourceactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.resourceactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.resourceactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.resourceactions.crud.configuration.database.SelectionProviderReference;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default AbstractCrudAction implementation. Implements a crud resource over a database table, based on a HQL query.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@SupportsPermissions({ CrudAction.PERMISSION_CREATE, CrudAction.PERMISSION_EDIT, CrudAction.PERMISSION_DELETE })
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template.groovy")
@ConfigurationClass(CrudConfiguration.class)
@ResourceActionName("Crud")
public class CrudAction<T extends Serializable> extends AbstractCrudAction<T> {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public static final String[][] CRUD_CONFIGURATION_FIELDS =
                {{"name", "database", "query", "searchTitle", "createTitle", "readTitle", "editTitle", "variable",
                  "largeResultSet", "rowsPerPage", "columns"}};

    public Table baseTable;

    //--------------------------------------------------------------------------
    // Data objects
    //--------------------------------------------------------------------------

    public Session session;

    @Autowired
    public Persistence persistence;

    protected long totalSearchRecords = -1;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(CrudAction.class);

    @Override
    public long getTotalSearchRecords() {
        if(totalSearchRecords < 0) {
            calculateTotalSearchRecords();
        }
        return totalSearchRecords;
    }

    protected long calculateTotalSearchRecords() {
        TableCriteria criteria = new TableCriteria(baseTable);
        if(searchForm != null) {
            searchForm.configureCriteria(criteria);
        }
        QueryStringWithParameters query =
                QueryUtils.mergeQuery(getBaseQuery(), criteria, this);

        String queryString = query.getQueryString();
        String totalRecordsQueryString;
        try {
            totalRecordsQueryString = generateCountQuery(queryString);
        } catch (JSQLParserException e) {
            throw new Error(e);
        }
        //TODO gestire count non disponibile (totalRecordsQueryString == null)
        List<Object> result = QueryUtils.runHqlQuery(session, totalRecordsQueryString, query.getParameters());
        return totalSearchRecords = ((Number) result.get(0)).longValue();
    }

    protected String generateCountQuery(String queryString) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try {
            PlainSelect plainSelect =
                (PlainSelect) ((Select) parserManager.parse(new StringReader(queryString))).getSelectBody();
            logger.debug("Query string {} contains select", queryString);
            List items = plainSelect.getSelectItems();
            if(items.size() != 1) {
                logger.error("I don't know how to generate a count query for {}", queryString);
                return null;
            }
            SelectExpressionItem item = (SelectExpressionItem) items.get(0);
            Function function = new Function();
            function.setName("count");
            function.setParameters(new ExpressionList(Arrays.asList(item.getExpression())));
            item.setExpression(function);
            plainSelect.setOrderByElements(null);
            return plainSelect.toString();
        } catch(Exception e) {
            logger.debug("Query string " + queryString + " does not contain select", e);
            queryString = "SELECT count(*) " + queryString;
            PlainSelect plainSelect =
                (PlainSelect) ((Select) parserManager.parse(new StringReader(queryString))).getSelectBody();
            plainSelect.setOrderByElements(null);
            return plainSelect.toString();
        }
    }

    @Override
    protected void commitTransaction() {
        session.getTransaction().commit();
    }

    @Override
    public boolean isCreateEnabled() {
        return classAccessor != null &&
               (classAccessor.getAnnotation(Insertable.class) == null ||
                classAccessor.getAnnotation(Insertable.class).value());
    }

    @Override
    protected void doSave(T object) {
        try {
            session.save(baseTable.getActualEntityName(), object);
        } catch(ConstraintViolationException e) {
            logger.warn("Constraint violation in save", e);
            throw new RuntimeException(ElementsThreadLocals.getText("save.failed.because.constraint.violated"));
        }
    }

    @Override
    public boolean isEditEnabled() {
        return classAccessor != null &&
               (classAccessor.getAnnotation(Updatable.class) == null ||
                classAccessor.getAnnotation(Updatable.class).value());
    }

    @Override
    protected void doUpdate(T object) {
        try {
            session.update(baseTable.getActualEntityName(), object);
        } catch(ConstraintViolationException e) {
            logger.warn("Constraint violation in update", e);
            throw new RuntimeException(ElementsThreadLocals.getText("save.failed.because.constraint.violated"));
        }
    }

    @Override
    public boolean isDeleteEnabled() {
        return classAccessor != null &&
                (classAccessor.getAnnotation(Updatable.class) == null ||
                 classAccessor.getAnnotation(Updatable.class).value());
    }

    @Override
    protected void doDelete(T object) {
        session.delete(baseTable.getActualEntityName(), object);
    }

    //**************************************************************************
    // Setup
    //**************************************************************************

    protected ModelSelectionProviderSupport createSelectionProviderSupport() {
        return new ModelSelectionProviderSupport(this, persistence);
    }

    @Override
    protected boolean saveConfiguration(Object configuration) {
        CrudConfiguration crudConfiguration = (CrudConfiguration) configuration;
        List<SelectionProviderReference> sps = new ArrayList<>(crudConfiguration.getSelectionProviders());
        crudConfiguration.getSelectionProviders().clear();
        crudConfiguration.persistence = persistence;
        crudConfiguration.init();
        sps.forEach(sp -> {
            ForeignKey fk = DatabaseLogic.findForeignKeyByName(
                    crudConfiguration.getActualTable(), sp.getSelectionProviderName());
            if(fk != null) {
                sp.setForeignKeyName(sp.getSelectionProviderName());
                sp.setSelectionProviderName(null);
            }
            if(sp.getSelectionProviderName() != null || sp.getForeignKeyName() != null) {
                crudConfiguration.getSelectionProviders().add(sp);
            }
        });
        List<CrudProperty> existingProperties = this.crudConfiguration.getProperties();
        List<CrudProperty> configuredProperties = crudConfiguration.getProperties();
        List<CrudProperty> newProperties = configuredProperties.stream().map(p1 -> {
            Optional<CrudProperty> maybeP2 =
                    existingProperties.stream().filter(p2 -> p1.getName().equals(p2.getName())).findFirst();
            CrudProperty p2 = maybeP2.orElse(new CrudProperty());
            p2.setName(p1.getName());
            p2.setEnabled(p1.isEnabled());
            p2.setInsertable(p1.isInsertable());
            p2.setInSummary(p1.isInSummary());
            p2.setLabel(p1.getLabel());
            p2.setSearchable(p1.isSearchable());
            p2.setUpdatable(p1.isUpdatable());
            return p2;
        }).collect(Collectors.toList());
        crudConfiguration.setProperties(newProperties);
        return super.saveConfiguration(crudConfiguration);
    }

    @Override
    protected ClassAccessor prepare(ActionInstance actionInstance) {
        Database actualDatabase = getCrudConfiguration().getActualDatabase();
        //TODO I18n
        if (actualDatabase == null) {
            String message =
                    "Crud " + crudConfiguration.getName() + " (" + actionInstance.getPath() + ") " +
                    "refers to a nonexistent database: " + getCrudConfiguration().getDatabase();
            logger.warn(message);
            RequestMessages.addErrorMessage(message);
            return null;
        }

        baseTable = getCrudConfiguration().getActualTable();
        if (baseTable == null) {
            String message =
                    "Crud " + crudConfiguration.getName() + " (" + actionInstance.getPath() + ") " +
                    "refers to an invalid table.";
            logger.warn(message);
            RequestMessages.addErrorMessage(message);
            return null;
        }

        return persistence.getTableAccessor(baseTable);
    }

    @Override
    public CrudAction<T> init() {
        super.init();
        if(getCrudConfiguration() != null && getCrudConfiguration().getActualDatabase() != null) {
            session = persistence.getSession(getCrudConfiguration().getDatabase());
            selectionProviderSupport = createSelectionProviderSupport();
            selectionProviderSupport.setup();
        }
        return this;
    }

    //**************************************************************************
    // Object loading
    //**************************************************************************

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<T> loadObjects() {
        try {
            TableCriteria criteria = new TableCriteria(baseTable);
            if(searchForm != null) {
                searchForm.configureCriteria(criteria);
            }
            if(!StringUtils.isBlank(sortProperty) && !StringUtils.isBlank(sortDirection)) {
                try {
                    PropertyAccessor orderByProperty = classAccessor.getProperty(sortProperty);
                    criteria.orderBy(orderByProperty, sortDirection);
                } catch (NoSuchFieldException e) {
                    logger.error("Can't order by " + sortProperty + ", property accessor not found", e);
                }
            }
            objects = (List) QueryUtils.getObjects(session, getBaseQuery(), criteria, this, firstResult, maxResults);
        } catch (ClassCastException e) {
            objects = new ArrayList<>();
            logger.warn("Incorrect Field Type", e);
            RequestMessages.addWarningMessage(ElementsThreadLocals.getText("incorrect.field.type"));
        }
        return objects;
    }

    /**
     * Computes the query underlying the CRUD action. By default, it returns configuration.query i.e. the HQL query
     * stored in configuration.xml. However, you can override this method to insert your own logic, for example to
     * change the query depending on the user's role.
     * @return the query used as a basis for search and object loading.
     */
    protected String getBaseQuery() {
        return getCrudConfiguration().getQuery();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T loadObjectByPrimaryKey(Serializable pkObject) {
        return (T) QueryUtils.getObjectByPk(
                persistence,
                baseTable, pkObject,
                getBaseQuery(), this);
    }

    //--------------------------------------------------------------------------
    // Accessors
    //--------------------------------------------------------------------------

    public Table getBaseTable() {
        return baseTable;
    }

    public void setBaseTable(Table baseTable) {
        this.baseTable = baseTable;
    }

    public CrudConfiguration getCrudConfiguration() {
        return (CrudConfiguration) crudConfiguration;
    }

}
