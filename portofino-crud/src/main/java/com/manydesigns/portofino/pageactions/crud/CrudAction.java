/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.crud;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.database.TableCriteria;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SelectionProviderLogic;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.reflection.TableAccessor;
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
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default AbstractCrudAction implementation. Implements a crud page over a database table, based on a HQL query.
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
@PageActionName("Crud")
public class CrudAction extends AbstractCrudAction<Object> {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String[][] CRUD_CONFIGURATION_FIELDS =
                {{"name", "database", "query", "searchTitle", "createTitle", "readTitle", "editTitle", "variable",
                  "largeResultSet", "rowsPerPage", "columns"}};

    public Table baseTable;

    //--------------------------------------------------------------------------
    // Data objects
    //--------------------------------------------------------------------------

    public Session session;

    @Inject(DatabaseModule.PERSISTENCE)
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
        // calculate totalRecords
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
        List<Object> result = QueryUtils.runHqlQuery
                (session, totalRecordsQueryString,
                        query.getParameters());
        return totalSearchRecords = ((Number) result.get(0)).longValue();
    }

    protected String generateCountQuery(String queryString) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        try {
            PlainSelect plainSelect =
                (PlainSelect) ((Select) parserManager.parse(new StringReader(queryString))).getSelectBody();
            logger.debug("Query string {} contains select");
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
            logger.debug("Query string {} does not contain select", e);
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
    protected void doSave(Object object) {
        try {
            session.save(baseTable.getActualEntityName(), object);
        } catch(ConstraintViolationException e) {
            logger.warn("Constraint violation in save", e);
            throw new RuntimeException(ElementsThreadLocals.getText("save.failed.because.constraint.violated"));
        }
    }

    @Override
    protected void doUpdate(Object object) {
        try {
            session.update(baseTable.getActualEntityName(), object);
        } catch(ConstraintViolationException e) {
            logger.warn("Constraint violation in update", e);
            throw new RuntimeException(ElementsThreadLocals.getText("save.failed.because.constraint.violated"));
        }
    }

    @Override
    protected void doDelete(Object object) {
        session.delete(baseTable.getActualEntityName(), object);
    }

    //**************************************************************************
    // Setup
    //**************************************************************************

    @Before
    public void prepare() {
        if(getCrudConfiguration() != null && getCrudConfiguration().getActualDatabase() != null) {
            selectionProviderSupport = createSelectionProviderSupport();
            selectionProviderSupport.setup();
        }
    }

    protected ModelSelectionProviderSupport createSelectionProviderSupport() {
        return new ModelSelectionProviderSupport(this, persistence);
    }

    @Override
    protected void setupConfigurationForm(FormBuilder formBuilder) {
        super.setupConfigurationForm(formBuilder);
        SelectionProvider databaseSelectionProvider =
                SelectionProviderLogic.createSelectionProvider(
                        "database",
                        persistence.getModel().getDatabases(),
                        Database.class,
                        null,
                        new String[]{"databaseName"});

        formBuilder
                .configFields(CRUD_CONFIGURATION_FIELDS)
                .configFieldSetNames("Crud")
                .configSelectionProvider(databaseSelectionProvider, "database");
    }

    @Override
    protected ClassAccessor prepare(PageInstance pageInstance) {
        Database actualDatabase = getCrudConfiguration().getActualDatabase();
        if (actualDatabase == null) {
            logger.warn("Crud " + crudConfiguration.getName() + " (" + pageInstance.getPath() + ") " +
                        "has an invalid database: " + getCrudConfiguration().getDatabase());
            return null;
        }

        baseTable = getCrudConfiguration().getActualTable();
        if (baseTable == null) {
            logger.warn("Crud " + crudConfiguration.getName() + " (" + pageInstance.getPath() + ") " +
                        "has an invalid table");
            return null;
        }

        session = persistence.getSession(getCrudConfiguration().getDatabase());
        return new TableAccessor(baseTable);
    }

    //**************************************************************************
    // Object loading
    //**************************************************************************

    public void loadObjects() {
        //Se si passano dati sbagliati al criterio restituisco messaggio d'errore
        // ma nessun risultato
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
            objects = QueryUtils.getObjects(session, getBaseQuery(), criteria, this, firstResult, maxResults);
        } catch (ClassCastException e) {
            objects=new ArrayList<Object>();
            logger.warn("Incorrect Field Type", e);
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("incorrect.field.type"));
        }
    }

    protected String getBaseQuery() {
        return getCrudConfiguration().getQuery();
    }

    @Override
    protected Object loadObjectByPrimaryKey(Serializable pkObject) {
        return QueryUtils.getObjectByPk(
                persistence,
                baseTable, pkObject,
                getBaseQuery(), this);
    }

    //**************************************************************************
    // Configuration
    //**************************************************************************

    protected Resolution getConfigurationView() {
        return new ForwardResolution("/m/crud/configure.jsp");
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
