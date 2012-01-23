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

package com.manydesigns.portofino.pageactions.crud.configuration;

import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.portofino.application.QueryUtils;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.model.datamodel.DataModelLogic;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.datamodel.Table;

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
public class CrudConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";
    

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

    public void init(Application application) {
        actualDatabase = DataModelLogic.findDatabaseByName(application.getModel(), database);
        if(actualDatabase != null) {
            actualTable = QueryUtils.getTableFromQueryString(actualDatabase, query);
        }
        for (CrudProperty property : properties) {
            property.init(application);
        }
        //TODO gestire table == null
        for(SelectionProviderReference ref : selectionProviders) {
            ref.init(getActualTable());
        }
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlElementWrapper(name="properties")
    @XmlElement(name="property",type=CrudProperty.class)
    public List<CrudProperty> getProperties() {
        return properties;
    }

    @LabelI18N("com.manydesigns.portofino.model.pages.crud.Crud.name")
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
    @XmlAttribute(required = true)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @XmlAttribute(required = false)
    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    @XmlAttribute(required = false)
    public String getCreateTitle() {
        return createTitle;
    }

    public void setCreateTitle(String createTitle) {
        this.createTitle = createTitle;
    }

    @XmlAttribute(required = false)
    public String getReadTitle() {
        return readTitle;
    }

    public void setReadTitle(String readTitle) {
        this.readTitle = readTitle;
    }

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

    @XmlAttribute(required = false)
    public Integer getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public int getActualRowsPerPage() {
        return getRowsPerPage() != null ? getRowsPerPage() : 10;
    }
}
