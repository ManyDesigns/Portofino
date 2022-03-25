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
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.ForeignKey;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.persistence.*;
import com.manydesigns.portofino.reflection.TableAccessor;
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
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
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
public class CrudAction<T> extends AbstractCrudAction<T> {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    @Autowired
    public Persistence persistence;

    protected SingleTableQueryCollection collection;

    public static final Logger logger = LoggerFactory.getLogger(CrudAction.class);

    @Override
    public long getTotalSearchRecords() {
        return collection.count(this);
    }

    @Override
    protected void commitTransaction() {
        collection.getSession().getTransaction().commit();
    }

    @Override
    protected void doSave(T object) {
        try {
            collection.save(object);
        } catch(ConstraintViolationException e) {
            logger.warn("Constraint violation in save", e);
            throw new RuntimeException(violatedConstraintMessage(e));
        }
    }

    @Override
    protected void doUpdate(T object) {
        try {
            collection.update(object);
        } catch(ConstraintViolationException e) {
            logger.warn("Constraint violation in update", e);
            throw new RuntimeException(violatedConstraintMessage(e));
        }
    }

    protected String violatedConstraintMessage(ConstraintViolationException e) {
        return ElementsThreadLocals.getText("save.failed.because.constraint.violated", e.getConstraintName());
    }

    @Override
    protected void doDelete(T object) {
        collection.delete(object);
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

        Table baseTable = getCrudConfiguration().getActualTable();
        if (baseTable == null) {
            String message =
                    "Crud " + crudConfiguration.getName() + " (" + actionInstance.getPath() + ") " +
                    "refers to an invalid table.";
            logger.warn(message);
            RequestMessages.addErrorMessage(message);
            return null;
        }
        collection = getCollection(baseTable);
        return collection.getClassAccessor();
    }

    @NotNull
    protected SingleTableQueryCollection getCollection(Table baseTable) {
        return new SingleTableQueryCollection(persistence, baseTable, getBaseQuery());
    }

    @Override
    public CrudAction<T> init() {
        super.init();
        if(getCrudConfiguration() != null && getCrudConfiguration().getActualDatabase() != null) {
            selectionProviderSupport = createSelectionProviderSupport();
            selectionProviderSupport.setup();
        }
        return this;
    }

    public CrudConfiguration getCrudConfiguration() {
        return (CrudConfiguration) crudConfiguration;
    }

    //**************************************************************************
    // Object loading
    //**************************************************************************

    @NotNull
    protected IdStrategy getIdStrategy(ClassAccessor classAccessor, ClassAccessor innerAccessor) {
        Class<? extends IdStrategy> idStrategyClass = ((TableAccessor) innerAccessor).getIdStrategy().getClass();
        try {
            return idStrategyClass.getConstructor(ClassAccessor.class).newInstance(classAccessor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<T> loadObjects() {
        try {
            TableCriteria criteria = setupCriteria();
            collection = collection.where(criteria);
            objects = (List<T>) collection.load(firstResult, maxResults, this);
        } catch (ClassCastException e) {
            objects = new ArrayList<>();
            logger.warn("Incorrect Field Type", e);
            RequestMessages.addWarningMessage(ElementsThreadLocals.getText("incorrect.field.type"));
        }
        return objects;
    }

    @NotNull
    protected TableCriteria setupCriteria() {
        TableCriteria criteria = new TableCriteria(collection.getTable());
        if(searchForm != null) {
            searchForm.configureCriteria(criteria);
        }
        applySorting(criteria);
        return criteria;
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
    protected T loadObjectByPrimaryKey(Object pkObject) {
        return (T) collection.load(pkObject, this);
    }

}
