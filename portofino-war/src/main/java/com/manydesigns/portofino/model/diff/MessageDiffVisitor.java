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
import org.apache.commons.lang.ObjectUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class MessageDiffVisitor extends BaseDiffVisitor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final List<String> messages;

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

    public MessageDiffVisitor() {
        super();
        messages = new ArrayList<String>();
    }

    @Override
    public void visitDatabaseDiff(DatabaseDiff databaseDiff) {
        sourceDatabase = databaseDiff.getSourceDatabase();
        targetDatabase = databaseDiff.getTargetDatabase();

        if (sourceDatabase == null) {
            String difference = MessageFormat.format(
                    "Source does not contain database: {0}",
                    targetDatabase.getDatabaseName());
            messages.add(difference);
        } else if (targetDatabase == null) {
            String difference = MessageFormat.format(
                    "Target does not contain database: {0}",
                    sourceDatabase.getDatabaseName());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourceDatabase.getDatabaseName(),
                    targetDatabase.getDatabaseName())) {
                String difference = MessageFormat.format(
                        "Database names {0} / {1} are different",
                        sourceDatabase.getDatabaseName(),
                        targetDatabase.getDatabaseName());
                messages.add(difference);
            }

            super.visitDatabaseDiff(databaseDiff);
        }
    }

    @Override
    public void visitSchemaDiff(SchemaDiff schemaDiff) {
        sourceSchema = schemaDiff.getSourceSchema();
        targetSchema = schemaDiff.getTargetSchema();

        if (sourceSchema == null) {
            String difference = MessageFormat.format(
                    "Source does not contain schema: {0}.{1}",
                    sourceDatabase.getDatabaseName(),
                    targetSchema.getSchemaName());
            messages.add(difference);
        } else if (targetSchema == null) {
            String difference = MessageFormat.format(
                    "Target does not contain schema: {0}.{1}",
                    targetDatabase.getDatabaseName(),
                    sourceSchema.getSchemaName());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourceSchema.getSchemaName(),
                    targetSchema.getSchemaName())) {
                String difference = MessageFormat.format(
                        "Schema names {0} / {1} are different",
                        sourceSchema.getQualifiedName(),
                        targetSchema.getQualifiedName());
                messages.add(difference);
            }

            super.visitSchemaDiff(schemaDiff);
        }
    }

    @Override
    public void visitTableDiff(TableDiff tableDiff) {
        sourceTable = tableDiff.getSourceTable();
        targetTable = tableDiff.getTargetTable();

        if (sourceTable == null) {
            String difference = MessageFormat.format(
                    "Source does not contain table: {0}.{1}",
                    sourceSchema.getQualifiedName(),
                    targetTable.getTableName());
            messages.add(difference);
        } else if (targetTable == null) {
            String difference = MessageFormat.format(
                    "Target does not contain table: {0}.{1}",
                    targetSchema.getQualifiedName(),
                    sourceTable.getTableName());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourceTable.getTableName(),
                    targetTable.getTableName())) {
                String difference = MessageFormat.format(
                        "Table names {0} / {1} are different",
                        sourceTable.getQualifiedName(),
                        targetTable.getQualifiedName());
                messages.add(difference);
            }

            super.visitTableDiff(tableDiff);
        }
    }

    @Override
    public void visitTableAnnotationDiff(ModelAnnotationDiff modelAnnotationDiff) {
        sourceModelAnnotation =
                modelAnnotationDiff.getSourceModelAnnotation();
        targetModelAnnotation =
                modelAnnotationDiff.getTargetModelAnnotation();

        if (sourceModelAnnotation == null) {
            String difference = MessageFormat.format(
                    "Source table {0} does not contain annotation of type: {1}",
                    sourceTable.getQualifiedName(),
                    targetModelAnnotation.getType());
            messages.add(difference);
        } else if (targetModelAnnotation == null) {
            String difference = MessageFormat.format(
                    "Target table {0} does not contain annotation of type: {1}",
                    targetTable.getQualifiedName(),
                    sourceModelAnnotation.getType());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourceModelAnnotation.getType(),
                    targetModelAnnotation.getType())) {
                String difference = MessageFormat.format(
                        "Annotation types {0} / {1} are different",
                        sourceModelAnnotation.getType(),
                        targetModelAnnotation.getType());
                messages.add(difference);
            }

            super.visitTableAnnotationDiff(modelAnnotationDiff);
        }
    }

    @Override
    public void visitColumnDiff(ColumnDiff columnDiff) {
        sourceColumn = columnDiff.getSourceColumn();
        targetColumn = columnDiff.getTargetColumn();

        if (sourceColumn == null) {
            String difference = MessageFormat.format(
                    "Source does not contain column: {0}.{1}",
                    sourceTable.getQualifiedName(),
                    targetColumn.getColumnName());
            messages.add(difference);
        } else if (targetColumn == null) {
            String difference = MessageFormat.format(
                    "Target does not contain column: {0}.{1}",
                    targetTable.getQualifiedName(),
                    sourceColumn.getColumnName());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourceColumn.getColumnName(),
                    targetColumn.getColumnName())) {
                String difference = MessageFormat.format(
                        "Column names {0} / {1} are different",
                        sourceColumn.getQualifiedName(),
                        targetColumn.getQualifiedName());
                messages.add(difference);
            }

            super.visitColumnDiff(columnDiff);
        }
    }

    @Override
    public void visitColumnAnnotationDiff(ModelAnnotationDiff modelAnnotationDiff) {
        sourceModelAnnotation =
                modelAnnotationDiff.getSourceModelAnnotation();
        targetModelAnnotation =
                modelAnnotationDiff.getTargetModelAnnotation();

        if (sourceModelAnnotation == null) {
            String difference = MessageFormat.format(
                    "Source column {0} does not contain annotation of type: {1}",
                    sourceColumn.getQualifiedName(),
                    targetModelAnnotation.getType());
            messages.add(difference);
        } else if (targetModelAnnotation == null) {
            String difference = MessageFormat.format(
                    "Target column {0} does not contain annotation of type: {1}",
                    targetColumn.getQualifiedName(),
                    sourceModelAnnotation.getType());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourceModelAnnotation.getType(),
                    targetModelAnnotation.getType())) {
                String difference = MessageFormat.format(
                        "Annotation types {0} / {1} are different",
                        sourceModelAnnotation.getType(),
                        targetModelAnnotation.getType());
                messages.add(difference);
            }

            super.visitColumnAnnotationDiff(modelAnnotationDiff);
        }
    }

    @Override
    public void visitPrimaryKeyDiff(PrimaryKeyDiff primaryKeyDiff) {
        sourcePrimaryKey = primaryKeyDiff.getSourcePrimaryKey();
        targetPrimaryKey = primaryKeyDiff.getTargetPrimaryKey();

        if (sourcePrimaryKey == null) {
            String difference = MessageFormat.format(
                    "Source table {0} does not contain primary key: {1}",
                    sourceTable.getQualifiedName(),
                    targetPrimaryKey.getPrimaryKeyName());
            messages.add(difference);
        } else if (targetPrimaryKey == null) {
            String difference = MessageFormat.format(
                    "Target table {0} does not contain primary key: {1}",
                    targetTable.getQualifiedName(),
                    sourcePrimaryKey.getPrimaryKeyName());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourcePrimaryKey.getPrimaryKeyName(),
                    targetPrimaryKey.getPrimaryKeyName())) {
                String difference = MessageFormat.format(
                        "Primary key names {0} / {1} are different",
                        sourcePrimaryKey.getPrimaryKeyName(),
                        targetPrimaryKey.getPrimaryKeyName());
                messages.add(difference);
            }

            super.visitPrimaryKeyDiff(primaryKeyDiff);
        }
    }

    @Override
    public void visitPrimaryKeyColumnDiff(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        sourcePrimaryKeyColumn =
                primaryKeyColumnDiff.getSourcePrimaryKeyColumn();
        targetPrimaryKeyColumn =
                primaryKeyColumnDiff.getTargetPrimaryKeyColumn();

        if (sourcePrimaryKeyColumn == null) {
            String difference = MessageFormat.format(
                    "Source table {0} primary key {1} does not contain column: {2}",
                    sourceTable.getQualifiedName(),
                    sourcePrimaryKey.getPrimaryKeyName(),
                    targetPrimaryKeyColumn.getColumnName());
            messages.add(difference);
        } else if (targetPrimaryKeyColumn == null) {
            String difference = MessageFormat.format(
                    "Target table {0} primary key {1} does not contain column: {2}",
                    targetTable.getQualifiedName(),
                    targetPrimaryKey.getPrimaryKeyName(),
                    sourcePrimaryKeyColumn.getColumnName());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourcePrimaryKeyColumn.getColumnName(),
                    targetPrimaryKeyColumn.getColumnName())) {
                String difference = MessageFormat.format(
                        "Primary key names {0} / {1} are different",
                        sourcePrimaryKeyColumn.getColumnName(),
                        targetPrimaryKeyColumn.getColumnName());
                messages.add(difference);
            }

            super.visitPrimaryKeyColumnDiff(primaryKeyColumnDiff);
        }
    }

    @Override
    public void visitForeignKeyDiff(ForeignKeyDiff foreignKeyDiff) {
        sourceForeignKey =
                foreignKeyDiff.getSourceForeignKey();
        targetForeignKey =
                foreignKeyDiff.getTargetForeignKey();

        if (sourceForeignKey == null) {
            String difference = MessageFormat.format(
                    "Source table {0} does not contain foreign key: {1}",
                    sourceTable.getQualifiedName(),
                    targetForeignKey.getForeignKeyName());
            messages.add(difference);
        } else if (targetForeignKey == null) {
            String difference = MessageFormat.format(
                    "Target table {0} does not contain foreign key: {1}",
                    targetTable.getQualifiedName(),
                    sourceForeignKey.getForeignKeyName());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourceForeignKey.getForeignKeyName(),
                    targetForeignKey.getForeignKeyName())) {
                String difference = MessageFormat.format(
                        "Foreign key names {0} / {1} are different",
                        sourceForeignKey.getForeignKeyName(),
                        targetForeignKey.getForeignKeyName());
                messages.add(difference);
            }

            super.visitForeignKeyDiff(foreignKeyDiff);
        }
    }

    @Override
    public void visitReferenceDiff(ReferenceDiff referenceDiff) {
        sourceReference =
                referenceDiff.getSourceReference();
        targetReference =
                referenceDiff.getTargetReference();

        if (sourceReference == null) {
            String difference = MessageFormat.format(
                    "Source table {0} foreign key: {1} does not contain reference: {2}->{3}",
                    sourceTable.getQualifiedName(),
                    sourceForeignKey.getForeignKeyName(),
                    targetReference.getFromColumnName(),
                    targetReference.getToColumnName());
            messages.add(difference);
        } else if (targetReference == null) {
            String difference = MessageFormat.format(
                    "Target table {0} foreign key: {1} does not contain reference: {2}->{3}",
                    targetTable.getQualifiedName(),
                    targetForeignKey.getForeignKeyName(),
                    sourceReference.getFromColumnName(),
                    sourceReference.getToColumnName());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourceReference.getFromColumnName(),
                    targetReference.getFromColumnName())) {
                String difference = MessageFormat.format(
                        "Reference from column names {0} / {1} are different",
                        sourceReference.getFromColumnName(),
                        targetReference.getFromColumnName());
                messages.add(difference);
            }

            if (!ObjectUtils.equals(sourceReference.getToColumnName(),
                    targetReference.getToColumnName())) {
                String difference = MessageFormat.format(
                        "Reference to column names {0} / {1} are different",
                        sourceReference.getToColumnName(),
                        targetReference.getToColumnName());
                messages.add(difference);
            }

            super.visitReferenceDiff(referenceDiff);
        }
    }

    @Override
    public void visitForeignKeyAnnotationDiff(ModelAnnotationDiff modelAnnotationDiff) {
        sourceModelAnnotation =
                modelAnnotationDiff.getSourceModelAnnotation();
        targetModelAnnotation =
                modelAnnotationDiff.getTargetModelAnnotation();

        if (sourceModelAnnotation == null) {
            String difference = MessageFormat.format(
                    "Source foreign key {0} does not contain annotation of type: {1}",
                    sourceForeignKey.getForeignKeyName(),
                    targetModelAnnotation.getType());
            messages.add(difference);
        } else if (targetModelAnnotation == null) {
            String difference = MessageFormat.format(
                    "Target foreign key {0} does not contain annotation of type: {1}",
                    targetForeignKey.getForeignKeyName(),
                    sourceModelAnnotation.getType());
            messages.add(difference);
        } else {
            if (!ObjectUtils.equals(sourceModelAnnotation.getType(),
                    targetModelAnnotation.getType())) {
                String difference = MessageFormat.format(
                        "Annotation types {0} / {1} are different",
                        sourceModelAnnotation.getType(),
                        targetModelAnnotation.getType());
                messages.add(difference);
            }

            super.visitForeignKeyAnnotationDiff(modelAnnotationDiff);
        }
    }
    
    //--------------------------------------------------------------------------
    // Getter/setter
    //--------------------------------------------------------------------------

    public List<String> getMessages() {
        return messages;
    }

}
