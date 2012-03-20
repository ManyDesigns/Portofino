/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.actions.model;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TableDataAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

//    //**************************************************************************
//    // Injections
//    //**************************************************************************
//
//    @Inject(RequestAttributes.MODEL)
//    public Model model;
//
//    @Inject(RequestAttributes.APPLICATION)
//    public Application application;
//
//    //**************************************************************************
//    // Setup
//    //**************************************************************************
//
//    public void prepare() {
//        if (qualifiedName == null) {
//            return;
//        }
//        Table table =
//                DataModelLogic.findTableByQualifiedName(
//                        model, qualifiedName);
//        if (table == null) {
//            throw new ModelObjectNotFoundError(qualifiedName);
//        }
//        rootCrudUnit = createRootCrudUnit(table);
//    }
//
//    protected CrudUnit createRootCrudUnit(Table table) {
//        String qualifiedTableName = table.getQualifiedName();
//        ClassAccessor classAccessor =
//                    application.getTableAccessor(qualifiedTableName);
//        String query = MessageFormat.format(
//                "FROM {0}", table.getActualEntityName());
//        String searchTitle = MessageFormat.format(
//                "Search: {0}", qualifiedTableName);
//        String createTitle = MessageFormat.format(
//                "Create: {0}", qualifiedTableName);
//        String readTitle = MessageFormat.format(
//                "Read: {0}", qualifiedTableName);
//        String updateTitle = MessageFormat.format(
//                "Update: {0}", qualifiedTableName);
//        String prefix = null;
//        CrudUnit result = new CrudUnit(classAccessor, table, query,
//                searchTitle, createTitle, readTitle, updateTitle, null, prefix, true);
//
//        // setup selection providers
//        for (ForeignKey foreignKey : table.getForeignKeys()) {
//            CrudSelectionProvider crudSelectionProvider =
//                    createCrudSelectionProvider(foreignKey);
//            result.crudSelectionProviders.add(crudSelectionProvider);
//        }
//
//        // inject values
//        injectValues(result);
//
//        // setup sub crud units
//        int index = 0;
//        for (ForeignKey foreignKey : table.getOneToManyRelationships()) {
//            CrudUnit subCrudUnit =
//                    createSubCrudUnit(foreignKey, index);
////            result.subCrudUnits.add(subCrudUnit);
//
//            index++;
//        }
//
//        return result;
//    }
//
//    protected void injectValues(CrudUnit crudUnit) {
//        crudUnit.application = application;
//        crudUnit.model = model;
//    }
//
//    protected CrudSelectionProvider createCrudSelectionProvider(ForeignKey foreignKey) {
//        // retrieve the related objects
//        Table relatedTable = foreignKey.getToTable();
//        ClassAccessor classAccessor =
//                application.getTableAccessor(relatedTable.getQualifiedName());
//        List<Object> relatedObjects =
//                SessionUtils.getAllObjects(application, relatedTable.getQualifiedName());
//
//        // Create selection provider
//        ShortName shortNameAnnotation =
//                classAccessor.getAnnotation(ShortName.class);
//        TextFormat[] textFormats = null;
//        if (shortNameAnnotation != null) {
//            textFormats = new TextFormat[] {
//                OgnlTextFormat.create(shortNameAnnotation.value())
//            };
//        }
//        SelectionProvider selectionProvider =
//                DefaultSelectionProvider.create(foreignKey.getName(),
//                        relatedObjects, classAccessor, textFormats);
//
//        // create field names
//        List<Reference> references = foreignKey.getReferences();
//        String[] fieldNames = new String[references.size()];
//        int i = 0;
//        for (Reference reference : references) {
//            Column column = reference.getActualFromColumn();
//            fieldNames[i] = column.getActualPropertyName();
//            i++;
//        }
//
//        return new CrudSelectionProvider(selectionProvider, fieldNames);
//    }
//
//    protected CrudUnit createSubCrudUnit(ForeignKey foreignKey,
//                                         int index) {
//        Table subTable = foreignKey.getFromTable();
//        ClassAccessor subClassAccessor =
//                application.getTableAccessor(subTable.getQualifiedName());
//        StringBuilder sb = new StringBuilder();
//        sb.append("FROM ");
//        sb.append(subTable.getActualEntityName());
//        boolean first = true;
//        for (Reference reference : foreignKey.getReferences()) {
//            if (first) {
//                sb.append(" WHERE ");
//                first = false;
//            } else {
//                sb.append(" AND ");
//            }
//            Column fromColumn = reference.getActualFromColumn();
//            String fromPropertyName = fromColumn.getActualPropertyName();
//            Column toColumn = reference.getActualToColumn();
//            String toPropertyName = toColumn.getActualPropertyName();
//
//            sb.append(MessageFormat.format("{0}=%'{'{1}'}'",
//                    fromPropertyName, toPropertyName));
//        }
//        String subQuery = sb.toString();
//        String subSearchTitle = MessageFormat.format(
//                "Objects related through foreign key: {0}",
//                foreignKey.getName());
//        String subPrefix = MessageFormat.format("subCrudUnits[{0}].", index);
//
//        CrudUnit subCrudUnit =
//                new CrudUnit(subClassAccessor, subTable, subQuery,
//                        subSearchTitle, null, null, null, null, subPrefix, false);
//        injectValues(subCrudUnit);
//        return subCrudUnit;
//    }
//
//
//    //**************************************************************************
//    // Redirect to first use case
//    //**************************************************************************
//
//    @Override
//    public String redirectToFirst() {
//        List<Table> tables = DataModelLogic.getAllTables(model);
//        if (tables.isEmpty()) {
//            return PortofinoAction.NO_CLASSES;
//        } else {
//            qualifiedName = tables.get(0).getQualifiedName();
//            return PortofinoAction.REDIRECT_TO_FIRST;
//        }
//    }

}
