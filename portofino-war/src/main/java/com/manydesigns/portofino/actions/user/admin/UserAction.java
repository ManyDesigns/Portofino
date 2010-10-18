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
package com.manydesigns.portofino.actions.user.admin;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.NumericField;
import com.manydesigns.elements.fields.PasswordField;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.actions.RelatedTableForm;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.datamodel.Reference;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;
import com.manydesigns.portofino.util.TempFiles;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UserAction extends PortofinoAction implements ServletRequestAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String REDIRECT_TO_TABLE = "redirectToTable";
    public final static String NO_TABLES = "noTables";
    public final static String JSON_SELECT_FIELD_OPTIONS =
            "jsonSelectFieldOptions";
    public static final String EXPORT_FILENAME_FORMAT = "export-{0}";

    //**************************************************************************
    // ServletRequestAware implementation
    //**************************************************************************
    public HttpServletRequest req;

    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }

    //**************************************************************************
    // Web parameters
    //**************************************************************************

    public static final String qualifiedTableName = "portofino.public.user_";
    public String pk;
    public String[] selection;
    public String searchString;
    public String cancelReturnUrl;
    public String relName;
    public int optionProviderIndex;
    public String labelSearch;



    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public ClassAccessor tableAccessor;

    //**************************************************************************
    // Model objects
    //**************************************************************************

    public Object object;
    public List<Object> objects;


    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    public TableForm tableForm;
    public Form form;
    public SearchForm searchForm;
    public List<RelatedTableForm> relatedTableFormList;
    public InputStream inputStream;

    //**************************************************************************
    // export parameters
    //***************************************************************************

    public String contentType;
    public String fileName;
    public Long contentLength;
    public String chartId;

    //**************************************************************************
    // Other objects
    //**************************************************************************

    public PkHelper pkHelper;

    public static final Logger logger =
            LogUtil.getLogger(UserAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    public String execute() {

        if (pk == null) {
            return searchFromString();
        } else {
            return read();
        }
    }

    //**************************************************************************
    // Search
    //**************************************************************************

    public String searchFromString() {
        setupTable();


        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(tableAccessor);
        searchFormBuilder.configFields("uuid","email","state","lastName", "firstName");
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        return commonSearch();
    }

    protected void configureSearchFormFromString() {
        if (searchString != null) {
            DummyHttpServletRequest dummyRequest =
                    new DummyHttpServletRequest();
            String[] parts = searchString.split(",");
            Pattern pattern = Pattern.compile("(.*)=(.*)");
            for (String part : parts) {
                Matcher matcher = pattern.matcher(part);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2);
                    LogUtil.fineMF(logger, "Matched part: {0}={1}", key, value);
                    dummyRequest.setParameter(key, value);
                } else {
                    LogUtil.fineMF(logger, "Could not match part: {0}", part);
                }
            }
            searchForm.readFromRequest(dummyRequest);
        }
    }

    public String search() {
        setupTable();

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(tableAccessor);
        searchForm = searchFormBuilder.build();
        searchForm.readFromRequest(req);

        return commonSearch();
    }

    protected String commonSearch() {
        searchString = searchForm.toSearchString();
        if (searchString.length() == 0) {
            searchString = null;
        }

        Criteria criteria = new Criteria(tableAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        String readLinkExpression = getReadLinkExpression();
        OgnlTextFormat hrefFormat =
                OgnlTextFormat.create(readLinkExpression);
        hrefFormat.setUrl(true);

        TableFormBuilder tableFormBuilder =
                createTableFormBuilderWithOptionProviders()
                        .configNRows(objects.size())
                        .configMode(Mode.VIEW);
        tableFormBuilder.configFields("uuid","email","state",
                "lastName", "firstName");

        // ogni colonna chiave primaria sarà clickabile
        for (PropertyAccessor property : tableAccessor.getKeyProperties()) {
            tableFormBuilder.configHyperlinkGenerators(
                    property.getName(), hrefFormat, null);
        }

        tableForm = tableFormBuilder.build();
        tableForm.setKeyGenerator(pkHelper.createPkGenerator());
        tableForm.setSelectable(true);
        tableForm.readFromObject(objects);

        return SEARCH;
    }


    public String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder("/user-admin/UsersAction.action?pk=");
        boolean first = true;
        for (PropertyAccessor property : tableAccessor.getKeyProperties()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("%{");
            sb.append(property.getName());
            sb.append("}");
        }
        if (searchString != null) {
            sb.append("&searchString=");
            sb.append(Util.urlencode(searchString));
        }
        return sb.toString();
    }

    //**************************************************************************
    // Return to search
    //**************************************************************************

    public String returnToSearch() {
        setupTable();
        return RETURN_TO_SEARCH;
    }

    //**************************************************************************
    // Read
    //**************************************************************************

    public String read() {
        setupTable();
        Serializable pkObject = pkHelper.parsePkString(pk);

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(tableAccessor);
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        Criteria criteria = new Criteria(tableAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        object = context.getObjectByPk(qualifiedTableName, pkObject);
        FormBuilder builder = createFormBuilderWithOptionProviders()
                .configMode(Mode.VIEW);
        builder.configFields("uuid","email","state",
                "lastName", "firstName");
        form = builder.build();
        form.readFromObject(object);

        relatedTableFormList = new ArrayList<RelatedTableForm>();

        Table table = model.findTableByQualifiedName(qualifiedTableName);
        for (ForeignKey relationship : table.getOneToManyRelationships()) {
            setupRelatedTableForm(relationship);
        }

        return READ;
    }

    protected void setupRelatedTableForm(ForeignKey relationship) {
        List<Object> relatedObjects =
                context.getRelatedObjects(qualifiedTableName, object,
                        relationship.getForeignKeyName());

        String qualifiedFromTableName =
                relationship.getFromTable().getQualifiedName();
        TableAccessor relatedTableAccessor =
                context.getTableAccessor(qualifiedFromTableName);
        TableFormBuilder tableFormBuilder =
                new TableFormBuilder(relatedTableAccessor);
        tableFormBuilder.configNRows(relatedObjects.size());
        TableForm tableForm = tableFormBuilder
                .configMode(Mode.VIEW)
                .build();
        tableForm.readFromObject(relatedObjects);

        RelatedTableForm relatedTableForm =
                new RelatedTableForm(relationship, tableForm, relatedObjects);
        relatedTableFormList.add(relatedTableForm);
    }

    //**************************************************************************
    // Create/Save
    //**************************************************************************

    public String create() {
        setupTable();

        final FormBuilder builder = createFormBuilderWithOptionProviders()
                .configMode(Mode.CREATE);
        builder.configFields("uuid","email","state",
                "lastName", "firstName");
        form = builder.build();

        return CREATE;
    }

    public String save() {
        setupTable();

        final FormBuilder builder = createFormBuilderWithOptionProviders()
                .configMode(Mode.CREATE);
        builder.configFields("uuid","email","state",
                "lastName", "firstName");
        form = builder.build();

        form.readFromRequest(req);
        if (form.validate()) {
            object = tableAccessor.newInstance();
            form.writeToObject(object);
            context.saveObject(qualifiedTableName, object);
            String databaseName = model
                    .findTableByQualifiedName(qualifiedTableName)
                    .getDatabaseName();
            context.commit(databaseName);
            pk = pkHelper.generatePkString(object);
            SessionMessages.addInfoMessage("SAVE avvenuto con successo");
            return SAVE;
        } else {
            return CREATE;
        }
    }

    //**************************************************************************
    // Edit/Update
    //**************************************************************************

    public String edit() {
        setupTable();
        Serializable pkObject = pkHelper.parsePkString(pk);

        object = context.getObjectByPk(qualifiedTableName, pkObject);

        FormBuilder builder = createFormBuilderWithOptionProviders()
                .configMode(Mode.EDIT);
        builder.configFields("uuid","email","state",
                "lastName", "firstName");
        form = builder.build();

        form.readFromObject(object);

        return EDIT;
    }

    public String update() {
        setupTable();
        Serializable pkObject = pkHelper.parsePkString(pk);

        final FormBuilder builder = createFormBuilderWithOptionProviders()
                .configMode(Mode.EDIT);
        builder.configFields("uuid","email","state",
                "lastName", "firstName");
        form = builder.build();

        object = context.getObjectByPk(qualifiedTableName, pkObject);
        form.readFromObject(object);
        form.readFromRequest(req);
        if (form.validate()) {
            form.writeToObject(object);
            context.updateObject(qualifiedTableName, object);
            String databaseName = model
                    .findTableByQualifiedName(qualifiedTableName).getDatabaseName();
            context.commit(databaseName);
            SessionMessages.addInfoMessage("UPDATE avvenuto con successo");
            return UPDATE;
        } else {
            return EDIT;
        }
    }


    //**************************************************************************
    // Delete
    //**************************************************************************

    public String delete() {
        setupTable();
        Object pkObject = pkHelper.parsePkString(pk);
        context.deleteObject(qualifiedTableName, pkObject);
        String databaseName = model.findTableByQualifiedName(qualifiedTableName)
                .getDatabaseName();
        context.commit(databaseName);
        SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        return DELETE;
    }

    public String bulkDelete() {
        setupTable();
        if (selection == null) {
            SessionMessages.addWarningMessage(
                    "DELETE non avvenuto: nessun oggetto selezionato");
            return CANCEL;
        }
        for (String current : selection) {
            Object pkObject = pkHelper.parsePkString(current);
            context.deleteObject(qualifiedTableName, pkObject);
        }
        String databaseName = model.findTableByQualifiedName(qualifiedTableName)
                .getDatabaseName();
        context.commit(databaseName);
        SessionMessages.addInfoMessage(MessageFormat.format(
                "DELETE di {0} oggetti avvenuto con successo", selection.length));
        return DELETE;
    }

    //**************************************************************************
    // Cancel
    //**************************************************************************

    public String cancel() {
        return CANCEL;
    }

    //**************************************************************************
    // Ajax
    //**************************************************************************

    public String jsonSelectFieldOptions() {
        return jsonOptions(true);
    }

    public String jsonAutocompleteOptions() {
        return jsonOptions(false);
    }

    protected String jsonOptions(boolean includeSelectPrompt) {
        setupTable();
        Table table = model.findTableByQualifiedName(qualifiedTableName);
        ForeignKey relationship =
                table.findForeignKeyByName(relName);

        String[] fieldNames = createFieldNamesForRelationship(relationship);
        SelectionProvider selectionProvider =
                createOptionProviderForRelationship(relationship);

        Form form = new FormBuilder(tableAccessor)
                .configFields(fieldNames)
                .configSelectionProvider(selectionProvider, fieldNames)
                .configMode(Mode.EDIT)
                .build();
        form.readFromRequest(req);

        SelectField targetField =
                (SelectField) form.get(0).get(optionProviderIndex);
        targetField.setLabelSearch(labelSearch);

        form.validate();

        String text = targetField.jsonSelectFieldOptions(includeSelectPrompt);
        LogUtil.infoMF(logger, "jsonSelectFieldOptions: {0}", text);

        inputStream = new StringBufferInputStream(text);

        return JSON_SELECT_FIELD_OPTIONS;
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    public void setupTable() {
        tableAccessor = context.getTableAccessor(qualifiedTableName);
        pkHelper = new PkHelper(tableAccessor);
        if (tableAccessor == null) {
            throw new ModelObjectNotFoundError(qualifiedTableName);
        }
    }

    protected FormBuilder createFormBuilderWithOptionProviders() {
        FormBuilder formBuilder = new FormBuilder(tableAccessor);

        // setup relationship lookups
        Table table = model.findTableByQualifiedName(qualifiedTableName);
        for (ForeignKey rel : table.getForeignKeys()) {
            String[] fieldNames = createFieldNamesForRelationship(rel);
            SelectionProvider selectionProvider =
                    createOptionProviderForRelationship(rel);
            boolean autocomplete = false;
            for (ModelAnnotation current : rel.getAnnotations()) {
                if ("com.manydesigns.elements.annotations.Autocomplete"
                        .equals(current.getType())) {
                    autocomplete = true;
                }
            }
            selectionProvider.setAutocomplete(autocomplete);

            formBuilder.configSelectionProvider(selectionProvider, fieldNames);
        }

        return formBuilder;
    }

    protected TableFormBuilder createTableFormBuilderWithOptionProviders() {
        TableFormBuilder tableFormBuilder = new TableFormBuilder(tableAccessor);

        // setup relationship lookups
        Table table = model.findTableByQualifiedName(qualifiedTableName);
        for (ForeignKey rel : table.getForeignKeys()) {
            String[] fieldNames = createFieldNamesForRelationship(rel);
            SelectionProvider selectionProvider =
                    createOptionProviderForRelationship(rel);
            boolean autocomplete = false;
            for (ModelAnnotation current : rel.getAnnotations()) {
                if ("com.manydesigns.elements.annotations.Autocomplete"
                        .equals(current.getType())) {
                    autocomplete = true;
                }
            }
            selectionProvider.setAutocomplete(autocomplete);

            tableFormBuilder.configOptionProvider(selectionProvider, fieldNames);
        }
        return tableFormBuilder;
    }

    protected String[] createFieldNamesForRelationship(ForeignKey rel) {
        List<Reference> references = rel.getReferences();
        String[] fieldNames = new String[references.size()];
        int i = 0;
        for (Reference reference : references) {
            Column column = reference.getFromColumn();
            fieldNames[i] = column.getName();
            i++;
        }
        return fieldNames;
    }

    protected SelectionProvider createOptionProviderForRelationship(ForeignKey rel) {
        // retrieve the related objects
        Table relatedTable = rel.getToTable();
        ClassAccessor classAccessor =
                context.getTableAccessor(relatedTable.getQualifiedName());
        List<Object> relatedObjects =
                context.getAllObjects(relatedTable.getQualifiedName());
        ShortName shortNameAnnotation =
                classAccessor.getAnnotation(ShortName.class);
        TextFormat[] textFormat = null;
        if (shortNameAnnotation != null) {
            textFormat = new TextFormat[] {
                OgnlTextFormat.create(shortNameAnnotation.value())
            };
        }
        SelectionProvider selectionProvider =
                DefaultSelectionProvider.create(rel.getForeignKeyName(),
                        relatedObjects, classAccessor, textFormat);
        return selectionProvider;
    }


    //**************************************************************************
    // ExportSearch
    //**************************************************************************

    public String exportSearch() {
        setupTable();

        //Relationship relationship =
        //        table.findManyToOneByName(relName);

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(tableAccessor);
        searchFormBuilder.configFields("uuid","email","state",
                "lastName", "firstName");
        searchForm = searchFormBuilder.build();
        searchForm.readFromRequest(req);

        Criteria criteria = new Criteria(tableAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        TableFormBuilder tableFormBuilder =
            createTableFormBuilderWithOptionProviders()
                            .configNRows(objects.size());

        TableFormBuilder builder = tableFormBuilder
                .configMode(Mode.VIEW);
        builder.configFields("uuid","email","state",
                "lastName", "firstName");
        tableForm = builder.build();

        tableForm.readFromObject(objects);

        String exportId = TempFiles.generateRandomCode();
        File fileTemp = TempFiles.getTempFile(EXPORT_FILENAME_FORMAT, exportId);

        createExportExcel(fileTemp);

        contentLength = fileTemp.length();

        try {
            inputStream = new FileInputStream(fileTemp);
        } catch (IOException e) {
            LogUtil.warning(logger, "IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        }
        return EXPORT;

    }

    private void createExportExcel(File fileTemp) {
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(fileTemp);
            WritableSheet sheet = workbook.createSheet("First Sheet", 0);

            int l = 0;
            for (TableForm.Column col : tableForm.getColumns()) {
                sheet.addCell(new Label(l, 0, col.getLabel()));
                l++;
            }

            int i = 1;
            for ( TableForm.Row col : tableForm.getRows()) {
                int j = 0;

                for (Field field : col.getFields()) {
                    if ( field instanceof NumericField) {
                        //NumberFormat numberFormat = new NumberFormat();
                        NumericField numField = (NumericField)field;
                        //DecimalFormat format = numField.getDecimalFormat();
                        //String val = format.format(numField.getDecimalValue());
                        if ( numField.getDecimalValue() != null ) {
                            jxl.write.Number number = new Number(j, i,
                                    numField.getDecimalValue().doubleValue());
                            sheet.addCell(number);
                        }
                    } else if ( field instanceof PasswordField) {
                        Label label = new Label(j, i,
                                PasswordField.PASSWORD_PLACEHOLDER);
                        sheet.addCell(label);
                    } else {
                        Label label = new Label(j, i, field.getStringValue());
                        sheet.addCell(label);
                    }

                    j++;
                }
                i++;
            }

            workbook.write();
        } catch (IOException e) {
            LogUtil.warning(logger, "IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (RowsExceededException e) {
            LogUtil.warning(logger, "RowsExceededException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (WriteException e) {
            LogUtil.warning(logger, "WriteException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            try {
                if (workbook != null)
                    workbook.close();
            }
            catch (Exception e) {
                LogUtil.warning(logger, "IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }
        contentType = "application/ms-excel; charset=UTF-8";
        fileName = fileTemp.getName() + ".xls";
    }

      //**************************************************************************
    // ExportRead
    //**************************************************************************

    public String exportRead() {
        setupTable();

        //Relationship relationship =
        //        table.findManyToOneByName(relName);
        Serializable pkObject = pkHelper.parsePkString(pk);

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(tableAccessor);
        searchFormBuilder.configFields("uuid","email","state",
                "lastName", "firstName");
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        Criteria criteria = new Criteria(tableAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        object = context.getObjectByPk(qualifiedTableName, pkObject);

        TableFormBuilder tableFormBuilder =
            createTableFormBuilderWithOptionProviders()
                            .configMode(Mode.VIEW)
                            .configNRows(objects.size());
        tableFormBuilder.configFields("uuid","email","state",
                "lastName", "firstName");
        tableForm = tableFormBuilder.build();
        tableForm.readFromObject(object);

        relatedTableFormList = new ArrayList<RelatedTableForm>();
        Table table = model.findTableByQualifiedName(qualifiedTableName);
        for (ForeignKey relationship : table.getOneToManyRelationships()) {
            setupRelatedTableForm(relationship);
        }

        String exportId = TempFiles.generateRandomCode();
        File fileTemp = TempFiles.getTempFile(EXPORT_FILENAME_FORMAT, exportId);

        createExportExcelRel(fileTemp);

        contentLength = fileTemp.length();

        try {
            inputStream = new FileInputStream(fileTemp);
        } catch (IOException e) {
            LogUtil.warning(logger, "IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        }
        return EXPORT;

    }

    private void createExportExcelRel(File fileTemp) {
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(fileTemp);
            WritableSheet sheet = workbook.createSheet("First Sheet", 0);

            int l = 0;
            for (TableForm.Column col : tableForm.getColumns()) {
                sheet.addCell(new Label(l, 0, col.getLabel()));
                l++;
            }

            int i = 1;
            for (TableForm.Row col : tableForm.getRows()) {
                int j = 0;

                for (Field field : Arrays.asList(col.getFields())) {
                    if ( field instanceof NumericField) {
                        //NumberFormat numberFormat = new NumberFormat();
                        NumericField numField = (NumericField)field;
                        //DecimalFormat format = numField.getDecimalFormat();
                        //String val = format.format(numField.getDecimalValue());
                        if ( numField.getDecimalValue() != null ) {
                            Number number = new Number(j, i,
                                    numField.getDecimalValue().doubleValue());
                            sheet.addCell(number);
                        }
                    } else if ( field instanceof PasswordField) {
                        Label label = new Label(j, i,
                                PasswordField.PASSWORD_PLACEHOLDER);
                        sheet.addCell(label);
                    } else {
                        Label label = new Label(j, i, field.getStringValue());
                        sheet.addCell(label);
                    }

                    j++;
                }
                i++;
            }


            workbook.write();
        } catch (IOException e) {
            LogUtil.warning(logger, "IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (RowsExceededException e) {
            LogUtil.warning(logger, "RowsExceededException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (WriteException e) {
            LogUtil.warning(logger, "WriteException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            try {
                if (workbook != null)
                    workbook.close();
            }
            catch (Exception e) {
                LogUtil.warning(logger, "IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }
        contentType = "application/ms-excel; charset=UTF-8";
        fileName = fileTemp.getName() + ".xls";
    }
}
