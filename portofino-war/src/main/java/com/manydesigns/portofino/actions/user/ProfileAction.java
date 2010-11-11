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

package com.manydesigns.portofino.actions.user;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import com.manydesigns.portofino.system.model.users.UserDefs;
import com.manydesigns.portofino.PortofinoProperties;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.sql.Timestamp;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ProfileAction extends PortofinoAction implements ServletRequestAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final String CHANGE_PWD = "changePwd";
    public static final String UPDATE_PWD = "updatePwd";

    //**************************************************************************
    // ServletRequestAware implementation
    //**************************************************************************
    public HttpServletRequest req;
    public List<Group> groups;

    private Boolean enc;

    public ProfileAction() {
        groups = new ArrayList<Group>();
        enc = Boolean.parseBoolean(PortofinoProperties.getProperties()
                .getProperty(PortofinoProperties.PWD_ENCRYPTED, "false"));
    }


    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }

    //**************************************************************************
    // User
    //**************************************************************************
    public User user;
    public Form form;

    public static final Logger logger =
            LogUtil.getLogger(ProfileAction.class);

    public String execute() {
        return read();
    }

    private String read() {
        user = context.getCurrentUser();
        ClassAccessor accessor = context.getTableAccessor(UserDefs.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        formBuilder.configFields("email", "userName", "firstName",
                "middleName", "lastName", "creationDate");
        User thisUser = (User) context.getObjectByPk(UserDefs.USERTABLE, user);
        for (UsersGroups ug : thisUser.getGroups()){
            Group grp = ug.getGroup();

            if(ug.getDeletionDate()==null){
                groups.add(grp);
            }
        }
        form = formBuilder
                .configMode(Mode.VIEW)
                .build();
        form.readFromObject(user);
        return READ;
    }

    public String edit() {
        user = context.getCurrentUser();

        ClassAccessor accessor = context.getTableAccessor(UserDefs.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        form = formBuilder
                .configFields("email", "userName", "firstName",
                        "middleName", "lastName")
                .configMode(Mode.EDIT)
                .build();
        form.readFromObject(user);
        return EDIT;
    }

    public String update() {
        user = context.getCurrentUser();

        ClassAccessor accessor = context.getTableAccessor(UserDefs.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        form = formBuilder
                .configFields("email", "userName", "firstName",
                        "middleName", "lastName")
                .configMode(Mode.EDIT)
                .build();
        form.readFromObject(user);
        form.readFromRequest(req);
        
        if(form.validate()){
            form.writeToObject(user);
            context.updateObject(UserDefs.USERTABLE, user);
            context.commit("portofino");
            LogUtil.finestMF(logger, "User {0} updated", user.getEmail());
            SessionMessages.addInfoMessage("Utente aggiornato correttamente");
            return UPDATE;
        } else {
            return EDIT;
        }
    }

    public String changePwd() {
        form = new FormBuilder(ChangePasswordFormBean.class).configFields("oldPwd", "pwd")
                .configMode(Mode.EDIT)
                .build();
        return CHANGE_PWD;
    }

    public String updatePwd() {
        user = context.getCurrentUser();

        form = new FormBuilder(ChangePasswordFormBean.class).configFields("oldPwd", "pwd")
                .configMode(Mode.EDIT)
                .build();
        form.readFromRequest(req);

        if(form.validate()) {
            ChangePasswordFormBean bean = new ChangePasswordFormBean();
            form.writeToObject(bean);

            if(bean.getEncOldPwd().equals(user.getPwd())) {
                user.setPwd(bean.pwd);


                if (enc) {
                    user.encryptPwd();
                }
                user.setPwdModDate(new Timestamp(new Date().getTime()));
                context.updateObject(UserDefs.USERTABLE, user);
                context.commit("portofino");

                LogUtil.finestMF(logger, "User {0} updated", user.getEmail());
                SessionMessages.addInfoMessage("Password coorectely updated");

                return UPDATE_PWD;
            } else {
                SessionMessages.addErrorMessage
                        ("La password non corrisponde a quella in uso");
                return CHANGE_PWD;
            }
        } else {
            return CHANGE_PWD;
        }
    }
}
