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
package com.manydesigns.portofino.actions.user.admin;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.SearchFormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.RelatedTableForm;
import com.manydesigns.portofino.actions.model.TableDataAction;
import com.manydesigns.portofino.email.EmailHandler;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import com.manydesigns.portofino.util.DummyHttpServletRequest;
import com.manydesigns.portofino.util.PkHelper;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UserAction extends TableDataAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************
    private static final String userTable = "portofino.public.users";
    private static final String groupTable = "portofino.public.groups";
    private static final String usersGroupsTable = "portofino.public.users_groups";
    private final int pwdLength;
    private final Boolean enc;


    //**************************************************************************
    // Web parameters
    //**************************************************************************
    public String[] ng_selection;

    //**************************************************************************
    // Model objects
    //**************************************************************************

    public User user;
    public List<Object> users;
    public List<Group> activeGroups;



    //**************************************************************************
    // Presentation elements
    //**************************************************************************
    public TableForm activeGroupsForm;
    public TableForm deletedGroupsForm;
    public TableForm newGroupsForm;

    //**************************************************************************
    // Other objects
    //**************************************************************************

    public static final Logger logger =
            LogUtil.getLogger(UserAction.class);

    public UserAction() {
        this.pwdLength = Integer.parseInt(PortofinoProperties.getProperties()
                .getProperty("pwd.lenght.min","6"));
        enc = Boolean.parseBoolean(PortofinoProperties.getProperties()
                .getProperty(PortofinoProperties.PWD_ENCRYPTED, "false"));
    }

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

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
        setupMetadata();
        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
        searchFormBuilder.configFields("userId","email","state","lastName", "firstName");
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

        Criteria criteria = new Criteria(classAccessor);
        searchForm.configureCriteria(criteria);
        users = context.getObjects(criteria);

        String readLinkExpression = getReadLinkExpression();
        OgnlTextFormat hrefFormat =
                OgnlTextFormat.create(readLinkExpression);
        hrefFormat.setUrl(true);

        TableFormBuilder tableFormBuilder =
                createTableFormBuilderWithSelectionProviders()
                        .configNRows(users.size())
                        .configMode(Mode.VIEW);
        tableFormBuilder.configFields("userId","email","state",
                "lastName", "firstName");

        // ogni colonna chiave primaria sarà clickabile
        for (PropertyAccessor property : classAccessor.getKeyProperties()) {
            tableFormBuilder.configHyperlinkGenerators(
                    property.getName(), hrefFormat, null);
        }

        tableForm = tableFormBuilder.build();
        tableForm.setKeyGenerator(pkHelper.createPkGenerator());
        tableForm.setSelectable(true);
        tableForm.readFromObject(users);

        return SEARCH;
    }


    public String getReadLinkExpression() {
        StringBuilder sb = new StringBuilder("/user-admin/Users.action?pk=");
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
    // Read
    //**************************************************************************

    public String read() {
        setupMetadata();
        Serializable pkObject = pkHelper.parsePkString(pk);

        SearchFormBuilder searchFormBuilder =
                new SearchFormBuilder(classAccessor);
        searchForm = searchFormBuilder.build();
        configureSearchFormFromString();

        Criteria criteria = new Criteria(classAccessor);
        searchForm.configureCriteria(criteria);
        users = context.getObjects(criteria);

        user = (User) context.getObjectByPk(userTable, pkObject);
        FormBuilder builder = createFormBuilderWithSelectionProviders()
                .configMode(Mode.VIEW);
        builder.configFields("userId","email","state",
                "lastName", "firstName");
        form = builder.build();
        form.readFromObject(user);
        relatedTableFormList = new ArrayList<RelatedTableForm>();

        setupActiveGroups();
        setupDeletedGroups();
        setupNewGroups();
        return READ;
    }

    protected void setupActiveGroups(){
        try {
            TableAccessor ugAccessor = context.getTableAccessor(usersGroupsTable);
            Criteria criteria = new Criteria(ugAccessor);
            criteria.eq(ugAccessor.getProperty("userid"), user.getUserId());
            criteria.isNull(ugAccessor.getProperty("deletionDate"));
            users = context.getObjects(criteria);

            TableFormBuilder tableFormBuilder
                    = new TableFormBuilder(ugAccessor).configMode(Mode.VIEW)

                    .configNRows(users.size())
                    .configFields("userid", "groupid", "creationDate");

            // setup relationship lookups
            Table table = model.findTableByQualifiedName(usersGroupsTable);
            if(users.size()>0) {
                for (ForeignKey rel : table.getForeignKeys()) {
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
                    tableFormBuilder.configSelectionProvider
                            (selectionProvider, fieldNames);
                }
            }
            activeGroupsForm = tableFormBuilder.build();
            if (users.size()>0){
                PkHelper agPk = new PkHelper(ugAccessor);
                activeGroupsForm.setKeyGenerator(agPk.createPkGenerator());
                activeGroupsForm.setSelectable(true);
                activeGroupsForm.readFromObject(users);
            }
        } catch (NoSuchFieldException e) {
            LogUtil.warning(logger, "cannot find property userid", e);
        }
    }

    protected void setupDeletedGroups(){
        try {
            TableAccessor ugAccessor = context.getTableAccessor(usersGroupsTable);

            Criteria criteria = new Criteria(ugAccessor);
            criteria.eq(ugAccessor.getProperty("userid"), user.getUserId());
            criteria.isNotNull(ugAccessor.getProperty("deletionDate"));
            List<Object> objects = context.getObjects(criteria);

            TableFormBuilder tableFormBuilder
                    = new TableFormBuilder(ugAccessor).configNRows(objects.size())
                    .configMode(Mode.VIEW);


            // setup relationship lookups
            Table table = model.findTableByQualifiedName(usersGroupsTable);
            for (ForeignKey rel : table.getForeignKeys()) {
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

                tableFormBuilder.configSelectionProvider
                        (selectionProvider, fieldNames);
            }


            deletedGroupsForm = tableFormBuilder.build();
            deletedGroupsForm.readFromObject(objects);
        } catch (NoSuchFieldException e) {
            LogUtil.warning(logger, "cannot find property userid", e);
        }
    }

    protected void setupNewGroups(){
        try {
            TableAccessor groupAccessor = context.getTableAccessor(groupTable);
            Criteria criteria = new Criteria(groupAccessor);
            criteria.isNull(groupAccessor.getProperty("deletionDate"));

            List<Object> objects = context.getObjects(criteria);

            TableFormBuilder tableFormBuilder
                    = new TableFormBuilder(groupAccessor)
                    .configPrefix("ng_")
                    .configFields("name", "description")
                    .configNRows(objects.size())
                    .configMode(Mode.VIEW);

            
            newGroupsForm = tableFormBuilder.build();
            if (objects.size()>0){
                PkHelper agPk = new PkHelper(groupAccessor);
                newGroupsForm.setKeyGenerator(agPk.createPkGenerator());
                newGroupsForm.setSelectable(true);
                newGroupsForm.readFromObject(objects);
            }


        } catch (NoSuchFieldException e) {
            LogUtil.warning(logger, "cannot find property active", e);
        }
    }


    //**************************************************************************
    // Create/Save
    //**************************************************************************

    public String create() {
        setupMetadata();

        final FormBuilder builder = createFormBuilderWithSelectionProviders()
                .configMode(Mode.CREATE);
        builder.configFields("userId","email","state",
                "lastName", "firstName");
        form = builder.build();

        return CREATE;
    }

    public String save() {
        setupMetadata();

        final FormBuilder builder = createFormBuilderWithSelectionProviders()
                .configMode(Mode.CREATE);
        builder.configFields("userId","email","state",
                "lastName", "firstName");
        form = builder.build();

        form.readFromRequest(req);
        if (form.validate()) {
            user = (User) classAccessor.newInstance();
            form.writeToObject(user);
            context.saveObject(userTable, user);
            String databaseName = model
                    .findTableByQualifiedName(userTable)
                    .getDatabaseName();
            context.commit(databaseName);
            pk = pkHelper.generatePkString(user);
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

        user = (User) context.getObjectByPk(userTable, pkObject);

        FormBuilder builder = createFormBuilderWithSelectionProviders()
                .configMode(Mode.EDIT);
        builder.configFields("userId","email","state",
                "lastName", "firstName");
        form = builder.build();

        form.readFromObject(user);
        return EDIT;
    }

    public String update() {
        setupMetadata();
        Serializable pkObject = pkHelper.parsePkString(pk);

        FormBuilder builder = createFormBuilderWithSelectionProviders()
                .configMode(Mode.EDIT);
        builder.configFields("userId","email","state",
                "lastName", "firstName");
        form = builder.build();

        user =  (User)context.getObjectByPk(userTable, pkObject);
        form.readFromObject(user);
        form.readFromRequest(req);
        if (form.validate()) {
            form.writeToObject(user);
            context.updateObject(userTable, user);
            String databaseName = model
                    .findTableByQualifiedName(userTable).getDatabaseName();
            context.commit(databaseName);
            SessionMessages.addInfoMessage("UPDATE avvenuto con successo");
            return UPDATE;
        } else {
            return EDIT;
        }
    }


    //**************************************************************************
    // Delete
    //**************************************************************************

    public String delete() {
        setupMetadata();
        User pkUsr = new User(new Long(pk));
        User aUser = (User) context.getObjectByPk(userTable, pkUsr);
        aUser.setDeletionDate(new Timestamp(System.currentTimeMillis()));
        context.saveObject(userTable, aUser);
        String databaseName = model.findTableByQualifiedName(userTable)
                .getDatabaseName();
        context.commit(databaseName);
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
            User pkUsr = new User(new Long(current));
            User aUser = (User) context.getObjectByPk(userTable, pkUsr);
            aUser.setDeletionDate(new Timestamp(System.currentTimeMillis()));
            context.saveObject(userTable, aUser);
        }
        String databaseName = model.findTableByQualifiedName(userTable)
                .getDatabaseName();
        context.commit(databaseName);
        SessionMessages.addInfoMessage(MessageFormat.format(
                "DELETE di {0} oggetti avvenuto con successo", selection.length));
        return DELETE;
    }

    //**************************************************************************
    // Remove user from Group
    //**************************************************************************
    
    public String removeGroups(){

        if (null==selection) {
            SessionMessages.addInfoMessage("No group selected");
            return read();
        }
        for (String current : selection) {
            TableAccessor ugAccessor = context.getTableAccessor(usersGroupsTable);
            PkHelper agPkHelper = new PkHelper(ugAccessor);
            UsersGroups ug = (UsersGroups) agPkHelper.parsePkString(current);
            ug = (UsersGroups) context.getObjectByPk(usersGroupsTable,ug);
            ug.setDeletionDate(new Timestamp(new Date().getTime()));
            context.updateObject(usersGroupsTable, ug);
        }
        context.commit("portofino");
        SessionMessages.addInfoMessage("Group(s) removed");
        return read();
    }

    //**************************************************************************
    // Add user to Group
    //**************************************************************************

    public String addGroups(){

        if (null==ng_selection) {
            SessionMessages.addInfoMessage("No group selected");
            return read();
        }
        for (String current : ng_selection) {
            UsersGroups newUg = new UsersGroups();
            newUg.setCreationDate(new Timestamp(new Date().getTime()));
            newUg.setGroupid(Long.valueOf(current));
            Group pkGrp = new Group(Long.valueOf(current));
            newUg.setGroup((Group) context.getObjectByPk(groupTable, pkGrp));
            newUg.setUserid(Long.valueOf(pk));
            User pkUsr = new User(Long.valueOf(pk));
            newUg.setUser((User) context.getObjectByPk(userTable, pkUsr));

            context.saveObject(usersGroupsTable, newUg);
        }
        context.commit("portofino");
        SessionMessages.addInfoMessage("Group added");
        return read();
    }

    //**************************************************************************
    // ResetPassword
    //**************************************************************************

    public String resetPassword() {
        setupMetadata();
        Serializable pkObject = pkHelper.parsePkString(pk);
        user =  (User)context.getObjectByPk(userTable, pkObject);
        user.passwordGenerator(pwdLength);
        String generatedPwd = user.getPwd();

        final Properties properties = PortofinoProperties.getProperties();

        boolean mailEnabled = Boolean.parseBoolean(
                properties.getProperty(PortofinoProperties.MAIL_ENABLED, "false"));

        if (mailEnabled) {
        String msg = "La tua nuova password è " + generatedPwd;
        EmailHandler.addEmail(context, "new password", msg,
                user.getEmail(), context.getCurrentUser().getEmail());

        } else {
           SessionMessages.addInfoMessage("La nuova password per l'utente è "+generatedPwd); 
        }
        if (enc){
            user.encryptPwd();
        }
        String databaseName = model
                .findTableByQualifiedName(userTable).getDatabaseName();
        context.updateObject(userTable, user);
        context.commit(databaseName);

        SessionMessages.addInfoMessage("UPDATE avvenuto con successo");
        return read();
    }

}
