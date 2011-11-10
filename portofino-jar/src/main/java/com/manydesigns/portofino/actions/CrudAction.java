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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.fields.*;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XmlBuffer;
import com.manydesigns.portofino.actions.forms.CrudPropertyEdit;
import com.manydesigns.portofino.actions.forms.CrudSelectionProviderEdit;
import com.manydesigns.portofino.application.TableCriteria;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.dispatcher.CrudPageInstance;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.CrudLogic;
import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.pages.CrudPage;
import com.manydesigns.portofino.model.pages.crud.Crud;
import com.manydesigns.portofino.model.pages.crud.CrudProperty;
import com.manydesigns.portofino.model.pages.crud.SelectionProviderReference;
import com.manydesigns.portofino.navigation.ResultSetNavigation;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;
import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.lang.Boolean;
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
@UrlBinding("/actions/crud")
public class CrudAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public final static String SEARCH_STRING_PARAM = "searchString";

    public CrudPage crudPage;
    public Crud crud;

    public ClassAccessor classAccessor;
    public Table baseTable;
    public PkHelper pkHelper;
    public List<CrudButton> crudButtons;
    public List<CrudSelectionProvider> crudSelectionProviders;
    public MultiMap availableSelectionProviders; //List<String> -> DatabaseSelectionProvider
    public String pk;
    public String propertyName;

    public final static String prefix = "";
    public final static String searchPrefix = prefix + "search_";

    //--------------------------------------------------------------------------
    // Web parameters
    //--------------------------------------------------------------------------

    public String[] selection;
    public String searchString;
    public String successReturnUrl;
    public Integer firstResult;
    public Integer maxResults;

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
    // Scripting
    //**************************************************************************

    protected GroovyObject groovyObject;
    protected String script;
    protected File storageDirFile;

    //**************************************************************************
    // Setup
    //**************************************************************************

    @Before
    @Override
    public void prepare() {
        super.prepare();
        CrudPageInstance crudPageInstance = getPageInstance();
        pk = crudPageInstance.getPk();
        crudPage = getPageInstance().getPage();
        crud = crudPage.getCrud();
        availableSelectionProviders = new MultiHashMap();
        if(crud != null) {
            classAccessor = crudPageInstance.getClassAccessor();
            baseTable = crudPageInstance.getBaseTable();
            pkHelper = crudPageInstance.getPkHelper();
            crudButtons = new ArrayList<CrudButton>();
            crudSelectionProviders = new ArrayList<CrudSelectionProvider>();
            object = crudPageInstance.getObject();

            setupSelectionProviders();
        }

        storageDirFile = application.getAppStorageDir();

        if(script == null) {
            prepareScript();
        }
    }

    protected void prepareScript() {
        File file = ScriptingUtil.getGroovyScriptFile(storageDirFile, crudPage.getId());
        if(file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                script = IOUtils.toString(fr);
                IOUtils.closeQuietly(fr);
                groovyObject = ScriptingUtil.getGroovyObject(script, file.getAbsolutePath());
                Script scriptObject = (Script) groovyObject;
                Binding binding = new Binding(ElementsThreadLocals.getOgnlContext());
                binding.setVariable("actionBean", this);
                scriptObject.setBinding(binding);
            } catch (Exception e) {
                logger.warn("Couldn't load script for crud page " + crudPage.getId(), e);
            }
        }
    }

    private void setupSelectionProviders() {
        Set<String> configuredSPs = new HashSet<String>();
        for(SelectionProviderReference ref : crud.getSelectionProviders()) {
            boolean added = false;
            if(ref.getForeignKey() != null) {
                added = setupSelectionProvider(ref, ref.getForeignKey(), configuredSPs);
            } else if(ref.getSelectionProvider() instanceof DatabaseSelectionProvider) {
                DatabaseSelectionProvider dsp = (DatabaseSelectionProvider) ref.getSelectionProvider();
                added = setupSelectionProvider(ref, dsp, configuredSPs);
            } else {
                logger.error("Unsupported selection provider: " + ref.getSelectionProvider());
                continue;
            }
            if(ref.isEnabled() && !added) {
                logger.warn("Selection provider {} not added; check whether the fields on which it is configured " +
                        "overlap with some other selection provider", ref.getQualifiedName());
            }
        }

        Table table = crud.getActualTable();
        if(table != null) {
            for(ForeignKey fk : table.getForeignKeys()) {
                setupSelectionProvider(null, fk, configuredSPs);
            }
            for(ModelSelectionProvider dsp : table.getSelectionProviders()) {
                if(dsp instanceof DatabaseSelectionProvider) {
                    setupSelectionProvider(null, (DatabaseSelectionProvider) dsp, configuredSPs);
                } else {
                    logger.error("Unsupported selection provider: " + dsp);
                    continue;
                }
            }
        }
    }

    private boolean setupSelectionProvider(
            @Nullable SelectionProviderReference ref,
            DatabaseSelectionProvider current,
            Set<String> configuredSPs) {
        List<Reference> references = current.getReferences();

        String[] fieldNames = new String[references.size()];
        Class[] fieldTypes = new Class[references.size()];

        int i = 0;
        for (Reference reference : references) {
            Column column = reference.getActualFromColumn();
            String propertyName = column.getPropertyName();
            fieldNames[i] = propertyName != null ? propertyName : column.getColumnName();
            fieldTypes[i] = column.getActualJavaType();
            i++;
        }

        availableSelectionProviders.put(Arrays.asList(fieldNames), current);
        for(String fieldName : fieldNames) {
            //If another SP is configured for the same field, stop
            if(configuredSPs.contains(fieldName)) {
                return false;
            }
        }

        SelectionProvider selectionProvider;

        if(ref == null || ref.isEnabled()) {
            DisplayMode dm = ref != null ? ref.getDisplayMode() : DisplayMode.DROPDOWN;
            selectionProvider = createSelectionProvider
                    (current, fieldNames, fieldTypes, dm);
        } else {
            selectionProvider = null;
        }

        CrudSelectionProvider crudSelectionProvider =
                new CrudSelectionProvider(selectionProvider, fieldNames);
        crudSelectionProviders.add(crudSelectionProvider);
        Collections.addAll(configuredSPs, fieldNames);
        return true;
    }

    protected SelectionProvider createSelectionProvider
            (DatabaseSelectionProvider current, String[] fieldNames,
             Class[] fieldTypes, DisplayMode dm) {
        DefaultSelectionProvider selectionProvider = null;
        String name = current.getName();
        String database = current.getToDatabase();
        String sql = current.getSql();
        String hql = current.getHql();

        if (sql != null) {
            Collection<Object[]> objects = application.runSql(database, sql);
            selectionProvider = DefaultSelectionProvider.create(
                    name, fieldNames.length, fieldTypes, objects);
            selectionProvider.setDisplayMode(dm);
        } else if (hql != null) {
            Collection<Object> objects = application.getObjects(hql, null, null);
            String qualifiedTableName =
                    application.getQualifiedTableNameFromQueryString(hql);
            TableAccessor tableAccessor =
                    application.getTableAccessor(qualifiedTableName);
            ShortName shortNameAnnotation =
                    tableAccessor.getAnnotation(ShortName.class);
            TextFormat[] textFormats = null;
            if (shortNameAnnotation != null && tableAccessor.getKeyProperties().length == 1) { //???
                textFormats = new TextFormat[] {
                    OgnlTextFormat.create(shortNameAnnotation.value())
                };
            }

            selectionProvider = DefaultSelectionProvider.create(
                    name, objects, tableAccessor, textFormats);
            selectionProvider.setDisplayMode(dm);
        } else {
            logger.warn("ModelSelection provider '{}':" +
                    " both 'hql' and 'sql' are null", name);
        }
        return selectionProvider;
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

    @Button(list = "crud-search-form", key = "commons.search", order = 1)
    public Resolution search() {
        cancelReturnUrl = new UrlBuilder(
                    Locale.getDefault(), dispatch.getAbsoluteOriginalPath(), false)
                    .addParameter("searchString", searchString)
                    .toString();
        setupReturnToParentTarget();

        if (classAccessor == null) {
            logger.debug("Crud not correctly configured");
            return forwardToPortletPage(PAGE_PORTLET_NOT_CONFIGURED);
        }

        try {
            setupSearchForm();
//            loadObjects();
            setupTableForm(Mode.VIEW);

            String fwd = crudPage.getSearchUrl();
            if(StringUtils.isEmpty(fwd)) {
                fwd = "/layouts/crud/search.jsp";
            }
            return forwardToPortletPage(fwd);
        } catch(Exception e) {
            logger.warn("Crud not correctly configured", e);
            return forwardToPortletPage(PAGE_PORTLET_NOT_CONFIGURED);
        }
    }

    public Resolution embeddedSearch() {
        if (classAccessor == null) {
            logger.debug("Crud not correctly configured");
            return new ForwardResolution(PAGE_PORTLET_NOT_CONFIGURED);
        }

        try {
            loadObjects();
            setupTableForm(Mode.VIEW);
            tableForm.setSelectable(false);
            String fwd = crudPage.getEmbeddedSearchUrl();
            if(StringUtils.isEmpty(fwd)) {
                fwd = "/layouts/crud/embedded-search.jsp";
            }
            return new ForwardResolution(fwd);
        } catch(Exception e) {
            logger.error("Crud not correctly configured", e);
            return new ForwardResolution(PAGE_PORTLET_NOT_CONFIGURED);
        }
    }

    public Resolution jsonSearchData() throws JSONException, JSQLParserException {
        setupSearchForm();
        loadObjects();

        // calculate totalRecords
        TableCriteria criteria = new TableCriteria(baseTable);
        if(searchForm != null) {
            searchForm.configureCriteria(criteria);
        }
        QueryStringWithParameters query =
                application.mergeQuery(crud.getQuery(), criteria, this);

        String queryString = query.getQueryString();
        String totalRecordsQueryString = generateCountQuery(queryString);
        String qualifiedTableName =
                crud.getActualTable().getQualifiedName();
        List<Object> result = application.runHqlQuery
                (qualifiedTableName, totalRecordsQueryString,
                 query.getParamaters());
        long totalRecords = (Long) result.get(0);

        setupTableForm(Mode.VIEW);
        JSONStringer js = new JSONStringer();
        js.object()
                .key("recordsReturned")
                .value(objects.size())
                .key("totalRecords")
                .value(totalRecords)
                .key("startIndex")
                .value(firstResult == null ? 0 : firstResult)
                .key("Result")
                .array();
        for (TableForm.Row row : tableForm.getRows()) {
            js.object()
                    .key("__rowKey")
                    .value(row.getKey());
            for (Field field : row) {
                Object value = field.getValue();
                String displayValue = field.getDisplayValue();
                String href = field.getHref();
                js.key(field.getPropertyAccessor().getName());
                js.object()
                        .key("value")
                        .value(value)
                        .key("displayValue")
                        .value(displayValue)
                        .key("href")
                        .value(href)
                        .endObject();
            }
            js.endObject();
        }
        js.endArray();
        js.endObject();
        String jsonText = js.toString();
        return new StreamingResolution("application/json", jsonText);
    }

    protected String generateCountQuery(String queryString) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        queryString = "SELECT count(*) " + queryString;
        PlainSelect plainSelect =
                (PlainSelect) ((Select) parserManager.parse(new StringReader(queryString))).getSelectBody();
        plainSelect.setOrderByElements(null);
        return plainSelect.toString();
    }

    @Button(list = "crud-search-form", key = "commons.resetSearch", order = 2)
    public Resolution resetSearch() {
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    //**************************************************************************
    // Read
    //**************************************************************************

    public Resolution read() {
        if(!crud.isLargeResultSet()) {
            //setupSearchForm(); apparentemente non serve
            loadObjects();
            setupPagination();
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

        setupReturnToParentTarget();

        String fwd = crudPage.getReadUrl();
        if(StringUtils.isEmpty(fwd)) {
            fwd = "/layouts/crud/read.jsp";
        }
        return forwardToPortletPage(fwd);
    }

    protected void refreshBlobDownloadHref() {
        for (FieldSet fieldSet : form) {
            for (Field field : fieldSet) {
                if (field instanceof FileBlobField) {
                    FileBlobField fileBlobField = (FileBlobField) field;
                    Blob blob = fileBlobField.getValue();
                    if (blob != null) {
                        String url = getBlobDownloadUrl(
                                fileBlobField.getPropertyAccessor());
                        field.setHref(url);
                    }
                }
            }
        }
    }

    public String getBlobDownloadUrl(PropertyAccessor propertyAccessor) {
        UrlBuilder urlBuilder = new UrlBuilder(
                Locale.getDefault(), dispatch.getAbsoluteOriginalPath(), false)
                .addParameter("downloadBlob","")
                .addParameter("propertyName", propertyAccessor.getName());
        return urlBuilder.toString();
    }

    public Resolution downloadBlob() throws IOException, NoSuchFieldException {
        PropertyAccessor propertyAccessor =
                classAccessor.getProperty(propertyName);
        String code = (String) propertyAccessor.get(object);

        BlobManager blobManager = ElementsThreadLocals.getBlobManager();
        Blob blob = blobManager.loadBlob(code);
        long contentLength = blob.getSize();
        String contentType = blob.getContentType();
        InputStream inputStream = new FileInputStream(blob.getDataFile());
        String fileName = blob.getFilename();
        return new StreamingResolution(contentType, inputStream)
                .setFilename(fileName)
                .setLength(contentLength);
    }



    //**************************************************************************
    // Create/Save
    //**************************************************************************

    @Button(list = "crud-search", key = "commons.create", order = 1)
    public Resolution create() {
        setupForm(Mode.CREATE);
        object = classAccessor.newInstance();
        createSetup(object);
        form.readFromObject(object);

        return forwardToCreatePage();
    }

    protected Resolution forwardToCreatePage() {
        String fwd = crudPage.getCreateUrl();
        if(StringUtils.isEmpty(fwd)) {
            fwd = "/layouts/crud/create.jsp";
        }
        return new ForwardResolution(fwd);
    }

    @Button(list = "crud-create", key = "commons.create", order = 1)
    public Resolution save() {
        setupForm(Mode.CREATE);
        object = classAccessor.newInstance();
        createSetup(object);
        form.readFromObject(object);

        form.readFromRequest(context.getRequest());
        if (form.validate()) {
            form.writeToObject(object);
            if(createValidate(object)) {
                application.saveObject(baseTable.getQualifiedName(), object);
                createPostProcess(object);
                try {
                    application.commit(baseTable.getDatabaseName());
                } catch (Throwable e) {
                    String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
                    logger.warn(rootCauseMessage, e);
                    SessionMessages.addErrorMessage(rootCauseMessage);
                    return forwardToCreatePage();
                }
                pk = pkHelper.generatePkString(object);
                SessionMessages.addInfoMessage("SAVE avvenuto con successo");
                String url = dispatch.getOriginalPath() + "/" + pk;
                return new RedirectResolution(url);
            }
        }

        return forwardToCreatePage();
    }

    //**************************************************************************
    // Edit/Update
    //**************************************************************************

    @Button(list = "crud-read", key = "commons.edit", order = 1)
    public Resolution edit() {
        setupForm(Mode.EDIT);
        editSetup(object);
        form.readFromObject(object);
        return forwardToEditPage();
    }

    protected Resolution forwardToEditPage() {
        String fwd = crudPage.getEditUrl();
        if(StringUtils.isEmpty(fwd)) {
            fwd = "/layouts/crud/edit.jsp";
        }
        return new ForwardResolution(fwd);
    }

    @Button(list = "crud-edit", key = "commons.update", order = 1)
    public Resolution update() {
        setupForm(Mode.EDIT);
        editSetup(object);
        form.readFromObject(object);
        form.readFromRequest(context.getRequest());
        if (form.validate()) {
            form.writeToObject(object);
            if(editValidate(object)) {
                application.updateObject(baseTable.getQualifiedName(), object);
                editPostProcess(object);
                try {
                    application.commit(baseTable.getDatabaseName());
                } catch (Throwable e) {
                    String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
                    logger.warn(rootCauseMessage, e);
                    SessionMessages.addErrorMessage(rootCauseMessage);
                    return forwardToEditPage();
                }
                SessionMessages.addInfoMessage("UPDATE avvenuto con successo");
                return new RedirectResolution(dispatch.getOriginalPath())
                        .addParameter(SEARCH_STRING_PARAM, searchString);
            }
        }
        return forwardToEditPage();
    }

    //**************************************************************************
    // Bulk Edit/Update
    //**************************************************************************

    @Button(list = "crud-search", key = "commons.edit", order = 2)
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

        String fwd = crudPage.getBulkEditUrl();
        if(StringUtils.isEmpty(fwd)) {
            fwd = "/layouts/crud/bulk-edit.jsp";
        }
        return new ForwardResolution(fwd);
    }

    @Button(list = "crud-bulk-edit", key = "commons.update", order = 1)
    public Resolution bulkUpdate() {
        setupForm(Mode.BULK_EDIT);
        form.readFromRequest(context.getRequest());
        if (form.validate()) {
            for (String current : selection) {
                loadObject(current);
                form.writeToObject(object);
                application.updateObject(baseTable.getQualifiedName(), object);
            }
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

    @Button(list = "crud-read", key = "commons.delete", order = 2)
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

    @Button(list = "crud-search", key = "commons.delete", order = 3)
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

    /*public String button() throws Exception {
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
    }*/

    //**************************************************************************
    // Return to parent
    //**************************************************************************

    public Resolution returnToParent() {
        PageInstance[] pageInstancePath =
                dispatch.getPageInstancePath();
        int previousPos = pageInstancePath.length - 2;
        RedirectResolution resolution;
        if (previousPos >= 0) {
            PageInstance previousPageInstance = pageInstancePath[previousPos];
            if (previousPageInstance instanceof CrudPageInstance) {
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

    @Override
    @Buttons({
        @Button(list = "crud-edit", key = "commons.cancel", order = 99),
        @Button(list = "crud-create", key = "commons.cancel", order = 99),
        @Button(list = "crud-bulk-edit", key = "commons.cancel", order = 99)
    })
    public Resolution cancel() {
        return super.cancel();
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
            if(selectionProvider == null) {
                continue;
            }
            String[] fieldNames = current.getFieldNames();
            /*
            //Include only searchable fields
            List<String> actualFieldNames = new ArrayList<String>();
            for(PropertyAccessor p : classAccessor.getProperties()) {
                if(ArrayUtils.contains(fieldNames, p.getName())) {
                    Searchable searchable = p.getAnnotation(Searchable.class);
                    if(searchable != null && searchable.value()) {
                        actualFieldNames.add(p.getName());
                    }
                }
            }
            if(!actualFieldNames.isEmpty()) {
                String[] actualFieldNamesArr = actualFieldNames.toArray(new String[actualFieldNames.size()]);
                searchFormBuilder.configSelectionProvider(selectionProvider, actualFieldNamesArr);
            }*/
            searchFormBuilder.configSelectionProvider(selectionProvider, fieldNames);
        }

        searchForm = searchFormBuilder
                .configPrefix(searchPrefix)
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
            if(selectionProvider == null) {
                continue;
            }
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
            if(selectionProvider == null) {
                continue;
            }
            String[] fieldNames = current.getFieldNames();
            tableFormBuilder.configSelectionProvider(
                    selectionProvider, fieldNames);
        }

        for (PropertyAccessor property : classAccessor.getKeyProperties()) {
            tableFormBuilder.configHrefTextFormat(
                    property.getName(), hrefFormat);
        }

        int nRows;
        if (objects == null) {
            nRows = 0;
        } else {
            nRows = objects.size();
        }

        tableForm = tableFormBuilder
                .configPrefix(prefix)
                .configNRows(nRows)
                .configMode(mode)
                .build();
        tableForm.setKeyGenerator(pkHelper.createPkGenerator());
        tableForm.setSelectable(true);
        if (objects != null) {
            tableForm.readFromObject(objects);
        }
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
            objects = application.getObjects(
                    crud.getQuery(), criteria, this, firstResult, maxResults);
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
//        setupTableForm(Mode.VIEW);

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

//        setupTableForm(Mode.VIEW);
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
        for (Field field : row) {
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
            if (numField.getValue() != null) {
                Number number;
                BigDecimal decimalValue = numField.getValue();
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
            Date date = dateField.getValue();
            if (date != null) {
                DateFormat dateFormat = new DateFormat(
                        dateField.getDatePattern());
                WritableCellFormat wDateFormat =
                        new WritableCellFormat(dateFormat);
                dateCell = new DateTime(j, i,
                        dateField.getValue() == null
                                ? null : dateField.getValue(),
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
            for (Field field : row) {
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

    //**************************************************************************
    // Hooks/scripting
    //**************************************************************************

    protected void createSetup(Object object) {
        String methodName = "createSetup";
        Object methodArgs = new Object[] { object };
        invokeGroovyMethod(methodName, methodArgs);
    }

    protected boolean createValidate(Object object) {
        String methodName = "createValidate";
        Object methodArgs = new Object[] { object };
        Boolean b = (Boolean) invokeGroovyMethod(methodName, methodArgs);
        return (b == null) || b;
    }

    protected void createPostProcess(Object object) {
        String methodName = "createPostProcess";
        Object methodArgs = new Object[] { object };
        invokeGroovyMethod(methodName, methodArgs);
    }

    protected void editSetup(Object object) {
        String methodName = "editSetup";
        Object methodArgs = new Object[] { object };
        invokeGroovyMethod(methodName, methodArgs);
    }

    protected boolean editValidate(Object object) {
        String methodName = "editValidate";
        Object methodArgs = new Object[] { object };
        Boolean b = (Boolean) invokeGroovyMethod(methodName, methodArgs);
        return (b == null) || b;
    }

    protected void editPostProcess(Object object) {
        String methodName = "editPostProcess";
        Object methodArgs = new Object[] { object };
        invokeGroovyMethod(methodName, methodArgs);
    }

    protected Object invokeGroovyMethod(String methodName, Object methodArgs) {
        if(groovyObject != null) {
            try {
                logger.debug("Invoking Groovy method {}", methodName);
                return groovyObject.invokeMethod(methodName, methodArgs);
            } catch(MissingMethodException e) {
                logger.debug("The Groovy method {} is missing", methodName);
            }
        } else {
            logger.debug("No script for this page: {}", crudPage.getId());
        }
        return null;
    }

    //**************************************************************************
    // Configuration
    //**************************************************************************

    public static final String[][] CRUD_CONFIGURATION_FIELDS =
            {{"name", "table", "query", "searchTitle", "createTitle", "readTitle", "editTitle", "variable",
              "largeResultSet"}};

    public Form crudConfigurationForm;
    public TableForm propertiesTableForm;
    public CrudPropertyEdit[] edits;
    public TableForm selectionProvidersForm;
    public CrudSelectionProviderEdit[] selectionProviderEdits;

    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1)
    public Resolution configure() {
        prepareConfigurationForms();

        crudConfigurationForm.readFromObject(crudPage.getCrud());
        if(edits != null) {
            propertiesTableForm.readFromObject(edits);
        }

        if(selectionProviderEdits != null) {
            selectionProvidersForm.readFromObject(selectionProviderEdits);
        }

        return new ForwardResolution("/layouts/crud/configure.jsp");
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();

        SelectionProvider tableSelectionProvider =
                DefaultSelectionProvider.create("table",
                        DataModelLogic.getAllTables(model),
                        Table.class,
                        null,
                        "qualifiedName");
        crudConfigurationForm = new FormBuilder(Crud.class)
                .configFields(CRUD_CONFIGURATION_FIELDS)
                .configFieldSetNames("Crud")
                .configSelectionProvider(tableSelectionProvider, "table")
                .build();

        setupEdits();

        if(edits != null) {
            TableFormBuilder tableFormBuilder =
                    new TableFormBuilder(CrudPropertyEdit.class)
                        .configNRows(edits.length);
            propertiesTableForm = tableFormBuilder.build();
        }

        if(!availableSelectionProviders.isEmpty()) {
            setupSelectionProviderEdits();
            TableFormBuilder tableFormBuilder =
                    new TableFormBuilder(CrudSelectionProviderEdit.class);
            tableFormBuilder.configNRows(availableSelectionProviders.size());
            for(int i = 0; i < selectionProviderEdits.length; i++) {
                Collection<ModelSelectionProvider> availableProviders =
                        (Collection) availableSelectionProviders.get
                                (Arrays.asList(selectionProviderEdits[i].fieldNames));
                if(availableProviders == null || availableProviders.size() == 0) {
                    continue;
                }
                String[] availableProviderNames = new String[availableProviders.size() + 1];
                String[] availableProviderValues = new String[availableProviderNames.length];
                availableProviderNames[0] = "None";
                int j = 1;
                for(ModelSelectionProvider sp : availableProviders) {
                    availableProviderNames[j] = sp.getName();
                    availableProviderValues[j] = sp.getName();
                    j++;
                }
                DefaultSelectionProvider selectionProvider =
                        DefaultSelectionProvider.create
                                (selectionProviderEdits[i].columns, availableProviderValues, availableProviderNames);
                tableFormBuilder.configSelectionProvider(i, selectionProvider, "selectionProvider");
            }
            selectionProvidersForm = tableFormBuilder.build();
        }
    }

    private void setupEdits() {
        if(classAccessor == null) {
            return;
        }
        PropertyAccessor[] propertyAccessors = classAccessor.getProperties();
        edits = new CrudPropertyEdit[propertyAccessors.length];
        for (int i = 0; i < propertyAccessors.length; i++) {
            CrudPropertyEdit edit = new CrudPropertyEdit();
            PropertyAccessor propertyAccessor = propertyAccessors[i];
            CrudProperty crudProperty =
                    CrudLogic.findCrudPropertyByName(
                            crud, propertyAccessor.getName());
            edit.name = propertyAccessor.getName();
            if (crudProperty == null) {
                // default values
                edit.label = null;
                edit.searchable = false;
                edit.inSummary = ArrayUtils.contains(
                        classAccessor.getKeyProperties(), propertyAccessor);
                edit.enabled = true;
                edit.updatable = true;
                edit.insertable = true;
            } else {
                edit.label = crudProperty.getLabel();
                edit.searchable = crudProperty.isSearchable();
                edit.inSummary = crudProperty.isInSummary();
                edit.enabled = crudProperty.isEnabled();
                edit.insertable = crudProperty.isInsertable();
                edit.updatable = crudProperty.isUpdatable();
            }
            edits[i] = edit;
        }
    }

    private void setupSelectionProviderEdits() {
        selectionProviderEdits = new CrudSelectionProviderEdit[availableSelectionProviders.size()];
        int i = 0;
        for(Map.Entry entry : (Set<Map.Entry>) availableSelectionProviders.entrySet()) {
            selectionProviderEdits[i] = new CrudSelectionProviderEdit();
            String[] fieldNames = (String[]) ((Collection) entry.getKey()).toArray(new String[0]);
            selectionProviderEdits[i].fieldNames = fieldNames;
            selectionProviderEdits[i].columns = StringUtils.join(fieldNames, ", ");
            for(CrudSelectionProvider cp : crudSelectionProviders) {
                if(Arrays.equals(cp.fieldNames, fieldNames)) {
                    SelectionProvider selectionProvider = cp.getSelectionProvider();
                    if(selectionProvider != null) {
                        selectionProviderEdits[i].selectionProvider = selectionProvider.getName();
                        selectionProviderEdits[i].displayMode = selectionProvider.getDisplayMode();
                    } else {
                        selectionProviderEdits[i].selectionProvider = null;
                        selectionProviderEdits[i].displayMode = DisplayMode.DROPDOWN;
                    }
                }
            }
            i++;
        }
    }

    public Resolution updateConfiguration() {
        synchronized (application) {
            prepareConfigurationForms();

            if(crudPage.getCrud() == null) {
                crudPage.setCrud(new Crud());
            }

            crudConfigurationForm.readFromObject(crudPage.getCrud());

            readPageConfigurationFromRequest();

            crudConfigurationForm.readFromRequest(context.getRequest());

            boolean valid = crudConfigurationForm.validate();
            valid = validatePageConfiguration() && valid;

            if(propertiesTableForm != null) {
                propertiesTableForm.readFromObject(edits);
                propertiesTableForm.readFromRequest(context.getRequest());
                valid = propertiesTableForm.validate() && valid;
            }

            if(selectionProvidersForm != null) {
                selectionProvidersForm.readFromRequest(context.getRequest());
                valid = selectionProvidersForm.validate() && valid;
            }

            if (valid) {
                updatePageConfiguration();
                crudConfigurationForm.writeToObject(crudPage.getCrud());

                if(propertiesTableForm != null) {
                    updateProperties();
                }

                if(!availableSelectionProviders.isEmpty()) {
                    updateSelectionProviders();
                }

                File groovyScriptFile =
                        ScriptingUtil.getGroovyScriptFile(storageDirFile, crudPage.getId());
                if(!StringUtils.isBlank(script)) {
                    FileWriter fw = null;
                    try {
                        fw = new FileWriter(groovyScriptFile);
                        fw.write(script);
                    } catch (IOException e) {
                        logger.error("Error writing script to " + groovyScriptFile, e);
                    } finally {
                        IOUtils.closeQuietly(fw);
                    }
                } else {
                    groovyScriptFile.delete();
                }

                saveModel();
                SessionMessages.addInfoMessage("Configuration updated successfully");
                return cancel();
            } else {
                SessionMessages.addErrorMessage("The configuration could not be saved. " +
                        "Review any errors below and submit again.");
                return new ForwardResolution("/layouts/crud/configure.jsp");
            }
        }
    }

    private void updateSelectionProviders() {
        selectionProvidersForm.writeToObject(selectionProviderEdits);
        crud.getSelectionProviders().clear();
        for(CrudSelectionProviderEdit sp : selectionProviderEdits) {
            if(sp.selectionProvider == null) {
                //TODO this is a shortcut: takes the first available selection provider and disables it
                List<String> key = Arrays.asList(sp.fieldNames);
                Collection<ModelSelectionProvider> selectionProviders =
                        (Collection<ModelSelectionProvider>) availableSelectionProviders.get(key);
                ModelSelectionProvider dsp = selectionProviders.iterator().next();
                SelectionProviderReference sel = makeSelectionProviderReference(dsp);
                sel.setEnabled(false);
            } else {
                List<String> key = Arrays.asList(sp.fieldNames);
                Collection<ModelSelectionProvider> selectionProviders =
                        (Collection<ModelSelectionProvider>) availableSelectionProviders.get(key);
                for(ModelSelectionProvider dsp : selectionProviders) {
                    if(sp.selectionProvider.equals(dsp.getName())) {
                        SelectionProviderReference sel = makeSelectionProviderReference(dsp);
                        sel.setDisplayMode(sp.displayMode);
                        break;
                    }
                }
            }
        }
    }

    private void updateProperties() {
        propertiesTableForm.writeToObject(edits);

        crud.getProperties().clear();
        for (CrudPropertyEdit edit : edits) {
            CrudProperty crudProperty = new CrudProperty();

            crudProperty.setName(edit.name);
            crudProperty.setLabel(edit.label);
            crudProperty.setInSummary(edit.inSummary);
            crudProperty.setSearchable(edit.searchable);
            crudProperty.setEnabled(edit.enabled);
            crudProperty.setInsertable(edit.insertable);
            crudProperty.setUpdatable(edit.updatable);

            crudProperty.setCrud(crud);
            crud.getProperties().add(crudProperty);
        }
    }

    protected SelectionProviderReference makeSelectionProviderReference(ModelSelectionProvider dsp) {
        SelectionProviderReference sel = new SelectionProviderReference();
        if(dsp instanceof ForeignKey) {
            sel.setForeignKeyName(dsp.getName());
        } else {
            sel.setSelectionProviderName(dsp.getName());
        }
        sel.setParent(crud);
        crud.getSelectionProviders().add(sel);
        return sel;
    }

    public boolean isRequiredFieldsPresent() {
        return form.isRequiredFieldsPresent();
    }

    public CrudPageInstance getPageInstance() {
        return (CrudPageInstance) pageInstance;
    }

    public CrudPage getCrudPage() {
        return crudPage;
    }

    public void setCrudPage(CrudPage crudPage) {
        this.crudPage = crudPage;
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
        return pageInstance.getMode();
    }

    public Form getCrudConfigurationForm() {
        return crudConfigurationForm;
    }

    public void setCrudConfigurationForm(Form crudConfigurationForm) {
        this.crudConfigurationForm = crudConfigurationForm;
    }

    public TableForm getPropertiesTableForm() {
        return propertiesTableForm;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public TableForm getTableForm() {
        return tableForm;
    }

    public void setTableForm(TableForm tableForm) {
        this.tableForm = tableForm;
    }

    public TableForm getSelectionProvidersForm() {
        return selectionProvidersForm;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
