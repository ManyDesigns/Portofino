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

package com.manydesigns.portofino.resourceactions.crud.configuration.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manydesigns.elements.annotations.Enabled;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.database.model.Database;
import com.manydesigns.portofino.database.model.DatabaseLogic;
import com.manydesigns.portofino.database.model.Table;
import com.manydesigns.portofino.model.annotations.Transient;
import com.manydesigns.portofino.resourceactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;

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
@XmlType(name = "databaseConfiguration", propOrder = {"database","query","selectionProviders","subscriptionSupport"})
@XmlAccessorType(value = XmlAccessType.NONE)
@JsonIgnoreProperties({"persistence", "actualTable", "actualDatabase"})
public class CrudConfiguration extends com.manydesigns.portofino.resourceactions.crud.configuration.CrudConfiguration {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<SelectionProviderReference> selectionProviders;

    protected String database;
    protected String query;
    protected String subscriptionSupport;

    @Autowired
    @Enabled(false)
    @Transient
    public Persistence persistence;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    @Enabled(false)
    @Transient
    protected Table actualTable;
    @Enabled(false)
    @Transient
    protected Database actualDatabase;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public CrudConfiguration() {
        super();
        selectionProviders = new ArrayList<>();
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
        super.init();
        actualDatabase = DatabaseLogic.findDatabaseByName(persistence.getDatabases(), database);
        if(actualDatabase != null && query != null) { //Query can be null if the user hasn't got permission to see it
            actualTable = QueryUtils.getTableFromQueryString(actualDatabase, query);
        }
        for (CrudProperty property : properties) {
            property.init(persistence);
        }
        if(actualTable != null) {
            for (SelectionProviderReference ref : selectionProviders) {
                ref.init(actualTable);
            }
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
    @XmlAttribute(required = true)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
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
    @Enabled(false)
    public List<SelectionProviderReference> getSelectionProviders() {
        return selectionProviders;
    }

    public void setSelectionProviders(List<SelectionProviderReference> selectionProviders) {
        this.selectionProviders.clear();
        this.selectionProviders.addAll(selectionProviders);
    }

    @XmlAttribute
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public String getSubscriptionSupport() {
        return subscriptionSupport;
    }

    public void setSubscriptionSupport(String subscriptionSupport) {
        this.subscriptionSupport = subscriptionSupport;
    }


    public Table getActualTable() {
        return actualTable;
    }

}
