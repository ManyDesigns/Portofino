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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.system.model.users.User;
import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import groovy.lang.Script;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding("/actions/user/login")
public class LoginAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";
    public static final String LOGIN_ACTION_NAME = "loginAction";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    //**************************************************************************
    // Request parameters
    //**************************************************************************

    public String userName;
    public String pwd;

    //**************************************************************************
    // Scripting
    //**************************************************************************

    protected GroovyObject groovyObject;
    protected String script;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************
    public boolean recoverPwd;

    public String returnUrl;
    public String cancelReturnUrl;

    public static final Logger logger =
            LoggerFactory.getLogger(LoginAction.class);

    public LoginAction(){

    }

    @DefaultHandler
    public Resolution execute () {
        HttpSession session = getSession();
        if (session != null && session.getAttribute(SessionAttributes.USER_ID) != null) {
            return new ForwardResolution("/layouts/user/alreadyLoggedIn.jsp");
        }

        recoverPwd = portofinoConfiguration.getBoolean(
                PortofinoProperties.MAIL_ENABLED, false);

        return new ForwardResolution("/layouts/user/login.jsp");
    }

    protected void prepareScript() {
        File file = new File(application.getAppTextDir(), "security.groovy");
        if(file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                script = IOUtils.toString(fr);
                IOUtils.closeQuietly(fr);
                groovyObject = ScriptingUtil.getGroovyObject(script, file.getAbsolutePath());
                Script scriptObject = (Script) groovyObject;
                Binding binding = new Binding(ElementsThreadLocals.getOgnlContext());
                binding.setVariable(LOGIN_ACTION_NAME, this);
                scriptObject.setBinding(binding);
            } catch (Exception e) {
                logger.warn("Couldn't load script for login page", e);
            }
        }
    }

    @Button(list = "login-buttons", key = "commons.login", order = 1)
    public Resolution login() {
        boolean enc = portofinoConfiguration.getBoolean(
                PortofinoProperties.PWD_ENCRYPTED, true);

        User user;
        prepareScript();
        if(groovyObject != null) {
            user = (User) groovyObject.invokeMethod("login", new Object[] { userName, pwd });
        } else {
            if (enc) {
                pwd = SecurityLogic.encryptPassword(pwd);
            }
            user = SecurityLogic.defaultLogin(application, userName, pwd);
        }

        Locale locale = context.getLocale();
        ResourceBundle bundle = application.getBundle(locale);

        if (user==null) {
            String errMsg = MessageFormat.format(bundle.getString("user.login.failed"), userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg);
            updateFailedUser(userName);
            return new ForwardResolution("/layouts/user/login.jsp");
        }

        if (!user.getState().equals(SecurityLogic.ACTIVE)) {
            String errMsg = MessageFormat.format(bundle.getString("user.not.active"), userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg);
            return new ForwardResolution("/layouts/user/login.jsp");
        }

        updateUser(user);
        logger.info("User {} login", user.getUserName());
        HttpSession session = context.getRequest().getSession(true);
        session.setAttribute(SessionAttributes.USER_ID, user.getUserId());
        session.setAttribute(SessionAttributes.USER_NAME, user.getUserName());

        if (StringUtils.isEmpty(returnUrl)) {
            returnUrl = "/";
        }
        logger.debug("Redirecting to: {}", returnUrl);
        return new RedirectResolution(returnUrl);
    }

    @Button(list = "login-buttons", key = "commons.cancel", order = 2)
    public Resolution cancel() {
        String url = "/";
        if(!StringUtils.isBlank(cancelReturnUrl)) {
            url = cancelReturnUrl;
        } else if(!StringUtils.isBlank(returnUrl)) {
            url = returnUrl;
        }
        return new RedirectResolution(url);
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
        Session session = application.getSession("portofino");
        session.update(user);
        session.getTransaction().commit();
    }

    private void updateUser(User user) {
        user.setFailedLoginAttempts(0);
        user.setLastLoginDate(new Timestamp(new Date().getTime()));
        user.setToken(null);
        Session session = application.getSession("portofino");
        Transaction tx = session.getTransaction();
        try {
            User existingUser = application.findUserByUserName(user.getUserName());
            if(existingUser != null) {
                logger.debug("Updating existing user {} (userId: {})",
                        existingUser.getUserName(), existingUser.getUserId());
                user.setUserId(existingUser.getUserId());
                session.merge(SecurityLogic.USER_ENTITY_NAME, user);
            } else {
                user.setUserId(RandomUtil.createRandomId(20));
                logger.debug("Importing user {} (userId: {})",
                        user.getUserName(), user.getUserId());
                session.save(SecurityLogic.USER_ENTITY_NAME, user);
            }
            session.flush();
            tx.commit();
        } catch (RuntimeException e) {
            //Session will be closed by the filter
            throw e;
        }
    }

    public Resolution logout() {
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }

        Locale locale = context.getLocale();
        ResourceBundle bundle = application.getBundle(locale);
        String msg = bundle.getString("user.logout");
        SessionMessages.addInfoMessage(msg);
        SessionMessages.addWarningMessage(msg);
        SessionMessages.addErrorMessage(msg);

        return new RedirectResolution("/");
    }

    // do not expose this method publicly
    protected HttpSession getSession() {
        return context.getRequest().getSession(false);
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelReturnUrl() {
        return cancelReturnUrl;
    }

    public void setCancelReturnUrl(String cancelReturnUrl) {
        this.cancelReturnUrl = cancelReturnUrl;
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************


    public Application getApplication() {
        return application;
    }

    public Configuration getPortofinoConfiguration() {
        return portofinoConfiguration;
    }
}
