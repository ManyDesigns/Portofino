/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.actions.user;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.shiro.PasswordResetToken;
import com.manydesigns.portofino.shiro.PortofinoRealm;
import com.manydesigns.portofino.shiro.ShiroUtils;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Action that handles the standard Portofino login form.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class LoginAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    //**************************************************************************
    // Request parameters
    //**************************************************************************

    public String userName;
    public String pwd;

    public String email;

    public String newPassword;
    public String confirmNewPassword;

    public String token;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    protected Form signUpForm;
    public String returnUrl;
    public String cancelReturnUrl;

    public static final Logger logger =
            LoggerFactory.getLogger(LoginAction.class);

    public LoginAction() {}

    //**************************************************************************
    // Login
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        return new ForwardResolution(getLoginPage());
    }

    @Button(list = "login-buttons", key = "commons.login", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution login() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        userName = StringUtils.defaultString(userName);
        try {
            UsernamePasswordToken usernamePasswordToken =
                    new UsernamePasswordToken(userName, pwd);
            usernamePasswordToken.setRememberMe(false);
            subject.login(usernamePasswordToken);
            logger.info("User {} login", userName);
            String successMsg = ElementsThreadLocals.getText("user.login.success", userName);
            SessionMessages.addInfoMessage(successMsg);
            return redirectToReturnUrl();
        } catch (DisabledAccountException e) {
            String errMsg = ElementsThreadLocals.getText("user.not.active", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            return new ForwardResolution(getLoginPage());
        } catch (AuthenticationException e) {
            String errMsg = ElementsThreadLocals.getText("user.login.failed", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            return new ForwardResolution(getLoginPage());
        }
    }

    protected String getLoginPage() {
        return "/portofino-base/layouts/user/login.jsp";
    }


    //**************************************************************************
    // Cancel
    //**************************************************************************

    @Button(list = "login-buttons", key = "commons.cancel", order = 2)
    public Resolution cancel() {
        String url = "/";
        if(!StringUtils.isBlank(cancelReturnUrl)) {
            url = cancelReturnUrl;
        } else if(!StringUtils.isBlank(returnUrl)) {
            url = returnUrl;
        }
        return redirectToReturnUrl(url);
    }

    //**************************************************************************
    // Logout
    //**************************************************************************

    public Resolution logout() {
        String userName = "user"; //TODO ShiroUtils.getPrimaryPrincipal(SecurityUtils.getSubject()) + "";
        SecurityUtils.getSubject().logout();
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }

        String msg = ElementsThreadLocals.getText("user.logout");
        SessionMessages.addInfoMessage(msg);
        logger.info("User {} logout", userName);

        return new RedirectResolution("/");
    }

    //**************************************************************************
    // Forgot password
    //**************************************************************************

    public Resolution forgotPassword() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        return new ForwardResolution(getForgotPasswordPage());
    }

    public Resolution forgotPassword2() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        Serializable user = portofinoRealm.getUserByEmail(email);
        String token = portofinoRealm.generateOneTimeToken(user);
        sendForgotPasswordEmail(email, token);

        SessionMessages.addInfoMessage("Check your mailbox and follow the instructions");
        return new RedirectResolution(getOriginalPath());
    }

    public Resolution resetPassword() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        return new ForwardResolution("/portofino-base/layouts/user/resetPassword.jsp");
    }

    public Resolution resetPassword2() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        if (ObjectUtils.equals(newPassword, confirmNewPassword)) {
            PasswordResetToken token = new PasswordResetToken(this.token, newPassword);
            try {
                subject.login(token);
                SessionMessages.addInfoMessage(ElementsThreadLocals.getText("user.passwordReset.success"));
                return redirectToReturnUrl();
            } catch (AuthenticationException e) {
                String errMsg = ElementsThreadLocals.getText("user.passwordReset.invalidToken");
                SessionMessages.addErrorMessage(errMsg);
                logger.warn(errMsg, e);
                return new ForwardResolution(getLoginPage());
            }
        } else {
            SessionMessages.addErrorMessage("Passwords do not match");
            return resetPassword();
        }
    }

    protected abstract void sendForgotPasswordEmail(String email, String token);

    protected String getForgotPasswordPage() {
        return "/portofino-base/layouts/user/forgotPassword.jsp";
    }

    //**************************************************************************
    // Sign up
    //**************************************************************************

    public Resolution signUp() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        setupSignUpForm();
        return new ForwardResolution(getSignUpPage());
    }

    public Resolution signUp2() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        setupSignUpForm();
        signUpForm.readFromRequest(context.getRequest());
        if (signUpForm.validate()) {
            SessionMessages.addInfoMessage("Check your mailbox and follow the instructions");
            return new RedirectResolution(getOriginalPath());
        } else {
            SessionMessages.addErrorMessage("Correct the errors before proceding");
            return new ForwardResolution(getSignUpPage());
        }
    }

    protected String getSignUpPage() {
        return "/portofino-base/layouts/user/signUp.jsp";
    }

    protected void setupSignUpForm() {
        FormBuilder formBuilder = new FormBuilder(User.class)
                .configMode(Mode.CREATE)
                .configReflectiveFields();
        signUpForm = formBuilder.build();
    }

    //**************************************************************************
    // Change password
    //**************************************************************************

    public Resolution changePassword() throws Exception {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            throw new Exception("You must be logged in to change your password");
        }

        return new ForwardResolution("/portofino-base/layouts/user/changePassword.jsp");
    }

    @Button(list = "changepassword", key = "commons.ok", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution changePassword2() throws Exception {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            throw new Exception("You must be logged in to change your password");
        }

        Serializable principal = (Serializable) subject.getPrincipal();
        if (ObjectUtils.equals(newPassword, confirmNewPassword)) {
            PortofinoRealm portofinoRealm =
                    ShiroUtils.getPortofinoRealm();
            try {
                portofinoRealm.changePassword(principal, pwd, newPassword);
                SessionMessages.addInfoMessage("Password changed successfully");
            } catch (IncorrectCredentialsException e) {
                logger.error("Password update failed", e);
                SessionMessages.addErrorMessage("Your current password does not match.");
                return new ForwardResolution("/portofino-base/layouts/user/changePassword.jsp");
            } catch (Exception e) {
                logger.error("Password update failed", e);
                SessionMessages.addErrorMessage("Could not change password.");
                return new ForwardResolution("/portofino-base/layouts/user/changePassword.jsp");
            }
            return redirectToReturnUrl();

        } else {
            SessionMessages.addInfoMessage("New password fields do not match");
            return new ForwardResolution("/portofino-base/layouts/user/changePassword.jsp");
        }
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    protected Resolution redirectToReturnUrl() {
        return redirectToReturnUrl(returnUrl);
    }

    protected Resolution redirectToReturnUrl(String returnUrl) {
        if (StringUtils.isEmpty(returnUrl)) {
            returnUrl = "/";
        } else try {
            URL url = new URL(returnUrl);
            if(!context.getRequest().getServerName().equals(url.getHost())) {
                logger.warn("Forbidding suspicious return URL: " + returnUrl);
                return new RedirectResolution("/");
            }
        } catch (MalformedURLException e) {
            //Ok, if it is not a full URL there's no risk of XSS attacks with returnUrl=http://www.evil.com/hack
        }
        logger.debug("Redirecting to: {}", returnUrl);
        return new RedirectResolution(returnUrl);
    }

    // do not expose this method publicly
    protected HttpSession getSession() {
        return context.getRequest().getSession(false);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public abstract String getApplicationName();

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

    public Configuration getPortofinoConfiguration() {
        return portofinoConfiguration;
    }

    public Form getSignUpForm() {
        return signUpForm;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
