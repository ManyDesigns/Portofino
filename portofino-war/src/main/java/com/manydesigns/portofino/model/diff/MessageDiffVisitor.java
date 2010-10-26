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

    protected ModelAnnotation sourceModelAnnotation;
    protected ModelAnnotation targetModelAnnotation;

    public MessageDiffVisitor() {
        super();
        messages = new ArrayList<String>();
    }

    @Override
    public void visitDatabase(DatabaseDiff databaseDiff) {
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

            super.visitDatabase(databaseDiff);
        }
    }

    @Override
    public void visitSchema(SchemaDiff schemaDiff) {
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

            super.visitSchema(schemaDiff);
        }
    }

    @Override
    public void visitTable(TableDiff tableDiff) {
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

            super.visitTable(tableDiff);
        }
    }

    @Override
    public void visitTableAnnotations(ModelAnnotationDiff modelAnnotationDiff) {
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

            super.visitTableAnnotations(modelAnnotationDiff);
        }
    }

    @Override
    public void visitColumn(ColumnDiff columnDiff) {
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

            super.visitColumn(columnDiff);
        }
    }

    @Override
    public void visitColumnAnnotations(ModelAnnotationDiff modelAnnotationDiff) {
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

            super.visitTableAnnotations(modelAnnotationDiff);
        }
    }

    @Override
    public void visitPrimaryKey(PrimaryKeyDiff primaryKeyDiff) {
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

            super.visitPrimaryKey(primaryKeyDiff);
        }
    }


    //--------------------------------------------------------------------------
    // Getter/setter
    //--------------------------------------------------------------------------

    public List<String> getMessages() {
        return messages;
    }

}
