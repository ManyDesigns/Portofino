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
import com.manydesigns.elements.fields.DefaultOptionProvider;
import com.manydesigns.elements.fields.OptionProvider;
import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.actions.RelatedTableForm;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.datamodel.Reference;
import com.manydesigns.portofino.model.datamodel.Relationship;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringBufferInputStream;
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
                new TableFormBuilder(tableAccessor)
                        .configNRows(objects.size());

        // ogni colonna chiave primaria sarà clickabile
        for (PropertyAccessor property : tableAccessor.getKeyProperties()) {
            tableFormBuilder.configHyperlinkGenerators(
                    property.getName(), hrefFormat, null);
        }

        tableForm = tableFormBuilder.build();
        tableForm.setKeyGenerator(pkHelper.createPkGenerator());
        tableForm.setMode(Mode.VIEW);
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
        FormBuilder formBuilder = new FormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.VIEW);
        form.readFromObject(object);

        relatedTableFormList = new ArrayList<RelatedTableForm>();

        Table table = model.findTableByQualifiedName(qualifiedTableName);
        
        for (Relationship relationship : table.getOneToManyRelationships()) {
            setupRelatedTableForm(relationship);
        }

        return READ;
    }

    protected void setupRelatedTableForm(Relationship relationship) {
        List<Object> relatedObjects =
                context.getRelatedObjects(qualifiedTableName, object,
                        relationship.getRelationshipName());

        Table relatedTable = relationship.getFromTable();
        TableAccessor relatedTableAccessor =
                context.getTableAccessor(relatedTable.getQualifiedName());
        TableFormBuilder tableFormBuilder =
                new TableFormBuilder(relatedTableAccessor);
        tableFormBuilder.configNRows(relatedObjects.size());
        TableForm tableForm = tableFormBuilder.build();
        tableForm.setMode(Mode.VIEW);
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

        setupFormWithOptionProviders();
        form.setMode(Mode.CREATE);

        return CREATE;
    }

    public String save() {
        setupTable();

        setupFormWithOptionProviders();
        form.setMode(Mode.CREATE);

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

        setupFormWithOptionProviders();
        form.setMode(Mode.EDIT);

        form.readFromObject(object);

        return EDIT;
    }

    public String update() {
        setupTable();
        Serializable pkObject = pkHelper.parsePkString(pk);

        setupFormWithOptionProviders();
        form.setMode(Mode.EDIT);

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

        setupFormWithOptionProviders();
        form.setMode(Mode.BULK_EDIT);

        return BULK_EDIT;
    }

    public String bulkUpdate() {
        setupTable();

        setupFormWithOptionProviders();
        form.setMode(Mode.BULK_EDIT);
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
        setupTable();
        Table table = model.findTableByQualifiedName(qualifiedTableName);
        Relationship relationship =
                table.findManyToOneByName(relName);

        String[] fieldNames = createFieldNamesForRelationship(relationship);
        OptionProvider optionProvider =
                createOptionProviderForRelationship(relationship);

        Form form = new FormBuilder(tableAccessor)
                .configFields(fieldNames)
                .configOptionProvider(optionProvider, fieldNames)
                .build();
        form.readFromRequest(req);

        // prepariamo Json
        StringBuffer sb = new StringBuffer();
        boolean first = false;
        // apertura array Json
        sb.append("[\n");

        sb.append("{\"value\" : \"1\", \"label\" : \"uno\"},\n");
        sb.append("{\"value\" : \"2\", \"label\" : \"due\"}\n");

        // chiusura array Json
        if (!first) {
            sb.append("\n");
        }
        sb.append("]");

        inputStream = new StringBufferInputStream(sb.toString());

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

    private void setupFormWithOptionProviders() {
        FormBuilder formBuilder = new FormBuilder(tableAccessor);

        // setup relationship lookups
        Table table = model.findTableByQualifiedName(qualifiedTableName);
        for (Relationship rel : table.getManyToOneRelationships()) {
            String[] fieldNames = createFieldNamesForRelationship(rel);
            OptionProvider optionProvider =
                    createOptionProviderForRelationship(rel);

            formBuilder.configOptionProvider(optionProvider, fieldNames);
        }

        form = formBuilder.build();
    }

    private String[] createFieldNamesForRelationship(Relationship rel) {
        List<Reference> references = rel.getReferences();
        String[] fieldNames = new String[references.size()];
        int i = 0;
        for (Reference reference : references) {
            fieldNames[i] = reference.getFromColumn().getPropertyName();
            i++;
        }
        return fieldNames;
    }

    protected OptionProvider createOptionProviderForRelationship(Relationship rel) {
        // retrieve the related objects
        Table relatedTable = rel.getToTable();
        ClassAccessor classAccessor =
                context.getTableAccessor(relatedTable.getQualifiedName());
        List<Object> relatedObjects =
                context.getAllObjects(relatedTable.getQualifiedName());
        OptionProvider optionProvider =
                DefaultOptionProvider.create(rel.getRelationshipName(),
                        relatedObjects, classAccessor);
        return optionProvider;
    }

}
