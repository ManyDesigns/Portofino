/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.model.selectionproviders;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.xml.XmlAttribute;
import com.manydesigns.portofino.xml.XmlCollection;

import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ModelSelectionProvider implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final List<SelectionProperty> selectionProperties;

    protected String name;
    protected String database;
    protected String sql;
    protected String hql;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public ModelSelectionProvider() {
        selectionProperties = new ArrayList<SelectionProperty>();
    }

    public ModelSelectionProvider(String name, String database) {
        this();
        this.name = name;
        this.database = database;
    }

    public ModelSelectionProvider(String name, String database,
                                  String sql, String hql) {
        this(name, database);
        this.sql = sql;
        this.hql = hql;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void reset() {}

    public void init(Model model) {}

    public String getQualifiedName() {
        return name;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlCollection(itemClasses = SelectionProperty.class, itemNames = "selectionProperty")
    public List<SelectionProperty> getSelectionProperties() {
        return selectionProperties;
    }

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

    @XmlAttribute(required = false)
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @XmlAttribute(required = false)
    public String getHql() {
        return hql;
    }

    public void setHql(String hql) {
        this.hql = hql;
    }
}
