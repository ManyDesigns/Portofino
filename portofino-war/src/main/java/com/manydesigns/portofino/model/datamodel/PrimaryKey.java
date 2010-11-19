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

package com.manydesigns.portofino.model.datamodel;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.xml.XmlAttribute;
import com.manydesigns.portofino.xml.XmlCollection;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PrimaryKey implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final Table table;
    protected final List<PrimaryKeyColumn> primaryKeyColumns;

    protected String primaryKeyName;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected final List<Column> columns;

    public static final Logger logger = LogUtil.getLogger(Table.class);

    //**************************************************************************
    // Constructors and wire up
    //**************************************************************************

    public PrimaryKey(Table table) {
        this.table = table;
        primaryKeyColumns = new ArrayList<PrimaryKeyColumn>();
        columns = new ArrayList<Column>();
    }

    public PrimaryKey(Table table, String primaryKeyName) {
        this(table);
        this.primaryKeyName = primaryKeyName;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public String getQualifiedName() {
        return MessageFormat.format("{0}#{1}",
                table.getQualifiedName(), primaryKeyName);
    }

    public void reset() {
        columns.clear();

        for (PrimaryKeyColumn pkc : primaryKeyColumns) {
            pkc.reset();
        }
    }

    public void init(Model model) {
        if (primaryKeyColumns.isEmpty()) {
            throw new Error(MessageFormat.format(
                    "Primary key {0} has no columns",
                    getQualifiedName()));
        }

        for (PrimaryKeyColumn pkc : primaryKeyColumns) {
            pkc.init(model);
            Column column = pkc.getActualColumn();
            columns.add(column);
        }

        if (columns.isEmpty()) {
            LogUtil.warningMF(logger,
                    "Primary key ''{0}'' has no columns", this);
        }
    }

    //**************************************************************************
    // Find methods
    //**************************************************************************

    public PrimaryKeyColumn findPrimaryKeyColumnByName(String columnName) {
        for (PrimaryKeyColumn primaryKeyColumn : primaryKeyColumns) {
            if (primaryKeyColumn.getColumnName().equals(columnName)) {
                return primaryKeyColumn;
            }
        }
        return null;
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public Table getTable() {
        return table;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public String getDatabaseName() {
        return table.getTableName();
    }

    public String getSchemaName() {
        return table.getSchemaName();
    }

    public String getTableName() {
        return table.getTableName();
    }

    @XmlAttribute(required = true)
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    @XmlCollection(itemType = PrimaryKeyColumn.class)
    public List<PrimaryKeyColumn> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    //**************************************************************************
    // toString() override
    //**************************************************************************

    @Override
    public String toString() {
        return MessageFormat.format("primary key {0}", getQualifiedName());
    }
}
