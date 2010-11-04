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

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.PortofinoProperties;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.sql.Timestamp;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class LoginAction extends PortofinoAction
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


    //**************************************************************************
    // Presentation elements
    //**************************************************************************
    public Form form;
    public boolean recoverPwd;

    public static final Logger logger =
            LogUtil.getLogger(LoginAction.class);

    public LoginAction(){
        FormBuilder builder =
            new FormBuilder(User.class);
        builder.configFields("email", "pwd");

        form = builder.build();
        recoverPwd = false;
    }

    public String execute () {
        recoverPwd();

        return INPUT;
    }

    private void recoverPwd() {
        String smtp = PortofinoProperties.getProperties().
                getProperty(PortofinoProperties.MAIL_SMTP_HOST);
        if (null!=smtp) {
            recoverPwd=true;
        }
    }

    public String login (){
        recoverPwd();
        form.readFromRequest(req);
        User user = new User();
        form.writeToObject(user);
        final String email = user.getEmail();
        final String password = user.getPwd();
        user = context.login(email, password);
        if (null!=user && user.getUserId()!=0) {
            LogUtil.fineMF(logger, "User {0} login", user.getEmail());
            updateUser(user);
            return SUCCESS;
        } else {
            String errMsg = MessageFormat.format("FAILED AUTH for user {0}", email);
            SessionMessages.addInfoMessage(errMsg);            
            LogUtil.warningMF(logger, errMsg);
            updateFailedUser(email);
            return INPUT;
        }
    }

    private void updateFailedUser(String email) {
        User user;
        user = context.findUserByEmail(email);
        if (user == null) {
            return;
        }
        user.setLastFailedLoginDate(new Timestamp(new Date().getTime()));
        int failedAttempts = (null==user.getFailedLoginAttempts())?0:1;
        user.setFailedLoginAttempts(failedAttempts+1);
        context.updateObject("portofino.public.users", user);
        context.commit("portofino");
    }

    private void updateUser(User user) {
        user.setFailedLoginAttempts(0);
        user.setLastLoginDate(new Timestamp(new Date().getTime()));
        user.setToken(null);
        context.updateObject("portofino.public.users", user);
        context.commit("portofino");
    }

    public String logout(){
        recoverPwd();
        context.logout();
        return INPUT;
    }
}
