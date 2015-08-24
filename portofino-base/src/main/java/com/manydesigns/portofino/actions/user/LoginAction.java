/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.shiro.*;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import com.manydesigns.portofino.stripes.AuthenticationRequiredResolution;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
            "Copyright (c) 2005-2015, ManyDesigns srl";

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
    public boolean rememberMe;

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

    private static Cage cage;
    public static final String CAPTCHA_SESSION_ATTRIBUTE = "LoginAction.captcha";
    public boolean captchaValidationFailed;

    public static final Logger logger =
            LoggerFactory.getLogger(LoginAction.class);

    static {
        try {
            cage = new CaptchaGenerator();
        } catch (NoClassDefFoundError e) {
            cage = null;
            logger.warn("Captcha generator not available", e);
        }
    }

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
        return authenticate();
    }

    @Button(list = "login-buttons", key = "login", order = 1, type = Button.TYPE_PRIMARY)
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
            usernamePasswordToken.setRememberMe(rememberMe);
            subject.login(usernamePasswordToken);
            logger.info("User {} login", ShiroUtils.getUserId(subject));
            String successMsg = ElementsThreadLocals.getText("user._.logged.in.successfully", userName);
            SessionMessages.addInfoMessage(successMsg);
            return redirectToReturnUrl();
        } catch (DisabledAccountException e) {
            String errMsg = ElementsThreadLocals.getText("user._.is.not.active", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn("Login failed for '" + userName + "': " + e.getMessage());
        } catch (IncorrectCredentialsException e) {
            String errMsg = ElementsThreadLocals.getText("login.failed.for.user._", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn("Login failed for '" + userName + "': " + e.getMessage());
        } catch (UnknownAccountException e) {
            String errMsg = ElementsThreadLocals.getText("login.failed.for.user._", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn("Login failed for '" + userName + "': " + e.getMessage());
        } catch (AuthenticationException e) {
            String errMsg = ElementsThreadLocals.getText("login.failed.for.user._", userName);
            SessionMessages.addErrorMessage(errMsg);
            logger.warn("Login failed for '" + userName + "': " + e.getMessage(), e);
        }
        return authenticate();
    }

    public Resolution authenticate() {
        Subject subject = SecurityUtils.getSubject();
        context.getResponse().setStatus(401);
        context.getResponse().setHeader(
                AuthenticationRequiredResolution.LOGIN_PAGE_HEADER,
                context.getRequest().getRequestURL().toString());
        if(subject.isRemembered()) {
            Serializable principal = (Serializable) ShiroUtils.getPrimaryPrincipal(subject);
            userName = getRememberedUserName(principal);
            rememberMe = true;
            return new ForwardResolution(getAuthenticationPage());
        } else {
            return new ForwardResolution(getLoginPage());
        }
    }

    @POST
    @Produces("application/json")
    public String login(@FormParam("username") String username, @FormParam("password") String password)
            throws AuthenticationException{
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isAuthenticated()) try {
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            usernamePasswordToken.setRememberMe(false);
            subject.login(usernamePasswordToken);
            logger.info("User {} login", ShiroUtils.getUserId(subject));
            Session session = subject.getSession(true);
            JSONStringer stringer = new JSONStringer();
            stringer.object().key("portofinoSessionId").value(session.getId()).endObject();
            return stringer.toString();
        } catch (AuthenticationException e) {
            logger.warn("Login failed for '" + username + "': " + e.getMessage(), e);
        }
        return "{}";
    }

    protected String getLoginPage() {
        return "/m/base/actions/user/login.jsp";
    }

    protected String getAuthenticationPage() {
        return "/m/base/actions/user/authenticate.jsp";
    }

    protected String getRememberedUserName(Serializable principal) {
        PortofinoRealm realm = ShiroUtils.getPortofinoRealm();
        return realm.getUserPrettyName(principal);
    }

    //**************************************************************************
    // Cancel
    //**************************************************************************

    @Button(list = "login-buttons", key = "cancel", order = 2)
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

    @Path("{sessionId}")
    @DELETE
    public void logout(@PathParam("sessionId") String sessionId) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession(false);
        if(session != null && session.getId().equals(sessionId)) {
            subject.logout();
            session.stop();
            logger.info("User logout");
        } else {
            logger.debug("Unnecessary call to logout");
        }
    }

    public Resolution logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        HttpSession session = context.getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String msg = ElementsThreadLocals.getText("user.disconnected");
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
                        PortofinoProperties.MAIL_FROM, "example@example.com");
                sendForgotPasswordEmail(
                        from, email, ElementsThreadLocals.getText("password.reset.confirmation.required"), body);
            }

            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("check.your.mailbox.and.follow.the.instructions"));
        } catch (Exception e) {
            logger.error("Error during password reset", e);
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("password.reset.failed"));
        }
        return new RedirectResolution(context.getActionPath());
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

        if (!ObjectUtils.equals(newPassword, confirmNewPassword)) {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("passwords.dont.match"));
            return new ForwardResolution("/m/base/actions/user/resetPassword.jsp");
        }

        List<String> errorMessages = new ArrayList<String>();
        if (!checkPasswordStrength(newPassword, errorMessages)) {
            for (String current : errorMessages) {
                SessionMessages.addErrorMessage(current);
            }
            return new ForwardResolution("/m/base/actions/user/resetPassword.jsp");
        }

        PasswordResetToken token = new PasswordResetToken(this.token, newPassword);
        try {
            subject.login(token);
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("password.successfully.reset"));
            return redirectToReturnUrl();
        } catch (AuthenticationException e) {
            String errMsg = ElementsThreadLocals.getText("the.password.reset.link.is.no.longer.active");
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            return authenticate();
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
        if (subject.getPrincipal() != null) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        setupSignUpForm(ShiroUtils.getPortofinoRealm());
        return getSignUpView();
    }

    public Resolution signUp2() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.getPrincipal() != null) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        setupSignUpForm(portofinoRealm);
        signUpForm.readFromRequest(context.getRequest());
        if (!signUpForm.validate() || !validateCaptcha()) {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("please.correct.the.errors.before.proceeding"));
            return getSignUpView();
        }

        List<String> errorMessages = new ArrayList<String>();
        if (!validateSignUpPassword(errorMessages)) {
            for (String current : errorMessages) {
                SessionMessages.addErrorMessage(current);
            }
            return getSignUpView();
        }

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
                    PortofinoProperties.MAIL_FROM, "example@example.com");
            sendSignupConfirmationEmail(
                    from, email, ElementsThreadLocals.getText("confirm.signup"), body);
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("check.your.mailbox.and.follow.the.instructions"));
            return authenticate();
        } catch (ExistingUserException e) {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("a.user.with.the.same.username.already.exists"));
            return getSignUpView();
        } catch (Exception e) {
            logger.error("Error during sign-up", e);
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("signup.failed"));
            return getSignUpView();
        }
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
        if (subject.getPrincipal() != null) {
            logger.debug("Already logged in");
            return redirectToReturnUrl();
        }

        SignUpToken token = new SignUpToken(this.token);
        try {
            subject.login(token);
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("user.created"));
            return redirectToReturnUrl();
        } catch (AuthenticationException e) {
            String errMsg = ElementsThreadLocals.getText("the.sign.up.confirmation.link.is.no.longer.active");
            SessionMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            return signUp();
        }
    }

    protected Resolution getSignUpView() {
        return new ForwardResolution("/m/base/actions/user/signUp.jsp");
    }

    protected void setupSignUpForm(PortofinoRealm realm) {
        FormBuilder formBuilder = new FormBuilder(realm.getSelfRegisteredUserClassAccessor())
                .configMode(Mode.CREATE)
                .configReflectiveFields();
        signUpForm = formBuilder.build();
    }

    protected boolean validateSignUpPassword(List<String> errorMessages) {
        return true;
    }

    //**************************************************************************
    // Change password
    //**************************************************************************

    @RequiresUser
    public Resolution changePassword() throws Exception {
        return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
    }

    @Button(list = "changepassword", key = "ok", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresUser
    public Resolution changePassword2() throws Exception {
        Subject subject = SecurityUtils.getSubject();

        Serializable principal = (Serializable) subject.getPrincipal();
        if (!ObjectUtils.equals(newPassword, confirmNewPassword)) {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("passwords.dont.match"));
            return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
        }

        List<String> errorMessages = new ArrayList<String>();
        if (!checkPasswordStrength(newPassword, errorMessages)) {
            for (String current : errorMessages) {
                SessionMessages.addErrorMessage(current);
            }
            return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
        }

        PortofinoRealm portofinoRealm =
                ShiroUtils.getPortofinoRealm();
        try {
            portofinoRealm.changePassword(principal, pwd, newPassword);
            if(subject.isRemembered()) {
                UsernamePasswordToken usernamePasswordToken =
                        new UsernamePasswordToken(getRememberedUserName(principal), newPassword);
                usernamePasswordToken.setRememberMe(true);
                try {
                    subject.login(usernamePasswordToken);
                } catch (Exception e) {
                    logger.warn(
                            "User {} changed password but could not be subsequently authenticated",
                            portofinoRealm.getUserId(principal));
                }
            }
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("password.changed.successfully"));
        } catch (IncorrectCredentialsException e) {
            logger.warn("User {} password change: Incorrect credentials", portofinoRealm.getUserId(principal));
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("wrong.password"));
            return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
        } catch (Exception e) {
            logger.error("Password update failed for user " + portofinoRealm.getUserId(principal), e);
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("password.change.failed"));
            return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
        }
        return redirectToReturnUrl();
    }

    //**************************************************************************
    // Utility methods
    //**************************************************************************

    protected boolean checkPasswordStrength(String password, List<String> errorMessages) {
        if (password == null) {
            errorMessages.add(ElementsThreadLocals.getText("null.password"));
            return false;
        }
        if (password.length() < 8) {
            errorMessages.add(ElementsThreadLocals.getText("password.too.short", 8));
            return false;
        }
        if (StringUtils.isAlpha(password)) {
            errorMessages.add(ElementsThreadLocals.getText("password.only.letters"));
            return false;
        }
        return true;
    }

    protected Resolution redirectToReturnUrl() {
        return redirectToReturnUrl(returnUrl);
    }

    protected Resolution redirectToReturnUrl(String returnUrl) {
        boolean prependContext = true;
        if (StringUtils.isEmpty(returnUrl)) {
            returnUrl = "/";
        } else try {
            URL url = new URL(returnUrl);
            if (!isValidReturnUrl(url)) {
                logger.warn("Forbidding suspicious return URL: " + url);
                return new RedirectResolution("/");
            }
            prependContext = false;
        } catch (MalformedURLException e) {
            //Ok, if it is not a full URL there's no risk of XSS attacks with returnUrl=http://www.evil.com/hack
        }
        logger.debug("Redirecting to: {}", returnUrl);
        return new RedirectResolution(returnUrl, prependContext);
    }

    protected boolean isValidReturnUrl(URL url) {
        return context.getRequest().getServerName().equals(url.getHost());
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

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public boolean isCaptchaValidationFailed() {
        return captchaValidationFailed;
    }
}
