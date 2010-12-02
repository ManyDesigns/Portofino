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

import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.portofino.actions.AbstractCrudAction;
import com.manydesigns.portofino.actions.CrudSelectionProvider;
import com.manydesigns.portofino.actions.CrudUnit;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.annotations.Annotation;
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

    public void prepare() {
        if (qualifiedName == null) {
            return;
        }
        Table table = model.findTableByQualifiedName(qualifiedName);
        if (table == null) {
            throw new ModelObjectNotFoundError(qualifiedName);
        }
        rootCrudUnit = createRootCrudUnit(table);
    }

    protected CrudUnit createRootCrudUnit(Table table) {
        String qualifiedTableName = table.getQualifiedName();
        ClassAccessor classAccessor =
                    context.getTableAccessor(qualifiedTableName);
        String query = MessageFormat.format(
                "FROM {0}", table.getActualEntityName());
        String searchTitle = MessageFormat.format(
                "Search: {0}", qualifiedTableName);
        String createTitle = MessageFormat.format(
                "Create: {0}", qualifiedTableName);
        String readTitle = MessageFormat.format(
                "Read: {0}", qualifiedTableName);
        String updateTitle = MessageFormat.format(
                "Update: {0}", qualifiedTableName);
        String prefix = null;
        CrudUnit result = new CrudUnit(classAccessor, table, query,
                searchTitle, createTitle, readTitle, updateTitle, null, prefix, true);

        // setup selection providers
        for (ForeignKey foreignKey : table.getForeignKeys()) {
            CrudSelectionProvider crudSelectionProvider =
                    createCrudSelectionProvider(foreignKey);
            result.crudSelectionProviders.add(crudSelectionProvider);
        }

        // inject values
        injectValues(result);

        // setup sub crud units
        int index = 0;
        for (ForeignKey foreignKey : table.getOneToManyRelationships()) {
            CrudUnit subCrudUnit =
                    createSubCrudUnit(foreignKey, index);
            result.subCrudUnits.add(subCrudUnit);

            index++;
        }

        return result;
    }

    protected void injectValues(CrudUnit crudUnit) {
        crudUnit.context = context;
        crudUnit.model = model;
        crudUnit.req = req;
    }

    protected CrudSelectionProvider createCrudSelectionProvider(ForeignKey foreignKey) {
        // retrieve the related objects
        Table relatedTable = foreignKey.getActualToTable();
        ClassAccessor classAccessor =
                context.getTableAccessor(relatedTable.getQualifiedName());
        List<Object> relatedObjects =
                context.getAllObjects(relatedTable.getQualifiedName());

        // Create selection provider
        ShortName shortNameAnnotation =
                classAccessor.getAnnotation(ShortName.class);
        TextFormat[] textFormats = null;
        if (shortNameAnnotation != null) {
            textFormats = new TextFormat[] {
                OgnlTextFormat.create(shortNameAnnotation.value())
            };
        }
        SelectionProvider selectionProvider =
                DefaultSelectionProvider.create(foreignKey.getForeignKeyName(),
                        relatedObjects, classAccessor, textFormats);
        boolean autocomplete = false;
        for (Annotation current : foreignKey.getAnnotations()) {
            if ("com.manydesigns.elements.annotations.Autocomplete"
                    .equals(current.getType())) {
                autocomplete = true;
            }
        }
        selectionProvider.setAutocomplete(autocomplete);

        // create field names
        List<Reference> references = foreignKey.getReferences();
        String[] fieldNames = new String[references.size()];
        int i = 0;
        for (Reference reference : references) {
            Column column = reference.getActualFromColumn();
            fieldNames[i] = column.getActualPropertyName();
            i++;
        }

        return new CrudSelectionProvider(selectionProvider, fieldNames);
    }

    protected CrudUnit createSubCrudUnit(ForeignKey foreignKey,
                                         int index) {
        Table subTable = foreignKey.getFromTable();
        ClassAccessor subClassAccessor =
                context.getTableAccessor(subTable.getQualifiedName());
        StringBuilder sb = new StringBuilder();
        sb.append("FROM ");
        sb.append(subTable.getActualEntityName());
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
        String subPrefix = MessageFormat.format("subCrudUnits[{0}].", index);

        CrudUnit subCrudUnit =
                new CrudUnit(subClassAccessor, subTable, subQuery,
                        subSearchTitle, null, null, null, null, subPrefix, false);
        injectValues(subCrudUnit);
        return subCrudUnit;
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
