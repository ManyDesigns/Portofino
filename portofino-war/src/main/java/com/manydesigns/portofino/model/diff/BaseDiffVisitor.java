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

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class BaseDiffVisitor {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public BaseDiffVisitor() {}

    public void visitDatabaseDiff(DatabaseDiff databaseDiff) {
        visitSchemaDiffs(databaseDiff.getSchemaDiffs());
    }

    public void visitSchemaDiffs(List<SchemaDiff> schemaDiffs) {
        for (SchemaDiff schemaComparison : schemaDiffs) {
            visitSchemaDiff(schemaComparison);
        }
    }

    public void visitSchemaDiff(SchemaDiff schemaDiff) {
        visitTableDiffs(schemaDiff.getTableDiffs());
    }

    public void visitTableDiffs(List<TableDiff> tableDiffs) {
        for (TableDiff tableComparison : tableDiffs) {
            visitTableDiff(tableComparison);
        }
    }

    public void visitTableDiff(TableDiff tableDiff) {
        visitTableAnnotationDiffs(tableDiff.getModelAnnotationDiffs());
        visitColumnDiffs(tableDiff.getColumnDiffs());
        PrimaryKeyDiff primaryKeyDiff = tableDiff.getPrimaryKeyDiff();
        if (primaryKeyDiff != null) {
            visitPrimaryKeyDiff(primaryKeyDiff);
        }
        visitForeignKeyDiffs(tableDiff.getForeignKeyDiffs());
    }

    public void visitTableAnnotationDiffs(List<ModelAnnotationDiff> modelAnnotationDiffs) {
        for (ModelAnnotationDiff modelAnnotationComparison :
                modelAnnotationDiffs) {
            visitTableAnnotationDiff(modelAnnotationComparison);
        }
    }

    public void visitTableAnnotationDiff(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void visitColumnDiffs(List<ColumnDiff> columnDiffs) {
        for (ColumnDiff columnComparison : columnDiffs) {
            visitColumnDiff(columnComparison);
        }
    }

    public void visitColumnDiff(ColumnDiff columnDiff) {
        visitColumnAnnotationDiffs(columnDiff.getModelAnnotationDiffs());
    }

    public void visitColumnAnnotationDiffs(List<ModelAnnotationDiff> modelAnnotationDiffs) {
        for (ModelAnnotationDiff modelAnnotationComparison :
                modelAnnotationDiffs) {
            visitColumnAnnotationDiff(modelAnnotationComparison);
        }
    }

    public void visitColumnAnnotationDiff(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void visitPrimaryKeyDiff(PrimaryKeyDiff primaryKeyDiff) {
        visitPrimaryKeyColumnDiffs(primaryKeyDiff.getPrimaryKeyColumnDiffs());
    }

    public void visitPrimaryKeyColumnDiffs(List<PrimaryKeyColumnDiff> primaryKeyColumnDiffs) {
        for (PrimaryKeyColumnDiff primaryKeyColumnDiff :
                primaryKeyColumnDiffs) {
            visitPrimaryKeyColumnDiff(primaryKeyColumnDiff);
        }
    }

    public void visitPrimaryKeyColumnDiff(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
    }

    public void visitForeignKeyDiffs(List<ForeignKeyDiff> foreignKeyDiffs) {
        for (ForeignKeyDiff foreignKeyDiff : foreignKeyDiffs) {
            visitForeignKeyDiff(foreignKeyDiff);
        }
    }

    public void visitForeignKeyDiff(ForeignKeyDiff foreignKeyDiff) {
        visitReferenceDiffs(foreignKeyDiff.getReferenceDiffs());
        visitForeignKeyAnnotationDiffs(foreignKeyDiff.getModelAnnotationDiffs());
    }

    public void visitReferenceDiffs(List<ReferenceDiff> referenceDiffs) {
        for (ReferenceDiff referenceDiff : referenceDiffs) {
            visitReferenceDiff(referenceDiff);
        }
    }

    public void visitReferenceDiff(ReferenceDiff referenceDiff) {
    }

    public void visitForeignKeyAnnotationDiffs(List<ModelAnnotationDiff> modelAnnotationDiffs) {
        for (ModelAnnotationDiff modelAnnotationComparison :
                modelAnnotationDiffs) {
            visitForeignKeyAnnotationDiff(modelAnnotationComparison);
        }
    }

    public void visitForeignKeyAnnotationDiff(ModelAnnotationDiff modelAnnotationDiff) {
    }


}
