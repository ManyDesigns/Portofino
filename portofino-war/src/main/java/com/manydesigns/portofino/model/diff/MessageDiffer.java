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

import org.apache.commons.lang.ObjectUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class MessageDiffer extends AbstractDiffer {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final List<String> messages;

    public MessageDiffer() {
        super();
        messages = new ArrayList<String>();
    }

    //--------------------------------------------------------------------------
    // Databases
    //--------------------------------------------------------------------------

    public void diffDatabaseSourceNull(DatabaseDiff databaseDiff) {
        String difference = MessageFormat.format(
                "Source does not contain database: {0}",
                targetDatabase.getDatabaseName());
        messages.add(difference);
    }
    
    public void diffDatabaseTargetNull(DatabaseDiff databaseDiff) {
        String difference = MessageFormat.format(
                "Target does not contain database: {0}",
                sourceDatabase.getDatabaseName());
        messages.add(difference);
    }

    public void diffDatabaseSourceTarget(DatabaseDiff databaseDiff) {
        if (!ObjectUtils.equals(sourceDatabase.getDatabaseName(),
                targetDatabase.getDatabaseName())) {
            String difference = MessageFormat.format(
                    "Database names {0} / {1} are different",
                    sourceDatabase.getDatabaseName(),
                    targetDatabase.getDatabaseName());
            messages.add(difference);
        }
        diffDatabaseChildren(databaseDiff);
    }


    //--------------------------------------------------------------------------
    // Schemas
    //--------------------------------------------------------------------------

    public void diffSchemaSourceNull(SchemaDiff schemaDiff) {
        String difference = MessageFormat.format(
                "Source does not contain schema: {0}.{1}",
                sourceDatabase.getDatabaseName(),
                targetSchema.getSchemaName());
        messages.add(difference);
    }

    public void diffSchemaTargetNull(SchemaDiff schemaDiff) {
        String difference = MessageFormat.format(
                "Target does not contain schema: {0}.{1}",
                targetDatabase.getDatabaseName(),
                sourceSchema.getSchemaName());
        messages.add(difference);
    }

    public void diffSchemaSourceTarget(SchemaDiff schemaDiff) {
        if (!ObjectUtils.equals(sourceSchema.getSchemaName(),
                targetSchema.getSchemaName())) {
            String difference = MessageFormat.format(
                    "Schema names {0} / {1} are different",
                    sourceSchema.getQualifiedName(),
                    targetSchema.getQualifiedName());
            messages.add(difference);
        }

        diffSchemaChildren(schemaDiff);
    }

    //--------------------------------------------------------------------------
    // Tables
    //--------------------------------------------------------------------------

    public void diffTableSourceNull(TableDiff tableDiff) {
        String difference = MessageFormat.format(
                "Source does not contain table: {0}.{1}",
                sourceSchema.getQualifiedName(),
                targetTable.getTableName());
        messages.add(difference);
    }

    public void diffTableTargetNull(TableDiff tableDiff) {
        String difference = MessageFormat.format(
                "Target does not contain table: {0}.{1}",
                targetSchema.getQualifiedName(),
                sourceTable.getTableName());
        messages.add(difference);
    }

    public void diffTableSourceTarget(TableDiff tableDiff) {
        if (!ObjectUtils.equals(sourceTable.getTableName(),
                targetTable.getTableName())) {
            String difference = MessageFormat.format(
                    "Table names {0} / {1} are different",
                    sourceTable.getQualifiedName(),
                    targetTable.getQualifiedName());
            messages.add(difference);
        }
        diffTableChildren(tableDiff);
    }

    //--------------------------------------------------------------------------
    // Table annotations
    //--------------------------------------------------------------------------

    public void diffTableAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
        String difference = MessageFormat.format(
                "Source table {0} does not contain annotation of type: {1}",
                sourceTable.getQualifiedName(),
                targetAnnotation.getType());
        messages.add(difference);
    }

    public void diffTableAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        String difference = MessageFormat.format(
                "Target table {0} does not contain annotation of type: {1}",
                targetTable.getQualifiedName(),
                sourceAnnotation.getType());
        messages.add(difference);
    }

    public void diffTableAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
        if (!ObjectUtils.equals(sourceAnnotation.getType(),
                targetAnnotation.getType())) {
            String difference = MessageFormat.format(
                    "Annotation types {0} / {1} are different",
                    sourceAnnotation.getType(),
                    targetAnnotation.getType());
            messages.add(difference);
        }
        diffTableAnnotationChildren(modelAnnotationDiff);
    }

    //--------------------------------------------------------------------------
    // Columns
    //--------------------------------------------------------------------------

    public void diffColumnSourceNull(ColumnDiff columnDiff) {
        String difference = MessageFormat.format(
                "Source does not contain column: {0}.{1}",
                sourceTable.getQualifiedName(),
                targetColumn.getColumnName());
        messages.add(difference);
    }

    public void diffColumnTargetNull(ColumnDiff columnDiff) {
        String difference = MessageFormat.format(
                "Target does not contain column: {0}.{1}",
                targetTable.getQualifiedName(),
                sourceColumn.getColumnName());
        messages.add(difference);
    }

    public void diffColumnSourceTarget(ColumnDiff columnDiff) {
        if (!ObjectUtils.equals(sourceColumn.getColumnName(),
                targetColumn.getColumnName())) {
            String difference = MessageFormat.format(
                    "Column names {0} / {1} are different",
                    sourceColumn.getQualifiedName(),
                    targetColumn.getQualifiedName());
            messages.add(difference);
        }
        diffColumnChildren(columnDiff);
    }


    //--------------------------------------------------------------------------
    // Column annotations
    //--------------------------------------------------------------------------

    public void diffColumnAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
        String difference = MessageFormat.format(
                "Source column {0} does not contain annotation of type: {1}",
                sourceColumn.getQualifiedName(),
                targetAnnotation.getType());
        messages.add(difference);
    }

    public void diffColumnAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        String difference = MessageFormat.format(
                "Target column {0} does not contain annotation of type: {1}",
                targetColumn.getQualifiedName(),
                sourceAnnotation.getType());
        messages.add(difference);
    }

    public void diffColumnAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
        if (!ObjectUtils.equals(sourceAnnotation.getType(),
                targetAnnotation.getType())) {
            String difference = MessageFormat.format(
                    "Annotation types {0} / {1} are different",
                    sourceAnnotation.getType(),
                    targetAnnotation.getType());
            messages.add(difference);
        }
        diffColumnAnnotationChildren(modelAnnotationDiff);
    }

    //--------------------------------------------------------------------------
    // Primary keys
    //--------------------------------------------------------------------------

    public void diffPrimaryKeySourceNull(PrimaryKeyDiff primaryKeyDiff) {
        String difference = MessageFormat.format(
                "Source table {0} does not contain primary key: {1}",
                sourceTable.getQualifiedName(),
                targetPrimaryKey.getPrimaryKeyName());
        messages.add(difference);
    }

    public void diffPrimaryKeyTargetNull(PrimaryKeyDiff primaryKeyDiff) {
        String difference = MessageFormat.format(
                "Target table {0} does not contain primary key: {1}",
                targetTable.getQualifiedName(),
                sourcePrimaryKey.getPrimaryKeyName());
        messages.add(difference);
    }

    public void diffPrimaryKeySourceTarget(PrimaryKeyDiff primaryKeyDiff) {
        if (!ObjectUtils.equals(sourcePrimaryKey.getPrimaryKeyName(),
                targetPrimaryKey.getPrimaryKeyName())) {
            String difference = MessageFormat.format(
                    "Primary key names {0} / {1} are different",
                    sourcePrimaryKey.getPrimaryKeyName(),
                    targetPrimaryKey.getPrimaryKeyName());
            messages.add(difference);
        }
        diffPrimaryKeyChildren(primaryKeyDiff);
    }

    //--------------------------------------------------------------------------
    // Primary key columns
    //--------------------------------------------------------------------------

    public void diffPrimaryKeyColumnSourceNull(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        String difference = MessageFormat.format(
                "Source table {0} primary key {1} does not contain column: {2}",
                sourceTable.getQualifiedName(),
                sourcePrimaryKey.getPrimaryKeyName(),
                targetPrimaryKeyColumn.getColumnName());
        messages.add(difference);
    }

    public void diffPrimaryKeyColumnTargetNull(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        String difference = MessageFormat.format(
                "Target table {0} primary key {1} does not contain column: {2}",
                targetTable.getQualifiedName(),
                targetPrimaryKey.getPrimaryKeyName(),
                sourcePrimaryKeyColumn.getColumnName());
        messages.add(difference);
    }

    public void diffPrimaryKeyColumnSourceTarget(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        if (!ObjectUtils.equals(sourcePrimaryKeyColumn.getColumnName(),
                targetPrimaryKeyColumn.getColumnName())) {
            String difference = MessageFormat.format(
                    "Primary key column names {0} / {1} are different",
                    sourcePrimaryKeyColumn.getColumnName(),
                    targetPrimaryKeyColumn.getColumnName());
            messages.add(difference);
        }
        diffPrimaryKeyColumnChildren(primaryKeyColumnDiff);
    }


    //--------------------------------------------------------------------------
    // Foreign keys
    //--------------------------------------------------------------------------

    public void diffForeignKeySourceNull(ForeignKeyDiff foreignKeyDiff) {
        String difference = MessageFormat.format(
                "Source table {0} does not contain foreign key: {1}",
                sourceTable.getQualifiedName(),
                targetForeignKey.getForeignKeyName());
        messages.add(difference);
    }

    public void diffForeignKeyTargetNull(ForeignKeyDiff foreignKeyDiff) {
        String difference = MessageFormat.format(
                "Target table {0} does not contain foreign key: {1}",
                targetTable.getQualifiedName(),
                sourceForeignKey.getForeignKeyName());
        messages.add(difference);
    }

    public void diffForeignKeySourceTarget(ForeignKeyDiff foreignKeyDiff) {
        if (!ObjectUtils.equals(sourceForeignKey.getForeignKeyName(),
                targetForeignKey.getForeignKeyName())) {
            String difference = MessageFormat.format(
                    "Foreign key names {0} / {1} are different",
                    sourceForeignKey.getForeignKeyName(),
                    targetForeignKey.getForeignKeyName());
            messages.add(difference);
        }

        diffForeignKeyChildren(foreignKeyDiff);
    }

    //--------------------------------------------------------------------------
    // References
    //--------------------------------------------------------------------------

    public void diffReferenceSourceNull(ReferenceDiff referenceDiff) {
        String difference = MessageFormat.format(
                "Source table {0} foreign key: {1} does not contain reference: {2}->{3}",
                sourceTable.getQualifiedName(),
                sourceForeignKey.getForeignKeyName(),
                targetReference.getFromColumn(),
                targetReference.getToColumn());
        messages.add(difference);
    }

    public void diffReferenceTargetNull(ReferenceDiff referenceDiff) {
        String difference = MessageFormat.format(
                "Target table {0} foreign key: {1} does not contain reference: {2}->{3}",
                targetTable.getQualifiedName(),
                targetForeignKey.getForeignKeyName(),
                sourceReference.getFromColumn(),
                sourceReference.getToColumn());
        messages.add(difference);
    }

    public void diffReferenceSourceTarget(ReferenceDiff referenceDiff) {
        if (!ObjectUtils.equals(sourceReference.getFromColumn(),
                targetReference.getFromColumn())) {
            String difference = MessageFormat.format(
                    "Reference from column names {0} / {1} are different",
                    sourceReference.getFromColumn(),
                    targetReference.getFromColumn());
            messages.add(difference);
        }

        if (!ObjectUtils.equals(sourceReference.getToColumn(),
                targetReference.getToColumn())) {
            String difference = MessageFormat.format(
                    "Reference to column names {0} / {1} are different",
                    sourceReference.getToColumn(),
                    targetReference.getToColumn());
            messages.add(difference);
        }
        diffReferenceChildren(referenceDiff);
    }


    //--------------------------------------------------------------------------
    // Foreign key annotations
    //--------------------------------------------------------------------------

    public void diffForeignKeyAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
        String difference = MessageFormat.format(
                "Source foreign key {0} does not contain annotation of type: {1}",
                sourceForeignKey.getForeignKeyName(),
                targetAnnotation.getType());
        messages.add(difference);
    }

    public void diffForeignKeyAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        String difference = MessageFormat.format(
                "Target foreign key {0} does not contain annotation of type: {1}",
                targetForeignKey.getForeignKeyName(),
                sourceAnnotation.getType());
        messages.add(difference);
    }

    public void diffForeignKeyAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
        if (!ObjectUtils.equals(sourceAnnotation.getType(),
                targetAnnotation.getType())) {
            String difference = MessageFormat.format(
                    "Annotation types {0} / {1} are different",
                    sourceAnnotation.getType(),
                    targetAnnotation.getType());
            messages.add(difference);
        }
        diffForeignKeyAnnotationChildren(modelAnnotationDiff);
    }

    //--------------------------------------------------------------------------
    // Getter/setter
    //--------------------------------------------------------------------------

    public List<String> getMessages() {
        return messages;
    }

}
