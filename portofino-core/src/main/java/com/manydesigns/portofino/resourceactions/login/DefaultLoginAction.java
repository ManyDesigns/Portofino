/*
* Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.resourceactions.login;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.queue.QueueException;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.resourceactions.ResourceActionName;
import com.manydesigns.portofino.resourceactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.security.SecurityLogic;
import com.manydesigns.portofino.shiro.*;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@ScriptTemplate("script_template.groovy")
@ResourceActionName("Login")
public class DefaultLoginAction extends AbstractResourceAction {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(DefaultLoginAction.class);

    @Autowired(required = false)
    public MailQueue mailQueue;

    @POST
    @Produces("application/json")
    public String login(@FormParam("username") String username, @FormParam("password") String password)
            throws AuthenticationException {
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isAuthenticated()) try {
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            usernamePasswordToken.setRememberMe(false);
            subject.login(usernamePasswordToken);
            logger.info("User {} logged in", ShiroUtils.getUserId(subject));
            Object principal = subject.getPrincipal();
            subject.logout();
            PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
            String jwt = portofinoRealm.generateWebToken(principal);
            subject.login(new JSONWebToken(jwt));
            return userInfo(subject, portofinoRealm, jwt);
        } catch (AuthenticationException e) {
            logger.warn("Login failed for '" + username + "': " + e.getMessage(), e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } else {
            return checkJWT();
        }
    }

    @Path(":renew-token")
    @POST
    public String renewToken() {
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated()) {
            Object principal = subject.getPrincipal();
            subject.logout();
            PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
            String token = portofinoRealm.generateWebToken(principal);
            subject.login(new JSONWebToken(token));
            return token;
        } else {
            logger.warn("Token renew request for unauthenticated user");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthentication
    public String checkJWT() {
        Subject subject = SecurityUtils.getSubject();
        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        String jwt = JWTFilter.getJSONWebToken(context.getRequest());
        if(jwt == null) {
            subject.logout();
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return userInfo(subject, portofinoRealm, jwt);
    }

    public String userInfo(Subject subject, PortofinoRealm portofinoRealm, String jwt) {
        boolean administrator = SecurityLogic.isAdministrator(portofinoConfiguration);
        JSONStringer stringer = new JSONStringer();
        stringer.
            object().
                key("userId").value(ShiroUtils.getUserId(subject)).
                key("displayName").value(portofinoRealm.getUserPrettyName((Serializable) subject.getPrincipal())).
                key("administrator").value(administrator).
                key("groups").value(portofinoRealm.getGroups(subject.getPrincipal())).
                key("jwt").value(jwt).
            endObject();
        return stringer.toString();
    }

    public static class ResetPasswordEmailRequest {
        public String email;
        public String siteNameOrAddress;
        public String loginPageUrl;
    }

    @Path(":send-reset-password-email")
    @POST
    public void sendResetPasswordEmail(ResetPasswordEmailRequest req) {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            logger.debug("Already logged in");
            return;
        }

        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        try {
            Serializable user = portofinoRealm.getUserByEmail(req.email);
            if(user != null) {
                String token = portofinoRealm.generateOneTimeToken(user);
                String body = getResetPasswordEmailBody(req.siteNameOrAddress, req.loginPageUrl.replace("TOKEN", token));
                String from = portofinoConfiguration.getString(PortofinoProperties.MAIL_FROM);
                String subject = ElementsThreadLocals.getText("password.reset.confirmation.required");
                sendMail(from, req.email, subject, body);
            } else {
                logger.warn("Forgot password request for nonexistent email");
            }
        } catch (Exception e) {
            logger.error("Error during password reset", e);
            throw new WebApplicationException(ElementsThreadLocals.getText("password.reset.failed"), e);
        }
    }

    protected String getResetPasswordEmailBody(String site, String changePasswordLink) throws IOException {
        String countryIso = context.getRequest().getLocale().getLanguage().toLowerCase();
        InputStream is = DefaultLoginAction.class.getResourceAsStream("/com/manydesigns/portofino/actions/user/passwordResetEmail." + countryIso + ".html");
        if(is == null) {
            is = DefaultLoginAction.class.getResourceAsStream("/com/manydesigns/portofino/actions/user/passwordResetEmail.en.html");
        }
        try(InputStream stream = is) {
            String template = IOUtils.toString(stream, StandardCharsets.UTF_8);
            return template.replace("$link", changePasswordLink).replace("$site", site);
        }
    }

    protected String getConfirmSignUpEmailBody(String site, String confirmSignUpLink) throws IOException {
        String countryIso = context.getRequest().getLocale().getLanguage().toLowerCase();
        InputStream is = DefaultLoginAction.class.getResourceAsStream("/com/manydesigns/portofino/actions/user/confirmSignUpEmail." + countryIso + ".html");
        if(is == null) {
            is = DefaultLoginAction.class.getResourceAsStream("/com/manydesigns/portofino/actions/user/confirmSignUpEmail.en.html");
        }
        try(InputStream stream = is) {
            String template = IOUtils.toString(stream, StandardCharsets.UTF_8);
            return template.replace("$link", confirmSignUpLink).replace("$site", site);
        }
    }

    public static class ResetPasswordRequest {
        public String token;
        public String newPassword;
    }

    @Path(":reset-password")
    @POST
    public void resetPassword(ResetPasswordRequest request) {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            logger.debug("Already logged in");
            return;
        }

        List<String> errorMessages = new ArrayList<String>();
        if (!checkPasswordStrength(request.newPassword, errorMessages)) {
            for (String current : errorMessages) {
                RequestMessages.addErrorMessage(current);
            }
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        PasswordResetToken token = new PasswordResetToken(request.token, request.newPassword);
        try {
            subject.login(token);
        } catch (AuthenticationException e) {
            String errMsg = ElementsThreadLocals.getText("the.password.reset.link.is.no.longer.active");
            throw new WebApplicationException(errMsg, e, Response.Status.UNAUTHORIZED);
        }
    }

    protected void sendMail(String from, String to, String subject, String body) {
        if(mailQueue == null) {
            throw new UnsupportedOperationException("Mail queue is not enabled");
        }

        Email email = new Email();
        email.getRecipients().add(new Recipient(Recipient.Type.TO, to));
        email.setFrom(from);
        email.setSubject(subject);
        email.setHtmlBody(body);
        try {
            mailQueue.enqueue(email);
        } catch (QueueException e) {
            throw new RuntimeException(e);
        }
    }

    public String getApplicationName() {
        return portofinoConfiguration.getString(PortofinoProperties.APP_NAME);
    }

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

    @Path("password")
    @PUT
    public void changePassword(@FormParam("oldPassword") String oldPassword, @FormParam("newPassword") String newPassword) {
        List<String> errorMessages = new ArrayList<>();
        if (!checkPasswordStrength(newPassword, errorMessages)) {
            for (String current : errorMessages) {
                RequestMessages.addErrorMessage(current);
            }
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        Subject subject = SecurityUtils.getSubject();
        Serializable principal = (Serializable) subject.getPrincipal();
        Serializable userId = portofinoRealm.getUserId(principal);
        try {
            portofinoRealm.changePassword(principal, oldPassword, newPassword);
            if(subject.isRemembered()) {
                UsernamePasswordToken usernamePasswordToken =
                        new UsernamePasswordToken(getRememberedUserName(principal), newPassword);
                usernamePasswordToken.setRememberMe(true);
                try {
                    subject.login(usernamePasswordToken);
                } catch (Exception e) {
                    logger.warn("User " + userId + " changed password but could not be subsequently authenticated", e);
                }
            }
        } catch (IncorrectCredentialsException e) {
            logger.warn("User {} password change: Incorrect credentials", userId);
            RequestMessages.addErrorMessage(ElementsThreadLocals.getText("wrong.password"));
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Password update failed for user " + userId, e);
            RequestMessages.addErrorMessage(ElementsThreadLocals.getText("password.change.failed"));
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected String getRememberedUserName(Serializable principal) {
        PortofinoRealm realm = ShiroUtils.getPortofinoRealm();
        return realm.getUsername(principal);
    }

    @Path("user/classAccessor")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    @Operation(summary = "The class accessor that describes the registration of a new user")
    public String describeNewUserClassAccessor() {
        ClassAccessor classAccessor = getNewUserClassAccessor();
        JSONStringer jsonStringer = new JSONStringer();
        ReflectionUtil.classAccessorToJson(classAccessor, jsonStringer);
        return jsonStringer.toString();
    }

    public ClassAccessor getNewUserClassAccessor() {
        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        return portofinoRealm.getSelfRegisteredUserClassAccessor();
    }

    @Path("user")
    @POST
    public void createUser() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.getPrincipal() != null) {
            logger.debug("Already logged in");
            throw new WebApplicationException(Response.Status.CONFLICT);
        }

        String confirmationUrl = context.getRequest().getParameter("portofino:confirmationUrl");
        String siteNameOrAddress = context.getRequest().getParameter("portofino:siteNameOrAddress");
        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        Form signUpForm = setupSignUpForm();
        signUpForm.readFromRequest(context.getRequest());
        if (!signUpForm.validate()) {
            RequestMessages.addErrorMessage(ElementsThreadLocals.getText("please.correct.the.errors.before.proceeding"));
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        List<String> errorMessages = new ArrayList<String>();
        if (!validateSignUpPassword(signUpForm, errorMessages)) {
            for (String current : errorMessages) {
                RequestMessages.addErrorMessage(current);
            }
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            Object user = portofinoRealm.getSelfRegisteredUserClassAccessor().newInstance();
            signUpForm.writeToObject(user);
            String token = portofinoRealm.saveSelfRegisteredUser(user);
            String body = getConfirmSignUpEmailBody(siteNameOrAddress, confirmationUrl.replace("TOKEN", token));
            String from = portofinoConfiguration.getString(
                    PortofinoProperties.MAIL_FROM, "example@example.com");
            sendMail(from, portofinoRealm.getEmail((Serializable) user), ElementsThreadLocals.getText("confirm.signup"), body);
        } catch (ExistingUserException e) {
            RequestMessages.addErrorMessage(ElementsThreadLocals.getText("a.user.with.the.same.username.already.exists"));
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error during sign-up", e);
            throw new WebApplicationException(e);
        }
    }

    public static class ConfirmUserRequest {
        public String token;
    }

    @Path("user/:confirm")
    @POST
    public void confirmUser(ConfirmUserRequest request) {
        SignUpToken token = new SignUpToken(request.token);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
        } catch (AuthenticationException e) {
            String errMsg = ElementsThreadLocals.getText("the.sign.up.confirmation.link.is.no.longer.active");
            RequestMessages.addErrorMessage(errMsg);
            logger.warn(errMsg, e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

    protected Form setupSignUpForm() {
        FormBuilder formBuilder = new FormBuilder(getNewUserClassAccessor())
                .configMode(Mode.CREATE)
                .configReflectiveFields();
        return formBuilder.build();
    }

    protected boolean validateSignUpPassword(Form signUpForm, List<String> errorMessages) {
        return true;
    }

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

}
