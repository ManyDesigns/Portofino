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


import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.usecases.UseCase;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.util.PkHelper;
import com.manydesigns.portofino.actions.UseCaseAction;
import com.manydesigns.portofino.actions.CrudUnit;
import com.manydesigns.portofino.email.EmailHandler;
import com.manydesigns.portofino.system.model.users.User;

import java.io.Serializable;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UserAdminUseCaseAction extends UseCaseAction {
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

    public PkHelper pkHelper;

    @Override
    public void setupMetadata() {

        UseCase rootUseCase = model.findUseCaseByQualifiedName(qualifiedName);
        if (rootUseCase == null) {
            throw new ModelObjectNotFoundError(qualifiedName);
        }
        rootCrudUnit = setupUseCaseInstance(rootUseCase);
        TableAccessor classAccessor = context.getTableAccessor(rootUseCase.getActualTable().getQualifiedName());
        pkHelper = new PkHelper(classAccessor);
    }

    private CrudUnit setupUseCaseInstance(UseCase useCase) {
        ClassAccessor classAccessor =
                    context.getUseCaseAccessor(useCase.getQualifiedName());
        Table baseTable = useCase.getActualTable();
        String query = useCase.getQuery();
        CrudUnit result = new CrudUnit(classAccessor, baseTable, query,
                useCase.getSearchTitle(), useCase.getCreateTitle(),
                useCase.getReadTitle(), useCase.getEditTitle(), useCase.getName());
        result.buttons.addAll(useCase.getButtons());

        // inject values
        result.context = context;
        result.model = model;
        result.req = req;

        // expand recursively
        for (UseCase subUseCase : useCase.getSubUseCases()) {
            CrudUnit subCrudUnit = setupUseCaseInstance(subUseCase);
            result.subCrudUnits.add(subCrudUnit);
        }
        return result;
    }

    public UserAdminUseCaseAction() {
        super();
        this.pwdLength = Integer.parseInt(PortofinoProperties.getProperties()
                .getProperty("pwd.lenght.min","6"));
        enc = Boolean.parseBoolean(PortofinoProperties.getProperties()
                .getProperty(PortofinoProperties.PWD_ENCRYPTED, "false"));

    }


    //**************************************************************************
    // Setup
    //**************************************************************************

  /*  
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
    }*/


    //**************************************************************************
    // ResetPassword
    //**************************************************************************

    public void resetPassword() {
        setupMetadata();

        Serializable pkObject = pkHelper.parsePkString(pk);
        User user =  (User)context.getObjectByPk(userTable, pkObject);
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
    }
}
