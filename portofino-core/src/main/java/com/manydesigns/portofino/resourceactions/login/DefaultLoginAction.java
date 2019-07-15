/*
* Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.resourceactions.ResourceAction;
import com.manydesigns.portofino.resourceactions.ResourceActionName;
import com.manydesigns.portofino.resourceactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.rest.actions.user.LoginAction;
import com.manydesigns.portofino.security.SecurityLogic;
import com.manydesigns.portofino.shiro.JSONWebToken;
import com.manydesigns.portofino.shiro.JWTFilter;
import com.manydesigns.portofino.shiro.PortofinoRealm;
import com.manydesigns.portofino.shiro.ShiroUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONStringer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
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
            "Copyright (C) 2005-2019 ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    @Autowired
    public MailQueue mailQueue;

    //--------------------------------------------------------------------------
    // ResourceAction implementation
    //--------------------------------------------------------------------------

    @POST
    @Produces("application/json")
    public String login(@FormParam("username") String username, @FormParam("password") String password)
            throws AuthenticationException {
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isAuthenticated()) try {
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            usernamePasswordToken.setRememberMe(false);
            subject.login(usernamePasswordToken);
            logger.info("User {} login", ShiroUtils.getUserId(subject));
            Object principal = subject.getPrincipal();
            subject.logout();
            PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
            String jwt = portofinoRealm.generateWebToken(principal);
            subject.login(new JSONWebToken(jwt));
            return userInfo(subject, portofinoRealm, jwt);
        } catch (AuthenticationException e) {
            logger.warn("Login failed for '" + username + "': " + e.getMessage(), e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return checkJWT();
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
        Session session = subject.getSession(true);
        JSONStringer stringer = new JSONStringer();
        stringer.
            object().
                key("portofinoSessionId").value(session.getId()).
                key("userId").value(ShiroUtils.getUserId(subject)).
                key("displayName").value(portofinoRealm.getUserPrettyName((Serializable) subject.getPrincipal())).
                key("administrator").value(administrator).
                key("groups").value(portofinoRealm.getGroups(subject.getPrincipal())).
                key("jwt").value(jwt).
            endObject();
        return stringer.toString();
    }

    protected void sendForgotPasswordEmail(String from, String to, String subject, String body) {
        sendMail(from, to, subject, body);
    }

    protected void sendSignupConfirmationEmail(String from, String to, String subject, String body) {
        sendMail(from, to, subject, body);
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
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
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
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
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
    public String describeClassAccessor() {
        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        ClassAccessor classAccessor = portofinoRealm.getSelfRegisteredUserClassAccessor();
        JSONStringer jsonStringer = new JSONStringer();
        ReflectionUtil.classAccessorToJson(classAccessor, jsonStringer);
        return jsonStringer.toString();
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
