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
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.annotations.InjectApplication;
import com.manydesigns.portofino.annotations.InjectHttpSession;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UserUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class LoginAction extends AbstractActionBean implements LoginUnAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @InjectApplication
    public Application application;

    @InjectHttpSession
    public HttpSession session;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************
    public Form form;
    public boolean recoverPwd;

    public String returnUrl;
    //public Map parameters;
    
    private static final String home = "/";
    public static final Logger logger =
            LoggerFactory.getLogger(LoginAction.class);

    public LoginAction(){
        FormBuilder builder =
            new FormBuilder(User.class);
        builder.configFields("userName", "pwd");

        form = builder.build();
        recoverPwd = Boolean.parseBoolean(PortofinoProperties.getProperties().
                getProperty(PortofinoProperties.MAIL_ENABLED, "false"));

    }

    public String execute () {
        session.removeAttribute(UserUtils.USERID);
        session.removeAttribute(UserUtils.USERNAME);
        return "INPUT";
    }

    public String login (){
        form.readFromRequest(context.getRequest());
        User user = new User();
        form.writeToObject(user);
        Boolean enc = Boolean.parseBoolean(PortofinoProperties.getProperties()
                .getProperty(PortofinoProperties.PWD_ENCRYPTED, "false"));

        if (enc) {
            user.encryptPwd();
        }
        String username = user.getUserName();
        String password = user.getPwd();
        user = application.login(username, password);

        if (user==null) {
            String errMsg = MessageFormat.format("FAILED AUTH for user {0}",
                    username);
            SessionMessages.addInfoMessage(errMsg);
            logger.warn(errMsg);
            updateFailedUser(username);
            return "INPUT";
        }

        if (!user.getState().equals(UserUtils.ACTIVE)) {
            String errMsg = MessageFormat.format("User {0} is not active. " +
                    "Please contact the administrator", username);
            SessionMessages.addInfoMessage(errMsg);
            logger.warn(errMsg);
            return "INPUT";
        }

        logger.info("User {} login", user.getUserName());
        session.setAttribute(UserUtils.USERID, user.getUserId());
        session.setAttribute(UserUtils.USERNAME, user.getUserName());
        updateUser(user);
        returnUrl = StringUtils.trimToNull(returnUrl);
        returnUrl=(returnUrl!=null)?returnUrl:home;

        return "SUCCESS";
    }

    private void updateFailedUser(String username) {
        User user;
        user = application.findUserByUserName(username);
        if (user == null) {
            return;
        }
        user.setLastFailedLoginDate(new Timestamp(new Date().getTime()));
        int failedAttempts = (null==user.getFailedLoginAttempts())?0:1;
        user.setFailedLoginAttempts(failedAttempts+1);
        application.updateObject(UserUtils.USERTABLE, user);
        application.commit("portofino");
    }

    private void updateUser(User user) {
        user.setFailedLoginAttempts(0);
        user.setLastLoginDate(new Timestamp(new Date().getTime()));
        user.setToken(null);
        application.updateObject(UserUtils.USERTABLE, user);
        application.commit("portofino");
    }

    public String logout(){
        session.removeAttribute(UserUtils.USERID);
        SessionMessages.addInfoMessage("User disconnetected");

        return "logout";
    }
}
