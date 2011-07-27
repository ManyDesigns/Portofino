/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.fields.*;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.struts2.Struts2Utils;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XmlBuffer;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.context.TableCriteria;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.site.crud.Button;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class CrudUnit {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    public final ClassAccessor classAccessor;
    public final Table baseTable;
    public final String name;
    public final String prefix;
    public final String query;
    public final String searchTitle;
    public final String createTitle;
    public final String readTitle;
    public final String editTitle;
    public final PkHelper pkHelper;
    public final List<CrudUnit> subCrudUnits;
    public final List<CrudButton> crudButtons;
    public final List<CrudSelectionProvider> crudSelectionProviders;
    private final boolean first;

    public Application application;
    public Model model;
    public HttpServletRequest req;

    //--------------------------------------------------------------------------
    // Web parameters
    //--------------------------------------------------------------------------

    public String pk;
    public String[] selection;
    public String searchString;

    //--------------------------------------------------------------------------
    // UI forms
    //--------------------------------------------------------------------------

    public SearchForm searchForm;
    public TableForm tableForm;
    public Form form;

    //--------------------------------------------------------------------------
    // Data objects
    //--------------------------------------------------------------------------

    public List objects;
    public Object object;

    
    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractCrudAction.class);
    private static final String CONSTRAINT_VIOLATION = "Constraint violation";

    //**************************************************************************
    // Export
    //**************************************************************************
    private static final String TEMPLATE_FOP_SEARCH = "templateFOP-Search.xsl";
    private static final String TEMPLATE_FOP_READ = "templateFOP-Read.xsl";


    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public CrudUnit(ClassAccessor classAccessor,
                    Table baseTable,
                    String query,
                    String searchTitle,
                    String createTitle,
                    String readTitle,
                    String editTitle,
                    String name,
                    String prefix,
                    boolean first) {
        this.crudButtons = new ArrayList<CrudButton>();
        this.classAccessor = classAccessor;
        this.baseTable = baseTable;
        this.query = query;
        this.searchTitle = searchTitle;
        this.createTitle = createTitle;
        this.readTitle = readTitle;
        this.editTitle = editTitle;
        this.name=name;
        this.prefix = prefix;
        this.first = first;
        pkHelper = new PkHelper(classAccessor);
        subCrudUnits = new ArrayList<CrudUnit>();
        crudSelectionProviders = new ArrayList<CrudSelectionProvider>();
    }

    //--------------------------------------------------------------------------
    // Crud operations
    //--------------------------------------------------------------------------

    public String execute() {
        if (StringUtils.isEmpty(pk)) {
            return search();
        } else {
            return read();
        }
    }
    
    //**************************************************************************
    // Search
    //**************************************************************************

    public String search() {
        setupSearchForm();
        loadObjects();
        setupTableForm(Mode.VIEW);
        return PortofinoAction.SEARCH;
    }

    //**************************************************************************
    // Read
    //**************************************************************************

    public String read() {
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
        sb.append(Struts2Utils.buildActionUrl("downloadBlob"));
        sb.append("?code=");
        sb.append(Util.urlencode(code));
        return Util.getAbsoluteUrl(sb.toString());
    }

    //**************************************************************************
    // Create/Save
    //**************************************************************************

    public String create() {
        setupForm(Mode.CREATE);

        return PortofinoAction.CREATE;
    }

    public String save() {
        setupForm(Mode.CREATE);

        form.readFromRequest(req);
        if (form.validate()) {
            object = classAccessor.newInstance();
            form.writeToObject(object);
            application.saveObject(baseTable.getQualifiedName(), object);
            try {
                application.commit(baseTable.getDatabaseName());
            } catch (Throwable e) {
                String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
                logger.warn(rootCauseMessage, e);
                SessionMessages.addErrorMessage(rootCauseMessage);
                return PortofinoAction.CREATE;
            }
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
        loadObject();
        setupForm(Mode.EDIT);
        form.readFromObject(object);
        return PortofinoAction.EDIT;
    }

    public String update() {
        setupForm(Mode.EDIT);
        loadObject();
        form.readFromObject(object);
        form.readFromRequest(req);
        if (form.validate()) {
            form.writeToObject(object);
            application.updateObject(baseTable.getQualifiedName(), object);
            try {
                application.commit(baseTable.getDatabaseName());
            } catch (Throwable e) {
                String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
                logger.warn(rootCauseMessage, e);
                SessionMessages.addErrorMessage(rootCauseMessage);
                return PortofinoAction.EDIT;
            }
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

        setupForm(Mode.BULK_EDIT);

        return PortofinoAction.BULK_EDIT;
    }

    public String bulkUpdate() {
        setupForm(Mode.BULK_EDIT);
        form.readFromRequest(req);
        if (form.validate()) {
            for (String current : selection) {
                loadObject(current);
                form.writeToObject(object);
            }
            form.writeToObject(object);
            application.updateObject(baseTable.getQualifiedName(), object);
            try {
                application.commit(baseTable.getDatabaseName());
            } catch (Throwable e) {
                String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
                logger.warn(rootCauseMessage, e);
                SessionMessages.addErrorMessage(rootCauseMessage);
                return PortofinoAction.BULK_EDIT;
            }
            SessionMessages.addInfoMessage(MessageFormat.format(
                    "UPDATE di {0} oggetti avvenuto con successo",
                    selection.length));
            return PortofinoAction.BULK_UPDATE;
        } else {
            return PortofinoAction.BULK_EDIT;
        }
    }

    //**************************************************************************
    // Delete
    //**************************************************************************

    public String delete() {
        Object pkObject = pkHelper.parsePkString(pk);
        application.deleteObject(baseTable.getQualifiedName(), pkObject);
        try {
            application.commit(baseTable.getDatabaseName());
            SessionMessages.addInfoMessage("DELETE avvenuto con successo");

            // invalidate the pk on this crud unit
            pk = null;
        } catch (Exception e) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            logger.debug(rootCauseMessage, e);
            SessionMessages.addErrorMessage(rootCauseMessage);
        }
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
            application.deleteObject(baseTable.getQualifiedName(), pkObject);

        }
        try {
                application.commit(baseTable.getDatabaseName());
                SessionMessages.addInfoMessage(MessageFormat.format(
                "DELETE di {0} oggetti avvenuto con successo",
                selection.length));
            } catch (Exception e) {
                logger.warn(ExceptionUtils.getRootCauseMessage(e), e);
                SessionMessages.addErrorMessage(ExceptionUtils.getRootCauseMessage(e));
        }

        return PortofinoAction.DELETE;
    }

    //**************************************************************************
    // Button
    //**************************************************************************

    public String button() throws Exception {
        String value = req.getParameter("method:button");
        for (CrudButton crudButton : crudButtons) {
            Button button = crudButton.getButton();
            if (button.getLabel().equals(value)) {
                String script = button.getScript();
                String scriptLanguage = button.getActualScriptLanguage();
                return (String) ScriptingUtil.runScript(script, scriptLanguage, this);
            }
        }
        throw new Error("No button found");
    }

    //**************************************************************************
    // Ajax
    //**************************************************************************

    public String jsonOptions(String selectionProviderName,
                              int selectionProviderIndex,
                              String labelSearch,
                              boolean includeSelectPrompt) {
        String text = null;
        logger.debug("jsonSelectFieldOptions: {}", text);
        return text;
    }


    //**************************************************************************
    // Setup methods
    //**************************************************************************

    protected void setupSearchForm() {
        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);

        // setup option providers
        for (CrudSelectionProvider current : crudSelectionProviders) {
            SelectionProvider selectionProvider =
                    current.getSelectionProvider();
            String[] fieldNames = current.getFieldNames();
            searchFormBuilder.configSelectionProvider(selectionProvider, fieldNames);
        }
        
        searchForm = searchFormBuilder
                .configPrefix(prefix)
                .build();

        if (StringUtils.isBlank(searchString)) {
            searchForm.readFromRequest(req);
            searchString = searchForm.toSearchString();
            if (searchString.length() == 0) {
                searchString = null;
            }
        } else {
            DummyHttpServletRequest dummyRequest =
                    new DummyHttpServletRequest();
            String[] parts = searchString.split(",");
            Pattern pattern = Pattern.compile("(.*)=(.*)");
            for (String part : parts) {
                Matcher matcher = pattern.matcher(part);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2);
                    logger.debug("Matched part: {}={}", key, value);
                    dummyRequest.addParameter(key, value);
                } else {
                    logger.debug("Could not match part: {}", part);
                }
            }
            searchForm.readFromRequest(dummyRequest);
        }
    }

    protected void setupForm(Mode mode) {
        FormBuilder formBuilder = new FormBuilder(classAccessor);

        // setup option providers
        for (CrudSelectionProvider current : crudSelectionProviders) {
            SelectionProvider selectionProvider =
                    current.getSelectionProvider();
            String[] fieldNames = current.getFieldNames();
            formBuilder.configSelectionProvider(selectionProvider, fieldNames);
        }

        form = formBuilder
                .configPrefix(prefix)
                .configMode(mode)
                .build();
    }

    protected void setupTableForm(Mode mode) {
        String readLinkExpression = getReadLinkExpression();
        OgnlTextFormat hrefFormat =
                OgnlTextFormat.create(readLinkExpression);
        hrefFormat.setUrl(true);

        TableFormBuilder tableFormBuilder = new TableFormBuilder(classAccessor);

        // setup option providers
        for (CrudSelectionProvider current : crudSelectionProviders) {
            SelectionProvider selectionProvider =
                    current.getSelectionProvider();
            String[] fieldNames = current.getFieldNames();
            tableFormBuilder.configSelectionProvider(
                    selectionProvider, fieldNames);
        }

        // ogni colonna chiave primaria sarà clickabile
        //TODO rimuovere
        if (first){
            for (PropertyAccessor property : classAccessor.getKeyProperties()) {
                tableFormBuilder.configHrefTextFormat(
                        property.getName(), hrefFormat);
            }
        }

        tableForm = tableFormBuilder
                .configPrefix(prefix)
                .configNRows(objects.size())
                .configMode(mode)
                .build();
        tableForm.setKeyGenerator(pkHelper.createPkGenerator());
        tableForm.setSelectable(true);
        tableForm.readFromObject(objects);
    }

    protected String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder();
        sb.append(Struts2Utils.buildActionUrl(null));
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


    //**************************************************************************
    // Object loading
    //**************************************************************************

    public void loadObjects() {
        //Se si passano dati sbagliati al criterio restituisco messaggio d'errore
        // ma nessun risultato
        try {
            TableCriteria criteria = new TableCriteria(baseTable);
            searchForm.configureCriteria(criteria);
            objects = application.getObjects(query, criteria, this);
        } catch (ClassCastException e) {
            objects=new ArrayList<Object>();
            logger.warn("Incorrect Field Type", e);
            SessionMessages.addWarningMessage("Incorrect Field Type");
        }
    }

    private void loadObject() {
        loadObject(pk);
    }

    private void loadObject(String pk) {
        Serializable pkObject = pkHelper.parsePkString(pk);
        object = application.getObjectByPk(baseTable.getQualifiedName(), pkObject);
    }



    //**************************************************************************
    // ExportSearch
    //**************************************************************************

    public void exportSearchExcel(File fileTemp) {
        setupSearchForm();
        loadObjects();
        setupTableForm(Mode.VIEW);

        writeFileSearchExcel(fileTemp);
    }

    private void writeFileSearchExcel(File fileTemp) {
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(fileTemp);
            WritableSheet sheet =
                    workbook.createSheet(searchTitle, 0);

            addHeaderToSheet(sheet);

            int i = 1;
            for ( TableForm.Row row : tableForm.getRows()) {
                exportRows(sheet, i, row);
                i++;
            }

            workbook.write();
        } catch (IOException e) {
            logger.warn("IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (RowsExceededException e) {
            logger.warn("RowsExceededException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } catch (WriteException e) {
            logger.warn("WriteException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            try {
                if (workbook != null)
                    workbook.close();
            }
            catch (Exception e) {
                logger.warn("IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }
    }

    //**************************************************************************
    // ExportRead
    //**************************************************************************

    public void exportReadExcel(WritableWorkbook workbook)
            throws IOException, WriteException {
        setupSearchForm();

        loadObjects();
        loadObject();

        setupTableForm(Mode.VIEW);
        setupForm(Mode.VIEW);
        form.readFromObject(object);

        writeFileReadExcel(workbook);
    }


    private void writeFileReadExcel(WritableWorkbook workbook)
            throws IOException, WriteException {
        WritableSheet sheet =
                workbook.createSheet(readTitle,
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

        //Aggiungo le relazioni/sheet
        WritableCellFormat formatCell = headerExcel();
        for (CrudUnit subCrudUnit: subCrudUnits) {
            subCrudUnit.setupSearchForm();
            subCrudUnit.loadObjects();
            subCrudUnit.setupTableForm(Mode.VIEW);

            sheet = workbook.createSheet(subCrudUnit.searchTitle ,
                    workbook.getNumberOfSheets());

            int m = 0;
            for (TableForm.Column col : subCrudUnit.tableForm.getColumns()) {
                sheet.addCell(new Label(m, 0, col.getLabel(), formatCell));
                m++;
            }
            int k = 1;
            for (TableForm.Row row : subCrudUnit.tableForm.getRows()) {
                int j = 0;
                for (Field field : Arrays.asList(row.getFields())) {
                    addFieldToCell(sheet, k, j, field);
                    j++;
                }
                k++;
            }
        }
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
    // exportSearchPdf
    //**************************************************************************

    public void exportSearchPdf(File tempPdfFile) throws FOPException,
            IOException, TransformerException {
        
        setupSearchForm();

        loadObjects();

        setupTableForm(Mode.VIEW);

        FopFactory fopFactory = FopFactory.newInstance();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempPdfFile);

            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            ClassLoader cl = getClass().getClassLoader();
            InputStream xsltStream = cl.getResourceAsStream(
                    TEMPLATE_FOP_SEARCH);

            // Setup XSLT
            TransformerFactory Factory = TransformerFactory.newInstance();
            Transformer transformer = Factory.newTransformer(new StreamSource(
                    xsltStream));

            // Set the value of a <param> in the stylesheet
            transformer.setParameter("versionParam", "2.0");

            // Setup input for XSLT transformation
            Source src = new StreamSource(new StringReader(
                    composeXmlSearch().toString()));

            // Resulting SAX events (the generated FO) must be piped through to
            // FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

            out.flush();
        } catch (Exception e) {
            logger.warn("IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
            }
            catch (Exception e) {
                logger.warn("IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }
    }

    public XmlBuffer composeXmlSearch() {
        XmlBuffer xb = new XmlBuffer();
        xb.writeXmlHeader("UTF-8");
        xb.openElement("class");
        xb.openElement("table");
        xb.write(searchTitle);
        xb.closeElement("table");

        for (TableForm.Column col : tableForm.getColumns()) {
            xb.openElement("header");
            xb.openElement("nameColumn");
            xb.write(col.getLabel());
            xb.closeElement("nameColumn");
            xb.closeElement("header");
        }


        for (TableForm.Row row : tableForm.getRows()) {
            xb.openElement("rows");
            for (Field field : row.getFields()) {
                xb.openElement("row");
                xb.openElement("value");
                xb.write(field.getStringValue());
                xb.closeElement("value");
                xb.closeElement("row");
            }
            xb.closeElement("rows");
        }

        xb.closeElement("class");

        return xb;
    }


    //**************************************************************************
    // ExportRead
    //**************************************************************************

    private XmlBuffer composeXmlPort()
            throws IOException, WriteException {
        setupSearchForm();

        loadObjects();
        loadObject();

        setupTableForm(Mode.VIEW);
        setupForm(Mode.VIEW);
        form.readFromObject(object);


        XmlBuffer xb = new XmlBuffer();
        xb.writeXmlHeader("UTF-8");
        xb.openElement("class");
        xb.openElement("table");
        xb.write(readTitle);
        xb.closeElement("table");

        for (FieldSet fieldset : form) {
            xb.openElement("tableData");
            xb.openElement("rows");

            for (Field field : fieldset) {
                xb.openElement("row");
                xb.openElement("nameColumn");
                xb.write(field.getLabel());
                xb.closeElement("nameColumn");
                
                xb.openElement("value");
                xb.write(field.getStringValue());
                xb.closeElement("value");
                xb.closeElement("row");

            }
            xb.closeElement("rows");
            xb.closeElement("tableData");
        }

        //Aggiungo le relazioni
        for (CrudUnit subCrudUnit: subCrudUnits) {
            xb.openElement("tablerel");
            subCrudUnit.setupSearchForm();
            subCrudUnit.loadObjects();
            subCrudUnit.setupTableForm(Mode.VIEW);

            xb.openElement("nametablerel");
            xb.write(subCrudUnit.searchTitle);
            xb.closeElement("nametablerel");

            //stampo header
            for (TableForm.Column col : subCrudUnit.tableForm.getColumns()) {
                xb.openElement("headerrel");
                xb.openElement("nameColumn");
                xb.write(col.getLabel());
                xb.closeElement("nameColumn");
                xb.closeElement("headerrel");
            }

            for (TableForm.Row row : subCrudUnit.tableForm.getRows()) {
                xb.openElement("rowsrel");
                for (Field field : Arrays.asList(row.getFields())) {
                    xb.openElement("rowrel");
                    xb.openElement("value");
                    xb.write(field.getStringValue());
                    xb.closeElement("value");
                    xb.closeElement("rowrel");
                }
                xb.closeElement("rowsrel");
            }
            xb.closeElement("tablerel");
        }

        xb.closeElement("class");
        return xb;
    }

     public void exportReadPdf(File tempPdfFile) throws FOPException,
            IOException, TransformerException {
        setupSearchForm();

        loadObjects();

        setupTableForm(Mode.VIEW);

        FopFactory fopFactory = FopFactory.newInstance();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempPdfFile);

            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            ClassLoader cl = getClass().getClassLoader();
            InputStream xsltStream = cl.getResourceAsStream(
                    TEMPLATE_FOP_READ);

            // Setup XSLT
            TransformerFactory Factory = TransformerFactory.newInstance();
            Transformer transformer = Factory.newTransformer(new StreamSource(
                    xsltStream));

            // Set the value of a <param> in the stylesheet
            transformer.setParameter("versionParam", "2.0");

            // Setup input for XSLT transformation
            Source src = new StreamSource(new StringReader(
                    composeXmlPort().toString()));

            // Resulting SAX events (the generated FO) must be piped through to
            // FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

            out.flush();
        } catch (Exception e) {
            logger.warn("IOException", e);
            SessionMessages.addErrorMessage(e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
            }
            catch (Exception e) {
                logger.warn("IOException", e);
                SessionMessages.addErrorMessage(e.getMessage());
            }
        }
    }
}
