/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.rest.actions.user;

import com.github.cage.Cage;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.shiro.PortofinoRealm;
import com.manydesigns.portofino.shiro.ShiroUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import java.io.Serializable;
import java.util.List;

/**
 * Action that handles the standard Portofino login form.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class LoginAction extends AbstractResourceAction {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

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

    protected String getRememberedUserName(Serializable principal) {
        PortofinoRealm realm = ShiroUtils.getPortofinoRealm();
        return realm.getUserPrettyName(principal);
    }

    //**************************************************************************
    // Logout
    //**************************************************************************

    @Path("{sessionId}")
    @DELETE
    @Deprecated
    public void logout(@PathParam("sessionId") String sessionId) {
        logout();
    }

    @DELETE
    public void logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        logger.info("User logout");
    }

    //**************************************************************************
    // Forgot password
    //**************************************************************************

//    public Resolution forgotPassword() {
//        Subject subject = SecurityUtils.getSubject();
//        if (subject.isAuthenticated()) {
//            logger.debug("Already logged in");
//            return redirectToReturnUrl();
//        }
//
//        return new ForwardResolution(getForgotPasswordPage());
//    }
//
//    public Resolution forgotPassword2() {
//        Subject subject = SecurityUtils.getSubject();
//        if (subject.isAuthenticated()) {
//            logger.debug("Already logged in");
//            return redirectToReturnUrl();
//        }
//
//        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
//        try {
//            Serializable user = portofinoRealm.getUserByEmail(email);
//            if(user != null) {
//                String token = portofinoRealm.generateOneTimeToken(user);
//                HttpServletRequest req = context.getRequest();
//                String url = req.getRequestURL().toString();
//                UrlBuilder urlBuilder = new UrlBuilder(Locale.getDefault(), url, true);
//                urlBuilder.setEvent("resetPassword");
//                urlBuilder.addParameter("token", token);
//
//                String siteUrl = ServletUtils.getApplicationBaseUrl(req);
//                String changePasswordLink = urlBuilder.toString();
//
//                String body = getResetPasswordEmailBody(siteUrl, changePasswordLink);
//
//                String from = portofinoConfiguration.getString(
//                        PortofinoProperties.MAIL_FROM, "example@example.com");
//                sendForgotPasswordEmail(
//                        from, email, ElementsThreadLocals.getText("password.reset.confirmation.required"), body);
//            } else {
//                logger.warn("Forgot password request for nonexistent email");
//            }
//
//            //This is by design, for better security. Always give the successful message even if no mail was sent.
//            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("check.your.mailbox.and.follow.the.instructions"));
//        } catch (Exception e) {
//            logger.error("Error during password reset", e);
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("password.reset.failed"));
//        }
//        return new RedirectResolution(context.getActionPath());
//    }
//
//    protected String getResetPasswordEmailBody(String siteUrl, String changePasswordLink) throws IOException {
//        String countryIso = context.getLocale().getLanguage().toLowerCase();
//        InputStream is = LoginAction.class.getResourceAsStream("passwordResetEmail." + countryIso + ".html");
//        if(is == null) {
//            is = LoginAction.class.getResourceAsStream("passwordResetEmail.en.html");
//        }
//        String template = IOUtils.toString(is);
//        IOUtils.closeQuietly(is);
//        String body = template.replace("$link", changePasswordLink).replace("$site", siteUrl);
//        return body;
//    }
//
//    public Resolution resetPassword() {
//        Subject subject = SecurityUtils.getSubject();
//        if (subject.isAuthenticated()) {
//            logger.debug("Already logged in");
//            return redirectToReturnUrl();
//        }
//
//        return new ForwardResolution("/m/base/actions/user/resetPassword.jsp");
//    }
//
//    public Resolution resetPassword2() {
//        Subject subject = SecurityUtils.getSubject();
//        if (subject.isAuthenticated()) {
//            logger.debug("Already logged in");
//            return redirectToReturnUrl();
//        }
//
//        if (!ObjectUtils.equals(newPassword, confirmNewPassword)) {
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("passwords.dont.match"));
//            return new ForwardResolution("/m/base/actions/user/resetPassword.jsp");
//        }
//
//        List<String> errorMessages = new ArrayList<String>();
//        if (!checkPasswordStrength(newPassword, errorMessages)) {
//            for (String current : errorMessages) {
//                SessionMessages.addErrorMessage(current);
//            }
//            return new ForwardResolution("/m/base/actions/user/resetPassword.jsp");
//        }
//
//        PasswordResetToken token = new PasswordResetToken(this.token, newPassword);
//        try {
//            subject.login(token);
//            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("password.successfully.reset"));
//            return redirectToReturnUrl();
//        } catch (AuthenticationException e) {
//            String errMsg = ElementsThreadLocals.getText("the.password.reset.link.is.no.longer.active");
//            SessionMessages.addErrorMessage(errMsg);
//            logger.warn(errMsg, e);
//            return authenticate();
//        }
//    }
//
    protected abstract void sendForgotPasswordEmail(String from, String to, String subject, String body);

    //**************************************************************************
    // Sign up
    //**************************************************************************

//    public Resolution captcha() {
//        final String token = cage.getTokenGenerator().next();
//        context.getRequest().getSession().setAttribute(CAPTCHA_SESSION_ATTRIBUTE, token);
//        if(token != null) {
//            return new StreamingResolution("image/" + cage.getFormat()) {
//
//                @Override
//                protected void applyHeaders(HttpServletResponse response) {
//                    super.applyHeaders(response);
//                    response.setHeader("Cache-Control", "no-cache, no-store");
//                    response.setHeader("Pragma", "no-cache");
//                    long time = System.currentTimeMillis();
//                    response.setDateHeader("Last-Modified", time);
//                    response.setDateHeader("Date", time);
//                    response.setDateHeader("Expires", time);
//                }
//
//                @Override
//                protected void stream(HttpServletResponse response) throws Exception {
//                    cage.draw(token, response.getOutputStream());
//                }
//            };
//        } else {
//            return new ErrorResolution(404);
//        }
//    }
//
//    public Resolution signUp() {
//        Subject subject = SecurityUtils.getSubject();
//        if (subject.getPrincipal() != null) {
//            logger.debug("Already logged in");
//            return redirectToReturnUrl();
//        }
//
//        setupSignUpForm(ShiroUtils.getPortofinoRealm());
//        return getSignUpView();
//    }
//
//    public Resolution signUp2() {
//        Subject subject = SecurityUtils.getSubject();
//        if (subject.getPrincipal() != null) {
//            logger.debug("Already logged in");
//            return redirectToReturnUrl();
//        }
//
//        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
//        setupSignUpForm(portofinoRealm);
//        signUpForm.readFromRequest(context.getRequest());
//        if (!signUpForm.validate() || !validateCaptcha()) {
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("please.correct.the.errors.before.proceeding"));
//            return getSignUpView();
//        }
//
//        List<String> errorMessages = new ArrayList<String>();
//        if (!validateSignUpPassword(errorMessages)) {
//            for (String current : errorMessages) {
//                SessionMessages.addErrorMessage(current);
//            }
//            return getSignUpView();
//        }
//
//        try {
//            Object user = portofinoRealm.getSelfRegisteredUserClassAccessor().newInstance();
//            signUpForm.writeToObject(user);
//            String token = portofinoRealm.saveSelfRegisteredUser(user);
//
//            HttpServletRequest req = context.getRequest();
//            String url = req.getRequestURL().toString();
//            UrlBuilder urlBuilder = new UrlBuilder(Locale.getDefault(), url, true);
//            urlBuilder.setEvent("confirmSignUp");
//            urlBuilder.addParameter("token", token);
//
//            String siteUrl = ServletUtils.getApplicationBaseUrl(req);
//            String changePasswordLink = urlBuilder.toString();
//
//            String body = getConfirmSignUpEmailBody(siteUrl, changePasswordLink);
//
//            String from = portofinoConfiguration.getString(
//                    PortofinoProperties.MAIL_FROM, "example@example.com");
//            //TODO this line below assumes that the email property of the user is called 'email' but it should be
//            //extracted dynamically from the user object
//            sendSignupConfirmationEmail(
//                    from, email, ElementsThreadLocals.getText("confirm.signup"), body);
//            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("check.your.mailbox.and.follow.the.instructions"));
//            return authenticate();
//        } catch (ExistingUserException e) {
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("a.user.with.the.same.username.already.exists"));
//            return getSignUpView();
//        } catch (Exception e) {
//            logger.error("Error during sign-up", e);
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("signup.failed"));
//            return getSignUpView();
//        }
//    }
//
//    protected boolean validateCaptcha() {
//        HttpServletRequest request = context.getRequest();
//        HttpSession session = request.getSession();
//        boolean valid = StringUtils.equalsIgnoreCase(
//                request.getParameter("captchaText"),
//                (String) session.getAttribute(CAPTCHA_SESSION_ATTRIBUTE));
//        session.removeAttribute(CAPTCHA_SESSION_ATTRIBUTE);
//        captchaValidationFailed = !valid;
//        return valid;
//    }
//
//    protected String getConfirmSignUpEmailBody(String siteUrl, String confirmSignUpLink) throws IOException {
//        String countryIso = context.getLocale().getCountry().toLowerCase();
//        InputStream is = LoginAction.class.getResourceAsStream("confirmSignUpEmail." + countryIso + ".html");
//        if(is == null) {
//            is = LoginAction.class.getResourceAsStream("confirmSignUpEmail.en.html");
//        }
//        String template = IOUtils.toString(is);
//        IOUtils.closeQuietly(is);
//        String body = template.replace("$link", confirmSignUpLink).replace("$site", siteUrl);
//        return body;
//    }
//
    protected abstract void sendSignupConfirmationEmail(String from, String to, String subject, String body);

//    public Resolution confirmSignUp() {
//        Subject subject = SecurityUtils.getSubject();
//        if (subject.getPrincipal() != null) {
//            logger.debug("Already logged in");
//            return redirectToReturnUrl();
//        }
//
//        SignUpToken token = new SignUpToken(this.token);
//        try {
//            subject.login(token);
//            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("user.created"));
//            return redirectToReturnUrl();
//        } catch (AuthenticationException e) {
//            String errMsg = ElementsThreadLocals.getText("the.sign.up.confirmation.link.is.no.longer.active");
//            SessionMessages.addErrorMessage(errMsg);
//            logger.warn(errMsg, e);
//            return signUp();
//        }
//    }
//
//    protected Resolution getSignUpView() {
//        return new ForwardResolution("/m/base/actions/user/signUp.jsp");
//    }
//
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

//    @RequiresUser
//    public Resolution changePassword() throws Exception {
//        return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
//    }
//
//    @Button(list = "changepassword", key = "ok", order = 1, type = Button.TYPE_PRIMARY)
//    @RequiresUser
//    public Resolution changePassword2() throws Exception {
//        Subject subject = SecurityUtils.getSubject();
//
//        Serializable principal = (Serializable) subject.getPrincipal();
//        if (!ObjectUtils.equals(newPassword, confirmNewPassword)) {
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("passwords.dont.match"));
//            return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
//        }
//
//        List<String> errorMessages = new ArrayList<String>();
//        if (!checkPasswordStrength(newPassword, errorMessages)) {
//            for (String current : errorMessages) {
//                SessionMessages.addErrorMessage(current);
//            }
//            return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
//        }
//
//        PortofinoRealm portofinoRealm =
//                ShiroUtils.getPortofinoRealm();
//        try {
//            portofinoRealm.changePassword(principal, pwd, newPassword);
//            if(subject.isRemembered()) {
//                UsernamePasswordToken usernamePasswordToken =
//                        new UsernamePasswordToken(getRememberedUserName(principal), newPassword);
//                usernamePasswordToken.setRememberMe(true);
//                try {
//                    subject.login(usernamePasswordToken);
//                } catch (Exception e) {
//                    logger.warn(
//                            "User {} changed password but could not be subsequently authenticated",
//                            portofinoRealm.getUserId(principal));
//                }
//            }
//            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("password.changed.successfully"));
//        } catch (IncorrectCredentialsException e) {
//            logger.warn("User {} password change: Incorrect credentials", portofinoRealm.getUserId(principal));
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("wrong.password"));
//            return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
//        } catch (Exception e) {
//            logger.error("Password update failed for user " + portofinoRealm.getUserId(principal), e);
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("password.change.failed"));
//            return new ForwardResolution("/m/base/actions/user/changePassword.jsp");
//        }
//        return redirectToReturnUrl();
//    }
//
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
//
//    protected Resolution redirectToReturnUrl() {
//        return redirectToReturnUrl(returnUrl);
//    }
//
//    protected Resolution redirectToReturnUrl(String returnUrl) {
//        boolean prependContext = true;
//        if (StringUtils.isEmpty(returnUrl)) {
//            returnUrl = "/";
//        } else try {
//            URL url = new URL(returnUrl);
//            if (!isValidReturnUrl(url)) {
//                logger.warn("Forbidding suspicious return URL: " + url);
//                return new RedirectResolution("/");
//            }
//            prependContext = false;
//        } catch (MalformedURLException e) {
//            //Ok, if it is not a full URL there's no risk of XSS attacks with returnUrl=http://www.evil.com/hack
//        }
//        logger.debug("Redirecting to: {}", returnUrl);
//        return new RedirectResolution(returnUrl, prependContext);
//    }
//
//    protected boolean isValidReturnUrl(URL url) {
//        return context.getRequest().getServerName().equals(url.getHost());
//    }

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

    public boolean isCaptchaValidationFailed() {
        return captchaValidationFailed;
    }
}
