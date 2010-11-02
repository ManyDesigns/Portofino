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
import com.manydesigns.portofino.model.datamodel.*;

import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractDiffer {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

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

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger = LogUtil.getLogger(AbstractDiffer.class);

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public AbstractDiffer() {}

    //--------------------------------------------------------------------------
    // Databases
    //--------------------------------------------------------------------------

    public void diffDatabase(DatabaseDiff databaseDiff) {
        sourceDatabase = databaseDiff.getSourceDatabase();
        targetDatabase = databaseDiff.getTargetDatabase();

        if (sourceDatabase == null && targetDatabase == null) {
            diffDatabaseSourceTargetNull(databaseDiff);
        } else if (sourceDatabase == null) {
            diffDatabaseSourceNull(databaseDiff);
        } else if (targetDatabase == null) {
            diffDatabaseTargetNull(databaseDiff);
        } else {
            diffDatabaseSourceTarget(databaseDiff);
        }
    }

    public void diffDatabaseSourceTargetNull(DatabaseDiff databaseDiff) {
        logger.warning("Both souce and target databases are null");
    }
    public abstract void diffDatabaseSourceNull(DatabaseDiff databaseDiff);
    public abstract void diffDatabaseTargetNull(DatabaseDiff databaseDiff);
    public abstract void diffDatabaseSourceTarget(DatabaseDiff databaseDiff);

    public void diffDatabaseChildren(DatabaseDiff databaseDiff) {
        diffSchemas(databaseDiff.getSchemaDiffs());
    }

    //--------------------------------------------------------------------------
    // Schemas
    //--------------------------------------------------------------------------

    public void diffSchemas(List<SchemaDiff> schemaDiffs) {
        for (SchemaDiff schemaComparison : schemaDiffs) {
            diffSchema(schemaComparison);
        }
    }

    public void diffSchema(SchemaDiff schemaDiff) {
        sourceSchema = schemaDiff.getSourceSchema();
        targetSchema = schemaDiff.getTargetSchema();

        if (sourceSchema == null && targetDatabase == null) {
            diffSchemaSourceTargetNull(schemaDiff);
        } else if (sourceSchema == null) {
            diffSchemaSourceNull(schemaDiff);
        } else if (targetSchema == null) {
            diffSchemaTargetNull(schemaDiff);
        } else {
            diffSchemaSourceTarget(schemaDiff);
        }
    }

    public void diffSchemaSourceTargetNull(SchemaDiff schemaDiff) {
        logger.warning("Both source and target schemas are null");
    }
    public abstract void diffSchemaSourceNull(SchemaDiff schemaDiff);
    public abstract void diffSchemaTargetNull(SchemaDiff schemaDiff);
    public abstract void diffSchemaSourceTarget(SchemaDiff schemaDiff);

    public void diffSchemaChildren(SchemaDiff schemaDiff) {
        diffTables(schemaDiff.getTableDiffs());
    }

    //--------------------------------------------------------------------------
    // Tables
    //--------------------------------------------------------------------------

    public void diffTables(List<TableDiff> tableDiffs) {
        for (TableDiff tableComparison : tableDiffs) {
            diffTable(tableComparison);
        }
    }

    public void diffTable(TableDiff tableDiff) {
        sourceTable = tableDiff.getSourceTable();
        targetTable = tableDiff.getTargetTable();

        if (sourceTable == null && targetTable == null) {
            diffTableSourceTargetNull(tableDiff);
        } else if (sourceTable == null) {
            diffTableSourceNull(tableDiff);
        } else if (targetTable == null) {
            diffTableTargetNull(tableDiff);
        } else {
            diffTableSourceTarget(tableDiff);
        }
    }

    public void diffTableSourceTargetNull(TableDiff tableDiff) {
        logger.warning("Both source and target tables are null");
    }
    public abstract void diffTableSourceNull(TableDiff tableDiff);
    public abstract void diffTableTargetNull(TableDiff tableDiff);
    public abstract void diffTableSourceTarget(TableDiff tableDiff);

    public void diffTableChildren(TableDiff tableDiff) {
        diffTableAnnotations(tableDiff.getModelAnnotationDiffs());
        diffColumns(tableDiff.getColumnDiffs());
        PrimaryKeyDiff primaryKeyDiff = tableDiff.getPrimaryKeyDiff();
        if (primaryKeyDiff != null) {
            diffPrimaryKey(primaryKeyDiff);
        }
        diffForeignKeys(tableDiff.getForeignKeyDiffs());
    }

    //--------------------------------------------------------------------------
    // Table annotations
    //--------------------------------------------------------------------------

    public void diffTableAnnotations(List<ModelAnnotationDiff> modelAnnotationDiffs) {
        for (ModelAnnotationDiff modelAnnotationComparison :
                modelAnnotationDiffs) {
            diffTableAnnotation(modelAnnotationComparison);
        }
    }

    public void diffTableAnnotation(ModelAnnotationDiff modelAnnotationDiff) {
        sourceModelAnnotation =
                modelAnnotationDiff.getSourceModelAnnotation();
        targetModelAnnotation =
                modelAnnotationDiff.getTargetModelAnnotation();

        if (sourceModelAnnotation == null && targetModelAnnotation == null) {
            diffTableAnnotationSourceTargetNull(modelAnnotationDiff);
        } else if (sourceModelAnnotation == null) {
            diffTableAnnotationSourceNull(modelAnnotationDiff);
        } else if (targetModelAnnotation == null) {
            diffTableAnnotationTargetNull(modelAnnotationDiff);
        } else {
            diffTableAnnotationSourceTarget(modelAnnotationDiff);
        }
    }

    public void diffTableAnnotationSourceTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        logger.warning("Both source and target table annotations are null");
    }
    public abstract void diffTableAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff);
    public abstract void diffTableAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff);
    public abstract void diffTableAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff);

    public void diffTableAnnotationChildren(ModelAnnotationDiff modelAnnotationDiff) {}

    //--------------------------------------------------------------------------
    // Columns
    //--------------------------------------------------------------------------

    public void diffColumns(List<ColumnDiff> columnDiffs) {
        for (ColumnDiff columnComparison : columnDiffs) {
            diffColumn(columnComparison);
        }
    }

    public void diffColumn(ColumnDiff columnDiff) {
        sourceColumn = columnDiff.getSourceColumn();
        targetColumn = columnDiff.getTargetColumn();

        if (sourceColumn == null && targetColumn == null) {
            diffColumnSourceTargetNull(columnDiff);
        } else if (sourceColumn == null) {
            diffColumnSourceNull(columnDiff);
        } else if (targetColumn == null) {
            diffColumnTargetNull(columnDiff);
        } else {
            diffColumnSourceTarget(columnDiff);
        }
    }

    public void diffColumnSourceTargetNull(ColumnDiff columnDiff) {
        logger.warning("Both source and target columns are null");
    }
    public abstract void diffColumnSourceNull(ColumnDiff columnDiff);
    public abstract void diffColumnTargetNull(ColumnDiff columnDiff);
    public abstract void diffColumnSourceTarget(ColumnDiff columnDiff);

    public void diffColumnChildren(ColumnDiff columnDiff) {
        diffColumnAnnotations(columnDiff.getModelAnnotationDiffs());
    }

    //--------------------------------------------------------------------------
    // Column annotations
    //--------------------------------------------------------------------------

    public void diffColumnAnnotations(List<ModelAnnotationDiff> modelAnnotationDiffs) {
        for (ModelAnnotationDiff modelAnnotationComparison :
                modelAnnotationDiffs) {
            diffColumnAnnotation(modelAnnotationComparison);
        }
    }

    public void diffColumnAnnotation(ModelAnnotationDiff modelAnnotationDiff) {
        sourceModelAnnotation =
                modelAnnotationDiff.getSourceModelAnnotation();
        targetModelAnnotation =
                modelAnnotationDiff.getTargetModelAnnotation();

        if (sourceModelAnnotation == null && targetModelAnnotation == null) {
            diffColumnAnnotationSourceTargetNull(modelAnnotationDiff);
        } else if (sourceModelAnnotation == null) {
            diffColumnAnnotationSourceNull(modelAnnotationDiff);
        } else if (targetModelAnnotation == null) {
            diffColumnAnnotationTargetNull(modelAnnotationDiff);
        } else {
            diffColumnAnnotationSourceTarget(modelAnnotationDiff);
        }
    }

    public void diffColumnAnnotationSourceTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        logger.warning("Both source and target column annotations are null");
    }
    public abstract void diffColumnAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff);
    public abstract void diffColumnAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff);
    public abstract void diffColumnAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff);

    public void diffColumnAnnotationChildren(ModelAnnotationDiff modelAnnotationDiff) {}

    //--------------------------------------------------------------------------
    // Primary keys
    //--------------------------------------------------------------------------

    public void diffPrimaryKey(PrimaryKeyDiff primaryKeyDiff) {
        sourcePrimaryKey = primaryKeyDiff.getSourcePrimaryKey();
        targetPrimaryKey = primaryKeyDiff.getTargetPrimaryKey();

        if (sourcePrimaryKey == null && targetPrimaryKey == null) {
            diffPrimaryKeySourceTargetNull(primaryKeyDiff);
        } else if (sourcePrimaryKey == null) {
            diffPrimaryKeySourceNull(primaryKeyDiff);
        } else if (targetPrimaryKey == null) {
            diffPrimaryKeyTargetNull(primaryKeyDiff);
        } else {
            diffPrimaryKeySourceTarget(primaryKeyDiff);
        }
    }

    public void diffPrimaryKeySourceTargetNull(PrimaryKeyDiff primaryKeyDiff) {
        logger.warning("Both source and target primary keys are null");
    }
    public abstract void diffPrimaryKeySourceNull(PrimaryKeyDiff primaryKeyDiff);
    public abstract void diffPrimaryKeyTargetNull(PrimaryKeyDiff primaryKeyDiff);
    public abstract void diffPrimaryKeySourceTarget(PrimaryKeyDiff primaryKeyDiff);

    public void diffPrimaryKeyChildren(PrimaryKeyDiff primaryKeyDiff) {
        diffPrimaryKeyColumns(primaryKeyDiff.getPrimaryKeyColumnDiffs());
    }

    //--------------------------------------------------------------------------
    // Primary key columns
    //--------------------------------------------------------------------------

    public void diffPrimaryKeyColumns(List<PrimaryKeyColumnDiff> primaryKeyColumnDiffs) {
        for (PrimaryKeyColumnDiff primaryKeyColumnDiff :
                primaryKeyColumnDiffs) {
            diffPrimaryKeyColumn(primaryKeyColumnDiff);
        }
    }

    public void diffPrimaryKeyColumn(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        sourcePrimaryKeyColumn =
                primaryKeyColumnDiff.getSourcePrimaryKeyColumn();
        targetPrimaryKeyColumn =
                primaryKeyColumnDiff.getTargetPrimaryKeyColumn();

        if (sourcePrimaryKeyColumn == null && targetPrimaryKeyColumn == null) {
            diffPrimaryKeyColumnSourceTargetNull(primaryKeyColumnDiff);
        } else if (sourcePrimaryKeyColumn == null) {
            diffPrimaryKeyColumnSourceNull(primaryKeyColumnDiff);
        } else if (targetPrimaryKeyColumn == null) {
            diffPrimaryKeyColumnTargetNull(primaryKeyColumnDiff);
        } else {
            diffPrimaryKeyColumnSourceTarget(primaryKeyColumnDiff);
        }
    }

    public void diffPrimaryKeyColumnSourceTargetNull(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        logger.warning("Both source and target primary key columns are null");
    }
    public abstract void diffPrimaryKeyColumnSourceNull(PrimaryKeyColumnDiff primaryKeyColumnDiff);
    public abstract void diffPrimaryKeyColumnTargetNull(PrimaryKeyColumnDiff primaryKeyColumnDiff);
    public abstract void diffPrimaryKeyColumnSourceTarget(PrimaryKeyColumnDiff primaryKeyColumnDiff);

    public void diffPrimaryKeyColumnChildren(PrimaryKeyColumnDiff primaryKeyColumnDiff) {};

    //--------------------------------------------------------------------------
    // Foreign keys
    //--------------------------------------------------------------------------

    public void diffForeignKeys(List<ForeignKeyDiff> foreignKeyDiffs) {
        for (ForeignKeyDiff foreignKeyDiff : foreignKeyDiffs) {
            diffForeignKey(foreignKeyDiff);
        }
    }

    public void diffForeignKey(ForeignKeyDiff foreignKeyDiff) {
        sourceForeignKey =
                foreignKeyDiff.getSourceForeignKey();
        targetForeignKey =
                foreignKeyDiff.getTargetForeignKey();

        if (sourceForeignKey == null && targetForeignKey == null) {
            diffForeignKeySourceTargetNull(foreignKeyDiff);
        } else if (sourceForeignKey == null) {
            diffForeignKeySourceNull(foreignKeyDiff);
        } else if (targetForeignKey == null) {
            diffForeignKeyTargetNull(foreignKeyDiff);
        } else {
            diffForeignKeySourceTarget(foreignKeyDiff);
        }
    }

    public void diffForeignKeySourceTargetNull(ForeignKeyDiff foreignKeyDiff) {
        logger.warning("Both source and target foreign keys are null");
    }
    public abstract void diffForeignKeySourceNull(ForeignKeyDiff foreignKeyDiff);
    public abstract void diffForeignKeyTargetNull(ForeignKeyDiff foreignKeyDiff);
    public abstract void diffForeignKeySourceTarget(ForeignKeyDiff foreignKeyDiff);

    public void diffForeignKeyChildren(ForeignKeyDiff foreignKeyDiff) {
        diffReference(foreignKeyDiff.getReferenceDiffs());
        diffForeignKeyAnnotations(foreignKeyDiff.getModelAnnotationDiffs());
    }

    //--------------------------------------------------------------------------
    // References
    //--------------------------------------------------------------------------

    public void diffReference(List<ReferenceDiff> referenceDiffs) {
        for (ReferenceDiff referenceDiff : referenceDiffs) {
            diffReference(referenceDiff);
        }
    }

    public void diffReference(ReferenceDiff referenceDiff) {
        sourceReference =
                referenceDiff.getSourceReference();
        targetReference =
                referenceDiff.getTargetReference();

        if (sourceReference == null && targetReference == null) {
            diffReferenceSourceTargetNull(referenceDiff);
        } else if (sourceReference == null) {
            diffReferenceSourceNull(referenceDiff);
        } else if (targetReference == null) {
            diffReferenceTargetNull(referenceDiff);
        } else {
            diffReferenceSourceTarget(referenceDiff);
        }
    }

    public void diffReferenceSourceTargetNull(ReferenceDiff referenceDiff) {
        logger.warning("Both source and target references are null");
    }
    public abstract void diffReferenceSourceNull(ReferenceDiff referenceDiff);
    public abstract void diffReferenceTargetNull(ReferenceDiff referenceDiff);
    public abstract void diffReferenceSourceTarget(ReferenceDiff referenceDiff);

    public void diffReferenceChildren(ReferenceDiff referenceDiff) {
    }

    //--------------------------------------------------------------------------
    // Foreign key annotations
    //--------------------------------------------------------------------------

    public void diffForeignKeyAnnotations(List<ModelAnnotationDiff> modelAnnotationDiffs) {
        for (ModelAnnotationDiff modelAnnotationComparison :
                modelAnnotationDiffs) {
            diffForeignKeyAnnotation(modelAnnotationComparison);
        }
    }

    public void diffForeignKeyAnnotation(ModelAnnotationDiff modelAnnotationDiff) {
        sourceModelAnnotation =
                modelAnnotationDiff.getSourceModelAnnotation();
        targetModelAnnotation =
                modelAnnotationDiff.getTargetModelAnnotation();

        if (sourceModelAnnotation == null && targetModelAnnotation == null) {
            diffForeignKeyAnnotationSourceTargetNull(modelAnnotationDiff);
        } else if (sourceModelAnnotation == null) {
            diffForeignKeyAnnotationSourceNull(modelAnnotationDiff);
        } else if (targetModelAnnotation == null) {
            diffForeignKeyAnnotationTargetNull(modelAnnotationDiff);
        } else {
            diffForeignKeyAnnotationSourceTarget(modelAnnotationDiff);
        }
    }

    public void diffForeignKeyAnnotationSourceTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        logger.warning("Both source and target foreign key annotations are null");
    }
    public abstract void diffForeignKeyAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff);
    public abstract void diffForeignKeyAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff);
    public abstract void diffForeignKeyAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff);

    public void diffForeignKeyAnnotationChildren(ModelAnnotationDiff modelAnnotationDiff) {
    }

}
