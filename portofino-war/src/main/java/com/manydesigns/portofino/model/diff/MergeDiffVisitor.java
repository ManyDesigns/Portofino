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
public class MergeDiffVisitor extends BaseDiffVisitor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected Database sourceDatabase;
    protected Database targetDatabase;

    protected Schema sourceSchema;
    protected Schema targetSchema;

    protected Table sourceTable;
    protected Table targetTable;

    protected Column sourceColumn;
    protected Column targetColumn;

    protected PrimaryKey sourcePrimaryKey;
    protected PrimaryKey targetPrimaryKey;

    protected PrimaryKeyColumn sourcePrimaryKeyColumn;
    protected PrimaryKeyColumn targetPrimaryKeyColumn;

    protected ForeignKey sourceForeignKey;
    protected ForeignKey targetForeignKey;

    protected Reference sourceReference;
    protected Reference targetReference;

    protected ModelAnnotation sourceModelAnnotation;
    protected ModelAnnotation targetModelAnnotation;

    public MergeDiffVisitor() {}

    @Override
    public void visitDatabaseDiff(DatabaseDiff databaseDiff) {
        sourceDatabase = databaseDiff.getSourceDatabase();
        targetDatabase = databaseDiff.getTargetDatabase();

        if (sourceDatabase == null) {
            // do nothing
        } else if (targetDatabase == null) {
            targetDatabase = new Database(sourceDatabase.getDatabaseName());
        } else {
            targetDatabase.setDatabaseName(sourceDatabase.getDatabaseName());
        }
        super.visitDatabaseDiff(databaseDiff);
    }

    @Override
    public void visitSchemaDiff(SchemaDiff schemaDiff) {
        sourceSchema = schemaDiff.getSourceSchema();
        targetSchema = schemaDiff.getTargetSchema();

        if (sourceSchema == null) {
            // TODO: do nothing?
        } else if (targetSchema == null) {
            targetSchema = new Schema(targetDatabase, sourceSchema.getSchemaName());
            targetDatabase.getSchemas().add(targetSchema);
        } else {
            targetSchema.setSchemaName(sourceSchema.getSchemaName());
        }
        super.visitSchemaDiff(schemaDiff);
    }

    @Override
    public void visitTableDiff(TableDiff tableDiff) {
        sourceTable = tableDiff.getSourceTable();
        targetTable = tableDiff.getTargetTable();

        if (sourceTable == null) {
            // TODO: do nothing?
        } else if (targetTable == null) {
            targetTable = new Table(targetSchema, sourceTable.getTableName());
            mergeOptionalTableProperties();
            targetSchema.getTables().add(targetTable);
        } else {
            mergeOptionalTableProperties();
        }
        super.visitTableDiff(tableDiff);
    }

    protected void mergeOptionalTableProperties() {
        String sourceJavaClassName = sourceTable.getJavaClassName();
        if (sourceJavaClassName != null) {
            targetTable.setJavaClassName(sourceJavaClassName);
        }

        // TODO: isM2m() ha bisogno di una logica a 3 stati
        targetTable.setM2m(sourceTable.isM2m());
    }

    @Override
    public void visitTableAnnotationDiff(ModelAnnotationDiff modelAnnotationDiff) {
        sourceModelAnnotation =
                modelAnnotationDiff.getSourceModelAnnotation();
        targetModelAnnotation =
                modelAnnotationDiff.getTargetModelAnnotation();

        if (sourceModelAnnotation == null) {
            // TODO: do nothing?
        } else if (targetModelAnnotation == null) {
            targetModelAnnotation =
                    new ModelAnnotation(sourceModelAnnotation.getType());
            targetTable.getModelAnnotations().add(targetModelAnnotation);
        } else {
            targetModelAnnotation.setType(sourceModelAnnotation.getType());
        }

        super.visitTableAnnotationDiff(modelAnnotationDiff);
    }

    @Override
    public void visitColumnDiff(ColumnDiff columnDiff) {
        sourceColumn = columnDiff.getSourceColumn();
        targetColumn = columnDiff.getTargetColumn();

        if (sourceColumn == null) {
            // TODO: do nothing?
        } else if (targetColumn == null) {
            targetColumn = new Column(targetTable,
                    sourceColumn.getColumnName(),
                    sourceColumn.getColumnType(),
                    sourceColumn.isNullable(),
                    sourceColumn.isAutoincrement(),
                    sourceColumn.getLength(),
                    sourceColumn.getScale(),
                    sourceColumn.isSearchable());
            targetTable.getColumns().add(targetColumn);
        } else {
            targetColumn.setColumnName(sourceColumn.getColumnName());
            targetColumn.setColumnType(sourceColumn.getColumnType());
            targetColumn.setNullable(sourceColumn.isNullable());
            targetColumn.setAutoincrement(sourceColumn.isAutoincrement());
            targetColumn.setLength(sourceColumn.getLength());
            targetColumn.setScale(sourceColumn.getScale());
            targetColumn.setSearchable(sourceColumn.isSearchable());
            mergeOptionalColumnProperties();
        }

        super.visitColumnDiff(columnDiff);
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

    @Override
    public void visitColumnAnnotationDiff(ModelAnnotationDiff modelAnnotationDiff) {
        sourceModelAnnotation =
                modelAnnotationDiff.getSourceModelAnnotation();
        targetModelAnnotation =
                modelAnnotationDiff.getTargetModelAnnotation();

        if (sourceModelAnnotation == null) {
            // TODO: do nothing?
        } else if (targetModelAnnotation == null) {
            targetModelAnnotation =
                    new ModelAnnotation(sourceModelAnnotation.getType());
            targetColumn.getModelAnnotations().add(targetModelAnnotation);
        } else {
            targetModelAnnotation.setType(sourceModelAnnotation.getType());
        }

        super.visitColumnAnnotationDiff(modelAnnotationDiff);
    }

    @Override
    public void visitPrimaryKeyDiff(PrimaryKeyDiff primaryKeyDiff) {
        sourcePrimaryKey = primaryKeyDiff.getSourcePrimaryKey();
        targetPrimaryKey = primaryKeyDiff.getTargetPrimaryKey();

        if (sourcePrimaryKey == null) {
            // TODO: do nothing?
        } else if (targetPrimaryKey == null) {
            targetPrimaryKey = new PrimaryKey(
                    targetTable, sourcePrimaryKey.getPrimaryKeyName());
            targetTable.setPrimaryKey(targetPrimaryKey);
        } else {
            targetPrimaryKey.setPrimaryKeyName(
                    sourcePrimaryKey.getPrimaryKeyName());
            mergeOptionalPrimaryKeyProperties();

        }

        super.visitPrimaryKeyDiff(primaryKeyDiff);
    }

    private void mergeOptionalPrimaryKeyProperties() {
        String sourceClassname = sourcePrimaryKey.getClassName();
        if (sourceClassname != null) {
            targetPrimaryKey.setClassName(sourceClassname);
        }
    }

    @Override
    public void visitPrimaryKeyColumnDiff(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        super.visitPrimaryKeyColumnDiff(primaryKeyColumnDiff);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void visitForeignKeyDiff(ForeignKeyDiff foreignKeyDiff) {
        super.visitForeignKeyDiff(foreignKeyDiff);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void visitReferenceDiff(ReferenceDiff referenceDiff) {
        super.visitReferenceDiff(referenceDiff);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void visitForeignKeyAnnotationDiff(ModelAnnotationDiff modelAnnotationDiff) {
        super.visitForeignKeyAnnotationDiff(modelAnnotationDiff);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
