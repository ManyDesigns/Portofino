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
import com.manydesigns.elements.fields.TextField;
import com.manydesigns.elements.forms.FieldSet;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.systemModel.users.Password;
import com.manydesigns.portofino.systemModel.users.User;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class LostPwdChangeAction extends PortofinoAction 
        implements ServletRequestAware, LoginUnAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // ServletRequestAware implementation
    //**************************************************************************
    public HttpServletRequest req;

    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }

    public Form form;
    public String token;





    public static final Logger logger =
            LogUtil.getLogger(LostPwdChangeAction.class);



    public String execute() {
        try {
            ClassAccessor accessor = JavaClassAccessor.getClassAccessor(
                    Password.class);
            form = new Form();
            FieldSet fs = new FieldSet("passwordField", 1);
            TextField emailField
                    = new TextField(accessor.getProperty("email"));
            PasswordField pwdField
                    = new PasswordField(accessor.getProperty("pwd"));
            pwdField.setConfirmationRequired(true);
            pwdField.setRequired(true);
            emailField.setRequired(true);
            fs.add(emailField);
            fs.add(pwdField);


            form.add(fs);
            form.setMode(Mode.EDIT);

        } catch (NoSuchFieldException e) {
            LogUtil.warning(logger, "Field pwd not found", e);
        }

        return INPUT;
    }

    public String updatePwd() {
        try {
            Password pwd = new Password();

            form = new Form();
            ClassAccessor pwdAccessor = JavaClassAccessor
                    .getClassAccessor(Password.class);
            ClassAccessor userAccessor =
                    context.getTableAccessor("portofino.public.user_");
            FormBuilder userBuilder = new FormBuilder(userAccessor);

            FieldSet fs = new FieldSet("passwordField", 1);
            TextField emailField =
                    null;
            PasswordField pwdField =
                    null;
            if (pwdField != null) {
                pwdField.setRequired(true);
            }
            if (emailField != null) {
                emailField.setRequired(true);
            }
            try {
                emailField = new TextField(pwdAccessor.getProperty("email"));
                pwdField = new PasswordField(pwdAccessor.getProperty("pwd"));
            } catch (NoSuchFieldException e) {
                LogUtil.warning(logger, "Field pwd not found", e);
            }

            if (pwdField != null) {
                pwdField.setConfirmationRequired(true);
            }
            fs.add(emailField);
            fs.add(pwdField);

            form.add(fs);
            form.readFromRequest(req);
            form.writeToObject(pwd);

            if(!form.validate()){
            form.setMode(Mode.EDIT);
            return INPUT;
            }

            User user = context.findUserByToken(token);
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

            SessionMessages.addInfoMessage("Password aggiornata correttamente");

            return SUCCESS;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}