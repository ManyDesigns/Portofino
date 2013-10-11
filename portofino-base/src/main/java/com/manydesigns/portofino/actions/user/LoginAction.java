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

import com.github.cage.Cage;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.portofino.BaseProperties;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.shiro.*;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

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

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
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

    //**************************************************************************
    // Captcha
    //**************************************************************************

    private static final Cage cage = new CaptchaGenerator();
    public static final String CAPTCHA_SESSION_ATTRIBUTE = "LoginAction.captcha";
    public boolean captchaValidationFailed;

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
            logger.info("User {} login", ShiroUtils.getUserId(subject));
            String successMsg = ElementsThreadLocals.getText("user.login.success", userName);
            SessionMessages.addInfoMessage(successMsg);
            return redirectToReturnUrl();
        } catch (DisabledAccountException e) {
            String errMsg = ElementsThreadLocals.getText("user.not.active", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn("Login failed for '" + userName + "': " + e.getMessage());
        } catch (IncorrectCredentialsException e) {
            String errMsg = ElementsThreadLocals.getText("user.login.failed", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn("Login failed for '" + userName + "': " + e.getMessage());
        } catch (UnknownAccountException e) {
            String errMsg = ElementsThreadLocals.getText("user.login.failed", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn("Login failed for '" + userName + "': " + e.getMessage());
        } catch (AuthenticationException e) {
            String errMsg = ElementsThreadLocals.getText("user.login.failed", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn("Login failed for '" + userName + "': " + e.getMessage(), e);
        }
        return new ForwardResolution(getLoginPage());
    }

    protected String getLoginPage() {
        return "/m/base/actions/user/login.jsp";
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
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        HttpSession session = context.getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String msg = ElementsThreadLocals.getText("user.logout");
        SessionMessages.addInfoMessage(msg);
        logger.info("User logout");

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
        try {
            Serializable user = portofinoRealm.getUserByEmail(email);
            if(user != null) {
                String token = portofinoRealm.generateOneTimeToken(user);
                HttpServletRequest req = context.getRequest();
                String url = req.getRequestURL().toString();
                UrlBuilder urlBuilder = new UrlBuilder(Locale.getDefault(), url, true);
                urlBuilder.setEvent("resetPassword");
                urlBuilder.addParameter("token", token);

                String siteUrl = ServletUtils.getApplicationBaseUrl(req);
                String changePasswordLink = urlBuilder.toString();

                String body = getResetPasswordEmailBody(siteUrl, changePasswordLink);

                String from = portofinoConfiguration.getString(
                        BaseProperties.MAIL_FROM, "example@example.com");
                sendForgotPasswordEmail(
                        from, email, ElementsThreadLocals.getText("user.passwordReset.emailSubject"), body);
            }

            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("user.passwordReset.email.sent"));
        } catch (Exception e) {
            logger.error("Error during password reset", e);
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("user.passwordReset.failure"));
        }
        return new RedirectResolution(context.getActualServletPath());
    }

    protected String getResetPasswordEmailBody(String siteUrl, String changePasswordLink) throws IOException {
        String countryIso = context.getLocale().getCountry().toLowerCase();
        InputStream is = LoginAction.class.getResourceAsStream("passwordResetEmail." + countryIso + ".html");
        if(is == null) {
            is = LoginAction.class.getResourceAsStream("passwordResetEmail.en.html");
        }
        String template = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        String body = template.replace("$link", changePasswordLink).replace("$site", siteUrl);
        return body;
    }

    public Resolution resetPassword() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        return new ForwardResolution("/m/base/actions/user/resetPassword.jsp");
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
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("user.passwordReset.failure.passwordsDontMatch"));
            return resetPassword();
        }
    }

    protected abstract void sendForgotPasswordEmail(String from, String to, String subject, String body);

    protected String getForgotPasswordPage() {
        return "/m/base/actions/user/forgotPassword.jsp";
    }

    //**************************************************************************
    // Sign up
    //**************************************************************************

    public Resolution captcha() {
        final String token = cage.getTokenGenerator().next();
        context.getRequest().getSession().setAttribute(CAPTCHA_SESSION_ATTRIBUTE, token);
        if(token != null) {
            return new StreamingResolution("image/" + cage.getFormat()) {

                @Override
                protected void applyHeaders(HttpServletResponse response) {
                    super.applyHeaders(response);
                    response.setHeader("Cache-Control", "no-cache, no-store");
                    response.setHeader("Pragma", "no-cache");
                    long time = System.currentTimeMillis();
                    response.setDateHeader("Last-Modified", time);
                    response.setDateHeader("Date", time);
                    response.setDateHeader("Expires", time);
                }

                @Override
                protected void stream(HttpServletResponse response) throws Exception {
                    cage.draw(token, response.getOutputStream());
                }
            };
        } else {
            return new ErrorResolution(404);
        }
    }

    public Resolution signUp() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        setupSignUpForm(ShiroUtils.getPortofinoRealm());
        return new ForwardResolution(getSignUpPage());
    }

    public Resolution signUp2() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        setupSignUpForm(portofinoRealm);
        signUpForm.readFromRequest(context.getRequest());
        if (signUpForm.validate() && validateCaptcha()) {
            try {
                Object user = portofinoRealm.getSelfRegisteredUserClassAccessor().newInstance();
                signUpForm.writeToObject(user);
                String token = portofinoRealm.saveSelfRegisteredUser(user);

                HttpServletRequest req = context.getRequest();
                String url = req.getRequestURL().toString();
                UrlBuilder urlBuilder = new UrlBuilder(Locale.getDefault(), url, true);
                urlBuilder.setEvent("confirmSignUp");
                urlBuilder.addParameter("token", token);

                String siteUrl = ServletUtils.getApplicationBaseUrl(req);
                String changePasswordLink = urlBuilder.toString();

                String body = getConfirmSignUpEmailBody(siteUrl, changePasswordLink);

                String from = portofinoConfiguration.getString(
                        BaseProperties.MAIL_FROM, "example@example.com");
                sendSignupConfirmationEmail(
                        from, email, ElementsThreadLocals.getText("user.signUp.email.subject"), body);
                SessionMessages.addInfoMessage(ElementsThreadLocals.getText("user.signUp.email.sent"));
                return new ForwardResolution(getLoginPage());
            } catch (ExistingUserException e) {
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("user.signUp.failure.userExists"));
            } catch (Exception e) {
                logger.error("Error during sign-up", e);
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("user.signUp.failure"));
            }
        } else {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("user.signUp.failure.formErrors"));
        }
        return new ForwardResolution(getSignUpPage());
    }

    protected boolean validateCaptcha() {
        HttpServletRequest request = context.getRequest();
        HttpSession session = request.getSession();
        boolean valid = StringUtils.equalsIgnoreCase(
                request.getParameter("captchaText"),
                (String) session.getAttribute(CAPTCHA_SESSION_ATTRIBUTE));
        session.removeAttribute(CAPTCHA_SESSION_ATTRIBUTE);
        captchaValidationFailed = !valid;
        return valid;
    }

    protected String getConfirmSignUpEmailBody(String siteUrl, String confirmSignUpLink) throws IOException {
        String countryIso = context.getLocale().getCountry().toLowerCase();
        InputStream is = LoginAction.class.getResourceAsStream("confirmSignUpEmail." + countryIso + ".html");
        if(is == null) {
            is = LoginAction.class.getResourceAsStream("confirmSignUpEmail.en.html");
        }
        String template = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        String body = template.replace("$link", confirmSignUpLink).replace("$site", siteUrl);
        return body;
    }

    protected abstract void sendSignupConfirmationEmail(String from, String to, String subject, String body);

    public Resolution confirmSignUp() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        SignUpToken token = new SignUpToken(this.token);
        try {
            subject.login(token);
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("user.signUp.success"));
            return redirectToReturnUrl();
        } catch (AuthenticationException e) {
            String errMsg = ElementsThreadLocals.getText("user.signUp.invalidToken");
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            return signUp();
        }
    }

    protected String getSignUpPage() {
        return "/m/base/actions/user/signUp.jsp";
    }

    protected void setupSignUpForm(PortofinoRealm realm) {
        FormBuilder formBuilder = new FormBuilder(realm.getSelfRegisteredUserClassAccessor())
                .configMode(Mode.CREATE)
                .configReflectiveFields();
        signUpForm = formBuilder.build();
    }

    //**************************************************************************
    // Change password
    //**************************************************************************

    @RequiresAuthentication
    public Resolution changePassword() throws Exception {
        return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
    }

    @Button(list = "changepassword", key = "commons.ok", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresAuthentication
    public Resolution changePassword2() throws Exception {
        Subject subject = SecurityUtils.getSubject();

        Serializable principal = (Serializable) subject.getPrincipal();
        if (ObjectUtils.equals(newPassword, confirmNewPassword)) {
            PortofinoRealm portofinoRealm =
                    ShiroUtils.getPortofinoRealm();
            try {
                portofinoRealm.changePassword(principal, pwd, newPassword);
                SessionMessages.addInfoMessage(ElementsThreadLocals.getText("user.passwordChange.success"));
            } catch (IncorrectCredentialsException e) {
                logger.error("Password update failed", e);
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("user.passwordChange.failure.passwordDoesntMatch"));
                return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
            } catch (Exception e) {
                logger.error("Password update failed", e);
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("user.passwordChange.failure"));
                return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
            }
            return redirectToReturnUrl();
        } else {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("user.passwordChange.failure.passwordsDontMatch"));
            return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
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

    public boolean isCaptchaValidationFailed() {
        return captchaValidationFailed;
    }
}
