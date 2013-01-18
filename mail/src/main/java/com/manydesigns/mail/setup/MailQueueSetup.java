/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.mail.setup;

import com.manydesigns.mail.queue.FileSystemMailQueue;
import com.manydesigns.mail.queue.LockingMailQueue;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.sender.DefaultMailSender;
import com.manydesigns.mail.sender.MailSender;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Performs standard setup using mail.properties
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MailQueueSetup {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(MailQueueSetup.class);

    protected MailQueue mailQueue;
    protected MailSender mailSender;
    protected Configuration mailConfiguration;

    public MailQueueSetup() {}

    public MailQueueSetup(Configuration mailConfiguration) {
        this.mailConfiguration = mailConfiguration;
    }

    public void setup() {
        mailConfiguration = new CompositeConfiguration();
        try {
            PropertiesConfiguration propertiesConfiguration =
                    new PropertiesConfiguration(MailProperties.PROPERTIES_RESOURCE);
            ((CompositeConfiguration) mailConfiguration).addConfiguration(propertiesConfiguration);
        } catch (Throwable e) {
            logger.info("{} not found. Mail queue not enabled.", MailProperties.PROPERTIES_RESOURCE);
            logger.debug("Error loading configuration", e);
            return;
        }

        boolean mailEnabled = mailConfiguration.getBoolean(MailProperties.MAIL_ENABLED, false);
        if (mailEnabled) {
            String mailHost = mailConfiguration.getString(MailProperties.MAIL_SMTP_HOST);
            if (null == mailHost) {
                logger.error("Mail is enabled but smtp server not set in portofino-custom.properties");
            } else {
                logger.info("Mail is enabled, starting sender");
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
                //TODO rendere configurabile
                mailQueue = new LockingMailQueue(new FileSystemMailQueue(new File(mailQueueLocation)));
                logger.info("Mail queue location: {}", mailQueueLocation);
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
