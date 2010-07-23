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
import com.manydesigns.elements.composites.ClassTableFormBuilder;
import com.manydesigns.elements.composites.TableForm;
import com.manydesigns.elements.forms.ClassFormBuilder;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.hyperlinks.ExpressionHyperlinkGenerator;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.base.context.MDContext;
import com.manydesigns.portofino.base.context.ModelObjectNotFoundException;
import com.manydesigns.portofino.base.model.Column;
import com.manydesigns.portofino.base.model.Relationship;
import com.manydesigns.portofino.base.model.Table;
import com.manydesigns.portofino.base.reflection.TableAccessor;
import com.manydesigns.portofino.interceptors.MDContextAware;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TableAction extends ActionSupport
        implements MDContextAware, ServletRequestAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String SEARCH = "search";
    public final static String RETURN_TO_SEARCH = "returnToSearch";
    public final static String READ = "read";
    public final static String CREATE = "create";
    public final static String SAVE = "save";
    public final static String EDIT = "edit";
    public final static String UPDATE = "update";
    public final static String DELETE = "delete";
    public final static String CANCEL = "cancel";

    //--------------------------------------------------------------------------
    // MDContextAware implementation
    //--------------------------------------------------------------------------

    public MDContext context;

    public void setContext(MDContext context) {
        this.context = context;
    }

    //--------------------------------------------------------------------------
    // ServletRequestAware implementation
    //--------------------------------------------------------------------------

    public HttpServletRequest req;

    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }


    //--------------------------------------------------------------------------
    // Web parameters
    //--------------------------------------------------------------------------

    public String qualifiedTableName;
    public String pk;
    public String cancelReturnUrl;
    public String skin;

    //--------------------------------------------------------------------------
    // Web parameters setters (for struts.xml inspections in IntelliJ)
    //--------------------------------------------------------------------------

    public void setQualifiedTableName(String qualifiedTableName) {
        this.qualifiedTableName = qualifiedTableName;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    //--------------------------------------------------------------------------
    // Model metadata
    //--------------------------------------------------------------------------

    public Table table;
    public TableAccessor tableAccessor;


    //--------------------------------------------------------------------------
    // Model objects
    //--------------------------------------------------------------------------

    public HashMap<String, Object> pkMap;
    public Map<String, Object> object;
    public List<Map<String, Object>> objects;


    //--------------------------------------------------------------------------
    // Presentation elements
    //--------------------------------------------------------------------------

    public TableForm tableForm;
    public Form form;
    public List<RelatedTableForm> relatedTableFormList;

    //--------------------------------------------------------------------------
    // Action default execute method
    //--------------------------------------------------------------------------

    public String execute() throws ModelObjectNotFoundException {
        setupTable();

        if (pk == null) {
            return search();
        } else {
            return read();
        }
    }

    //--------------------------------------------------------------------------
    // Common methods
    //--------------------------------------------------------------------------

    public void setupTable() throws ModelObjectNotFoundException {
        table = context.findTableByQualifiedName(qualifiedTableName);
        tableAccessor = new TableAccessor(table);
    }

    public void parsePkString() {
        String[] pkList = StringUtils.split(pk,",");

        int i = 0;
        pkMap = new HashMap<String, Object>();

        for(Column column : table.getPrimaryKey().getColumns() ) {
            pkMap.put(column.getColumnName(), pkList[i]);
            i++;
        }
    }

    public String generatePkString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(Column column : table.getPrimaryKey().getColumns() ) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(map.get(column.getColumnName()));
        }
        return sb.toString();
    }

    //--------------------------------------------------------------------------
    // Search
    //--------------------------------------------------------------------------

    public String search() {
        objects = context.getAllObjects(qualifiedTableName);

        String readLinkExpression = getReadLinkExpression();
        ExpressionHyperlinkGenerator generator =
                new ExpressionHyperlinkGenerator(
                        tableAccessor, readLinkExpression, "dummy-alt");

        ClassTableFormBuilder tableFormBuilder =
                new ClassTableFormBuilder(tableAccessor)
                        .configNRows(objects.size());

        // ogni colonna chiave primaria sarà clickabile
        for (Column column : table.getPrimaryKey().getColumns()) {
            tableFormBuilder.configHyperlinkGenerator(
                    column.getColumnName(), generator);
        }

        tableForm = tableFormBuilder.build();
        tableForm.setMode(Mode.VIEW);
        tableForm.readFromObject(objects);

        return SEARCH;
    }

    public String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder("/");
        sb.append(table.getQualifiedName());
        sb.append("/Table.action?pk=");
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
        return sb.toString();
    }

    //--------------------------------------------------------------------------
    // Read
    //--------------------------------------------------------------------------

    public String returnToSearch() throws ModelObjectNotFoundException {
        setupTable();
        return RETURN_TO_SEARCH;
    }

    //--------------------------------------------------------------------------
    // Read
    //--------------------------------------------------------------------------

    public String read() {
        parsePkString();

        object = context.getObjectByPk(qualifiedTableName, pkMap);
        ClassFormBuilder formBuilder =
                new ClassFormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.VIEW);
        form.readFromObject(object);

        relatedTableFormList = new ArrayList<RelatedTableForm>();
        for (Relationship relationship : table.getOneToManyRelationships()) {
            setupRelatedTableForm(relationship);
        }

        return READ;
    }

    public void setupRelatedTableForm(Relationship relationship) {
        List<Map<String, Object>> relatedObjects =
                context.getRelatedObjects(object,
                        relationship.getRelationshipName());

        Table relatedTable = relationship.getFromTable();
        ClassTableFormBuilder tableFormBuilder =
                new ClassTableFormBuilder(new TableAccessor(relatedTable));
        tableFormBuilder.configNRows(relatedObjects.size());
        TableForm tableForm = tableFormBuilder.build();
        tableForm.setMode(Mode.VIEW);
        tableForm.readFromObject(relatedObjects);

        RelatedTableForm relatedTableForm =
                new RelatedTableForm(relationship, tableForm, relatedObjects);
        relatedTableFormList.add(relatedTableForm);
    }

    //--------------------------------------------------------------------------
    // Create/Save
    //--------------------------------------------------------------------------

    public String create() throws ModelObjectNotFoundException {
        setupTable();

        ClassFormBuilder formBuilder = new ClassFormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.CREATE);

        return CREATE;
    }

    public String save() throws ModelObjectNotFoundException {
        setupTable();

        ClassFormBuilder formBuilder = new ClassFormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.CREATE);

        form.readFromRequest(req);
        if (form.validate()) {
            object = new HashMap<String, Object>();
            object.put("$type$", table.getQualifiedName());
            form.writeToObject(object);
            context.saveObject(object);
            pk = generatePkString(object);
            SessionMessages.addInfoMessage("SAVE avvenuto con successo");
            return SAVE;
        } else {
            return CREATE;
        }
    }

    //--------------------------------------------------------------------------
    // Edit/Update
    //--------------------------------------------------------------------------

    public String edit() throws ModelObjectNotFoundException {
        setupTable();
        parsePkString();
        object = context.getObjectByPk(qualifiedTableName, pkMap);

        ClassFormBuilder formBuilder = new ClassFormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.EDIT);

        form.readFromObject(object);

        return EDIT;
    }

    public String update() throws ModelObjectNotFoundException {
        setupTable();
        parsePkString();
        
        ClassFormBuilder formBuilder = new ClassFormBuilder(tableAccessor);
        form = formBuilder.build();
        form.setMode(Mode.EDIT);

        object = context.getObjectByPk(qualifiedTableName, pkMap);
        form.readFromObject(object);
        form.readFromRequest(req);
        if (form.validate()) {
            form.writeToObject(object);
            context.updateObject(object);
            SessionMessages.addInfoMessage("UPDATE avvenuto con successo");
            return UPDATE;
        } else {
            return EDIT;
        }
    }

    //--------------------------------------------------------------------------
    // Delete
    //--------------------------------------------------------------------------

    public String delete() throws ModelObjectNotFoundException {
        setupTable();
        parsePkString();
        object = context.getObjectByPk(qualifiedTableName, pkMap);
        context.deleteObject(object);
        SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        return DELETE;
    }

    //--------------------------------------------------------------------------
    // Cancel
    //--------------------------------------------------------------------------

    public String cancel() {
        return CANCEL;
    }

}
