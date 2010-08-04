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

package com.manydesigns.portofino.model.diff;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.model.Column;
import com.manydesigns.portofino.model.Database;
import com.manydesigns.portofino.model.Schema;
import com.manydesigns.portofino.model.Table;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ModelDiff extends ArrayList<String> {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final Logger logger = LogUtil.getLogger(ModelDiff.class);


    public ModelDiff() {
    }

    public void diff(Database database1, Database database2) {
        String databaseName1 = database1.getDatabaseName();
        String databaseName2 = database2.getDatabaseName();
        if (!databaseName1.equals(databaseName2)) {
            addDifference("Database names {0} / {1} are different",
                    databaseName1, databaseName2);
        }

        Set<String> schemaNames = new HashSet<String>();
        extractSchemaNames(database1, schemaNames);
        extractSchemaNames(database2, schemaNames);
        List<String> sortedSchemaNames = new ArrayList<String>(schemaNames);
        Collections.sort(sortedSchemaNames);

        List<String> commonSchemaNames = new ArrayList<String>();

        for (String schemaName : sortedSchemaNames) {
            Schema schema1 = database1.findSchemaByQualifiedName(schemaName);
            Schema schema2 = database2.findSchemaByQualifiedName(schemaName);
            if (schema1 == null) {
                addDifference("Model 1 does not contain schema: {0}.{1}",
                        databaseName1, schemaName);
            } else if (schema2 == null) {
                addDifference("Model 2 does not contain schema: {0}.{1}",
                        databaseName2, schemaName);
            } else {
                commonSchemaNames.add(schemaName);
            }
        }

        for (String schemaName : commonSchemaNames) {
            Schema schema1 = database1.findSchemaByQualifiedName(schemaName);
            Schema schema2 = database2.findSchemaByQualifiedName(schemaName);
            diff(schema1, schema2);
        }
    }

    private void diff(Schema schema1, Schema schema2) {
        String databaseName1 = schema1.getDatabaseName();
        String databaseName2 = schema2.getDatabaseName();
        String schemaName1 = schema1.getSchemaName();
        String schemaName2 = schema2.getSchemaName();
        if (!schemaName1.equals(schemaName2)) {
            addDifference("Schema names {0}.{1} / {2}.{3} are different",
                    databaseName1, schemaName1, databaseName2, schemaName2);
        }

        Set<String> tableNames = new HashSet<String>();
        extractTableNames(schema1, tableNames);
        extractTableNames(schema2, tableNames);
        List<String> sortedTableNames = new ArrayList<String>(tableNames);
        Collections.sort(sortedTableNames);

        List<String> commonTableNames = new ArrayList<String>();

        for (String tableName : sortedTableNames) {
            Table table1 = schema1.findTableByQualifiedName(tableName);
            Table table2 = schema2.findTableByQualifiedName(tableName);
            if (table1 == null) {
                addDifference("Model 1 does not contain table: {0}.{1}.{2}",
                        databaseName1, schemaName1, tableName);
            } else if (table2 == null) {
                addDifference("Model 2 does not contain table: {0}.{1}.{2}",
                        databaseName2, schemaName2, tableName);
            } else {
                commonTableNames.add(tableName);
            }
        }

        for (String tableName : commonTableNames) {
            Table table1 = schema1.findTableByQualifiedName(tableName);
            Table table2 = schema2.findTableByQualifiedName(tableName);
            diff(table1, table2);
        }
    }

    private void diff(Table table1, Table table2) {
        String databaseName1 = table1.getDatabaseName();
        String databaseName2 = table2.getDatabaseName();
        String schemaName1 = table1.getSchemaName();
        String schemaName2 = table2.getSchemaName();
        String tableName1 = table1.getTableName();
        String tableName2 = table2.getTableName();
        if (!schemaName1.equals(schemaName2)) {
            addDifference("Table names {0}.{1}.{3} / {4}.{5}.{6} are different",
                    databaseName1, schemaName1, tableName1,
                    databaseName2, schemaName2, tableName2);
        }

        Set<String> columnNames = new HashSet<String>();
        extractColumnNames(table1, columnNames);
        extractColumnNames(table2, columnNames);
        List<String> sortedColumnNames = new ArrayList<String>(columnNames);
        Collections.sort(sortedColumnNames);

        List<String> commonColumnNames = new ArrayList<String>();

        for (String columnName : sortedColumnNames) {
            Column column1 = table1.findColumnByName(columnName);
            Column column2 = table2.findColumnByName(columnName);
            if (column1 == null) {
                addDifference("Model 1 does not contain column: {0}.{1}.{2}.{3}",
                        databaseName1, schemaName1, tableName1, columnName);
            } else if (column2 == null) {
                addDifference("Model 2 does not contain column: {0}.{1}.{2}.{3}",
                        databaseName2, schemaName2, tableName2, columnName);
            } else {
                commonColumnNames.add(columnName);
            }
        }

        for (String columnName : commonColumnNames) {
            Column column1 = table1.findColumnByName(columnName);
            Column column2 = table2.findColumnByName(columnName);
//            diff(column1, column2);
        }
    }

    private void addDifference(String format, Object... args) {
        String message = MessageFormat.format(format, args);
        add(message);
        logger.fine(message);
    }

    private void extractSchemaNames(Database database, Set<String> schemaNames) {
        for (Schema schema : database.getSchemas()) {
            schemaNames.add(schema.getSchemaName());
        }
    }

    private void extractTableNames(Schema schema, Set<String> tableNames) {
        for (Table table : schema.getTables()) {
            tableNames.add(table.getTableName());
        }
    }

    private void extractColumnNames(Table table, Set<String> columnNames) {
        for (Column column : table.getColumns()) {
            columnNames.add(column.getColumnName());
        }
    }

}
