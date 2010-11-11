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


import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.UseCaseAction;
import com.manydesigns.portofino.email.EmailHandler;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
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


    //**************************************************************************
    // Web parameters
    //**************************************************************************
    public String[] activeselection;
    public String[] currentselection;

    public UserAdminUseCaseAction() {
        super();
        this.pwdLength = Integer.parseInt(PortofinoProperties.getProperties()
                .getProperty("pwd.lenght.min","6"));
        enc = Boolean.parseBoolean(PortofinoProperties.getProperties()
                .getProperty(PortofinoProperties.PWD_ENCRYPTED, "false"));

    }



    //**************************************************************************
    // Remove user from Group
    //**************************************************************************

    public void removeGroups() throws NoSuchFieldException {
        String pk = rootCrudUnit.pk;
        if (null==currentselection) {
            SessionMessages.addInfoMessage("No group selected");
            return;
        }
        for (String current : currentselection) {
            TableAccessor ugAccessor = context.getTableAccessor(usersGroupsTable);

            Criteria criteria = new Criteria(ugAccessor);
            criteria.eq(ugAccessor.getProperty("userid"), Long.parseLong(pk));
            criteria.eq(ugAccessor.getProperty("groupid"), Long.parseLong(current));
            criteria.isNull(ugAccessor.getProperty("deletionDate"));
            
            List<Object> ugList = context.getObjects(criteria);
            for(Object obj : ugList) {
                UsersGroups ug = (UsersGroups) obj;
                ug.setDeletionDate(new Timestamp(new Date().getTime()));
                context.updateObject(usersGroupsTable, ug);
            }
        }
        context.commit("portofino");
        SessionMessages.addInfoMessage("Group(s) removed");

    }

    //**************************************************************************
    // Add user to Group
    //**************************************************************************

    public void addGroups(){
        String pk = rootCrudUnit.pk;
        if (null==activeselection) {
            SessionMessages.addInfoMessage("No group selected");
            return;
        }
        for (String current : activeselection) {
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
    }


    //**************************************************************************
    // ResetPassword
    //**************************************************************************

    public void resetPassword() {
        String pk = rootCrudUnit.pk;
        Serializable pkObject = rootCrudUnit.pkHelper.parsePkString(pk);
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
