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

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.context.TableCriteria;
import com.manydesigns.portofino.system.model.email.EmailBean;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UserUtils;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/


public class EmailTask extends TimerTask {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected static final int N_THREADS = 5;
    protected static ExecutorService outbox = Executors.newFixedThreadPool
            (N_THREADS);
    /*protected static final ConcurrentLinkedQueue<EmailSender> successQueue
            = new ConcurrentLinkedQueue<EmailSender>();
    protected static final ConcurrentLinkedQueue<EmailSender> rejectedQueue
            = new ConcurrentLinkedQueue<EmailSender>();*/
    protected final POP3Client client;
    protected static final Logger logger =
            LoggerFactory.getLogger(TimerTask.class);
    protected final Application application;
    private static final String USERTABLE = UserUtils.USERTABLE;


    public EmailTask(Application application) {
        this.application = application;
        //Setto il client smtp per il bouncing
        Configuration configuration = application.getPortofinoProperties();
        String popHost = configuration.getString(
                PortofinoProperties.MAIL_POP3_HOST, "127.0.0.1");
        String protocol = configuration.getString(
                PortofinoProperties.MAIL_POP3_PROTOCOL, "pop3");
        int popPort = configuration.getInt(
                PortofinoProperties.MAIL_POP3_PORT, 25);
        String popLogin = configuration.getString(
                PortofinoProperties.MAIL_POP3_LOGIN);
        String popPassword = configuration.getString(
                PortofinoProperties.MAIL_POP3_PASSWORD);
        boolean bounceEnabled = configuration.getBoolean(
                PortofinoProperties.MAIL_BOUNCE_ENABLED, false);
        boolean sslEnabled = configuration.getBoolean(
                PortofinoProperties.MAIL_POP3_SSL_ENABLED, false);
        if (bounceEnabled &&
                popHost != null && protocol != null && popLogin != null
                && popPassword != null) {
            if (sslEnabled) {
                client = new POP3SSLClient(popHost, protocol, popPort, popLogin,
                        popPassword);
            } else {
                client = new POP3SimpleClient(popHost, protocol, popPort, popLogin,
                        popPassword);
            }
        } else {
            client = null;
        }
    }

    public static void stop() {
        outbox.shutdownNow();
    }


    public void run() {
        try {
            createQueue();
            checkBounce();
        } finally {
            application.closeSessions();
        }
    }

    public synchronized void createQueue() {
        try {
            ClassAccessor accessor = application.getTableAccessor(
                    EmailUtils.EMAILQUEUE_TABLE);
            Table table = application.getModel()
                    .findTableByQualifiedName(EmailUtils.EMAILQUEUE_TABLE);
            TableCriteria criteria = new TableCriteria(table);
            criteria.eq(accessor.getProperty("state"), EmailUtils.TOBESENT);
            List<Object> emails = application.getObjects(
                    criteria.eq(accessor.getProperty("state"),
                            EmailUtils.TOBESENT));
            for (Object obj : emails) {
                EmailSender emailSender = new EmailSender(application,
                        (EmailBean) obj);
                EmailBean email = emailSender.getEmailBean();
                try {
                    email.setState(EmailUtils.SENDING);
                    application.saveObject(EmailUtils.EMAILQUEUE_TABLE, email);
                    application.commit(EmailUtils.PORTOFINO);
                } catch (Throwable e) {
                    logger.warn("cannot store email state", e);
                }
                outbox.submit(emailSender);
            }
        } catch (Throwable e) {
            logger.warn("cannot create emailQueue", e);
        }
    }


    private synchronized void checkBounce() {
        if (client != null) {
            Set<String> emails = client.read();
            for (String email : emails) {
                incrementBounce(email);
            }
        }
    }

    private void incrementBounce(String email) {
        try {
            Table table = application.getModel()
                    .findTableByQualifiedName(EmailUtils.EMAILQUEUE_TABLE);
            TableCriteria criteria = new TableCriteria(table);

            ClassAccessor accessor = application.getTableAccessor(USERTABLE);
            List<Object> users = application.getObjects(
                    criteria.gt(accessor.getProperty("email"), email));
            if (users.size() == 0) {
                logger.warn("no user found for email {}", email);
                return;
            }
            User user = (User) users.get(0);
            Integer value = user.getBounced();
            if (null == value) {
                value = 1;
            } else {
                value++;
            }
            user.setBounced(value);
            application.saveObject(USERTABLE, user);
        } catch (NoSuchFieldException e) {
            logger.warn("cannot increment bounce for user", e);
        }
    }
}
