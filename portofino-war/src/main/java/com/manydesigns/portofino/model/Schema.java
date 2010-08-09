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

package com.manydesigns.portofino.model;

import com.manydesigns.elements.logging.LogUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Schema {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String databaseName;
    protected String schemaName;
    protected final List<Table> tables;

    public static final Logger logger = LogUtil.getLogger(Schema.class);


    //**************************************************************************
    // Constructors
    //**************************************************************************
    public Schema(String databaseName, String schemaName) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tables = new ArrayList<Table>();
    }

    //**************************************************************************
    // Getters/setter
    //**************************************************************************

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public List<Table> getTables() {
        return tables;
    }


    public String getQualifiedName() {
        return MessageFormat.format("{0}.{1}", databaseName, schemaName);
    }

        //**************************************************************************
    // Get all objects of a certain kind
    //**************************************************************************

    public List<Column> getAllColumns() {
        List<Column> result = new ArrayList<Column>();
        for (Table table : tables) {
            for (Column column : table.getColumns()) {
                result.add(column);
            }
        }
        return result;
    }

    //**************************************************************************
    // Search objects of a certain kind
    //**************************************************************************

    public Table findTableByQualifiedName(String qualifiedTableName) {
        int lastDot = qualifiedTableName.lastIndexOf(".");
        String tableName = qualifiedTableName.substring(lastDot + 1);
        for (Table table : tables) {
            if (table.getTableName().equals(tableName)) {
                return table;
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

    public Relationship findOneToManyRelationship(String qualifiedTableName,
                                                  String relationshipName) {
        Table table = findTableByQualifiedName(qualifiedTableName);
        if (table != null) {
            for (Relationship relationship : table.getOneToManyRelationships()) {
                if (relationship.getRelationshipName().equals(relationshipName)) {
                    return relationship;
                }
            }
        }
        LogUtil.fineMF(logger, "Relationship not found: {0}", relationshipName);
        return null;
    }
}
