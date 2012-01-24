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
package com.manydesigns.portofino.email;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.system.model.email.EmailBean;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.mail.*;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class EmailSender implements Runnable{
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected final EmailBean emailBean;
    protected final @NotNull Application application;
    protected static final Logger logger =
            LoggerFactory.getLogger(EmailSender.class);

    //Costruttore che prende le proprietà dal portofino.properties
    public EmailSender(@NotNull Application application, EmailBean emailBean) {
        this.application = application;
        this.emailBean = emailBean;
    }

    public void run() {
        Session session = application.getSystemSession();
        try {
            Configuration configuration = application.getPortofinoProperties();
            String server = configuration
                    .getString(PortofinoProperties.MAIL_SMTP_HOST);
            int port = configuration.getInt(
                    PortofinoProperties.MAIL_SMTP_PORT, 25);
            boolean ssl = configuration.getBoolean(
                    PortofinoProperties.MAIL_SMTP_SSL_ENABLED, false);
            String login = configuration.getString(
                    PortofinoProperties.MAIL_SMTP_LOGIN);
            String password = configuration.getString(
                    PortofinoProperties.MAIL_SMTP_PASSWORD);
            boolean keepSent = configuration.getBoolean(
                    PortofinoProperties.KEEP_SENT, false);


            Email email;
            if(null == emailBean.getAttachmentPath()) {
                email = new SimpleEmail();
            } else {
                email =  new MultiPartEmail();
                EmailAttachment attachment = new EmailAttachment();
                attachment.setPath(emailBean.getAttachmentPath());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription(emailBean.getAttachmentDescription());
                attachment.setName(emailBean.getAttachmentName());
                ((MultiPartEmail) email).attach(attachment);
            }

            if (null!=login && null!=password ) {
                email.setAuthenticator(new DefaultAuthenticator(login, password));
            }
            email.setHostName(server);
            email.setSmtpPort(port);
            email.setFrom(emailBean.getFrom());
            email.setSubject(emailBean.getSubject());
            email.setMsg(emailBean.getBody());
            email.addTo(emailBean.getTo());
            email.setTLS(ssl);
            email.send();

            if (keepSent) {
                emailBean.setState(EmailUtils.SENT);
                session.update(EmailUtils.EMAILQUEUE_ENTITY, emailBean);
            } else {
                session.delete(EmailUtils.EMAILQUEUE_ENTITY, emailBean);
            }
            session.getTransaction().commit();
        } catch (Throwable e) {
            //TODO gestire HibernateException
            logger.warn("Cannot send email with id " + emailBean.getId(), e);
            emailBean.setState(EmailUtils.TOBESENT);
            if (application != null){
                session.update(EmailUtils.EMAILQUEUE_ENTITY, emailBean);
                session.getTransaction().commit();
            }
        }finally {
            if (application != null)
            application.closeSessions();
        }
    }

    public EmailBean getEmailBean() {
        return emailBean;
    }
}