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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.fields.*;
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
import com.manydesigns.elements.util.RandomUtil;
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
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.fop.apps.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableDataAction extends PortofinoAction
        implements ServletRequestAware {
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

    public String qualifiedTableName;
    public String pk;
    public String[] selection;
    public String searchString;
    public String cancelReturnUrl;
    public String relName;
    public int optionProviderIndex;
    public String labelSearch;

    //**************************************************************************
    // Web parameters setters (for struts.xml inspections in IntelliJ)
    //**************************************************************************

    public void setQualifiedTableName(String qualifiedTableName) {
        this.qualifiedTableName = qualifiedTableName;
    }

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
            LogUtil.getLogger(TableDataAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    public String execute() {
        if (qualifiedTableName == null) {
            List<Table> tables = model.getAllTables();
            if (tables.isEmpty()) {
                return NO_TABLES;
            } else {
                qualifiedTableName = tables.get(0).getQualifiedName();
                return REDIRECT_TO_TABLE;
            }
        }
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
        StringBuilder sb = new StringBuilder("/model/");
        sb.append(tableAccessor.getName());
        sb.append("/TableData.action?pk=");
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
        form = createFormBuilderWithOptionProviders()
                .configMode(Mode.VIEW)
                .build();
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

        form = createFormBuilderWithOptionProviders()
                .configMode(Mode.CREATE)
                .build();

        return CREATE;
    }

    public String save() {
        setupTable();

        form = createFormBuilderWithOptionProviders()
                .configMode(Mode.CREATE)
                .build();

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

        form = createFormBuilderWithOptionProviders()
                .configMode(Mode.EDIT)
                .build();

        form.readFromObject(object);

        return EDIT;
    }

    public String update() {
        setupTable();
        Serializable pkObject = pkHelper.parsePkString(pk);

        form = createFormBuilderWithOptionProviders()
                .configMode(Mode.EDIT)
                .build();

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
    // Bulk Edit/Update
    //**************************************************************************

    public String bulkEdit() {
        if (selection == null || selection.length == 0) {
            SessionMessages.addWarningMessage(
                    "Nessun oggetto selezionato");
            return CANCEL;
        }

        if (selection.length == 1) {
            pk = selection[0];
            return edit();
        }

        setupTable();

        form = createFormBuilderWithOptionProviders()
                .configMode(Mode.BULK_EDIT)
                .build();

        return BULK_EDIT;
    }

    public String bulkUpdate() {
        setupTable();

        form = createFormBuilderWithOptionProviders()
                .configMode(Mode.BULK_EDIT)
                .build();
        form.readFromRequest(req);
        if (form.validate()) {
            for (String current : selection) {
                Serializable pkObject = pkHelper.parsePkString(current);
                object = context.getObjectByPk(qualifiedTableName, pkObject);
                form.writeToObject(object);
            }
            form.writeToObject(object);
            context.updateObject(qualifiedTableName, object);
            String databaseName = model.findTableByQualifiedName(qualifiedTableName)
                    .getDatabaseName();
            context.commit(databaseName);
            SessionMessages.addInfoMessage(MessageFormat.format(
                    "UPDATE di {0} oggetti avvenuto con successo", selection.length));
            return BULK_UPDATE;
        } else {
            return BULK_EDIT;
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
        TextFormat[] textFormats = null;
        if (shortNameAnnotation != null) {
            textFormats = new TextFormat[] {
                OgnlTextFormat.create(shortNameAnnotation.value())
            };
        }
        SelectionProvider selectionProvider =
                DefaultSelectionProvider.create(rel.getForeignKeyName(),
                        relatedObjects, classAccessor, textFormats);
        return selectionProvider;
    }


    //**************************************************************************
    // ExportSearch
    //**************************************************************************

    public String exportSearchExcel() {
        setupTable();

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(tableAccessor);
        searchForm = searchFormBuilder.build();
        searchForm.readFromRequest(req);

        Criteria criteria = new Criteria(tableAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        TableFormBuilder tableFormBuilder =
            createTableFormBuilderWithOptionProviders()
                            .configNRows(objects.size());
        tableForm = tableFormBuilder.configMode(Mode.VIEW)
                .build();
        tableForm.readFromObject(objects);

        writeFileSearchExcel();

        return EXPORT;
    }

    private void writeFileSearchExcel() {
        File fileTemp = createExportTempFile();
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(fileTemp);
            WritableSheet sheet = workbook.createSheet(qualifiedTableName, 0);

            addHeaderToSheet(sheet);

            int i = 1;
            for ( TableForm.Row row : tableForm.getRows()) {
                exportRows(sheet, i, row);
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
        paramExport(fileTemp);
    }

    //**************************************************************************
    // ExportRead
    //**************************************************************************

    public String exportReadExcel() {
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

        TableFormBuilder tableFormBuilder =
            createTableFormBuilderWithOptionProviders()
                            .configMode(Mode.VIEW)
                            .configNRows(objects.size());
        tableForm = tableFormBuilder.build();
        tableForm.readFromObject(object);

        form = createFormBuilderWithOptionProviders()
                .configMode(Mode.VIEW)
                .build();
        form.readFromObject(object);

        relatedTableFormList = new ArrayList<RelatedTableForm>();
        Table table = model.findTableByQualifiedName(qualifiedTableName);
        for (ForeignKey relationship : table.getOneToManyRelationships()) {
            setupRelatedTableForm(relationship);
        }

        writeFileReadExcel();

        return EXPORT;
    }


    private void writeFileReadExcel() {
        File fileTemp = createExportTempFile();
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(fileTemp);
            WritableSheet sheet = workbook.createSheet(qualifiedTableName, 0);

            addHeaderToSheet(sheet);

            int i = 1;
            for (FieldSet fieldset : form) {
                int j = 0;
                for (Field field : fieldset) {
                    addFieldToCell(sheet, i, j, field);
                    j++;
                }
                i++;
            }

            //Aggiungo le relazioni/sheet
           int k = 1;
           WritableCellFormat formatCell = headerExcel();
           for (RelatedTableForm relTabForm : relatedTableFormList) {
                sheet = workbook.createSheet(relTabForm.relationship.
                        getFromTable().getQualifiedName() , k);
                k++;
                int m = 0;
                for (TableForm.Column col : relTabForm.tableForm.getColumns()) {
                    sheet.addCell(new Label(m, 0, col.getLabel(), formatCell));
                    m++;
                }
                i = 1;
                for (TableForm.Row row : relTabForm.tableForm.getRows()) {
                    int j = 0;
                    for (Field field : Arrays.asList(row.getFields())) {
                        addFieldToCell(sheet, i, j, field);
                        j++;
                    }
                    i++;
                }
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

        paramExport(fileTemp);
    }


    private WritableCellFormat headerExcel() {
        WritableFont fontCell = new WritableFont(WritableFont.ARIAL, 12,
             WritableFont.BOLD, true);
        return new WritableCellFormat (fontCell);
    }

    private void exportRows(WritableSheet sheet, int i,
                            TableForm.Row row) throws WriteException {
        int j = 0;
        for (Field field : row.getFields()) {
            addFieldToCell(sheet, i, j, field);

            j++;
        }
    }

    private File createExportTempFile() {
         String exportId = RandomUtil.createRandomCode();
         return RandomUtil.getTempCodeFile(EXPORT_FILENAME_FORMAT, exportId);
     }


    private void paramExport(File fileTemp) {
        contentType = "application/ms-excel; charset=UTF-8";
        fileName = fileTemp.getName() + ".xls";

        contentLength = fileTemp.length();

        try {
            inputStream = new FileInputStream(fileTemp);
        } catch (IOException e) {
            LogUtil.warning(logger, "IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        }
    }

    private void addHeaderToSheet(WritableSheet sheet) throws WriteException {
        WritableCellFormat formatCell = headerExcel();
        int l = 0;
        for (TableForm.Column col : tableForm.getColumns()) {
            sheet.addCell(new Label(l, 0, col.getLabel(), formatCell));
            l++;
        }
    }

    private void addFieldToCell(WritableSheet sheet, int i, int j,
                                Field field) throws WriteException {
        if (field instanceof NumericField) {
            NumericField numField = (NumericField) field;
            if (numField.getDecimalValue() != null) {
                Number number;
                BigDecimal decimalValue = numField.getDecimalValue();
                if (numField.getDecimalFormat() == null) {
                    number = new Number(j, i,
                            decimalValue == null
                                    ? null : decimalValue.doubleValue());
                } else {
                    NumberFormat numberFormat = new NumberFormat(
                            numField.getDecimalFormat().toPattern());
                    WritableCellFormat writeCellNumberFormat =
                            new WritableCellFormat(numberFormat);
                    number = new Number(j, i,
                            decimalValue == null
                                    ? null : decimalValue.doubleValue(),
                            writeCellNumberFormat);
                }
                sheet.addCell(number);
            }
        } else if (field instanceof PasswordField) {
            Label label = new Label(j, i,
                    PasswordField.PASSWORD_PLACEHOLDER);
            sheet.addCell(label);
        } else if (field instanceof DateField) {
            DateField dateField = (DateField) field;
            DateTime dateCell;
            Date date = dateField.getDateValue();
            if (date != null) {
                DateFormat dateFormat = new DateFormat(
                        dateField.getDatePattern());
                WritableCellFormat wDateFormat =
                        new WritableCellFormat(dateFormat);
                dateCell = new DateTime(j, i,
                        dateField.getDateValue() == null
                                ? null : dateField.getDateValue(),
                        wDateFormat);
                sheet.addCell(dateCell);
            }
        } else {
            Label label = new Label(j, i, field.getStringValue());
            sheet.addCell(label);
        }
    }



    //**************************************************************************
    // ExportReadPdf
    //**************************************************************************

    public String exportSearchPdf() throws FOPException,
            IOException, TransformerException {
        FopFactory fopFactory = FopFactory.newInstance();


        FileOutputStream out = null;
        File tempPdfFile = createExportTempFile();
        try {
                               
            File fo = new File("/Users/proprietario/FOP/example.xml");
            File xsl = new File("/Users/proprietario/FOP/example.xsl");
            out = new FileOutputStream(tempPdfFile);

            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            ClassLoader cl = getClass().getClassLoader();
            //InputStream xsltStream = cl.getResourceAsStream(
            //        "templateFOP.xsl");

            // Setup XSLT
            TransformerFactory tFactory = TransformerFactory.newInstance();
            //Transformer transformer = tFactory.newTransformer(new StreamSource(
            //        xsltStream));
            Transformer transformer = tFactory.newTransformer(new StreamSource(xsl));

            // Set the value of a <param> in the stylesheet
            transformer.setParameter("versionParam", "2.0");


            // Setup input for XSLT transformation
            //String xml = composeXml();
            //Source src = new StreamSource(xml);
            Source src = new StreamSource(fo);

            // Resulting SAX events (the generated FO) must be piped through to
            // FOP
            Result res = new SAXResult(fop.getDefaultHandler());


            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);


            out.flush();


        } catch (Exception e) {
            LogUtil.warning(logger, "IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
            }
            catch (Exception e) {
                LogUtil.warning(logger, "IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }


        inputStream = new FileInputStream(tempPdfFile);

        contentType = "application/pdf";

        fileName = tempPdfFile.getName() + ".pdf";

        contentLength = tempPdfFile.length();

        return EXPORT;
    }

    public String composeXml() {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
sb.append("<projectteam>");
sb.append("<projectname>The Killer Application</projectname>");
sb.append("<member>");
sb.append("<name>John Doe</name>");
sb.append("<function>lead</function>");
sb.append("<email>jon.doe@killerapp.fun</email>");
sb.append("</member>");
sb.append("<member>");
sb.append("<name>Paul Coder</name>");
sb.append("<function>dev</function>");
sb.append("<email>paul.coder@killerapp.fun</email>");
sb.append("</member>");
sb.append("<member>");
sb.append("<name>Max Hacker</name>");
sb.append("<function>dev</function>");
sb.append("<email>max.hacker@killerapp.fun</email>");
sb.append("</member>");
sb.append("<member>");
sb.append("<name>Donna Book</name>");
sb.append("<function>doc</function>");
sb.append("<email>donna.book@killerapp.fun</email>");
sb.append("</member>");
sb.append("<member>");
sb.append("<name>Henry Tester</name>");
sb.append("<function>qa</function>");
sb.append("<email>henry.tester@killerapp.fun</email>");
sb.append("</member>");
sb.append("</projectteam>");
      /*  sb.append("<fax>");

        sb.append("<mittente>");
        sb.append("prova");
        sb.append("</mittente>");

        sb.append("<oggetto>");
        sb.append("prova");
        sb.append("</oggetto>");


        sb.append("<destinatario>");
        
        sb.append("prova");
        sb.append("</destinatario>");


        sb.append("<field name=\"");
        sb.append("prova");
        sb.append("\" value=\"");
        sb.append("prova");
        sb.append("\" />");

        sb.append("</fax>");
                              */
        return sb.toString();
    }

}
