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
import com.manydesigns.elements.fields.*;
import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.struts2.Struts2Util;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.datamodel.Reference;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.usecases.Button;
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;
import com.opensymphony.xwork2.util.CompoundRoot;
import com.opensymphony.xwork2.util.ValueStack;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import ognl.OgnlException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
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
public class CrudUnit {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    public final ClassAccessor classAccessor;
    public final Table baseTable;
    public final String query;
    public final String searchTitle;
    public final String createTitle;
    public final String readTitle;
    public final String editTitle;
    public final PkHelper pkHelper;
    public final List<CrudUnit> subCrudUnits;
    public final List<Button> buttons;

    public Context context;
    public Model model;
    public HttpServletRequest req;
    public String pk;
    public String[] selection;
    public String searchString;

    public List objects;
    public Object object;
    public SearchForm searchForm;
    public TableForm tableForm;
    public Form form;


    
    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LogUtil.getLogger(AbstractCrudAction.class);


    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public CrudUnit(ClassAccessor classAccessor,
                    Table baseTable,
                    String query,
                    String searchTitle,
                    String createTitle,
                    String readTitle,
                    String editTitle
    ) {
        this.buttons = new ArrayList<Button>();
        this.classAccessor = classAccessor;
        this.baseTable = baseTable;
        this.query = query;
        this.searchTitle = searchTitle;
        this.createTitle = createTitle;
        this.readTitle = readTitle;
        this.editTitle = editTitle;
        pkHelper = new PkHelper(classAccessor);
        subCrudUnits = new ArrayList<CrudUnit>();
    }

    //--------------------------------------------------------------------------
    // Crud operations
    //--------------------------------------------------------------------------

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
        buildSearchForm();
        configureSearchFormFromString();

