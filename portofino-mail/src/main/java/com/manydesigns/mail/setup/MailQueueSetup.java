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

package com.manydesigns.mail.setup;

import com.manydesigns.mail.queue.FileSystemMailQueue;
import com.manydesigns.mail.queue.LockingMailQueue;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.sender.DefaultMailSender;
import com.manydesigns.mail.sender.MailSender;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Performs standard setup using a Configuration.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MailQueueSetup {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(MailQueueSetup.class);

    protected MailQueue mailQueue;
    protected MailSender mailSender;
    protected final Configuration mailConfiguration;

    public MailQueueSetup(Configuration mailConfiguration) {
        this.mailConfiguration = mailConfiguration;
    }

    public void setup() {
        boolean mailEnabled = mailConfiguration.getBoolean(MailProperties.MAIL_ENABLED, false);
        if (mailEnabled) {
            String mailHost = mailConfiguration.getString(MailProperties.MAIL_SMTP_HOST);
            if (null == mailHost) {
                logger.error("Mail queue is enabled but smtp server not set in configuration");
            } else {
                logger.info("Mail queue is enabled, starting sender");
                int port = mailConfiguration.getInt(
                        MailProperties.MAIL_SMTP_PORT, 25);
                boolean ssl = mailConfiguration.getBoolean(
                        MailProperties.MAIL_SMTP_SSL_ENABLED, false);
                boolean tls = mailConfiguration.getBoolean(
                        MailProperties.MAIL_SMTP_TLS_ENABLED, false);
                String login = mailConfiguration.getString(
                        MailProperties.MAIL_SMTP_LOGIN);
                String password = mailConfiguration.getString(
                        MailProperties.MAIL_SMTP_PASSWORD);
                boolean keepSent = mailConfiguration.getBoolean(
                        MailProperties.MAIL_KEEP_SENT, false);

                String mailQueueLocation =
                        mailConfiguration.getString(MailProperties.MAIL_QUEUE_LOCATION);
                logger.info("Mail queue location: {}", mailQueueLocation);
                //TODO rendere configurabile
                mailQueue = new LockingMailQueue(new FileSystemMailQueue(new File(mailQueueLocation)));
                mailQueue.setKeepSent(keepSent);
                mailSender = new DefaultMailSender(mailQueue);
                mailSender.setServer(mailHost);
                mailSender.setLogin(login);
                mailSender.setPassword(password);
                mailSender.setPort(port);
                mailSender.setSsl(ssl);
                mailSender.setTls(tls);

                logger.info("Mail sender started");
            }
        } else {
            logger.info("Mail queue is not enabled");
        }
    }

    public MailQueue getMailQueue() {
        return mailQueue;
    }

    public MailSender getMailSender() {
        return mailSender;
    }

    public Configuration getMailConfiguration() {
        return mailConfiguration;
    }
}
