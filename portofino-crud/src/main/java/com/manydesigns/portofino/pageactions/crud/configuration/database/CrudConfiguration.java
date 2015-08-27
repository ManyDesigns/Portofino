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

package com.manydesigns.portofino.pageactions.crud.configuration.database;

import com.manydesigns.elements.annotations.CssClass;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.util.BootstrapSizes;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
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
@XmlType(name = "databaseConfiguration",propOrder = {"database","query","selectionProviders"})
@XmlAccessorType(value = XmlAccessType.NONE)
public class CrudConfiguration extends com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<SelectionProviderReference> selectionProviders;

    protected String database;
    protected String query;

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
        super();
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

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @Required
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

    @Required
    @Multiline
    @CssClass(BootstrapSizes.FILL_ROW)
    @XmlAttribute(required = true)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @XmlElementWrapper(name="selectionProviders")
    @XmlElements({
          @XmlElement(name="selectionProvider",type=SelectionProviderReference.class)
    })
    public List<SelectionProviderReference> getSelectionProviders() {
        return selectionProviders;
    }

    public Table getActualTable() {
        return actualTable;
    }

}