        return commonSearch();
    }

    private void buildSearchForm() {
        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
        searchForm = searchFormBuilder.build();
    }

    public String search() {
        buildSearchForm();
        searchForm.readFromRequest(req);

        return commonSearch();
    }

    public String commonSearch() {
        searchString = searchForm.toSearchString();
        if (searchString.length() == 0) {
            searchString = null;
        }

        loadObjectsFromCriteria();

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

        return PortofinoAction.SEARCH;
    }

    public void loadObjectsFromCriteria() {
        ValueStack valueStack = Struts2Util.getValueStack();
        CompoundRoot root = valueStack.getRoot();

        Criteria criteria = new Criteria(classAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(query, criteria, root);
    }

    public String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder();
        sb.append(Struts2Util.buildActionUrl(null));
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



    //**************************************************************************
    // Read
    //**************************************************************************

    public String read() {
        Serializable pkObject = pkHelper.parsePkString(pk);

        buildSearchForm();
        configureSearchFormFromString();

        loadObjectsFromCriteria();

        object = context.getObjectByPk(
                baseTable.getQualifiedName(), pkObject);
        if (!objects.contains(object)) {
            throw new Error("Object not found");
        }
        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.VIEW)
                .build();
        form.readFromObject(object);
        refreshBlobDownloadHref();


        ValueStack valueStack = Struts2Util.getValueStack();

        valueStack.push(object);
        for (CrudUnit subCrudUnit : subCrudUnits) {
            subCrudUnit.search();
        }
        valueStack.pop();

        return PortofinoAction.READ;
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
        sb.append(Struts2Util.buildActionUrl("downloadBlob"));
        sb.append("?code=");
        sb.append(Util.urlencode(code));
        return Util.getAbsoluteUrl(sb.toString());
    }

    //**************************************************************************
    // Create/Save
    //**************************************************************************

    public String create() {
        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.CREATE)
                .build();

        return PortofinoAction.CREATE;
    }

    public String save() {
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
            return PortofinoAction.SAVE;
        } else {
            return PortofinoAction.CREATE;
        }
    }

    //**************************************************************************
    // Edit/Update
    //**************************************************************************

    public String edit() {
        loadObject(pk);

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.EDIT)
                .build();

        form.readFromObject(object);

        return PortofinoAction.EDIT;
    }

    public String update() {
        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.EDIT)
                .build();

        loadObject();
        form.readFromObject(object);
        form.readFromRequest(req);
        if (form.validate()) {
            form.writeToObject(object);
            context.updateObject(baseTable.getQualifiedName(), object);
            context.commit(baseTable.getDatabaseName());
            SessionMessages.addInfoMessage("UPDATE avvenuto con successo");
            return PortofinoAction.UPDATE;
        } else {
            return PortofinoAction.EDIT;
        }
    }

    //**************************************************************************
    // Bulk Edit/Update
    //**************************************************************************

    public String bulkEdit() {
        if (selection == null || selection.length == 0) {
            SessionMessages.addWarningMessage(
                    "Nessun oggetto selezionato");
            return PortofinoAction.CANCEL;
        }

        if (selection.length == 1) {
            pk = selection[0];
            return edit();
        }

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.BULK_EDIT)
                .build();

        return PortofinoAction.BULK_EDIT;
    }

    public String bulkUpdate() {
        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.BULK_EDIT)
                .build();
        form.readFromRequest(req);
        if (form.validate()) {
            for (String current : selection) {
                loadObject(current);
                form.writeToObject(object);
            }
            form.writeToObject(object);
            context.updateObject(baseTable.getQualifiedName(), object);
            context.commit(baseTable.getDatabaseName());
            SessionMessages.addInfoMessage(MessageFormat.format(
                    "UPDATE di {0} oggetti avvenuto con successo",
                    selection.length));
            return PortofinoAction.BULK_UPDATE;
        } else {
            return PortofinoAction.BULK_EDIT;
        }
    }

    private void loadObject() {
        loadObject(pk);
    }

    private void loadObject(String pk) {
        Serializable pkObject = pkHelper.parsePkString(pk);
        object = context.getObjectByPk(baseTable.getQualifiedName(), pkObject);
    }

    //**************************************************************************
    // Delete
    //**************************************************************************

    public String delete() {
        Object pkObject = pkHelper.parsePkString(pk);
        context.deleteObject(baseTable.getQualifiedName(), pkObject);
        context.commit(baseTable.getDatabaseName());
        SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        return PortofinoAction.DELETE;
    }

    public String bulkDelete() {
        if (selection == null) {
            SessionMessages.addWarningMessage(
                    "DELETE non avvenuto: nessun oggetto selezionato");
            return PortofinoAction.CANCEL;
        }
        for (String current : selection) {
            Object pkObject = pkHelper.parsePkString(current);
            context.deleteObject(baseTable.getQualifiedName(), pkObject);
        }
        context.commit(baseTable.getDatabaseName());
        SessionMessages.addInfoMessage(MessageFormat.format(
                "DELETE di {0} oggetti avvenuto con successo",
                selection.length));
        return PortofinoAction.DELETE;
    }

    //**************************************************************************
    // Button
    //**************************************************************************

    public String button() throws OgnlException {
        String value = req.getParameter("method:button");
        for (Button button : buttons) {
            if (button.getLabel().equals(value)) {
                String script = button.getScript();
                return (String) Struts2Util.getValue(script);
            }
        }
        throw new Error("No button found");
    }

    //**************************************************************************
    // Ajax
    //**************************************************************************

    public String jsonOptions(String relName, int selectionProviderIndex,
                              String labelSearch, boolean includeSelectPrompt) {
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
        return text;
    }


    //**************************************************************************
    // Utility methods
    //**************************************************************************

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



    //**************************************************************************
    // ExportSearch
    //**************************************************************************

    public void exportSearchExcel(File fileTemp) {
        buildSearchForm();
        searchForm.readFromRequest(req);

        loadObjectsFromCriteria();

        TableFormBuilder tableFormBuilder =
            createTableFormBuilderWithSelectionProviders()
                            .configNRows(objects.size());
        tableForm = tableFormBuilder.configMode(Mode.VIEW)
                .build();
        tableForm.readFromObject(objects);

        writeFileSearchExcel(fileTemp);
    }

    private void writeFileSearchExcel(File fileTemp) {
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(fileTemp);
            WritableSheet sheet =
                    workbook.createSheet(classAccessor.getName(), 0);

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
    }

    //**************************************************************************
    // ExportRead
    //**************************************************************************

    public void exportReadExcel(WritableWorkbook workbook)
            throws IOException, WriteException {
        buildSearchForm();
        configureSearchFormFromString();

        loadObjectsFromCriteria();
        loadObject();

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

        writeFileReadExcel(workbook);
    }


    private void writeFileReadExcel(WritableWorkbook workbook)
            throws IOException, WriteException {
        WritableSheet sheet =
                workbook.createSheet(classAccessor.getName(),
                        workbook.getNumberOfSheets());

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

        ValueStack valueStack = Struts2Util.getValueStack();
        valueStack.push(object);

        //Aggiungo le relazioni/sheet
        WritableCellFormat formatCell = headerExcel();
        for (CrudUnit subCrudUnit: subCrudUnits) {
            subCrudUnit.buildSearchForm();
            subCrudUnit.loadObjectsFromCriteria();
            TableFormBuilder tableFormBuilder =
                    subCrudUnit.createTableFormBuilderWithSelectionProviders();
            TableForm subTableForm = tableFormBuilder
                    .configNRows(subCrudUnit.objects.size())
                    .build();
            subTableForm.readFromObject(subCrudUnit.objects);

            sheet = workbook.createSheet(subCrudUnit.searchTitle ,
                    workbook.getNumberOfSheets());

            int m = 0;
            for (TableForm.Column col : subTableForm.getColumns()) {
                sheet.addCell(new Label(m, 0, col.getLabel(), formatCell));
                m++;
            }
            i = 1;
            for (TableForm.Row row : subTableForm.getRows()) {
                int j = 0;
                for (Field field : Arrays.asList(row.getFields())) {
                    addFieldToCell(sheet, i, j, field);
                    j++;
                }
                i++;
            }
        }
        
        valueStack.pop();

        workbook.write();
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

    public void exportSearchPdf(File tempPdfFile) throws FOPException,
            IOException, TransformerException {
        buildSearchForm();
        searchForm.readFromRequest(req);

        loadObjectsFromCriteria();

        TableFormBuilder tableFormBuilder =
            createTableFormBuilderWithSelectionProviders()
                            .configNRows(objects.size());
        tableForm = tableFormBuilder.configMode(Mode.VIEW)
                .build();
        tableForm.readFromObject(objects);

        FopFactory fopFactory = FopFactory.newInstance();

        FileOutputStream out = null;
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
    }

    public String composeXml() {
        // TODO: per favore usa XmlBuffer
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<class>");
        sb.append("<table>");
        sb.append(classAccessor.getName());
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
