/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.pageactions.m2m.configuration;

import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.QueryUtils;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class ManyToManyConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String oneExpression;
    protected SelectionProviderReference oneSelectionProvider;

    protected SelectionProviderReference manySelectionProvider;

    protected String database;
    protected String query;
    protected String viewType;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Database actualOneDatabase;
    protected Database actualManyDatabase;
    protected Database actualRelationDatabase;
    protected Table actualRelationTable;
    protected Table actualManyTable;
    protected ViewType actualViewType;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ManyToManyConfiguration.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public ManyToManyConfiguration() {}

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init(Application application) {
        assert viewType != null;
        assert database != null;
        assert query != null;
        assert manySelectionProvider != null;

        actualRelationDatabase = DatabaseLogic.findDatabaseByName(application.getModel(), database);
        if(actualRelationDatabase != null) {
            actualRelationTable = QueryUtils.getTableFromQueryString(actualRelationDatabase, query);

            manySelectionProvider.init(actualRelationTable);
            String manyDatabaseName = manySelectionProvider.getActualSelectionProvider().getToDatabase();
            actualManyDatabase =
                DatabaseLogic.findDatabaseByName(application.getModel(), manyDatabaseName);
            actualManyTable = manySelectionProvider.getActualSelectionProvider().getToTable();

            if(oneSelectionProvider != null) {
                oneSelectionProvider.init(actualRelationTable);
                String oneDatabaseName = oneSelectionProvider.getActualSelectionProvider().getToDatabase();
                actualOneDatabase =
                    DatabaseLogic.findDatabaseByName(application.getModel(), oneDatabaseName);
            }
        } else {
            throw new Error("Relation database " + database + " not found");
        }
        actualViewType = ViewType.valueOf(viewType);
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

    @Required
    @Multiline
    @XmlAttribute(required = true)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Required
    @XmlAttribute(required = true)
    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    @XmlAttribute(required = false)
    public String getOneExpression() {
        return oneExpression;
    }

    public void setOneExpression(String oneExpression) {
        this.oneExpression = oneExpression;
    }

    @XmlElement(name = "one", required = false)
    public SelectionProviderReference getOneSelectionProvider() {
        return oneSelectionProvider;
    }

    public void setOneSelectionProvider(SelectionProviderReference oneSelectionProvider) {
        this.oneSelectionProvider = oneSelectionProvider;
    }

    @Required
    @XmlElement(name = "many", required = true)
    public SelectionProviderReference getManySelectionProvider() {
        return manySelectionProvider;
    }

    public void setManySelectionProvider(SelectionProviderReference manySelectionProvider) {
        this.manySelectionProvider = manySelectionProvider;
    }

    public Database getActualOneDatabase() {
        return actualOneDatabase;
    }

    public Database getActualManyDatabase() {
        return actualManyDatabase;
    }

    public ViewType getActualViewType() {
        return actualViewType;
    }

    public Database getActualRelationDatabase() {
        return actualRelationDatabase;
    }

    public Table getActualRelationTable() {
        return actualRelationTable;
    }

    public Table getActualManyTable() {
        return actualManyTable;
    }
}
