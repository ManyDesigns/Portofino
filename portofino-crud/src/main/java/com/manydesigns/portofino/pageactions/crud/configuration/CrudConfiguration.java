/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.crud.configuration;

import com.manydesigns.elements.annotations.CssClass;
import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.util.BootstrapSizes;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.ConfigurationWithDefaults;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/

@XmlRootElement(name = "configuration")
@XmlAccessorType(value = XmlAccessType.NONE)
public class CrudConfiguration implements PageActionConfiguration, ConfigurationWithDefaults {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";
    

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<CrudProperty> properties;
    protected final List<SelectionProviderReference> selectionProviders;

    protected String name;
    protected String database;
    protected String query;
    protected String searchTitle;
    protected String createTitle;
    protected String readTitle;
    protected String editTitle;
    protected String variable;

    protected boolean largeResultSet;

    protected Integer rowsPerPage;

    @Inject(DatabaseModule.PERSISTENCE)
    public Persistence persistence;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Table actualTable;
    protected Database actualDatabase;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public CrudConfiguration() {
        properties = new ArrayList<CrudProperty>();
        selectionProviders = new ArrayList<SelectionProviderReference>();
    }

    public CrudConfiguration(String name, String database, String query,
                             String searchTitle, String createTitle,
                             String readTitle, String editTitle) {
        this();
        this.name = name;
        this.database = database;
        this.query = query;
        this.searchTitle = searchTitle;
        this.createTitle = createTitle;
        this.readTitle = readTitle;
        this.editTitle = editTitle;
    }

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init() {
        actualDatabase = DatabaseLogic.findDatabaseByName(persistence.getModel(), database);
        if(actualDatabase != null) {
            actualTable = QueryUtils.getTableFromQueryString(actualDatabase, query);
        }
        for (CrudProperty property : properties) {
            property.init(persistence.getModel());
        }
        //TODO gestire table == null
        for(SelectionProviderReference ref : selectionProviders) {
            ref.init(getActualTable());
        }
    }

    public void setupDefaults() {
        rowsPerPage = 10;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlElementWrapper(name="properties")
    @XmlElement(name="property",type=CrudProperty.class)
    public List<CrudProperty> getProperties() {
        return properties;
    }

    @LabelI18N("name")
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(required = true)
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Database getActualDatabase() {
        return actualDatabase;
    }

    public void setActualDatabase(Database actualDatabase) {
        this.actualDatabase = actualDatabase;
    }

    @Multiline
    @CssClass(BootstrapSizes.BLOCK_LEVEL)
    @XmlAttribute(required = true)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @CssClass(BootstrapSizes.BLOCK_LEVEL)
    @XmlAttribute(required = false)
    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    @CssClass(BootstrapSizes.BLOCK_LEVEL)
    @XmlAttribute(required = false)
    public String getCreateTitle() {
        return createTitle;
    }

    public void setCreateTitle(String createTitle) {
        this.createTitle = createTitle;
    }

    @CssClass(BootstrapSizes.BLOCK_LEVEL)
    @XmlAttribute(required = false)
    public String getReadTitle() {
        return readTitle;
    }

    public void setReadTitle(String readTitle) {
        this.readTitle = readTitle;
    }

    @CssClass(BootstrapSizes.BLOCK_LEVEL)
    @XmlAttribute(required = false)
    public String getEditTitle() {
        return editTitle;
    }

    public void setEditTitle(String editTitle) {
        this.editTitle = editTitle;
    }

    @XmlAttribute(required = false)
    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getActualVariable() {
        return variable != null ? variable : name;
    }

    @XmlElementWrapper(name="selectionProviders")
    @XmlElements({
          @XmlElement(name="selectionProvider",type=SelectionProviderReference.class)
    })
    public List<SelectionProviderReference> getSelectionProviders() {
        return selectionProviders;
    }

    @XmlAttribute(required = true)
    public boolean isLargeResultSet() {
        return largeResultSet;
    }

    public void setLargeResultSet(boolean largeResultSet) {
        this.largeResultSet = largeResultSet;
    }

    public Table getActualTable() {
        return actualTable;
    }

    @CssClass(BootstrapSizes.SMALL)
    @XmlAttribute(required = false)
    public Integer getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

}
