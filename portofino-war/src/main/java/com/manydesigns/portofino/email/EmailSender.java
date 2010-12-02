/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.system.model.email.EmailBean;
import org.apache.commons.mail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class EmailSender implements Runnable{

    protected final boolean ssl;
    protected final String login;
    protected final String password;
    protected final String server;
    protected final int port;
    protected final EmailBean emailBean;
    protected static final Logger logger =
            LoggerFactory.getLogger(EmailSender.class);



    //Costruttore con proprietà da inserire
    public EmailSender(String server, EmailBean emailBean, int port, boolean ssl,
                 String login, String password) {
        this.emailBean = emailBean;
        this.server = server;
        this.port = port;
        this.ssl = ssl;
        this.login = login;
        this.password = password;
    }

    //Costruttore che prende le proprietà dal portofino.properties
    public EmailSender(EmailBean emailBean) {
        this.emailBean = emailBean;
        this.server = (String) PortofinoProperties.getProperties()
                    .getProperty(PortofinoProperties.MAIL_SMTP_HOST);
        this.port = Integer.parseInt((String)PortofinoProperties.getProperties()
                    .getProperty(PortofinoProperties.MAIL_SMTP_PORT, "25"));
        this.ssl = Boolean.parseBoolean((String) PortofinoProperties.getProperties()
                    .getProperty(PortofinoProperties.MAIL_SMTP_SSL_ENABLED));
        this.login = (String) PortofinoProperties.getProperties()
                    .getProperty(PortofinoProperties.MAIL_SMTP_LOGIN);
        this.password = (String) PortofinoProperties.getProperties()
                    .getProperty(PortofinoProperties.MAIL_SMTP_PASSWORD);
    }

    public void run() {

        try {

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
            emailBean.setState(EmailUtils.SENT);
            EmailTask.successQueue.add(this);          

        } catch (Throwable e) {
            logger.warn("Cannot send email with id " + emailBean.getId(), e);
            emailBean.setState(EmailUtils.REJECTED);
            EmailTask.rejectedQueue.add(this);
        }
    }

    public EmailBean getEmailBean() {
        return emailBean;
    }
}