/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.tt

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.servlet.ByteArrayServletOutputStream
import com.manydesigns.elements.servlet.MutableHttpServletRequest
import com.manydesigns.elements.servlet.MutableHttpServletResponse
import com.manydesigns.mail.queue.MailQueue
import com.manydesigns.mail.queue.model.Email
import com.manydesigns.mail.queue.model.Recipient
import com.manydesigns.mail.queue.model.Recipient.Type
import com.manydesigns.mail.setup.MailProperties
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.i18n.I18nUtils
import com.manydesigns.portofino.modules.BaseModule
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.modules.MailModule
import com.manydesigns.portofino.pageactions.activitystream.ActivityItem
import com.manydesigns.portofino.persistence.Persistence
import javax.servlet.RequestDispatcher
import javax.servlet.ServletContext
import org.apache.commons.configuration.Configuration
import org.hibernate.Criteria
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@DisallowConcurrentExecution
public class NotificationsJob implements Job {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";


    public final static Logger logger =
            LoggerFactory.getLogger(NotificationsJob.class);

    public static String PROJECT_ACTIVTY_SQL = TtUtils.ACTIVITY_SQL +
            "WHERE act.project IS NOT NULL AND act.notifications_sent is null ORDER BY act.id ASC";

    @Inject(BaseModule.SERVLET_CONTEXT)
    public ServletContext servletContext;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    @Inject(DatabaseModule.PERSISTENCE)
    Persistence persistence;

    @Inject(MailModule.MAIL_QUEUE)
    MailQueue mailQueue;

    void execute(JobExecutionContext context) {
        List<ActivityItem> activityItems = new ArrayList<ActivityItem>();
        try {
            ElementsThreadLocals.setupDefaultElementsContext();

            MutableHttpServletRequest request =
                new MutableHttpServletRequest();
            ElementsThreadLocals.setHttpServletRequest(request);
            I18nUtils.setupTextProvider(servletContext, request);
            request.setRequestURI("http://localhost:8080/demo-tt/");
            request.setContextPath("/demo-tt");

            Session session = persistence.getSession("tt");

            logger.debug("Find project activities to be notified");
            List items = session.createSQLQuery(PROJECT_ACTIVTY_SQL).list();
            String keyPrefix = "project.";
            Locale locale = Locale.getDefault();

            TtUtils.populateActivityItems(items, activityItems, keyPrefix, locale, null);

            int i = 0;
            for (Object current : items) {
                logger.info("Notifying activity #{}", current[0]);

                request.setAttribute("activityItem", activityItems.get(i));

                ByteArrayServletOutputStream stream =
                    new ByteArrayServletOutputStream();
                MutableHttpServletResponse response =
                        new MutableHttpServletResponse(stream)

                RequestDispatcher requestDispatcher =
                        servletContext.getRequestDispatcher("/jsp/notifications/project-activity.jsp");
                requestDispatcher.include(request, response);
                response.flushBuffer();

                ByteArrayOutputStream baos = stream.getByteArrayOutputStream();
                String htmlBody = baos.toString(response.getCharacterEncoding());
                logger.info("Html body: {}", htmlBody);

                String subject;
                if (current[14] == null) {
                    subject = "${current[12]}: ${current[13]}";
                } else {
                    subject = "${current[12]}-${current[14]}: ${current[15]}";
                }

                notifyActivity(session, current, subject, htmlBody);
                Date now = new Date();
                Object activity = session.load("activity", (Long)current[0]);
                activity.notifications_sent = now;
                session.update("activity", (Object)activity);
                i++;
            }
            session.getTransaction().commit();
        } finally {
            persistence.closeSessions();
            ElementsThreadLocals.removeElementsContext();
        }

    }

    void notifyActivity(Session session, Object activity, String subject, String htmlBody) {

        Criteria criteria = session.createCriteria("members");
        criteria.add(Restrictions.eq("project", activity[12]));
        criteria.add(Restrictions.eq("notifications", true));
//        criteria.add(Restrictions.ne("user_", activity.user_));
        List membersToBeNotified = criteria.list();
        for (Object current : membersToBeNotified) {
            Object user = current.fk_member_user;
            logger.info("Notifying user {}", user.email);

            Email email = new Email();

            email.subject = subject;
            email.htmlBody = htmlBody;
            Recipient recipient = new Recipient(Type.TO, user.email);
            email.recipients.add(recipient);
            String sender = portofinoConfiguration.getString(MailProperties.MAIL_SMTP_LOGIN);
            email.from = sender;
            mailQueue.enqueue(email);
        }
    }

}
