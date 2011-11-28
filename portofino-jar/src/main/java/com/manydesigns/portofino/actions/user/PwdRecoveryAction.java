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
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.database.QueryUtils;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.email.EmailUtils;
import com.manydesigns.portofino.system.model.email.EmailBean;
import com.manydesigns.portofino.system.model.users.User;
import org.apache.commons.configuration.Configuration;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PwdRecoveryAction extends AbstractActionBean implements LoginUnAware{
    public static final String copyright
            = "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    public static final Logger logger =
        LoggerFactory.getLogger(PwdRecoveryAction.class);

    public String email;

    public String execute(){
        return INPUT;
    }

    public String send(){
        User user;
        Session session = application.getSession("portofino");
        try {

            user = application.findUserByEmail(email);
            if (user==null){
                SessionMessages.addErrorMessage("email non esistente");
                return INPUT;
            }
            user.tokenGenerator();
            session.update("users", user);
            HttpServletRequest req = context.getRequest();
            String port = (req.getServerPort()!=0)?":"+req.getServerPort():"";


            String url = MessageFormat.format("{0}://{1}{2}{3}?token={4}",
                    req.getScheme(),
                    req.getServerName(),
                    port,
                    com.manydesigns.elements.util.Util.getAbsoluteUrl(req,
                            "user/LostPasswordChange.action"),
                    user.getToken());
            String from = portofinoConfiguration.getString(
                    PortofinoProperties.MAIL_SMTP_SENDER);
            String subject = "Password recovery";
            String body = new StringBuilder().append("Someone has requested a reset of your password, ")
                    .append("if it isn't you simply ignore this email.\n")
                    .append("othrewise go to this url ")
                    .append(url)
                    .append(" to insert a new one. \n\n")
                    .append("Thank you.").toString();
            EmailBean emailBean = new EmailBean(subject, body, email , from);
            session.save(EmailUtils.EMAILQUEUE_ENTITY, emailBean);
            session.getTransaction().commit();
            SessionMessages.addInfoMessage("An email was sent to your address. " +
                    "Please check your email.");
            QueryUtils.commit(application, "portofino");
            return SUCCESS;
        } catch (Exception e) {
            final String errore = "Errore nella verifica della email. " +
                    "L'email non è stata inviata";
            SessionMessages.addErrorMessage(
                    errore);
            logger.warn(errore, e);
            return INPUT;
        }
    }
}
