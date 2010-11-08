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

package com.manydesigns.portofino.actions;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobsManager;
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
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.datamodel.Reference;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;

import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import jxl.Workbook;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractCrudAction extends PortofinoAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Common action results
    //**************************************************************************


    //**************************************************************************
    // Constants
    //**************************************************************************

    public final String DEFAULT_EXPORT_FILENAME_FORMAT = "export-{0}";

    //**************************************************************************
    // ServletRequestAware implementation
    //**************************************************************************

    public HttpServletRequest req;

    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }

    //**************************************************************************
    // Configuration parameters and setters (for struts.xml inspections in IntelliJ)
    //**************************************************************************

    public String qualifiedName;
    public String exportFilenameFormat = DEFAULT_EXPORT_FILENAME_FORMAT;

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public void setExportFilenameFormat(String exportFilenameFormat) {
        this.exportFilenameFormat = exportFilenameFormat;
    }

    //**************************************************************************
    // Web parameters
    //**************************************************************************

    public String pk;
    public String[] selection;
    public String searchString;
    public String cancelReturnUrl;
    public String relName;
    public int selectionProviderIndex;
    public String labelSearch;
    public String code;

    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public ClassAccessor classAccessor;
    public Table baseTable;

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
    public String errorMessage;

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
            LogUtil.getLogger(AbstractCrudAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    public String execute() {
        if (qualifiedName == null) {
            return redirectToFirst();
        } else if (pk == null) {
            return searchFromString();
        } else {
            return read();
        }
    }

    public abstract String redirectToFirst();

    //**************************************************************************
    // Search
    //**************************************************************************

    public String searchFromString() {
        setupMetadata();

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
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
        setupMetadata();

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
        searchForm = searchFormBuilder.build();
        searchForm.readFromRequest(req);

        return commonSearch();
    }

    protected String commonSearch() {
        searchString = searchForm.toSearchString();
        if (searchString.length() == 0) {
            searchString = null;
        }

        setupCriteria();

        String readLinkExpression = getReadLinkExpression();
        OgnlTextFormat hrefFormat =
                OgnlTextFormat.create(readLinkExpression);
        hrefFormat.setUrl(true);

        TableFormBuilder tableFormBuilder =
                createTableFormBuilderWithSelectionProviders()
                        .configNRows(objects.size())
                        .configMode(Mode.VIEW);

        // ogni colonna chiave primaria sarà clickabile
        for (PropertyAccessor property : classAccessor.getKeyProperties()) {
            tableFormBuilder.configHyperlinkGenerators(
                    property.getName(), hrefFormat, null);
        }

        tableForm = tableFormBuilder.build();
        tableForm.setKeyGenerator(pkHelper.createPkGenerator());
        tableForm.setSelectable(true);
        tableForm.readFromObject(objects);

        return SEARCH;
    }

    public void setupCriteria() {
        Criteria criteria = new Criteria(classAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);
    }

    public String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder();
        sb.append(buildActionUrl(null));
        sb.append("?pk=");
        boolean first = true;
        for (PropertyAccessor property : classAccessor.getKeyProperties()) {
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

    private String buildActionUrl(String method) {
        ActionContext actionContext = ActionContext.getContext();
        ActionInvocation actionInvocation = actionContext.getActionInvocation();
        ActionProxy actionProxy = actionInvocation.getProxy();
        String namespace = actionProxy.getNamespace();
        String actionName = actionProxy.getActionName();

        StringBuilder sb = new StringBuilder();
        if ("/".equals(namespace)) {
            sb.append("/");
        } else {
            sb.append(namespace);
            sb.append("/");
        }
        sb.append(actionName);
        if (method != null) {
            sb.append("!");
            sb.append(method);
        }
        sb.append(".action");
        return sb.toString();
    }

    //**************************************************************************
    // Return to search
    //**************************************************************************

    public String returnToSearch() {
        setupMetadata();
        return RETURN_TO_SEARCH;
    }

    //**************************************************************************
    // Read
    //**************************************************************************

    public String read() {
        setupMetadata();
        Serializable pkObject = pkHelper.parsePkString(pk);

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        setupCriteria();

        object = context.getObjectByPk(
                baseTable.getQualifiedName(), pkObject);
        if (!objects.contains(object)) {
            errorMessage = "Object not found";
            return STATUS_404;
        }
        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.VIEW)
                .build();
        form.readFromObject(object);
        refreshBlobDownloadHref();

        relatedTableFormList = new ArrayList<RelatedTableForm>();

        for (ForeignKey relationship : baseTable.getOneToManyRelationships()) {
            setupRelatedTableForm(relationship);
        }

        return READ;
    }

    protected abstract void setupRelatedTableForm(ForeignKey relationship);

    //**************************************************************************
    // Blobs
    //**************************************************************************

    public String downloadBlob() throws IOException {
        Blob blob = BlobsManager.getManager().loadBlob(code);
        contentLength = blob.getSize();
        contentType = blob.getContentType();
        inputStream = new FileInputStream(blob.getDataFile());
        fileName = blob.getFilename();
        return EXPORT;
    }

    protected void refreshBlobDownloadHref() {
        for (FieldSet fieldSet : form) {
            for (Field field : fieldSet) {
                if (field instanceof FileBlobField) {
                    FileBlobField fileBlobField = (FileBlobField) field;
                    Blob blob = fileBlobField.getBlob();
                    if (blob != null) {
                        String url = getBlobDownloadUrl(blob.getCode());
                        field.setHref(url);
                    }
                }
            }
        }
    }

    public String getBlobDownloadUrl(String code) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildActionUrl("downloadBlob"));
        sb.append("?code=");
        sb.append(Util.urlencode(code));
        return Util.getAbsoluteUrl(sb.toString());
    }


    //**************************************************************************
    // Create/Save
    //**************************************************************************

    public String create() {
        setupMetadata();

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.CREATE)
                .build();

        return CREATE;
    }

    public String save() {
        setupMetadata();

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.CREATE)
                .build();

        form.readFromRequest(req);
        if (form.validate()) {
            object = classAccessor.newInstance();
            form.writeToObject(object);
            context.saveObject(baseTable.getQualifiedName(), object);
            context.commit(baseTable.getDatabaseName());
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
        setupMetadata();
        Serializable pkObject = pkHelper.parsePkString(pk);

        object = context.getObjectByPk(
                baseTable.getQualifiedName(), pkObject);

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.EDIT)
                .build();

        form.readFromObject(object);

        return EDIT;
    }

    public String update() {
        setupMetadata();
        Serializable pkObject = pkHelper.parsePkString(pk);

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.EDIT)
                .build();

        object = context.getObjectByPk(
                baseTable.getQualifiedName(), pkObject);
        form.readFromObject(object);
        form.readFromRequest(req);
        if (form.validate()) {
            form.writeToObject(object);
            context.updateObject(baseTable.getQualifiedName(), object);
            context.commit(baseTable.getDatabaseName());
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

        setupMetadata();

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.BULK_EDIT)
                .build();

        return BULK_EDIT;
    }

    public String bulkUpdate() {
        setupMetadata();

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.BULK_EDIT)
                .build();
        form.readFromRequest(req);
        if (form.validate()) {
            for (String current : selection) {
                Serializable pkObject = pkHelper.parsePkString(current);
                object = context.getObjectByPk(
                        baseTable.getQualifiedName(), pkObject);
                form.writeToObject(object);
            }
            form.writeToObject(object);
            context.updateObject(baseTable.getQualifiedName(), object);
            context.commit(baseTable.getDatabaseName());
            SessionMessages.addInfoMessage(MessageFormat.format(
                    "UPDATE di {0} oggetti avvenuto con successo",
                    selection.length));
            return BULK_UPDATE;
        } else {
            return BULK_EDIT;
        }
    }

    //**************************************************************************
    // Delete
    //**************************************************************************

    public String delete() {
        setupMetadata();
        Object pkObject = pkHelper.parsePkString(pk);
        context.deleteObject(baseTable.getQualifiedName(), pkObject);
        context.commit(baseTable.getDatabaseName());
        SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        return DELETE;
    }

    public String bulkDelete() {
        setupMetadata();
        if (selection == null) {
            SessionMessages.addWarningMessage(
                    "DELETE non avvenuto: nessun oggetto selezionato");
            return CANCEL;
        }
        for (String current : selection) {
            Object pkObject = pkHelper.parsePkString(current);
            context.deleteObject(baseTable.getQualifiedName(), pkObject);
        }
        context.commit(baseTable.getDatabaseName());
        SessionMessages.addInfoMessage(MessageFormat.format(
                "DELETE di {0} oggetti avvenuto con successo",
                selection.length));
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
        setupMetadata();
        ForeignKey relationship =
                baseTable.findForeignKeyByName(relName);

        String[] fieldNames = createFieldNamesForRelationship(relationship);
        SelectionProvider selectionProvider =
                createSelectionProviderForRelationship(relationship);

        Form form = new FormBuilder(classAccessor)
                .configFields(fieldNames)
                .configSelectionProvider(selectionProvider, fieldNames)
                .configMode(Mode.EDIT)
                .build();
        form.readFromRequest(req);

        SelectField targetField =
                (SelectField) form.get(0).get(selectionProviderIndex);
        targetField.setLabelSearch(labelSearch);

        String text = targetField.jsonSelectFieldOptions(includeSelectPrompt);
        LogUtil.infoMF(logger, "jsonSelectFieldOptions: {0}", text);

        inputStream = new StringBufferInputStream(text);

        return JSON_SELECT_FIELD_OPTIONS;
    }

    public abstract void setupMetadata();

    protected FormBuilder createFormBuilderWithSelectionProviders() {
        FormBuilder formBuilder = new FormBuilder(classAccessor);

        // setup relationship lookups
        for (ForeignKey rel : baseTable.getForeignKeys()) {
            String[] fieldNames = createFieldNamesForRelationship(rel);
            SelectionProvider selectionProvider =
                    createSelectionProviderForRelationship(rel);
            boolean autocomplete = false;
            for (ModelAnnotation current : rel.getModelAnnotations()) {
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

    protected TableFormBuilder createTableFormBuilderWithSelectionProviders() {
        TableFormBuilder tableFormBuilder = new TableFormBuilder(classAccessor);

        // setup relationship lookups
        for (ForeignKey rel : baseTable.getForeignKeys()) {
            String[] fieldNames = createFieldNamesForRelationship(rel);
            SelectionProvider selectionProvider =
                    createSelectionProviderForRelationship(rel);
            boolean autocomplete = false;
            for (ModelAnnotation current : rel.getModelAnnotations()) {
                if ("com.manydesigns.elements.annotations.Autocomplete"
                        .equals(current.getType())) {
                    autocomplete = true;
                }
            }
            selectionProvider.setAutocomplete(autocomplete);

            tableFormBuilder.configSelectionProvider(selectionProvider, fieldNames);
        }
        return tableFormBuilder;
    }

    protected String[] createFieldNamesForRelationship(ForeignKey rel) {
        List<Reference> references = rel.getReferences();
        String[] fieldNames = new String[references.size()];
        int i = 0;
        for (Reference reference : references) {
            Column column = reference.getActualFromColumn();
            fieldNames[i] = column.getActualPropertyName();
            i++;
        }
        return fieldNames;
    }

    protected SelectionProvider createSelectionProviderForRelationship(ForeignKey rel) {
        // retrieve the related objects
        Table relatedTable = rel.getActualToTable();
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
    // Utility methods
    //**************************************************************************

    //**************************************************************************
    // ExportSearch
    //**************************************************************************

    public String exportSearchExcel() {
        setupMetadata();

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
        searchForm = searchFormBuilder.build();
        searchForm.readFromRequest(req);

        Criteria criteria = new Criteria(classAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        TableFormBuilder tableFormBuilder =
            createTableFormBuilderWithSelectionProviders()
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
            WritableSheet sheet = workbook.createSheet(qualifiedName, 0);

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
        setupMetadata();
        Serializable pkObject = pkHelper.parsePkString(pk);

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        Criteria criteria = new Criteria(classAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        object = context.getObjectByPk(qualifiedName, pkObject);

        TableFormBuilder tableFormBuilder =
            createTableFormBuilderWithSelectionProviders()
                            .configMode(Mode.VIEW)
                            .configNRows(objects.size());
        tableForm = tableFormBuilder.build();
        tableForm.readFromObject(object);

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.VIEW)
                .build();
        form.readFromObject(object);

        relatedTableFormList = new ArrayList<RelatedTableForm>();
        Table table = model.findTableByQualifiedName(qualifiedName);
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
            WritableSheet sheet = workbook.createSheet(qualifiedName, 0);

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
         return RandomUtil.getTempCodeFile(exportFilenameFormat, exportId);
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
        setupMetadata();

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
        searchForm = searchFormBuilder.build();
        searchForm.readFromRequest(req);

        Criteria criteria = new Criteria(classAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        TableFormBuilder tableFormBuilder =
            createTableFormBuilderWithSelectionProviders()
                            .configNRows(objects.size());
        tableForm = tableFormBuilder.configMode(Mode.VIEW)
                .build();
        tableForm.readFromObject(objects);

        FopFactory fopFactory = FopFactory.newInstance();

        FileOutputStream out = null;
        File tempPdfFile = createExportTempFile();
        try {
            out = new FileOutputStream(tempPdfFile);

            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            ClassLoader cl = getClass().getClassLoader();
            InputStream xsltStream = cl.getResourceAsStream(
                   "templateFOP.xsl");

            // Setup XSLT
            TransformerFactory Factory = TransformerFactory.newInstance();
            Transformer transformer = Factory.newTransformer(new StreamSource(
                    xsltStream));

            // Set the value of a <param> in the stylesheet
            transformer.setParameter("versionParam", "2.0");

            // Setup input for XSLT transformation
            String xml = composeXml();
            Source src = new StreamSource(new StringReader(xml));

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
        sb.append("<class>");
        sb.append("<table>");
        sb.append(qualifiedName);
        sb.append("</table>");
        for (TableForm.Row row : tableForm.getRows()) {
            for (Field field : row.getFields()) {
                sb.append("<header>");
                sb.append("<nameColumn>");
                sb.append(field.getLabel());
                sb.append("</nameColumn>");
                sb.append("</header>");
            }
        }

        for (TableForm.Row row : tableForm.getRows()) {
            for (Field field : row.getFields()) {
                sb.append("<row>");
                sb.append("<value>");
                sb.append(field.getStringValue());
                sb.append("</value>");
                sb.append("</row>");
            }
        }
        sb.append("</class>");
        return sb.toString();
    }

}
