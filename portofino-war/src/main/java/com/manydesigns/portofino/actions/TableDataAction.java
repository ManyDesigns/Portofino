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
import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.forms.*;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.text.Generator;
import com.manydesigns.elements.text.HrefExpressionGenerator;
import com.manydesigns.portofino.context.MDContext;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.interceptors.MDContextAware;
import com.manydesigns.portofino.model.Column;
import com.manydesigns.portofino.model.DataModel;
import com.manydesigns.portofino.model.Relationship;
import com.manydesigns.portofino.model.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.util.TableHelper;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableDataAction extends ActionSupport
        implements MDContextAware, ServletRequestAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // MDContextAware implementation
    //**************************************************************************

    public MDContext context;
    public DataModel dataModel;

    public void setContext(MDContext context) {
        this.context = context;
        dataModel = context.getDataModel();
    }

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
    public String skin;

    //**************************************************************************
    // Web parameters setters (for struts.xml inspections in IntelliJ)
    //**************************************************************************

    public void setQualifiedTableName(String qualifiedTableName) {
        this.qualifiedTableName = qualifiedTableName;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public Table table;
    public TableAccessor tableAccessor;


    //**************************************************************************
    // Model objects
    //**************************************************************************

    public Map<String, Object> object;
    public List<Map<String, Object>> objects;


    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    public TableForm tableForm;
    public Form form;
    public SearchForm searchForm;
    public List<RelatedTableForm> relatedTableFormList;

    //**************************************************************************
    // Other objects
    //**************************************************************************

    public TableHelper tableHelper = new TableHelper();

    public static final Logger logger =
            LogUtil.getLogger(TableDataAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    public String execute() {
        if (qualifiedTableName == null) {
            qualifiedTableName = dataModel.getAllTables().get(0).getQualifiedName();
            return "redirectToTable";
        }
        if (pk == null) {
            return searchFromString();
        } else {
            return read();
        }
    }

    //**************************************************************************
    // Common methods
    //**************************************************************************

    public void setupTable() {
        table = dataModel.findTableByQualifiedName(qualifiedTableName);
        if (table == null) {
            throw new ModelObjectNotFoundError(qualifiedTableName);
        }
        tableAccessor = new TableAccessor(table);
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

    private void configureSearchFormFromString() {
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

    private String commonSearch() {
        searchString = searchForm.toSearchString();
        if (searchString.length() == 0) {
            searchString = null;
        }

        Criteria criteria = context.createCriteria(qualifiedTableName);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        String readLinkExpression = getReadLinkExpression();
        Generator hrefGenerator =
                new HrefExpressionGenerator(tableAccessor, readLinkExpression);

        TableFormBuilder tableFormBuilder =
                new TableFormBuilder(tableAccessor)
                        .configNRows(objects.size());

        // ogni colonna chiave primaria sarà clickabile
        for (Column column : table.getPrimaryKey().getColumns()) {
            tableFormBuilder.configHyperlinkGenerators(
                    column.getColumnName(), hrefGenerator, null);
        }

        tableForm = tableFormBuilder.build();
        tableForm.setKeyGenerator(tableHelper.createKeyGenerator(table));
        tableForm.setMode(Mode.VIEW);
        tableForm.setSelectable(true);
        tableForm.readFromObject(objects);

        return ActionResults.SEARCH;
    }

    public String urlencode(String s) {
        if (s == null) {
            return null;
        } else {
            try {
                return URLEncoder.encode(s, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            }
        }
    }



    public String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder("/model/");
        sb.append(table.getQualifiedName());
        sb.append("/TableData.action?pk=");
        boolean first = true;
        for (Column column : table.getPrimaryKey().getColumns()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("{");
            sb.append(column.getColumnName());
            sb.append("}");
        }
        if (searchString != null) {
            sb.append("&searchString=");
            sb.append(urlencode(searchString));
        }
        return sb.toString();
    }

    //**************************************************************************
    // Return to search
    //**************************************************************************

    public String returnToSearch() {
        setupTable();
        return ActionResults.RETURN_TO_SEARCH;
    }

    //**************************************************************************
    // Read
    //**************************************************************************

    public String read() {
        setupTable();
        HashMap<String, Object> pkMap = tableHelper.parsePkString(table, pk);

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(tableAccessor);
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        Criteria criteria = context.createCriteria(qualifiedTableName);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(criteria);

        object = context.getObjectByPk(qualifiedTableName, pkMap);
        FormBuilder formBuilder =
                new FormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.VIEW);
        form.readFromObject(object);

        relatedTableFormList = new ArrayList<RelatedTableForm>();
        for (Relationship relationship : table.getOneToManyRelationships()) {
            setupRelatedTableForm(relationship);
        }

        return ActionResults.READ;
    }

    public void setupRelatedTableForm(Relationship relationship) {
        List<Map<String, Object>> relatedObjects =
                context.getRelatedObjects(object,
                        relationship.getRelationshipName());

        Table relatedTable = relationship.getFromTable();
        TableFormBuilder tableFormBuilder =
                new TableFormBuilder(new TableAccessor(relatedTable));
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

        FormBuilder formBuilder = new FormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.CREATE);

        return ActionResults.CREATE;
    }

    public String save() {
        setupTable();

        FormBuilder formBuilder = new FormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.CREATE);

        form.readFromRequest(req);
        if (form.validate()) {
            object = new HashMap<String, Object>();
            object.put("$type$", table.getQualifiedName());
            form.writeToObject(object);
            context.saveObject(object);
            pk = tableHelper.generatePkString(table, object);
            SessionMessages.addInfoMessage("SAVE avvenuto con successo");
            return ActionResults.SAVE;
        } else {
            return ActionResults.CREATE;
        }
    }

    //**************************************************************************
    // Edit/Update
    //**************************************************************************

    public String edit() {
        setupTable();
        HashMap<String, Object> pkMap = tableHelper.parsePkString(table, pk);

        object = context.getObjectByPk(qualifiedTableName, pkMap);

        FormBuilder formBuilder = new FormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.EDIT);

        form.readFromObject(object);

        return ActionResults.EDIT;
    }

    public String update() {
        setupTable();
        HashMap<String, Object> pkMap = tableHelper.parsePkString(table, pk);

        FormBuilder formBuilder = new FormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.EDIT);

        object = context.getObjectByPk(qualifiedTableName, pkMap);
        form.readFromObject(object);
        form.readFromRequest(req);
        if (form.validate()) {
            form.writeToObject(object);
            context.updateObject(object);
            SessionMessages.addInfoMessage("UPDATE avvenuto con successo");
            return ActionResults.UPDATE;
        } else {
            return ActionResults.EDIT;
        }
    }

    public String bulkEdit() {
        if (selection == null || selection.length == 0) {
            SessionMessages.addWarningMessage(
                    "Nessun oggetto selezionato");
            return ActionResults.CANCEL;
        }

        if (selection.length == 1) {
            pk = selection[0];
            return edit();
        }

        setupTable();

        FormBuilder formBuilder = new FormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.BULK_EDIT);

        return ActionResults.BULK_EDIT;
    }

    public String bulkUpdate() {
        setupTable();

        FormBuilder formBuilder = new FormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.BULK_EDIT);
        form.readFromRequest(req);
        if (form.validate()) {
            for (String current : selection) {
                HashMap<String, Object> pkMap =
                        tableHelper.parsePkString(table, current);
                object = context.getObjectByPk(qualifiedTableName, pkMap);
                form.writeToObject(object);
            }
            form.writeToObject(object);
            context.updateObject(object);
            SessionMessages.addInfoMessage(MessageFormat.format(
                    "UPDATE di {0} oggetti avvenuto con successo", selection.length));
            return ActionResults.BULK_UPDATE;
        } else {
            return ActionResults.BULK_EDIT;
        }
    }

    //**************************************************************************
    // Delete
    //**************************************************************************

    public String delete() {
        setupTable();
        HashMap<String, Object> pkMap = tableHelper.parsePkString(table, pk);
        context.deleteObject(pkMap);
        SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        return ActionResults.DELETE;
    }

    public String bulkDelete() {
        setupTable();
        if (selection == null) {
            SessionMessages.addWarningMessage(
                    "DELETE non avvenuto: nessun oggetto selezionato");
            return ActionResults.CANCEL;
        }
        for (String current : selection) {
            HashMap<String, Object> pkMap =
                    tableHelper.parsePkString(table, current);
            context.deleteObject(pkMap);
        }
        SessionMessages.addInfoMessage(MessageFormat.format(
                "DELETE di {0} oggetti avvenuto con successo", selection.length));
        return ActionResults.DELETE;
    }

    //**************************************************************************
    // Cancel
    //**************************************************************************

    public String cancel() {
        return ActionResults.CANCEL;
    }

}
