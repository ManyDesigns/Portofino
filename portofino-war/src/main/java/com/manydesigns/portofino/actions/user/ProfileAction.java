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
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UserUtils;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private Long userId;

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
    public Form form;

    public static final Logger logger =
            LoggerFactory.getLogger(ProfileAction.class);

    public String execute() {
        Map session = getSession();
        userId = (Long) session.get(UserUtils.USERID);
        return read();
    }

    private String read() {
        User thisUser =
            (User) context.getObjectByPk(UserUtils.USERTABLE, new User(userId));
        ClassAccessor accessor = context.getTableAccessor(UserUtils.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        formBuilder.configFields("email", "userName", "firstName",
                "middleName", "lastName", "creationDate");
        for (UsersGroups ug : thisUser.getGroups()){
            Group grp = ug.getGroup();

            if(ug.getDeletionDate()==null){
                groups.add(grp);
            }
        }
        form = formBuilder
                .configMode(Mode.VIEW)
                .build();
        form.readFromObject(thisUser);
        return READ;
    }

    public String edit() {
        Map session = getSession();
        userId = (Long) session.get(UserUtils.USERID);
        User thisUser =
            (User) context.getObjectByPk(UserUtils.USERTABLE, new User(userId));

        ClassAccessor accessor = context.getTableAccessor(UserUtils.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        form = formBuilder
                .configFields("email", "userName", "firstName",
                        "middleName", "lastName")
                .configMode(Mode.EDIT)
                .build();
        form.readFromObject(thisUser);
        return EDIT;
    }

    public String update() {
        Map session = getSession();
        userId = (Long) session.get(UserUtils.USERID);
        User thisUser =
            (User) context.getObjectByPk(UserUtils.USERTABLE, new User(userId));
        ClassAccessor accessor = context.getTableAccessor(UserUtils.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        form = formBuilder
                .configFields("email", "userName", "firstName",
                        "middleName", "lastName")
                .configMode(Mode.EDIT)
                .build();
        form.readFromObject(thisUser);
        form.readFromRequest(req);
        
        if(form.validate()){
            form.writeToObject(thisUser);
            context.updateObject(UserUtils.USERTABLE, thisUser);
            context.commit("portofino");
            logger.debug("User {} updated", thisUser.getEmail());
            SessionMessages.addInfoMessage("Utente aggiornato correttamente");
            return UPDATE;
        } else {
            return EDIT;
        }
    }

    public String changePwd() {
        Map session = getSession();
        userId = (Long) session.get(UserUtils.USERID);
        form = new FormBuilder(ChangePasswordFormBean.class).configFields("oldPwd", "pwd")
                .configMode(Mode.EDIT)
                .build();
        return CHANGE_PWD;
    }

    public String updatePwd() {
        Map session = getSession();
        userId = (Long) session.get(UserUtils.USERID);
        User thisUser =
            (User) context.getObjectByPk(UserUtils.USERTABLE, new User(userId));

        form = new FormBuilder(ChangePasswordFormBean.class).configFields("oldPwd", "pwd")
                .configMode(Mode.EDIT)
                .build();
        form.readFromRequest(req);

        if(form.validate()) {
            ChangePasswordFormBean bean = new ChangePasswordFormBean();
            form.writeToObject(bean);

            if(bean.getEncOldPwd().equals(thisUser.getPwd())) {
                thisUser.setPwd(bean.pwd);
                if (enc) {
                    thisUser.encryptPwd();
                }
                thisUser.setPwdModDate(new Timestamp(new Date().getTime()));
                context.updateObject(UserUtils.USERTABLE, thisUser);
                context.commit("portofino");

                logger.debug("User {} updated", thisUser.getEmail());
                SessionMessages.addInfoMessage("Password correctely updated");

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
