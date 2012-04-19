/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.mail.sender;

import com.manydesigns.mail.queue.MailParseException;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.queue.model.Attachment;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import org.apache.commons.mail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.FileDataSource;
import javax.mail.IllegalWriteException;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.internet.ParseException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DefaultMailSender implements MailSender {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

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
        List<String> idsToMarkAsSent = new ArrayList<String>();
        int pollIntervalMultiplier = 1;
        while (alive) {
            long now = System.currentTimeMillis();
            List<String> ids;
            try {
                ids = queue.getEnqueuedEmailIds();
            } catch (Throwable e) {
                logger.error("Couldn't read email queue", e);
                continue;
            }
            int serverErrors = 0;
            for(String id : ids) {
                if(idsToMarkAsSent.contains(id)) {
                    logger.info("Mail with id {} already sent but mark failed, retrying", id);
                    try {
                        queue.markSent(id);
                        idsToMarkAsSent.remove(id);
                    } catch (Throwable e) {
                        logger.error("Couldn't mark mail as sent", e);
                    }
                    continue;
                }
                Email email;
                try {
                    email = queue.loadEmail(id);
                } catch (MailParseException e) {
                    logger.error("Mail with id " + id + " is corrupted, marking as failed", e);
                    markFailed(id, e);
                    continue;
                } catch (Throwable e) {
                    logger.error("Unexpected error loading mail with id " + id + ", skipping", e);
                    continue;
                }
                if(email != null) {
                    try {
                        logger.info("Sending email with id {}", id);
                        send(email);
                        logger.info("Email with id {} sent, marking", id);
                        queue.markSent(id);
                    } catch (EmailException e) {
                        Throwable cause = e.getCause();
                        if(cause instanceof ParseException ||
                           cause instanceof IllegalWriteException ||
                           cause instanceof MethodNotSupportedException) {
                            markFailed(id, cause);
                        } else if(cause instanceof MessagingException) {
                            logger.warn("Mail not sent due to known server error, NOT marking as failed", e);
                            serverErrors++;
                        } else {
                            markFailed(id, e);
                        }
                    } catch (Throwable e) {
                        logger.error("Couldn't mark mail as sent", e);
                        idsToMarkAsSent.add(id);
                    }
                }
            }
            if(serverErrors > 0) {
                if(pollIntervalMultiplier < 10) {
                    pollIntervalMultiplier++;
                    logger.debug("{} server errors, increased poll interval multiplier to {}",
                            serverErrors, pollIntervalMultiplier);
                }
            } else {
                pollIntervalMultiplier = 1;
                logger.debug("No server errors, poll interval multiplier reset");
            }
            long sleep = pollInterval * pollIntervalMultiplier - (System.currentTimeMillis() - now);
            if(sleep > 0) {
                logger.debug("Sleeping for {}ms", sleep);
                Thread.sleep(sleep);
            }
        }
    }

    protected void markFailed(String id, Throwable e) {
        logger.error("Unrecognized error while sending mail, marking as failed", e);
        try {
            queue.markFailed(id);
        } catch (Throwable error) {
            logger.warn("Couldn't mark mail with id " + id +
                        " as failed; it will probably fail again", error);
        }
    }

    protected void send(Email emailBean) throws EmailException {
        logger.debug("Entering send(Email)");
        org.apache.commons.mail.Email email;
        String textBody = emailBean.getTextBody();
        String htmlBody = emailBean.getHtmlBody();
        if(null == htmlBody) {
            if(emailBean.getAttachments().isEmpty()) {
                SimpleEmail simpleEmail = new SimpleEmail();
                simpleEmail.setMsg(textBody);
                email = simpleEmail;
            } else {
                MultiPartEmail multiPartEmail = new MultiPartEmail();
                multiPartEmail.setMsg(textBody);
                for(Attachment attachment : emailBean.getAttachments()) {
                    EmailAttachment emailAttachment = new EmailAttachment();
                    emailAttachment.setName(attachment.getName());
                    emailAttachment.setDisposition(attachment.getDisposition());
                    emailAttachment.setDescription(attachment.getDescription());
                    emailAttachment.setPath(attachment.getFilePath());
                    multiPartEmail.attach(emailAttachment);
                }
                email = multiPartEmail;
            }
        } else {
            HtmlEmail htmlEmail =  new HtmlEmail();
            htmlEmail.setHtmlMsg(htmlBody);
            if(textBody != null) {
                htmlEmail.setTextMsg(textBody);
            }
            for(Attachment attachment : emailBean.getAttachments()) {
                if(!attachment.isEmbedded()) {
                    EmailAttachment emailAttachment = new EmailAttachment();
                    emailAttachment.setName(attachment.getName());
                    emailAttachment.setDisposition(attachment.getDisposition());
                    emailAttachment.setDescription(attachment.getDescription());
                    emailAttachment.setPath(attachment.getFilePath());
                    htmlEmail.attach(emailAttachment);
                } else {
                    FileDataSource dataSource = new FileDataSource(new File(attachment.getFilePath()));
                    htmlEmail.embed(dataSource, attachment.getName(), attachment.getContentId());
                }
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
        logger.debug("Exiting send(Email)");
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
