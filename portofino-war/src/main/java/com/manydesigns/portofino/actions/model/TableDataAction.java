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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.actions.AbstractCrudAction;
import com.manydesigns.portofino.actions.CrudUnit;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.datamodel.Reference;
import com.manydesigns.portofino.model.datamodel.Table;

import java.text.MessageFormat;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableDataAction extends AbstractCrudAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";


    //**************************************************************************
    // Setup
    //**************************************************************************

    @Override
    public void setupMetadata() {
        if (qualifiedName == null) {
            return;
        }
        Table table = model.findTableByQualifiedName(qualifiedName);
        if (table == null) {
            throw new ModelObjectNotFoundError(qualifiedName);
        }
        rootCrudUnit = setupUseCaseInstance(table);
    }

    private CrudUnit setupUseCaseInstance(Table table) {
        String qualifiedTableName = table.getQualifiedName();
        ClassAccessor classAccessor =
                    context.getTableAccessor(qualifiedTableName);
        String query = MessageFormat.format(
                "FROM {0}", qualifiedTableName);
        String searchTitle = MessageFormat.format(
                "Search: {0}", qualifiedTableName);
        String createTitle = MessageFormat.format(
                "Create: {0}", qualifiedTableName);
        String readTitle = MessageFormat.format(
                "Read: {0}", qualifiedTableName);
        String updateTitle = MessageFormat.format(
                "Update: {0}", qualifiedTableName);
        CrudUnit result = new CrudUnit(classAccessor, table, query,
                searchTitle, createTitle, readTitle, updateTitle);

        // inject values
        result.context = context;
        result.model = model;
        result.req = req;

        for (ForeignKey foreignKey : table.getOneToManyRelationships()) {
            Table subTable = foreignKey.getFromTable();
            ClassAccessor subClassAccessor =
                    context.getTableAccessor(subTable.getQualifiedName());
            StringBuilder sb = new StringBuilder();
            sb.append("FROM ");
            sb.append(subTable.getQualifiedName());
            boolean first = true;
            for (Reference reference : foreignKey.getReferences()) {
                if (first) {
                    sb.append(" WHERE ");
                    first = false;
                } else {
                    sb.append(" AND ");
                }
                Column fromColumn = reference.getActualFromColumn();
                String fromPropertyName = fromColumn.getActualPropertyName();
                Column toColumn = reference.getActualToColumn();
                String toPropertyName = toColumn.getActualPropertyName();

                sb.append(MessageFormat.format("{0}=%'{'{1}'}'",
                        fromPropertyName, toPropertyName));
            }
            String subQuery = sb.toString();
            String subSearchTitle = MessageFormat.format(
                    "Objects related through foreign key: {0}",
                    foreignKey.getForeignKeyName());

            CrudUnit subCrudUnit =
                    new CrudUnit(subClassAccessor, subTable, subQuery,
                            subSearchTitle, null, null, null);
            subCrudUnit.context = context;
            subCrudUnit.model = model;
            subCrudUnit.req = req;
            result.subCrudUnits.add(subCrudUnit);
        }

        return result;
    }


    //**************************************************************************
    // Redirect to first use case
    //**************************************************************************

    @Override
    public String redirectToFirst() {
        List<Table> tables = model.getAllTables();
        if (tables.isEmpty()) {
            return NO_CLASSES;
        } else {
            qualifiedName = tables.get(0).getQualifiedName();
            return REDIRECT_TO_FIRST;
        }
    }

}
