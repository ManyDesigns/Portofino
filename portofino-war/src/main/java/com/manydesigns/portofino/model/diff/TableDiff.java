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

import com.manydesigns.portofino.model.datamodel.PrimaryKey;
import com.manydesigns.portofino.model.datamodel.Table;

import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableDiff {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    private final Table sourceTable;
    private final Table targetTable;
    private final List<ModelAnnotationDiff> modelAnnotationDiffs;
    private final List<ColumnDiff> columnDiffs;
    private final PrimaryKeyDiff primaryKeyDiff;

    public TableDiff(Table sourceTable, Table targetTable) {
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        modelAnnotationDiffs = new ArrayList<ModelAnnotationDiff>();
        columnDiffs = new ArrayList<ColumnDiff>();
        PrimaryKey sourcePrimaryKey = (sourceTable == null)
                ? null
                : sourceTable.getPrimaryKey();
        PrimaryKey targetPrimaryKey = (targetTable == null)
                ? null
                : targetTable.getPrimaryKey();
        primaryKeyDiff = new PrimaryKeyDiff(sourcePrimaryKey,
                targetPrimaryKey);
    }

    public Table getSourceTable() {
        return sourceTable;
    }

    public Table getTargetTable() {
        return targetTable;
    }

    public List<ModelAnnotationDiff> getModelAnnotationDiffs() {
        return modelAnnotationDiffs;
    }

    public List<ColumnDiff> getColumnDiffs() {
        return columnDiffs;
    }

    public PrimaryKeyDiff getPrimaryKeyDiff() {
        return primaryKeyDiff;
    }
}
