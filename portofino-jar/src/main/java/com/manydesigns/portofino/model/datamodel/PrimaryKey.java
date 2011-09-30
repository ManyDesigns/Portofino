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

package com.manydesigns.portofino.model.datamodel;

import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/

@XmlAccessorType(value = XmlAccessType.NONE)
public class PrimaryKey implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<PrimaryKeyColumn> primaryKeyColumns;

    protected Table table;
    protected String primaryKeyName;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected final List<Column> columns;
    protected boolean valid;

    public static final Logger logger = LoggerFactory.getLogger(Table.class);

    //**************************************************************************
    // Constructors and wire up
    //**************************************************************************

    public PrimaryKey() {
        columns = new ArrayList<Column>();
        primaryKeyColumns = new ArrayList<PrimaryKeyColumn>();
    }

    public PrimaryKey(Table table) {
        this();
        this.table = table;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public String getQualifiedName() {
        return MessageFormat.format("{0}#{1}",
                table.getQualifiedName(), primaryKeyName);
    }

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        table = (Table) parent;
    }

    public void reset() {
        columns.clear();
        valid = true;
    }

    public void init() {
        assert table != null;

// Liquibase on MySQL returns null primaryKey name if the name is "PRIMARY"
//        assert primaryKeyName != null;

        if (primaryKeyColumns.isEmpty()) {
            throw new Error(MessageFormat.format(
                    "Primary key {0} has no columns",
                    getQualifiedName()));
        }

    }

    public void link(Model model) {
        for (PrimaryKeyColumn pkc : primaryKeyColumns) {
            Column column = pkc.getActualColumn();
            if (column == null) {
                valid = false;
                logger.warn("Invalid primary key column: {}-{}",
                        getQualifiedName(), pkc.getColumnName());
            } else {
                columns.add(column);
            }
        }

        if (columns.isEmpty()) {
            logger.warn("Primary key '{}' has no columns", this);
        }
    }

    public void visitChildren(ModelVisitor visitor) {
        for (PrimaryKeyColumn pkc : primaryKeyColumns) {
            visitor.visit(pkc);
        }
    }

    //**************************************************************************
    // Find methods
    //**************************************************************************

    public PrimaryKeyColumn findPrimaryKeyColumnByName(String columnName) {
        for (PrimaryKeyColumn primaryKeyColumn : primaryKeyColumns) {
            if (primaryKeyColumn.getColumnName().equalsIgnoreCase(columnName)) {
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

    public void setTable(Table table) {
        this.table = table;
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

    @Required
    @XmlAttribute(required = true)
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    @XmlElement(name="column",type=PrimaryKeyColumn.class)
    public List<PrimaryKeyColumn> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public boolean isValid() {
        return valid;
    }

    //**************************************************************************
    // toString() override
    //**************************************************************************

    @Override
    public String toString() {
        return MessageFormat.format("primary key {0}", getQualifiedName());
    }
}
