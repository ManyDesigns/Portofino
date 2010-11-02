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

import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class MergeDiffer extends AbstractDiffer {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public MergeDiffer() {
        super();
    }

    //--------------------------------------------------------------------------
    // Databases
    //--------------------------------------------------------------------------

    public void diffDatabaseSourceNull(DatabaseDiff databaseDiff) {
        diffDatabaseChildren(databaseDiff);
    }

    public void diffDatabaseTargetNull(DatabaseDiff databaseDiff) {
        targetDatabase = new Database(sourceDatabase.getDatabaseName());
        diffDatabaseChildren(databaseDiff);
    }

    public void diffDatabaseSourceTarget(DatabaseDiff databaseDiff) {
        targetDatabase.setDatabaseName(sourceDatabase.getDatabaseName());
        diffDatabaseChildren(databaseDiff);
    }

    //--------------------------------------------------------------------------
    // Schemas
    //--------------------------------------------------------------------------

    public void diffSchemaSourceNull(SchemaDiff schemaDiff) {
        diffSchemaChildren(schemaDiff);
    }

    public void diffSchemaTargetNull(SchemaDiff schemaDiff) {
        targetSchema = new Schema(targetDatabase, sourceSchema.getSchemaName());
        targetDatabase.getSchemas().add(targetSchema);
        diffSchemaChildren(schemaDiff);
    }

    public void diffSchemaSourceTarget(SchemaDiff schemaDiff) {
        targetSchema.setSchemaName(sourceSchema.getSchemaName());
        diffSchemaChildren(schemaDiff);
    }

    //--------------------------------------------------------------------------
    // Tables
    //--------------------------------------------------------------------------

    public void diffTableSourceNull(TableDiff tableDiff) {
        diffTableChildren(tableDiff);
    }

    public void diffTableTargetNull(TableDiff tableDiff) {
        targetTable = new Table(targetSchema, sourceTable.getTableName());
        mergeOptionalTableProperties();
        targetSchema.getTables().add(targetTable);
        diffTableChildren(tableDiff);
    }

    public void diffTableSourceTarget(TableDiff tableDiff) {
        mergeOptionalTableProperties();
        diffTableChildren(tableDiff);
    }

    protected void mergeOptionalTableProperties() {
        String sourceJavaClassName = sourceTable.getJavaClassName();
        if (sourceJavaClassName != null) {
            targetTable.setJavaClassName(sourceJavaClassName);
        }

        // TODO: isM2m() ha bisogno di una logica a 3 stati
        targetTable.setM2m(sourceTable.isM2m());
    }

    //--------------------------------------------------------------------------
    // Table annotations
    //--------------------------------------------------------------------------

    public void diffTableAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
        diffTableAnnotationChildren(modelAnnotationDiff);
    }

    public void diffTableAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        targetModelAnnotation =
                new ModelAnnotation(sourceModelAnnotation.getType());
        targetTable.getModelAnnotations().add(targetModelAnnotation);
        diffTableAnnotationChildren(modelAnnotationDiff);
    }

    public void diffTableAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
        targetModelAnnotation.setType(sourceModelAnnotation.getType());
        diffTableAnnotationChildren(modelAnnotationDiff);
    }

    //--------------------------------------------------------------------------
    // Columns
    //--------------------------------------------------------------------------

    public void diffColumnSourceNull(ColumnDiff columnDiff) {
        diffColumnChildren(columnDiff);
    }

    public void diffColumnTargetNull(ColumnDiff columnDiff) {
        targetColumn = new Column(targetTable,
                sourceColumn.getColumnName(),
                sourceColumn.getColumnType(),
                sourceColumn.isNullable(),
                sourceColumn.isAutoincrement(),
                sourceColumn.getLength(),
                sourceColumn.getScale(),
                sourceColumn.isSearchable());
        targetTable.getColumns().add(targetColumn);
        diffColumnChildren(columnDiff);
    }

    public void diffColumnSourceTarget(ColumnDiff columnDiff) {
        targetColumn.setColumnName(sourceColumn.getColumnName());
        targetColumn.setColumnType(sourceColumn.getColumnType());
        targetColumn.setNullable(sourceColumn.isNullable());
        targetColumn.setAutoincrement(sourceColumn.isAutoincrement());
        targetColumn.setLength(sourceColumn.getLength());
        targetColumn.setScale(sourceColumn.getScale());
        targetColumn.setSearchable(sourceColumn.isSearchable());
        mergeOptionalColumnProperties();
        diffColumnChildren(columnDiff);
    }

    protected void mergeOptionalColumnProperties() {
        String sourcePropertyName = sourceColumn.getPropertyName();
        if (sourcePropertyName != null) {
            targetColumn.setPropertyName(sourcePropertyName);
        }

        String sourceJavaTypeName = sourceColumn.getJavaTypeName();
        if (sourceJavaTypeName != null) {
            targetColumn.setJavaTypeName(sourceJavaTypeName);
        }
    }

    //--------------------------------------------------------------------------
    // Column annotations
    //--------------------------------------------------------------------------

    public void diffColumnAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
        diffColumnAnnotationChildren(modelAnnotationDiff);
    }

    public void diffColumnAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        targetModelAnnotation =
                new ModelAnnotation(sourceModelAnnotation.getType());
        targetColumn.getModelAnnotations().add(targetModelAnnotation);
        diffColumnAnnotationChildren(modelAnnotationDiff);
    }

    public void diffColumnAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
        targetModelAnnotation.setType(sourceModelAnnotation.getType());
        diffColumnAnnotationChildren(modelAnnotationDiff);
    }

    //--------------------------------------------------------------------------
    // Primary keys
    //--------------------------------------------------------------------------

    public void diffPrimaryKeySourceNull(PrimaryKeyDiff primaryKeyDiff) {
        diffPrimaryKeyChildren(primaryKeyDiff);
    }

    public void diffPrimaryKeyTargetNull(PrimaryKeyDiff primaryKeyDiff) {
        targetPrimaryKey = new PrimaryKey(
                targetTable, sourcePrimaryKey.getPrimaryKeyName());
        targetTable.setPrimaryKey(targetPrimaryKey);
        diffPrimaryKeyChildren(primaryKeyDiff);
    }

    public void diffPrimaryKeySourceTarget(PrimaryKeyDiff primaryKeyDiff) {
        targetPrimaryKey.setPrimaryKeyName(
                sourcePrimaryKey.getPrimaryKeyName());
        mergeOptionalPrimaryKeyProperties();
        diffPrimaryKeyChildren(primaryKeyDiff);
    }

    private void mergeOptionalPrimaryKeyProperties() {
        String sourceClassname = sourcePrimaryKey.getClassName();
        if (sourceClassname != null) {
            targetPrimaryKey.setClassName(sourceClassname);
        }
    }

    //--------------------------------------------------------------------------
    // Primary key columns
    //--------------------------------------------------------------------------

    public void diffPrimaryKeyColumnSourceNull(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        diffPrimaryKeyColumnChildren(primaryKeyColumnDiff);
    }

    public void diffPrimaryKeyColumnTargetNull(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        diffPrimaryKeyColumnChildren(primaryKeyColumnDiff);
    }

    public void diffPrimaryKeyColumnSourceTarget(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        diffPrimaryKeyColumnChildren(primaryKeyColumnDiff);
    }

    //--------------------------------------------------------------------------
    // Foreign keys
    //--------------------------------------------------------------------------

    public void diffForeignKeySourceNull(ForeignKeyDiff foreignKeyDiff) {
        diffForeignKeyChildren(foreignKeyDiff);
    }

    public void diffForeignKeyTargetNull(ForeignKeyDiff foreignKeyDiff) {
        diffForeignKeyChildren(foreignKeyDiff);
    }

    public void diffForeignKeySourceTarget(ForeignKeyDiff foreignKeyDiff) {
        diffForeignKeyChildren(foreignKeyDiff);
    }

    //--------------------------------------------------------------------------
    // References
    //--------------------------------------------------------------------------

    public void diffReferenceSourceNull(ReferenceDiff referenceDiff) {
        diffReferenceChildren(referenceDiff);
    }

    public void diffReferenceTargetNull(ReferenceDiff referenceDiff) {
        diffReferenceChildren(referenceDiff);
    }

    public void diffReferenceSourceTarget(ReferenceDiff referenceDiff) {
        diffReferenceChildren(referenceDiff);
    }

    //--------------------------------------------------------------------------
    // Foreign key annotations
    //--------------------------------------------------------------------------

    public void diffForeignKeyAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
        diffForeignKeyAnnotationChildren(modelAnnotationDiff);
    }

    public void diffForeignKeyAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        diffForeignKeyAnnotationChildren(modelAnnotationDiff);
    }

    public void diffForeignKeyAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
        diffForeignKeyAnnotationChildren(modelAnnotationDiff);
    }
}
