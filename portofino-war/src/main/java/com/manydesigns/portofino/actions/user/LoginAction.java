/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.annotations.InjectApplication;
import com.manydesigns.portofino.annotations.InjectHttpRequest;
import com.manydesigns.portofino.annotations.InjectHttpSession;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UserUtils;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding("/user/login.action")
public class LoginAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @InjectApplication
    public Application application;

    @InjectHttpRequest
    public HttpServletRequest request;

    @InjectHttpSession
    public HttpSession session;

    //**************************************************************************
    // Request parameters
    //**************************************************************************

    public String userName;
    public String pwd;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************
    public boolean recoverPwd;

    public String returnUrl;

    private static final String home = "/";
    public static final Logger logger =
            LoggerFactory.getLogger(LoginAction.class);

    public LoginAction(){

    }

    @DefaultHandler
    public Resolution execute () {
        if (session != null && session.getAttribute(SessionAttributes.USER_ID) != null) {
            return new ForwardResolution("/layouts/user/alreadyLoggedIn.jsp");
        }

        recoverPwd = Boolean.parseBoolean(PortofinoProperties.getProperties().
                getProperty(PortofinoProperties.MAIL_ENABLED, "false"));
        
        return new ForwardResolution("/layouts/user/login.jsp");
    }

    public Resolution login () {
        Boolean enc = Boolean.parseBoolean(PortofinoProperties.getProperties()
                .getProperty(PortofinoProperties.PWD_ENCRYPTED, "false"));

        if (enc) {
            pwd = UserUtils.encryptPassword(pwd);
        }
        User user = application.login(userName, pwd);

        if (user==null) {
            String errMsg = MessageFormat.format("FAILED AUTH for user {0}",
                    userName);
            SessionMessages.addInfoMessage(errMsg);
            logger.warn(errMsg);
            updateFailedUser(userName);
            return new ForwardResolution("/layouts/user/login.jsp");
        }

        if (!user.getState().equals(UserUtils.ACTIVE)) {
            String errMsg = MessageFormat.format("User {0} is not active. " +
                    "Please contact the administrator", userName);
            SessionMessages.addInfoMessage(errMsg);
            logger.warn(errMsg);
            return new ForwardResolution("/layouts/user/login.jsp");
        }

        logger.info("User {} login", user.getUserName());
        session = request.getSession(true);
        session.setAttribute(SessionAttributes.USER_ID, user.getUserId());
        session.setAttribute(SessionAttributes.USER_NAME, user.getUserName());
        updateUser(user);
        returnUrl = StringUtils.trimToNull(returnUrl);
        returnUrl=(returnUrl!=null)?returnUrl:home;

        return new RedirectResolution(returnUrl);
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
        session.invalidate();
        SessionMessages.addInfoMessage("User disconnetected");

        return "logout";
    }
}
