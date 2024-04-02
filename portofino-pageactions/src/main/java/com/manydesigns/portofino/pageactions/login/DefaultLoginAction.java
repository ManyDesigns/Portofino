/*
* Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.login;

import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.queue.QueueException;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.user.LoginAction;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.DispatchElement;
import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.modules.MailModule;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.shiro.JSONWebToken;
import com.manydesigns.portofino.shiro.JWTFilter;
import com.manydesigns.portofino.shiro.PortofinoRealm;
import com.manydesigns.portofino.shiro.ShiroUtils;
import net.sourceforge.stripes.action.Resolution;
import org.apache.commons.collections.MultiMap;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONStringer;
import org.jsoup.Jsoup;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.Serializable;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@ScriptTemplate("script_template.groovy")
@PageActionName("Login")
public class DefaultLoginAction extends LoginAction implements PageAction {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    /**
     * The PageInstance property. Injected.
     */
    public PageInstance pageInstance;

    @Inject(MailModule.MAIL_QUEUE)
    public MailQueue mailQueue;

    //--------------------------------------------------------------------------
    // PageAction implementation
    //--------------------------------------------------------------------------

    @Override
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
            throw new WebApplicationException(HttpServletResponse.SC_UNAUTHORIZED);
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

    @Override
    protected void sendForgotPasswordEmail(String from, String to, String subject, String body) {
        sendMail(from, to, subject, body);
    }

    @Override
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
        email.setTextBody( Jsoup.parse(body).text().toString() );
        try {
            mailQueue.enqueue(email);
        } catch (QueueException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getApplicationName() {
        return portofinoConfiguration.getString(PortofinoProperties.APP_NAME);
    }

    @Override
    public Resolution preparePage() {
        return null;
    }

    @Override
    public MultiMap initEmbeddedPageActions() {
        return null;
    }

    @Override
    public PageInstance getPageInstance() {
        return pageInstance;
    }

    @Override
    public void setPageInstance(PageInstance pageInstance) {
        this.pageInstance = pageInstance;
    }

    @Override
    public DispatchElement consumePathFragment(String pathFragment) {
        return null;
    }

    @Override
    public String getPageTemplate() {
        return pageInstance.getLayout().getTemplate();
    }

    @Override
    public PageAction getParent() {
        return pageInstance.getParent().getActionBean();
    }

}
