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

package com.manydesigns.portofino.model.pages;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Multiline;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.Database;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class ChartPage extends Page {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String name;
    protected String type;
    protected String legend;
    protected String database;
    protected String query;
    protected String urlExpression;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Database actualDatabase;

    //**************************************************************************
    // Constructors
    //**************************************************************************


    public ChartPage() {
        super();
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void reset() {
        super.reset();
        actualDatabase = null;
    }

    @Override
    public void init(Model model) {
        super.init(model);
        assert name != null;
        assert type != null;
        assert title != null;
        assert legend != null;
        assert database != null;
        assert query != null;
        assert urlExpression != null;

        actualDatabase = DataModelLogic.findDatabaseByName(model, database);
    }


    public String getQualifiedName() {
        return null;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************
    @Required
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Required
    @XmlAttribute(required = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Required
    @XmlAttribute(required = true)
    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    @Required
    @XmlAttribute(required = true)
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Required
    @Label("SQL Query")
    @Multiline
    @XmlAttribute(required = true)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Label("URL expression")
    @XmlAttribute(required = true)
    public String getUrlExpression() {
        return urlExpression;
    }

    public void setUrlExpression(String urlExpression) {
        this.urlExpression = urlExpression;
    }

    public Database getActualDatabase() {
        return actualDatabase;
    }

    public void setActualDatabase(Database actualDatabase) {
        this.actualDatabase = actualDatabase;
    }

}
