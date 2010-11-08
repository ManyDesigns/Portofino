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
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.FileBlobField;
import com.manydesigns.elements.fields.SelectField;
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
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.CompoundRoot;
import com.opensymphony.xwork2.util.ValueStack;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
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

    public Context context;
    public Model model;
    public HttpServletRequest req;
    public String pk;
    public String[] selection;
    public String searchString;

    public final ClassAccessor classAccessor;
    public final Table baseTable;
    public final String query;
    public final String searchTitle;
    public final String createTitle;
    public final String readTitle;
    public final String editTitle;
    public final PkHelper pkHelper;
    public final List<CrudUnit> subCrudUnits;
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

        return PortofinoAction.SEARCH;
    }

    public void setupCriteria() {
        ActionContext actionContext = ActionContext.getContext();
        ValueStack valueStack = actionContext.getValueStack();
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

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        setupCriteria();

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


        ActionContext actionContext = ActionContext.getContext();
        ValueStack valueStack = actionContext.getValueStack();

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
        Serializable pkObject = pkHelper.parsePkString(pk);

        object = context.getObjectByPk(
                baseTable.getQualifiedName(), pkObject);

        form = createFormBuilderWithSelectionProviders()
                .configMode(Mode.EDIT)
                .build();

        form.readFromObject(object);

        return PortofinoAction.EDIT;
    }

    public String update() {
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




}
