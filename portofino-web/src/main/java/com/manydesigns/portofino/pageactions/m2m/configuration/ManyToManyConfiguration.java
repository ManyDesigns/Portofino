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

package com.manydesigns.portofino.pageactions.m2m.configuration;

import com.manydesigns.elements.annotations.Required;
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
public class ManyToManyConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    //Source
    protected String oneDatabase;
    protected String oneQuery;
    protected String oneExpression;
    protected String onePropertyName;
    //Target
    protected String manyDatabase;
    protected String manyQuery;
    protected String manyPropertyName;

    protected String relationDatabase;
    protected String relationQuery;
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
        assert relationDatabase != null;
        assert relationQuery != null;
        assert onePropertyName != null;
        assert manyPropertyName != null;
        assert manyQuery != null;

        actualRelationDatabase = DatabaseLogic.findDatabaseByName(application.getModel(), relationDatabase);
        if(actualRelationDatabase != null) {
            actualRelationTable = QueryUtils.getTableFromQueryString(actualRelationDatabase, relationQuery);
        }
        if(oneDatabase != null) {
            actualOneDatabase = DatabaseLogic.findDatabaseByName(application.getModel(), oneDatabase);
        } else {
            actualOneDatabase = actualRelationTable.getSchema().getDatabase();
        }
        if(manyDatabase != null) {
            actualManyDatabase = DatabaseLogic.findDatabaseByName(application.getModel(), manyDatabase);
        } else {
            actualManyDatabase = actualRelationTable.getSchema().getDatabase();
        }
        actualManyTable = QueryUtils.getTableFromQueryString(actualManyDatabase, manyQuery);
        actualViewType = ViewType.valueOf(viewType);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlAttribute(required = false)
    public String getOneDatabase() {
        return oneDatabase;
    }

    public void setOneDatabase(String oneDatabase) {
        this.oneDatabase = oneDatabase;
    }

    @XmlAttribute(required = false)
    public String getManyDatabase() {
        return manyDatabase;
    }

    public void setManyDatabase(String manyDatabase) {
        this.manyDatabase = manyDatabase;
    }

    @Required
    @XmlAttribute(required = true)
    public String getRelationDatabase() {
        return relationDatabase;
    }

    public void setRelationDatabase(String relationDatabase) {
        this.relationDatabase = relationDatabase;
    }

    @Required
    @XmlAttribute(required = true)
    public String getRelationQuery() {
        return relationQuery;
    }

    public void setRelationQuery(String relationQuery) {
        this.relationQuery = relationQuery;
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
    public String getOneQuery() {
        return oneQuery;
    }

    public void setOneQuery(String oneQuery) {
        this.oneQuery = oneQuery;
    }

    @Required
    @XmlAttribute(required = true)
    public String getManyQuery() {
        return manyQuery;
    }

    public void setManyQuery(String manyQuery) {
        this.manyQuery = manyQuery;
    }

    @Required
    @XmlAttribute(required = true)
    public String getManyPropertyName() {
        return manyPropertyName;
    }

    public void setManyPropertyName(String manyPropertyName) {
        this.manyPropertyName = manyPropertyName;
    }

    @Required
    @XmlAttribute(required = true)
    public String getOnePropertyName() {
        return onePropertyName;
    }

    public void setOnePropertyName(String onePropertyName) {
        this.onePropertyName = onePropertyName;
    }

    @XmlAttribute(required = false)
    public String getOneExpression() {
        return oneExpression;
    }

    public void setOneExpression(String oneExpression) {
        this.oneExpression = oneExpression;
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
