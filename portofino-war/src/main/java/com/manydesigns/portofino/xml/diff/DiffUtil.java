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

package com.manydesigns.portofino.xml.diff;

import com.manydesigns.portofino.model.annotations.Annotation;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class DiffUtil {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(DiffUtil.class);

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
            SchemaDiff schemaDiff =
                    diff(sourceSchema, targetSchema);
            result.getSchemaDiffs().add(schemaDiff);
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
            TableDiff tableDiff =
                    diff(sourceTable, targetTable);
            result.getTableDiffs().add(tableDiff);
        }

        return result;
    }

    public static TableDiff diff(Table sourceTable,
                                 Table targetTable) {
        TableDiff result =
                new TableDiff(sourceTable, targetTable);

        // diff columns
        // columns shold preserve their natural order like on the db
        // i.e., NOT sorted alphabetically
        Set<String> columnNames = new LinkedHashSet<String>();
        extractColumnNames(sourceTable, columnNames);
        extractColumnNames(targetTable, columnNames);

        for (String columnName : columnNames) {
            Column sourceColumn = findColumn(sourceTable, columnName);
            Column targetColumn = findColumn(targetTable, columnName);
            ColumnDiff columnDiff = diff(sourceColumn, targetColumn);
            result.getColumnDiffs().add(columnDiff);
        }

        // diff primary key
        PrimaryKeyDiff primaryKeyDiff = result.getPrimaryKeyDiff();
        if (primaryKeyDiff != null) {
            diff(primaryKeyDiff);
        }

        // diff foreign keys
        Set<String> foreignKeyNames = new HashSet<String>();
        extractForeignKeyNames(sourceTable, foreignKeyNames);
        extractForeignKeyNames(targetTable, foreignKeyNames);
        List<String> sortedForeignKeyNames =
                new ArrayList<String>(foreignKeyNames);
        Collections.sort(sortedForeignKeyNames);

        for (String foreignKeyName : sortedForeignKeyNames) {
            ForeignKey sourceForeignKey =
                    findForeignKey(sourceTable, foreignKeyName);
            ForeignKey targetForeignKey =
                    findForeignKey(targetTable, foreignKeyName);
            ForeignKeyDiff foreignKeyDiff =
                    diff(sourceForeignKey, targetForeignKey);
            result.getForeignKeyDiffs().add(foreignKeyDiff);
        }

        // diff table annotations
        Set<String> annotationTypes = new HashSet<String>();
        extractAnnotationTypes(sourceTable, annotationTypes);
        extractAnnotationTypes(targetTable, annotationTypes);
        List<String> sortedAnnotationTypes =
                new ArrayList<String>(annotationTypes);
        Collections.sort(sortedAnnotationTypes);

        for (String annotationType : sortedAnnotationTypes) {
            Annotation sourceAnnotation =
                    findModelAnnotation(sourceTable, annotationType);
            Annotation targetAnnotation =
                    findModelAnnotation(targetTable, annotationType);

            ModelAnnotationDiff modelAnnotationDiff =
                    diff(sourceAnnotation, targetAnnotation);
            result.getModelAnnotationDiffs().add(modelAnnotationDiff);
        }

        return result;
    }

    private static void diff(PrimaryKeyDiff primaryKeyDiff) {
        PrimaryKey sourcePrimaryKey = primaryKeyDiff.getSourcePrimaryKey();
        PrimaryKey targetPrimaryKey = primaryKeyDiff.getTargetPrimaryKey();

        Set<String> pkColumnNames = new HashSet<String>();
        extractPrimaryKeyColumnNames(sourcePrimaryKey, pkColumnNames);
        extractPrimaryKeyColumnNames(targetPrimaryKey, pkColumnNames);
        List<String> sortedPkColumnNames = new ArrayList<String>(pkColumnNames);
        Collections.sort(sortedPkColumnNames);

        for (String pkColumnName : sortedPkColumnNames) {
            PrimaryKeyColumn sourcePkColumn =
                    findPrimaryKeyColumn(sourcePrimaryKey, pkColumnName);
            PrimaryKeyColumn targetPkColumn =
                    findPrimaryKeyColumn(targetPrimaryKey, pkColumnName);
            PrimaryKeyColumnDiff primaryKeyColumnDiff =
                    diff(sourcePkColumn, targetPkColumn);
            primaryKeyDiff.getPrimaryKeyColumnDiffs().add(primaryKeyColumnDiff);
        }
    }

    public static ForeignKeyDiff diff(ForeignKey sourceForeignKey,
                                      ForeignKey targetForeignKey) {
        ForeignKeyDiff result =
                new ForeignKeyDiff(sourceForeignKey, targetForeignKey);

        // diff references
        Set<Pair<String>> columnNamePairs = new HashSet<Pair<String>>();
        extractReferencePairs(sourceForeignKey, columnNamePairs);
        extractReferencePairs(targetForeignKey, columnNamePairs);

        for (Pair<String> columnNamePair : columnNamePairs) {
            Reference sourceReference =
                    findReference(sourceForeignKey, columnNamePair);
            Reference targetReference =
                    findReference(targetForeignKey, columnNamePair);
            ReferenceDiff referenceDiff =
                    diff(sourceReference, targetReference);
            result.getReferenceDiffs().add(referenceDiff);
        }

        // diff table annotations
        Set<String> annotationTypes = new HashSet<String>();
        extractAnnotationTypes(sourceForeignKey, annotationTypes);
        extractAnnotationTypes(sourceForeignKey, annotationTypes);
        List<String> sortedAnnotationTypes =
                new ArrayList<String>(annotationTypes);
        Collections.sort(sortedAnnotationTypes);

        for (String annotationType : sortedAnnotationTypes) {
            Annotation sourceAnnotation =
                    findModelAnnotation(sourceForeignKey, annotationType);
            Annotation targetAnnotation =
                    findModelAnnotation(sourceForeignKey, annotationType);

            ModelAnnotationDiff modelAnnotationDiff =
                    diff(sourceAnnotation, targetAnnotation);
            result.getModelAnnotationDiffs().add(modelAnnotationDiff);
        }

        return result;
    }

    private static ReferenceDiff diff(Reference sourceReference, Reference targetReference) {
        return new ReferenceDiff(sourceReference, targetReference);
    }

    public static PrimaryKeyColumnDiff diff(PrimaryKeyColumn sourcePkColumn,
                                             PrimaryKeyColumn targetPkColumn) {
        return new PrimaryKeyColumnDiff(sourcePkColumn, targetPkColumn);
    }

    public static ColumnDiff diff(Column sourceColumn,
                                  Column targetColumn) {
        ColumnDiff result =
                new ColumnDiff(sourceColumn, targetColumn);

        // diff column annotations
        Set<String> annotationTypes = new HashSet<String>();
        extractAnnotationTypes(sourceColumn, annotationTypes);
        extractAnnotationTypes(targetColumn, annotationTypes);
        List<String> sortedAnnotationTypes =
                new ArrayList<String>(annotationTypes);
        Collections.sort(sortedAnnotationTypes);

        for (String annotationType : sortedAnnotationTypes) {
            Annotation sourceAnnotation =
                    findModelAnnotation(sourceColumn, annotationType);
            Annotation targetAnnotation =
                    findModelAnnotation(targetColumn, annotationType);

            ModelAnnotationDiff modelAnnotationDiff =
                    diff(sourceAnnotation, targetAnnotation);
            result.getModelAnnotationDiffs().add(modelAnnotationDiff);
        }

        return result;
    }

    public static ModelAnnotationDiff diff(Annotation sourceAnnotation,
                                           Annotation targetAnnotation) {
        return new ModelAnnotationDiff(
                sourceAnnotation, targetAnnotation);
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

    public static PrimaryKeyColumn findPrimaryKeyColumn(PrimaryKey primaryKey,
                                                        String columnName) {
        if (primaryKey == null) {
            return null;
        }
        return primaryKey.findPrimaryKeyColumnByName(columnName);
    }

    public static ForeignKey findForeignKey(Table table, String foreignKeyName) {
        if (table == null) {
            return null;
        }
        return table.findForeignKeyByName(foreignKeyName);
    }

    public static Reference findReference(ForeignKey foreignKey,
                                          Pair<String> columnNamePair) {
        if (foreignKey == null) {
            return null;
        }
        return foreignKey.findReferenceByColumnNamePair(columnNamePair);
    }

    public static Annotation findModelAnnotation(Table table,
                                                      String annotationType) {
        if (table == null) {
            return null;
        }
        return table.findModelAnnotationByType(annotationType);
    }

    public static Annotation findModelAnnotation(Column column,
                                                      String annotationType) {
        if (column == null) {
            return null;
        }
        return column.findModelAnnotationByType(annotationType);
    }

    public static Annotation findModelAnnotation(ForeignKey foreignKey,
                                                      String annotationType) {
        if (foreignKey == null) {
            return null;
        }
        return foreignKey.findModelAnnotationByType(annotationType);
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

    public static void extractPrimaryKeyColumnNames(PrimaryKey primaryKey,
                                                    Set<String> columnNames) {
        if (primaryKey == null) {
            return;
        }
        for (PrimaryKeyColumn primaryKeyColumn : primaryKey) {
            columnNames.add(primaryKeyColumn.getColumnName());
        }
    }

    public static void extractForeignKeyNames(Table table,
                                              Set<String> foreignKeyNames) {
        if (table == null) {
            return;
        }
        for (ForeignKey foreignKey : table.getForeignKeys()) {
            foreignKeyNames.add(foreignKey.getForeignKeyName());
        }
    }

    public static void extractReferencePairs(ForeignKey foreignKey,
                                             Set<Pair<String>> columnNamePairs) {
        if (foreignKey == null) {
            return;
        }
        for (Reference reference : foreignKey.getReferences()) {
            Pair<String> columnNamePair =
                    new Pair<String>(reference.getFromColumn(),
                            reference.getToColumn());
            columnNamePairs.add(columnNamePair);
        }
    }


    public static void extractAnnotationTypes(Table table,
                                              Set<String> annotationTypes) {
        if (table == null) {
            return;
        }
        for (Annotation annotation : table.getAnnotations()) {
            annotationTypes.add(annotation.getType());
        }
    }

    public static void extractAnnotationTypes(Column column,
                                              Set<String> annotationTypes) {
        if (column == null) {
            return;
        }
        for (Annotation annotation : column.getAnnotations()) {
            annotationTypes.add(annotation.getType());
        }
    }

    public static void extractAnnotationTypes(ForeignKey foreignKey,
                                              Set<String> annotationTypes) {
        if (foreignKey == null) {
            return;
        }
        for (Annotation annotation : foreignKey.getAnnotations()) {
            annotationTypes.add(annotation.getType());
        }
    }

}
