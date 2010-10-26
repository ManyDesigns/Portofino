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

    public void visitDatabase(DatabaseDiff databaseDiff) {
        visitSchemas(databaseDiff.getSchemaDiffs());
    }

    public void visitSchemas(List<SchemaDiff> schemaDiffs) {
        for (SchemaDiff schemaComparison : schemaDiffs) {
            visitSchema(schemaComparison);
        }
    }

    public void visitSchema(SchemaDiff schemaDiff) {
        visitTables(schemaDiff.getTableDiffs());
    }

    public void visitTables(List<TableDiff> tableDiffs) {
        for (TableDiff tableComparison : tableDiffs) {
            visitTable(tableComparison);
        }
    }

    public void visitTable(TableDiff tableDiff) {
        visitTableAnnotations(tableDiff.getModelAnnotationDiffs());
        visitColumns(tableDiff.getColumnDiffs());
        visitPrimaryKey(tableDiff.getPrimaryKeyDiff());
    }

    public void visitTableAnnotations(List<ModelAnnotationDiff> modelAnnotationDiffs) {
        for (ModelAnnotationDiff modelAnnotationComparison :
                modelAnnotationDiffs) {
            visitTableAnnotations(modelAnnotationComparison);
        }
    }

    public void visitTableAnnotations(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void visitColumns(List<ColumnDiff> columnDiffs) {
        for (ColumnDiff columnComparison : columnDiffs) {
            visitColumn(columnComparison);
        }
    }

    public void visitColumn(ColumnDiff columnDiff) {
        visitColumnAnnotations(columnDiff.getModelAnnotationDiffs());
    }

    public void visitColumnAnnotations(List<ModelAnnotationDiff> modelAnnotationDiffs) {
        for (ModelAnnotationDiff modelAnnotationComparison :
                modelAnnotationDiffs) {
            visitColumnAnnotations(modelAnnotationComparison);
        }
    }

    public void visitColumnAnnotations(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void visitPrimaryKey(PrimaryKeyDiff primaryKeyDiff) {
    }

}
