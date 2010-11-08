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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Database implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final List<Schema> schemas;

    protected String databaseName;

    
    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LogUtil.getLogger(Database.class);


    //**************************************************************************
    // Constructors
    //**************************************************************************
    public Database() {
        this.schemas = new ArrayList<Schema>();
    }

    public Database(String databaseName) {
        this();
        this.databaseName = databaseName;
    }

    //**************************************************************************
    // DatamodelObject implementation
    //**************************************************************************

    public String getQualifiedName() {
        return databaseName;
    }

    public void reset() {
        for (Schema schema : schemas) {
            schema.reset();
        }
    }

    public void init(Model model) {
        for (Schema schema : schemas) {
            schema.init(model);
        }
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    @XmlAttribute(required = true)
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<Schema> getSchemas() {
        return schemas;
    }

    //**************************************************************************
    // Get all objects of a certain kind
    //**************************************************************************

    public List<Table> getAllTables() {
        List<Table> result = new ArrayList<Table>();
        for (Schema schema : schemas) {
            for (Table table : schema.getTables()) {
                result.add(table);
            }
        }
        return result;
    }

    public List<Column> getAllColumns() {
        List<Column> result = new ArrayList<Column>();
        for (Schema schema : schemas) {
            for (Table table : schema.getTables()) {
                for (Column column : table.getColumns()) {
                    result.add(column);
                }
            }
        }
        return result;
    }

    public List<ForeignKey> getAllForeignKeys() {
        List<ForeignKey> result = new ArrayList<ForeignKey>();
        for (Schema schema : schemas) {
            for (Table table : schema.getTables()) {
                for (ForeignKey foreignKey : table.getForeignKeys()) {
                    result.add(foreignKey);
                }
            }
        }
        return result;
    }


    //**************************************************************************
    // Search objects of a certain kind
    //**************************************************************************

    public Schema findSchemaByQualifiedName(String qualifiedSchemaName) {
        int lastDot = qualifiedSchemaName.lastIndexOf(".");
        String schemaName = qualifiedSchemaName.substring(lastDot + 1);
        for (Schema schema : schemas) {
            if (schema.getSchemaName().equals(schemaName)) {
                return schema;
            }
        }
        LogUtil.fineMF(logger, "Schema not found: {0}", qualifiedSchemaName);
        return null;
    }

    public Table findTableByQualifiedName(String qualifiedTableName) {
        int lastDot = qualifiedTableName.lastIndexOf(".");
        String qualifiedSchemaName = qualifiedTableName.substring(0, lastDot);
        String tableName = qualifiedTableName.substring(lastDot + 1);
        Schema schema = findSchemaByQualifiedName(qualifiedSchemaName);
        if (schema != null) {
            for (Table table : schema.getTables()) {
                if (table.getTableName().equals(tableName)) {
                    return table;
                }
            }
        }
        LogUtil.fineMF(logger, "Table not found: {0}", qualifiedTableName);
        return null;
    }

    public Column findColumnByQualifiedName(String qualifiedColumnName) {
        int lastDot = qualifiedColumnName.lastIndexOf(".");
        String qualifiedTableName = qualifiedColumnName.substring(0, lastDot);
        String columnName = qualifiedColumnName.substring(lastDot + 1);
        Table table = findTableByQualifiedName(qualifiedTableName);
        if (table != null) {
            for (Column column : table.getColumns()) {
                if (column.getColumnName().equals(columnName)) {
                    return column;
                }
            }
        }
        LogUtil.fineMF(logger, "Column not found: {0}", qualifiedColumnName);
        return null;
    }

    //**************************************************************************
    // toString() override
    //**************************************************************************

    @Override
    public String toString() {
        return MessageFormat.format("database {0}", getQualifiedName());
    }

    
}
