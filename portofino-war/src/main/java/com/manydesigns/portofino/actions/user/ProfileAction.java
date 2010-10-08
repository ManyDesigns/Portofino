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
import com.manydesigns.elements.fields.PasswordField;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.systemModel.users.Group;
import com.manydesigns.portofino.systemModel.users.Password;
import com.manydesigns.portofino.systemModel.users.User;
import com.manydesigns.portofino.systemModel.users.UsersGroups;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ProfileAction extends PortofinoAction implements ServletRequestAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // ServletRequestAware implementation
    //**************************************************************************
    public HttpServletRequest req;

    public ProfileAction() {
        groups = new ArrayList<Group>();
    }

    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }

    //**************************************************************************
    // User
    //**************************************************************************
    public User user;
    public List<Group> groups;
    public Form form;

    //**************************************************************************
    // Modo
    //**************************************************************************
    public int state;
    public static final int VIEW=1;
    public static final int EDIT=2;
    public static final int CHANGE_PWD=3;


    public static final Logger logger =
            LogUtil.getLogger(ProfileAction.class);

    public String execute() {
        return read();
    }

    private boolean setUser() {
        user = context.getCurrentUser();
        groups.clear();
        for (UsersGroups group : user.getGroups())
        {
            groups.add(group.getGroup());
        }
        //se l'utente è null lo mando ad Unauthorized
        if (user==null) {
            return true;
        }
        return false;
    }

    private String read() {
        if (setUser()) {
            return UNAUTHORIZED;
        }

        ClassAccessor accessor = context.getTableAccessor("portofino.public.user_");
        FormBuilder formBuilder = new FormBuilder(accessor);
        formBuilder.configFields("email", "screenName", "firstName",
                "middleName", "lastName", "createDate");
        form = formBuilder.build();
        form.readFromObject(user);
        form.setMode(Mode.VIEW);
        state=VIEW;
        return SUCCESS;
    }

    public String edit() {
        if (setUser()) {
            return UNAUTHORIZED;
        }
        ClassAccessor accessor = context.getTableAccessor("portofino.public.user_");
        FormBuilder formBuilder = new FormBuilder(accessor);
        formBuilder.configFields("email", "screenName", "firstName",
                "middleName", "lastName");
        form = formBuilder.build();
        form.readFromObject(user);
        form.setMode(Mode.EDIT);
        state=EDIT;
        return SUCCESS;
    }

    public String update() {
        if (setUser()) {
            return UNAUTHORIZED;
        }
        ClassAccessor accessor = context.getTableAccessor("portofino.public.user_");
        FormBuilder formBuilder = new FormBuilder(accessor);
        formBuilder.configFields("email", "screenName", "firstName",
                "middleName", "lastName");
        form = formBuilder.build();
        form.readFromRequest(req);
        
        if(!form.validate()){
            form.setMode(Mode.EDIT);
            state = EDIT;
            return SUCCESS;
        }

        form.writeToObject(user);

        context.updateObject("portofino.public.user_", user);
        context.commit("portofino");
        context.setCurrentUser(user);

        LogUtil.finestMF(logger, "User {0} updated", user.getEmail());

        form.setMode(Mode.VIEW);
        state=VIEW;
        SessionMessages.addInfoMessage("Utente aggiornato correttamente");
        return SUCCESS;
    }

    public String changePwd() {
        if (setUser()) {
            return UNAUTHORIZED;
        }

        try {
            ClassAccessor accessor = JavaClassAccessor.getClassAccessor(Password.class);
            form = new Form();
            FieldSet fs = new FieldSet("passwordField", 1);
            PasswordField oldPwdField = new PasswordField(accessor.getProperty("oldPwd"));
            PasswordField pwdField = new PasswordField(accessor.getProperty("pwd"));
            pwdField.setConfirmationRequired(true);
            fs.add(oldPwdField);
            fs.add(pwdField);

            form.add(fs);
            form.setMode(Mode.EDIT);
            state=EDIT;
        } catch (NoSuchFieldException e) {
            LogUtil.warning(logger, "Field pwd not found", e);
        }

        state=CHANGE_PWD;
        return SUCCESS;
    }

    public String updatePwd() {
        Password pwd = new Password();

        if (setUser()) {
            return UNAUTHORIZED;
        }

        form = new Form();
        ClassAccessor pwdAccessor = JavaClassAccessor
                .getClassAccessor(Password.class);
        ClassAccessor userAccessor =
                context.getTableAccessor("portofino.public.user_");
        FormBuilder userBuilder = new FormBuilder(userAccessor);

        FieldSet fs = new FieldSet("passwordField", 1);
        PasswordField oldPwdField =
                null;
        PasswordField pwdField =
                null;
        try {
            oldPwdField = new PasswordField(pwdAccessor.getProperty("oldPwd"));
            pwdField = new PasswordField(pwdAccessor.getProperty("pwd"));
        } catch (NoSuchFieldException e) {
            LogUtil.warning(logger, "Field pwd not found", e);  
        }

        pwdField.setConfirmationRequired(true);
        fs.add(oldPwdField);
        fs.add(pwdField);

        form.add(fs);
        form.readFromRequest(req);
        form.writeToObject(pwd);

        if(!form.validate()){
            form.setMode(Mode.EDIT);
            state = CHANGE_PWD;
            return SUCCESS;
        }

        if(!pwd.getOldPwd().equals(user.getPwd())){
            SessionMessages.addErrorMessage
                    ("La password non corrisponde a quella in uso");
            form.setMode(Mode.EDIT);
            state = CHANGE_PWD;
            return SUCCESS;
        }

        user.setPwd(pwd.getPwd());
        user.setPwdModDate(new Date());
        context.updateObject("portofino.public.user_", user);
        context.commit("portofino");
        context.setCurrentUser(user);

        LogUtil.finestMF(logger, "User {0} updated", user.getEmail());
        userBuilder.configFields("email", "screenName", "firstName",
                "middleName", "lastName", "createDate");

        form = userBuilder.build();
        form.readFromObject(user);
        form.setMode(Mode.VIEW);
        state=VIEW;
        SessionMessages.addInfoMessage("Password aggiornata correttamente");

        return SUCCESS;
    }
}
