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

package com.manydesigns.mail.sender;

import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DefaultMailSender implements MailSender {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected final MailQueue queue;
    protected boolean alive;
    protected int pollInterval = 1000;

    protected String server = "localhost";
    protected int port = 25;
    protected boolean ssl = false;
    protected String login;
    protected String password;

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMailSender.class);

    public DefaultMailSender(MailQueue queue) {
        this.queue = queue;
    }

    public void run() {
        alive = true;
        try {
            mainLoop();
        } catch (InterruptedException e) {
            stop();
        }
    }

    protected void mainLoop() throws InterruptedException {
        while (alive) {
            List<String> ids = queue.getEnqueuedEmailIds();
            for(String id : ids) {
                Email email = queue.loadEmail(id);
                if(email != null) {
                    try {
                        send(email);
                        queue.markSent(id);
                    } catch (EmailException e) {
                        Throwable cause = e.getCause();
                        if(cause instanceof AuthenticationFailedException ||
                           cause instanceof MessagingException) {
                            logger.warn("Mail not sent due to server error, NOT marking as failed", e);
                        } else {
                            logger.error("Unrecognized error while sending mail, marking as failed", e);
                            queue.markFailed(id);
                        }
                    }
                }
            }
            Thread.sleep(pollInterval);
        }
    }

    protected void send(Email emailBean) throws EmailException {
        org.apache.commons.mail.Email email;
        String textBody = emailBean.getTextBody();
        String htmlBody = emailBean.getHtmlBody();
        if(null == htmlBody) {
            SimpleEmail simpleEmail = new SimpleEmail();
            simpleEmail.setMsg(textBody);
            email = simpleEmail;
        } else {
            HtmlEmail htmlEmail =  new HtmlEmail();
            htmlEmail.setHtmlMsg(htmlBody);
            if(textBody != null) {
                htmlEmail.setTextMsg(textBody);
            }
            email = htmlEmail;
        }

        if (null != login && null != password) {
            email.setAuthenticator(new DefaultAuthenticator(login, password));
        }
        email.setHostName(server);
        email.setSmtpPort(port);
        email.setSubject(emailBean.getSubject());
        email.setFrom(emailBean.getFrom());

        for(Recipient recipient : emailBean.getRecipients()) {
            switch (recipient.getType()) {
                case TO:
                    email.addTo(recipient.getAddress());
                    break;
                case CC:
                    email.addCc(recipient.getAddress());
                    break;
                case BCC:
                    email.addBcc(recipient.getAddress());
                    break;
            }
        }
        email.setSSL(ssl);
        email.setSslSmtpPort(port + "");
        email.send();
    }

    public void stop() {
        alive = false;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAlive() {
        return alive;
    }
}
