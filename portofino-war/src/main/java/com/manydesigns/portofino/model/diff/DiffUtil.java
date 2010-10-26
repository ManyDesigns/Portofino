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
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.datamodel.Schema;
import com.manydesigns.portofino.model.datamodel.Table;

import java.util.*;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DiffUtil {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final Logger logger = LogUtil.getLogger(DiffUtil.class);

    //--------------------------------------------------------------------------
    // Compare
    //--------------------------------------------------------------------------

    public static DatabaseDiff diff(Database sourceDatabase,
                                    Database targetDatabase) {
        DatabaseDiff result =
                new DatabaseDiff(sourceDatabase, targetDatabase);

        Set<String> schemaNames = new HashSet<String>();
        extractSchemaNames(sourceDatabase, schemaNames);
        extractSchemaNames(targetDatabase, schemaNames);
        List<String> sortedSchemaNames = new ArrayList<String>(schemaNames);
        Collections.sort(sortedSchemaNames);

        for (String schemaName : sortedSchemaNames) {
            Schema sourceSchema = findSchema(sourceDatabase, schemaName);
            Schema targetSchema = findSchema(targetDatabase, schemaName);
            SchemaDiff schemaComparison =
                    diff(sourceSchema, targetSchema);
            result.getSchemaDiffs().add(schemaComparison);
        }

        return result;
    }

    public static SchemaDiff diff(Schema sourceSchema,
                                  Schema targetSchema) {
        SchemaDiff result =
                new SchemaDiff(sourceSchema, targetSchema);

        Set<String> tableNames = new HashSet<String>();
        extractTableNames(sourceSchema, tableNames);
        extractTableNames(targetSchema, tableNames);
        List<String> sortedTableNames = new ArrayList<String>(tableNames);
        Collections.sort(sortedTableNames);

        for (String tableName : sortedTableNames) {
            Table sourceTable = findTable(sourceSchema, tableName);
            Table targetTable = findTable(targetSchema, tableName);
            TableDiff tableComparison =
                    diff(sourceTable, targetTable);
            result.getTableDiffs().add(tableComparison);
        }

        return result;
    }

    public static TableDiff diff(Table sourceTable,
                                 Table targetTable) {
        TableDiff result =
                new TableDiff(sourceTable, targetTable);

        Set<String> columnNames = new HashSet<String>();
        extractColumnNames(sourceTable, columnNames);
        extractColumnNames(targetTable, columnNames);
        List<String> sortedColumnNames = new ArrayList<String>(columnNames);
        Collections.sort(sortedColumnNames);

        for (String columnName : sortedColumnNames) {
            Column sourceColumn = findColumn(sourceTable, columnName);
            Column targetColumn = findColumn(targetTable, columnName);
            ColumnDiff columnComparison =
                    diff(sourceColumn, targetColumn);
            result.getColumnDiffs().add(columnComparison);
        }

        Set<String> annotationTypes = new HashSet<String>();
        extractAnnotationTypes(sourceTable, annotationTypes);
        extractAnnotationTypes(targetTable, annotationTypes);
        List<String> sortedAnnotationTypes =
                new ArrayList<String>(annotationTypes);
        Collections.sort(sortedAnnotationTypes);

        for (String annotationType : sortedAnnotationTypes) {
            ModelAnnotation sourceModelAnnotation =
                    findModelAnnotation(sourceTable, annotationType);
            ModelAnnotation targetModelAnnotation =
                    findModelAnnotation(sourceTable, annotationType);

            ModelAnnotationDiff modelAnnotationComparison =
                    diff(sourceModelAnnotation, targetModelAnnotation);
            result.getModelAnnotationDiffs().add(modelAnnotationComparison);
        }

        return result;
    }

    public static ColumnDiff diff(Column sourceColumn,
                                  Column targetColumn) {
        ColumnDiff result =
                new ColumnDiff(sourceColumn, targetColumn);
        
        Set<String> annotationTypes = new HashSet<String>();
        extractAnnotationTypes(sourceColumn, annotationTypes);
        extractAnnotationTypes(targetColumn, annotationTypes);
        List<String> sortedAnnotationTypes =
                new ArrayList<String>(annotationTypes);
        Collections.sort(sortedAnnotationTypes);

        for (String annotationType : sortedAnnotationTypes) {
            ModelAnnotation sourceModelAnnotation =
                    findModelAnnotation(sourceColumn, annotationType);
            ModelAnnotation targetModelAnnotation =
                    findModelAnnotation(targetColumn, annotationType);

            ModelAnnotationDiff modelAnnotationComparison =
                    diff(sourceModelAnnotation, targetModelAnnotation);
            result.getModelAnnotationDiffs().add(modelAnnotationComparison);
        }

        return result;
    }

    public static ModelAnnotationDiff diff(ModelAnnotation sourceModelAnnotation,
                                           ModelAnnotation targetModelAnnotation) {
        return new ModelAnnotationDiff(
                sourceModelAnnotation, targetModelAnnotation);
    }

    //--------------------------------------------------------------------------
    // Find
    //--------------------------------------------------------------------------

    public static Schema findSchema(Database database, String schemaName) {
        if (database == null) {
            return null;
        }
        return database.findSchemaByQualifiedName(schemaName);
    }
    
    public static Table findTable(Schema schema, String tableName) {
        if (schema == null) {
            return null;
        }
        return schema.findTableByQualifiedName(tableName);
    }

    public static Column findColumn(Table table, String columnName) {
        if (table == null) {
            return null;
        }
        return table.findColumnByName(columnName);
    }

    public static ModelAnnotation findModelAnnotation(Table table,
                                                      String annotationType) {
        if (table == null) {
            return null;
        }
        return table.findModelAnnotationByType(annotationType);
    }

    public static ModelAnnotation findModelAnnotation(Column column,
                                                      String annotationType) {
        if (column == null) {
            return null;
        }
        return column.findModelAnnotationByType(annotationType);
    }

    //--------------------------------------------------------------------------
    // Extract names
    //--------------------------------------------------------------------------

    public static void extractSchemaNames(Database database,
                                          Set<String> schemaNames) {
        if (database == null) {
            return;
        }
        for (Schema schema : database.getSchemas()) {
            schemaNames.add(schema.getSchemaName());
        }
    }

    public static void extractTableNames(Schema schema,
                                         Set<String> tableNames) {
        if (schema == null) {
            return;
        }
        for (Table table : schema.getTables()) {
            tableNames.add(table.getTableName());
        }
    }

    public static void extractColumnNames(Table table,
                                          Set<String> columnNames) {
        if (table == null) {
            return;
        }
        for (Column column : table.getColumns()) {
            columnNames.add(column.getColumnName());
        }
    }


    public static void extractAnnotationTypes(Table table,
                                              Set<String> annotationTypes) {
        if (table == null) {
            return;
        }
        for (ModelAnnotation modelAnnotation : table.getModelAnnotations()) {
            annotationTypes.add(modelAnnotation.getType());
        }
    }

    public static void extractAnnotationTypes(Column column,
                                              Set<String> annotationTypes) {
        if (column == null) {
            return;
        }
        for (ModelAnnotation modelAnnotation : column.getModelAnnotations()) {
            annotationTypes.add(modelAnnotation.getType());
        }
    }
}
