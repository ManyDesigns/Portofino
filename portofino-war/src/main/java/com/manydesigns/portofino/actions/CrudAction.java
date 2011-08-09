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
import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.fields.*;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.struts2.Struts2Utils;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XmlBuffer;
import com.manydesigns.portofino.context.TableCriteria;
import com.manydesigns.portofino.dispatcher.CrudNodeInstance;
import com.manydesigns.portofino.dispatcher.SiteNodeInstance;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.selectionproviders.ModelSelectionProvider;
import com.manydesigns.portofino.model.selectionproviders.SelectionProperty;
import com.manydesigns.portofino.model.site.CrudNode;
import com.manydesigns.portofino.model.site.crud.Button;
import com.manydesigns.portofino.model.site.crud.Crud;
import com.manydesigns.portofino.navigation.ResultSetNavigation;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding("/crud.action")
public class CrudAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String SEARCH_STRING_PARAM = "searchString";

    public CrudNode crudNode;
    public Crud crud;

    public ClassAccessor classAccessor;
    public Table baseTable;
    public PkHelper pkHelper;
    public List<CrudButton> crudButtons;
    public List<CrudSelectionProvider> crudSelectionProviders;
    public String pk;

    public final static String prefix = null;

    //--------------------------------------------------------------------------
    // Web parameters
    //--------------------------------------------------------------------------

    public String[] selection;
    public String searchString;
    public String successReturnUrl;

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
            LoggerFactory.getLogger(CrudAction.class);
    private static final String CONSTRAINT_VIOLATION = "Constraint violation";

    //**************************************************************************
    // Export
    //**************************************************************************
    private static final String TEMPLATE_FOP_SEARCH = "templateFOP-Search.xsl";
    private static final String TEMPLATE_FOP_READ = "templateFOP-Read.xsl";

    //**************************************************************************
    // Setup
    //**************************************************************************

    @Before
    public void prepare() {
        CrudNodeInstance crudNodeInstance = getSiteNodeInstance();
        pk = crudNodeInstance.getPk();
        crudNode = getSiteNodeInstance().getSiteNode();
        crud = crudNode.getCrud();
        classAccessor = crudNodeInstance.getClassAccessor();
        baseTable = crudNodeInstance.getBaseTable();
        pkHelper = crudNodeInstance.getPkHelper();
        crudButtons = new ArrayList<CrudButton>();
        crudSelectionProviders = new ArrayList<CrudSelectionProvider>();
        object = crudNodeInstance.getObject();

        setupSelectionProviders();
    }

    private void setupSelectionProviders() {
        for (ModelSelectionProvider current : crud.getSelectionProviders()) {
            String name = current.getName();
            String database = current.getDatabase();
            String sql = current.getSql();
            String hql = current.getHql();
            List<SelectionProperty> selectionProperties =
                    current.getSelectionProperties();

            String[] fieldNames = new String[selectionProperties.size()];
            Class[] fieldTypes = new Class[selectionProperties.size()];

            int i = 0;
            for (SelectionProperty selectionProperty : selectionProperties) {
                try {
                    fieldNames[i] = selectionProperty.getName();
                    PropertyAccessor propertyAccessor =
                            classAccessor.getProperty(fieldNames[i]);
                    fieldTypes[i] = propertyAccessor.getType();
                    i++;
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            }


            SelectionProvider selectionProvider;
            if (sql != null) {
                Collection<Object[]> objects = application.runSql(database, sql);
                selectionProvider = DefaultSelectionProvider.create(
                        name, fieldNames.length, fieldTypes, objects);
            } else if (hql != null) {
                Collection<Object> objects = application.getObjects(hql);
                String qualifiedTableName = 
                        application.getQualifiedTableNameFromQueryString(hql);
                TableAccessor tableAccessor =
                        application.getTableAccessor(qualifiedTableName);
                ShortName shortNameAnnotation =
                        tableAccessor.getAnnotation(ShortName.class);
                TextFormat[] textFormats = null;
                if (shortNameAnnotation != null) {
                    textFormats = new TextFormat[] {
                        OgnlTextFormat.create(shortNameAnnotation.value())
                    };
                }

                selectionProvider = DefaultSelectionProvider.create(
                        name, objects, tableAccessor, textFormats);
            } else {
                logger.warn("ModelSelection provider '{}':" +
                        " both 'hql' and 'sql' are null", name);
                break;
            }

            CrudSelectionProvider crudSelectionProvider =
                    new CrudSelectionProvider(selectionProvider, fieldNames);
            crudSelectionProviders.add(crudSelectionProvider);
        }
    }

    //--------------------------------------------------------------------------
    // Crud operations
    //--------------------------------------------------------------------------

    @DefaultHandler
    public Resolution execute() {
        if (StringUtils.isEmpty(pk)) {
            if(isEmbedded()) {
                return embeddedSearch();
            } else {
                return search();
            }
        } else {
            return read();
        }
    }

    //**************************************************************************
    // Search
    //**************************************************************************

    public Resolution search() {
        setupSearchForm();
        loadObjects();
        setupTableForm(Mode.VIEW);
        cancelReturnUrl = new UrlBuilder(
                Locale.getDefault(), dispatch.getAbsoluteOriginalPath(), false)
                .addParameter("searchString", searchString)
                .toString();

        setupReturnToParentTarget();

        return forwardToPortletPage("/layouts/crud/search.jsp");
    }

    public Resolution embeddedSearch() {
//        setupSearchForm();
        loadObjects();
        setupTableForm(Mode.VIEW);
        tableForm.setSelectable(false);
        return new ForwardResolution("/layouts/crud/embedded-search.jsp");
    }

    public Resolution resetSearch() {
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    //**************************************************************************
    // Read
    //**************************************************************************

    public Resolution read() {
        setupSearchForm();
        loadObjects();

        if (!objects.contains(object)) {
            // TODO: gestire situazione:
            // pratiche da approvare; seleziono una pratica; la approvo;
            // la pratica "esce" dallo use case perché lo stato non è più
            // "in approvazione". Prima dava "object not found".
            // Adesso dovrebbe tornare alla ricerca o comunque non dare errore.
            //throw new Error("Object not found");
        }
        setupForm(Mode.VIEW);
        form.readFromObject(object);
        refreshBlobDownloadHref();

        // refresh crud buttons (enabled/disabled)
        for (CrudButton crudButton : crudButtons) {
            crudButton.runGuard(this);
        }

        cancelReturnUrl = new UrlBuilder(
                Locale.getDefault(), dispatch.getAbsoluteOriginalPath(), false)
                .addParameter("searchString", searchString)
                .toString();

        setupPagination();

        setupReturnToParentTarget();

        return forwardToPortletPage("/layouts/crud/read.jsp");
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

    public Resolution create() {
        setupForm(Mode.CREATE);

        return new ForwardResolution("/layouts/crud/create.jsp");
    }

    public Resolution save() {
        setupForm(Mode.CREATE);

        form.readFromRequest(context.getRequest());
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
                return new ForwardResolution("/layouts/crud/create.jsp");
            }
            pk = pkHelper.generatePkString(object);
            SessionMessages.addInfoMessage("SAVE avvenuto con successo");
            String url = dispatch.getOriginalPath() + "/" + pk;
            return new RedirectResolution(url);
        } else {
            return new ForwardResolution("/layouts/crud/create.jsp");
        }
    }

    //**************************************************************************
    // Edit/Update
    //**************************************************************************

    public Resolution edit() {
        setupForm(Mode.EDIT);
        form.readFromObject(object);
        return new ForwardResolution("/layouts/crud/edit.jsp");
    }

    public Resolution update() {
        setupForm(Mode.EDIT);
        form.readFromObject(object);
        form.readFromRequest(context.getRequest());
        if (form.validate()) {
            form.writeToObject(object);
            application.updateObject(baseTable.getQualifiedName(), object);
            try {
                application.commit(baseTable.getDatabaseName());
            } catch (Throwable e) {
                String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
                logger.warn(rootCauseMessage, e);
                SessionMessages.addErrorMessage(rootCauseMessage);
                return new ForwardResolution("/layouts/crud/edit.jsp");
            }
            SessionMessages.addInfoMessage("UPDATE avvenuto con successo");
            return new RedirectResolution(dispatch.getOriginalPath())
                    .addParameter(SEARCH_STRING_PARAM, searchString);
        } else {
            return new ForwardResolution("/layouts/crud/edit.jsp");
        }
    }

    //**************************************************************************
    // Bulk Edit/Update
    //**************************************************************************

    public Resolution bulkEdit() {
        if (selection == null || selection.length == 0) {
            SessionMessages.addWarningMessage(
                    "Nessun oggetto selezionato");
            return new RedirectResolution(cancelReturnUrl, false);
        }

        if (selection.length == 1) {
            pk = selection[0];
            String url = dispatch.getOriginalPath() + "/" + pk;
            return new RedirectResolution(url)
                    .addParameter("cancelReturnUrl", cancelReturnUrl)
                    .addParameter("edit");
        }

        setupForm(Mode.BULK_EDIT);

        return new ForwardResolution("/layouts/crud/bulk-edit.jsp");
    }

    public Resolution bulkUpdate() {
        setupForm(Mode.BULK_EDIT);
        form.readFromRequest(context.getRequest());
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
                return new ForwardResolution("/layouts/crud/bulk-edit.jsp");
            }
            SessionMessages.addInfoMessage(MessageFormat.format(
                    "UPDATE di {0} oggetti avvenuto con successo",
                    selection.length));
            return new RedirectResolution(dispatch.getOriginalPath())
                    .addParameter(SEARCH_STRING_PARAM, searchString);
        } else {
            return new ForwardResolution("/layouts/crud/bulk-edit.jsp");
        }
    }

    //**************************************************************************
    // Delete
    //**************************************************************************

    public Resolution delete() {
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
        int lastSlashPos = dispatch.getOriginalPath().lastIndexOf("/");
        String url = dispatch.getOriginalPath().substring(0, lastSlashPos);
        return new RedirectResolution(url)
                .addParameter(SEARCH_STRING_PARAM, searchString);
    }

    public Resolution bulkDelete() {
        if (selection == null) {
            SessionMessages.addWarningMessage(
                    "DELETE non avvenuto: nessun oggetto selezionato");
            return new RedirectResolution(dispatch.getOriginalPath())
                    .addParameter(SEARCH_STRING_PARAM, searchString);
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

        return new RedirectResolution(dispatch.getOriginalPath())
                .addParameter(SEARCH_STRING_PARAM, searchString);
    }

    //**************************************************************************
    // Button
    //**************************************************************************

    public String button() throws Exception {
        String value = context.getRequest().getParameter("method:button");
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
    // Return to parent
    //**************************************************************************

    public Resolution returnToParent() {
        SiteNodeInstance[] siteNodeInstancePath =
                dispatch.getSiteNodeInstancePath();
        int previousPos = siteNodeInstancePath.length - 2;
        RedirectResolution resolution;
        if (previousPos >= 0) {
            SiteNodeInstance previousNode = siteNodeInstancePath[previousPos];
            if (previousNode instanceof CrudNodeInstance) {
                String url = dispatch.getPathUrl(previousPos + 1);
                resolution = new RedirectResolution(url, true);
            } else {
                resolution = new RedirectResolution(calculateBaseSearchUrl(), false);
                if(!StringUtils.isEmpty(searchString)) {
                    resolution.addParameter("searchString", searchString);
                }
            }
        } else {
            resolution = new RedirectResolution(calculateBaseSearchUrl(), false);
            if(!StringUtils.isEmpty(searchString)) {
                resolution.addParameter("searchString", searchString);
            }
        }

        return resolution;
    }

    //**************************************************************************
    // Ajax
    //**************************************************************************

    public String jsonOptions(String selectionProviderName,
                              int selectionProviderIndex,
                              String labelSearch,
                              boolean includeSelectPrompt) {
        CrudSelectionProvider crudSelectionProvider = null;
        for (CrudSelectionProvider current : crudSelectionProviders) {
            SelectionProvider selectionProvider =
                    current.getSelectionProvider();
            if (selectionProvider.getName().equals(selectionProviderName)) {
                crudSelectionProvider = current;
                break;
            }
        }
        if (crudSelectionProvider == null) {
            return "ActionSupport.ERROR";
        }

        SelectionProvider selectionProvider =
                crudSelectionProvider.getSelectionProvider();
        String[] fieldNames = crudSelectionProvider.getFieldNames();

        Form form = new FormBuilder(classAccessor)
                .configFields(fieldNames)
                .configSelectionProvider(selectionProvider, fieldNames)
                .configPrefix(prefix)
                .configMode(Mode.EDIT)
                .build();
        form.readFromRequest(context.getRequest());

        SelectField targetField =
                (SelectField) form.get(0).get(selectionProviderIndex);
        targetField.setLabelSearch(labelSearch);

        String text = targetField.jsonSelectFieldOptions(includeSelectPrompt);
        logger.debug("jsonSelectFieldOptions: {}", text);
        return text;
    }


    //**************************************************************************
    // Setup methods
    //**************************************************************************

    protected void setupPagination() {
        resultSetNavigation = new ResultSetNavigation();
        int position = objects.indexOf(object);
        int size = objects.size();
        resultSetNavigation.setPosition(position);
        resultSetNavigation.setSize(size);
        String baseUrl = calculateBaseSearchUrl();
        if(position >= 0) {
            if(position > 0) {
                resultSetNavigation.setFirstUrl(generateObjectUrl(baseUrl, 0));
                resultSetNavigation.setPreviousUrl(
                        generateObjectUrl(baseUrl, position - 1));
            }
            if(position < size - 1) {
                resultSetNavigation.setLastUrl(
                        generateObjectUrl(baseUrl, size - 1));
                resultSetNavigation.setNextUrl(
                        generateObjectUrl(baseUrl, position + 1));
            }
        }
    }

    protected String calculateBaseSearchUrl() {
        assert pk != null; //Ha senso solo in modalità read/detail
        String baseUrl = dispatch.getAbsoluteOriginalPath();
        int lastSlashIndex = baseUrl.lastIndexOf('/');
        baseUrl = baseUrl.substring(0, lastSlashIndex);
        return baseUrl;
    }

    protected String generateObjectUrl(String baseUrl, int index) {
        Object o = objects.get(index);
        return generateObjectUrl(baseUrl, o);
    }

    protected String generateObjectUrl(String baseUrl, Object o) {
        String objPk = pkHelper.generatePkString(o);
        return new UrlBuilder(
                Locale.getDefault(), baseUrl + "/" + objPk, false)
                .addParameter("searchString", searchString)
                .toString();
    }

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
            searchForm.readFromRequest(context.getRequest());
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

        for (PropertyAccessor property : classAccessor.getKeyProperties()) {
            tableFormBuilder.configHrefTextFormat(
                    property.getName(), hrefFormat);
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
        sb.append(dispatch.getOriginalPath());
        sb.append("/");
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
            sb.append("?searchString=");
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
            if(searchForm != null) {
                searchForm.configureCriteria(criteria);
            }
            objects = application.getObjects(crud.getQuery(), criteria, this);
        } catch (ClassCastException e) {
            objects=new ArrayList<Object>();
            logger.warn("Incorrect Field Type", e);
            SessionMessages.addWarningMessage("Incorrect Field Type");
        }
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
                    workbook.createSheet(crud.getSearchTitle(), 0);

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

        setupTableForm(Mode.VIEW);
        setupForm(Mode.VIEW);
        form.readFromObject(object);

        writeFileReadExcel(workbook);
    }


    private void writeFileReadExcel(WritableWorkbook workbook)
            throws IOException, WriteException {
        WritableSheet sheet =
                workbook.createSheet(crud.getReadTitle(),
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

        /*
        ValueStack valueStack = Struts2Utils.getValueStack();
        valueStack.push(object);

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
        valueStack.pop();
        */
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
        xb.write(crud.getSearchTitle());
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

        setupTableForm(Mode.VIEW);
        setupForm(Mode.VIEW);
        form.readFromObject(object);


        XmlBuffer xb = new XmlBuffer();
        xb.writeXmlHeader("UTF-8");
        xb.openElement("class");
        xb.openElement("table");
        xb.write(crud.getReadTitle());
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

        /*
        ValueStack valueStack = Struts2Utils.getValueStack();
        valueStack.push(object);

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
        valueStack.pop();
        */

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

    public boolean isRequiredFieldsPresent() {
        return form.isRequiredFieldsPresent();
    }

    public CrudNodeInstance getSiteNodeInstance() {
        return (CrudNodeInstance) siteNodeInstance;
    }

    public CrudNode getCrudNode() {
        return crudNode;
    }

    public void setCrudNode(CrudNode crudNode) {
        this.crudNode = crudNode;
    }

    public Crud getCrud() {
        return crud;
    }

    public void setCrud(Crud crud) {
        this.crud = crud;
    }

    public ClassAccessor getClassAccessor() {
        return classAccessor;
    }

    public void setClassAccessor(ClassAccessor classAccessor) {
        this.classAccessor = classAccessor;
    }

    public Table getBaseTable() {
        return baseTable;
    }

    public void setBaseTable(Table baseTable) {
        this.baseTable = baseTable;
    }

    public PkHelper getPkHelper() {
        return pkHelper;
    }

    public void setPkHelper(PkHelper pkHelper) {
        this.pkHelper = pkHelper;
    }

    public List<CrudButton> getCrudButtons() {
        return crudButtons;
    }

    public void setCrudButtons(List<CrudButton> crudButtons) {
        this.crudButtons = crudButtons;
    }

    public List<CrudSelectionProvider> getCrudSelectionProviders() {
        return crudSelectionProviders;
    }

    public void setCrudSelectionProviders(List<CrudSelectionProvider> crudSelectionProviders) {
        this.crudSelectionProviders = crudSelectionProviders;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String[] getSelection() {
        return selection;
    }

    public void setSelection(String[] selection) {
        this.selection = selection;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSuccessReturnUrl() {
        return successReturnUrl;
    }

    public void setSuccessReturnUrl(String successReturnUrl) {
        this.successReturnUrl = successReturnUrl;
    }

    public SearchForm getSearchForm() {
        return searchForm;
    }

    public void setSearchForm(SearchForm searchForm) {
        this.searchForm = searchForm;
    }

    public TableForm getTableForm() {
        return tableForm;
    }

    public void setTableForm(TableForm tableForm) {
        this.tableForm = tableForm;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public List getObjects() {
        return objects;
    }

    public void setObjects(List objects) {
        this.objects = objects;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public boolean isMultipartRequest() {
        return form != null && form.isMultipartRequest();
    }

    public String getMode() {
        return siteNodeInstance.getMode();
    }
}
