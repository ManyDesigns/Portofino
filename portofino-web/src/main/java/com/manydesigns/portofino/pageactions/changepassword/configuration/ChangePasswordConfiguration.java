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

package com.manydesigns.portofino.pageactions.changepassword.configuration;

import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.application.QueryUtils;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class ChangePasswordConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String database;
    protected String query;
    protected String property;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Database actualDatabase;
    protected Table actualTable;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ChangePasswordConfiguration.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public ChangePasswordConfiguration() {}

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init(Application application) {
        if(database != null && query != null) {
            actualDatabase = DatabaseLogic.findDatabaseByName(application.getModel(), database);
            if(actualDatabase != null) {
                actualTable = QueryUtils.getTableFromQueryString(actualDatabase, query);
            } else {
                logger.error("Database " + database + " not found");
            }
        }
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlAttribute(required = false)
    @LabelI18N("changepasswordaction.configuration.database")
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Multiline
    @XmlAttribute(required = false)
    @LabelI18N("changepasswordaction.configuration.query")
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @XmlAttribute(required = false)
    @LabelI18N("changepasswordaction.configuration.property")
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Database getActualDatabase() {
        return actualDatabase;
    }

    public Table getActualTable() {
        return actualTable;
    }
}
